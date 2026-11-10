# BridgeTap

A **Minecraft 1.8.9** client mod for analyzing your bridges: jump coordinates, run history, and replays with a free camera, a ghost player, and first-person playback.

[Download the latest version](https://github.com/sgggnnn/bridgetap/releases/latest) · [All releases](https://github.com/sgggnnn/bridgetap/releases) · [Changelog](CHANGELOG.md) · [Report a bug](https://github.com/sgggnnn/bridgetap/issues)

## Installation

1. Open [release 1.4.5](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.5).
2. Download the file matching your setup:
   - **Forge 1.8.9**: `BridgeTap-1.4.5-Forge-1.8.9.jar`.
   - **Ornithe / Legacy Fabric 1.8.9**: `BridgeTap-1.4.5.jar`, for an environment using Ornithe mappings.
3. Place the JAR in your instance's `mods` folder, replacing the previous BridgeTap version.
4. Launch Minecraft with **Java 8** and your chosen loader.

Keep only one BridgeTap edition installed per instance. The Forge edition bundles its Mixin runtime, so no additional Mixin loader is required. The Forge version used for testing is **11.15.1.2318**.

Download JARs from the **GitHub release assets**. The “Source code” archives contain the project sources for development.

## Features

- **Bridge HUD**: jump coordinates, deltas, ticks, turns, finish coordinates, and a Gain / No Gain indicator.
- **Customizable display**: position, scale, background, shadow, individual element colors, chat output, and a `LAST_TURN` mode showing only the latest jump and turn coordinates.
- **Run library**: recent history, saved runs, favorites, search, renaming, and sorting by date, time, or name.
- **Local-world replays**: recorded movement, jumps, rotations, sneaking, animations, skin, and block placements.
- **Per-tick analysis**: held, pressed, and released inputs, clicks, CPS, velocity, and ghost coordinates.
- **Free camera and POV**: fly around the bridge, inspect the white hitbox, or follow the recorded player's view.
- **Sharing**: export and import self-contained `.btreplay` files.
- **Bridge measurements**: the gap between platform edges and their height difference, when the recording contains the required data.

Use **Replay info: ON/OFF** to show or hide the replay information panel. The regular bridge coordinates HUD is independent of this setting. Choose the indicator wording with **No gain label**, and control its visibility with **Gain type**.

## Usage

### Open and save a replay

Press **B** to open the gallery. You can change the binding in Minecraft's controls. The **Settings** button opens the mod settings.

- Click a card to view its details, then **`>`** to play the replay.
- **+ Save** keeps a recent run.
- Right-click a card to add or remove a favorite. Favoriting a recent run also saves it.
- Use **Rename**, **Sort**, search, and filters to organize your recordings.

### Replay controls

Select a hotbar tool, then click to use it:

- **Slot 1 — backward stick**: move back one second during playback, or one tick while paused.
- **Slot 2 — Pause / Play**: pause or resume. At the end, the tool becomes **Replay** to start again.
- **Slot 3 — forward stick**: move forward one second during playback, or one tick while paused.
- **Slot 4 — clock**: cycle through `0.25x`, `0.5x`, `1x`, `2x`, and `4x` playback speed.
- **Slot 9 — barrier**: exit the replay.

The mouse wheel cycles through occupied slots. Use your Minecraft movement keys, **Jump** to move up, **Sneak** to move down, and **Sprint** to fly faster.

**Left-click the recorded player** to enter their POV. Press **Sneak** to return to the free camera. **B** or **Escape** also returns to the gallery.

### Share a replay with a friend

1. Select a run and click **Export**.
2. Find its `.btreplay` file in `config/bridgetap/exports/`; **Files** opens that folder.
3. Send the file to your friend.
4. Your friend places it in `config/bridgetap/imports/`, opens **Import**, clicks **Refresh**, and imports the file.

The mod does not send files automatically. Settings are stored in `config/bridgetap.properties`, and saved runs in `config/bridgetap/saved/`. You can copy this data between your Forge and Ornithe instances.

## Compatibility and validation

BridgeTap targets Minecraft **1.8.9**. Older recordings remain readable with the information they originally contained; missing metrics are not recreated. Accurate platform measurements require placement markers from recording format 4; otherwise, they may display `N/A`.

Version 1.4.5 fixes the Forge coordinates HUD and placed blocks remaining invisible during replays. Since 1.4.4, the missing arrival tick is also fixed: a **3.85-second** run correctly reaches **77 ticks**.

Validation covers ten regression programs, all 34 native hooks, and a real Forge/LaunchWrapper startup test. The startup test also reproduces the synthetic-chunk notification issue and verifies rendering updates. These checks do not replace visual testing in Minecraft with the other mods in your instance.

## Development

See [docs/BUILDING.md](docs/BUILDING.md) for build and test instructions, and [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

- `src/main/`: shared Java sources, mixins, and resources.
- `forge/`: Forge entry point, Mixin bootstrap, and mappings.
- `scripts/`: build scripts and the test runner.
- `tests/`: Java regressions and native hook checks.
- `docs/`: technical documentation and release notes.
- `dist/`: locally compiled JARs, ignored by Git.
- `build/`: caches, compiled classes, and temporary data, ignored by Git.

## Credits

Maintained at [sgggnnn/bridgetap](https://github.com/sgggnnn/bridgetap). Thanks to **pavelpuchika** for their help.

BridgeTap uses Minecraft Forge, Ornithe, Fabric Loader, Mixin, and their mapping tools. Licenses for bundled libraries are included in Forge JARs under `META-INF/licenses/`.
