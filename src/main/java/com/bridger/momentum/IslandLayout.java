package com.bridger.momentum;

/** Portable offsets relative to the current island attachment point. */
public final class IslandLayout {
    public final int x,y,z,startX,startY,startZ,finishX,finishY,finishZ,rotation;public final boolean focus,stableAnchor;
    public IslandLayout(int x,int y,int z,int sx,int sy,int sz,int fx,int fy,int fz,int rotation,boolean focus){this(x,y,z,sx,sy,sz,fx,fy,fz,rotation,focus,true);}
    public IslandLayout(int x,int y,int z,int sx,int sy,int sz,int fx,int fy,int fz,int rotation,boolean focus,boolean stableAnchor){for(int n:new int[]{x,z,sx,sz,fx,fz})if(Math.abs((long)n)>30000000)throw new IllegalArgumentException("Preset position is out of range");for(int n:new int[]{y,sy,fy})if(Math.abs((long)n)>512)throw new IllegalArgumentException("Preset height is out of range");if(rotation<0||rotation>3)throw new IllegalArgumentException("Invalid preset orientation");this.x=x;this.y=y;this.z=z;startX=sx;startY=sy;startZ=sz;finishX=fx;finishY=fy;finishZ=fz;this.rotation=rotation;this.focus=focus;this.stableAnchor=stableAnchor;}
}
