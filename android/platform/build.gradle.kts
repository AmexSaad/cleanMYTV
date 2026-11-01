import java.security.MessageDigest

plugins { id("cleanyourtv.android.lib") }

// Implementations of the core seams (W2, W5, W8 per PRODUCT-SPEC 6).
dependencies {
    implementation(project(":core:model"))
    implementation(project(":adb:wire"))
    implementation(project(":adb:pairing"))
    implementation(project(":core:trust"))
    implementation(project(":core:registry"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(project(":testing:fake-adbd"))
    testImplementation(libs.kotlinx.coroutines.test)
}

// W5: the bundled launcher APK (assets/bundled) must match its pin before anything is packaged;
// RFC-CURTAIN-1: so must the bundled TV curtain.
tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(":core:trust:verifyLauncherPin", ":core:trust:verifyCurtainPin") }

val prayerApk = layout.projectDirectory.file("src/main/assets/bundled/cleanmytv-prayer.apk").asFile
val prayerPin = layout.projectDirectory.file("src/main/assets/bundled/cleanmytv-prayer.json").asFile
val verifyPrayerBundle = tasks.register("verifyPrayerBundle") {
    inputs.files(prayerApk, prayerPin)
    doLast {
        val digest = MessageDigest.getInstance("SHA-256").digest(prayerApk.readBytes()).joinToString("") { "%02x".format(it.toInt() and 255) }
        val pin = Regex("\"sha256\"\\s*:\\s*\"([0-9a-f]{64})\"").find(prayerPin.readText())?.groupValues?.get(1)
        check(digest == pin) { "Bundled prayer APK does not match its packaged pin" }
    }
}
tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(verifyPrayerBundle) }
