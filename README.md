# cleanMYTV

**BM** · [English below](#english)

Aplikasi Android (telefon) untuk membersihkan bloatware pada Android TV / Google TV melalui Wi-Fi. Tiada root, tiada kabel, tiada apa dipadam. Pakej hanya dimatikan (`pm disable-user`) dan semuanya boleh dipulihkan. Dwibahasa: Bahasa Melayu & English (butang **BM | EN** di penjuru atas).

Idea fungsi diambil daripada projek sumber terbuka [CleanYourTV](https://github.com/Gathin23/cleanyourtv) (MIT). Kod dan rekaan antara muka cleanMYTV ditulis sendiri.

### Cara guna
1. Di TV: Tetapan → Pilihan Peranti → Perihal → tekan OK pada **Build** 7 kali → Pilihan pembangun → hidupkan **USB debugging** (dan **ADB rangkaian** jika ada).
2. Telefon dan TV atas Wi-Fi yang sama. Buka cleanMYTV → **Cari TV di rangkaian** (atau taip IP) → **Sambung & analisis**.
3. Di TV, tekan **Allow USB debugging?** → Always allow.
4. Semak pelan pembersihan (Disyorkan / Semak sendiri / Lanjutan) → **Bersihkan**.
5. Jika ada yang tidak kena: **Pulih / Sejarah** → **Pulihkan**. Selepas kemas kini firmware: **Matikan semula**.

### Ciri
- Imbas rangkaian untuk cari TV (port ADB 5555), sambungan ADB terus dari telefon (pustaka [dadb](https://github.com/mobile-dev-inc/dadb), tiada binari `adb`).
- Analisis: model, versi Android, skrin utama semasa, senarai pakej sistem.
- Perlindungan: skrin utama (dan semua pakej yang menjawab HOME), pakej sistem penting, Chromecast built-in, dan aplikasi ini sendiri tidak akan disentuh.
- Rekod pemulihan ditulis **sebelum** setiap pakej dimatikan (write-ahead), jadi Wi-Fi terputus pun tidak menghilangkan senarai undo.
- **Import senarai**: tampal atau ambil senarai `debloat.txt` komuniti (satu pakej setiap baris, komen `#`) dari pautan GitHub; hanya pakej yang wujud dan selamat pada TV anda dipilih.

### Bina APK
**Cara paling mudah (tanpa Android Studio):** muat naik folder ini ke repo GitHub anda. Tab **Actions → Build APK** akan menghasilkan `cleanMYTV-apk` (fail `app-debug.apk`) yang boleh dipasang terus.

**Android Studio:** buka folder ini, tunggu Gradle sync, kemudian **Build → Build APK(s)**. Perlu JDK 17, compileSdk 35.

Pasang APK: benarkan "Install unknown apps" untuk pelayar/pengurus fail anda.

### Had yang jujur
- Hanya Android TV / Google TV. Samsung (Tizen), LG (webOS), Roku, Vizio tiada ADB, jadi tidak disokong.
- Fire TV: bahagian "matikan pakej" mungkin berfungsi, tetapi Fire OS tidak membenarkan skrin utama pihak ketiga.
- Senarai pakej terkurasi dalam app sengaja pendek dan konservatif. Untuk senarai khusus model TV, guna **Import senarai** dengan senarai komuniti.
- Pelancar (launcher) gaya Apple TV dalam projek asal **tidak** termasuk dalam versi ini; cleanMYTV fokus pada pembersihan + pemulihan.
- Guna pada TV milik sendiri, atas risiko sendiri. Tiada kaitan dengan mana-mana pengeluar TV.

---

## English

An Android (phone) app that debloats Android TV / Google TV over Wi-Fi. No root, no cable, nothing deleted. Packages are only disabled (`pm disable-user`) and everything can be restored. Bilingual: Bahasa Melayu & English (**BM | EN** switch at the top right).

The functional idea comes from the open-source [CleanYourTV](https://github.com/Gathin23/cleanyourtv) project (MIT). cleanMYTV's code and UI design are original.

### How to use
1. On the TV: Settings → Device Preferences → About → press OK on **Build** 7 times → Developer options → turn on **USB debugging** (and **Network ADB** if shown).
2. Phone and TV on the same Wi-Fi. Open cleanMYTV → **Find TVs on my network** (or type the IP) → **Connect & analyse**.
3. On the TV, accept **Allow USB debugging?** → Always allow.
4. Review the plan (Recommended / Review first / Advanced) → **Clean**.
5. If something feels off: **Restore / History** → **Restore**. After a firmware update: **Disable again**.

### Features
- Network scan for TVs (ADB port 5555) and a direct ADB connection from the phone ([dadb](https://github.com/mobile-dev-inc/dadb), no `adb` binary).
- Analysis: model, Android version, current home app, system package list.
- Safety: the home app (and every package that answers HOME), essential system packages, built-in Chromecast and this app are never touched.
- The restore record is written **before** each package is disabled (write-ahead), so a dropped Wi-Fi never loses the undo list.
- **Import a list**: paste or fetch a community `debloat.txt` (one package per line, `#` comments) from a GitHub link; only packages that exist and are safe on your TV get selected.

### Build the APK
**Easiest (no Android Studio):** push this folder to a GitHub repo. **Actions → Build APK** produces the `cleanMYTV-apk` artifact (`app-debug.apk`), which installs directly.

**Android Studio:** open this folder, let Gradle sync, then **Build → Build APK(s)**. Needs JDK 17, compileSdk 35.

### Honest limits
- Android TV / Google TV only. Samsung (Tizen), LG (webOS), Roku and Vizio have no ADB.
- Fire TV: the disable part may work, but Fire OS will not hand Home to a third-party launcher.
- The built-in curated list is deliberately short and conservative. For model-specific lists, use **Import a list** with a community list.
- The Apple-TV-style launcher from the original project is **not** included; cleanMYTV focuses on cleaning + restoring.
- Use it on a TV you own, at your own risk. Not affiliated with any TV maker.
