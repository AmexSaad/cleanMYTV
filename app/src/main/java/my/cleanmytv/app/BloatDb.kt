package my.cleanmytv.app

/** A bilingual snippet of text. */
class Bi(val ms: String, val en: String) {
    fun of(lang: Lang): String = if (lang == Lang.MS) ms else en
}

enum class Group { RECOMMENDED, REVIEW, OTHER }

data class Candidate(
    val pkg: String,
    val group: Group,
    val label: Bi? = null,
    val reason: Bi? = null,
    val warn: Bi? = null
)

private class Known(val group: Group, val label: Bi, val reason: Bi, val warn: Bi? = null)

/**
 * Decides which packages are protected (never shown) and which are worth offering.
 *
 * Philosophy: be conservative. Only a handful of packages are curated; everything else comes from
 * name-based hints or from lists the user imports (see ListImport). Nothing is ever uninstalled;
 * packages are only disabled for user 0 and a restore record is written first.
 */
object BloatDb {

    private val PROTECTED_EXACT = setOf(
        "android",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.vending",
        "com.google.android.tvlauncher",
        "com.google.android.apps.tv.launcherx",
        "com.google.android.leanbacklauncher",
        "com.mitv.tvhome",
        "com.google.android.tungsten.setupwraith",
        "com.google.android.apps.mediashell", // Chromecast built-in
        "com.google.android.webview",
        "com.google.android.permissioncontroller",
        "com.google.android.packageinstaller",
        "com.google.android.networkstack",
        "com.google.android.ext.services",
        "com.google.android.ext.shared",
        "com.google.android.tts",
        "com.google.android.youtube.tv",
        "com.google.android.inputmethod.latin"
    )

    private val PROTECTED_PREFIXES = listOf(
        "android.",
        "com.android.",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.tv.",
        "com.google.android.apps.tv.launcherx"
    )

    /** If a package name contains any of these, we never offer it. */
    private val PROTECTED_CONTAINS = listOf(
        "launcher", "settings", "setup", "provision", "systemui", "overlay", "inputmethod",
        "keyboard", "bluetooth", "wifi", "cec", "hdmi", "tvinput", "tvservice", "permission",
        "installer", "webview", "vending", "wizard", "fallback", "bootstrap", "framework",
        "networkstack", "telephony", "tuner"
    )

    private val KNOWN: Map<String, Known> = mapOf(
        "com.google.android.tvrecommendations" to Known(
            Group.RECOMMENDED,
            Bi("Cadangan Android TV", "Android TV recommendations"),
            Bi("Baris cadangan dan kandungan ditaja pada skrin utama.", "Recommendation and sponsored rows on the home screen.")
        ),
        "com.google.android.leanbacklauncher.recommendations" to Known(
            Group.RECOMMENDED,
            Bi("Servis cadangan Leanback", "Leanback recommendations service"),
            Bi("Servis di belakang tabir untuk baris cadangan.", "Background service behind the recommendation rows.")
        ),
        "com.google.android.feedback" to Known(
            Group.RECOMMENDED,
            Bi("Maklum balas Google", "Google feedback"),
            Bi("Menghantar laporan dan maklum balas ke Google.", "Sends reports and feedback to Google.")
        ),
        "com.google.android.partnersetup" to Known(
            Group.REVIEW,
            Bi("Persediaan rakan kongsi", "Partner setup"),
            Bi("Digunakan semasa persediaan awal sahaja.", "Only used during first-time setup.")
        ),
        "com.google.android.backdrop" to Known(
            Group.REVIEW,
            Bi("Mod ambien Google", "Google ambient mode"),
            Bi("Screensaver bergambar daripada internet.", "Screensaver with pictures from the internet.")
        ),
        "com.google.android.apps.tv.dreamx" to Known(
            Group.REVIEW,
            Bi("Ambien Google TV", "Google TV ambient"),
            Bi("Screensaver Google TV.", "Google TV screensaver.")
        ),
        "com.google.android.videos" to Known(
            Group.REVIEW,
            Bi("Google TV / Play Filem", "Google TV / Play Movies"),
            Bi("Kedai dan pustaka filem Google. Matikan jika tidak guna.", "Google's movie store and library. Disable if you never use it.")
        ),
        "com.google.android.play.games" to Known(
            Group.REVIEW,
            Bi("Play Games", "Play Games"),
            Bi("Perkhidmatan permainan Google.", "Google's gaming service.")
        ),
        "com.google.android.music" to Known(
            Group.REVIEW,
            Bi("Play Muzik (lama)", "Play Music (legacy)"),
            Bi("Aplikasi muzik lama Google.", "Google's old music app.")
        ),
        "com.google.android.youtube.tvmusic" to Known(
            Group.REVIEW,
            Bi("YouTube Music", "YouTube Music"),
            Bi("Matikan jika anda tidak dengar muzik di TV.", "Disable if you never play music on the TV.")
        ),
        "com.google.android.katniss" to Known(
            Group.REVIEW,
            Bi("Carian suara & Pembantu Google", "Voice search & Google Assistant"),
            Bi("Pembantu suara dan carian pada TV.", "Voice assistant and search on the TV."),
            Bi(
                "Butang mikrofon pada alat kawalan jauh akan berhenti berfungsi.",
                "The microphone button on the remote will stop working."
            )
        )
    )

