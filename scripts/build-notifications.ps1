param(
    [Parameter(Mandatory=$true)][string]$JavaHome,
    [Parameter(Mandatory=$true)][string]$Classpath,
    [Parameter(Mandatory=$true)][string]$OneConfigDependencies,
    [string]$WinRTHeaders='build/notifications-winrt'
)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
if(!(Get-Command cl.exe -ErrorAction SilentlyContinue)){throw 'Run this script in the Visual Studio x64 developer shell (MSVC and Windows SDK required)'}
$work=Join-Path $root ('build/notifications-'+[guid]::NewGuid().ToString('N'))
$classes=Join-Path $work 'classes';$native=Join-Path $classes 'native/windows-x64'
New-Item -ItemType Directory -Force $classes,$native | Out-Null
& cl.exe /nologo /std:c++20 /EHsc /permissive- /MT /O2 /LD "/I$WinRTHeaders" "/I$JavaHome/include" "/I$JavaHome/include/win32" (Join-Path $root 'integrations/notifications/native/windows_notifications.cpp') "/Fo$work/windows_notifications.obj" "/Fe$native/BridgeTapNotifications.dll" /link "/IMPLIB:$work/BridgeTapNotifications.lib" windowsapp.lib runtimeobject.lib CoreMessaging.lib user32.lib
if($LASTEXITCODE -ne 0){throw 'Native Windows notification library compilation failed'}
& (Join-Path $JavaHome 'bin/javac.exe') --release 8 -encoding UTF-8 -proc:none -cp "$OneConfigDependencies;$Classpath" -d $classes @(Get-ChildItem (Join-Path $root 'integrations/notifications/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
if($LASTEXITCODE -ne 0){throw 'Notification mod compilation failed'}
$version=(Get-Content (Join-Path $root 'integrations/notifications/resources/fabric.mod.json') -Raw | ConvertFrom-Json).version
$output=Join-Path $root "dist/BridgeTapClient-Notifications-$version-Ornithe-1.8.9.jar"
& (Join-Path $JavaHome 'bin/jar.exe') cf $output -C $classes . -C (Join-Path $root 'integrations/notifications/resources') .
if($LASTEXITCODE -ne 0){throw 'Notification JAR packaging failed'}
Write-Output "Built single-JAR notification mod: $output"
