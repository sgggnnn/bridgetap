package com.bridger.momentum;

/** Inclusive client-only mask volume, independent of chunk arrival and block connectivity. */
public final class IslandMaskArea {
    public final int minX,minY,minZ,maxX,maxY,maxZ;
    public IslandMaskArea(int x,int y,int z,int maxX,int maxY,int maxZ){if(x>maxX||y>maxY||z>maxZ)throw new IllegalArgumentException("Invalid island mask bounds");minX=x;minY=Math.max(0,y);minZ=z;this.maxX=maxX;this.maxY=Math.min(255,maxY);this.maxZ=maxZ;}
    public boolean contains(int x,int y,int z){return x>=minX&&x<=maxX&&y>=minY&&y<=maxY&&z>=minZ&&z<=maxZ;}
    public boolean covers(IslandMaskArea b){return contains(b.minX,b.minY,b.minZ)&&contains(b.maxX,b.maxY,b.maxZ);}
}
