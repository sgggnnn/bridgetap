# bridgetapclient — OneClient integration

bridgetapclient is an unofficial OneClient edition with BridgeTap bundled and enabled by default on Minecraft 1.8.9 Ornithe profiles. The Windows x64 executable is available locally as `dist/bridgetapclient.exe` with BridgeTap 1.10.0-dev.6 embedded.

## In-game integration

The OneClient JAR registers **BridgeTap** in OneConfig with native switches and numeric controls for coordinate visibility, replay telemetry, keystrokes, backgrounds, shadow, chat output, F3 visibility, gain labels and detection offsets. Settings stay synchronized with BridgeTap's existing configuration.

The **Replays**, **Islands** and **Island presets** buttons open the original BridgeTap menus again. The B key opens the original replay file manager. Search, folders, filters, island management and presets keep their existing behavior. The experimental native library pages from dev.6/dev.7 have been removed.

OneConfig continues to provide native settings and the shared HUD editor. Video clips, block appearance, world editors and advanced settings retain their existing specialized screens. The Overlay colors button opens the native OneClient HUD editor.

Fabric and Forge author metadata identify the mod as **sggn**; restart the client after replacing the JAR so OneClient refreshes the author label.

Three independent HUDs are registered in the OneConfig HUD editor: **Bridge coordinates**, **Replay information**, **Replay keystrokes**. Dragging, scale and visibility update the same configuration used by the existing BridgeTap editor. Replay information and recorded keystrokes remain replay-only during normal gameplay; editor previews show all three.

Requires Minecraft **1.8.9 Ornithe**, OneConfig **1.7.0**, its usual dependencies, and Java **21+** (tested against installed Java 25 libraries). Do not install the Forge JAR in this profile or keep a second BridgeTap JAR enabled.

Build the integration with JDK 21+:

```powershell
./scripts/build-oneclient.ps1 -JavaHome <jdk> -Classpath <existing-intermediary-classpath> -OneConfigJar <OneConfig-1.8.9-ornithe-1.7.0.jar> -Python python
```

OneConfig dependencies are compile-only and are not repackaged into BridgeTap. The ordinary standalone builds keep working without OneConfig.

## Customized launcher source

`upstream.txt` pins the upstream OneLauncher commit. `launcher.patch` contains the small upstream changes; `launcher/` contains new source modules. `prepare-launcher.py` fetches that commit, checks the patch, and supplies the locally built mod payload:

```powershell
python integrations/oneclient/prepare-launcher.py --jar build/libs/BridgeTap-1.10.0-dev.6-OneClient.jar --destination build/BridgeTapClient
cd build/BridgeTapClient
cargo build --locked --release -p oneclient_app
```

Rust 1.97+ and Windows Visual C++ Build Tools/Windows SDK are required on Windows. The upstream binary is produced at `target/release/oneclient_app.exe`; the wrapper copies it to `dist/bridgetapclient.exe`. MSVC and Windows SDK have been installed locally. Local Rust build scripts are blocked by the Windows application-control policy, so the Windows build runs on GitHub Actions.

The customized launcher adds a **BridgeTap** navigation page with BridgeTap enabled by default on compatible profiles and a persistent per-profile opt-out. Installation happens after normal content materialization, under the existing launch directory lock, and only on 1.8.9 Ornithe profiles. Enable the normal OneConfig bundle before launching. Previous BridgeTap JARs are identified by manifest ID and moved to `.oneclient/bridgetap-backups`; they are not deleted. Configurations, recordings and island libraries are preserved. Disabling stops installing the embedded payload on subsequent launches; backups are retained for manual recovery.

The local edition has a distinct data directory, IPC endpoint, application identity and title. Upstream self-update and protocol-handler registration are disabled to keep it from replacing the fork or the official launcher. Existing OneClient accounts and profiles are not automatically copied; use the launcher's import flow.

## Validation and remaining checks

The Java mod regression suite and real menu preview suite passed. Contract tests check all three HUD wrappers against the actual installed OneConfig API (positions, scale, visibility, provider IDs, bounds and resize policy). The optional settings adapter compiles against that API. The build script also extracts compile-only dependencies from the installed Fabric Language Kotlin JAR (automatically found beside OneConfig, or supplied through `-KotlinJar`).

The full Windows launcher compiles successfully. Remaining: test native OneConfig registration, menu transitions and HUD dragging in a running Minecraft client. Windows application-control policy blocks this unsigned executable on the local PC, so no claim of local startup or in-game visual verification is made.

Upstream: https://github.com/Polyfrost/OneLauncher and https://github.com/Polyfrost/OneConfig. The launcher retains upstream GPL-3.0 licensing and copyright notices. BridgeTap's adapter uses the installed OneConfig API and does not bundle OneConfig source or binaries.

