package com.bridger.momentum;

/** A completed run must age five client ticks before another can start. */
public final class RunStartCooldown {
    public static final int TICKS=5;
    private boolean finished;
    private int finishedTick;
    public void finish(int tick){finished=true;finishedTick=tick;}
    public boolean ready(int tick){return !finished||tick-finishedTick>=TICKS;}
    public void reset(){finished=false;}
}
