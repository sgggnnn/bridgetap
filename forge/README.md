# BridgeTap for Forge 1.8.9

Install `BridgeTap-1.4.4-Forge-1.8.9.jar` in the `mods` directory of a Forge
1.8.9 client (tested with 11.15.1.2318 and Java 8). Keep one BridgeTap JAR in
that instance. No separate Mixin loader is required. The Ornithe/Fabric JAR is
a separate build and is not installed in Forge.

The same recording library, replay GUI, free camera, click-to-POV, skin,
time controls, speed clock, per-tick inputs/CPS/velocity, settings, names,
sorting and sharing are retained. Existing `config/bridgetap` recordings and
`config/bridgetap.properties` can be copied between the two instances.

Version 1.4.4 also captures the arrival sample that used to be excluded when a
successful run ended. A 3.85-second run now reaches tick 77. If a legacy replay
is exactly one tick shorter than its saved run time, playback holds its last
known pose for that missing interval. No legacy position or telemetry is
fabricated, and larger discrepancies/truncated recordings are not extended.

## Build

Use JDK 9+ targeting Java 8 and Python 3. The original shared sources remain in
`src/main/java`. The build converts their Ornithe identifiers to Forge runtime
SRG identifiers using the bundled Calamus mapping and Forge MCP mapping;
generated sources stay under `build/forge/generated`. It compiles a separate
Forge entry point and core plugin, without Fabric metadata or dependencies.

```powershell
python build-forge.py --java-home '<JDK directory>' --minecraft '<original Minecraft 1.8.9 client.jar>' --classpath '<LWJGL.jar;authlib.jar;guava.jar>'
```

Build dependencies are downloaded from Forge Maven and Maven Central into
`build/forge`. The JAR bundles Mixin 0.8.5 with private copies of ASM 9.2 and
Guava 21, preserving Forge's own ASM 5 / Guava 17. The two FML remapper adapter
classes deliberately retain references to Forge's ASM so their bridge works.
Third-party licenses are included under `META-INF/licenses`.

Mapping source: [Ornithe Calamus](https://github.com/OrnitheMC/calamus), official
1.8.9 client identifiers to intermediary; Forge runtime mapping is downloaded
from `de.oceanlabs.mcp:mcp:1.8.9:srg`.

## Validation

The ten shared Java regressions run against the Forge artifact on Java 8,
including the 3.85s endpoint and legacy one-tick compatibility cases.

```powershell
python tests/check_forge_hooks.py build/forge/minecraft-srg.jar BridgeTap-1.4.4-Forge-1.8.9.jar
python tests/run_forge_smoke.py --java '<Java 8 java.exe>' --javac '<JDK javac.exe>' --classpath '<Forge/Minecraft runtime libraries>'
```

The smoke test runs real Forge/LaunchWrapper discovery in an isolated temporary
game directory, applies all 13 Mixins, checks all 34 injected/accessor methods
and resolves the replay classes. It does not create a graphics context or
exercise a live world. Rendering and interaction still need an in-game check.
