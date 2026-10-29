package my.cleanmytv.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import my.cleanmytv.app.ui.CleanTheme

/** Only the ADB shell/system (DUMP permission) may deliver a design to this entry point. */
class LauncherConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            val raw = intent.getStringExtra("config") ?: error("Missing configuration")
            require(raw.length <= 28000)
            val config = LauncherConfig.parse(String(Base64.decode(raw, Base64.URL_SAFE), Charsets.UTF_8))
            check(getSharedPreferences("mylauncher_tv", 0).edit().putString("config", config.json()).commit())
            java.io.File(filesDir, "mylauncher_config_ack").writeText(raw)
        }.onFailure { Toast.makeText(this, "MyLauncher: invalid configuration", Toast.LENGTH_LONG).show() }
        finish()
    }
}

private data class LocalTvApp(val pkg: String, val label: String, val intent: Intent, val icon: androidx.compose.ui.graphics.ImageBitmap?)

class TvLauncherActivity : ComponentActivity() {
    private var config by mutableStateOf(LauncherConfig())
    private var apps by mutableStateOf<List<LocalTvApp>>(emptyList())
    private var loading by mutableStateOf(true)
    private var loadJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        setContent { CleanTheme { TvHome() } }
    }
    override fun onResume() {
        super.onResume()
        config = runCatching { LauncherConfig.parse(getSharedPreferences("mylauncher_tv",0).getString("config", "")!!) }.getOrDefault(LauncherConfig())
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            loading = true
            try { apps = withContext(Dispatchers.IO) { readApps() } }
            finally { loading = false }
        }
    }
    @Suppress("DEPRECATION")
    private fun readApps(): List<LocalTvApp> {
        val pm = packageManager
        return listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER).flatMap { category ->
            pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)
        }.distinctBy { it.activityInfo.packageName }.filter { it.activityInfo.packageName != packageName && it.activityInfo.enabled && it.activityInfo.applicationInfo.enabled }.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            val start = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg) ?: return@mapNotNull null
            val icon = runCatching {
                val drawable = info.loadIcon(pm)
                val bitmap = android.graphics.Bitmap.createBitmap(96,96,android.graphics.Bitmap.Config.ARGB_8888)
                drawable.setBounds(0,0,96,96); drawable.draw(android.graphics.Canvas(bitmap)); bitmap.asImageBitmap()
            }.getOrNull()
            LocalTvApp(pkg, info.loadLabel(pm).toString(), start, icon)
        }.sortedBy { it.label.lowercase() }
    }
    private fun open(intent: Intent) { runCatching { startActivity(intent) }.onFailure { Toast.makeText(this, if(config.lang == "ms") "App tidak dapat dibuka" else "Unable to open app", Toast.LENGTH_LONG).show() } }

    @Composable
    private fun TvHome() {
        val ms = config.lang == "ms"
        fun t(a: String,b: String) = if(ms) a else b
        var showAll by remember { mutableStateOf(false) }
        val selected = if(showAll) apps else config.apps.mapNotNull { pkg -> apps.find { it.pkg == pkg } }
        val firstFocus = remember { FocusRequester() }
        LaunchedEffect(loading) { if(!loading) runCatching { firstFocus.requestFocus() } }
        LauncherScene(config, Modifier.fillMaxSize()) {
            Row(Modifier.align(Alignment.TopEnd).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvButton(t("Semua app", "All apps"), Modifier.focusRequester(firstFocus)) { showAll = !showAll }
                TvButton(t("Tetapan Home", "Home settings")) { open(Intent(Settings.ACTION_HOME_SETTINGS)) }
                TvButton(t("Tetapan TV", "TV settings")) { open(Intent(Settings.ACTION_SETTINGS)) }
            }
            if(loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
            if(!loading && selected.isEmpty()) Surface(Modifier.align(Alignment.BottomStart).padding(28.dp), color = Color(0xCC10223A), shape = RoundedCornerShape(16.dp)) {
                Text(t("Pilih app melalui editor telefon, atau buka Semua app.", "Choose apps in the phone editor, or open All apps."), Modifier.padding(20.dp), color = Color.White)
            }
            if(config.vertical) LazyColumn(Modifier.align(Alignment.CenterEnd).padding(top = 92.dp, bottom = 28.dp, end = 28.dp).width(200.dp).fillMaxHeight(.78f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(selected, key = {it.pkg}) { app -> AppTile(app) }
            } else LazyRow(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(28.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(selected, key = {it.pkg}) { app -> AppTile(app) }
            }
            Text("cleanMYTV • MyLauncher", Modifier.align(Alignment.BottomEnd).padding(8.dp), color = Color.White.copy(alpha = .70f), style = MaterialTheme.typography.labelSmall)
        }
    }
    @Composable
    private fun TvButton(label: String, modifier: Modifier = Modifier, click: () -> Unit) {
        var focused by remember { mutableStateOf(false) }
        Button(onClick = click, modifier = modifier.onFocusChanged { focused = it.isFocused }.border(if(focused) 3.dp else 0.dp, if(focused) Color.White else Color.Transparent, RoundedCornerShape(24.dp)), colors = ButtonDefaults.buttonColors(containerColor = if(focused) Color(0xFF0866FF) else Color(0xD912233B))) { Text(label) }
    }
    @Composable
    private fun AppTile(app: LocalTvApp) {
        var focused by remember { mutableStateOf(false) }
        Surface(onClick = { open(app.intent) }, modifier = Modifier.width(160.dp).heightIn(min = 108.dp).onFocusChanged { focused = it.isFocused }
            .border(if(focused) 4.dp else 1.dp, if(focused) Color.White else Color.White.copy(alpha=.22f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp), color = if(focused) Color(0xFF0866FF) else Color(0xDD14243E)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                app.icon?.let { Image(it, contentDescription = null, modifier = Modifier.size(42.dp)) }
                Text(app.label, color = Color.White, maxLines = 2, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
