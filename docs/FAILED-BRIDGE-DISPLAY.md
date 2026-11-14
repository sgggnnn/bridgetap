# Failed bridge display

Open **Settings > Failed bridges**, or the native OneClient HUD category's **Failed bridge display** button.

- **Show all failed bridge information** keeps the existing failed-run HUD/chat behavior.
- **Hide failed bridges** hides the coordinate HUD once a run fails and suppresses its end-of-run BridgeTap chat output.
- **Show Fail Tap/4b only** keeps only the recorded Fail Tap/4b entries. The failed-run header, flat, ordinary taps and turn are hidden. When no fail-tap sample exists, nothing is invented.

The choice is saved between launches. It applies after a failed attempt; successful runs and live bridging retain their normal display. It does not delete recordings, alter original saved details, change success rates or hide replay playback telemetry. Other mods' or the server's chat messages are unaffected.

Auto fail analysis in chat is disabled by default and has its own toggle on this screen. The first upgrade to opt-in analysis switches the previous automatic default off; subsequent explicit choices are preserved. When enabled, it posts one message for the latest failed attempt, independent of normal coordinate output. The live diagnosis samples position and view angles at actual Minecraft use actions, including held RMB, and compares them with the last placed support. A later successful placement clears earlier unsuccessful inputs. Aim above, below or beside that support is reported as a likely issue; empty hand, missing input, out-of-reach and incomplete recordings are identified separately. A ray reaching the support does not prove a server rejection or another specific cause. This does not inspect server-side anticheat or packets beyond the existing recording.

Short chat messages use green for likely aim too high, red for likely aim too low, yellow for sideways aim and gray for uncertain causes. These are client-side observations, not proof of a server rejection.

Live analysis also records the native clicked face, placement target occupancy, probable overlap with the player's body and the native use result. These concrete observations take priority over the ray estimate. Top and bottom face hits are reported as observations rather than asserted causes; a local failure without additional evidence remains uncertain.
