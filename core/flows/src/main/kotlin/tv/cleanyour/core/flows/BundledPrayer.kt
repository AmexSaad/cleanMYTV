package tv.cleanyour.core.flows

import tv.cleanyour.core.model.*

// This companion ships inside the phone APK. No remote index entry or unverified download is
// accepted here; AppGraph inspects the bundled asset and checks its pinned digest and signer.
data class BundledPrayer(val module: Module, val version: ModuleVersion, val blob: BlobRef) {
    companion object {
        val PKG = PackageName("tv.cleanmytv.prayer")
        const val VERSION_CODE = 3L
        fun of(info: ApkInfo, blob: BlobRef): BundledPrayer {
            require(info.pkg == PKG && info.sha256 == blob.sha256 && info.size == blob.size)
            val version = ModuleVersion(info.versionCode, info.versionName, "2026-11-01",
                ArtifactRef("bundled:cleanmytv-prayer.apk", blob.size, blob.sha256, info.signer),
                listOf(PermissionStatement("android.permission.INTERNET", "Jadual solat Malaysia"),
                    PermissionStatement("android.permission.SYSTEM_ALERT_WINDOW", "Paparan penuh semasa azan, jika dipilih"),
                    PermissionStatement("android.permission.SCHEDULE_EXACT_ALARM", "Azan mengikut waktu yang dipilih")),
                "Jadual e-Solat JAKIM melalui solat.my; audio disertakan tanpa muat turun", 26, emptyList(), emptyList())
            val module = Module("cleanmytv-prayer", "Prayer Time", "Menu TV + tambah app, widget solat Malaysia dan azan",
                "Fungsi pilihan; azan dimatikan sehingga pengguna menghidupkannya pada TV.", PKG, "app", Tier.COMMUNITY,
                "cleanMYTV", "https://github.com/AmexSaad/cleanMYTV", "MIT; audio CC BY 3.0", "#D6FF38", null, listOf(version))
            return BundledPrayer(module, version, blob)
        }
    }
}
