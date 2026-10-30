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
    return subprocess.run(["adb", *args], check=True, capture_output=True, text=True).stdout

def window():
    adb("shell", "uiautomator", "dump", "/sdcard/cleanmytv-ui.xml")
    return adb("shell", "cat", "/sdcard/cleanmytv-ui.xml")

def wait_text(text, timeout=60):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        xml = window()
        if text in xml:
            return xml
        if "Pixel Launcher isn" in xml:
            adb("shell", "am", "force-stop", "com.google.android.apps.nexuslauncher")
            adb("shell", "am", "start", "-n", ACTIVITY)
        time.sleep(2)
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
adb("shell", "input", "keyevent", "KEYCODE_HOME")
time.sleep(5)
adb("shell", "input", "swipe", "540", "1700", "540", "350", "350")
time.sleep(3)
wait_text("cleanMYTV")
shot("phone-launcher")
