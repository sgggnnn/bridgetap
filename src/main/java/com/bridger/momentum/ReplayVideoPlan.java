package com.bridger.momentum;

/** Render samples include both endpoints and never depend on wall-clock FPS. */
public final class ReplayVideoPlan {
    public final int fps,frames,lastTick,playbackFrames;
    public ReplayVideoPlan(int lastTick,int fps){this(lastTick,fps,0);}
    public ReplayVideoPlan(int lastTick,int fps,int endingSeconds){if(lastTick<0||lastTick>=ReplayData.MAX_FRAMES||(fps!=30&&fps!=60&&fps!=120)||endingSeconds<0||endingSeconds>5)throw new IllegalArgumentException("Invalid replay video settings");this.lastTick=lastTick;this.fps=fps;playbackFrames=(int)Math.ceil(lastTick/20.0*fps)+1;frames=playbackFrames+endingSeconds*fps;}
    public double tick(int frame){if(frame<0||frame>=frames)throw new IllegalArgumentException("Invalid video frame");return frame>=playbackFrames-1?lastTick:Math.min(lastTick,frame*20.0/fps);}
    public boolean ending(int frame){return frame>=playbackFrames&&frame<frames;}
}
