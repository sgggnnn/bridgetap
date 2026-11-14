# Replay sharing

## Offline clipboard code

Open a replay's **Details → Copy share code**, or use **Tools → Copy share code**. Review what will be shared, then click **Copy share code**. Paste the complete `BTR1:` code into your message. The recipient opens **Import → Import from clipboard**, reviews the name, time, axis, technique, jumps and recording version, then clicks **Confirm import**. The recording is saved under **Imported** only after confirmation.

Codes contain a complete compressed `.btreplay` archive, a version prefix and a SHA-256 integrity checksum. They are limited to **1,900 characters**, leaving room inside a standard 2,000-character Discord message. Many full-world recordings exceed this limit. Use **Export shared file** instead; no frames, blocks or telemetry are dropped to make a code fit. A checksum detects corruption, not the sender's identity or trustworthiness.

The new share flow removes recorded player name, UUID, skin, custom replay name, server address, creation date and free-text notes by default. The shared name becomes **Shared replay** and techniques use canonical labels. Movement, coordinates, rotations, blocks, items, inputs, dimension and recorded sky/biome/weather context remain. Coordinates/world geometry may still reveal the map. **Include recorded player name, UUID and skin** is OFF by default; enabling it and copying/exporting explicitly consents to sharing that recorded identity. It never queries or adds the current player identity.

The old **Export replay file / Export file** action is unchanged and retains the original recording's identity and metadata. Use the privacy-reviewed **Export shared file** for an anonymized attachment. Importing or preparing a code never changes the source recording.

## Import safety

Both archive names are allowlisted and never used as output paths. Imported run identifiers must be UUIDs; collisions create a new UUID. Archives are limited to 10 MiB, their recording entries to 8 MiB, metadata to 1 MiB, and decompressed recording data to 16 MiB. Frame/block counts and every recording version are checked. Malformed or unsupported inputs produce an error instead of an import. Files are validated into an in-memory preview before confirmation, so changing the original file afterwards does not change the approved import.

Recording versions v4 (with or without placement provenance) and v5 keep their timing/version metadata. Existing sky-context formats remain supported; the existing reader discards legacy v6 recorded chat. No original files are rewritten. All code preparation and validation run on a background worker. The clipboard is read only when its button is pressed. This offline flow makes no network requests.
