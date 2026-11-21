package com.bridger.momentum;

/** Semantic BridgeGlass palette. HUD colors are deliberately separate. */
public final class UiTheme {
 public static final int ACCENT=ThemeManager.DEFAULT_ACCENT;
 public static final int PADDING=16,GAP=12,SMALL_GAP=8,RADIUS=12,BODY_SIZE=12,TITLE_SIZE=20,CONTROL_HEIGHT=28;
 public static int BACKGROUND,PANEL,RAISED,BORDER,TEXT,SECONDARY,SUCCESS,FAILURE,HOVER,SELECTED,DANGER,DISABLED,DISABLED_TEXT;
 static{refresh();}
 public static void refresh(){ThemeManager.Settings s=ThemeManager.local().current();boolean light=s.appearance==ThemeManager.Appearance.LIGHT_GLASS,solid=s.appearance==ThemeManager.Appearance.OLED||s.surface==ThemeManager.Surface.FLAT;double opacity=s.glassOpacity/100.0;if(s.surface==ThemeManager.Surface.FROSTED)opacity=Math.min(.95,.2+.8*opacity);
  BACKGROUND=alpha(light?0xEDF2F8:s.appearance==ThemeManager.Appearance.OLED?0x020305:0x0D121B,solid?.98:opacity*.42);PANEL=alpha(light?0xF8FAFF:s.appearance==ThemeManager.Appearance.OLED?0x080C13:0x17202B,solid?.98:opacity);RAISED=alpha(light?0xFFFFFF:0x34404F,solid?.96:Math.min(.85,opacity+.14));BORDER=light?0x180E1B2C:0x14FFFFFF;
  TEXT=light?0xFF162132:0xFFF3F6FD;SECONDARY=light?0xFF46566C:0xFFB5C1D2;SUCCESS=light?0xFF116842:0xFF8CDEB2;FAILURE=light?0xFFA52546:0xFFFFA7B8;HOVER=alpha(light?0xE0E8F4:0x536479,.4);SELECTED=alpha(s.accent,.17);DANGER=alpha(light?0xAF3450:0xB65268,.24);DISABLED=alpha(light?0xCDD5E0:0x344050,.24);DISABLED_TEXT=light?0xFF647185:0xFF95A4B7;
 }
 public static int alpha(int color,double opacity){return ((int)Math.round(Math.max(0,Math.min(1,opacity))*255)<<24)|(color&0xFFFFFF);}
 public static int accent(){return ThemeManager.local().current().accent;}
 public static void accent(int color)throws java.io.IOException{ThemeManager.local().set(ThemeManager.local().current().accent(color));ThemeManager.local().flush();refresh();}
 public static int blend(int color,int target,double amount){int r=(int)(((color>>16)&255)*(1-amount)+((target>>16)&255)*amount),g=(int)(((color>>8)&255)*(1-amount)+((target>>8)&255)*amount),b=(int)((color&255)*(1-amount)+(target&255)*amount);return 0xFF000000|(r<<16)|(g<<8)|b;}
 private static double linear(int value){double v=value/255.0;return v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4);}
 public static int onAccent(int color){double luminance=.2126*linear((color>>16)&255)+.7152*linear((color>>8)&255)+.0722*linear(color&255);return luminance>.179?0xFF111824:0xFFFFFFFF;}
 private UiTheme(){}
}
