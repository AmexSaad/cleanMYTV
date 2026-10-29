#!/usr/bin/env bash
set -euo pipefail
gradle assembleDebugAndroidTest --no-daemon --console=plain
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -r my.cleanmytv.app.test/androidx.test.runner.AndroidJUnitRunner | tee instrumentation.log
grep -q '^OK (3 tests)' instrumentation.log
adb pull /sdcard/Android/data/my.cleanmytv.app/files/screenshots ui-screenshots
test -f ui-screenshots/editor-ms-clocks.png
test -f ui-screenshots/editor-ms-wallpapers.png
test -f ui-screenshots/editor-en-wallpapers.png
test -f ui-screenshots/tv-home.png
test "$(find ui-screenshots -name 'scene-*.png' | wc -l)" -eq 20
