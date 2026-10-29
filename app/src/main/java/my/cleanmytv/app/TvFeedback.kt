package my.cleanmytv.app

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@Composable
fun TvFeedback(vm: MainViewModel) {
    val info = vm.analysis?.info ?: return
    if (vm.mode != Mode.CLEAN) return
    val ms = vm.lang == Lang.MS
    fun t(a: String, b: String) = if(ms) a else b
    val context = LocalContext.current
    val browser = LocalUriHandler.current
    var show by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var result by remember { mutableStateOf(-1) }
    var comment by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val statuses = listOf(t("Berjaya, TV berfungsi baik", "Successful, TV works well"), t("Berjaya sebahagian", "Partly successful"), t("Ada masalah", "There is a problem"))
    Button(onClick = { show = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text(t("Kongsi Keputusan TV", "Share TV results")) }
    if(show) AlertDialog(
        onDismissRequest = { show = false },
        title = { Text(t("Rekod keserasian TV", "TV compatibility record")) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${info.brand} ${info.model}\nAndroid ${info.release} (SDK ${info.sdk})\nFirmware: ${info.firmware.ifBlank { "Unknown" }}")
                Text(t("Operasi: ${vm.okCount} berjaya, ${vm.failed.size} gagal, ${(vm.total-vm.okCount-vm.failed.size).coerceAtLeast(0)} belum disahkan.", "Operations: ${vm.okCount} succeeded, ${vm.failed.size} failed, ${(vm.total-vm.okCount-vm.failed.size).coerceAtLeast(0)} unconfirmed."))
                Text(t("Cuba remote, home screen dan app penting dahulu. Pilih keputusan sebenar:", "Test your remote, home screen and essential apps first. Choose the actual result:"))
                statuses.forEachIndexed { i, label ->
                    Row { RadioButton(result == i, onClick = { result = i }); TextButton(onClick = { result = i }) { Text(label) } }
                }
                OutlinedTextField(comment, { comment = it.take(1200) }, label = { Text(t("Komen (pilihan)", "Comment (optional)")) }, modifier = Modifier.fillMaxWidth())
                Text(t("Rekod akan dibuka sebagai draf GitHub Issue awam. Semak kemudian tekan Create untuk menghantar. IP, nombor siri dan kunci ADB tidak disertakan.", "A public GitHub Issue draft will open. Review it and press Create to submit. IP, serial number and ADB keys are excluded."), style = MaterialTheme.typography.bodySmall)
                if(error) Text(t("Tidak dapat membuka pelayar.", "Could not open browser."))
            }
        },
        confirmButton = { TextButton(enabled = result >= 0, onClick = {
            @Suppress("DEPRECATION")
            val version = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrDefault("unknown")
            val outcome = listOf("User confirmed working", "Partly successful", "Problem reported")[result]
            val body = "## Community compatibility report\nUser-reported; not independently verified.\n\n- Brand: ${info.brand}\n- Model: ${info.model}\n- Android: ${info.release}\n- SDK: ${info.sdk}\n- Firmware build: ${info.firmware}\n- cleanMYTV: $version\n- User result: $outcome\n- Operation: disable apps\n- Attempted total: ${vm.total}\n- Command successes: ${vm.okCount}\n- Failures: ${vm.failed.size}\n- Unconfirmed: ${(vm.total-vm.okCount-vm.failed.size).coerceAtLeast(0)}\n\n## User comments\n$comment\n"
            val title = "[TV Feedback] ${info.brand} ${info.model}: $outcome"
            error = runCatching { browser.openUri("https://github.com/AmexSaad/cleanMYTV/issues/new?title=" + Uri.encode(title) + "&body=" + Uri.encode(body)) }.isFailure
            if(!error) show = false
        }) { Text(t("Semak & buka GitHub", "Review & open GitHub")) } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { show = false }) { Text(t("Batal", "Cancel")) }
                TextButton(onClick = { error = runCatching { browser.openUri("https://sociabuzz.com/cleanmytv/tribe") }.isFailure }) { Text(t("♡ Sokong", "♡ Donate")) }
            }
        }
    )
}
