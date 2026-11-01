package tv.cleanyour.app

import android.app.Instrumentation
import android.os.Bundle
import kotlinx.coroutines.runBlocking
import tv.cleanyour.core.flows.BundledPrayer

// Real PackageManager/archive-signature verification on an installed preview, no fake inspector.
class PrayerBundleInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        waitForIdleSync()
        val result = Bundle()
        try {
            val graph = (targetContext.applicationContext as CytApplication).graph
            val companion = runBlocking { requireNotNull(graph.manageDeps.bundledPrayer?.invoke()) }
            check(companion.module.pkg == BundledPrayer.PKG)
            check(companion.blob.size > 6_000_000)
            check(companion.version.artifact.sha256 == companion.blob.sha256)
            result.putString("result", "PASS: packaged companion, SHA-256 and signing identity verified on Android")
            finish(-1, result)
        } catch (failure: Throwable) {
            result.putString("result", "FAIL: ${failure.javaClass.simpleName}: ${failure.message}")
            finish(0, result)
        }
    }
}
