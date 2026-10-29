package my.cleanmytv.app

import org.json.JSONArray
import org.json.JSONObject

val clockNamesMs = listOf("Digital Bold", "Digital Nipis", "LED", "Analog Klasik", "Analog Minimal", "Flip Gelap", "Flip Cerah", "Jam + Tarikh", "Kad Kalendar", "Hari & Tarikh", "Transparan", "Lingkaran")
val clockNamesEn = listOf("Digital Bold", "Thin Digital", "LED", "Classic Analog", "Minimal Analog", "Dark Flip", "Light Flip", "Clock + Date", "Calendar Card", "Day & Date", "Transparent", "Ring")
val wallpaperNamesMs = listOf("Ombak Biru", "Aurora", "Gunung", "Tasik", "Pantai", "Hutan", "Air Terjun", "Senja", "Langit Malam", "Angkasa", "Geometri", "Kaca Biru", "Navy Minimal", "Putih Lembut", "Pasir", "Daun Tropika", "Bukit Hijau", "Awan", "Hujan", "Neon Biru")
val wallpaperNamesEn = listOf("Blue Waves", "Aurora", "Mountains", "Lake", "Beach", "Forest", "Waterfall", "Sunset", "Night Sky", "Space", "Geometry", "Blue Glass", "Minimal Navy", "Soft White", "Dunes", "Tropical Leaves", "Green Hills", "Clouds", "Rain", "Blue Neon")

/** Shared, bounded configuration used by both the phone editor and TV renderer. */
data class LauncherConfig(
    val clock: Int = 0, val wallpaper: Int = 0, val vertical: Boolean = false,
    val clockX: Float = .06f, val clockY: Float = .09f, val clockSize: Float = 1f,
    val showClock: Boolean = true, val showDate: Boolean = true,
    val hour24: Boolean = true, val darkText: Boolean = false, val dim: Float = .18f,
    val lang: String = "ms", val apps: List<String> = emptyList()
) {
    fun json(): String = JSONObject().apply {
        put("version", 1); put("clock", clock); put("wallpaper", wallpaper)
        put("vertical", vertical); put("x", clockX); put("y", clockY); put("size", clockSize)
        put("showClock", showClock); put("showDate", showDate); put("hour24", hour24)
        put("darkText", darkText); put("dim", dim); put("lang", lang)
        put("apps", JSONArray(apps))
    }.toString()

    companion object {
        fun parse(raw: String): LauncherConfig {
            require(raw.length <= 20000) { "Configuration too large" }
            val o = JSONObject(raw)
            require(o.optInt("version", 1) == 1) { "Unsupported configuration" }
            fun number(key: String, fallback: Float, min: Float, max: Float): Float =
                o.optDouble(key, fallback.toDouble()).toFloat().let { if (it.isFinite()) it.coerceIn(min, max) else fallback }
            val list = o.optJSONArray("apps") ?: JSONArray()
            return LauncherConfig(
                clock = o.optInt("clock").coerceIn(0, 11), wallpaper = o.optInt("wallpaper").coerceIn(0, 19),
                vertical = o.optBoolean("vertical"), clockX = number("x", .06f, 0f, 1f), clockY = number("y", .09f, 0f, 1f),
                clockSize = number("size", 1f, .6f, 1.5f), showClock = o.optBoolean("showClock", true),
                showDate = o.optBoolean("showDate", true), hour24 = o.optBoolean("hour24", true),
                darkText = o.optBoolean("darkText"), dim = number("dim", .18f, 0f, .7f),
                lang = if (o.optString("lang") == "en") "en" else "ms",
                apps = (0 until minOf(list.length(), 60)).map { list.optString(it) }.filter { PkgName.valid(it) && it.length <= 200 }.distinct()
            )
        }
    }
}

/** Component strings come from package manager output, never arbitrary shell fragments. */
fun launcherComponent(raw: String): String? = raw.lineSequence().map { it.trim() }
    .lastOrNull { Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+/[A-Za-z0-9_.$]+$").matches(it) }

data class LauncherApp(val pkg: String, val label: String = pkg)
