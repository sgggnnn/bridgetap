# Islands, clips and replay organization - BridgeTap 1.6.0

This build adds island scenery, high-quality video clips with Windows output audio, and replay folders. Version 1.6.0 is the final release of the dev.11 code. Earlier development builds are archived as historical pre-releases. Install one matching JAR from `dist/`, replacing the previous BridgeTap JAR in your Minecraft instance.

Changes in dev.5: asynchronous GPU readback on OpenGL 3.2, faster lossless capture compression, fewer frame copies, bounded capture workers, two H.264 export threads, and measured capture FPS. Island replacement now hides both original endpoints for single-island templates and retains valid start-island detection when finish terrain is unavailable or the camera is far away.

Changes in dev.6: independently position the finish island in paired scenery and rename saved islands in the library.

Changes in dev.7: keep the video buffer ON while automatically falling back from incompatible GPU capture APIs, record the original server address in replay descriptions, and rotate island scenery. New replays retain the server address through saves, renames and friend exports/imports. Singleplayer recordings are labelled **Singleplayer**; older recordings show **Unknown (older recording)** instead of the currently connected server.

Changes in dev.8: refresh the original-island mask after moving/rotating scenery, cover disconnected and late-loaded island blocks, resolve glass-pane/fence connections against the copied blocks, expand full bridge coordinates with **Details**, and automatically save/file new recordings by server.

Changes in dev.9: resolve GPU fence calls using the active runtime API. Standard LWJGL 2 uses GLSync objects; LWJGL 3 and client adapters use 64-bit handles. Supporting both avoids the NoSuchMethodError that sent 60/120 FPS capture into the slower compatibility mode. Restart Minecraft after replacing the JAR, then select the desired rate in Video clips. The capture status should say **Async GPU readback**; measured captured FPS still depends on game FPS and compression throughput.

Changes in dev.10: Windows clips use a continuous hardware H.264 rolling disk buffer instead of per-frame PNGs. Manual replay saves are protected from optional 48-hour failed-run cleanup. Added placed-block appearance selection, Focus mode, separate start/finish positioning, and larger v4 island files with explicit island membership.

Changes in dev.11: presets use a fixed reference on the original start island instead of the camera supporting block. New presets keep exact voxel positions when the camera or run start moves by one block. Legacy presets stay readable; position the scene correctly and save a new preset once to use the fixed anchor. Windows 120 FPS capture uses four conversion workers to remove the previous single-worker bottleneck; 60 FPS retains its existing path. Focus mode filters remote spatial sounds while preserving local interactions and interface audio. Island exports use readable sanitized names, with numbered duplicates instead of overwriting earlier exports. Opening the island library also renames older UUID-named exports from their embedded island names; saved/imported library paths remain unchanged.

Named island presets restore a layout in one click. The replay file manager also supports success/failure and imported-recording filters.

## Island scenery

Open **B > Tools > Island scenery**, or /bt island.

- **My islands** shows saved islands with a top-view preview. Click **Place island** to open the transparent world placement editor. Each island has its own **Delete** button with a confirmation; cancelling keeps the file.
- **From replay** uses the selected recording (or current active replay). **Copy both islands** saves the start and finish together, preserving their gap and height difference. It excludes recorded bridge placements. The saved copy appears in **My islands**; choose **Place island** when ready.
- **Select blocks > Select an island in the world** opens a transparent copy editor with a visible selection box. Drag to move it, scroll for distance, resize with the width/height/depth controls, or use **Pick two corners**. Right-drag to look around, then click **Save island**. Copying saves the visible static decoration without placing it automatically. /bt island pos1, /bt island pos2 and /bt island copy My island remain available.
- **Import** reads .btisland files from config/bridgetap/islands/imports/. Import selects the new copy automatically. **Export** writes shareable files into config/bridgetap/islands/exports/.

### Position and replace an island

When an existing island is found, the preview attaches to it and **Hide old blocks** starts ON. Both recorded endpoint islands are hidden, even for a single-island template. Recorded endpoint positions allow detection while the replay camera is far away; a missing finish island no longer discards the start island. Live-world detection expands recorded fragments to the connected terrain. The old terrain is hidden through the client terrain renderer, so it does not show through the new decoration. **Replace current island** reattaches the preview to that location. If detection is unavailable, the panel reports it and offers **Detect original islands** to retry once terrain is loaded. Turn **Hide old blocks** OFF to keep the original terrain visible.

Drag in the world to move the island horizontally or vertically; use the mouse wheel for distance. The panel also provides **Left / Right** and **Nearer / Farther**; the up/down arrow keys move vertically. Press **Place island** or Enter to confirm after the preview has rendered. **Cancel** or Escape restores the previous decoration and original terrain. **Remove placed island** removes the decoration and restores hidden blocks. Removing a saved template also removes its currently placed decoration.

