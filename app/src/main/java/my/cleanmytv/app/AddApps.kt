package my.cleanmytv.app

import android.app.Application
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID
import my.cleanmytv.app.ui.AppRoot

class AddAppsModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app
    private val prefs = app.getSharedPreferences("cleanmytv", 0)
    var host by mutableStateOf(prefs.getString("host", "") ?: "")
    var port by mutableStateOf(prefs.getString("port", "5555") ?: "5555")
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set
    var packageDetails by mutableStateOf("")
        private set
    private var apk: File? = null

    fun choose(uri: Uri) {
        if (busy) return
        busy = true
        message = ""
        packageDetails = ""
        apk?.delete()
        apk = null
        viewModelScope.launch {
            var staged: File? = null
            try {
                val file = withContext(Dispatchers.IO) {
                    val dir = File(ctx.cacheDir, "tv-installer").apply { mkdirs() }
                    val target = File(dir, "${UUID.randomUUID()}.apk")
                    staged = target
                    val input = ctx.contentResolver.openInputStream(uri) ?: error("Fail tidak dapat dibuka / Cannot open file")
                    input.use { source -> target.outputStream().use { out ->
                        val buffer = ByteArray(65536)
                        var total = 0L
                        while (true) {
                            ensureActive()
                            val count = source.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= 200L * 1024 * 1024) { "Had APK 200 MB / APK limit 200 MB" }
                            out.write(buffer, 0, count)
                        }
                    } }
                    target
                }
                val info = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    ctx.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
                        ?: error("APK tidak sah. Pilih satu fail .apk, bukan XAPK/APKS / Invalid APK; split bundles are not supported")
                }
                packageDetails = "${info.packageName}\n${info.versionName ?: ""} • ${file.length() / 1024 / 1024} MB"
                apk = file
                staged = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message = e.message ?: "Gagal membaca APK / Cannot read APK"
            } finally {
                staged?.delete()
                busy = false
            }
        }
    }

    fun perform(install: Boolean) {
        if (busy) return
        val file = apk
        if (install && file == null) return
        val h = host.trim()
        val p = port.trim().toIntOrNull()
        if (!Regex("^[A-Za-z0-9.-]{1,253}$").matches(h) || p == null || p !in 1..65535) {
            message = "Semak IP dan port box / Check box IP and port"
            return
        }
        busy = true
        message = "Benarkan sambungan ADB pada TV. / Allow the ADB connection on your TV."
        viewModelScope.launch {
            try {
                require(Adb.portOpen(h, p)) { "Box tidak dapat dihubungi / Box is unreachable" }
                Adb.connect(ctx, h, p).use { tv ->
                    message = if (install) "Menghantar dan memasang APK… / Sending and installing APK…" else "Membuka Play Store pada TV… / Opening Play Store on TV…"
                    val result = withContext(Dispatchers.IO) {
                        if (install) {
                            val remote = "/data/local/tmp/cleanmytv-${UUID.randomUUID()}.apk"
                            try {
                                tv.dadb.push(requireNotNull(file), remote)
                                ensureActive()
                                val (code, output) = tv.run("pm install -r --user 0 $remote")
                                if (code == 0 && output.lineSequence().any { it.trim() == "Success" })
                                    "Berjaya dipasang pada box / Installed on box"
                                else "Pemasangan gagal / Installation failed:\n${output.take(1200)}"
                            } finally { runCatching { tv.sh("rm -f $remote") } }
                        } else {
                            val (code, output) = tv.run("am start -a android.intent.action.VIEW -d 'market://details?id=my.riqtech.waktusolat' -p com.android.vending")
                            if (code == 0 && !output.contains("Error", true) && !output.contains("Exception", true))
                                "Play Store dibuka pada TV. Pilih Install dengan remote. / Play Store opened on TV; select Install with your remote."
                            else "Play Store tidak dapat dibuka. Cari Waktu Solat oleh RIQTech di Play Store TV. / Cannot open Play Store; search for Waktu Solat by RIQTech on TV.\n${output.take(600)}"
                        }
                    }
                    message = result
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message = "Gagal / Failed: ${e.message ?: e.javaClass.simpleName}"
            } finally { busy = false }
        }
    }

    override fun onCleared() {
        apk?.delete()
        super.onCleared()
    }
}

