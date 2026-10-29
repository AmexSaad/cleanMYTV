package my.cleanmytv.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { CONNECT, ANALYSING, REVIEW, WORKING, DONE, HISTORY }
enum class Mode { CLEAN, RESTORE, REAPPLY }

private val HOST_RE = Regex("^[A-Za-z0-9.-]{1,253}$")

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx: Context get() = getApplication<Application>()
    private val prefs = ctx.getSharedPreferences("cleanmytv", Context.MODE_PRIVATE)
    private val store = RecordStore(ctx)
    private var job: Job? = null
    private var tv: Tv? = null

    // ---- UI state ----
    var lang by mutableStateOf(Lang.fromCode(prefs.getString("lang", null) ?: Lang.systemDefault().code))
        private set
    var screen by mutableStateOf(Screen.CONNECT)
        private set

    var host by mutableStateOf(prefs.getString("host", "") ?: "")
    var port by mutableStateOf(prefs.getString("port", "5555") ?: "5555")
    var error by mutableStateOf<((S) -> String)?>(null)
        private set

    var scanning by mutableStateOf(false)
        private set
    var scanDone by mutableStateOf(false)
        private set
    val found = mutableStateListOf<String>()

    var stage by mutableStateOf(Stage.CONNECT)
        private set
    var analysis by mutableStateOf<Analysis?>(null)
        private set
    val selected = mutableStateMapOf<String, Boolean>()
    var query by mutableStateOf("")
    var expandOther by mutableStateOf(false)
    var notice by mutableStateOf<((S) -> String)?>(null)

    // Working / done state
    var mode by mutableStateOf(Mode.CLEAN)
        private set
    var total by mutableStateOf(0)
        private set
    var doneCount by mutableStateOf(0)
        private set
    var currentPkg by mutableStateOf("")
        private set
    var okCount by mutableStateOf(0)
        private set
    val failed = mutableStateListOf<OpResult>()

    // History
    val records = mutableStateListOf<RestoreRecord>()

    // Import dialog
    var importOpen by mutableStateOf(false)
    var importText by mutableStateOf("")
    var importUrl by mutableStateOf("")
    var importBusy by mutableStateOf(false)
        private set
    var importMsg by mutableStateOf<((S) -> String)?>(null)
        private set

    // ---- Language ----
    fun changeLanguage(l: Lang) {
        lang = l
        prefs.edit().putString("lang", l.code).apply()
    }

    // ---- Discovery ----
    fun scanNetwork() {
        if (scanning) return
        val p = port.trim().toIntOrNull() ?: 5555
        scanning = true
        scanDone = false
        found.clear()
        viewModelScope.launch {
            try {
                val hosts = NetScan.scan(ctx, p)
                found.addAll(hosts)
                if (hosts.size == 1 && host.isBlank()) host = hosts.first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // ignore: user can still type the IP
            } finally {
                scanning = false
                scanDone = true
            }
        }
    }

    // ---- Connect + analyse ----
    private fun failToConnect(msg: (S) -> String) {
        error = msg
        tv?.close()
        tv = null
        screen = Screen.CONNECT
    }

    private fun parseTarget(): Pair<String, Int>? {
        val h = host.trim()
        val p = port.trim().toIntOrNull()
        if (!HOST_RE.matches(h) || p == null || p !in 1..65535) {
            error = { it.errInvalidIp }
            return null
        }
        prefs.edit().putString("host", h).putString("port", p.toString()).apply()
        return h to p
    }

    fun connectAndAnalyse() {
        val (h, p) = parseTarget() ?: return
        error = null
        notice = null
        stage = Stage.CONNECT
        screen = Screen.ANALYSING
        job?.cancel()
        job = viewModelScope.launch {
            try {
                if (!Adb.portOpen(h, p)) {
                    failToConnect { it.errPortClosed(h, p) }
                    return@launch
                }
                tv?.close()
                val conn = Adb.connect(ctx, h, p)
                tv = conn
                val result = withContext(Dispatchers.IO) {
                    Analyzer.analyse(conn, ctx.packageName) { stage = it }
                }
                analysis = result
                selected.clear()
                result.candidates.forEach { selected[it.pkg] = it.group == Group.RECOMMENDED }
                query = ""
                expandOther = false
                screen = Screen.REVIEW
            } catch (e: TimeoutCancellationException) {
                failToConnect { it.errTimeout }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val m = e.message ?: e.javaClass.simpleName
                failToConnect { it.errConnect(m) }
            }
        }
    }

    // ---- Selection helpers ----
    fun selectedList(): List<String> =
        analysis?.candidates?.map { it.pkg }?.filter { selected[it] == true }.orEmpty()

    fun setGroup(group: Group, value: Boolean) {
        analysis?.candidates?.filter { it.group == group }?.forEach { selected[it.pkg] = value }
    }

    // ---- Import list ----
    fun fetchImport() {
        val url = importUrl.trim()
        if (url.isEmpty() || importBusy) return
        importBusy = true
        importMsg = null
        viewModelScope.launch {
            try {
                importText = ListImport.fetch(url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val m = e.message ?: e.javaClass.simpleName
                importMsg = { it.importFailed(m) }
            } finally {
                importBusy = false
            }
        }
    }

    fun applyImport() {
        val a = analysis ?: return
        val wanted = ListImport.parse(importText)
        val known = a.candidates.map { it.pkg }.toSet()
        val hits = wanted.filter { it in known }
        hits.forEach { selected[it] = true }
        if (hits.isNotEmpty()) expandOther = true
        val matched = hits.size
        val listed = wanted.size
        notice = { it.importMatched(matched, listed) }
        importOpen = false
        importText = ""
        importUrl = ""
        importMsg = null
    }

    // ---- Clean ----
    fun startClean() {
        val a = analysis ?: return
        val conn = tv ?: return
        val list = selectedList()
        if (list.isEmpty()) return

        mode = Mode.CLEAN
        total = list.size
        doneCount = 0
        okCount = 0
        currentPkg = ""
        failed.clear()
        screen = Screen.WORKING

        val record = RestoreRecord(
            id = System.currentTimeMillis().toString(),
            time = System.currentTimeMillis(),
            host = conn.host,
            port = conn.port,
            brand = a.info.brand,
            model = a.info.model,
            packages = emptyList()
        )

        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val okList = mutableListOf<String>()
            try {
                list.forEachIndexed { i, pkg ->
                    ensureActive()
                    doneCount = i
                    currentPkg = pkg
                    // Write-ahead: the package is on the undo list *before* it is touched, so a crash or a
                    // dropped Wi-Fi can never leave something disabled that the app does not know about.
                    store.save(record.copy(packages = okList + pkg))
                    val r = Cleaner.disable(conn, pkg)
                    if (r.ok) {
                        okList += pkg
                        okCount = okList.size
                    } else {
                        failed += r
                        store.save(record.copy(packages = okList.toList()))
                    }
                }
                doneCount = list.size
            } catch (e: CancellationException) {
                // user pressed Stop: keep what was done so far
            } catch (e: Throwable) {
                failed += OpResult(currentPkg, false, e.message ?: e.javaClass.simpleName)
            } finally {
                // If Stop hit while a package was in flight, it stays on the undo list on purpose
                // (restore treats "already enabled" as fine). Drop the file only if every attempt failed.
                if (okList.isEmpty() && failed.size == list.size) store.delete(record.id)
                screen = Screen.DONE
            }
        }
    }

    fun stopWork() {
        job?.cancel()
    }

    // ---- History / restore ----
    fun openHistory() {
        records.clear()
        records.addAll(store.list())
        error = null
        screen = Screen.HISTORY
    }

    fun runRecord(rec: RestoreRecord, reapply: Boolean) {
        mode = if (reapply) Mode.REAPPLY else Mode.RESTORE
        total = rec.packages.size
        doneCount = 0
        okCount = 0
        currentPkg = ""
        failed.clear()
        error = null
        screen = Screen.WORKING

        job?.cancel()
        job = viewModelScope.launch {
            try {
                if (!Adb.portOpen(rec.host, rec.port)) {
                    error = { it.errPortClosed(rec.host, rec.port) }
                    screen = Screen.HISTORY
                    return@launch
                }
                val existing = tv
                val conn = if (existing != null && existing.host == rec.host && existing.port == rec.port) {
                    existing
                } else {
                    existing?.close()
                    Adb.connect(ctx, rec.host, rec.port).also { tv = it }
                }

                withContext(Dispatchers.IO) {
                    val disabledNow = Analyzer.parsePkgs(conn.sh("pm list packages -d"))
                    val remaining = mutableListOf<String>()
                    rec.packages.forEachIndexed { i, pkg ->
                        ensureActive()
                        doneCount = i
                        currentPkg = pkg
                        if (reapply) {
                            if (pkg in disabledNow) {
                                okCount++
                            } else {
                                val r = Cleaner.disable(conn, pkg)
                                if (r.ok) okCount++ else failed += r
                            }
                        } else {
                            if (pkg !in disabledNow) {
                                okCount++ // already enabled
                            } else {
                                val r = Cleaner.enable(conn, pkg)
                                if (r.ok) okCount++ else {
                                    failed += r
                                    remaining += pkg
                                }
                            }
                        }
                    }
                    doneCount = rec.packages.size
                    if (!reapply) {
                        if (remaining.isEmpty()) store.delete(rec.id)
                        else store.save(rec.copy(packages = remaining))
                    }
                }
                screen = Screen.DONE
            } catch (e: TimeoutCancellationException) {
                error = { it.errTimeout }
                screen = Screen.HISTORY
            } catch (e: CancellationException) {
                screen = Screen.DONE
            } catch (e: Throwable) {
                val m = e.message ?: e.javaClass.simpleName
                error = { it.errConnect(m) }
                screen = Screen.HISTORY
            }
        }
    }

    // ---- Navigation ----
    fun goHome() {
        job?.cancel()
        tv?.close()
        tv = null
        error = null
        screen = Screen.CONNECT
    }

    /** Returns true if the back press was handled. */
    fun onBack(): Boolean {
        return when (screen) {
            Screen.CONNECT -> false
            Screen.WORKING -> true // block back while changing things; use the Stop button
            Screen.HISTORY, Screen.ANALYSING, Screen.REVIEW, Screen.DONE -> {
                goHome()
                true
            }
        }
    }

    override fun onCleared() {
        job?.cancel()
        tv?.close()
        super.onCleared()
    }
}
