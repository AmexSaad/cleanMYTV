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
                ErrorReports.record("Tambah app / Add app", e.javaClass.simpleName)
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
                    if (result.startsWith("Pemasangan gagal") || result.startsWith("Play Store tidak")) ErrorReports.record("Tambah app / Add app", Regex("INSTALL_FAILED_[A-Z_]+").find(result)?.value ?: "INSTALL_OR_STORE_FAILED")
                    message = result
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ErrorReports.record("Tambah app / Add app", e.javaClass.simpleName)
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
    var custom by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var manage by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val model: AddAppsModel = viewModel()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val ms = vm.lang == Lang.MS
    fun t(a: String, b: String) = if (ms) a else b
    LaunchedEffect(vm.connected) { if (vm.connected) manage = false }
    LaunchedEffect(manage, open, custom, vm.connected) {
        while (!manage && !open && !custom && vm.connected) { delay(5000); vm.checkConnection() }
    }
    if (custom) {
        LauncherEditor(vm) { custom = false }
        return
    }
    BackHandler(open) { if (!model.busy) open = false }
    BackHandler(!open && manage && vm.screen == Screen.CONNECT) { manage = false }
    if (!open) {
        if ((vm.screen == Screen.CONNECT || vm.screen == Screen.REVIEW) && !manage) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.systemBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    my.cleanmytv.app.ui.TopBar(vm.lang, horizontalPadding = 0.dp) { vm.changeLanguage(it) }
                    Text(t("Ruang TV anda.", "Your TV space."), style = MaterialTheme.typography.headlineLarge)
                    Text(t("Satu tempat untuk urus app, tambah pilihan dan pulihkan perubahan.", "One place to manage apps, add your favourites and restore changes."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FC))) {
                        if (!vm.connected) androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(R.drawable.tv_hero),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        ) else Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            my.cleanmytv.app.ui.Logo(100.dp)
                        }
                        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (vm.connected) t("● TV disambungkan", "● TV connected") else t("TV belum disambungkan", "TV not connected"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if(vm.connected) Color(0xFF16704B) else Color(0xFF142039), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Text(if (vm.connected) vm.analysis?.info?.let { "${it.brand} ${it.model}\n${vm.host}" }.orEmpty() else t("Sambungkan TV anda ke rangkaian Wi-Fi yang sama untuk mula mengurusnya.", "Connect your TV to the same Wi-Fi to get started."), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = { manage = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) {
                                Text(if (vm.connected) t("Urus App", "Manage apps") else t("Sambung TV Anda", "Connect your TV"), fontWeight = FontWeight.Bold)
                            }
                            if(vm.connected) TextButton(onClick = { vm.goHome() }) { Text(t("Putuskan sambungan", "Disconnect")) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f).fillMaxHeight()) { HomeAction(t("Urus App", "Manage apps"), t("Semak dan nyahaktif app", "Review and disable apps"), "01") { manage = true } }
                        Box(Modifier.weight(1f).fillMaxHeight()) { HomeAction(t("Tambah App", "Add apps"), t("Waktu Solat & APK sendiri", "Waktu Solat & APK files"), "02") { model.host = vm.host; model.port = vm.port; open = true } }
                        Box(Modifier.weight(1f).fillMaxHeight()) { HomeAction(t("Sejarah & Pulihkan", "History & restore"), t("Pulihkan perubahan anda", "Restore your changes"), "03") { vm.openHistory() } }
                    }
                    OutlinedCard(onClick = { custom = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Custom MyLauncher  →", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0866FF))
                            Text(t("Selepas urus TV, reka ruang anda. 12 gaya jam/tarikh, 20 wallpaper dan susunan app pilihan.", "After managing your TV, make it yours. 12 clock/date styles, 20 wallpapers and your app layout."))
                        }
                    }
                    ProblemReporter(ms)
                    Text(t("Laporan GitHub adalah awam. Semak maklumat sebelum hantar; akaun GitHub diperlukan.", "GitHub reports are public. Review details before submitting; a GitHub account is required."), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    Text(t("Dibina untuk pilihan anda.", "Built around your choices."), style = MaterialTheme.typography.labelLarge)
                    Button(onClick = { runCatching { uriHandler.openUri("https://sociabuzz.com/cleanmytv/tribe") } }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp), shape = RoundedCornerShape(16.dp)) { Text(t("♡  Sokong cleanMYTV", "♡  Support cleanMYTV"), fontWeight = FontWeight.Bold) }
                }
            }
            return
        }
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
            if (vm.screen == Screen.CONNECT || vm.screen == Screen.REVIEW) {
                TextButton(onClick = { manage = false }) { Text(t("← Halaman utama", "← Home")) }
            }
            if (vm.screen == Screen.DONE) {
                TvFeedback(vm)
                Button(onClick = { custom = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text(t("Seterusnya: Custom MyLauncher", "Next: Custom MyLauncher")) }
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
    val accent = Color(0xFF0866FF)
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth().fillMaxHeight(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.outlinedCardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = Color(0xFFEAF2FF), shape = RoundedCornerShape(14.dp)) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Text(when(number) { "01" -> "▦"; "02" -> "+"; else -> "↶" }, color = accent, style = MaterialTheme.typography.headlineMedium)
                }
            }
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("→", color = accent, modifier = Modifier.align(Alignment.End))
        }
    }
}
