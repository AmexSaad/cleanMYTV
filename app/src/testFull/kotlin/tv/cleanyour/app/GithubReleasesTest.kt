package tv.cleanyour.app

import java.io.ByteArrayInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Test
import tv.cleanyour.core.registry.HttpFetcher
import tv.cleanyour.core.registry.HttpResponse
import kotlin.test.*

class GithubReleasesTest {
    private fun release(version: String, draft: Boolean = false, prerelease: Boolean = '-' in version,
        url: String = "https://github.com/AmexSaad/cleanMYTV/releases/download/v$version/cleanMYTV.apk",
        apk: String = "cleanMYTV.apk", state: String = "uploaded"): String =
        """{"tag_name":"v$version","draft":$draft,"prerelease":$prerelease,"body":"New changes","assets":[{"name":"$apk","state":"$state","size":100,"browser_download_url":"$url"}]}"""

    private fun select(current: String, vararg releases: String) = GithubReleases.select(releases.joinToString(",", "[", "]"), current)

    @Test fun betaInstallationsDiscoverBetaAssets() {
        val result = assertIs<AppUpdateStatus.Available>(select("1.2.2-beta1", release("1.3.0-beta1")))
        assertEquals("1.3.0-beta1", result.release.version)
        assertEquals("New changes", result.release.notes)
        assertTrue(result.release.apkUrl.endsWith("/v1.3.0-beta1/cleanMYTV.apk"))
    }

    @Test fun beta10FollowsBeta2RegardlessOfFeedOrdering() {
        assertEquals("1.3.0-beta10", assertIs<AppUpdateStatus.Available>(select("1.3.0-beta2",
            release("1.3.0-beta3"), release("1.3.0-beta10"), release("1.3.0-beta1"))).release.version)
    }

    @Test fun stableChannelDoesNotOfferBetasOrOlderStableReleases() {
        assertEquals(AppUpdateStatus.Latest, select("1.3.0", release("1.4.0-beta1"), release("1.2.2")))
    }

    @Test fun betaCanUpgradeToStableWithTheSameVersionNumbers() {
        assertEquals("1.3.0", assertIs<AppUpdateStatus.Available>(select("1.3.0-beta2", release("1.3.0"))).release.version)
    }

    @Test fun sameBuildAndOldBuildsAreNeverOffered() {
        assertEquals(AppUpdateStatus.Latest, select("1.3.0-beta2", release("1.3.0-beta2"), release("1.3.0-beta1"), release("1.2.9")))
        assertEquals(AppUpdateStatus.Latest, select("1.3.0", release("1.3.0+rebuild")))
    }

    @Test fun draftAndIncompleteAssetsAreIgnored() {
        assertEquals(AppUpdateStatus.Latest, select("1.3.0-beta1", release("1.4.0", draft = true),
            release("1.5.0", state = "new"), release("1.6.0", apk = "other-app.apk")))
    }

    @Test fun apkMustBelongToTheExactRepositoryAndRelease() {
        for (url in listOf("http://github.com/AmexSaad/cleanMYTV/releases/download/v2.0.0/cleanMYTV.apk",
            "https://github.com/other/repo/releases/download/v2.0.0/cleanMYTV.apk",
            "https://github.com.evil.test/AmexSaad/cleanMYTV/releases/download/v2.0.0/cleanMYTV.apk",
            "https://github.com/AmexSaad/cleanMYTV/releases/download/v1.0.0/cleanMYTV.apk",
            "https://github.com/AmexSaad/cleanMYTV/releases/download/v2.0.0/cleanMYTV.apk?redirect=1")) {
            assertEquals(AppUpdateStatus.Latest, select("1.3.0-beta1", release("2.0.0", url = url)), url)
        }
    }

    @Test fun malformedRepliesAndUnknownInstalledVersionsFailClearly() {
        assertEquals(AppUpdateStatus.Failed, GithubReleases.select("not JSON", "1.3.0-beta1"))
        assertEquals(AppUpdateStatus.Failed, GithubReleases.select("{}", "1.3.0-beta1"))
        assertEquals(AppUpdateStatus.Failed, select("unknown", release("2.0.0")))
    }

    @Test fun checkUsesTheReleaseListAndClosesTheBody() = runBlocking {
        var closed = false
        val body = object : ByteArrayInputStream("[${release("1.4.0-beta1")}]".toByteArray()) {
            override fun close() { closed = true; super.close() }
        }
        val http = object : HttpFetcher {
            override suspend fun get(url: String, etag: String?, rangeFrom: Long?): HttpResponse {
                assertEquals(GithubReleases.URL, url)
                assertNull(etag); assertNull(rangeFrom)
                return HttpResponse(200, null, body, null)
            }
        }
        assertIs<AppUpdateStatus.Available>(GithubReleases.check(http, "1.3.0-beta1"))
        assertTrue(closed)
    }

    @Test fun rateLimitsAndNetworkFailuresAreNotReportedAsUpToDate() = runBlocking {
        for (code in listOf(403, 429, 500)) {
            val http = object : HttpFetcher {
                override suspend fun get(url: String, etag: String?, rangeFrom: Long?) = HttpResponse(code, null, ByteArrayInputStream("[]".toByteArray()), 2)
            }
            assertEquals(AppUpdateStatus.Failed, GithubReleases.check(http, "1.3.0-beta1"))
        }
    }

    @Test fun cancellationIsNotSwallowed() = runBlocking {
        val http = object : HttpFetcher {
            override suspend fun get(url: String, etag: String?, rangeFrom: Long?): HttpResponse = throw CancellationException("closed dialog")
        }
        assertFailsWith<CancellationException> { GithubReleases.check(http, "1.3.0-beta1") }
    }
}
