package com.bridger.momentum;

/** Recorded inputs, never the spectator's live movement or mouse buttons. */
public final class ReplayInputOverlay {
    private ReplayInputOverlay(){}
    public static int height(ReplayData.TickStats stats){return stats==null?110:92;}
    public static void paint(UiCanvas canvas,int x,int y,ReplayData.TickStats stats){
        int held=stats==null?0:stats.held|stats.pressed;
        key(canvas,x+26,y,24,24,(held&1)!=0,0);
        key(canvas,x,y+26,24,24,(held&4)!=0,2);
        key(canvas,x+26,y+26,24,24,(held&2)!=0,1);
        key(canvas,x+52,y+26,24,24,(held&8)!=0,3);
        key(canvas,x,y+52,76,14,(held&16)!=0,4);
        key(canvas,x,y+68,37,24,(held&128)!=0,5);
        key(canvas,x+39,y+68,37,24,(held&256)!=0,6);
        if(stats==null)canvas.text("No input data",x,y+99,0xFF94A4BA);
    }
    private static void key(UiCanvas c,int x,int y,int w,int h,boolean held,int icon){
        c.rect(x,y,w,h,OverlayStyle.backgroundShown(2)?(held?OverlayStyle.pressedBackground:OverlayStyle.background(2)):0x00000000);
        if(held)c.rect(x,y+h-1,w,1,OverlayStyle.accent);
        int color=held?OverlayStyle.pressedText:OverlayStyle.text[2];
        if(icon==4){c.rect(x+(w-24)/2,y+8,24,2,color);return;}
        if(icon>=5){String text=icon==5?"LMB":"RMB";c.text(text,x+(w-c.width(text))/2,y+8,color);return;}
        int cx=x+w/2,cy=y+h/2;
        for(int i=0;i<4;i++){
            int n=7-i*2;
            if(icon==0)c.rect(cx-n/2,cy+2-i,n,1,color);
            if(icon==1)c.rect(cx-n/2,cy-2+i,n,1,color);
            if(icon==2)c.rect(cx+2-i,cy-n/2,1,n,color);
            if(icon==3)c.rect(cx-2+i,cy-n/2,1,n,color);
        }
    }
}
