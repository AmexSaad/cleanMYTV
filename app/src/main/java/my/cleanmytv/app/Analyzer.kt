package my.cleanmytv.app

enum class Stage { CONNECT, INFO, PACKAGES, HOME, SORT }

data class TvInfo(
    val brand: String,
    val model: String,
    val release: String,
    val sdk: Int,
    val homePkg: String?
)

data class Analysis(
    val info: TvInfo,
    val candidates: List<Candidate>,
    /** Every enabled system package on the TV, including protected ones. */
    val enabledSystem: Set<String>,
    val homePackages: Set<String>,
    val alreadyDisabled: Int
)

object PkgName {
    val REGEX = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+$")
    fun valid(s: String) = REGEX.matches(s)
}

object Analyzer {

    fun parsePkgs(out: String): Set<String> =
        out.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:").substringBefore(' ').trim() }
            .filter { PkgName.valid(it) }
            .toSet()

    private fun componentLines(out: String): List<String> =
        out.lineSequence()
            .map { it.trim() }
            .filter { it.contains("/") && !it.contains(" ") && !it.startsWith("/") }
            .toList()

    private fun homeInfo(tv: Tv): Pair<String?, Set<String>> {
        val main = runCatching {
            tv.sh("cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME")
        }.getOrDefault("")
        val defaultHome = componentLines(main).lastOrNull()?.substringBefore("/")

        // Every package that answers HOME counts as "home" and stays protected (setup wizards included).
        val all = runCatching {
            tv.sh("cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME")
        }.getOrDefault("")
        val set = componentLines(all).map { it.substringBefore("/") }.toMutableSet()
        if (defaultHome != null) set += defaultHome
        return defaultHome to set
    }

    fun analyse(tv: Tv, selfPkg: String, onStage: (Stage) -> Unit): Analysis {
        onStage(Stage.INFO)
        fun prop(key: String) = tv.sh("getprop $key").trim()
        val brand = prop("ro.product.brand").ifEmpty { prop("ro.product.manufacturer") }
        val model = prop("ro.product.model")
        val release = prop("ro.build.version.release")
        val sdk = prop("ro.build.version.sdk").toIntOrNull() ?: 0

        onStage(Stage.PACKAGES)
        val disabled = parsePkgs(tv.sh("pm list packages -d"))
        var enabledSys = parsePkgs(tv.sh("pm list packages -e -s"))
        if (enabledSys.isEmpty()) {
            enabledSys = parsePkgs(tv.sh("pm list packages -s")) - disabled
        }

        onStage(Stage.HOME)
        val (home, homes) = homeInfo(tv)

        onStage(Stage.SORT)
        val candidates = (parsePkgs(tv.sh("pm list packages -e")) + enabledSys)
            .mapNotNull { BloatDb.classify(it, homes, selfPkg) }.map { c -> if (c.group == Group.OTHER) c.copy(reason = Bi("Pilihan manual: pilih app yang anda kenal.", "Manual selection: choose apps you recognise."), warn = Bi("Belum dinilai. Boleh menjejaskan servis Unifi. Pulihkan melalui Sejarah jika perlu. Firmware mungkin menyekat perubahan.", "Not reviewed. May affect Unifi services. Restore from History if needed. Firmware may block changes.")) else c }
            .sortedWith(compareBy<Candidate>({ it.group.ordinal }, { it.pkg }))

        return Analysis(
            info = TvInfo(brand, model, release, sdk, home),
            candidates = candidates,
            enabledSystem = enabledSys,
            homePackages = homes,
            alreadyDisabled = disabled.size
        )
    }
}

data class OpResult(val pkg: String, val ok: Boolean, val msg: String)

object Cleaner {
    /** pm disable-user keeps the app installed; it can be re-enabled from the TV's own Settings → Apps. */
    fun disable(tv: Tv, pkg: String): OpResult {
        if (!PkgName.valid(pkg)) return OpResult(pkg, false, "invalid package name")
        val (code, out) = tv.run("pm disable-user --user 0 $pkg")
        val clean = out.trim()
        val ok = code == 0 && clean.contains("disabled", ignoreCase = true) &&
            !clean.contains("exception", ignoreCase = true)
        return OpResult(pkg, ok, clean.lineSequence().firstOrNull().orEmpty())
    }

    fun enable(tv: Tv, pkg: String): OpResult {
        if (!PkgName.valid(pkg)) return OpResult(pkg, false, "invalid package name")
        val (code, out) = tv.run("pm enable --user 0 $pkg")
        val clean = out.trim()
        val ok = code == 0 && clean.contains("enabled", ignoreCase = true) &&
            !clean.contains("exception", ignoreCase = true)
        return OpResult(pkg, ok, clean.lineSequence().firstOrNull().orEmpty())
    }
}