For a template made with **Copy both islands**, choose **Move: Start island** or **Move: Finish island** to position each decoration independently with dragging, scrolling or the direction buttons. **Gap** adjusts the empty distance between the two scenery bounding boxes along the bridge axis; type a distance directly or use minus/plus. **Height** changes the selected island's vertical offset; **Scene Y** moves the whole scene vertically. **Reset part** resets the selected part (or both offsets in whole-scene mode). The original platform mask remains in place while the decoration moves. These adjustments affect the current placement, without changing the saved template. Each island uses a separate cached mesh, so adjusting the finish does not rebuild its blocks. Single-island templates keep whole-scene movement; copy both endpoints to enable independent finish controls.

To rename a saved island, select it under **My islands**, click **Rename**, enter the new name and click **Save name**. Renaming preserves the file, blocks, attachment point and paired-island metadata.

### One-click island presets

While positioning scenery, click **Save preset**, enter a name and save. This records the template, relative placement, independent start/finish offsets, orientation and Focus mode. In **Island library > Presets**, click **Apply** beside a preset to place and confirm it immediately near the current island. New presets use a fixed reference on the original start island; camera movement does not shift them. Rows marked **legacy - resave** use the old anchor: position the scene correctly and save a new preset once. Each preset owns a template copy, so deleting or renaming the original library island does not break it. **Save current layout** also saves an already placed scene. Presets are stored in `config/bridgetap/island-presets/`; deleting a preset removes its own copy only.

In the placement editor, **Rotate** cycles through 0/90/180/270 degrees around the start attachment block. Both islands rotate together; independent finish spacing and height are retained. Rotation is a current-placement adjustment, and cancelling restores the previous orientation. It reuses the cached meshes.

Only rendering changes: server blocks and their collisions stay intact. Replacement excludes recorded bridge placements when available. Masking covers vanilla render caches and the optional Argentum section snapshots; a terrain rebuild clears previously uploaded meshes. A saved replay can only supply the terrain captured in its recording; uncaptured chunks cannot be reconstructed. Static baked blocks and metadata are supported; liquids and special block-entity rendering are excluded. Version 4 island files preserve explicit start/finish membership and support up to 131072 static blocks, 512 blocks horizontally and 1048576 cells. Versions 1-3 can still be imported.

Replacement coverage updates after positioning settles and once more on confirmation. Separate per-island volumes cover disconnected decorations and cells that arrive after chunk loading; recorded and new bridge placements remain visible. Glass panes and other neighbour-dependent models resolve their native connection properties using the copied island, avoiding the default cross-shaped pane model.

## Video clips and audio

1. Open **B > Tools > Video clips**, or enter `/bt clips`.
2. Set **Video buffer** to **ON**, then return to gameplay.
3. Press **F9** to export the recent video. A chat notification confirms the export start and the finished filename. You can also click **Save recent video** in the clip menu.
4. Open **Open clips folder** to find the MP4 in your instance's `bridgetap-clips/` directory. Share that MP4 as a normal video file.

Defaults: a **30-second buffer**, **15 FPS**, up to **1280 x 720**, preserving aspect ratio. Choose 360p/720p/1080p, 15/30/60 seconds and 10/15/20/30/60/120 FPS in the menu.

On Windows x64 the buffer sends RGB frames directly to a persistent native H.264 encoder. It automatically tries NVIDIA NVENC, Intel QSV and AMD AMF, then falls back to CPU/libx264 ultrafast. The menu names the active encoder and reports measured captured FPS. A bounded mailbox holds at most three raw frames; no PNG compression runs on the game capture path. One-second encoded segments rotate under `config/bridgetap/clip-buffer/` with a **2 GiB disk cap**. Old segments expire by duration and size. Normal buffer shutdown clears its temporary segments. Hardware encoders use quality 16; CPU uses CRF 16. Export copies the encoded video into MP4 without another video compression pass. Clips start on a one-second keyframe boundary, so they may include less than one extra second at the beginning. The most recent captured frames are retained at the end. In very complex scenes the disk cap can shorten the retained interval, which is shown in the menu.

Other platforms retain the older lossless PNG / 128 MiB memory buffer and Java MP4 export, without output audio. Higher rates can shorten that compatibility buffer.

On Windows x64, **System audio** defaults to ON. A small bundled WASAPI helper records the default Windows output; it includes Minecraft and any other applications playing on that output, but never the microphone. Use the same output device for Minecraft and Windows. Audio is synchronized to the exported frame interval and encoded as AAC at 192 kb/s. If Windows audio capture is unavailable, a chat notification explains that the clip is being saved silently. Turn System audio OFF and ON to retry after changing output devices.

The buffer starts **OFF** each launch. Duration, frame rate, resolution and audio preferences persist. Changing settings, resizing the game window, losing window focus or changing worlds clears buffered frames. Output audio capture stops when the buffer is OFF, the window loses focus or no game world is open. No desktop video capture or OBS installation is required. Windows tools are bundled and extracted into `config/bridgetap/runtime/`; no separate FFmpeg installation is needed. Windows exports remux H.264 and encode AAC in a background worker. Other platforms retain the Java MP4 encoder without audio. Wait for the saved notification before closing Minecraft. At most one MP4 export runs at a time.

If an optional async GPU or framebuffer API fails, capture automatically switches to a compatible method while the buffer stays ON. Compatibility capture may run at a lower actual FPS, shown in the clip menu. Complete exception details are written to `config/bridgetap/clip-capture.log`. Only failure of the final compatibility path stops the buffer and displays an error.

