package my.cleanmytv.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import my.cleanmytv.app.Lang

@Composable
fun HelpGuide(lang: Lang) {
    var open by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val ms = lang == Lang.MS
    fun t(a: String, b: String) = if(ms) a else b
    TextButton(onClick = { open = true }, contentPadding = PaddingValues(horizontal = 6.dp)) {
        Text(t("Bantuan", "Help"), maxLines = 1, softWrap = false)
    }
    if(open) AlertDialog(
        onDismissRequest = { open = false },
        title = { Text(t("Panduan cleanMYTV", "cleanMYTV guide")) },
        text = {
            val sections = if(ms) listOf(
                "1 · Sebelum mula" to "Pasang cleanMYTV pada telefon Android 8 atau lebih baharu. Telefon dan TV / box perlu pada rangkaian tempatan yang sama. Nama menu TV mungkin berbeza ikut model.",
                "2 · Cari alamat IP TV" to "Pada TV: Tetapan → Rangkaian & Internet → pilih Wi-Fi yang sedang disambungkan, atau Ethernet jika guna kabel → cari Alamat IP. Jika tiada, semak Tetapan → Perihal → Status. Contoh IP: 192.168.1.105. Masukkan IP TV, bukan IP telefon.",
                "3 · Hidupkan ADB pada TV" to "Tetapan → Pilihan Peranti / Sistem → Perihal → tekan Android TV OS Build / Build sebanyak 7 kali. Kembali → Pilihan pembangun → hidupkan USB debugging dan Network / ADB debugging jika tersedia. Gunakan hanya pada TV milik anda.",
                "4 · Port yang betul" to "Sambungan ADB TCP biasa selalunya guna 5555. Jika TV menyatakan port ADB lain, gunakan port itu. Isi IP dan port pada ruangan berasingan. Versi cleanMYTV ini belum menyokong pairing Wireless debugging melalui kod; port Pair device tidak boleh digunakan terus. Jika TV hanya menyediakan pairing, rujuk panduan pengeluar.",
                "5 · Sambung secara automatik atau manual" to "Tekan Sambung TV Anda → Cari TV di rangkaian. Jika TV ditemui, pilih IP dalam senarai. Jika tidak ditemui, isi IP dan port sendiri. Tekan Sambung & analisis. Pada TV, benarkan permintaan debugging. Jangan anggap TV tersambung sehingga app mengesahkannya.",
                "6 · Jika TV tidak ditemui" to "Semak TV hidup, IP terkini, port dan debugging aktif. Elakkan rangkaian tetamu atau rangkaian yang mengasingkan peranti. Sesetengah firmware menyekat ADB. Jangan dedahkan port ADB ke internet. Selepas selesai, matikan debugging melalui tetapan TV jika tidak diperlukan.",
                "7 · Urus dan pulihkan app" to "Semak hasil analisis. Pilih sedikit app yang anda kenal, kemudian jalankan nyahaktif. Ini tidak memadam app sistem dan tidak menyekat semua iklan. Jika ada masalah, buka Sejarah & Pulihkan. Uji remote, skrin utama dan app penting selepas perubahan.",
                "8 · Tambah app dan Waktu Solat" to "Buka Tambah App. Untuk APK sendiri, pilih satu fail .apk sehingga 200 MB dan semak pakej sebelum pasang. XAPK / APKS tidak disokong. Untuk Waktu Solat, buka Play Store pada TV dan lengkapkan pemasangan dengan remote.",
                "9 · Laporan dan maklum balas" to "Lapor Masalah menyenaraikan ralat terkini untuk dipilih. Selepas nyahaktif selesai, Kongsi Keputusan TV membolehkan anda menyemak model dan firmware serta keputusan sebenar. Kedua-duanya membuka draf GitHub Issue; anda perlu log masuk dan menghantarnya sendiri. Laporan adalah awam: jangan masukkan maklumat peribadi."
            ) else listOf(
                "1 · Before you start" to "Install cleanMYTV on an Android 8+ phone. Your phone and TV / box must be on the same local network. Menu names vary by device.",
                "2 · Find the TV IP address" to "On the TV: Settings → Network & Internet → select the connected Wi-Fi, or Ethernet for a cable connection → IP address. Alternatively try Settings → About → Status. Example: 192.168.1.105. Use the TV IP, not your phone IP.",
                "3 · Enable ADB on the TV" to "Settings → Device Preferences / System → About → press Android TV OS Build / Build 7 times. Go back → Developer options → enable USB debugging and Network / ADB debugging where available. Use only on a TV you own.",
                "4 · Choose the correct port" to "Traditional TCP ADB often uses 5555. Use a different port only if your TV specifies it for ADB. Enter the IP and port separately. This version of cleanMYTV does not support Wireless debugging code pairing; a Pair device port cannot be used directly. If your TV only offers pairing, consult its manufacturer guide.",
                "5 · Connect automatically or manually" to "Tap Connect your TV → Find TVs on the network. Select a discovered IP. If nothing appears, enter the TV IP and port yourself. Tap Connect & analyse and accept the debugging authorization on the TV. Wait for the app to confirm the connection.",
                "6 · If the TV cannot be found" to "Check that the TV is on, its IP is current and ADB is enabled on the correct port. Avoid guest networks or device isolation. Some firmware blocks ADB. Do not expose ADB to the internet. Turn debugging off in TV settings when finished if it is no longer needed.",
                "7 · Manage and restore apps" to "Review the analysis and start with a few apps you recognise. Disabling does not delete system apps or block every advertisement. Use History & restore if needed. Test your remote, home screen and important apps after changes.",
                "8 · Add apps and Waktu Solat" to "Open Add apps. Choose one .apk file up to 200 MB and review the package before installing. XAPK / APKS bundles are unsupported. For Waktu Solat, open the TV Play Store and complete installation with your remote.",
                "9 · Reports and feedback" to "Report a problem lets you select recent errors. After cleanup, Share TV results lets you review the model, firmware and actual outcome. Both open a GitHub Issue draft; sign in and submit it yourself. Reports are public: remove private information."
            )
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                sections.forEach { (title, body) -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(body, style = MaterialTheme.typography.bodyMedium)
                } }
            }
        },
        confirmButton = { TextButton(onClick = { open = false }) { Text(t("Faham", "Got it")) } }
    )
}
