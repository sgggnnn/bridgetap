package com.bridger.momentum;

/** Editable inclusive copy bounds, independent of the renderer. */
public final class IslandBox {
    public int x,y,z,width,height,depth;
    public IslandBox(int x,int y,int z,int width,int height,int depth){this.x=x;this.y=y;this.z=z;this.width=width;this.height=height;this.depth=depth;}
    public long volume(){return (long)width*height*depth;}
    public boolean valid(){return width>0&&height>0&&depth>0&&width<=IslandTemplate.MAX_SIDE&&height<=256&&depth<=IslandTemplate.MAX_SIDE&&y>=0&&y+height<=256&&volume()<=IslandTemplate.MAX_VOLUME;}
    public void move(int dx,int dy,int dz){x+=dx;y=Math.max(0,Math.min(256-height,y+dy));z+=dz;}
    public void resize(int axis,int amount){if(axis==0)width=Math.max(1,Math.min(IslandTemplate.MAX_SIDE,width+amount));if(axis==1)height=Math.max(1,Math.min(Math.min(256,256-y),height+amount));if(axis==2)depth=Math.max(1,Math.min(IslandTemplate.MAX_SIDE,depth+amount));}
}
