param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$RuntimeJavaHome = $JavaHome,
    [string]$Jar = 'dist/BridgeTap-1.7.0.jar',
    [string]$MinecraftJar,
    [string]$Python = 'python',
    [switch]$Forge
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$compiler = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac' }
$runtime = if ($RuntimeJavaHome) { Join-Path $RuntimeJavaHome 'bin/java.exe' } else { 'java' }
$artifact = if ([IO.Path]::IsPathRooted($Jar)) { $Jar } else { Join-Path $projectRoot $Jar }
if (-not (Test-Path -LiteralPath $artifact)) { throw "Build or download the mod first: $artifact" }
$work = Join-Path $projectRoot ('build/tests-' + [guid]::NewGuid().ToString('N'))
$classes = Join-Path $work 'classes'
New-Item -ItemType Directory -Force $classes | Out-Null
$sources = @(Get-ChildItem (Join-Path $projectRoot 'tests') -Filter '*.java' | ForEach-Object FullName)
& $compiler --release 8 -encoding UTF-8 -cp $artifact -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
$clipTools = Join-Path $projectRoot 'build/libs/BridgeTap-clip-tools-windows-x64.zip'
if (Test-Path -LiteralPath $clipTools) { Expand-Archive -LiteralPath $clipTools -DestinationPath $work }
Push-Location $work
try {
    foreach ($test in @('ConfigRegression', 'HistoryRegression', 'ReplayRegression', 'DisplayRegression', 'ReplayTimelineRegression','ReplayCaptureRegression', 'ReplayCameraRegression', 'TelemetryRegression','JumpVelocityRegression', 'BridgeGeometryRegression','ServerModeRegression', 'ReplayLibraryRegression', 'ReplayControlsRegression', 'BridgeDetectionRegression', 'ReplayQueryRegression', 'OverlayPlacementRegression','OverlayStyleRegression', 'IslandRegression','IslandPresetsRegression','FocusAudioRegression','PresetServersRegression','PresetArchiveRegression','IslandEditorRegression','FailureAnalysisRegression','ReplayVideoRegression','ReplayVideoPresentationRegression', 'ReplayFoldersRegression','ReplayRetentionRegression', 'ClipRegression','NativeClipToolsRegression','ClipStreamRegression')) {
        & $runtime -cp "$artifact;$classes" $test
        if ($LASTEXITCODE -ne 0) { throw "Regression failed: $test" }
    }
    & $runtime -cp "$artifact;$classes" ClipStreamRegression 120
    if ($LASTEXITCODE -ne 0) { throw "120 FPS native clip regression failed" }
} finally { Pop-Location }
if ($MinecraftJar) {
    $checker = if ($Forge) { 'tests/check_forge_hooks.py' } else { 'tests/check_native_hooks.py' }
    & $Python (Join-Path $projectRoot $checker) $MinecraftJar $artifact
    if ($LASTEXITCODE -ne 0) { throw 'Native hook checks failed' }
}
Write-Output 'All requested checks passed.'
