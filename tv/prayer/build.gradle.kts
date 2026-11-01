plugins { id("cleanyourtv.tv.app") }

android {
    namespace = "tv.cleanmytv.prayer"
    defaultConfig {
        applicationId = "tv.cleanmytv.prayer"
        versionCode = 1
        versionName = "1.0-beta1"
    }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}
dependencies { testImplementation(libs.robolectric) }
tasks.withType<Test>().configureEach {
    maxHeapSize = "1280m"
    systemProperty("robolectric.graphicsMode", "NATIVE")
}
