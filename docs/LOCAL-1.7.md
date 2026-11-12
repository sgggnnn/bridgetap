> Development archive: this work is now consolidated in [release 1.7.0](releases/v1.7.0.md). Earlier local-only status notes below describe development history.

# Local testing build: 1.7.0-dev.4

This build is local only. Do not publish, commit or push it until explicitly requested. Replace the previous mod JAR with the matching Forge or Ornithe edition and restart Minecraft.

## Automatic island presets by server

Open **B > Tools > Island library > Presets** while connected to a server. Click **Auto here** beside the desired preset. The header identifies the server being linked. **Unlink server** disables that association. One server can have one automatic preset; different non-default ports remain separate. Singleplayer has a shared Singleplayer binding.

Stand on your start island and click **Set start island** once. This stores a local start anchor; beginning a recorded bridge also identifies its start. Presets always use this known start, even when the spectator stands on the finish. If the start is missing, unloaded or in another lane, placement is refused instead of falling back to the nearest island.

On the next world connection, BridgeTap waits for a known start and loaded native terrain, then applies the preset once. It does not apply while a replay, bridge attempt or menu is active, and manual placement cancels the pending automatic action. Missing preset files are ignored safely. Bindings are stored in `config/bridgetap/preset-servers.properties`. Linking also snapshots the current placed-block appearance, which is restored with the server preset. Change your appearance first, then link/re-link to save a new combination. Server bindings and their appearance snapshots stay local when sharing a preset.

The association is per server, not per map. A server using multiple different maps may require manual positioning or different presets. An existing recording from a different server is not used as the replacement scene.

## Share island presets

Click **Export** beside a preset to create a named `.btpreset` archive and open its export folder. Send that file to a friend. They open **Presets > Import > Open imports folder**, put the file there, click **Refresh**, then **Import**. The archive includes a detached copy of both islands, their relative positions, orientation and Focus setting. It contains no server bindings. Duplicate names receive a numeric suffix. Imports validate the two expected entries and bound their sizes; rejected imports do not create a saved preset.

## Replay video export

Select a playable recording and open **Tools > Export replay video**, or use **Details > Export video**. Choose **Player POV**, **Follow camera** or **Fixed overview**, 30/60/120 FPS and resolution, then click **EXPORT MP4**.

Export defaults to up to **1080p**, recorded **Keystrokes + CPS**, a compact run title/timer and a clean Minecraft HUD. Keystrokes use your saved replay-input HUD position/scale and ignore its normal visibility toggle. Coordinates/velocity are a separate option. No spectator inputs or playback instructions are shown in the video. Older recordings honestly show that input data is unavailable. Preferences are remembered independently from the live clip settings.

A nine-slot **Block stack hotbar** shows a 64-stack of the recorded construction block (including its variant or active appearance substitution), rather than replay control tools. The stack is a display quantity, not an inferred inventory count. With no recorded placement, the slot remains empty.

**Final time (2 seconds)** adds a frozen ending with a large green/yellow `Time: 3.950` result using the saved run time to three decimals. The ending does not advance replay time. Both the hotbar and ending are enabled by default and can be toggled separately.

A live progress bar is drawn after each captured frame, so it is visible in the game window but absent from the MP4. Camera bobbing is suppressed during export, and original HUD/bobbing settings are restored on completion or cancellation. Native Windows replay video encoding uses a higher quality setting than the live clip buffer. Output filenames include the replay name.

Keep the game window active and unchanged until export completes. The replay advances by fixed video samples, not wall-clock time; encoder backpressure waits rather than dropping output frames. The final recorded sample is included, adding at most one frame of endpoint padding. Initial and tick-change settling frames allow native replay geometry to update. Rendering can take longer than playback.

**Escape** or opening another menu cancels the video export and keeps the replay. Window focus loss, resizing or leaving the replay also cancels safely. Original replay time, playback state and camera are restored after completion/cancellation. After success the export menu returns; **Open exported videos** opens `bridgetap-clips/replays/`.

Exports are **silent** because replay recordings contain no audio track. The live clip buffer is cleared and suspended during export, then resumes if it was enabled. Its FPS/quality/audio preferences are retained. Windows uses bundled FFmpeg with a supported hardware encoder or CPU fallback; other systems use the privately bundled Java encoder. Memory holds at most a small bounded number of raw frames, not the full replay.

Automated tests verify frame planning, exact endpoint inclusion, encoding/decoding and cancellation cleanup. OpenGL video rendering, lighting, chunks and compatibility with other graphics mods still need validation in a real Minecraft session.

## Fail analysis

Select a recording and open **Tools > Analyze fail**, or **Details > Analyze fail**. The analysis reports confirmed placements, right-click samples without an observed placement in a short following window, and recorded downward/sideways motion near the end. Click a tick to open the replay paused at that sample.

Findings are observed clues, not a proven reason for the failure. A click may be rejected or delayed by the server; missing/truncated samples and old recordings cannot supply facts that were never recorded. Successes are not diagnosed as failures.

## Island editing

The placement and region-copy screens skip LegacyGuiBlur background blur/gradients when that optional mod is installed, leaving the world clear without disabling unrelated game shaders.

