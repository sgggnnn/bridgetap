# Building and testing BridgeTap

Run these commands from the repository root. PowerShell examples use `;` as the classpath separator. On other systems, use `:` with the Python scripts.

## Requirements

- **JDK 9 or newer** to compile with `--release 8`; JDK 17 was used for validation.
- **Java 8** to run Minecraft 1.8.9 and verify test compatibility.
- Python 3 for the Forge build and bytecode checks.
- The first build downloads JCodec 0.2.5 and SpecialSource from Maven Central, verifies pinned SHA-256 checksums, and bundles a private relocated video runtime. No separate codec install is required by players. See `third_party/jcodec-LICENSE.txt`.
- Windows builds also compile `native/Loopback.cs` with .NET Framework 4's C# compiler and bundle the pinned imageio-ffmpeg 0.6.0 Windows runtime. The wheel checksum is verified. See `third_party/ffmpeg-NOTICE.md` and the bundled GPLv3 license. Native tools are separate processes, extracted with checksum verification at runtime.
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

Output: `dist/BridgeTap-1.6.0.jar`.

## Forge 1.8.9

```powershell
python scripts/build-forge.py --java-home 'C:/path/to/jdk-17' --minecraft 'C:/libs/minecraft-1.8.9-client.jar' --classpath 'C:/libs/lwjgl.jar;C:/libs/authlib.jar;C:/libs/guava.jar'
```

`--minecraft` expects the **original Minecraft 1.8.9 client JAR** provided by your launcher. The script downloads Forge 11.15.1.2318, MCP mappings, and the required tools into `build/forge/`.

Shared sources are converted to SRG names, then compiled with the Forge entry point. Converted files are written to `build/forge/generated/`. Mixin and its private dependencies are bundled in the mod JAR; Minecraft and Forge are not.

Output: `dist/BridgeTap-1.6.0-Forge-1.8.9.jar`.

See [forge/README.md](../forge/README.md) for details of the port.

## Regression tests

The PowerShell runner creates an isolated working directory under `build/`, so it does not modify your Minecraft configuration.

```powershell
./scripts/test.ps1 -JavaHome 'C:/path/to/jdk-17' -RuntimeJavaHome 'C:/path/to/java-8' -Jar dist/BridgeTap-1.6.0.jar
./scripts/test.ps1 -JavaHome 'C:/path/to/jdk-17' -RuntimeJavaHome 'C:/path/to/java-8' -Jar dist/BridgeTap-1.6.0-Forge-1.8.9.jar
```

It compiles and runs thirteen programs covering configuration, history, replay persistence, display, timeline, camera, telemetry, geometry, library management, controls, opening detection, combined filtering, and HUD placement/input display.

### Native hooks

```powershell
python tests/check_native_hooks.py C:/libs/client-intermediary.jar dist/BridgeTap-1.6.0.jar
python tests/check_forge_hooks.py build/forge/minecraft-srg.jar dist/BridgeTap-1.6.0-Forge-1.8.9.jar
```

For Forge, run `scripts/build-forge.py` first to prepare the mappings and SRG client. These scripts verify injections and accessors, render-context restoration, block invalidation, and, on Forge, the HUD overlay event subscription.

### Forge startup without a game window

```powershell
python tests/run_forge_smoke.py --java 'C:/path/to/java-8/bin/java.exe' --javac 'C:/path/to/jdk-17/bin/javac.exe' --classpath '<complete Forge launcher libraries, separated by ;>'
```

This classpath must include Forge universal, LaunchWrapper, the original Minecraft client, and its regular libraries. The mod JAR is copied into an isolated game directory. The test checks the registered mixins and all 43 injected/accessor methods, replay class linkage, and rendering notifications for synthetic chunks.

It does not create a graphics context. Textures, visual rendering, and interactions must also be tested in Minecraft.

## Publishing

Build scripts do not publish anything. For a release, attach the JAR for each supported edition and its SHA-256 checksums to the GitHub release, then document the changes in the [changelog](../CHANGELOG.md). The main branch contains only sources, scripts, and documentation.

## Menu previews and interaction checks

Build the Ornithe edition first. The preview runner executes the real Java menu drawing paths using Minecraft's bitmap font and a headless drawing adapter. It also checks opening detection through the tracker, filter/sort click targets, swatches, and dragging.

```powershell
./scripts/preview-ui.ps1 -JavaHome 'C:/path/to/jdk-17' -RuntimeJavaHome 'C:/path/to/java-8' -Minecraft 'C:/libs/minecraft-1.8.9-client.jar' -Classpath '<Ornithe intermediary client and complete Minecraft runtime libraries, separated by ;>'
```

Use the full Minecraft runtime libraries, including Log4j and Guava, in addition to the Ornithe intermediary client. The original client JAR supplies the font texture to this local test; that texture is not bundled in the mod. PNG previews and isolated fixture data are written under `build/ui-preview-<id>/`. This does not test the live world, GL state, or other mods.

## Portable release build

Run `./gradlew build` with JDK 17 and Python 3. The Gradle Wrapper verifies its distribution; the build provisions JDK 21 for the optional OneConfig adapter. Override the Python executable with `-PpythonExecutable=/path/to/python` or the adapter JDK with `-PoneclientJavaHome=/path/to/jdk21` when needed. Linux packaging also requires a .NET SDK to compile the optional Windows audio helper.

Outputs in `build/libs`: both edition JARs and `BridgeTap-clip-tools-windows-x64.zip`. The executables are in the optional tools ZIP, never in either mod JAR. Run `scripts/test.ps1` on Windows for the regression suite; it installs the build's optional tools only into its isolated test directory.

The Modrinth workflow downloads the already-tested GitHub release assets and verifies their release checksums. It publishes Forge and Ornithe/Fabric as separate loader-specific versions. Configure `MODRINTH_TOKEN` (or the legacy `BRIDGERTAP` secret) and `MODRINTH_PROJECT_ID`; the latter is the non-secret project ID. Manual workflow dispatch can retry an existing release without creating another GitHub release. The optional clip-tools ZIP remains available from GitHub and is linked in the Modrinth changelog.

## Importing historical releases into Modrinth

The manually triggered **Import all GitHub releases to Modrinth** workflow migrates every published release JAR without rebuilding or modifying historical binaries. It verifies GitHub SHA-256 digests before upload and the Modrinth response's file size/SHA-512 afterward. Release notes and original GitHub release dates are included in each changelog; development previews stay in the beta channel. Newly imported historical versions are not featured.

`docs/modrinth-versions.json` records verified editions already present, including the existing 1.4.5 Ornithe build whose ZIP contents match despite different container timestamps. Preserve this ledger when retrying. Each workflow run uploads `modrinth-import-receipts`, including partial progress after errors; merge that JSON into the ledger before retrying interrupted imports. This avoids requiring private-project read scopes or duplicating uploads. The token needs version-creation permission for the target project.

Use `python scripts/publish-modrinth-history.py --project ueMpSLbl --dry-run` to review the plan, and `python tests/test_modrinth_history.py` to verify multipart binary preservation, server-response validation and historical edition planning.
