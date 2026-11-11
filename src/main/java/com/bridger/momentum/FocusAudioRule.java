package com.bridger.momentum;

/** Keep interface audio and the local player's interactions; mute remote sources. */
public final class FocusAudioRule {
    public static boolean mute(boolean active,boolean positional,double ownDistanceSquared,double otherDistanceSquared){
        return active&&positional&&(ownDistanceSquared>36||(otherDistanceSquared<4&&otherDistanceSquared+.25<ownDistanceSquared));
    }
}
