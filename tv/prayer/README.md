# Optional cleanMYTV prayer times and azan

Built-in companion for Android TV / boxes, API 26+. In cleanMYTV's **Home** screen choose **+ Tambah waktu solat & azan**. The existing journalled AddModule plan installs the bundled APK, enables module cards and adds its shortcut; the TV asks whether to show the card. This does not enable sound or alarms. The Play flavour does not offer this sideloaded module.

Open **Waktu Solat** on the box, choose **Tambah**, then **Malaysia → state → district/zone**. The bundled catalogue covers Peninsular Malaysia, Sabah, Sarawak and the Federal Territories. Download the zone's current-year JAKIM timetable via Solat.my. Turn **Azan automatik** on only if wanted. Turn **Paparan penuh** on separately if wanted, grant Android overlay access, and tap the control again. Exact-alarm access is needed on Android 12+. There are separate **Cuba azan Subuh** and **Cuba azan biasa** controls. The Subuh recording is different; the other four prayers use the regular recording. Sunrise/syuruk, imsak and dhuha do not sound an azan.

The full-screen overlay remains until playback completion, decoder error, audio-focus loss, an explicit emergency stop or a ten-minute failsafe. During a real call Back is consumed by that overlay. This is not device-owner/kiosk lockdown: firmware can hide overlays or reserve Home. Apps that honour Android audio focus pause/quiet their playback; universal video suspension is not promised. Sound-only mode does not cover other apps. The notification provides **Hentikan azan** and the app also has a stop control. Volume/mute settings are respected and not raised automatically.

The module is opt-in at two levels: installation is optional; within it, adding the prayer feature and enabling sound are separate choices, both off by default. **Buang fungsi** cancels alarms and playback. The phone's Remove control uninstalls only the companion that cleanMYTV installed, through its normal recorded RemoveModule operation. No stock Home or system package is changed.

## Offline and lifecycle

Audio is bundled, with [attribution](ATTRIBUTION.md). Zone-matched dated schedules are cached atomically. Malaysia's timezone is fixed to Asia/Kuala_Lumpur even if the box timezone differs. Stale/malformed/foreign-zone data never generates guessed alarms. Alarms are recreated after reboot, time changes, app upgrades and exact-alarm permission grant. Refresh happens around 00:05 Malaysian time and when the page opens on a new day; with no internet, remaining valid cached dates still work. At year rollover a fresh year's timetable is required. A powered-off box cannot play; firmware battery/auto-start restrictions can delay or block events and require physical testing.

## Permissions and network

* INTERNET: HTTPS requests only to solat.my for the selected zone/year; no GPS or account.
* RECEIVE_BOOT_COMPLETED: restore opted-in schedules.
* SCHEDULE_EXACT_ALARM: user-granted on supported Android versions, for the five daily calls.
* SYSTEM_ALERT_WINDOW: user-granted, only for the selected full-screen call display.
* FOREGROUND_SERVICE / MEDIA_PLAYBACK: play audio for the duration of one azan; no always-running service.
* POST_NOTIFICATIONS: playback status and emergency stop.

No ads, analytics, accessibility service, root or device-owner provisioning. Sources are plain Java with no runtime dependencies.

## Build

`./gradlew :tv:prayer:assembleRelease :tv:prayer:testDebugUnitTest` makes an unsigned release APK. Sign it with the same dedicated companion key for future updates, then update `android/platform/src/main/assets/bundled/cleanmytv-prayer.apk` and its SHA-256/signer pin in `cleanmytv-prayer.json` before building the phone app. Never replace a deployed companion's key silently. The phone inspects the packaged APK, compares its digest and signer with the packaged pin and refuses mismatches.
