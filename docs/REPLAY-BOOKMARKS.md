# Replay bookmarks

During playback, select the **Bookmarks book in hotbar slot 5**, then left- or right-click to open the jump list. The clock controls playback speed. Each bookmark row has a **Go** button that immediately seeks to its tick; selecting the name first is unnecessary.

**Auto: Jump #1, Jump #2, ...** markers are generated automatically at recorded takeoff boundaries and sorted with manual bookmarks. They are read-only, do not consume the 128 manual slots and do not write extra files. Loading and detection run in the background once when the menu opens; the displayed list is cached. Holding jump during flight creates no extra markers. Old recordings can use grounded takeoffs when their pose data is sufficient; an incomplete recording cannot recover a jump before its first frame.

Pause or use the sticks to reach the desired tick. Open bookmarks, enter a name (optional; defaults to Tick N), and choose **Add current tick**. Select an existing bookmark to rename or delete it. **Go to bookmark** seeks to its exact tick, reconstructs blocks and the ghost pose, and leaves playback paused. Play resumes through the usual center item. Esc / Return to replay closes the menu without leaving the replay.

Outside playback, select a replay and use **Tools > Replay bookmarks** to manage its bookmarks or start playback at a selected tick. Join a world before playing a recording.

Up to 128 named bookmarks are stored per replay ID under `config/bridgetap/replay-bookmarks/`. Bookmarks are local sidecars, not included in replay exports. They do not change the recording or enable replay auto-save. Save the replay itself in the library if you want to retain a recent recording after restarting or recent-history cleanup.

**Join Discord** opens the official invitation in your system browser: https://discord.gg/7gYxewvgjD. If browsing is unavailable, the invitation is copied to the clipboard. Available in the replay library, settings, Tools and native OneClient Tools.
