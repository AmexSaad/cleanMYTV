package my.cleanmytv.app

import android.content.Context
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/** Thin wrapper over a connected ADB session. */
class Tv(val dadb: Dadb, val host: String, val port: Int) : AutoCloseable {
    /** Runs a shell command on the TV and returns stdout + stderr. */
    fun sh(cmd: String): String {
        val r = dadb.shell(cmd)
        return r.output + r.errorOutput
    }

    /** Runs a shell command and returns exit code + combined output. */
    fun run(cmd: String): Pair<Int, String> {
        val r = dadb.shell(cmd)
        return r.exitCode to (r.output + r.errorOutput)
    }

    override fun close() {
        runCatching { dadb.close() }
    }
}

object Adb {
    /** The phone's own ADB key. The TV remembers it after you tick "Always allow". */
    private fun keyPair(ctx: Context): AdbKeyPair {
        val priv = File(ctx.filesDir, "adbkey")
        val pub = File(ctx.filesDir, "adbkey.pub")
        if (!priv.exists() || !pub.exists()) AdbKeyPair.generate(priv, pub)
        return AdbKeyPair.read(priv, pub)
    }

    suspend fun portOpen(host: String, port: Int, timeoutMs: Int = 1500): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Socket().use { it.connect(InetSocketAddress(host, port), timeoutMs) }
                true
            } catch (e: Exception) {
                false
            }
        }

    /**
     * Connects and authenticates. The TV shows an "Allow USB debugging?" prompt the first time and the
     * handshake blocks until it is answered, so the blocking call runs on its own thread and we wait
     * with a timeout the user can actually cancel.
     */
    suspend fun connect(ctx: Context, host: String, port: Int, timeoutMs: Long = 90_000): Tv {
        val result = CompletableDeferred<Dadb>()
        val abandoned = AtomicBoolean(false)
        thread(isDaemon = true, name = "cleanmytv-adb-connect") {
            try {
                val d = Dadb.create(host, port, keyPair(ctx))
                if (abandoned.get()) runCatching { d.close() } else result.complete(d)
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
        try {
            val d = withTimeout(timeoutMs) { result.await() }
            return Tv(d, host, port)
        } catch (t: Throwable) {
            abandoned.set(true)
            throw t
        }
    }
}