The placement editor has **Undo** and **Redo** buttons for position and rotation edits. Dragging in the world preview moves the selected whole scene, start island or finish island. One continuous drag is one undo operation; a new change after Undo replaces the abandoned redo branch. The last 100 operations are retained. Preview cancellation still restores the original scene.

## Validation

Checks cover persisted server bindings, address normalization, port isolation, undo/redo grouping and bounds, conservative fail evidence, deterministic video endpoints, native/Java MP4 writing, and cancellation cleanup. Native Forge startup also exercises real island position/rotation undo and resolves the new screens/classes. Menu previews render the actual screen drawing paths with fixture recordings. These checks do not replace testing a server join and a full OpenGL replay export in-game.

## Server modes in replays

McPlayHD distance rules apply only to `mcplayhd.net` and its subdomains: **16b = Extra Short**, **28b = Short**, **51b = Normal**, **83b = Long**. Height is not part of these rules, including Short. Measurements use the actual native platform-edge gap, not player travel, copied scenery or number of placements. Other servers, missing platform evidence and unrecognized distances remain **Unknown**. Partial failures may not contain the finish edge needed for detection.

New recordings save measured mode evidence with their metadata; portable replay exports/imports retain it. Older files without this metadata are measured in a background worker while the library is open, without changing their original files or names.

The replay details and playback info show the mode. In the file manager, **Mode: All modes / Extra Short / Short / Normal / Long / Unknown mode** combines with the selected folder, search, success/failure and import filters. The mode filter is remembered across launches.

## Local OneClient integration

See [OneClient integration](../integrations/oneclient/README.md) for the optional OneConfig menu/HUD adapter and the pinned customized-launcher sources. McPlay server folders now contain mode subfolders; opening the server folder includes every child mode. Existing direct server-folder recordings are sorted when their background mode detection finishes. Personal folder assignments are preserved. Mode metadata from the previous development build is remeasured to clear stale Unknown classifications.

### OneClient dev.6

The native OneClient integration now provides Replays and Islands pages rather than opening the legacy libraries. Native controls cover browsing, filters, folders, recording metadata/actions, island files, presets and replay-video settings. World copy/placement views remain transparent, with return callbacks into OneClient. The author shown in client metadata is `sggn`.

### OneClient dev.7

Fixed `Cannot overwrite a tree with a non-tree node!` when opening Replays, Islands or Presets. Native pages now use reserved library IDs instead of the IDs of the annotated action buttons. The regression fixture includes the real action-node names, reproduces the reported merge error and verifies repeated navigation without replacing persistent buttons.

### OneClient dev.8

Restored the original replay file manager, island library and preset menus from both the OneConfig buttons and the B key. Removed the experimental native library implementation. The `sggn` author label, OneConfig settings and all three shared HUD-editor integrations remain. Existing replay folders, filters, recordings, island files and presets are unchanged.

### OneClient dev.9

Adds native HUD Style bindings for text/background/shadow colors and alpha, plus independent coordinate-component and keystroke-state color pickers in HUD Settings. RGB styling uses a render-only formatter; chat and saved summaries retain legacy colors. Overlay colors opens the OneClient HUD editor. Original replay/island/preset menus remain restored.

### OneClient / Forge dev.10

Adds an opt-in live pre-jump velocity line, sampled from the previous completed client tick and independent of coordinate offsets. Adds a verified Gradle 8.14.3 Wrapper and a portable release build producing both editions in `build/libs`, matching the Modrinth workflow file glob. Gradle provisions JDK 21 for the optional OneConfig adapter; Linux packaging cross-compiles the Windows audio helper using a .NET SDK and pinned Microsoft reference/compiler packages.

The same development build also removes the 120 ms stick navigation gate, retaining edge/debounce handling for pause/speed/exit. Replay v5 stores tick-boundary poses and in-tick input/placement events on one clock; the old initial pose is no longer replaced by the first completed tick. For tagged v4 recordings, playback advances placement/input events by one tick to match their post-tick pose origin. Source event timestamps and the v4 phase marker survive friend exports.

### OneClient / Forge dev.11

The optional **Velo Z decimals (jump -1t)** setting now appends the fractional absolute Z coordinate from the tick before each jump to its flat/speed entry, instead of showing motion vectors. Values use the same three-decimal convention as jump coordinates: Z -23.456 becomes `.456`. The tick-start position is retained before the jump and is independent of the jump-coordinate offset. Every entry retains its own sample after the run ends. Previously saved summaries cannot supply missing samples.

The JARs no longer embed or extract FFmpeg or the Windows loopback executable. Native clips/audio and Windows replay-video encoding require the optional `BridgeTap-clip-tools-windows-x64.zip`, generated in `build/libs` by the build. Extract it into the **Minecraft game directory** (the directory containing `mods`), producing `config/bridgetap/tools/ffmpeg.exe` and `config/bridgetap/tools/bridgetap-loopback.exe`. Restart the buffer afterward. Existing verified tools extracted by earlier builds remain supported. Tools must match the SHA-256 values pinned in the mod; modified files are rejected. Nothing is automatically downloaded or installed at runtime.

This separates native capture dependencies from the Java mod and makes installation explicit. It does **not** establish the cause of a particular antivirus report or guarantee that the optional tools will not be flagged. Obtain the antivirus name, detection name and flagged path before reporting a suspected false positive to that vendor. Do not disable protection or add exclusions as a workaround.