    /** Name tokens (split by '.' and '_') that hint at ads, tracking or demo junk. */
    private val HINTS: Map<String, Bi> = buildMap<String, Bi> {
        val ads = Bi("Nama pakej menunjukkan iklan.", "Package name suggests ads.")
        listOf("ads", "ad", "advert", "adverts", "advertising", "adservice", "adservices").forEach { put(it, ads) }
        val ana = Bi("Nama pakej menunjukkan analitik / penjejakan.", "Package name suggests analytics / tracking.")
        listOf(
            "analytics", "telemetry", "tracking", "tracker", "statistics", "stats", "metrics",
            "acr", "logger", "bugreport"
        ).forEach { put(it, ana) }
        val fb = Bi("Nama pakej menunjukkan maklum balas / promosi / cadangan.", "Package name suggests feedback / promo / recommendations.")
        listOf("feedback", "promo", "promotion", "recommend", "recommendation", "recommendations", "discover")
            .forEach { put(it, fb) }
        val demo = Bi("Nama pakej menunjukkan mod demo kedai.", "Package name suggests retail demo mode.")
        listOf("demo", "storemode", "retailmode").forEach { put(it, demo) }
    }

    fun isProtected(pkg: String, homePackages: Set<String>, selfPkg: String): Boolean {
        if (pkg == selfPkg || pkg in homePackages) return true
        if (pkg in PROTECTED_EXACT) return true
        if (PROTECTED_PREFIXES.any { pkg.startsWith(it) }) return true
        val lower = pkg.lowercase()
        return PROTECTED_CONTAINS.any { lower.contains(it) }
    }

    /** Returns null if the package must not be offered at all. */
    fun classify(pkg: String, homePackages: Set<String>, selfPkg: String): Candidate? {
        // A curated entry is allowed to override the generic "contains" rules, but never the home/self guard.
        if (pkg == selfPkg || pkg in homePackages) return null
        val known = KNOWN[pkg]
        if (known != null) {
            return Candidate(pkg, known.group, known.label, known.reason, known.warn)
        }
        if (isProtected(pkg, homePackages, selfPkg)) return null

        val tokens = pkg.lowercase().split('.', '_')
        val hint = tokens.firstNotNullOfOrNull { HINTS[it] }
        return if (hint != null) {
            Candidate(pkg, Group.REVIEW, null, hint, null)
        } else {
            Candidate(pkg, Group.OTHER)
        }
    }
}
