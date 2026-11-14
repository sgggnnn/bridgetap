# Automatic custom islands

1. Stand on the start platform of your bridge lane.
2. Open **Island library** and choose a default or saved island.
3. Click **Apply automatically**. When the lane is detected, both islands are applied immediately.

The fit uses the walkable platform edges to match orientation, bridge gap and finish elevation. Copied decorations do not determine the distance. A finish pressure plate, including iron and gold, helps distinguish your finish from neighbouring start platforms. In replays the recorded start is used even if the free camera is elsewhere.

Both native platforms must be loaded, with a visible deck at least three blocks wide and two blocks deep. The search covers cardinal lanes up to 160 blocks away, from eight blocks lower to sixteen blocks higher. If the lane cannot be identified, the placement editor opens with a retry message. No unverified fit is automatically confirmed. **Preview / Adjust** always opens the editor; **Re-detect bridge** resets the fit, and Advanced controls retain independent island movement and presets.

Successful live fits remember the start anchor for presets. Presets can also detect the start beneath you on first use, without a separate marking step. Existing marked presets continue to validate their lane and retain manual adjustments.

# Connected textures

Enable a resource pack containing connected-texture rules and enable CTM in your rendering mod. Island meshes use their own copied neighbouring blocks, including independent island offsets, instead of the underlying server terrain. Cera transforms and overlay quads are supported; an optional OptiFine adapter handles its base connected quads. If no compatible CTM implementation is installed, ordinary block textures remain available.

CTM results are stored in the island mesh. Rule changes invalidate the cache, and positional changes refresh after movement settles. The optional OptiFine path still needs in-game verification with an OptiFine installation; the Cera contract, neighbour view and cache lifecycle have automated coverage. Pack-specific visual effects and shaders should also be checked in game.

Legacy paired island files can have their attachment inside the empty bridge corridor. Their start group is resolved by the nearest island geometry, preserving the original start/finish order. Weighted finish plates identify the arrival floor; stone and wooden decorative plates do not override it.

Automatic fitting uses the copied weighted finish-plate row for both islands (the attachment is a fallback without plates), snapping to a supported three-wide edge. Asymmetric side decorations do not redefine these rows. Small decorations and pressure plates are excluded when measuring copied floor elevations.

The native bridge row is taken from weighted finish plates at the selected arrival-floor height. A matching recording is only a fallback when plates are unavailable, so an off-centre first placement cannot move both islands. Both endpoints must support the same row. This avoids one- or two-block shifts caused by an off-centre native platform footprint.

Random island changes the style after each completed attempt (success or fail), once the player is back on a loaded start island. Random block follows the same completed-attempt timing. The first placed block, rejected ghost blocks and duplicate server updates do not trigger appearance changes. Replay playback does not count.

Auto reapply runs the same **Apply automatically** action after each completed attempt, only if no custom island is currently displayed. It waits while the player is bridging, falling, on the finish island, or editing a menu. Turning it on does not move the island. There is no separate mode monitor or saved-offset restoration loop: the manual and scheduled actions share the same placement method. With Random island enabled, its automatic placement also satisfies a pending reapply so the island is not fitted twice.