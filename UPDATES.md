# App updates / Kemas kini app

The full build provides an Update / Kemas kini button below the brand header, without connecting a TV first. Settings uses the same release check. It checks only after a user asks, reads the repository's published release list, ignores drafts and incomplete or foreign APK assets, compares numeric versions, and includes beta releases for beta installations. Stable installations remain on the stable channel. Release notes and the exact repository APK link appear before download. The browser downloads the APK and Android asks the user to confirm installation. No silent installer or new phone permissions are added. A download is blocked while a known TV has an unfinished engine run.

Butang Kemas kini tersedia sebelum TV disambung. Tekan untuk semak versi, lihat perubahan dan muat turun APK. Buka fail yang dimuat turun dan sahkan pemasangan Android. Operasi TV yang belum selesai mesti diselesaikan atau dipulihkan dahulu. Enjin sambungan, analisis, pembersihan dan pemulihan tidak diubah.

## Permanent signing

The approved repository secret `CLEANMYTV_SIGNING` contains JSON fields `keystore` (base64 PKCS#12), `password`, and `alias` (`cleanmytv`). Never commit this secret or its decoded key. Only the public certificate fingerprint is committed in `signing-certificate.sha256`. The signing script verifies that identity before building; the publisher refuses debug or wrongly signed APKs. Configuration caching is disabled for builds that read signing credentials, Gradle caches are read-only, and temporary key material is removed from the disposable runner.

The signed build tests a same-key `adb install -r` upgrade from a test-only predecessor and checks that the chosen Malay language preference remains. This verifies an in-place Android upgrade without uninstalling or clearing data. The normal published APK has version code 6; the test-only predecessor has code 5 and is never published as a release asset.

Earlier 1.2.x debug APKs were signed with temporary keys that cannot be reused for this new release. Finish any needed TV recovery with the old app before uninstalling it for the first permanent-key installation. Later releases using this permanent key can update that installation in place. Keep an independent secure backup of the release keystore and its password; replacing a lost key breaks this update identity.

Beta debug 1.2.x memerlukan pemasangan semula sekali selepas pemulihan TV yang diperlukan selesai. Keluaran dengan kunci tetap ini menyokong kemas kini seterusnya tanpa membuang app lama, selagi kunci yang sama digunakan.
