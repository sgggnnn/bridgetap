package com.bridger.momentum;
/** Stable observed geometry, independent of player travel and respawn height. */
public final class IslandModeChange {
 private int gap,rise,surface,nextGap,nextRise,nextSurface,count;
 public IslandModeChange(int gap,int rise,int surface){this.gap=gap;this.rise=rise;this.surface=surface;}
 public int[] observe(int newGap,int newRise,int newSurface){if(newGap==gap&&newRise==rise&&newSurface==surface){count=0;return null;}if(count==0||nextGap!=newGap||nextRise!=newRise||nextSurface!=newSurface){nextGap=newGap;nextRise=newRise;nextSurface=newSurface;count=1;return null;}if(++count<2)return null;int[] delta={newGap-gap,newRise-rise,newSurface-surface};gap=newGap;rise=newRise;surface=newSurface;count=0;return delta;}
 public int gap(){return gap;}public int rise(){return rise;}public int surface(){return surface;}
 public void unavailable(){count=0;}
}
