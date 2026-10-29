<div align="center">

# cleanMYTV
### TV lebih kemas. Pilihan di tangan anda.

Urus app Android TV dan TV box melalui telefon — dalam Bahasa Melayu dan English.

[![Build APK](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml/badge.svg)](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml)
![Android phone 8+](https://img.shields.io/badge/Android%20phone-8%2B-0866FF?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Language](https://img.shields.io/badge/Bahasa-BM%20%7C%20EN-0866FF)

[**Muat turun APK**](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml) · [**Lapor masalah**](https://github.com/AmexSaad/cleanMYTV/issues/new) · [**Sokong projek**](https://sociabuzz.com/cleanmytv/tribe)

![cleanMYTV — TV lebih kemas](cover.jpg)

</div>

## Tentang cleanMYTV

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

## Mula dalam 3 langkah

### 1 · Pasang pada telefon

[**📥 Muat turun cleanMYTV di Google Drive**](https://drive.google.com/file/d/1_OXyHOOSbgyuJNnI0jDn1y2V-h0bGQW8/view?usp=sharing)

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
| Launcher pengganti | Tidak termasuk dalam app ini |

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

### English

**cleanMYTV is an Android phone app for managing apps on Android TV and Google TV over Wi-Fi ADB.** It offers app disabling with restore records, manual selection, single-APK installation, a Waktu Solat Play Store shortcut, and user-reviewed GitHub issue reports. The interface supports Malay and English.

Download an artifact from the latest successful [Build APK run](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml), extract it and install the debug APK on your phone. Device support depends on ADB availability and firmware restrictions. A successful build does not mean every TV model has been tested.