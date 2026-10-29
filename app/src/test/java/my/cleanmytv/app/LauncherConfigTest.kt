package my.cleanmytv.app

import org.junit.Assert.*
import org.junit.Test

class LauncherConfigTest {
    @Test fun designRoundTripPreservesOptionsAndOrder() {
        val design = LauncherConfig(clock=11, wallpaper=19, vertical=true, clockX=.8f, clockY=.3f, clockSize=1.3f,
            showClock=false, showDate=true, hour24=false, darkText=true, dim=.4f, lang="en", apps=listOf("tv.example.video", "tv.example.music"))
        assertEquals(design, LauncherConfig.parse(design.json()))
    }
    @Test fun untrustedConfigurationsAreBoundedAndPackagesValidated() {
        val result=LauncherConfig.parse("""{"clock":999,"wallpaper":-1,"x":50,"size":0,"dim":4,"lang":"invalid","apps":["tv.example.video","tv.example.video","bad;reboot","tv.example.music"]}""")
        assertEquals(11,result.clock); assertEquals(0,result.wallpaper); assertEquals(1f,result.clockX)
        assertEquals(.6f,result.clockSize); assertEquals(.7f,result.dim); assertEquals("ms",result.lang)
        assertEquals(listOf("tv.example.video","tv.example.music"),result.apps)
    }
    @Test fun componentParserRejectsShellFragmentsAndResolverNoise() {
        assertEquals("com.example.home/.MainActivity",launcherComponent("priority=0\ncom.example.home/.MainActivity\n"))
        assertNull(launcherComponent("com.example.home/.Home;reboot"))
        assertNull(launcherComponent("com.example.home/.Home $(reboot)"))
        assertNull(launcherComponent("No activity found"))
    }
    @Test fun unknownSchemaAndOversizedPayloadAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {LauncherConfig.parse("""{"version":2}""")}
        assertThrows(IllegalArgumentException::class.java) {LauncherConfig.parse(" ".repeat(20001))}
    }
    @Test fun catalogueContainsExactlyTwelveAndTwentyBilingualEntries() {
        assertEquals(12,clockNamesMs.size);assertEquals(12,clockNamesEn.size)
        assertEquals(20,wallpaperNamesMs.size);assertEquals(20,wallpaperNamesEn.size)
    }
    @Test fun externalConfigurationRequiresShellPermissionAndPhoneHomeIsDisabled() {
        val doc=javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {isNamespaceAware=true}.newDocumentBuilder()
            .parse(java.io.File("src/main/AndroidManifest.xml"))
        val ns="http://schemas.android.com/apk/res/android"
        val activities=doc.getElementsByTagName("activity")
        val receiver=(0 until activities.length).map {activities.item(it) as org.w3c.dom.Element}.first {it.getAttributeNS(ns,"name")==".LauncherConfigActivity"}
        assertEquals("android.permission.DUMP",receiver.getAttributeNS(ns,"permission"))
        val alias=doc.getElementsByTagName("activity-alias").item(0) as org.w3c.dom.Element
        assertEquals("false",alias.getAttributeNS(ns,"enabled"))
    }
}
