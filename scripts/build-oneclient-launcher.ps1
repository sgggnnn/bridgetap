param(
    [string]$ModJar='dist/BridgeTap-1.8.1-OneClient.jar',
    [string]$Python='python',
    [string]$Destination=''
)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
if(-not $Destination){$Destination=Join-Path $root ('build/BridgeTapClient-'+[guid]::NewGuid().ToString('N'))}
$payload=if([IO.Path]::IsPathRooted($ModJar)){$ModJar}else{Join-Path $root $ModJar}
& $Python (Join-Path $root 'integrations/oneclient/prepare-launcher.py') --jar $payload --destination $Destination
if($LASTEXITCODE -ne 0){throw 'Launcher source preparation failed'}
Push-Location $Destination
try {
    & cargo build --locked --release -p oneclient_app
    if($LASTEXITCODE -ne 0){throw 'Launcher build failed. Use a Visual Studio Developer PowerShell with C++ build tools and Windows SDK installed.'}
    Copy-Item -LiteralPath 'target/release/oneclient_app.exe' -Destination (Join-Path $root 'dist/BridgeTapClient-local.exe')
} finally {Pop-Location}
