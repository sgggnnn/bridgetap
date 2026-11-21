param(
    [string]$ModJar='',
    [string]$Python='python',
    [string]$Destination=''
)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
if(-not $ModJar){
    $metadata=Get-Content (Join-Path $root 'src/main/resources/fabric.mod.json') -Raw | ConvertFrom-Json
    $ModJar='build/libs/BridgeTap-'+$metadata.version+'-OneClient.jar'
}
if(-not (Get-Command cargo -ErrorAction SilentlyContinue)){
    $cargoBin=Join-Path $env:USERPROFILE '.cargo/bin'
    if(Test-Path (Join-Path $cargoBin 'cargo.exe')){$env:PATH=$cargoBin+';'+$env:PATH}
}
# Load MSVC and Windows SDK when invoked from ordinary PowerShell.
if(-not (Get-Command link.exe -ErrorAction SilentlyContinue)){
    $vswhere=Join-Path ${env:ProgramFiles(x86)} 'Microsoft Visual Studio/Installer/vswhere.exe'
    if(Test-Path -LiteralPath $vswhere){
        $installation=& $vswhere -latest -products '*' -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath
        if($installation){
            & (Join-Path $installation 'Common7/Tools/Launch-VsDevShell.ps1') -Arch amd64 -HostArch amd64 -SkipAutomaticLocation
        }
    }
}
if(-not $Destination){$Destination=Join-Path $root ('build/BridgeTapClient-'+[guid]::NewGuid().ToString('N'))}
$payload=if([IO.Path]::IsPathRooted($ModJar)){$ModJar}else{Join-Path $root $ModJar}
& $Python (Join-Path $root 'integrations/oneclient/prepare-launcher.py') --jar $payload --destination $Destination
if($LASTEXITCODE -ne 0){throw 'Launcher source preparation failed'}
Push-Location $Destination
try {
    & cargo build --locked --release -p oneclient_app
    if($LASTEXITCODE -ne 0){throw 'Launcher build failed. Use a Visual Studio Developer PowerShell with C++ build tools and Windows SDK installed.'}
    New-Item -ItemType Directory -Force (Join-Path $root 'dist') | Out-Null
    Copy-Item -LiteralPath 'target/release/oneclient_app.exe' -Destination (Join-Path $root 'dist/bridgetapclient.exe')
} finally {Pop-Location}
