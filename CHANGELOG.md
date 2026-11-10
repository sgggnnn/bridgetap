# Changelog

History of versions available in [GitHub Releases](https://github.com/sgggnnn/bridgetap/releases). Each release preserves its historical JARs alongside SHA-256 checksums. Forge editions start with version 1.4.4.

Some commits contain several intermediate versions. Those versions are tagged at the first commit archiving their binary, rather than at a dedicated source snapshot. The affected release notes explain this distinction.

## [1.4.5](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.5) — Forge HUD and replay block rendering fixes

### Fixed

- Rendered the coordinates HUD through RenderGameOverlayEvent.Post on Forge, since GuiIngameForge bypasses the previously targeted vanilla method.
- Explicitly refreshed replay chunk geometry when a block appears or is removed, including when seeking backward.
- Fixed placed blocks remaining invisible in synthetic replay chunks.

### Validated

- Ten regression programs and all 34 native hooks/accessors.
- Real Forge/LaunchWrapper startup, HUD event subscription, and reproduction of the synthetic-chunk notification issue.
- The headless test does not replace visual verification in Minecraft.

[Full release notes and provenance](docs/releases/v1.4.5.md).

## [1.4.4](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.4) — First Forge edition and final-tick fix

### Added

- A Forge 1.8.9 edition, tested with Forge 11.15.1.2318 and Java 8.
- Forge/Mixin bootstrap and bundled private dependencies in the Forge JAR, retaining the existing features.

### Fixed

- Captured the arrival tick previously omitted when a run ended: a 3.85-second run now reaches 77 ticks.
- Compatibility for legacy recordings missing exactly that final tick: their last known pose is held until the saved run time, without inventing movement or telemetry.

### Archive notes

- The Forge HUD and invisible placed-block rendering issues were fixed in version 1.4.5.

[Full release notes and provenance](docs/releases/v1.4.4.md).

## [1.4.3](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.3) — Display options and playback speed

### Added

- A Replay info option to show or hide the entire replay information panel.
- A LAST_TURN mode showing only the latest jump and turn coordinates.
- A choice between No Gain and No Gay indicator labels, with visibility still controlled by Gain type.

### Changed

- Restored playback speed control with a clock in slot 4: 0.25x, 0.5x, 1x, 2x, and 4x.
- Removed Player XYZ outside replays while retaining regular bridge coordinates.

[Full release notes and provenance](docs/releases/v1.4.3.md).

## [1.4.2](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.2) — Live coordinate display restored

### Fixed

- Restored the regular bridge coordinates HUD during normal gameplay.
- Restored the Player XYZ line above the hotbar, without the detailed replay-only telemetry.
- Restored coordinate chat output at the end of an attempt.

[Full release notes and provenance](docs/releases/v1.4.2.md).

## [1.4.1](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.1) — Simplified hotbar and click-to-POV

### Changed

- Limited the replay hotbar to two seeking sticks, Pause / Play, and an exit barrier.
- Pause / Play becomes Replay at the end to restart from the beginning.
- Left-clicking the recorded player activates POV; Sneak returns to the free camera.
- Restricted the new telemetry information to replay playback.

### Removed

- The replay shortcut in the second hotbar slot during normal gameplay.
- Separate speed, restart, and camera-switching tools in this version.

[Full release notes and provenance](docs/releases/v1.4.1.md).

## [1.4.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.0) — Replay library, sharing, and continuous POV

### Added

- Persistent replay names, name search, and sorting by date, time, or name.
- Export and import of self-contained .btreplay files for sharing recordings.
- Platform-edge gap and height-difference measurements when recorded placement markers support them.
- Continuous POV following the recorded bridge movement and rotations.

### Retained

- Detailed per-tick telemetry, favorites, and support for older replay formats.

[Full release notes and provenance](docs/releases/v1.4.0.md).

## [1.3.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.3.0) — Per-tick inputs, clicks, and velocity

### Added

- Per-tick recording of held, pressed, and released keyboard and mouse inputs.
- Left/right/middle click counts, trailing-one-second CPS, and XYZ/horizontal velocity.
- A telemetry panel above the hotbar.
- A shortcut in the second hotbar slot to replay the last bridge after a run.

### Changed

- Seeking sticks now move by one second during playback and by one tick while paused.

[Full release notes and provenance](docs/releases/v1.3.0.md).

## [1.2.2](https://github.com/sgggnnn/bridgetap/releases/tag/v1.2.2) — Camera directions and ghost inspection

### Fixed

- Aligned left/right camera movement with Minecraft orientation.
- Increased free-flight speed to 12 blocks per second, or 36 while sprinting.
- Allowed the camera to enter the recorded player for position inspection, hiding the nearby model while keeping its hitbox visible.
- Synchronized camera rotations and respected custom Minecraft movement bindings.

### Changed

- Moved replay information above the hotbar.

[Full release notes and provenance](docs/releases/v1.2.2.md).

## [1.2.1](https://github.com/sgggnnn/bridgetap/releases/tag/v1.2.1) — Independent free camera and gallery restoration

### Fixed

- Restored the previous gallery interface while retaining the native replay engine.
- Made the replay camera independent of the live player and controllable with Minecraft keybindings during playback or pause.
- Separated the replay render context and controller, restoring the live client after rendering.
- Reused renderer resources and cleaned up replay-world buffers.

[Full release notes and provenance](docs/releases/v1.2.1.md).

## [1.2.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.2.0) — Native Minecraft replay playback

### Added

- Replay playback in a local Minecraft world using native block textures and models.
- A ghost using the recorded player name, skin, rotations, sneak state, and animations, with a visible white hitbox.
- Timed block placement reconstruction, including removing and restoring blocks when seeking backward.
- Hotbar controls for playback, pause, seeking, and speed, with Action Bar status messages.
- Gallery favorites.

### Archive notes

- Free-camera movement fixes were introduced in versions 1.2.1 and 1.2.2.

[Full release notes and provenance](docs/releases/v1.2.0.md).

## [1.1.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.1.0) — First replays and coordinate customization

### Added

- Movement and block recording, replays saved with runs, and an initial 3D replay viewer.
- Gallery with search, run cards, and run details.
- Choice between an on-screen coordinate overlay and coordinate messages in chat at the end of a run.
- Individual color settings for HUD elements.

### Fixed

- No Gain wording and consistent application of the gain-type visibility setting.

[Full release notes and provenance](docs/releases/v1.1.0.md).

## [1.0.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.0.0) — First bridge HUD and run history

### Added

- Bridge and jump tracking: coordinates, deltas, ticks, timing, and turn detection.
- Configurable HUD position, scale, background, shadow, display modes, and visibility while F3 is open.
- Recent run history and saved run summaries.
- Jump and arrival tick offsets, plus a Gain / No Gain indicator.

[Full release notes and provenance](docs/releases/v1.0.0.md).
