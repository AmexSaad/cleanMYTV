package my.cleanmytv.app

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import my.cleanmytv.app.ui.CleanTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Emulator coverage of both editor galleries and every production clock/wallpaper renderer. */
@RunWith(AndroidJUnit4::class)
class LauncherSmokeTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun screenshot(name:String) {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val dir=File(context.getExternalFilesDir(null),"screenshots").apply{mkdirs()}
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(dir,"$name.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        }
    }
    @Test fun editorShowsBilingualClockAndWallpaperCollections() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val vm=ViewModelProvider(activity)[MainViewModel::class.java]
                vm.changeLanguage(Lang.MS)
                activity.setContent {CleanTheme {LauncherEditor(vm) {}}}
            }
            compose.waitForIdle()
            compose.onNodeWithText("12 gaya jam & tarikh").assertExists()
            screenshot("editor-ms-clocks")
            compose.onNodeWithText("Wallpaper",useUnmergedTree=true).performClick()
            compose.onNodeWithText("20 wallpaper").assertExists()
            screenshot("editor-ms-wallpapers")
            scenario.onActivity { activity ->
                val vm=ViewModelProvider(activity)[MainViewModel::class.java]
                vm.changeLanguage(Lang.EN)
            }
            compose.waitForIdle()
            compose.onNodeWithText("Install & send design to TV").assertExists()
            screenshot("editor-en-wallpapers")
        }
    }
    @Test fun allTwelveClockStylesAndTwentyWallpapersRender() {
        var design by mutableStateOf(LauncherConfig())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { CleanTheme {
                Box(Modifier.fillMaxSize()) {LauncherScene(design,Modifier.fillMaxWidth().aspectRatio(16f/9f))}
            } } }
            repeat(20) { i ->
                compose.runOnIdle {design=LauncherConfig(clock=i%12,wallpaper=i)}
                compose.waitForIdle()
                screenshot("scene-${(i+1).toString().padStart(2,'0')}")
            }
        }
    }
    @Test fun tvHomeOpensAndAllAppsControlWorks() {
        ActivityScenario.launch(TvLauncherActivity::class.java).use {
            compose.waitForIdle()
            compose.onNodeWithText("Semua app").performClick()
            compose.onNodeWithText("Tetapan Home").assertExists()
            screenshot("tv-home")
        }
    }
}