## OneClient HUD style controls (dev.9)

Open **Edit HUD**, select **Bridge coordinates**, **Replay information** or **Replay keystrokes**, then use **Style** for uniform text color, background color/opacity and normal/full text shadow with a custom shadow color. The renderers and editor previews use the same saved RGB/ARGB values. The font-family/weight/case controls offered by OneClient are not implemented by BridgeTap's legacy renderer.

The HUD's **Settings** tab adds native color pickers for individual coordinate components (labels, coordinates, deltas, ticks, gain labels, header, background, border, Velo, positive/negative X-Z delta and jump angle). The replay keystrokes expose separate released/pressed text and background colors plus the active accent. Each HUD has a **Reset this overlay's style** action. Selecting an individual coordinate color restores component coloring if a uniform Style text color was previously applied.

Styles are stored locally in `config/bridgetap/overlay-style.properties`. RGB formatting is applied only when drawing the HUD; server chat and saved run summaries retain Minecraft's legacy formatting. Replay telemetry and keystrokes remain replay-only during gameplay. The original replay, island and preset menus remain active.

The native Style adapter was exercised with an actual OneConfig Hud instance in a headless contract test, including both directions of color/background/shadow synchronization. Visual behavior in a running game still needs user testing.


## Preinstalled local edition (2026-11-20)

The launcher embeds the native OneClient JAR and installs it automatically on the first launch of each Minecraft 1.8.9 Ornithe profile. Keep the regular OneConfig bundle enabled; other Minecraft versions/loaders are excluded. The BridgeTap page allows opting out. The choice persists in `.oneclient/bridgetap-disabled`; removing that marker or re-enabling restores the bundled default. OneConfig settings, RGB controls and three HUDs use the adapter already included in the payload.

Build with `./scripts/build-oneclient-launcher.ps1 -Python <python-executable>` after `./gradlew.bat build`. The script selects the current mod version from `fabric.mod.json`. Source preparation rejects a JAR without the native adapter, records its version and SHA-256 in `BRIDGETAP-BUILD.json`, and preserves existing source destinations.

The **Build bridgetapclient** GitHub Actions workflow builds the Windows launcher on a runner with MSVC. A checked-in native payload can be supplied under `integrations/oneclient/payload/`; otherwise it builds the current JAR. The dedicated build branch is `codex/oneclient-integrated-build`. Executable build and upload: https://github.com/sgggnnn/bridgetap/actions/runs/44849597708. That run failed afterwards because the upstream core tests require debug assertions; the verification-only rerun uses `cargo test --locked -p oneclient_core --lib bridgetap::tests`: https://github.com/sgggnnn/bridgetap/actions/runs/44899635639.

Local validation for this change: payload acceptance/rejection tests passed; the OneConfig three-HUD API contract passed against the embedded JAR; the pinned upstream patch applied successfully. The local launcher build was attempted after installing MSVC, but Windows application control blocked an unsigned Rust build script. The remote Windows build is used to produce the executable. Test Minecraft startup and native menus before distributing a compiled edition.

The downloaded PE was checked as Windows x64, its product metadata reads `bridgetapclient`, and the complete native BridgeTap payload was found byte-for-byte inside it. `dist/bridgetapclient-build.json` records the executable and payload SHA-256 values. `dist/bridgetapclient-windows-x64.zip` includes the executable, checksums, upstream notices and French setup instructions.

Verification-only run succeeded: https://github.com/sgggnnn/bridgetap/actions/runs/44899635639. The native preference test passed in the supported debug test profile. The executable remains unsigned; no usable user code-signing certificate is installed locally.


## Branding, native menus and mod versions

