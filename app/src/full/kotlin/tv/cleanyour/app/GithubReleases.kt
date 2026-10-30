package tv.cleanyour.app

import java.net.URI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import tv.cleanyour.core.registry.HttpFetcher

/** No automatic traffic. Both update entry points call this only after a user taps Check. */
internal object GithubReleases {
    const val URL = "https://api.github.com/repos/AmexSaad/cleanMYTV/releases?per_page=100"
    private const val MAX_BODY = 2_000_000

    suspend fun check(http: HttpFetcher, current: String): AppUpdateStatus = withContext(Dispatchers.IO) {
        try {
            val response = http.get(URL, null, null)
            val body = response.body ?: return@withContext AppUpdateStatus.Failed
            body.use { input ->
                if (response.code != 200) return@withContext AppUpdateStatus.Failed
                val text = buildString {
                    val buffer = CharArray(8192)
                    val reader = input.reader(Charsets.UTF_8)
                    while (true) {
                        val n = reader.read(buffer)
                        if (n < 0) break
                        require(length + n <= MAX_BODY) { "Release response too large" }
                        append(buffer, 0, n)
                    }
                }
                select(text, current)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AppUpdateStatus.Failed
        }
    }

    internal fun select(text: String, current: String): AppUpdateStatus =
        runCatching { selectRelease(text, current) }.getOrDefault(AppUpdateStatus.Failed)

    private fun selectRelease(text: String, current: String): AppUpdateStatus {
        val installed = AppVersion.parse(current) ?: return AppUpdateStatus.Failed
        val releases = Json.parseToJsonElement(text) as? JsonArray ?: return AppUpdateStatus.Failed
        val candidates = releases.mapNotNull { element ->
            val release = element as? JsonObject ?: return@mapNotNull null
            if (release["draft"]?.jsonPrimitive?.booleanOrNull != false) return@mapNotNull null
            val tag = release["tag_name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            val version = AppVersion.parse(tag) ?: return@mapNotNull null
            val prerelease = release["prerelease"]?.jsonPrimitive?.booleanOrNull ?: return@mapNotNull null
            // Stable installations stay on the stable channel; beta installations see both.
            if (installed.pre == null && (prerelease || version.pre != null)) return@mapNotNull null
            if (version <= installed) return@mapNotNull null
            val assets = release["assets"] as? JsonArray ?: return@mapNotNull null
            val apk = assets.mapNotNull { it as? JsonObject }.firstOrNull { asset ->
                asset["name"]?.jsonPrimitive?.contentOrNull == "cleanMYTV.apk" &&
                    asset["state"]?.jsonPrimitive?.contentOrNull == "uploaded" &&
                    (asset["size"]?.jsonPrimitive?.longOrNull ?: 0) in 1L..100_000_000L &&
                    validApkUrl(asset["browser_download_url"]?.jsonPrimitive?.contentOrNull.orEmpty(), tag)
            } ?: return@mapNotNull null
            version to AppRelease(tag.removePrefix("v"),
                release["body"]?.jsonPrimitive?.contentOrNull.orEmpty().take(6000),
                apk.getValue("browser_download_url").jsonPrimitive.content)
        }
        return candidates.maxByOrNull { it.first }?.let { AppUpdateStatus.Available(it.second) }
            ?: AppUpdateStatus.Latest
    }

    private fun validApkUrl(url: String, tag: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host == "github.com" && uri.rawUserInfo == null &&
            uri.port == -1 && uri.rawQuery == null && uri.rawFragment == null &&
            uri.rawPath == "/AmexSaad/cleanMYTV/releases/download/$tag/cleanMYTV.apk"
    }.getOrDefault(false)
}

/** Numeric versions and prerelease ordering; never offer a downgrade or the same build. */
internal data class AppVersion(val main: List<Int>, val pre: List<String>?) : Comparable<AppVersion> {
    override fun compareTo(other: AppVersion): Int {
        for (i in 0..2) main[i].compareTo(other.main[i]).takeIf { it != 0 }?.let { return it }
        if (pre == null) return if (other.pre == null) 0 else 1
        if (other.pre == null) return -1
        for (i in 0 until maxOf(pre.size, other.pre.size)) {
            val a = pre.getOrNull(i) ?: return -1
            val b = other.pre.getOrNull(i) ?: return 1
            val an = a.toIntOrNull(); val bn = b.toIntOrNull()
            val order = when {
                an != null && bn != null -> an.compareTo(bn)
                an != null -> -1
                bn != null -> 1
                else -> a.compareTo(b)
            }
            if (order != 0) return order
        }
        return 0
    }

    companion object {
        private val format = Regex("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(?:-([0-9A-Za-z]+(?:[.-][0-9A-Za-z]+)*))?(?:\\+[0-9A-Za-z]+(?:[.-][0-9A-Za-z]+)*)?")
        fun parse(text: String): AppVersion? {
            if (text.length > 80) return null
            val match = format.matchEntire(text) ?: return null
            val main = (1..3).map { match.groupValues[it].toIntOrNull() ?: return null }
            val suffix = match.groupValues[4].takeIf { it.isNotEmpty() }
            val pre = suffix?.split('.')?.flatMap { part ->
                // This project's existing tags use beta1/beta2, including beta10.
                Regex("(alpha|beta|rc)([0-9]+)").matchEntire(part)?.let { listOf(it.groupValues[1], it.groupValues[2]) } ?: listOf(part)
            }
            return AppVersion(main, pre)
        }
    }
}
