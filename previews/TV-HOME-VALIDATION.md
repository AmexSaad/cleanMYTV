# Pengesahan Menu TV — beta4

## Permintaan dan bukti

| Permintaan | Hasil ujian |
|---|---|
| Butang + terus dalam dock | Tile + Tambah app kelihatan di sebelah app dock. Klik membuka pemilih app sebenar. |
| App Android biasa tidak hilang | App QA yang hanya mempunyai LAUNCHER, tanpa LEANBACK_LAUNCHER, ditemui melalui carian Loklok, dipin dan dibuka dengan D-pad/OK. |
| Pilihan dock kekal | App QA masih pada dock selepas membuka app, menekan Home dan memasang semula kemas kini modul dengan signer yang sama. |
| Widget solat pada Home | Lima waktu dan seterusnya dipaparkan terus pada Home daripada cache zon SGR01 hari ujian. Tiada pembukaan app diperlukan untuk melihatnya. |
| Logo Prayer Time | Ikon emas/biru kelihatan pada tile Prayer Time dan widget dalam screenshot emulator. Activity turut menyatakan icon/banner secara eksplisit. |
| Pengaktifan Home | Butang membuka tetapan Home. Entry Prayer Time dipilih dan dialog Change disahkan. Resolve HOME kemudian melaporkan tv.cleanmytv.prayer/.HomeActivity. |
| Popup melindungi tontonan | Semasa percubaan azan, Home dan Back ditekan, kemudian app QA dibuka. Activity QA berada di belakang tetapi UI yang kelihatan masih poster Prayer Time. |
| Popup tamat sendiri | Selepas audio biasa habis, tiada service foreground azan dan UI app QA kelihatan semula. |
| Penggera kekal | Selepas naik taraf modul dan membuka Home, AlarmManager masih mempunyai CALL untuk waktu seterusnya dan REFRESH harian. |
| Laluan konfigurasi selamat | Unit test menyemak folder modul berasingan, penolakan target sewenang-wenang, signer Native Home terpinkan, tiada pertukaran default Home automatik, serta penolakan pembuangan Home aktif. |

## Screenshot

- [Menu Home, widget, ikon dan +](tv-home-beta4.png)
- [Pemilih app Android biasa](tv-app-picker-beta4.png)
- [Poster di atas app QA](tv-popup-over-app-beta4.png)
- [Dashboard telefon](prayer-dashboard.png)

## Skop ujian

Emulator Android 37.1 sendiri, paparan 1280 × 720. Modul kod versi 2 dinaik taraf ke kod 3 dengan sijil yang sama; zon SGR01 serta pilihan azan/paparan penuh kekal. Tetapan Home dipilih melalui UI Android, bukan dengan membuang atau mematikan launcher lama. Navigasi D-pad dari tile pertama ke app QA dan OK untuk membukanya telah diperiksa.

**Loklok QA (ujian sahaja)** ialah fixture dalam `tv/prayer/src/androidTest`, bukan APK Loklok sebenar. Ia sengaja tiada LEANBACK_LAUNCHER untuk menguji masalah kelas app Android biasa. Tiada TV fizikal pengguna bersambung dalam sesi ini. Bukti ini tidak menjanjikan kiosk mutlak, semua butang firmware, atau semua varian APK Loklok di semua box.

Ujian native Home/pemilih app, jadual/poster, planner, probe, engine dan flows dijalankan. Keputusan binaan release dan SHA APK ditambah selepas pengesahan GitHub selesai.
