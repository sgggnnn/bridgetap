package com.bridger.momentum;

/** Deterministic tutorial timeline, independent of game ticks and replay state. */
public final class TutorialPlayback {
    public static final double STEP_SECONDS=6, LENGTH=24;
    private double time;private boolean playing=true;
    public double time(){return time;}
    public int step(){return Math.min(3,(int)(time/STEP_SECONDS));}
    public boolean playing(){return playing;}
    public void advance(double seconds){if(playing&&Double.isFinite(seconds)&&seconds>0){time=Math.min(LENGTH,time+Math.min(seconds,.25));if(time==LENGTH)playing=false;}}
    public void toggle(){if(time>=LENGTH)restart();else playing=!playing;}
    public void restart(){time=0;playing=true;}
    public void seek(double seconds){if(Double.isFinite(seconds))time=Math.max(0,Math.min(LENGTH,seconds));}
    public void step(int step){seek(Math.max(0,Math.min(3,step))*STEP_SECONDS);}
}
