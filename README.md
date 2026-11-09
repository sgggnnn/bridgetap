pavelpuchika helped

# BridgeTap — redesigned history test build

For Minecraft 1.8.9 with Legacy Fabric. Put BridgeTap.jar in the instance's mods folder and keep only one BridgeTap version there. B opens the run browser (change the binding in Options > Controls > BridgeTap). Use Recent and Saved to switch lists; select a row for details; + Save keeps the selected recent summary in config/bridgetap/saved; Settings opens HUD controls, including HUD visibility and Hide F3. Use < and > for pages and the Up/Down keys to scroll long details.

Only the last ten unsaved summaries are kept in memory. Saved summaries survive restart. This records statistics and displayed entries, not player movements, video or replay data. The HUD toggle hides the display but does not stop tracking. The Fall timer uses an empirical two-tick correction; Head hitter and the game UI have not been tested in a running game. No independent security audit or project open-source license is provided.

## Source and build

All mod classes in BridgeTap.jar are compiled from src/main/java and src/main/resources. To rebuild with JDK 9+, set BRIDGETAP_CLASSPATH to colon-separated paths for the Minecraft 1.8.9 intermediary client JAR, Fabric Loader 0.20.1, Sponge Mixin 0.17.5+mixin.0.8.7 and LWJGL 2.9.4+legacyfabric.15; these dependencies are not included. Run python3 build-source.py to create BridgeTap-source-built.jar; it refuses to overwrite an existing file. The build targets Java 8 bytecode. tests/ contains the standalone persistence and settings tests.
