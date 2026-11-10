package com.bridger.momentum;

/** Canvas transform used by the HUD studio; font metrics remain in local pixels. */
public final class ScaledUiCanvas implements UiCanvas {
    private final UiCanvas parent;private final int x,y;private final float scale;
    public ScaledUiCanvas(UiCanvas parent,int x,int y,float scale){this.parent=parent;this.x=x;this.y=y;this.scale=scale;}
    public void rect(int a,int b,int w,int h,int color){parent.rect(x+Math.round(a*scale),y+Math.round(b*scale),Math.max(1,Math.round(w*scale)),Math.max(1,Math.round(h*scale)),color);}
    public void text(String value,int a,int b,int color){parent.textScaled(value,x+Math.round(a*scale),y+Math.round(b*scale),color,scale);}
    public int width(String value){return parent.width(value);}
    public void textScaled(String value,int a,int b,int color,float other){parent.textScaled(value,x+Math.round(a*scale),y+Math.round(b*scale),color,scale*other);}
}
