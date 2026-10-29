package my.cleanmytv.app

import android.app.Application
import android.util.Base64
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class LauncherModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app
    private val prefs = app.getSharedPreferences("mylauncher_editor", 0)
    var config by mutableStateOf(runCatching { LauncherConfig.parse(prefs.getString("draft", "")!!) }.getOrDefault(LauncherConfig()))
        private set
    var host by mutableStateOf("")
    var port by mutableStateOf("5555")
    var apps by mutableStateOf<List<LauncherApp>>(emptyList())
        private set
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set
    var saved by mutableStateOf(readThemes())
        private set
    private var scannedTarget = ""
    private fun readThemes(): Map<String, String> = runCatching {
        val obj = JSONObject(prefs.getString("themes", "{}")!!)
        obj.keys().asSequence().take(10).associateWith { obj.getString(it) }
    }.getOrDefault(emptyMap())
    fun edit(c: LauncherConfig) { config = c; prefs.edit().putString("draft", c.json()).apply() }
    fun save(name: String) {
        val key = name.trim().take(36)
        if(key.isBlank()) return
        if(saved.size >= 10 && key !in saved) { message = "Maksimum 10 tema / Maximum 10 themes"; return }
        saved = saved + (key to config.json()); prefs.edit().putString("themes", JSONObject(saved).toString()).apply()
        message = "Tema disimpan pada telefon / Theme saved on phone"
    }
    fun load(name: String) { saved[name]?.let { raw -> runCatching { edit(LauncherConfig.parse(raw)) } } }
    fun delete(name: String) { saved = saved - name; prefs.edit().putString("themes", JSONObject(saved).toString()).apply() }

    private fun target(): Pair<String, Int> {
        val h = host.trim(); val p = port.toIntOrNull()
        require(Regex("^[A-Za-z0-9.-]{1,253}$").matches(h) && p != null && p in 1..65535) { "Semak IP TV dan port / Check TV IP and port" }
        return h to p!!
    }
    private fun launchable(tv: Tv): List<LauncherApp> {
        val enabled = Analyzer.parsePkgs(tv.sh("pm list packages -e --user 0"))
        val components = listOf("LEANBACK_LAUNCHER", "LAUNCHER").flatMap { category ->
            tv.sh("cmd package query-activities --components --user 0 -a android.intent.action.MAIN -c android.intent.category.$category")
                .lineSequence().mapNotNull { launcherComponent(it) }.toList()
        }
        val pkgs = components.map { it.substringBefore('/') }.distinct().filter { it in enabled && it != ctx.packageName }.sorted()
        require(pkgs.isNotEmpty()) { "Senarai app TV tidak dapat dibaca. Semak sambungan ADB dan cuba lagi. / Cannot read TV apps. Check ADB and try again." }
        return pkgs.take(150).map { LauncherApp(it) }
    }
    fun refresh() = operation {
        val (h,p) = target()
        val found = Adb.connect(ctx,h,p).use { tv -> withContext(Dispatchers.IO) { launchable(tv) } }
        val same = scannedTarget == "$h:$p"
        scannedTarget = "$h:$p"; apps = found
        val current = config.apps.filter { pkg -> found.any { it.pkg == pkg } }
        edit(config.copy(apps = if(current.isEmpty() && !same) found.take(12).map { it.pkg } else current))
        message = "${found.size} app aktif dikesan / active apps found."
    }
    private fun operation(block: suspend () -> Unit) {
        if(busy) return
        busy = true; message = "Sila tunggu; benarkan ADB pada TV / Please wait; allow ADB on TV"
        viewModelScope.launch {
            try { block() }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { message = e.message ?: "Operasi gagal / Operation failed"; ErrorReports.record("MyLauncher", e.javaClass.simpleName) }
            finally { busy = false }
        }
    }
    private fun checked(tv: Tv, command: String): String {
        val (code, out) = tv.run(command)
        require(code == 0 && !out.contains("Error:", true) && !out.contains("Exception",true) && !out.contains("Failure",true)) { out.take(700).ifBlank { "Operasi TV gagal / TV operation failed" } }
        return out
    }
    fun applyToTv() {
        val snapshot = config
        operation {
            val (h,p) = target()
            Adb.connect(ctx,h,p).use { tv ->
                withContext(Dispatchers.IO) {
                    val live = launchable(tv).map { it.pkg }.toSet()
                    val validConfig = snapshot.copy(apps = snapshot.apps.filter { it in live })
                    val oldHome = launcherComponent(tv.sh("cmd package resolve-activity --brief --user 0 -a android.intent.action.MAIN -c android.intent.category.HOME"))
                    if(oldHome != null && !oldHome.startsWith("${ctx.packageName}/") && !oldHome.startsWith("android/"))
                        prefs.edit().putString("home_$h:$p", oldHome).commit()
                    val remote = "/data/local/tmp/cleanmytv-launcher-${UUID.randomUUID()}.apk"
                    try {
                        tv.dadb.push(File(ctx.applicationInfo.sourceDir), remote)
                        ensureActive()
                        val installed = checked(tv, "pm install -r --user 0 $remote")
                        require(installed.lineSequence().any { it.trim() == "Success" }) { "Pemasangan MyLauncher gagal / MyLauncher installation failed: ${installed.take(500)}" }
                    } finally { runCatching { tv.sh("rm -f $remote") } }
                    val encoded = Base64.encodeToString(validConfig.json().toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                    checked(tv, "am start -W --user 0 -n ${ctx.packageName}/.LauncherConfigActivity --es config $encoded")
                    val ack = checked(tv, "run-as ${ctx.packageName} cat files/mylauncher_config_ack").trim()
                    require(ack == encoded) { "TV belum mengesahkan reka bentuk. Cuba hantar semula. / TV did not confirm the design. Try sending again." }
                    checked(tv, "pm enable --user 0 ${ctx.packageName}/.MyLauncherHome")
                    checked(tv, "am start -W --user 0 -n ${ctx.packageName}/.TvLauncherActivity")
                }
            }
            message = "Reka bentuk dihantar dan MyLauncher dibuka pada TV. Untuk butang Home, pilih MyLauncher dalam tetapan Home TV. / Design sent and MyLauncher opened on TV. To use the Home button, select MyLauncher in TV Home settings."
        }
    }
    fun homeSettings() = operation {
        val (h,p) = target()
        Adb.connect(ctx,h,p).use { tv -> withContext(Dispatchers.IO) { checked(tv, "am start --user 0 -a android.settings.HOME_SETTINGS") } }
        message = "Pilih launcher dalam tetapan TV dengan remote. Sesetengah TV mengehadkan launcher lalai. / Choose a launcher in TV settings with your remote. Some TVs restrict the default launcher."
    }
    fun restoreHome() = operation {
        val (h,p) = target()
        val original = prefs.getString("home_$h:$p", null)?.let { launcherComponent(it) }
            ?: error("Launcher asal belum direkod. Pilih launcher asal dalam Tetapan Home TV. / No original launcher recorded. Choose it in TV Home settings.")
        Adb.connect(ctx,h,p).use { tv -> withContext(Dispatchers.IO) {
            checked(tv, "cmd package set-home-activity --user 0 $original")
            checked(tv, "am start --user 0 -a android.intent.action.MAIN -c android.intent.category.HOME")
        } }
        message = "Launcher asal dipilih semula / Original launcher selected again"
    }
}
