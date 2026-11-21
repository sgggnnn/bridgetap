# BridgeTapClient Notifications — single JAR

Minecraft 1.8.9 Ornithe, Java 21+ and OneConfig 1.9.0+ on Windows x64.

The mod receives new Windows toast notifications from every application and displays them through OneConfig's own notification renderer. Application name, title and message are shown by default. An optional app allowlist, content masking, duration and master toggle are available under **Right Shift → BridgeTapClient Notifications**.

There is **no companion application, executable, service, loopback server or separate runtime to install**. The JAR embeds a small JNI DLL, automatically loaded into Minecraft's JVM. Windows' notification API runs on a dedicated STA dispatcher inside the game process; polling returns to that dispatcher after asynchronous waits. Notification bodies remain in memory in bounded queues.

Version **1.1.1** fixes a startup compatibility issue with OneConfig 1.9: reflecting over every notification overload attempted to resolve the absent legacy `org.jetbrains.skia.Image` type, preventing the mod's settings card from being registered. The integration now links only the exact modern notification signatures through method handles. The corrected initialization was verified in the running game: settings card registered, all four senders linked, Windows listener allowed, and a preview was queued without a renderer error.

If Windows has already granted access, listening starts automatically. Otherwise use **Windows → Allow access** in the mod's settings and answer Windows' permission prompt. No code changes Windows privacy settings. Only notifications actually delivered through Windows toast notifications can be received. Existing notification history is ignored when listening starts; disabling the mod stops new reads and clears its pending Windows notifications.

BridgeTap confirmations and errors use the optional `com.bridgetapclient.notifications.Notifications.post` API. The updated BridgeTap JAR adds import, clip, replay video and update hooks; BridgeTap continues to work without the notification addon.

## Build

Run `scripts/build-notifications.ps1` in a Visual Studio x64 developer shell with MSVC, Windows SDK, JDK 21+, intermediary Minecraft compile dependencies and the extracted OneConfig config API dependencies. Pass `-JavaHome`, `-Classpath`, `-OneConfigDependencies` and optionally `-WinRTHeaders`.

The current build uses Windows SDK 10.0.26100.0 and Microsoft.Windows.CppWinRT **3.0.260818.1**. Generate projections with the package's `cppwinrt.exe -input sdk -output build/notifications-winrt`. These are developer tools only, not runtime software required by the user. C++/WinRT's MIT license and the mod's GPL license are included in the JAR.

`tests/NotificationsRegression.java` tests all-app/full-message defaults, optional app filtering and privacy, pending-message redaction, deduplication, expiry, bounded queues, stopping Windows while preserving BridgeTap events, automatic DLL extraction, JNI Unicode transport and Windows API activation. With existing access it also verifies the real native listener and dispatcher without requesting new permission or logging notification contents.

Microsoft references: [Notification listener](https://learn.microsoft.com/en-us/windows/apps/develop/notifications/app-notifications/notification-listener), [DispatcherQueue](https://learn.microsoft.com/en-us/uwp/api/windows.system.dispatcherqueuecontroller).
