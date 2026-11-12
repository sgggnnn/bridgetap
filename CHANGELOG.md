# Changelog

History of versions available in [GitHub Releases](https://github.com/sgggnnn/bridgetap/releases). Each release preserves its historical JARs alongside SHA-256 checksums. Forge editions start with version 1.4.4.

Some commits contain several intermediate versions. Those versions are tagged at the first commit archiving their binary, rather than at a dedicated source snapshot. The affected release notes explain this distinction.

## [1.7.5](https://github.com/sgggnnn/bridgetap/releases/tag/v1.7.5) - Axis delta and angle colors

- Adds independent positive/negative X-Z delta colors and a jump-angle color in the palette menu and native OneClient color pickers.
- Keeps these colors separate from speed deltas and tick text, with persistence and live preview.
- Includes the Windows update installation fix from local 1.7.4.

## 1.7.4 - Windows update installation (local build)

- Handles an authenticated parent socket reset as game exit, fixing Windows updates that downloaded correctly but failed during installation with Connection reset.
- Rejects resets before the parent handshake, retaining the current JAR.
- Reproduces both cases with real helper processes and abortive TCP closes.

## [1.7.3](https://github.com/sgggnnn/bridgetap/releases/tag/v1.7.3) - Jump details, default islands and confirmed updates

- Adds **Both axes + X-Z difference**, showing both sampled horizontal decimals and their signed difference, with independent previous-tick X/Z decimals when Velo is enabled.
- Moves the existing previous-tick Z decimal next to each jump coordinate when Velo is enabled, using `coordinate/velo` and retaining its independent color.
- Adds an optional viewing angle (yaw in degrees) captured at each actual jump, independent of the coordinate offset.
- Renames failed tap entries to Fail Tap/4b and shows the saved 9-tick and 10-tick coordinates separately when available.

## 1.7.3 - Jump details (local build)

- Moves the existing previous-tick Z decimal next to each jump coordinate when Velo is enabled, retaining its independent color.
- Adds an optional viewing angle (yaw in degrees) captured at each actual jump, independent of the coordinate offset.
- Renames failed tap entries to Fail Tap/4b and shows the saved 9-tick and 12-tick coordinates separately when available.

## 1.7.2 - Confirmed updates (local build)

- Checks the latest stable GitHub release once on startup, with a title-menu prompt before downloading.
- Selects the current edition and verifies SHA-256, size and mod/Minecraft metadata.
- Installs after the game exits, removes the old JAR from mods, and keeps a recovery backup.
- Offers same-profile OneClient restart where supported, or an explicit close/manual relaunch flow for other launchers.
- Adds manual update settings, an opt-out, installation logs and next-launch failure reporting. See [update behavior and limits](docs/UPDATES.md).

## 1.7.1 - Bundled default islands (local build)

- Includes Default, Halloween, Summer, Winter, Fall Island and Speedcubing scenery, each with start and finish islands.
- Opens the island library on a new Defaults tab, with normal placement/export and a Save a copy action for customization.
- Keeps bundled files separate from My islands and preserves existing local files. Default templates are protected from library rename/delete actions.

## [1.7.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.7.0) - OneClient styles, server presets and replay video

Adds native OneClient HUD colors including an independent Velo color, per-speed previous-tick Z decimals, per-server/shareable island presets, island undo/redo, replay MP4 exports with recorded keystrokes/hotbar/final time, fail analysis, McPlay modes and mode subfolders. Fixes replay block/input alignment and removes stick navigation cooldowns. Native clip executables move into a separate, explicit-install tools pack.

See the [full release notes](docs/releases/v1.7.0.md) for installation, compatibility, validation and remaining limits.

## [1.6.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.6.0) - Islands, clips and replay organization

Final release of dev.11. Adds island copying and one-click presets, independent start/finish placement, Focus mode, readable island exports, a Windows hardware H.264/audio buffer, replay folders and filters, protected saves and optional failed-run cleanup. Fixes preset drift and parallelizes conversion for 120 FPS capture.

See the [full release notes](docs/releases/v1.6.0.md) and [feature guide](docs/ISLANDS-AND-CLIPS.md).

### Development history

Development JARs were preserved without separate source snapshots. Tags for dev.1-dev.10 contain manifests and checksums only; their release assets are the original binaries. The final v1.6.0 tag contains the complete consolidated source.

- [1.6.0-dev.10 - Hardware clip buffer, presets, Focus and protected saves](docs/releases/v1.6.0-dev.10.md)
- [1.6.0-dev.9 - LWJGL GPU-fence compatibility](docs/releases/v1.6.0-dev.9.md)
- [1.6.0-dev.8 - Reliable island masking, connected panes and server folders](docs/releases/v1.6.0-dev.8.md)
- [1.6.0-dev.7 - Island rotation, capture recovery and server metadata](docs/releases/v1.6.0-dev.7.md)
- [1.6.0-dev.6 - Finish-island positioning and island renaming](docs/releases/v1.6.0-dev.6.md)
- [1.6.0-dev.5 - Async GPU capture and endpoint visibility fixes](docs/releases/v1.6.0-dev.5.md)
- [1.6.0-dev.4 - Visual island copying and file-manager replay browser](docs/releases/v1.6.0-dev.4.md)
- [1.6.0-dev.3 - Island previews and original-terrain replacement](docs/releases/v1.6.0-dev.3.md)
- [1.6.0-dev.2 - Higher-quality clips, Windows audio and replay-island copying](docs/releases/v1.6.0-dev.2.md)
- [1.6.0-dev.1 - Island scenery, silent clips and replay folders](docs/releases/v1.6.0-dev.1.md)

## [1.5.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.5.0) — Visual HUD studio, replay library and opening detection

### Added

- Drag-and-drop positioning, handle resizing, snapping and saved layout for three overlays.
- Replay-only directional/jump/LMB/RMB input overlay using recorded ticks.
- 3-tick 1.0 Flat and opening-only 5b/6b Late detection, with timing, axial distance, height and placement checks.
- Combined replay filters, multi-term search and two-way sorting by date, time, name and jump count.

### Redesigned

- Replay tabs, cards, grouped details, secondary tools and a primary PLAY REPLAY action.
- Categorized settings, consistent toggles/steppers, color swatches and live preview.

### Changed

- Five client ticks of cooldown after a completed attempt, excluding intervening placements from the finished replay.
- Jump and plate offsets default to zero; existing custom values are retained.
- The barrier is the only replay exit control; library keys and Escape no longer exit playback.

[Full release notes](docs/releases/v1.5.0.md).

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
