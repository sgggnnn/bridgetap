# Live chat and replay custom skies

Replays share the current session's actual chat widget. Open chat normally to read and send messages, use commands, scroll or access sent-message history. Pausing or seeking the replay does not rewind chat or change message age. Chat messages are not recorded or included in new replay exports.

The experimental v6 chat section from 1.8.2-dev.5 is ignored when reading and omitted when re-exporting. New sky recordings use v7, which has no chat section. Existing files are not rewritten automatically.

Recordings keep biome arrays near the route, the world clock and initial weather. The replay uses the resource packs currently enabled on your client. OneClient's Cera custom sky assets are loaded for the replay renderer and refreshed when the live renderer reloads its sky assets. Minecraft still needs a custom sky implementation, such as Cera or OptiFine, and its custom sky setting enabled.

Old replay files remain readable and can use the currently enabled sky pack. Clean HUD video exports continue to hide native chat together with the native HUD.
