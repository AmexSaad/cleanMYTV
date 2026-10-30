# cleanMYTV 05 — Hitam + Limau

Implementasi calon untuk versi 1.2.1-beta1, berdasarkan main bbc7816.

- Tema hitam arang dengan aksi utama limau dan teks putih.
- Grafik TV vektor dengan jalur limau; tiada bitmap besar tambahan.
- Dashboard empat kad untuk skrin utama, modul, app dinyahaktif dan pemulihan.
- Maklumat app, status, akses tetapan, semakan app tertinggal dan nota pemulihan dikekalkan.
- Bahasa Melayu/English serta sokongan SociaBuzz kekal.
- Tiada perubahan pada enjin ADB, analisis, pelan, pembersihan, jurnal atau perlindungan TV.

Build:

```powershell
./gradlew.bat :app:assembleFullDebug :app:testFullDebugUnitTest :feature:manage:testDebugUnitTest :feature:connect:testDebugUnitTest
```

Binaan baharu perlu disahkan sebelum diterbitkan. Reka bentuk 05 ialah arah visual; tiada angka peningkatan prestasi atau ruang storan direka-reka. Android 9+ diperlukan. Pengesahan TV fizikal masih belum dibuat.
