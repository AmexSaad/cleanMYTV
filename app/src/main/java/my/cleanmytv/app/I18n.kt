package my.cleanmytv.app

import java.util.Locale

enum class Lang(val code: String, val label: String) {
    MS("ms", "BM"),
    EN("en", "EN");

    companion object {
        fun fromCode(code: String?): Lang = values().firstOrNull { it.code == code } ?: EN

        /** Default: Bahasa Melayu if the phone is set to Malay/Indonesian, otherwise English. */
        fun systemDefault(): Lang {
            val l = Locale.getDefault().language
            return if (l == "ms" || l == "in" || l == "id") MS else EN
        }
    }
}

/** Both languages live side by side so a missing translation is easy to spot. */
class S(val lang: Lang) {
    private fun t(ms: String, en: String) = if (lang == Lang.MS) ms else en

    // ---- General ----
    val tagline get() = t("TV anda, tanpa sampah iklan.", "Your TV, minus the clutter.")
    val stepConnect get() = t("Sambung", "Connect")
    val stepAnalyse get() = t("Analisis", "Analyse")
    val stepClean get() = t("Bersih", "Clean")
    val cancel get() = t("Batal", "Cancel")
    val ok get() = t("OK", "OK")
    val back get() = t("Kembali", "Back")
    val close get() = t("Tutup", "Close")
    val home get() = t("Utama", "Home")

    // ---- Connect ----
    val connectTitle get() = t("Sambung ke TV anda", "Connect to your TV")
    val connectIntro
        get() = t(
            "Telefon dan TV mesti guna Wi-Fi yang sama. Tiada root, tiada kabel, tiada apa dipadam. Semua boleh dipulihkan.",
            "Phone and TV must be on the same Wi-Fi. No root, no cable, nothing deleted. Everything can be restored."
        )
    val ipLabel get() = t("Alamat IP TV", "TV IP address")
    val portLabel get() = "Port"
    val scanBtn get() = t("Cari TV di rangkaian", "Find TVs on my network")
    val scanning get() = t("Mengimbas rangkaian…", "Scanning network…")
    val scanNone
        get() = t(
            "Tiada TV dijumpai. Pastikan USB debugging / ADB rangkaian dihidupkan, atau taip IP sendiri.",
            "No TV found. Make sure USB debugging / network ADB is on, or type the IP yourself."
        )
    val foundTvs get() = t("TV dijumpai", "TVs found")
    val connectBtn get() = t("Sambung & analisis", "Connect & analyse")
    val historyBtn get() = t("Pulih / Sejarah", "Restore / History")
    val setupTitle get() = t("Hidupkan USB debugging pada TV", "Turn on USB debugging on your TV")
    val setupSteps
        get() = if (lang == Lang.MS) listOf(
            "Tetapan → Pilihan Peranti → Perihal.",
            "Tekan OK pada “Build” sebanyak tujuh kali.",
            "Kembali → Pilihan pembangun → hidupkan “USB debugging” (dan “ADB rangkaian” jika ada).",
            "Biarkan TV hidup. IP TV ada di Tetapan → Rangkaian.",
            "Bila TV tanya “Allow USB debugging?”, pilih Always allow."
        ) else listOf(
            "Settings → Device Preferences → About.",
            "Press OK on “Build” seven times.",
            "Back → Developer options → turn on “USB debugging” (and “Network ADB” if shown).",
            "Keep the TV on. The TV's IP is under Settings → Network.",
            "When the TV asks “Allow USB debugging?”, choose Always allow."
        )
    val legal
        get() = t(
            "cleanMYTV ialah projek bebas dan tiada kaitan dengan mana-mana pengeluar TV. Idea fungsinya datang daripada projek sumber terbuka CleanYourTV (MIT). Guna pada TV milik sendiri dan atas risiko sendiri.",
            "cleanMYTV is an independent project, not affiliated with any TV maker. Its idea comes from the open-source CleanYourTV project (MIT). Use it on a TV you own, at your own risk."
        )

    // ---- Errors ----
    val errInvalidIp get() = t("IP atau port tidak sah.", "Invalid IP or port.")
    fun errPortClosed(host: String, port: Int) = t(
        "Port $port pada $host tertutup. Hidupkan USB debugging / ADB rangkaian pada TV dan pastikan ia atas Wi-Fi yang sama.",
        "Port $port on $host is closed. Turn on USB debugging / network ADB on the TV and make sure it is on the same Wi-Fi."
    )
    val errTimeout
        get() = t(
            "Tiada jawapan. Terima “Allow USB debugging?” pada TV, kemudian cuba lagi.",
            "No answer. Accept “Allow USB debugging?” on the TV, then try again."
        )
    fun errConnect(msg: String) = t("Gagal: $msg", "Failed: $msg")

    // ---- Analysing ----
    val stageConnect
        get() = t(
            "Menyambung… Terima “Allow USB debugging?” pada TV.",
            "Connecting… Accept “Allow USB debugging?” on the TV."
        )
    val stageInfo get() = t("Membaca maklumat TV…", "Reading TV info…")
    val stagePackages get() = t("Membaca senarai aplikasi…", "Reading app list…")
    val stageHome get() = t("Mengesan skrin utama…", "Detecting the home app…")
    val stageSort get() = t("Menyusun pelan pembersihan…", "Building the cleaning plan…")

