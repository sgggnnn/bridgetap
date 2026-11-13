param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Classpath = $env:BRIDGETAP_CLASSPATH,
    [string]$Output = 'dist/BridgeTap-1.8.1.jar',
    [string]$Python = 'python'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $Classpath) { throw 'Set BRIDGETAP_CLASSPATH to Minecraft intermediary, Fabric Loader, Mixin, LWJGL 2, Authlib and Guava JARs (semicolon-separated on Windows).' }
$gsonRoot = Join-Path $projectRoot 'build/gradle-bootstrap/libraries/com/google/code/gson'
if (Test-Path -LiteralPath $gsonRoot) {
    $gsonJar = Get-ChildItem -LiteralPath $gsonRoot -Recurse -Filter '*.jar' | Select-Object -First 1 -ExpandProperty FullName
    if ($gsonJar) { $Classpath += ";$gsonJar" }
}
$compiler = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac' }
$archiver = if ($JavaHome) { Join-Path $JavaHome 'bin/jar.exe' } else { 'jar' }
$classes = Join-Path $projectRoot ('build/classes-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $classes | Out-Null
$sources = @(Get-ChildItem (Join-Path $projectRoot 'src/main/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
& $compiler --release 8 -encoding UTF-8 -proc:none -cp $Classpath -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
$destination = if ([IO.Path]::IsPathRooted($Output)) { $Output } else { Join-Path $projectRoot $Output }
if (Test-Path -LiteralPath $destination) { throw "Output already exists: $destination" }
New-Item -ItemType Directory -Force (Split-Path -Parent $destination) | Out-Null
& $archiver cf $destination -C $classes . -C (Join-Path $projectRoot 'src/main/resources') .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed' }
$runtime = if ($JavaHome) { Join-Path $JavaHome 'bin/java.exe' } else { 'java' }
& $Python (Join-Path $PSScriptRoot 'video_bundle.py') $destination --java $runtime
if ($LASTEXITCODE -ne 0) { throw 'Video runtime packaging failed' }
Write-Output "Built $destination"
