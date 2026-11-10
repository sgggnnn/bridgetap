package com.bridger.momentum;

/** Bounds and snapping in Minecraft GUI pixels, independent of editor scaling. */
public final class OverlayPlacement {
    private OverlayPlacement(){}
    public static int clamp(int position,int size,int screen){return Math.max(0,Math.min(Math.max(0,screen-size),position));}
    public static int snap(int position,int size,int screen,boolean enabled){
        int value=clamp(position,size,screen);if(!enabled)return value;
        int[] guides={0,(screen-size)/2,screen-size};
        for(int guide:guides)if(Math.abs(value-guide)<=4)return clamp(guide,size,screen);
        return value;
    }
}
