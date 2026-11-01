package tv.cleanyour.core.flows

import kotlinx.coroutines.test.runTest
import tv.cleanyour.core.model.*
import tv.cleanyour.core.planner.PlanRequest
import tv.cleanyour.testing.fixtures.Samples
import kotlin.test.*

class BundledPrayerTest {
    @Test fun anInstalledOlderCompanionCanBeUpdatedExplicitly() = runTest {
        val h=Harness(); val tv=h.knownTv()
        val old=PackageState(EnabledState.ENABLED,false,1,null,false,false,null,false)
        h.snapshots.save(Samples.snapshot(packages=Samples.miTvPackages() + (BundledPrayer.PKG to old),takenAtMs=Samples.NOW_MS+1))
        val blob=h.blobs.add("updated bundled companion".encodeToByteArray())
        val info=ApkInfo(BundledPrayer.PKG,2,"1.0-beta2",Samples.zeroHash,blob.sha256,blob.size,"test")
        val home=HomeScreenMachine(h.manageDeps(encoder=ConfigEncoder.Codec,bundledPrayer={BundledPrayer.of(info,blob)}),backgroundScope,tv.id)
        settle();assertTrue(home.state.value.prayerInstalled);assertTrue(home.state.value.prayerUpdateAvailable)
        assertTrue(h.planner.requests.none { it is PlanRequest.AddModule })
        home.dispatch(HomeScreenIntent.AddPrayer);settle()
        assertEquals(2L,h.planner.requests.filterIsInstance<PlanRequest.AddModule>().single().version.versionCode)
    }
    @Test fun openingHomeDoesNotInstallTheOptionalModule() = runTest {
        val h = Harness(); val tv = h.knownTv()
        var reads = 0
        val home = HomeScreenMachine(h.manageDeps(bundledPrayer = { reads++; null }), backgroundScope, tv.id)
        settle()
        assertTrue(home.state.value.prayerAvailable)
        assertFalse(home.state.value.prayerInstalled)
        assertEquals(0, reads)
        assertTrue(h.planner.requests.none { it is PlanRequest.AddModule })
    }
    @Test fun explicitAddUsesTheBundledCompanionAndIncludesItInHome() = runTest {
        val h = Harness(); val tv = h.knownTv()
        val blob = h.blobs.add("test packaged APK".encodeToByteArray())
        val info = ApkInfo(BundledPrayer.PKG,1,"1.0-beta1",Samples.zeroHash,blob.sha256,blob.size,"test")
        val home = HomeScreenMachine(h.manageDeps(encoder = ConfigEncoder.Codec, bundledPrayer = { BundledPrayer.of(info,blob) }), backgroundScope,tv.id)
        settle(); home.dispatch(HomeScreenIntent.AddPrayer); settle()
        val add = h.planner.requests.filterIsInstance<PlanRequest.AddModule>().single()
        assertEquals(BundledPrayer.PKG,add.module.pkg)
        assertEquals(blob,add.apk)
        assertNotNull(add.config)
        assertTrue(BundledPrayer.PKG in HomePrefs(h.documents).load(tv.id)!!.dock)
    }
    @Test fun corruptOrWrongPackageCannotBeWrappedAsTheBundledCompanion() {
        val blob = BlobRef(Samples.zeroHash,1,"test")
        assertFailsWith<IllegalArgumentException> { BundledPrayer.of(ApkInfo(PackageName("com.example.foreign"),1,"1",Samples.zeroHash,Samples.zeroHash,1,"test"),blob) }
        assertFailsWith<IllegalArgumentException> { BundledPrayer.of(ApkInfo(BundledPrayer.PKG,1,"1",Samples.zeroHash,Samples.zeroHash,2,"test"),blob) }
    }
}
