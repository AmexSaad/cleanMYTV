# Prayer Time — pengesahan poster dinamik

Kod binaan: `07f997179e24b2b45953de947764e18da8fa8d2e`.

## Ujian tempatan

- `PrayerPosterTest`: kelima-lima waktu dilukis daripada event berlainan, menggunakan waktu Malaysia dan lokasi pilihan; lokasi Kuala Lumpur tidak dikunci dalam gambar. Mod percubaan dilabel dan tidak mendakwa waktu solat telah masuk.
- `PrayerClockTest`: pemformatan jam poster untuk Subuh, Zohor, Asar, Maghrib dan Isyak; sumber audio Subuh berasingan; jadual mengikut waktu Malaysia walaupun zon masa peranti berlainan.
- `PrayerDashboardTest`: kad Prayer Time muncul pada dashboard dan membuka seksyen Prayer Time.
- Ujian sentuhan terlindung: kemas kini Prayer Time tidak bermula melalui sentuhan yang dilindungi app lain; sentuhan biasa mengeluarkan tindakan yang betul.
- Ujian aliran: membuka skrin tidak memasang modul secara automatik; pemasangan dan kemas kini modul lama memerlukan tindakan pengguna. Laluan navigasi menghormati binaan tanpa add-on dan Back.

## Emulator Android

- APK modul bertandatangan versi kod 2 dipasang dengan `adb install -r` di atas versi kod 1 menggunakan sijil yang sama. Zon Selangor SGR01, pilihan audio dan paparan penuh kekal.
- Popup Zohor menggunakan jadual tersimpan hari tersebut, 1:00 tengah hari, serta senarai daerah SGR01 sebenar daripada tetapan. Butang Back tidak menutup popup. Service/paparan dilepaskan sendiri selepas audio biasa tamat.
- Popup Subuh menggunakan 5:48 pagi daripada jadual tersimpan. Butang Tamatkan percubaan menghentikan service/paparan. [Screenshot Subuh](prayer-popup-subuh-emulator.png).
- [Screenshot popup biasa](prayer-popup-emulator.png) dan [screenshot ujian UI dashboard](prayer-dashboard.png).

Screenshot popup dilabel **PRATONTON**, bukan bukti penggera solat sebenar berbunyi tepat pada waktunya. Screenshot dashboard menggunakan data ujian. Emulator bukan pengesahan keserasian semua firmware TV fizikal.

## Binaan keluaran

[Binaan bertandatangan dan ujian GitHub](https://github.com/AmexSaad/cleanMYTV/actions/runs/41286958780). Lihat keputusan langsung di situ; nota ini tidak menggantikan status binaan.
