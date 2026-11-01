"""Extract the exact bundled TV APK and verify its hash, Android identity and dedicated signer."""
import glob
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import zipfile

phone = Path(sys.argv[1])
out = phone.parent
with zipfile.ZipFile(phone) as archive:
    apk = archive.read("assets/bundled/cleanmytv-prayer.apk")
    pin = json.loads(archive.read("assets/bundled/cleanmytv-prayer.json").decode("utf-8-sig"))
digest = hashlib.sha256(apk).hexdigest()
assert pin["package"] == "tv.cleanmytv.prayer" and pin["versionCode"] == 3
assert digest == pin["sha256"]
assert pin["signer"] == "48c25741767909d84e0b225334fe96bb9afb65985b6e62ed0ee6eb24b29ac03f"
target = out / "Prayer-Time-TV.apk"
target.write_bytes(apk)
sdk = os.environ.get("ANDROID_HOME") or os.environ["ANDROID_SDK_ROOT"]
tools = sorted(glob.glob(str(Path(sdk) / "build-tools" / "*" / "apksigner")))[-1]
result = subprocess.run([tools, "verify", "--print-certs", str(target)], check=True, capture_output=True, text=True)
certs = set(re.findall(r"certificate SHA-256 digest:\s*([0-9a-fA-F]{64})", result.stdout + result.stderr))
assert {value.lower() for value in certs} == {pin["signer"]}
badging = subprocess.run([str(Path(tools).with_name("aapt2")), "dump", "badging", str(target)], check=True, capture_output=True, text=True).stdout
package = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging)
assert package and package[1] == pin["package"] and int(package[2]) == pin["versionCode"]
metadata = dict(package=package[1], versionCode=int(package[2]), versionName=package[3], sha256=digest, certificateSha256=pin["signer"])
(out / "tv-release-info.json").write_text(json.dumps(metadata, indent=2) + "\n")
(out / "TV-SHA256SUMS.txt").write_text(f"{digest}  Prayer-Time-TV.apk\n")
print(json.dumps(metadata))
