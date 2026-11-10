# Building and testing BridgeTap

Run these commands from the repository root. PowerShell examples use `;` as the classpath separator. On other systems, use `:` with the Python scripts.

## Requirements

- **JDK 9 or newer** to compile with `--release 8`; JDK 17 was used for validation.
- **Java 8** to run Minecraft 1.8.9 and verify test compatibility.
- Python 3 for the Forge build and bytecode checks.
- The Minecraft libraries listed below. Minecraft and build dependencies are not tracked in this repository.

Generated JARs are written to `dist/`. The scripts refuse to overwrite an existing file; remove that output or choose a different name with `--output` / `-Output` before rebuilding.

## Ornithe / Legacy Fabric 1.8.9

The shared edition uses **Ornithe intermediary mappings**, rather than the original Minecraft client names.

The classpath must include:

- The Minecraft 1.8.9 client remapped to Ornithe intermediary.
- Fabric Loader, Mixin, LWJGL 2, Authlib, and Guava.

Versions used locally: Fabric Loader 0.20.1, sponge-mixin 0.17.5 + Mixin 0.8.7, LWJGL 2.9.4 + legacyfabric.15, Authlib 1.5.21, and Guava 17.0.

```powershell
$env:JAVA_HOME = 'C:/path/to/jdk-17'
$env:BRIDGETAP_CLASSPATH = 'C:/libs/client-intermediary.jar;C:/libs/fabric-loader.jar;C:/libs/sponge-mixin.jar;C:/libs/lwjgl.jar;C:/libs/authlib.jar;C:/libs/guava.jar'
./scripts/build.ps1
```

Cross-platform Python alternative:

```powershell
python scripts/build-source.py --java-home $env:JAVA_HOME --classpath $env:BRIDGETAP_CLASSPATH
```

Output: `dist/BridgeTap-1.4.5.jar`.

## Forge 1.8.9

```powershell
python scripts/build-forge.py --java-home 'C:/path/to/jdk-17' --minecraft 'C:/libs/minecraft-1.8.9-client.jar' --classpath 'C:/libs/lwjgl.jar;C:/libs/authlib.jar;C:/libs/guava.jar'
```

`--minecraft` expects the **original Minecraft 1.8.9 client JAR** provided by your launcher. The script downloads Forge 11.15.1.2318, MCP mappings, and the required tools into `build/forge/`.

Shared sources are converted to SRG names, then compiled with the Forge entry point. Converted files are written to `build/forge/generated/`. Mixin and its private dependencies are bundled in the mod JAR; Minecraft and Forge are not.

Output: `dist/BridgeTap-1.4.5-Forge-1.8.9.jar`.

See [forge/README.md](../forge/README.md) for details of the port.

## Regression tests

The PowerShell runner creates an isolated working directory under `build/`, so it does not modify your Minecraft configuration.

```powershell
./scripts/test.ps1 -JavaHome 'C:/path/to/jdk-17' -RuntimeJavaHome 'C:/path/to/java-8' -Jar dist/BridgeTap-1.4.5.jar
./scripts/test.ps1 -JavaHome 'C:/path/to/jdk-17' -RuntimeJavaHome 'C:/path/to/java-8' -Jar dist/BridgeTap-1.4.5-Forge-1.8.9.jar
```

It compiles and runs ten programs covering configuration, history, replay persistence, display, timeline, camera, telemetry, geometry, library management, and controls.

### Native hooks

```powershell
python tests/check_native_hooks.py C:/libs/client-intermediary.jar dist/BridgeTap-1.4.5.jar
python tests/check_forge_hooks.py build/forge/minecraft-srg.jar dist/BridgeTap-1.4.5-Forge-1.8.9.jar
```

For Forge, run `scripts/build-forge.py` first to prepare the mappings and SRG client. These scripts verify injections and accessors, render-context restoration, block invalidation, and, on Forge, the HUD overlay event subscription.

### Forge startup without a game window

```powershell
python tests/run_forge_smoke.py --java 'C:/path/to/java-8/bin/java.exe' --javac 'C:/path/to/jdk-17/bin/javac.exe' --classpath '<complete Forge launcher libraries, separated by ;>'
```

This classpath must include Forge universal, LaunchWrapper, the original Minecraft client, and its regular libraries. The mod JAR is copied into an isolated game directory. The test checks all 13 mixins and 34 injected/accessor methods, replay class linkage, and rendering notifications for synthetic chunks.

It does not create a graphics context. Textures, visual rendering, and interactions must also be tested in Minecraft.

## Publishing

Build scripts do not publish anything. For a release, attach the JAR for each supported edition and its SHA-256 checksums to the GitHub release, then document the changes in the [changelog](../CHANGELOG.md). The main branch contains only sources, scripts, and documentation.
