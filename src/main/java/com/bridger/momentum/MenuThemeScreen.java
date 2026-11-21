package com.bridger.momentum;

import java.util.Locale;
import net.minecraft.unmapped.C_85740840;

/** BridgeGlass composer. Every control edits the same immutable ThemeManager snapshot. */
public final class MenuThemeScreen extends BridgeGlassScreen {
    public static final int VIEW_WIDTH=1120,VIEW_HEIGHT=640;
    private final C_85740840 parent;private String hex=String.format(Locale.ROOT,"%06X",UiTheme.accent()&0xFFFFFF),message="";
    private static final int[] COLORS={0xFF6B00,0x8B5CF6,0x3B82F6,0x10B981,0xEC4899};
    private static final String[] NAMES={"Orange","Violet","Blue","Emerald","Pink"};
    public MenuThemeScreen(C_85740840 parent){this.parent=parent;}
    @Override protected int canvasWidth(){return VIEW_WIDTH;}
    @Override protected int canvasHeight(){return VIEW_HEIGHT;}
    @Override protected boolean keyboardNavigation(){return true;}
    private ThemeManager.Settings settings(){return ThemeManager.local().current();}
    private void update(ThemeManager.Settings value){ThemeManager.local().set(value);hex=String.format(Locale.ROOT,"%06X",value.accent&0xFFFFFF);message="";}
    private void apply(int color){update(settings().accent(color));}
    private void channel(int channel,int value){int shift=(2-channel)*8;int rgb=(settings().accent&~(255<<shift))|(value<<shift);update(settings().accent(rgb));}
    private void save(){try{ThemeManager.local().flush();}catch(java.io.IOException ex){message="Could not save the theme. Check folder permissions.";}}
    @Override protected void paint(){
        ThemeManager.Settings s=settings();windowHeader("BridgeGlass");
        button("Reset theme",844,25,128,32,true,false,()->update(ThemeManager.Settings.defaults()));button("Back",984,25,104,32,true,false,this::back);
        text("APPEARANCE",40,104,MUTED);
        String[] modes={"Dark Glass","Light Glass","OLED / Solid Dark"};for(int i=0;i<3;i++){final int mode=i;choice(modes[i],40+i*204,134,188,s.appearance.ordinal()==i,()->update(settings().appearance(ThemeManager.Appearance.values()[mode])));}
        text("SURFACE",40,210,MUTED);
        String[] surfaces={"See-through Glass","Tinted / Frosted","Flat / Minimal"};for(int i=0;i<3;i++){final int surface=i;choice(surfaces[i],40+i*204,238,188,s.surface.ordinal()==i,()->update(settings().surface(ThemeManager.Surface.values()[surface])));}
        text("ACCENT COLOR",40,310,MUTED);
        for(int i=0;i<COLORS.length;i++){
            final int color=COLORS[i];int x=40+i*88;ui.roundedRect(x,338,72,38,10,0xFF000000|color);if((s.accent&0xFFFFFF)==color)check(x+31,352,UiTheme.onAccent(color));region(x,338,72,38,()->apply(color));
            clipped(NAMES[i],x,385,72,TEXT);tooltip(String.format(Locale.ROOT,"#%06X",color),x,338,72,38);
        }
        label("Custom HEX",496,332);input("hex",hex,"FF6B00",496,354,140,6,value->hex=value);
        button("Apply HEX",496,392,140,28,hex.matches("[0-9a-fA-F]{6}"),false,false,"Six hexadecimal digits, without #",()->apply(Integer.parseInt(hex,16)));
        text("FINE TUNING",40,464,MUTED);
        slider("blur","Blur radius",s.blurRadius,0,30," px",40,488,596,s.appearance!=ThemeManager.Appearance.OLED&&s.surface!=ThemeManager.Surface.FLAT,v->update(settings().blur(v)));
        slider("opacity","Glass opacity",s.glassOpacity,10,95,"%",40,532,596,s.appearance!=ThemeManager.Appearance.OLED&&s.surface!=ThemeManager.Surface.FLAT,v->update(settings().opacity(v)));
        slider("radius","Corner radius",s.cornerRadius,4,16," px",40,576,596,true,v->update(settings().radius(v)));
        text("LIVE PREVIEW",768,108,MUTED);
        String appearance=modes[s.appearance.ordinal()],surface=surfaces[s.surface.ordinal()];clipped(appearance+" / "+surface,768,134,296,TEXT);
        label("EVENING PRACTICE",786,188);ui.textScaled("3.850s",786,220,TEXT,1.85f);statusBadge(true,964,222,false);label("Short / 9 jumps / Axis Z",786,268);
        button("PLAY REPLAY",786,316,260,38,true,true,()->message="Preview only. Your replays are unchanged.");
        text("CUSTOM RGB",768,432,MUTED);
        slider("red","Red",(s.accent>>16)&255,0,255,"",768,458,296,true,v->channel(0,v));
        slider("green","Green",(s.accent>>8)&255,0,255,"",768,510,296,true,v->channel(1,v));
        slider("blue","Blue",s.accent&255,0,255,"",768,562,296,true,v->channel(2,v));
        rect(720,96,1,508,UiTheme.alpha(TEXT,.06));String info=!message.isEmpty()?message:!s.blurEnabled()?"Background blur is off for this surface. Gameplay HUD colors stay unchanged.":BackdropBlur.status()+" / Inter typography / Settings save automatically";windowStatus(info);
    }
    @Override public void releasePreview(){super.releasePreview();save();}
    @Override public void m_47002234(){save();}
    @Override protected void back(){save();open(parent);}
}
