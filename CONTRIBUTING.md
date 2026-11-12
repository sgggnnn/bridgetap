# Contributing to BridgeTap

## Report an issue

Open an [issue](https://github.com/sgggnnn/bridgetap/issues) and include:

- Your BridgeTap version and edition: Forge or Ornithe.
- Your loader and Java versions, plus other mods affecting rendering or controls.
- Steps to reproduce the issue, the expected result, and the actual result.
- A relevant log excerpt, screenshot, or `.btreplay` file if it helps reproduce the problem.

## Propose a change

1. Create a branch from `main`.
2. Read the [build documentation](docs/BUILDING.md).
3. Edit the shared sources in `src/main/`. Files in `build/forge/generated/` are generated and should not be edited directly.
4. Build the affected editions and run the checks relevant to your change.
5. Open a pull request explaining the problem, the change, and how you verified it.

Keep the sources compatible with Java 8. Replay changes must preserve older recordings and restore the client context after rendering. For visual changes, describe what you also checked in Minecraft.

## Binaries and releases

Do not commit generated `.jar` files (the verified official `gradle/wrapper/gradle-wrapper.jar` is the sole build-bootstrap exception), `.class` files, build caches, or local Minecraft data. Build outputs belong in `dist/`, and caches in `build/`.

Published JARs are attached to GitHub releases with a `SHA256SUMS.txt` file. The [changelog](CHANGELOG.md) and notes in `docs/releases/` describe each version. Historical binaries are preserved in their original form.
