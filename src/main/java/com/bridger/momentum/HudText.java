package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.C_23014920;

/** RGB tokens are render-only; saved summaries and server chat retain legacy color codes. */
public final class HudText {
    private static final int[] PALETTE={0,0xAA,0xAA00,0xAAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    public static final class Span {public final String text;public final int color;Span(String text,int color){this.text=text;this.color=color;}}
    private static final char[] HEX="0123456789ABCDEF".toCharArray();
    private static final java.util.regex.Pattern RGB=java.util.regex.Pattern.compile("(?i)\u00a7#[0-9a-f]{6}"),LEGACY=java.util.regex.Pattern.compile("(?i)\u00a7[0-9a-fk-or]");
    public static String token(int color){char[] token={'\u00a7','#','0','0','0','0','0','0'};for(int i=7;i>=2;i--){token[i]=HEX[color&15];color>>>=4;}return new String(token);}
    public static String legacy(String value){return value.indexOf("\u00a7#")<0?value:RGB.matcher(value).replaceAll("");}
    public static String unformatted(String value){return value.indexOf('\u00a7')<0?value:LEGACY.matcher(legacy(value)).replaceAll("");}
    public static List<Span> spans(String value,int base){List<Span> result=new ArrayList<>();StringBuilder text=new StringBuilder();int color=base;
        for(int i=0;i<value.length();i++){
            if(value.charAt(i)=='\u00a7'&&i+1<value.length()){
                int next=-1,used=0;char code=Character.toLowerCase(value.charAt(i+1));
                if(code=='#'&&i+8<=value.length())try{next=(int)Long.parseLong(value.substring(i+2,i+8),16)|0xFF000000;used=7;}catch(NumberFormatException invalid){}
                int palette="0123456789abcdef".indexOf(code);if(palette>=0){next=0xFF000000|PALETTE[palette];used=1;}else if(code=='r'){next=base;used=1;}
                if(used>0){if(text.length()>0){result.add(new Span(text.toString(),color));text.setLength(0);}color=next;i+=used;continue;}
            }
            text.append(value.charAt(i));
        }
        if(text.length()>0)result.add(new Span(text.toString(),color));return result;
    }
    public static int width(C_23014920 font,String value){return font.m_09235706(legacy(value));}
    public static void drawStyled(int type,C_23014920 font,String value,int x,int y,int color){
        boolean shadow=OverlayStyle.shadow(type);
        if(shadow&&(OverlayStyle.customShadow[type]||OverlayStyle.fullShadow[type])){
            String plain=unformatted(value);int sc=OverlayStyle.shadowColor[type];
            if((sc>>>24)!=0&&OverlayStyle.fullShadow[type]){for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(dx!=0||dy!=0)font.m_11445413(plain,x+dx,y+dy,sc,false);}
            else if((sc>>>24)!=0)font.m_11445413(plain,x+1,y+1,sc,false);
            draw(font,value,x,y,color,false);
        }else draw(font,value,x,y,color,shadow);
    }
    public static void draw(C_23014920 font,String value,int x,int y,int color,boolean shadow){
        if(!value.contains("\u00a7#")){font.m_11445413(value,x,y,color,shadow);return;}
        for(Span span:spans(value,color)){font.m_11445413(span.text,x,y,span.color,shadow);x+=font.m_09235706(span.text);}
    }
}
