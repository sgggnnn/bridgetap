pavelpuchika helped

# BridgeTap 1.4.3 â€” replay speed and display options

Minecraft 1.8.9 / Legacy Fabric with Ornithe intermediary mappings. Replace the previous BridgeTap JAR in mods with BridgeTap-1.4.3.jar, keep only one BridgeTap version installed, and restart Minecraft. Existing saved recordings, names, favourites and sharing files remain compatible; no new run is required for these interface changes.

## Live coordinates and replay-only telemetry

The original bridge coordinate/jump HUD is restored in normal gameplay with its existing visibility, F3, display-mode, Gain / No Gain and color settings. The extra Player XYZ line above the hotbar is removed outside replays. The optional chat output for coordinate entries at the end of an attempt works again. The newer input, click/CPS, velocity and platform measurement panels remain exclusive to replays. The slot-2 replay book remains removed, and normal inventory actions outside replays are not intercepted. Recording continues in the background.

Open the replay gallery with B and click a card's > icon to watch it. Naming, sorting, favourites, search, import/export and saved history remain available.

## Replay items

The private replay inventory contains only:

- Slot 1: backward stick.
- Slot 2: Pause / Play, automatically replaced by Replay from beginning at the end.
- Slot 3: forward stick.
- Slot 4: clock to cycle playback speed (0.25x, 0.5x, 1x, 2x, 4x).
- Slot 9: barrier to exit.

The two direction controls are both Minecraft sticks. They skip one recorded second while playback is running and one exact tick while paused. The center item changes its label with the playback state; at the final sample it becomes a replay arrow. Clicking that arrow restarts at tick zero and restores Pause. Rewinding away from the end restores normal Play/Pause behavior.

Slots 4â€“8 are empty and cannot select extra tools. Mouse-wheel navigation cycles only through the four occupied slots. There are no separate speed, restart, camera-reset, inspection or POV items.

Right-click uses the selected control. Left-click also uses it unless the click targets the recorded player, in which case it enters POV (the barrier always exits). B or Esc also returns to the gallery.

## POV by clicking the player

Aim at the recorded player and left-click to follow their POV throughout the bridge. Targeting uses the private replay camera, the player's recorded collision box, a maximum reach of 64 blocks and a replay-world block raycast, so clicking through a wall does not enter POV. A click beside or behind the player does not activate it.

POV follows recorded position, rotations, sneak state, swings, held item and skin. Manual mouse rotation is suppressed while following; pause, step, skip and Replay still work. Press the configured Sneak key to return to an external free camera without adding another item. A Sneak key already held while entering POV must be released and pressed again to leave.

Free camera uses the configured movement keys, Jump/Sneak vertically and Sprint for faster flight. The existing corrected strafe directions and 12/36 blocks-per-second speeds remain. The ghost model is hidden around an internal camera to clear the view, while its white debug hitbox remains.

## Recorded information and library

The replay panel keeps the recording name/time/tick, XYZ, held/pressed/released movement and mouse inputs, per-tick physical clicks, trailing-one-second CPS, native velocity XYZ/horizontal magnitude, and available platform gap/height delta. Telemetry comes from the selected recorded sample. Old recordings show unavailable data rather than invented statistics.

The gallery offers Rename, Sort, Export, Import and Files. Names persist and appear in search. Sorting supports Newest, Oldest, Fastest, Slowest and Name A-Z. Right-clicking a card toggles its favourite status and saves a recent run when adding it.

Export creates a self-contained .btreplay file under config/bridgetap/exports; Files opens that directory. A friend using this mod places the file under config/bridgetap/imports, opens Import, clicks Refresh, then imports the listed file. Import preserves the recording and name while avoiding overwriting an existing ID. No messages or files are sent automatically.

Gap is measured between platform block faces and height is their signed surface-elevation difference. Precise platform boundaries require recordings with version-4 placement tags. Missing or unsupported platform boundaries display N/A. Versions 1â€“4 of .btr remain readable, with their original available data.

The local replay world and renderer remain separate from the live world. Render context is restored in a JVM finally block. No live ability changes or shader-resetting camera setter are used. Renderer workers are reused and per-world buffers are released on exit.

## Build and verification

Use JDK 9+ targeting Java 8. BRIDGETAP_CLASSPATH must contain the Minecraft intermediary client, Fabric Loader, Mixin, LWJGL 2, Authlib and Guava JARs (semicolon-separated on Windows). Run ./build.ps1 -JavaHome <JDK path>, or python build-source.py. Dependencies are not bundled.

Ten standalone Java regression programs pass on Java 8, including ReplayControlsRegression for the five-item inventory, center Pause/Play/Replay transitions, restart, occupied-slot scrolling and player-ray targeting. Earlier persistence, configuration, telemetry, geometry, camera, naming, sorting and archive tests remain passing.

python tests/check_native_hooks.py <minecraft-intermediary.jar> <mod.jar> validates all 34 remaining native hooks/accessors, render restoration, end-of-tick recording, the absence of the live shortcut, replay-tool initialization, replay-world target occlusion and continuous POV guards/skin delegation.

Compilation and automated checks pass. The reduced native hotbar, click-to-POV and interaction with the full Minecraft/rendering-mod stack still require an in-game visual check here.

Settings: Replay info toggles the entire replay information panel, including time, tick, Ghost XYZ, inputs, CPS, velocity, gap/height and hints. The preference persists and defaults to ON.

Display cycles through LAST_TURN to show only Last and Turn coordinates, without the header or other entries. Last is the latest recorded jump/tap; Turn appears once recorded. This also applies to coordinate chat output. The No gain label setting chooses No Gain (default) or No Gay for the gain indicator; the Gain type visibility toggle still applies.
