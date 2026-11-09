pavelpuchika helped

# BridgeTap 1.1.0 — saved 3D replays

Minecraft 1.8.9 / Legacy Fabric (Ornithe intermediary). Install BridgeTap-1.1.0.jar in the instance's mods folder, replacing the previous BridgeTap JAR. Keep one version installed.

Press B (configurable under Controls > BridgeTap) to open the replay gallery. Recent contains the last ten attempts. Select a card and use Play >, or its small > button. + Save persists the summary and replay together in config/bridgetap/saved; Saved lists recordings from earlier sessions. Search matches IDs, status, axis and jump descriptions. The All runs / Success button filters results.

New attempts record player positions at 20 Hz and blocks near the bridge, including placement timing. Playback provides pause/play, restart, a draggable timeline, 0.25x–4x speeds, an overview/follow camera, mouse drag to orbit, +/- zoom, left/right arrows to seek and Space to pause. Esc returns to the gallery. The viewport uses simplified colored cubes and a player marker, rather than Minecraft textures, skins, the complete map, or server replay functionality. Playback never changes the live world. Up to ten minutes and 24,000 block events are captured per attempt; partial recordings are marked. Old summary-only saves remain readable, but cannot be converted into replays.

Settings > Coordinates output switches between the overlay and local chat. Chat prints the complete coordinate list at the end of each attempt and hides the overlay. Messages are local to the client. The HUD visibility toggle still only controls the overlay, not tracking. Gain type consistently controls the Gain / No Gain label in every coordinate mode. Overlay colors opens independent choices for labels, coordinates, positive/negative deltas, ticks, Gain, No Gain, header, background and border. Colors use the sixteen Minecraft text colors. All settings persist in config/bridgetap.properties.

## Build

JDK 9+ is required for compilation; output targets Java 8. Set BRIDGETAP_CLASSPATH to the Minecraft 1.8.9 intermediary client, Fabric Loader, Sponge Mixin and LWJGL 2 JARs, separated by the platform classpath separator (semicolon on Windows). Dependencies are not bundled.

On Windows, run ./build.ps1 -JavaHome <JDK path>; it creates BridgeTap-1.1.0.jar and refuses to overwrite an existing output. Alternatively use python build-source.py to create BridgeTap-source-built.jar.

## Validation

ConfigRegression, HistoryRegression, ReplayRegression and DisplayRegression cover settings persistence, history, replay round trips, block timing, old saves, malformed replay input, gain spelling/visibility and independent colors. Run tests from a temporary working directory because settings tests write a test config. ReplayRenderSmoke is an optional offscreen OpenGL test and needs Minecraft dependencies plus the LWJGL native library.

The release compiles successfully and all four logic/persistence tests pass on Java 8. The graphical smoke test could not execute because Windows Application Control rejected the LWJGL DLL in this environment. This build has not been validated in a running Minecraft client.
