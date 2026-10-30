"""Restore an approved GitHub Actions secret without writing credentials into the checkout."""
import base64
import hashlib
import json
import os
from pathlib import Path
import subprocess

payload = os.environ.get("CLEANMYTV_SIGNING", "")
required = os.environ.get("REQUIRE_SIGNING", "false") == "true"
if not payload:
    if required:
        raise SystemExit("The protected CLEANMYTV_SIGNING secret must be configured before a release build.")
    with open(os.environ["GITHUB_ENV"], "a") as env:
        env.write("CLEANMYTV_SIGNING_MODE=debug\n")
    with open(os.environ["GITHUB_OUTPUT"], "a") as out:
        out.write("signed_release=false\n")
    raise SystemExit(0)

secret = json.loads(payload)
password = secret["password"]
assert isinstance(password, str) and len(password) >= 24 and "\n" not in password and "\r" not in password
assert secret["alias"] == "cleanmytv"
print("::add-mask::" + password)
key = Path(os.environ["RUNNER_TEMP"]) / "cleanmytv-release.p12"
password_file = Path(os.environ["RUNNER_TEMP"]) / "cleanmytv-signing.pass"
key.write_bytes(base64.b64decode(secret["keystore"], validate=True))
password_file.write_text(password, encoding="utf-8")
key.chmod(0o600)
password_file.chmod(0o600)
cert = subprocess.run(["keytool", "-exportcert", "-keystore", str(key), "-alias", "cleanmytv",
    "-storepass:file", str(password_file)], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE).stdout
expected = Path("signing-certificate.sha256").read_text().strip()
assert hashlib.sha256(cert).hexdigest() == expected, "Signing certificate does not match the pinned release identity"
with open(os.environ["GITHUB_ENV"], "a") as env:
    env.write(f"CLEANMYTV_SIGNING_MODE=release\nCYT_RELEASE_KEYSTORE={key}\nCYT_RELEASE_KEY_ALIAS=cleanmytv\nCYT_RELEASE_KEYSTORE_PASSWORD_FILE={password_file}\n")
with open(os.environ["GITHUB_OUTPUT"], "a") as out:
    out.write("signed_release=true\n")
print("Pinned cleanMYTV release signing identity verified.")