OpenGL 3.2 hardware uses three asynchronous pixel-transfer buffers with GPU fences. Large RGB frame arrays are reused through a bounded pool to reduce allocation and garbage-collection stalls. The render thread polls completed transfers without waiting; busy transfers or the bounded encoder mailbox cause captures to be skipped instead of building a queue. OpenGL 3.0 uses synchronous downscaled readback capped at 30 FPS and an adaptive capture interval to limit render-thread cost. The older compatibility path reads the full window and is capped at 5 captured FPS and a 3840 x 2160 input. The selected FPS is a target: actual captures depend on the game's frame rate, GPU readback and encoder throughput. The clip menu shows measured captured FPS alongside the target. Export resamples recorded timestamps so dropped capture frames hold their image rather than speeding up the video. 120 FPS additionally requires the game to render at least 120 FPS; disable VSync and raise the in-game FPS cap if needed. Actual GPU performance still needs in-game testing.

## Replay folders

The file manager is now the default replay library opened by **B**, including during playback and after exiting with the barrier.

- The left sidebar contains **All replays**, **Saved replays**, **Unfiled** and custom folders.
- The middle table shows the selected folder's recordings. Search filters this table; clicking **Name**, **Time** or **Date** changes sorting.
- The status menu beside Search offers **All runs**, **Successful only** and **Failed only**. It combines with folder selection and search, and remembers the last choice. The source menu offers **All sources**, **Imported only** and **Recorded here**. Import origin is retained for new imports when saved or renamed; older recordings without that metadata cannot be identified retroactively.
- The right panel shows the selected replay's details, **PLAY REPLAY**, **Rename replay** and **Move to folder**.
- **Details** expands the saved bridge coordinates and full jump breakdown in a scrollable screen without starting playback.
- Drag a replay row onto a sidebar folder to move it, or click **Move to folder** and then choose a destination on the left. Moving also saves a recent replay so it survives history cleanup.
- Use **New folder**, **Rename folder** and **Delete folder** in the toolbar. Deleting a folder moves its replays to **Unfiled**, without deleting recordings.

Folder organization is stored in config/bridgetap/replay-folders.properties. Existing folder memberships are preserved by this update.

**Auto server folders** defaults to ON. Completed runs with a recorded server address are saved automatically into that server's folder, so they survive recent-history cleanup. Opening the library also files existing unfiled recordings that contain server metadata. Server address case and the default port share one folder; different non-default ports remain separate. Renaming a server folder keeps its server association. Existing manual folder assignments are respected. Singleplayer has its own folder; older recordings without a known address stay Unfiled. Use **Tools > Auto server folders** to turn the feature OFF. While it is ON, deleting a server folder can recreate it when its unfiled recordings are next processed.

## Protected replay saves and cleanup

Click **Save replay** in a replay's right-hand details panel to preserve it. **Saved replays** lists protected recordings. Favorites, imports, manual saves and older files without a protection flag are protected. Automatic server-folder saves are not protected until you explicitly save them. **Tools > Delete fails (2d)** enables optional cleanup; it defaults OFF. With cleanup enabled, unprotected FAILED recordings older than 48 hours are deleted during hourly checks. Successful runs, recent failures and protected/favorite recordings remain.

## Placed-block appearance and Focus mode

Open **B > Tools > Block appearance**. Search the registered block list, select any block and variant, enable it and click **Apply appearance**. It changes your newly placed blocks and recorded replay placements visually; server block state and collisions remain original. **Restore original blocks** disables the substitution. Blocks placed before this mod started tracking them are not retroactively identified.

In island placement, **Focus mode** hides other native map terrain while your scenery is active. Your copied scenery and tracked bridge placements remain visible. Removing the scenery restores normal terrain and sound playback; Focus preferences persist. Focus also mutes positional sounds farther than six blocks from the local player and sources closer to a nearby other player. Local footsteps, reachable block interactions and non-positional interface/music sounds remain audible. Sound attribution uses source positions because Minecraft 1.8.9 sound packets do not include a player identity. Focus does not alter collisions or server blocks.

## HUD and playback menus

**F4** during playback opens the recorded-input layout editor. Drag HUD elements anywhere on the screen and press **Tab** to hide the floating controls. **Escape** saves and closes the editor.

During a replay, **B** opens the library and **Escape** opens Minecraft's pause menu. Closing these menus keeps the same replay and position. The barrier remains the replay exit control; disconnecting from the world also cleans up playback.

## Validation

Automated checks cover replay-island extraction, template persistence and malformed files, folder membership and filtering, bounded capture buffers, pixel orientation, fine image detail and actual MP4/AAC encoding and decoding. A WASAPI smoke check verifies output capture produces a WAV. Native hook checks and a headless Forge startup verify screen linkage and copy/paste of native sandstone scenery while a replay is active, including camera positioning and world changes. The UI preview harness exercises folder moves, filtering and the buffer toggle. These checks do not validate the OpenGL scenery rendering in a running graphical Minecraft client; that remains a focus of this release.
