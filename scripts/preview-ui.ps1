param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$RuntimeJavaHome = $JavaHome,
    [string]$Classpath = $env:BRIDGETAP_CLASSPATH,
    [Parameter(Mandatory=$true)][string]$Minecraft,
    [string]$Jar = 'dist/BridgeTap-1.7.3.jar'
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$compiler = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac' }
$runtime = if ($RuntimeJavaHome) { Join-Path $RuntimeJavaHome 'bin/java.exe' } else { 'java' }
$artifact = if ([IO.Path]::IsPathRooted($Jar)) { $Jar } else { Join-Path $root $Jar }
$minecraftJar = (Resolve-Path -LiteralPath $Minecraft).Path
if (-not (Test-Path -LiteralPath $artifact)) { throw "Build the Ornithe edition first: $artifact" }
$work = Join-Path $root ('build/ui-preview-' + [guid]::NewGuid().ToString('N'))
$classes = Join-Path $work 'classes'
New-Item -ItemType Directory -Force $classes | Out-Null
& $compiler --release 8 -proc:none -encoding UTF-8 -cp "$artifact;$Classpath" -d $classes (Join-Path $root 'tests/ui/UiPreview.java')
if ($LASTEXITCODE -ne 0) { throw 'UI preview compilation failed' }
Push-Location $work
try {
    & $runtime '-Djava.awt.headless=true' -cp "$classes;$artifact;$Classpath" UiPreview $minecraftJar
    if ($LASTEXITCODE -ne 0) { throw 'UI preview or interaction checks failed' }
} finally { Pop-Location }
Write-Output "UI previews: $work"
