package tv.cleanyour.core.flows

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import tv.cleanyour.core.model.PackageName
import tv.cleanyour.core.model.EnabledState
import tv.cleanyour.core.model.PackageState
import tv.cleanyour.core.probe.LaunchableApps
import tv.cleanyour.testing.fixtures.Samples
import tv.cleanyour.core.spec.DecodeResult
import tv.cleanyour.core.spec.LauncherConfigCodec
import tv.cleanyour.core.spec.LauncherConfigDoc
import tv.cleanyour.testing.fixtures.MemoryBlobStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class HomeAppLibraryTest {
    @Test fun connectedHomeOffersLaunchableSystemAppsAndKeepsTheOfflineFallback() = runTest {
        val h = Harness()
        val tv = h.knownTv()
        val systemApp = PackageName("com.example.systemplayer")
        val phoneApp = PackageName("com.example.phoneplayer")
        val newlyInstalled = PackageName("com.example.newplayer")
        val disabled = PackageName("com.example.disabled")
        val provider = PackageName("com.android.providers.calendar")
        fun state(system: Boolean, enabled: EnabledState = EnabledState.ENABLED) =
            PackageState(enabled, system, 1, null, false, false, null, false)
        h.snapshots.save(Samples.snapshot(packages = Samples.miTvPackages() + mapOf(
            systemApp to state(true), phoneApp to state(false), provider to state(true),
            disabled to state(false, EnabledState.DISABLED_USER)), takenAtMs = Samples.NOW_MS + 1))
        val deps = h.manageDeps(launchable = LaunchableApps { setOf(systemApp, phoneApp, newlyInstalled, provider, disabled) })
        HomePrefs(h.documents).save(tv.id, HomeChoice(listOf(newlyInstalled), null))
        val home = HomeScreenMachine(deps, backgroundScope, tv.id)
        settle()
        val apps = home.state.value.apps.map { it.pkg }
        assertTrue(systemApp in apps)
        assertTrue(phoneApp in apps)
        assertTrue(newlyInstalled in apps, "live apps must not disappear because the snapshot predates installation")
        assertTrue(newlyInstalled in home.state.value.dock.map { it.pkg }, "a saved new app must remain visible in the dock editor")
        assertFalse(disabled in apps)
        assertFalse(provider in apps)

        val fallback = HomeScreenMachine(h.manageDeps(launchable = LaunchableApps { error("TV cannot answer") }), backgroundScope, tv.id)
        settle()
        assertFalse(systemApp in fallback.state.value.apps.map { it.pkg })
        assertTrue(phoneApp in fallback.state.value.apps.map { it.pkg })
    }

    @Test fun sentConfigEnablesTheRemotePickerAndSeedsPhoneOnlyApps() = runTest {
        val phoneApp = PackageName("com.example.phoneplayer")
        val doc = LauncherConfigDoc(dockApps = listOf(phoneApp), appLibrary = false,
            unknown = mapOf("library_button" to JsonPrimitive(false),
                "library_apps" to JsonArray(listOf(JsonPrimitive("com.example.existing"), JsonPrimitive(phoneApp.value)))))
        val blobs = MemoryBlobStore()
        val sent = Look.encode(ConfigEncoder.Codec, blobs, doc, null)
        val decoded = assertIs<DecodeResult.Ok>(LauncherConfigCodec.decode(blobs.bytes(sent.config)))
        assertEquals(emptyList(), decoded.warnings)
        assertEquals(true, decoded.doc.appLibrary)
        for (key in listOf("library_button", "show_library_pill", "app_library_selection")) {
            assertEquals(JsonPrimitive(true), decoded.doc.unknown[key], key)
        }
        assertEquals(JsonArray(listOf(JsonPrimitive("com.example.existing"), JsonPrimitive(phoneApp.value))),
            decoded.doc.unknown["library_apps"])
        assertEquals(listOf(phoneApp), decoded.doc.dockApps)
        assertEquals(sent.doc, HomeAppLibrary.applyTo(sent.doc), "repeated sends must not duplicate shortcuts")
        assertEquals(false, doc.appLibrary, "reading the old saved config must not change it")
    }

    @Test fun anEmptyDockStillOffersTheLibraryWithoutPreselectingApps() = runTest {
        val blobs = MemoryBlobStore()
        val sent = Look.encode(ConfigEncoder.Codec, blobs, LauncherConfigDoc(), null)
        val decoded = assertIs<DecodeResult.Ok>(LauncherConfigCodec.decode(blobs.bytes(sent.config)))
        assertEquals(true, decoded.doc.appLibrary)
        assertEquals(JsonArray(emptyList()), decoded.doc.unknown["library_apps"])
    }
}
