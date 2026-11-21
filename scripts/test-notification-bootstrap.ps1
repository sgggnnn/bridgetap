param([Parameter(Mandatory=$true)][string]$JavaHome,[string]$ProductJar='dist/BridgeTapClient-Notifications-1.1.1-Ornithe-1.8.9.jar')
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$work=Join-Path $root ('build/notification-bootstrap-'+[guid]::NewGuid().ToString('N'))
$dependency=Join-Path $work 'dependency';$fixture=Join-Path $work 'fixture'
New-Item -ItemType Directory -Force $dependency,$fixture | Out-Null
& "$JavaHome/bin/javac.exe" --release 8 -proc:none -d $dependency @(Get-ChildItem (Join-Path $root 'tests/notifications-bootstrap/dependency') -Recurse -Filter '*.java' | ForEach-Object FullName)
if($LASTEXITCODE -ne 0){throw 'Legacy compile-only fixture failed'}
& "$JavaHome/bin/javac.exe" --release 8 -proc:none -cp "$dependency;$ProductJar" -d $fixture @(Get-ChildItem (Join-Path $root 'tests/notifications-bootstrap/fixture') -Recurse -Filter '*.java' | ForEach-Object FullName)
if($LASTEXITCODE -ne 0){throw 'Notification API fixture compilation failed'}
# The dependency folder is intentionally excluded from the runtime classpath.
& "$JavaHome/bin/java.exe" -cp "$fixture;$ProductJar" com.bridgetapclient.notifications.NotificationBootstrapRegression
if($LASTEXITCODE -ne 0){throw 'Notification bootstrap failed with optional legacy graphics type absent'}
