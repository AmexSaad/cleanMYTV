package tv.cleanyour.app

import tv.cleanyour.core.flows.StoreRules
import tv.cleanyour.core.flows.UpdateChecker
import tv.cleanyour.core.registry.ArtifactFetcher
import tv.cleanyour.core.registry.HttpFetcher
import tv.cleanyour.core.trust.DownloadHosts

// The full build (GitHub and F-Droid, STORES-PLAN 1): everything the app did before the store
// flavours, unchanged.
object Store : StorePorts {
    override val flavour = "full"
    override val hosts: Set<String> = DownloadHosts.ALL
    override val rules = StoreRules.FULL
    override val updatesMayGoOff = rules.updatesOff
    override val releasePage: String = RELEASE_PAGE

    // Both the visible Update button and Settings share the same validated release feed.
    override suspend fun releaseStatus(http: HttpFetcher, current: String): AppUpdateStatus =
        GithubReleases.check(http, current)

    override fun updateChecker(http: HttpFetcher) = UpdateChecker { current ->
        when (val result = releaseStatus(http, current)) {
            is AppUpdateStatus.Available -> result.release.version
            AppUpdateStatus.Latest -> ""
            else -> null
        }
    }

    internal fun offered(tag: String, current: String): Boolean {
        val candidate = AppVersion.parse(tag) ?: return false
        val installed = AppVersion.parse(current) ?: return false
        return candidate > installed && (installed.pre != null || candidate.pre == null)
    }

    override fun addOnFetcher(make: () -> ArtifactFetcher): ArtifactFetcher = make()

    const val RELEASES_URL = "https://api.github.com/repos/AmexSaad/cleanMYTV/releases?per_page=100"
    const val RELEASE_PAGE = "https://github.com/AmexSaad/cleanMYTV/releases"
}
