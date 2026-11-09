param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Classpath = $env:BRIDGETAP_CLASSPATH,
    [string]$Output = 'BridgeTap-1.1.0.jar'
)
$ErrorActionPreference = 'Stop'
if (-not $Classpath) { throw 'Set BRIDGETAP_CLASSPATH to the intermediary Minecraft, Fabric Loader, Mixin and LWJGL JARs (semicolon-separated on Windows).' }
$compiler = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac' }
$archiver = if ($JavaHome) { Join-Path $JavaHome 'bin/jar.exe' } else { 'jar' }
$classes = Join-Path $PSScriptRoot ('build/classes-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $classes | Out-Null
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'src/main/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
& $compiler --release 8 -encoding UTF-8 -proc:none -cp $Classpath -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
$destination = Join-Path $PSScriptRoot $Output
if (Test-Path -LiteralPath $destination) { throw "Output already exists: $destination" }
& $archiver cf $destination -C $classes . -C (Join-Path $PSScriptRoot 'src/main/resources') .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed' }
Write-Output "Built $destination"
