"""Record public APK identity; release packages must match the pinned signing certificate."""
import glob
import json
import os
from pathlib import Path
import re
import subprocess
import sys

apk = Path(sys.argv[1])
mode = sys.argv[2]
assert mode in ("debug", "release")
sdk = os.environ.get("ANDROID_HOME") or os.environ["ANDROID_SDK_ROOT"]
tools = sorted(glob.glob(str(Path(sdk) / "build-tools" / "*" / "apksigner")))[-1]
result = subprocess.run([tools, "verify", "--print-certs", str(apk)], check=True, capture_output=True, text=True)
certs = re.findall(r"Signer #\d+ certificate SHA-256 digest: ([0-9a-f]+)", result.stdout)
assert len(certs) == 1
if mode == "release":
    assert certs[0] == Path("signing-certificate.sha256").read_text().strip(), "Wrong release signing certificate"
badging = subprocess.run([str(Path(tools).with_name("aapt2")), "dump", "badging", str(apk)], check=True, capture_output=True, text=True).stdout
package = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging)
assert package and package[1] == "my.cleanmytv.app"
metadata = dict(package=package[1], versionCode=int(package[2]), versionName=package[3], signing=mode, certificateSha256=certs[0])
apk.with_name("release-info.json").write_text(json.dumps(metadata, indent=2) + "\n")
apk.with_name("SIGNING_CERTIFICATE.txt").write_text(certs[0] + "\n")
print(json.dumps(metadata))