    // ---- Review ----
    val reviewTitle get() = t("Pelan pembersihan", "Cleaning plan")
    val lblBrand get() = t("Jenama", "Brand")
    val lblModel get() = "Model"
    val lblAndroid get() = "Android"
    val lblHome get() = t("Skrin utama", "Home app")
    val lblUnknown get() = t("tidak diketahui", "unknown")
    val protectedNote
        get() = t(
            "Skrin utama semasa dan pakej sistem penting dilindungi. Ia tidak akan disentuh.",
            "The current home app and essential system packages are protected. They are never touched."
        )
    val nothingFound get() = t("Tiada pakej untuk dibersihkan dijumpai.", "No packages to clean were found.")
    val groupRec get() = t("Disyorkan", "Recommended")
    val groupRecDesc get() = t("Selamat dimatikan pada kebanyakan TV.", "Safe to disable on most TVs.")
    val groupRev get() = t("Semak sendiri", "Review first")
    val groupRevDesc
        get() = t(
            "Dikesan daripada nama pakej (iklan, analitik, telemetri…) atau pilihan. Tidak dipilih automatik.",
            "Found from the package name (ads, analytics, telemetry…) or optional extras. Not selected automatically."
        )
    val groupOth get() = t("Lanjutan: pakej sistem lain", "Advanced: other system packages")
    val groupOthDesc
        get() = t(
            "Hanya jika anda tahu apa yang dibuat. Pakej kritikal disembunyikan.",
            "Only if you know what you are doing. Critical packages are hidden."
        )
    val showAll get() = t("Tunjuk", "Show")
    val hideAll get() = t("Sembunyi", "Hide")
    fun selectedCount(n: Int) = t("$n dipilih", "$n selected")
    val searchHint get() = t("Cari pakej", "Search packages")
    val selectGroup get() = t("Pilih semua", "Select all")
    val clearGroup get() = t("Kosongkan", "Clear")
    fun cleanBtn(n: Int) = t("Bersihkan $n item", "Clean $n items")
    val confirmTitle get() = t("Sahkan pembersihan", "Confirm cleaning")
    fun confirmBody(n: Int) = t(
        "$n pakej akan dimatikan (bukan dipadam). Rekod pemulihan disimpan dahulu, jadi anda boleh pulihkan bila-bila masa. Teruskan?",
        "$n packages will be disabled (not deleted). A restore record is saved first, so you can undo this any time. Continue?"
    )
    val confirmGo get() = t("Ya, bersihkan", "Yes, clean")
    val warnPrefix get() = t("Perhatian", "Heads-up")

    // ---- Import list ----
    val importBtn get() = t("Import senarai", "Import a list")
    val importTitle get() = t("Import senarai debloat", "Import a debloat list")
    val importHint
        get() = t(
            "Tampal senarai pakej (satu setiap baris, komen bermula dengan #), atau ambil dari pautan GitHub. Hanya pakej yang ada & selamat pada TV ini akan dipilih.",
            "Paste a package list (one per line, comments start with #), or fetch it from a GitHub link. Only packages that exist and are safe on this TV get selected."
        )
    val importPaste get() = t("Tampal senarai di sini", "Paste the list here")
    val importUrl get() = t("Pautan senarai (https)", "List link (https)")
    val importFetch get() = t("Ambil dari pautan", "Fetch from link")
    val importApply get() = t("Pilih padanan", "Select matches")
    fun importMatched(matched: Int, total: Int) = t(
        "$matched daripada $total pakej dalam senarai dipadankan dan dipilih.",
        "$matched of $total packages in the list matched and were selected."
    )
    fun importFailed(msg: String) = t("Tidak dapat ambil senarai: $msg", "Could not fetch the list: $msg")

    // ---- Cleaning / progress ----
    val cleaningTitle get() = t("Membersihkan…", "Cleaning…")
    val restoringTitle get() = t("Memulihkan…", "Restoring…")
    val reapplyingTitle get() = t("Mematikan semula…", "Disabling again…")
    fun progress(i: Int, n: Int) = "$i / $n"
    val stop get() = t("Berhenti", "Stop")

    // ---- Done ----
    val doneCleanTitle get() = t("Siap dibersihkan!", "All cleaned up!")
    val doneRestoreTitle get() = t("TV dipulihkan", "TV restored")
    val doneReapplyTitle get() = t("Dibersihkan semula", "Cleaned again")
    fun doneSummary(ok: Int, fail: Int) = t("$ok berjaya, $fail gagal", "$ok succeeded, $fail failed")
    val doneHint
        get() = t(
            "Jika ada yang tidak kena, buka Pulih. Anda juga boleh hidupkan semula terus di TV: Tetapan → Aplikasi.",
            "If something feels off, open Restore. You can also re-enable apps right on the TV: Settings → Apps."
        )
    val rebootTip get() = t("Mulakan semula TV untuk hasil terbaik.", "Restart your TV for the best result.")
    val failedList get() = t("Yang gagal", "Failed items")

    // ---- History ----
    val historyTitle get() = t("Pulih & sejarah", "Restore & history")
    val historyEmpty
        get() = t(
            "Belum ada rekod. Rekod disimpan setiap kali anda membersihkan TV.",
            "No records yet. One is saved every time you clean a TV."
        )
    fun recordLine(n: Int) = t("$n pakej dimatikan", "$n packages disabled")
    val restoreThis get() = t("Pulihkan", "Restore")
    val reapply get() = t("Matikan semula", "Disable again")
    val reapplyHint
        get() = t(
            "“Matikan semula” berguna selepas kemas kini firmware, bila pakej hidup balik.",
            "“Disable again” helps after a firmware update brings packages back."
        )
}
