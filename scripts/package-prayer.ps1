# Build/sign the optional companion with its existing dedicated key, then pin the packaged asset.
# Required: JAVA_HOME, ANDROID_HOME, CLEANMYTV_PRAYER_KEYSTORE, CLEANMYTV_PRAYER_PASSWORD.
# Keep that key and password outside the repository and back them up for future updates.
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    if (-not $env:CLEANMYTV_PRAYER_KEYSTORE -or -not $env:CLEANMYTV_PRAYER_PASSWORD) { throw 'Set the existing companion signing key and password environment variables first.' }
    & .\gradlew.bat :tv:prayer:assembleRelease :tv:prayer:testDebugUnitTest --no-configuration-cache
    if ($LASTEXITCODE -ne 0) { throw 'Companion build/tests failed.' }
    $buildTools = Join-Path $env:ANDROID_HOME 'build-tools/36.0.0'
    $aligned = Join-Path $PWD 'tv/prayer/build/prayer-aligned.apk'
    $asset = 'android/platform/src/main/assets/bundled/cleanmytv-prayer.apk'
    & (Join-Path $buildTools 'zipalign.exe') -f -p 4 tv/prayer/build/outputs/apk/release/prayer-release-unsigned.apk $aligned
    if ($LASTEXITCODE -ne 0) { throw 'Alignment failed.' }
    & (Join-Path $buildTools 'apksigner.bat') sign --ks $env:CLEANMYTV_PRAYER_KEYSTORE --ks-key-alias prayer --ks-pass env:CLEANMYTV_PRAYER_PASSWORD --v4-signing-enabled false --out $asset $aligned
    if ($LASTEXITCODE -ne 0) { throw 'Signing failed.' }
    $certificate = & (Join-Path $buildTools 'apksigner.bat') verify --print-certs $asset
    if ($LASTEXITCODE -ne 0) { throw 'Signature verification failed.' }
    $signer = ($certificate | Select-String '^Signer #1 certificate SHA-256 digest: (.*)$').Matches.Groups[1].Value
    if ($signer -notmatch '^[0-9a-fA-F]{64}$') { throw 'Signing certificate digest missing.' }
    @{sha256=(Get-FileHash $asset -Algorithm SHA256).Hash.ToLower(); signer=$signer.ToLower(); package='tv.cleanmytv.prayer'; versionCode=2} |
        ConvertTo-Json | Set-Content -Encoding utf8NoBOM android/platform/src/main/assets/bundled/cleanmytv-prayer.json
    Write-Output 'Companion bundled and pinned. No private signing material was copied into the repository.'
} finally { Pop-Location }
