# BridgeTap Client — OneClient integration

This is an unofficial Polyfrost integration included with BridgeTap 1.7.0. The in-game mod is shipped; the customized launcher remains source-only.

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
python integrations/oneclient/prepare-launcher.py --jar dist/BridgeTap-1.7.0-OneClient.jar --destination build/BridgeTapClient
cd build/BridgeTapClient
cargo build --locked --release -p oneclient_app
```

Rust 1.97+ and Windows Visual C++ Build Tools/Windows SDK are required on Windows. The executable is produced at `target/release/oneclient_app.exe`. It has **not** been built on this machine: Cargo stops because `link.exe` is missing. Rust syntax/format checks were run for the new modules, not a complete Rust type check.

The customized launcher adds a **BridgeTap** navigation page with per-profile activation. Installation happens after normal content materialization, under the existing launch directory lock, and only on 1.8.9 Ornithe profiles. Enable the normal OneConfig bundle before launching. Previous BridgeTap JARs are identified by manifest ID and moved to `.oneclient/bridgetap-backups`; they are not deleted. Configurations, recordings and island libraries are preserved. Disabling stops installing the embedded payload on subsequent launches; backups are retained for manual recovery.

The local edition has a distinct data directory, IPC endpoint, application identity and title. Upstream self-update and protocol-handler registration are disabled to keep it from replacing the fork or the official launcher. Existing OneClient accounts and profiles are not automatically copied; use the launcher's import flow.

## Validation and remaining checks

The Java mod regression suite and real menu preview suite passed. Contract tests check all three HUD wrappers against the actual installed OneConfig API (positions, scale, visibility, provider IDs, bounds and resize policy). The optional settings adapter compiles against that API. The build script also extracts compile-only dependencies from the installed Fabric Language Kotlin JAR (automatically found beside OneConfig, or supplied through `-KotlinJar`).

Remaining: build/type-check the full launcher once Windows build tools are available, and test native OneConfig registration, menu transitions and HUD dragging in a running Minecraft client. No claim of in-game visual verification is made.

Upstream: https://github.com/Polyfrost/OneLauncher and https://github.com/Polyfrost/OneConfig. The launcher retains upstream GPL-3.0 licensing and copyright notices. BridgeTap's adapter uses the installed OneConfig API and does not bundle OneConfig source or binaries.

## OneClient HUD style controls (dev.9)

Open **Edit HUD**, select **Bridge coordinates**, **Replay information** or **Replay keystrokes**, then use **Style** for uniform text color, background color/opacity and normal/full text shadow with a custom shadow color. The renderers and editor previews use the same saved RGB/ARGB values. The font-family/weight/case controls offered by OneClient are not implemented by BridgeTap's legacy renderer.

The HUD's **Settings** tab adds native color pickers for individual coordinate components (labels, coordinates, deltas, ticks, gain labels, header, background, border, Velo, positive/negative X-Z delta and jump angle). The replay keystrokes expose separate released/pressed text and background colors plus the active accent. Each HUD has a **Reset this overlay's style** action. Selecting an individual coordinate color restores component coloring if a uniform Style text color was previously applied.

Styles are stored locally in `config/bridgetap/overlay-style.properties`. RGB formatting is applied only when drawing the HUD; server chat and saved run summaries retain Minecraft's legacy formatting. Replay telemetry and keystrokes remain replay-only during gameplay. The original replay, island and preset menus remain active.

The native Style adapter was exercised with an actual OneConfig Hud instance in a headless contract test, including both directions of color/background/shadow synchronization. Visual behavior in a running game still needs user testing.
