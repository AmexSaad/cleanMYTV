package my.cleanmytv.app

import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.HttpURLConnection
import java.net.URL

object NetScan {

    fun localIpv4(ctx: Context): String? {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return null
        val lp = cm.getLinkProperties(net) ?: return null
        return lp.linkAddresses
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress }
            ?.hostAddress
    }

    /** Looks for hosts with the ADB port open on the phone's /24 network. */
    suspend fun scan(ctx: Context, port: Int): List<String> = coroutineScope {
        val me = localIpv4(ctx) ?: return@coroutineScope emptyList<String>()
        val base = me.substringBeforeLast('.')
        val gate = Semaphore(48)
        (1..254)
            .map { n ->
                async(Dispatchers.IO) {
                    val h = "$base.$n"
                    if (h == me) null
                    else gate.withPermit { if (Adb.portOpen(h, port, 350)) h else null }
                }
            }
            .awaitAll()
            .filterNotNull()
    }
}

object ListImport {

    /** One package per line, anything after '#' is a comment. Matches the community debloat.txt style. */
    fun parse(text: String): List<String> =
        text.lineSequence()
            .map { it.substringBefore('#').trim() }
            .map { it.split(Regex("\\s+")).firstOrNull().orEmpty() }
            .filter { PkgName.valid(it) }
            .distinct()
            .toList()

    /** Turns a github.com/.../blob/... page link into its raw file link. */
    fun toRaw(url: String): String {
        val m = Regex("^https://github\\.com/([^/]+)/([^/]+)/blob/(.+)$").matchEntire(url.trim())
        return if (m != null) {
            "https://raw.githubusercontent.com/${m.groupValues[1]}/${m.groupValues[2]}/${m.groupValues[3]}"
        } else {
            url.trim()
        }
    }

    suspend fun fetch(url: String): String = withContext(Dispatchers.IO) {
        val raw = toRaw(url)
        require(raw.startsWith("https://")) { "https only" }
        val conn = URL(raw).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            val code = conn.responseCode
            if (code !in 200..299) error("HTTP $code")
            conn.inputStream.bufferedReader().use { r ->
                val buf = CharArray(8 * 1024)
                val sb = StringBuilder()
                while (sb.length < 1_000_000) {
                    val n = r.read(buf)
                    if (n < 0) break
                    sb.append(buf, 0, n)
                }
                sb.toString()
            }
        } finally {
            conn.disconnect()
        }
    }
}
