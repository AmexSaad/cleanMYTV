#!/usr/bin/env bash
set -euo pipefail
gradle assembleDebugAndroidTest --no-daemon --console=plain
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -r my.cleanmytv.app.test/androidx.test.runner.AndroidJUnitRunner | tee instrumentation.log
grep -q '^OK (3 tests)' instrumentation.log
mkdir -p ui-screenshots
# Scoped storage prevents the shell UID reading app-specific external storage directly.
# Read only the test screenshot directory as the debuggable app's own UID.
adb exec-out run-as my.cleanmytv.app tar -C /sdcard/Android/data/my.cleanmytv.app/files/screenshots -cf - . | tar -xf - -C ui-screenshots
test -f ui-screenshots/editor-ms-clocks.png
test -f ui-screenshots/editor-ms-wallpapers.png
test -f ui-screenshots/editor-en-wallpapers.png
test -f ui-screenshots/tv-home.png
test "$(find ui-screenshots -name 'scene-*.png' | wc -l)" -eq 20
