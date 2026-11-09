pavelpuchika helped

# BridgeTap 1.2.2 — camera direction and ghost inspection

Minecraft 1.8.9 / Legacy Fabric with Ornithe intermediary mappings. Replace the previous BridgeTap JAR in mods with BridgeTap-1.2.2.jar, keeping one version installed. Restart Minecraft. Existing 1.2.0 recordings, saved runs, favourites and HUD settings remain compatible.

## Replay gallery

B opens the previous gallery interface: Recent / Saved, search, run cards, details and pagination. Select a card and use Play >, or click the card's small > button. + Save persists a recent run. Settings opens the existing HUD/chat/color controls. Favourites filters saved favourites; right-clicking a card toggles its favourite status and saves a recent run automatically when favouriting it. The gallery is a plain client screen and never installs a synthetic chest container on the live player.

## Native replay and free camera

The 1.2.0 replay engine is retained: a separate native Minecraft world, real block textures/models, a ghost with the recorded skin, name/rank, head/body rotations, sneak and animations, a permanent white debug hitbox, and timed physical block placements. Going backwards removes/restores the appropriate blocks. Profile skin layers and exact block metadata are preserved.

Fly using the configured Minecraft movement keys (including AZERTY/custom bindings), Jump to ascend, Sneak to descend and Sprint to move faster. Camera movement follows native Minecraft input at 20 client ticks per second, with frame interpolation. Normal flight is 12 blocks/second and Sprint flight is 36 blocks/second (twice the 1.2.1 speeds). Screen-left and screen-right now match Minecraft yaw in all four cardinal directions. It is independent of playback pause/speed and the recorded actor's position. Mouse motion changes the camera direction. Native input is captured before the live player's input is suppressed; only that player's input is suppressed. B or Esc exits to the gallery.

The private hotbar still provides:

- Slot 1: backward 0.05 seconds.
- Slot 2: Pause / Play.
- Slot 3: forward 0.05 seconds.
- Slot 4: playback speed (0.25x, 0.5x, 1x, 2x, 4x).
- Slot 5: restart.
- Slot 6: reset the camera near the ghost.
- Slot 7: pause at a recorded tick and place the free camera at the ghost viewpoint.
- Slot 9: exit.

Select with the configured number keys or mouse wheel; left- or right-click to activate. Status messages use the action bar. Replay information now sits above the hotbar, leaving the top-left keystroke display unobstructed.

## Corrections from 1.2.0

The replay controller's game mode is configured directly on its private instance. The native setGameMode setter is no longer used during construction because it changes the live Minecraft player's abilities. Render-scoped camera switches now update only the camera reference; they no longer call Minecraft.setCamera, which resets post-processing shaders. The live render context is restored in a JVM finally block.

Both the ghost and the camera are independent of the live player's spectator status. The camera's arm rotation is synchronized with its viewing direction. Hotbar selection follows Minecraft keybindings, and B is registered only in the general controls list, preserving the nine-slot hotbar binding array.

The replay renderer's worker pool is reused across sessions instead of allocating a new pool on every replay. Pending rendering tasks are settled and per-world chunk GPU buffers and entity references are released on exit. The live entity renderer's world is restored after detaching the replay world.

## Compatibility and limits

1.2.0 recordings open unchanged. 1.1.0 recordings contain only positions and block IDs, so missing pose/skin information uses defaults. Original summary-only saves remain readable but cannot become replays. New recordings capture nearby platforms and the bridge (initial horizontal radius 12 blocks, then 8; from 24 blocks below to 12 above). Other players, remote terrain and block-entity contents such as sign text are not recorded. Textures use the current resource pack; skins use the recorded profile and Minecraft skin loader/cache.

Limits are ten minutes, 48,000 block events and 400,000 scanned positions per attempt, with ten recent attempts in memory. Saved data and favourites are under config/bridgetap/saved. The live player remains in the server world with normal networking while the replay camera and scene are local. Finish or reset an active bridge attempt before starting playback.

## Build and verification

Compile with JDK 9+ targeting Java 8. BRIDGETAP_CLASSPATH must contain the Minecraft 1.8.9 intermediary client, Fabric Loader, Sponge Mixin, LWJGL 2, Mojang Authlib and Guava JARs (semicolon-separated on Windows). Run ./build.ps1 -JavaHome <JDK path>. python build-source.py is also supported. Dependencies are not bundled.

Six standalone Java regression programs pass on Java 8: ConfigRegression, HistoryRegression, ReplayRegression, DisplayRegression, ReplayTimelineRegression and ReplayCameraRegression. These cover persistence and formats, skin/rank and layers, block rewind, favourites, gain/colors, pause, independent free flight, yaw-relative movement, ascent/descent, sprint, diagonal speed and interpolation. Run from a temporary directory because configuration tests write test config files.

python tests/check_native_hooks.py <minecraft-intermediary.jar> <mod.jar> verifies all 32 native hooks/accessors and accessor types against the actual client bytecode, the render invocation and finally restoration. It also checks that live ability-changing and shader-resetting setters are absent, free flight is wired to client ticks, the replay entities override spectator status, and chunk buffers are released.

Compilation and automated checks pass. This correction was informed by the supplied gameplay video and native client bytecode, but has not been visually tested in a running Minecraft client here. The environment's previous OpenGL test was blocked by Windows Application Control; compatibility with the full rendering-mod stack still needs an in-game check.

## Inspecting coordinates in 1.2.2

Select hotbar slot 7 and click to pause at the current recorded tick and move the free camera to the ghost's exact recorded feet position and view direction. The camera remains free afterwards. Moving manually through the ghost is also supported. When the camera eye is within the ghost model, only that model is hidden; the white debug hitbox remains visible. The skin becomes visible again when the camera leaves. The Ghost XYZ line above the hotbar always reports the recorded actor position to four decimal places, independently of the free camera position shown by F3.

All existing recording formats remain compatible. The camera regression now explicitly checks left/right for yaw 0, 90, 180 and 270, the new speeds, and visibility while passing through the ghost. Six Java regression programs and all 32 native hook/accessor checks pass; in-game visual validation still requires a Minecraft run.
