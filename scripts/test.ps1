param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$RuntimeJavaHome = $JavaHome,
    [string]$Jar = 'dist/BridgeTap-1.10.0-dev.1.jar',
    [string]$MinecraftJar,
    [string]$GsonJar,
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
$sources += @(Get-ChildItem (Join-Path $projectRoot 'tests/sky') -Recurse -Filter '*.java' | ForEach-Object FullName)
& $compiler --release 8 -encoding UTF-8 -cp $artifact -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
$clipTools = Join-Path $projectRoot 'build/libs/BridgeTap-clip-tools-windows-x64.zip'
if (Test-Path -LiteralPath $clipTools) { Expand-Archive -LiteralPath $clipTools -DestinationPath $work }
Push-Location $work
try {
    foreach ($test in @('ConfigRegression','KeybindProfilesRegression','ClipMicrophoneRegression','ClipAudioMixRegression', 'HistoryRegression', 'ReplayRegression','ReplayPresentationRegression','ReplaySkyCompatRegression', 'DisplayRegression', 'ReplayTimelineRegression','ReplayCaptureRegression','ReplayInputTimingRegression', 'ReplayCameraRegression', 'TelemetryRegression','JumpVelocityRegression','BothAxesRegression', 'BridgeGeometryRegression','ServerModeRegression', 'ReplayLibraryRegression','ReplayShareRegression', 'ReplayControlsRegression','ReplayClickRegression', 'BridgeDetectionRegression', 'ReplayQueryRegression', 'OverlayPlacementRegression','OverlayStyleRegression','ZeroDeltaRegression','TechniqueColorsRegression','ReplayAimAnalysisRegression','FailAttemptEvidenceRegression','FailAnalysisOptInRegression', 'IslandRegression','IslandAutoPlacementRegression','IslandLegacyRolesRegression','IslandAlignmentRegression','IslandNarrowFloorRegression','IslandModeChangeRegression','AttemptScheduleRegression','IslandBridgeRowRegression','IslandSharedAxisRegression','IslandPlatformDeckRegression','IslandPresetsRegression','FocusAudioRegression','PresetServersRegression','PresetArchiveRegression','IslandEditorRegression','FailureAnalysisRegression','ReplayVideoRegression','ReplayVideoPresentationRegression', 'ReplayFoldersRegression','ReplayRetentionRegression','ReplayStorageRegression', 'ClipRegression','NativeClipToolsRegression','ClipStreamRegression','DefaultIslandsRegression','UpdateRegression','UpdatePromptRegression','BridgeStatsRegression','ServerModeProbeRegression')) {
        & $runtime -cp "$artifact;$classes" $test
        if ($LASTEXITCODE -ne 0) { throw "Regression failed: $test" }
    }
    & $runtime -cp "$artifact;$classes" ReplayBookmarksRegression
    if ($LASTEXITCODE -ne 0) { throw 'Replay bookmark regression failed' }
    & $runtime -cp "$artifact;$classes" ReplayJumpBookmarksRegression
    if ($LASTEXITCODE -ne 0) { throw 'Automatic jump bookmark regression failed' }
    & $runtime -cp "$artifact;$classes" RunStartConfirmationRegression
    if ($LASTEXITCODE -ne 0) { throw 'Run start confirmation regression failed' }
    & $runtime -cp "$artifact;$classes" FailureDisplayRegression
    if ($LASTEXITCODE -ne 0) { throw 'Failure display regression failed' }
    & $runtime -cp "$artifact;$classes" ClipStreamRegression 120
    if ($LASTEXITCODE -ne 0) { throw "120 FPS native clip regression failed" }
} finally { Pop-Location }
if ($MinecraftJar) {
    $checker = if ($Forge) { 'tests/check_forge_hooks.py' } else { 'tests/check_native_hooks.py' }
    & $Python (Join-Path $projectRoot $checker) $MinecraftJar $artifact
    if ($LASTEXITCODE -ne 0) { throw 'Native hook checks failed' }
}
if (-not $GsonJar) {
    $gsonRoot = Join-Path $projectRoot 'build/gradle-bootstrap/libraries/com/google/code/gson'
    if (Test-Path -LiteralPath $gsonRoot) { $GsonJar = Get-ChildItem -LiteralPath $gsonRoot -Recurse -Filter '*.jar' | Select-Object -First 1 -ExpandProperty FullName }
}
if (-not $GsonJar) { throw 'Supply -GsonJar for the update metadata tests, or build once with Gradle.' }
& $compiler --release 8 -proc:none -encoding UTF-8 -cp "$artifact;$GsonJar" -d $classes (Join-Path $projectRoot 'tests/update/UpdateMetadataRegression.java')
if ($LASTEXITCODE -ne 0) { throw 'Update metadata test compilation failed' }
& $runtime -cp "$classes;$artifact;$GsonJar" UpdateMetadataRegression
if ($LASTEXITCODE -ne 0) { throw 'Update metadata regression failed' }
Write-Output 'All requested checks passed.'
