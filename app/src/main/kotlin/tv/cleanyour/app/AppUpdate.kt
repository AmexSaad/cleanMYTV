package tv.cleanyour.app

/** A validated APK asset from this app's own published GitHub release. */
data class AppRelease(val version: String, val notes: String, val apkUrl: String)

sealed interface AppUpdateStatus {
    data object Checking : AppUpdateStatus
    data object Latest : AppUpdateStatus
    data object Failed : AppUpdateStatus
    data object TvBusy : AppUpdateStatus
    data class Available(val release: AppRelease) : AppUpdateStatus
}
