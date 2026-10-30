# cleanMYTV · 1.2.0 beta

[Bahasa Melayu](#bahasa-melayu) · [English](#english) · [Binaan APK](https://github.com/AmexSaad/cleanMYTV/actions/workflows/build.yml) · [Sokong projek](https://sociabuzz.com/cleanmytv/tribe)

## Bahasa Melayu

Versi ini menggunakan sumber **Clean Your TV v0.4.6-beta2** sebagai asas fungsi, dengan reka bentuk cleanMYTV putih dan biru serta pilihan Bahasa Melayu / English.

1. Pasang `cleanMYTV.apk` pada **telefon Android 9 atau lebih baharu**. Telefon dan Android TV mesti berada dalam rangkaian yang sama.
2. Sambung TV melalui carian automatik atau alamat TV. Ikut panduan debugging dan benarkan telefon pada TV.
3. Semak hasil analisis dan pelan perubahan sebelum meneruskan. Pembersihan menyahaktif app; pakej sistem tidak dipadam.
4. Pilihan skrin utama, susunan app, gaya, pemulihan dan laporan menggunakan aliran rujukan. Custom MyLauncher versi cleanMYTV lama telah digantikan dengan aliran skrin utama rujukan.

Ambil `cleanMYTV-apk` daripada bahagian **Artifacts** pada run berjaya. Ekstrak ZIP dan pasang `cleanMYTV.apk`. Binaan ini ialah beta debug, bukan keluaran Play Store. Pautan Drive versi lama bukan binaan versi ini.

Rekod cleanMYTV 1.1 disimpan pada telefon tetapi tidak diimport ke jurnal baharu. Eksport rekod/pemulihan daripada versi lama dahulu jika diperlukan. Versi ini menaikkan keperluan telefon daripada Android 8 kepada Android 9 mengikut sumber rujukan. Keserasian dan sekatan firmware bergantung pada TV. Ujian automatik tidak menggantikan pengesahan pada TV fizikal.

## English

This version uses **Clean Your TV v0.4.6-beta2** as its functional foundation, with cleanMYTV's white/blue interface and Malay/English selection. It retains the reference connection, discovery, analysis, plan review, cleaning, home-screen setup, layout, restore, backup, add-on and report flows.

Install `cleanMYTV.apk` from a successful build's **cleanMYTV-apk** artifact on an **Android 9+ phone**. Keep the phone and Android TV on the same network, follow the debugging instructions, authorize the phone on the TV, and review the plan before applying changes. System packages are disabled rather than uninstalled. The reference's home confirmation and recovery guards remain in place.

This is a beta debug build. The previous custom MyLauncher is replaced by the reference home-screen flow. Legacy cleanMYTV records remain on disk but are not imported into the new journal; export recovery information from the old app before upgrading if needed. Previous Google Drive builds are not this version. Physical TV and firmware compatibility still require device testing.

## Build

JDK 21, Android SDK 37, the checked-in Gradle wrapper, and the pinned Git submodule are required:

```sh
git submodule update --init --recursive
./gradlew :app:assembleFullDebug
```

## Source and credits

Derived from [udhaykumarbala/cleanyourtv-app](https://github.com/udhaykumarbala/cleanyourtv-app), tag `v0.4.6-beta2`, commit `0413da3068c563d8325f14f782209927f9783e1d`, under its MIT licence. Original copyright, licence, bundled-asset attribution and third-party notices are retained. The reference TV launcher is pinned through the original submodule. See [upstream documentation](README.upstream.md) for architecture and the full original documentation. cleanMYTV is a separate derivative project; no official affiliation is claimed.
