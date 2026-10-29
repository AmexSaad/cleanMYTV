<div align="center">

# cleanMYTV
### TV lebih kemas. Pilihan di tangan anda.

Urus app Android TV dan TV box melalui telefon — dalam Bahasa Melayu dan English.

[**Bahasa Melayu**](#bahasa-melayu) · [**English**](#english)

[![Build APK](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml/badge.svg)](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml)
![Android phone 8+](https://img.shields.io/badge/Android%20phone-8%2B-0866FF?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Language](https://img.shields.io/badge/Bahasa-BM%20%7C%20EN-0866FF)

[**Muat turun APK**](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml) · [**Lapor masalah**](https://github.com/AmexSaad/cleanMYTV/issues/new) · [**Sokong projek**](https://sociabuzz.com/cleanmytv/tribe)

![cleanMYTV — TV lebih kemas](cover.jpg)

</div>

## Bahasa Melayu

### Tentang cleanMYTV

cleanMYTV ialah aplikasi **pada telefon Android** untuk mengurus app pada Android TV / Google TV melalui ADB di rangkaian Wi-Fi. Pilih app yang mahu dinyahaktifkan, pulihkan perubahan melalui Sejarah, atau hantar APK sendiri ke box.

**Tiada root untuk aliran yang disokong.** Fungsi pembersihan menggunakan nyahaktif app, bukan memadam pakej sistem. Sekatan firmware masih boleh menyebabkan arahan ditolak.

## Apa yang boleh dibuat?

| Fungsi | Kegunaan |
| :--- | :--- |
| **Sambung TV Anda** | Cari TV dalam rangkaian atau masukkan IP dan port sendiri. |
| **Urus App** | Semak cadangan dan pilih app lain secara manual, dengan perlindungan komponen penting. |
| **Tambah App** | Pilih satu fail APK pada telefon dan pasang pada box; had 200 MB. |
| **Waktu Solat** | Buka halaman rasmi Waktu Solat oleh RIQTech di Play Store TV; lengkapkan pemasangan dengan remote. |
| **Sejarah & Pulihkan** | Lihat rekod perubahan, aktifkan semula app atau lakukan nyahaktif semula. |
| **Status sambungan** | Papar model dan IP selepas sambungan berjaya; semak sambungan semasa dashboard dibuka. |
| **Lapor Masalah** | Buka draf GitHub Issue dengan maklumat asas app dan peranti untuk disemak sebelum dihantar. |
| **BM / EN** | Tukar bahasa terus dalam app. |
| **Custom MyLauncher** | 12 gaya jam/tarikh, 20 wallpaper, susunan app, tema dan penghantaran ke TV. |

### Custom MyLauncher · versi 1.1.0

Selepas urus app, buka **Custom MyLauncher** pada halaman utama atau skrin selesai.

1. Baca semula app TV yang masih aktif, pilih app dan susun melintang atau menegak.
2. Pilih **J01–J12** untuk jam/tarikh dan **W01–W20** untuk wallpaper. Seret jam atau laras saiz serta kedudukannya. Pilih format 12/24 jam dan gelapkan wallpaper.
3. Simpan sehingga 10 tema pada telefon. Tekan **Pasang & hantar reka bentuk ke TV** dan sahkan alamat IP.
4. APK telefon memasang MyLauncher dan membuka reka bentuk pada TV. Ikon/nama app sebenar dibaca pada TV; waktu mengikut jam dan zon waktu TV.
5. Untuk butang Home, buka **Pilih launcher lalai pada TV** dan pilih MyLauncher dengan remote. Sokongan bergantung pada firmware. **Kembali ke launcher asal** memilih launcher yang direkod sebelum penghantaran pertama; launcher asal kekal aktif.

Wallpaper asal dijana untuk projek dan dibundel sebagai satu atlas ringan. MyLauncher tidak menyokong widget Android pihak ketiga. Pengesahan penghantaran ADB kini menggunakan binaan debug; versi release memerlukan saluran pengesahan lain. Belum disahkan pada TV fizikal.

## Mula dalam 3 langkah

### 1 · Pasang pada telefon

**MyLauncher 1.1.0:** ambil `cleanMYTV-apk` daripada run `main` terkini yang berjaya di [GitHub Actions](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml). Ekstrak ZIP dan pasang `cleanMYTV.apk`.

Pautan Google Drive di bawah ialah **binaan terdahulu**, sebelum MyLauncher:

[**📥 Muat turun cleanMYTV di Google Drive**](https://drive.google.com/file/d/1oJF38W7EZH4YznjOjDgGsNYLy6kkbTu2/view?usp=sharing)

Buka pautan di atas dan tekan **Download / Muat turun**. Jika fail yang dimuat turun ialah ZIP, ekstrak dahulu. Kemudian buka **`cleanMYTV.apk`** pada telefon Android untuk memasang app.

Jika Android meminta kebenaran pemasangan, benarkan untuk pelayar atau pengurus fail yang digunakan. APK ini ialah binaan debug dan belum disediakan sebagai keluaran Play Store.

Pilihan lain: binaan terkini tersedia melalui [GitHub Actions](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml), di bahagian **Artifacts** pada run yang berjaya.

### 2 · Sambung box

- Pastikan telefon dan TV / box menggunakan Wi-Fi yang sama.
- Aktifkan pilihan pembangun dan ADB rangkaian pada TV, jika firmware menyokongnya.
- Tekan **Sambung TV Anda**, cari TV atau isi IP serta port.
- Terima permintaan kebenaran debugging pada TV.

### 3 · Pilih tindakan

Buka **Urus App** untuk analisis dan nyahaktif app, **Tambah App** untuk pemasangan APK, atau **Sejarah & Pulihkan** untuk membatalkan perubahan yang direkodkan.

> Mulakan dengan sedikit app yang anda kenal. Keserasian bergantung pada model, versi Android dan firmware; binaan berjaya bukan pengesahan bahawa semua box telah diuji.

## Keserasian & had

| Perkara | Status |
| :--- | :--- |
| Telefon Android | Android 8.0+ |
| Android TV / Google TV / box Android | Memerlukan sambungan ADB yang boleh dicapai dan dibenarkan |
| Box Unifi | Bergantung pada model dan sekatan firmware; belum disahkan pada semua model |
| Samsung Tizen / LG webOS / Roku | Tidak disokong oleh aliran ADB ini |
| APK sendiri | Satu fail `.apk`; XAPK / APKS / split bundle tidak disokong |
| Widget waktu solat di home screen | Bergantung pada app dan launcher TV; bukan widget terbina dalam cleanMYTV |
| MyLauncher | Termasuk dalam APK; dipasang pada TV melalui editor telefon. Launcher lalai bergantung pada firmware. |

App sistem tertentu dilindungi daripada pemilihan. Perlindungan berasaskan senarai dan pengesanan launcher ini mengurangkan risiko, tetapi tidak menjamin semua app pengeluar telah dikenal pasti.

## Lapor masalah

Tekan **Lapor Masalah** dalam app atau [buka GitHub Issue](https://github.com/AmexSaad/cleanMYTV/issues/new).

Sertakan langkah masalah, hasil yang dijangka, model box dan screenshot jika berkaitan. **Issues adalah awam** — semak dan padam maklumat peribadi sebelum hantar. App membuka borang untuk semakan pengguna; laporan tidak dihantar secara automatik.

## Untuk pembangun

- Kotlin + Jetpack Compose
- Sambungan ADB terus dari telefon melalui [dadb](https://github.com/mobile-dev-inc/dadb)
- JDK 17 · Gradle 8.10.2 · compileSdk 35

Dengan Android SDK dan Gradle yang sesuai tersedia:

```sh
gradle assembleDebug
```

APK dihasilkan di `app/build/outputs/apk/debug/app-debug.apk`. Lihat [workflow binaan](.github/workflows/build.yml) untuk persekitaran CI.

Cadangan ciri dan pembaikan dialu-alukan melalui Issues dan pull request. Terangkan perubahan dan cara mengujinya; jangan masukkan kunci, kata laluan atau log yang mengandungi data peribadi.

## Kredit & sokongan

Idea fungsi mendapat inspirasi daripada [CleanYourTV](https://github.com/Gathin23/cleanyourtv). cleanMYTV ialah projek berasingan; tiada gabungan rasmi dinyatakan. Pustaka ADB: [dadb](https://github.com/mobile-dev-inc/dadb).

Jika cleanMYTV membantu anda, [**sokong pembangunannya di SociaBuzz**](https://sociabuzz.com/cleanmytv/tribe). Sumbangan adalah sukarela.

---

## English

[↑ Bahasa Melayu](#bahasa-melayu)

### Your TV, your choice

cleanMYTV is an **Android phone app** for managing apps on Android TV / Google TV over Wi-Fi ADB. Disable selected apps, restore recorded changes, or send an APK from your phone to your box. The interface supports **Malay and English**.

### Features

| Feature | What it does |
| :--- | :--- |
| Connect your TV | Scan your local network or enter the TV IP and ADB port. |
| Manage apps | Review recommendations and manually select other apps, with core-component protection. |
| Add apps | Send and install one APK from your phone, up to 200 MB. |
| Waktu Solat | Open the official RIQTech listing in the TV Play Store, then install with your remote. |
| History & restore | Review recorded changes, re-enable apps or reapply disabling. |
| Connection status | Show model and IP after a successful connection; check it while the dashboard is open. |
| Problem reports | Select locally recorded errors and review a prefilled GitHub Issue before submitting. |

### Custom MyLauncher · version 1.1.0

After managing apps, open **Custom MyLauncher** from Home or the completion screen.

1. Refresh the TV's enabled launchable apps, choose apps and arrange them horizontally or vertically.
2. Choose a **J01–J12** clock/date style and **W01–W20** wallpaper. Drag the clock or adjust its size and position; choose 12/24-hour time and wallpaper dimming.
3. Save up to 10 themes on the phone. Tap **Install & send design to TV** and confirm the target IP.
4. The phone APK installs MyLauncher on the TV and opens the design. The TV reads actual app names/icons and uses its own clock and time zone.
5. For the Home button, choose MyLauncher in **Choose default launcher on TV** using the remote. Firmware may restrict this. **Restore original launcher** selects the launcher recorded before the first send; the original stays enabled.

Original generated wallpapers are bundled as one lightweight atlas. MyLauncher does not host third-party Android widgets. ADB design acknowledgement currently uses the debug build; release builds need another acknowledgement channel. Physical TV behaviour has not yet been verified.

### 1 · Install on your phone

**MyLauncher 1.1.0:** get `cleanMYTV-apk` from the latest successful `main` run in [GitHub Actions](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml). Extract the ZIP and install `cleanMYTV.apk`.

The Google Drive link below is a **previous build**, before MyLauncher:

[**📥 Download cleanMYTV from Google Drive**](https://drive.google.com/file/d/1oJF38W7EZH4YznjOjDgGsNYLy6kkbTu2/view?usp=sharing)

Open the link and select **Download**. Extract the file first if it is a ZIP, then open **`cleanMYTV.apk`** on your Android phone. Allow installation for your browser or file manager if Android asks. This is a debug build, not a Play Store release.

Alternatively, download **cleanMYTV-apk** from **Artifacts** on a successful [GitHub Actions run](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml). GitHub sign-in may be required.

### 2 · Connect your TV or box

1. Keep your phone and TV on the same Wi-Fi network.
2. Enable Developer options and network ADB on the TV, if supported by its firmware.
3. Tap **Connect your TV**, scan for the device or enter its IP and port.
4. Accept the debugging authorization prompt on the TV.

### 3 · Choose an action

Open **Manage apps** to analyse and disable selected apps, **Add apps** to install your APK, or **History & restore** to undo recorded changes.

### Compatibility and limits

- Requires an Android 8.0+ phone and an Android TV / Google TV / Android box with reachable, authorized ADB.
- Unifi box support depends on the model and firmware; not all models have been tested.
- Samsung Tizen, LG webOS and Roku are not supported by this ADB workflow.
- Only single `.apk` files are supported; XAPK, APKS and split bundles are not supported.
- Home-screen prayer widgets depend on the TV launcher and app; cleanMYTV includes MyLauncher but does not include a built-in prayer widget.
- Firmware may refuse app changes. Protection rules reduce risk but cannot identify every critical manufacturer package. Start with a few apps you recognise.
- A successful build does not mean every device has been tested.

### Report a problem

Tap **Report a problem** in the app or [open a GitHub Issue](https://github.com/AmexSaad/cleanMYTV/issues/new). Include steps to reproduce, expected behaviour and your box model. You can select from up to 20 locally stored error records, including captured crash types where available.

**Issues are public.** Review the report and remove personal information before submitting or attaching screenshots. Reports are not submitted automatically.

### Build and contribute

Kotlin + Jetpack Compose, using [dadb](https://github.com/mobile-dev-inc/dadb) for direct ADB connections. Build with JDK 17, Gradle 8.10.2 and Android SDK 35:

```sh
gradle assembleDebug
```

The build output is `app/build/outputs/apk/debug/app-debug.apk`; GitHub Actions packages a copy named `cleanMYTV.apk`. See the [build workflow](.github/workflows/build.yml).

Feature suggestions and pull requests are welcome. Explain your change and how to test it. Do not include credentials or private logs.

### Credits and support

The functional idea was inspired by [CleanYourTV](https://github.com/Gathin23/cleanyourtv). cleanMYTV is a separate project; no official affiliation is claimed. ADB library: [dadb](https://github.com/mobile-dev-inc/dadb).

[**Support cleanMYTV on SociaBuzz**](https://sociabuzz.com/cleanmytv/tribe). Contributions are voluntary.
