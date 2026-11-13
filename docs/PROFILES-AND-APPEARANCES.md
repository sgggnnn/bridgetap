# Keybind profiles, microphone clips and modern appearances

Available in **BridgeTap 1.8.0**, for Minecraft 1.8.9.

## Replay storage and large libraries

Automatic server-folder classification no longer saves every recording to disk. By default, only the latest **10 recent runs** stay in memory; click **Save replay** or favorite/rename/import a replay to keep it permanently. Recent-only runs disappear when Minecraft closes.

Open **Replay files > Tools > Replay storage / auto-save**, or OneClient's Replay storage button. Optional auto-save can keep only successful runs or all runs, with a configurable limit of 25-500 automatic recordings (default 100). Excess automatic recordings are moved to an archive. Manual saves, favorites, imports and older recordings without an explicit unprotected flag are excluded.

For an existing oversized library, click **Archive older automatic replays**. This runs in the background and moves unprotected automatic metadata/recording pairs under `config/bridgetap/automatic-replay-archive`. Nothing is deleted. **Open archive** opens that directory. To restore after restarting, close the game and move a pair of `.properties` / `.btr` files back into `config/bridgetap/saved`, then use Refresh. A manual save of a just-archived run also restores and protects it during the same session.

Library metadata loads in the background and stays cached. Rendering does not rescan thousands of files, folder assignment is batched, and legacy mode indexing uses a small bounded queue instead of decoding the entire replay library at startup. Refresh explicitly reloads files changed outside the mod.

## Keybinds and profiles

Forge now registers BridgeTap shortcuts during pre-initialization, before Forge reloads game options. The shared late-registration fallback also restores BridgeTap's saved codes from the active game's `options.txt`, including mouse bindings and unbound shortcuts. It does not reload or reset other game options.

Open **BridgeTap Settings > Keybind profiles**, or the OneClient **Tools > Keybind profiles** button. Set up your Minecraft/mod controls, enter a profile name, and click **Save current as new**. Select a saved profile to apply, rename, replace its saved keys or delete it. Renaming preserves the original saved keybindings.

In **Minecraft Options > Controls > BridgeTap**, bind **Next keybind profile**. It starts unbound and cycles saved profiles in list order when no screen is open. Switching applies and saves game settings immediately; it does not generate timed input or gameplay actions. The cycle shortcut itself stays unchanged across profiles. Up to 50 profiles are stored locally.

## Microphone clips

Open **Video clips** and enable **Microphone** independently of **System audio**. Microphone capture defaults to OFF and uses the default Java/OS input device, subject to OS microphone permissions. Recording starts only with the video buffer ON, a world open and the game window active/visible. Closing the buffer, losing focus, exporting a replay video or exiting Minecraft stops microphone capture.

The microphone uses a bounded 65-second PCM ring. Saving a clip trims it to the video interval. Samples follow a continuous audio clock rather than each thread wake-up timestamp, avoiding packet-boundary gaps and overlaps that can make voices robotic. When both sound sources are available, FFmpeg mixes them during background export. The existing optional clip tools are still required for native audio/video encoding; no extra executable or microphone helper is downloaded. A missing input device is reported in the clip screen, and an unavailable source does not prevent exporting the other recorded source. System audio can include other applications using the default output. Replay-video exports remain silent.

## Placed blocks, held items and 26.2

**Tools > Block appearance** changes the client rendering of your own placed blocks. The selected construction block also appears in the hand and hotbar, while the actual inventory stack, item count, server block and collisions remain unchanged. Only locally initiated placement sounds are substituted; sounds made by other players are preserved.

Click **Download 26.2** once, or **Import local JAR** to select an existing Minecraft Java 26.2 client archive. BridgeTap downloads the official client resource archive, verifies its published SHA-1 and size (for downloads), and caches it under `config/bridgetap/modern-blocks`. Minecraft code from this archive is never loaded or executed, and no Mojang assets are distributed inside the mod. After the import, search for entries beginning with **modern:**. Applying an appearance reloads textures once.

The imported catalog lists **1,198 blocks** with **1,524 representative JSON model parts**. It uses a representative state, not an editor for every modern block-state combination. The legacy model parser/bakery accepts the imported geometry after normalization. **96 special/missing models or textures use a cube/stone approximation**. Block entities, modern render effects, biome tinting and translucent materials cannot be promised to match 26.2 in the 1.8.9 renderer. Modern placement samples are fetched lazily from Mojang's official 26.2 asset index, with SHA-1 verification, and cached locally. Wait for the original-sound status before testing a newly selected modern material. Until its samples are available, the old material fallback is used. The current optional clip-tools ZIP must replace the previous tools when installing 1.8.0 because the loopback sample-clock fix changes its helper checksum. Vanilla 1.8.9 choices use their native sound.

## Branding and Discord

The supplied PNG is the Fabric/Forge mod icon and native OneClient card icon. Dropdown arrows are drawn icons rather than a `v` text suffix.

The GitHub README links to [the Discord community](https://discord.gg/7gYxewvgjD). Published GitHub releases trigger an announcement containing the release additions/fixes and download link. The webhook is stored as the `DISCORD_UPDATE_WEBHOOK` Actions secret, never shipped inside the mod or committed to source. No announcement is sent just by building a local test JAR.

## Validation limits

Tests use synthetic microphone data and never open the user's recording device. They check WAV timing, lossless continuity under simulated recording-thread jitter and real FFmpeg mixing. Forge tests exercise native keybind restoration, model substitution without inventory mutation and local placement sound selection. Modern catalog tests read the verified 26.2 archive, resolve textures, and parse/bake model parts with the actual 1.8.9 classes. These are not a substitute for in-game visual and real microphone-device testing.

## Compatibility fixes

The Replay Tools menu is now a four-section screen: selected replay, islands/appearance, recordings, and settings/organization. OneClient uses a separate JSON configuration file instead of colliding with the `config/bridgetap` directory, and its title/icon constructor arguments are corrected. Imported texture aliases are resolved before the 1.8.9 model parser; held items also use the selected block-state geometry when a legacy item model is missing.
