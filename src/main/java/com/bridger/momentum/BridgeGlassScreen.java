package com.bridger.momentum;

/** Shared replay-window shell. All workspace pages use identical physical layout and glass. */
public abstract class BridgeGlassScreen extends BridgeScreen {
    @Override protected int canvasWidth(){return 1120;}
    @Override protected int canvasHeight(){return 640;}
    @Override protected boolean flatControls(){return true;}
    @Override protected int backgroundColor(){
        ThemeManager.Settings theme=ThemeManager.local().current();
        boolean light=theme.appearance==ThemeManager.Appearance.LIGHT_GLASS;
        boolean solid=theme.appearance==ThemeManager.Appearance.OLED||theme.surface==ThemeManager.Surface.FLAT;
        // Default 48% setting maps to 72% for the single window, without stacked panels.
        double opacity=Math.max(.1,Math.min(.95,.72+(theme.glassOpacity-48)/100.0));
        if(theme.surface==ThemeManager.Surface.FROSTED)opacity=Math.min(.95,opacity+.08);
        return UiTheme.alpha(light?0xEDF2F8:theme.appearance==ThemeManager.Appearance.OLED?0x020305:0x12141A,solid?.98:opacity);
    }
    @Override protected void paintBackground(){
        ui.glassRect(0,0,1120,640,16,backgroundColor(),UiTheme.alpha(TEXT,.12),.015f);
    }

    protected final void windowHeader(String title){
        ui.strongText(title,32,27,TEXT,1.5f);
        ui.textScaled("BRIDGETAP",32,54,MUTED,10f/12f);
        rect(24,78,1072,1,UiTheme.alpha(TEXT,.06));
    }
    protected final void windowStatus(String message){clipped(message,32,622,1056,UiTheme.alpha(TEXT,.5));}
    protected final void sidebar(String title){
        rect(232,96,1,508,UiTheme.alpha(TEXT,.06));text(title,32,104,UiTheme.alpha(TEXT,.5));
    }
    protected final void sidebarItem(String title,int y,boolean selected,Runnable action){
        if(selected||over(24,y,200,32))rounded(24,y,200,32,selected?UiTheme.alpha(ACCENT,.12):UiTheme.alpha(TEXT,.07));
        if(selected)rect(24,y+6,3,20,ACCENT);
        clipped(title,40,y+9,174,selected&&ThemeManager.local().current().appearance!=ThemeManager.Appearance.LIGHT_GLASS?ACCENT:TEXT);
        region(24,y,200,32,action);
    }
    protected final void choice(String title,int x,int y,int width,boolean selected,Runnable action){
        button(title,x,y,width,32,true,false,action);
        if(selected)ui.roundedRect(x+10,y+30,width-20,2,1,ACCENT);
    }
}
