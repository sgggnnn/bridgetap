param(
    [Parameter(Mandatory=$true)][string]$JavaHome,
    [Parameter(Mandatory=$true)][string]$Classpath,
    [Parameter(Mandatory=$true)][string]$OneConfigJar,
    [string]$KotlinJar='',
    [string]$Python='python',
    [string]$Output='dist/BridgeTap-1.7.3-OneClient.jar'
)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$work=Join-Path $root ('build/oneconfig-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $work | Out-Null
& $Python (Join-Path $root 'integrations/oneclient/extract-deps.py') $OneConfigJar $work
if($LASTEXITCODE -ne 0){throw 'OneConfig dependency extraction failed'}
if(-not $KotlinJar){$KotlinJar=Get-ChildItem (Split-Path -Parent $OneConfigJar) -Filter 'fabric-language-kotlin*.jar' | Select-Object -First 1 -ExpandProperty FullName}
if(-not $KotlinJar){throw 'Supply -KotlinJar for the installed Fabric Language Kotlin JAR'}
& $Python (Join-Path $root 'integrations/oneclient/extract-deps.py') $KotlinJar $work
if($LASTEXITCODE -ne 0){throw 'Kotlin compile dependency extraction failed'}
& (Join-Path $PSScriptRoot 'build.ps1') -JavaHome $JavaHome -Classpath $Classpath -Output $Output -Python $Python
$jarPath=if([IO.Path]::IsPathRooted($Output)){$Output}else{Join-Path $root $Output}
$classes=Join-Path $work 'classes';New-Item -ItemType Directory -Force $classes | Out-Null
& (Join-Path $JavaHome 'bin/javac.exe') --release 8 -encoding UTF-8 -proc:none -cp "$jarPath;$work/*;$Classpath" -d $classes @(Get-ChildItem (Join-Path $root 'integrations/oneclient/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
if($LASTEXITCODE -ne 0){throw 'OneConfig adapter compilation failed (JDK 21+ required)'}
& (Join-Path $JavaHome 'bin/jar.exe') uf $jarPath -C $classes .
if($LASTEXITCODE -ne 0){throw 'OneConfig adapter packaging failed'}
Write-Output "Built OneClient integration: $jarPath"
