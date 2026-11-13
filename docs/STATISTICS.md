# Bridge statistics (1.8.1)

Open **Replay library > Tools > Statistics**, or **OneClient > BridgeTap > Replays > Bridge statistics**.

Choose a server, a mode and **All time**, **Today**, **This week** (Monday onward) or **Last 30 days**. Dates follow your local time zone. Single-day data uses minute-based groups; multiple-day data uses daily averages, grouped into at most 30 chart points for long histories. Switch the graph between average successful time and success rate. Hover a point to see its sample count. Empty periods do not contribute zero-second runs.

The time mean, best/worst time and population standard deviation include successful runs only. Success rate is successful attempts divided by every recorded attempt in the selection. Averages are weighted by run count, not averages of daily averages. Imported friends' replays do not increase your personal statistics.

New attempts are counted even with replay auto-save OFF. Minute aggregate counters are saved to `config/bridgetap/statistics.properties` independently of replay files; deleting or archiving a replay does not remove its statistics. IO and chart queries run on a background worker, with writes coalesced every five seconds and flushed on normal shutdown.

**Import saved history** performs a one-time import of older local recordings still on disk. It excludes imported replays and new attempts already tracked by this version. Interrupted imports can resume without duplicating counts. Previously deleted or never-saved old attempts cannot be reconstructed; all-time statistics therefore mean the history available to this tracker, not a complete lifetime account history.

McPlayHD modes use the original platform distances (16 Extra Short, 28 Short, 51 Normal, 83 Long), independently of height. A loaded, unambiguous finish platform can identify a mode even for a failed or incomplete run. Missing/ambiguous evidence stays **Unknown**. Other servers do not inherit McPlayHD's distance rules. Valid older measurements are retained, while missing older measurements can be indexed from recordings.

This release also defers startup update prompts until a safe menu or idle moment and rebinds replay rendering after resource/shader/island changes. Neither the real GPU recovery nor an actual OneClient update/restart is exercised by the headless automated tests.

Charts default to Today. Selections with data on a single day use a time-of-day axis, with up to 60 minute-based groups. Lines connect available samples; empty intervals do not count as zero. Existing hourly statistics remain available, but their original per-minute values cannot be recovered. A single sample is shown as a point with an explanation.