@Composable
fun AddAppsRoot(vm: MainViewModel) {
    var open by remember { mutableStateOf(false) }
    var manage by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val model: AddAppsModel = viewModel()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val ms = vm.lang == Lang.MS
    fun t(a: String, b: String) = if (ms) a else b
    BackHandler(open) { if (!model.busy) open = false }
    BackHandler(!open && manage && vm.screen == Screen.CONNECT) { manage = false }
    if (!open) {
        if (vm.screen == Screen.CONNECT && !manage) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.systemBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    my.cleanmytv.app.ui.TopBar(vm.lang) { vm.changeLanguage(it) }
                    Text(t("Ruang TV anda.", "Your TV space."), style = MaterialTheme.typography.headlineLarge)
                    Text(t("Satu tempat untuk urus app, tambah pilihan dan pulihkan perubahan.", "One place to manage apps, add your favourites and restore changes."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(shape = RoundedCornerShape(28.dp)) {
                        Row(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF123F36), Color(0xFF267B61)))).padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(color = Color.White.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
                                    Text(t("RUANG PERANTI", "DEVICE SPACE"), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFFDBF4DA), style = MaterialTheme.typography.labelSmall)
                                }
                                Text(vm.host.ifBlank { t("TV anda,\npilihan anda.", "Your TV,\nyour choice.") }, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(t("Mulakan dengan sambungan Wi-Fi", "Start with a Wi-Fi connection"), color = Color(0xFFC7E4D5), style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(24.dp)) {
                                Box(Modifier.padding(12.dp)) { my.cleanmytv.app.ui.Logo(64.dp) }
                            }
                        }
                    }
                    Button(onClick = { manage = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text(t("Sambung TV Anda", "Connect your TV"), style = MaterialTheme.typography.titleMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("Pilihan untuk TV", "Tools for your TV"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("cleanMYTV", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                    HomeAction(t("Urus app", "Manage apps"), t("Semak dan nyahaktif app pada box", "Review and disable apps on your box"), "01") { manage = true }
                    HomeAction(t("Tambah app", "Add apps"), t("Waktu Solat dan pemasangan APK sendiri", "Waktu Solat and your own APK files"), "02") { model.host = vm.host; model.port = vm.port; open = true }
                    HomeAction(t("Sejarah & pulihkan", "History & restore"), t("Lihat perubahan dan aktifkan semula app", "Review changes and re-enable apps"), "03") { vm.openHistory() }
                    HorizontalDivider()
                    Text(t("Dibina untuk pilihan anda.", "Built around your choices."), style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { runCatching { uriHandler.openUri("https://sociabuzz.com/cleanmytv/tribe") } }) { Text(t("Sokong pembangunan cleanMYTV ↗", "Support cleanMYTV development ↗")) }
                }
            }
            return
        }
        Column(Modifier.fillMaxSize()) {
            if (vm.screen == Screen.CONNECT) {
                TextButton(onClick = { manage = false }) { Text(t("← Halaman utama", "← Home")) }
            }
            Box(Modifier.weight(1f)) { AppRoot(vm) }
        }
        return
    }
    var confirm by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) model.choose(uri)
    }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = { open = false }, enabled = !model.busy) { Text(t("Kembali", "Back")) }
            Text(t("Tambah app pada TV", "Add apps to TV"), style = MaterialTheme.typography.headlineSmall)
            Text(t("Telefon dan box mesti pada Wi-Fi yang sama. Hidupkan ADB pada box.", "Keep phone and box on the same Wi-Fi. Enable ADB on the box."))
            OutlinedTextField(model.host, { model.host = it }, label = { Text(t("IP box", "Box IP")) }, enabled = !model.busy, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(model.port, { model.port = it }, label = { Text("Port") }, enabled = !model.busy, singleLine = true)
            HorizontalDivider()
            Text("Waktu Solat — RIQTech", style = MaterialTheme.typography.titleLarge)
            Text(t("Buka halaman rasmi di Play Store box, kemudian pasang dengan remote. Paparan waktu solat ialah app berasingan; widget home screen bergantung pada launcher.", "Open the official listing in the box Play Store, then install with your remote. Prayer times run in a separate app; home widgets depend on your launcher."))
            Button(onClick = { model.perform(false) }, enabled = !model.busy) { Text(t("Buka Play Store pada TV", "Open Play Store on TV")) }
            HorizontalDivider()
            Text(t("Pasang APK sendiri", "Install your own APK"), style = MaterialTheme.typography.titleLarge)
            Text(t("Pilih satu fail APK pada telefon (maksimum 200 MB). Gunakan versi untuk Android TV. XAPK/APKS tidak disokong.", "Choose one APK on your phone (maximum 200 MB). Use an Android TV version. XAPK/APKS bundles are not supported."))
            OutlinedButton(onClick = { picker.launch(arrayOf("*/*")) }, enabled = !model.busy) { Text(t("Pilih fail APK", "Choose APK file")) }
            if (model.packageDetails.isNotEmpty()) {
                Text(model.packageDetails)
                Button(onClick = { confirm = true }, enabled = !model.busy) { Text(t("Pasang pada box", "Install on box")) }
            }
            if (model.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (model.message.isNotEmpty()) Text(model.message)
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text(t("Pasang pada box ini?", "Install on this box?")) },
        text = { Text("${model.host}:${model.port}\n${model.packageDetails}\n\n" + t("Pasang hanya APK daripada sumber yang anda percaya. App sedia ada mungkin dikemas kini. Kekalkan cleanMYTV terbuka sehingga selesai.", "Install only APKs from sources you trust. An existing app may be updated. Keep cleanMYTV open until finished.")) },
        confirmButton = { TextButton(onClick = { confirm = false; model.perform(true) }) { Text(t("Pasang", "Install")) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(t("Batal", "Cancel")) } }
    )
}

@Composable
private fun HomeAction(title: String, subtitle: String, number: String, onClick: () -> Unit) {
    val accent = when (number) { "01" -> Color(0xFF176B55); "02" -> Color(0xFF4765A8); else -> Color(0xFF9C4C30) }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = accent.copy(alpha = 0.10f), shape = RoundedCornerShape(16.dp)) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(when(number) { "01" -> "▦"; "02" -> "+"; else -> "↶" }, color = accent, style = MaterialTheme.typography.headlineMedium)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("→", color = MaterialTheme.colorScheme.primary)
        }
    }
}
