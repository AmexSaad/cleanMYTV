package tv.cleanyour.feature.manage

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*
import tv.cleanyour.core.flows.*
import tv.cleanyour.core.model.*
import tv.cleanyour.testing.fixtures.Samples
import tv.cleanyour.ui.design.theme.CytTheme

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[35],qualifiers="w360dp-h1000dp-xhdpi")
class PrayerDashboardTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    @Test fun prayerIsVisibleOnDashboardAndOpensItsOwnSection() {
        val id=Samples.miTvId()
        val card=TvCard(id,"unifi-TV",null,false,Samples.NOW_MS,Level.SIMPLE,ChipState.CONNECTED,null,0,
            listOf("Netflix","YouTube","Prayer Time"),Samples.NOW_MS,DebugState.ON)
        val hits=mutableListOf<ManageIntent>()
        compose.setContent { CytTheme(darkTheme=true,reducedMotion=true) {
            Box(Modifier.size(360.dp,1000.dp)){ManageScreen(ManageState(cards=listOf(card),selected=id,loading=false),{hits+=it})}
        } }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/prayer-dashboard.png")
        compose.onNodeWithText("TV menu & Prayer Time").performClick()
        Assert.assertEquals(listOf(ManageIntent.Open(ManageSection.PRAYER)),hits)
    }
}