Final Windows build: [44928418621](https://github.com/sgggnnn/bridgetap/actions/runs/44928418621), integration commit `8bb8d288e8b015b4f6e67f5e8c831b4f18223d75`, embedded BridgeTap `1.10.0-dev.10`. All eight Python checks, seven Rust tests, full app type checking and the release build succeeded. The executable was verified as Windows x64 with the complete expected JAR embedded. It was installed and opened locally; the new wordmark, mod icon, version controls and existing profiles were visually checked. The launcher was replaced through normal shutdown while the running Minecraft process stayed alive. Restart Minecraft to load the new native menu adapter; the current game retains its previously loaded mod.

The launcher uses the `BridgeTapClient` name throughout its local UI strings, runtime translation tables, wordmark, window resources and generated shortcut names. Launcher, tray and platform icons are converted from the mod's existing `assets/bridgetap/icon.png`. Upstream API identifiers, existing game folders, settings namespaces, service links and copyright notices retain compatibility.

**BridgeTap** in the launcher now contains a searchable catalog of 30 tools grouped into Replays, Islands, Appearance, Recording, HUD, Settings and Help. The catalog, native OneConfig buttons, version controls and status/error messages are in English. Choose a compatible profile, join a world with the bundled mod, and click a tool. The request opens on Minecraft's client thread; the launcher does not inject commands or modify the server. The same catalog appears as native OneConfig buttons under **Right Shift > BridgeTap**. Replay-specific tools first open a replay picker with an action button; tools that require an island begin with the island library if none is active. A world-required screen explains the prerequisite when a native entry is used from Minecraft's main menu.

The launcher version controls support:

- **Check updates**: find a newer stable GitHub release, verify the release asset's SHA-256, validate the native 1.8.9 manifest, and select it for the next game launch.
- **Automatic updates**: explicitly enabled by the user, checked on game launch at most every six hours. An offline/failed check keeps the selected version; development builds are not downgraded to older stable releases.
- **Choose a JAR**: validate and copy a native BridgeTap OneClient JAR into the launcher's version cache. Choosing a local file pins that version and disables automatic updates, including an automatic download already in flight.
- **Bundled version**: reset to the embedded payload and disable automatic updates. Corrupt selection metadata is retained aside before resetting.

The selected version applies to all compatible BridgeTap profiles. Importing or downloading a version never replaces a JAR while Minecraft is running. Installation is performed under the launch lock on the next launch, and previous profile JARs remain in the existing backup directory. Recordings, islands, accounts and the `E:\OneClient` game folder setting are preserved. A manually selected JAR is copied, so moving the original download afterwards does not break the selection.

Source preparation applies `launcher.patch`, copies the overlay assets and native payload, generates the shared menu catalog/version assets, and runs `brand-launcher.py`. `generate-brand-assets.py` is only needed to regenerate the committed logo conversions (Pillow and fontTools). The CI runs visible-branding/catalog checks, full Rust type checking and core version-selection tests before producing the release executable.

## In-game identity and Windows icons

The integrated payload brands the Minecraft window as **BridgeTapClient | Minecraft 1.8.9** and supplies Windows window icons from the mod logo. Standalone builds retain their host client identity. Before launching a compatible enabled profile, the launcher rebrands PolyPlus display strings and the three main client wordmark SVGs. Java class/method/field names, descriptors, URLs, saved paths and upstream legal notices are retained. Shared UTF8 constants are copied and only CONSTANT_String references are remapped. The original PolyPlus JAR is retained in `.oneclient/bridgetap-branding-backups`; a marker makes repeated application idempotent. Signed JARs are refused without changing their contents.

Local validation parsed all 7,380 class files from the installed PolyPlus JAR and rebranded 36 classes; the resulting archive passed CRC checks. Native LWJGL window creation verified the new title and icon call. Restart Minecraft to load the changed mod and PolyPlus presentation.

Final English/BridgeTapClient build: https://github.com/sgggnnn/bridgetap/actions/runs/44935193306 (commit `f6193b044ec6864d717eafdb0ce67163d5c98caf`). Eight Python checks, ten Rust tests, app type checking and the release build passed. The Windows x64 executable contains the exact expected native mod payload and reports ProductName `BridgeTapClient`. It was installed as `dist/BridgeTapClient.exe` and opened locally; the header name, mod logo and existing profiles were visually verified. Further automated clicks failed with `SendInput GetLastError=87`, including after a fresh observation, so the English page itself was not visually checked in that session. The pinned Windows shortcut now points to the mod icon and uses the BridgeTapClient name. Existing `E:\OneClient` data settings are preserved. Restart Minecraft to load the new menu labels, PolyPlus branding and window icon.

## Discord profile activity identity

The launcher now sets the actual Rich Presence `Activity.name` to **BridgeTapClient**, the large image to the mod logo's pinned public image URL, the image tooltip to BridgeTapClient and the website button to the project. The existing public application ID remains compatible: no new Discord application, token or RPC companion is required. A live IPC test against the local Discord client returned `name: BridgeTapClient` and an accepted proxied external logo image. This uses the name override supported by discord-rich-presence 1.1.0 and the current [Discord Rich Presence API](https://docs.discord.com/developers/discord-social-sdk/development-guides/setting-rich-presence#setting-the-application-name).

Windows build: https://github.com/sgggnnn/bridgetap/actions/runs/44958903769, application commit `a74878421239cd35202641aaca903d5b223aa03b`, executable SHA-256 `d5b5fd8b8e37d831551f2b1a13200a278aa46ae5eae272c52939cdd4c349ea07`. The x64 PE and exact embedded BridgeTap payload were verified, packaged and installed. The old launcher was shut down through its `QUIT` IPC command; the already running Minecraft process remained alive. The notification addon is supplied as a separate single JAR (1.1.0), with its Windows library embedded and no helper application or service.
