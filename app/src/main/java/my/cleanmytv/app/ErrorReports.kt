package my.cleanmytv.app

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavedError(val time: Long, val kind: String, val code: String)

/** Local, bounded diagnostics. Never stores raw shell output, exception messages or ADB keys. */
object ErrorReports {
    private var context: Context? = null
    @Synchronized fun init(ctx: Context) {
        if (context != null) return
        context = ctx.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { record("App crash", error.javaClass.simpleName) }
            if (previous != null) previous.uncaughtException(thread, error)
            else { android.os.Process.killProcess(android.os.Process.myPid()); kotlin.system.exitProcess(1) }
        }
    }
    @Synchronized fun list(): List<SavedError> = runCatching {
        val json = JSONArray(context?.getSharedPreferences("error_reports", 0)?.getString("items", "[]") ?: "[]")
        (0 until json.length()).map { i -> json.getJSONObject(i).let { SavedError(it.getLong("time"), it.getString("kind"), it.getString("code")) } }
    }.getOrDefault(emptyList())
    @Synchronized fun record(kind: String, code: String) {
        val ctx = context ?: return
        val safeCode = code.filter { it.isLetterOrDigit() || it == '_' }.take(100)
        val entries = (listOf(SavedError(System.currentTimeMillis(), kind.take(80), safeCode)) + list()).take(20)
        val array = JSONArray()
        entries.forEach { array.put(JSONObject().put("time", it.time).put("kind", it.kind).put("code", it.code)) }
        ctx.getSharedPreferences("error_reports", 0).edit().putString("items", array.toString()).commit()
    }
    @Synchronized fun clear() { context?.getSharedPreferences("error_reports", 0)?.edit()?.remove("items")?.apply() }
}

@Composable
fun ProblemReporter(ms: Boolean) {
    val ctx = LocalContext.current
    val uri = LocalUriHandler.current
    var show by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(emptyList<SavedError>()) }
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var openingError by remember { mutableStateOf(false) }
    fun t(a: String, b: String) = if (ms) a else b
    OutlinedButton(onClick = { errors = ErrorReports.list(); selected = emptySet(); show = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(t("Lapor Masalah ↗", "Report a problem ↗")) }
    if (show) AlertDialog(
        onDismissRequest = { show = false },
        title = { Text(t("Masalah terkini", "Recent problems")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t("Pilih ralat untuk dimasukkan. Laporan GitHub adalah awam dan boleh disemak sebelum hantar.", "Select errors to include. GitHub reports are public; review before submitting."))
                if (errors.isEmpty()) Text(t("Tiada ralat direkodkan. Anda masih boleh tulis laporan sendiri.", "No errors recorded. You can still write a report."))
                LazyColumn(Modifier.heightIn(max = 280.dp)) {
                    items(errors) { item ->
                        Row {
                            Checkbox(item.time in selected, { on -> selected = if(on) selected + item.time else selected - item.time })
                            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                                Text(item.kind, style = MaterialTheme.typography.titleSmall)
                                Text(item.code, style = MaterialTheme.typography.bodySmall)
                                Text(SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(item.time)), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                if (errors.isNotEmpty()) TextButton(onClick = { ErrorReports.clear(); errors = emptyList(); selected = emptySet() }) { Text(t("Kosongkan senarai", "Clear list")) }
                if (openingError) Text(t("Pelayar tidak dapat dibuka. Cuba lagi.", "Could not open browser. Try again."))
            }
        },
        confirmButton = { TextButton(onClick = {
            @Suppress("DEPRECATION")
            val version = runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrDefault("unknown")
            val picked = errors.filter { it.time in selected }.joinToString("\n") { "- ${it.kind}: ${it.code} (${Date(it.time)})" }
            val body = "## Masalah / Problem\n\n## Langkah / Steps to reproduce\n1. \n2. \n\n## Ralat dipilih / Selected errors\n${picked.ifBlank { "None selected" }}\n\n## App\ncleanMYTV $version\nPhone Android: ${android.os.Build.VERSION.RELEASE}\n\n## Model TV (pilihan / optional)\n\n## Screenshot\nRemove personal information before attaching screenshots.\n"
            openingError = runCatching { uri.openUri("https://github.com/AmexSaad/cleanMYTV/issues/new?title=" + Uri.encode("[Bug] ") + "&body=" + Uri.encode(body)) }.isFailure
            if (!openingError) show = false
        }) { Text(t("Buka laporan GitHub", "Open GitHub report")) } },
        dismissButton = { TextButton(onClick = { show = false }) { Text(t("Batal", "Cancel")) } }
    )
}
