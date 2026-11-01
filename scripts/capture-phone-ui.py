"""Exercise the installed APK's real language and update controls on the disposable CI emulator."""
from pathlib import Path
import os
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = Path("previews")
OUT.mkdir(exist_ok=True)
PACKAGE = "my.cleanmytv.app"
ACTIVITY = PACKAGE + "/tv.cleanyour.app.MainActivity"

def adb(*args):
    return subprocess.run(["adb", *args], check=True, capture_output=True, text=True, timeout=30).stdout

def window():
    # uiautomator can exit 0 without creating its XML while Android is still settling.
    # Never accept the previous dump as evidence of the new screen.
    adb("shell", "rm", "-f", "/sdcard/cleanmytv-ui.xml")
    adb("shell", "uiautomator", "dump", "/sdcard/cleanmytv-ui.xml")
    xml = adb("shell", "cat", "/sdcard/cleanmytv-ui.xml")
    ET.fromstring(xml)
    return xml

def wait_text(text, timeout=90, home=False):
    end = time.monotonic() + timeout
    xml = ""
    while time.monotonic() < end:
        try:
            xml = window()
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError):
            time.sleep(2)
            continue
        if text in xml:
            return xml
        if "Pixel Launcher isn" in xml:
            adb("shell", "am", "force-stop", "com.google.android.apps.nexuslauncher")
            if home:
                adb("shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.HOME")
            else:
                adb("shell", "am", "start", "-n", ACTIVITY)
        time.sleep(2)
    OUT.joinpath("phone-failure.xml").write_text(xml, encoding="utf-8")
    adb("shell", "screencap", "-p", "/sdcard/cleanmytv-failure.png")
    adb("pull", "/sdcard/cleanmytv-failure.png", str(OUT / "phone-failure.png"))
    raise AssertionError("Expected installed UI text: " + text)

def tap(text):
    root = ET.fromstring(wait_text(text))
    for node in root.iter("node"):
        if node.get("text") == text or node.get("content-desc") == text:
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.attrib["bounds"]))
            adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
            time.sleep(1)
            return
    raise AssertionError("Control not found: " + text)

def shot(name):
    OUT.joinpath(name + ".xml").write_text(window(), encoding="utf-8")
    adb("shell", "screencap", "-p", "/sdcard/cleanmytv-shot.png")
    adb("pull", "/sdcard/cleanmytv-shot.png", str(OUT / (name + ".png")))

def malay():
    tap("EN ▾")
    tap("Bahasa Melayu")
    wait_text("TV lebih kemas")

def english():
    tap("BM ▾")
    tap("English")
    wait_text("A cleaner TV")

def update_dialog(language):
    tap("Kemas kini" if language == "ms" else "Update")
    wait_text("Versi semasa:" if language == "ms" else "Installed version:")
    # Live GitHub can rate-limit shared runners; both Latest and the honest retry state are valid.
    terminal = ("Anda menggunakan versi", "Tidak dapat menyemak") if language == "ms" else ("You are using", "Unable to check")
    end = time.monotonic() + 75
    while time.monotonic() < end:
        xml = window()
        if any(text in xml for text in terminal):
            shot("phone-update-" + language)
            tap("Tutup" if language == "ms" else "Close")
            return
        time.sleep(2)
    raise AssertionError("Update check did not finish")

base = Path("upgrade-test/upgrade-base.apk")
apk = Path("apk-download/cleanMYTV.apk")
if base.exists():
    adb("install", str(base))
    adb("shell", "am", "force-stop", "com.google.android.apps.nexuslauncher")
    adb("shell", "am", "start", "-n", ACTIVITY)
    wait_text("A cleaner TV")
    malay()
    adb("shell", "am", "force-stop", PACKAGE)
    # Android rejects this command on a certificate mismatch. Do not uninstall or clear app data.
    adb("install", "-r", str(apk))
    adb("shell", "am", "start", "-n", ACTIVITY)
    wait_text("TV lebih kemas")
    OUT.joinpath("upgrade-verification.txt").write_text("Same-key in-place upgrade passed; Malay preference retained.\n")
else:
    adb("install", str(apk))
    adb("shell", "am", "force-stop", "com.google.android.apps.nexuslauncher")
    adb("shell", "am", "start", "-n", ACTIVITY)
    wait_text("A cleaner TV")
    malay()

shot("phone-ms")
update_dialog("ms")
english()
shot("phone-en")
update_dialog("en")
for attempt in range(3):
    # Recover the stock emulator launcher, not the app under test. A successful app screen
    # must not masquerade as a launcher screenshot after a Pixel Launcher ANR.
    adb("shell", "am", "start", "-a", "android.intent.action.MAIN", "-c", "android.intent.category.HOME")
    time.sleep(5)
    adb("shell", "input", "swipe", "540", "1700", "540", "350", "350")
    try:
        xml = wait_text("cleanMYTV", timeout=25, home=True)
        if 'package="my.cleanmytv.app"' in xml:
            raise AssertionError("Expected the emulator launcher, not the app window")
        break
    except AssertionError:
        if attempt == 2:
            raise
        adb("shell", "am", "force-stop", "com.google.android.apps.nexuslauncher")
shot("phone-launcher")
