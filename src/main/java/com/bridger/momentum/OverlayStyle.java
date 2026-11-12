package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** RGB HUD styling, independent from the legacy chat/palette configuration. */
public final class OverlayStyle {
    private static final Path FILE=Paths.get("config","bridgetap","overlay-style.properties");
    private static final int[] parts=new int[BridgeConfig.colors.length];private static final boolean[] customParts=new boolean[BridgeConfig.colors.length];
    public static final int[] text={0xFFFFFFFF,0xFFFFFFFF,0xFFABB2B9};
    public static final boolean[] customText={false,false,false};
    public static final int[] backgrounds={0xA0000000,0xA0000000,0xC0080F18};
    public static final boolean[] showBackground={true,false,true};
    public static final boolean[] shadow={true,true,false},customShadow={false,false,false},fullShadow={false,false,false};
    public static final int[] shadowColor={0xFF000000,0xFF000000,0xFF000000};
    public static int pressedBackground=0xE0275665,pressedText=0xFFF1FFFF,accent=0xFF7AE2E3;
    static{load();}
    private OverlayStyle(){}
    public static String color(int part){return customParts[part]?HudText.token(parts[part]):BridgeConfig.color(part);}
    public static int part(int part){if(customParts[part])return parts[part];return (part==8?0xA0000000:part==9?0x80000000:0xFF000000)|BridgeConfig.rgb(part);}
    public static void part(int part,int argb){parts[part]=(part<8||part==10)?argb|0xFF000000:argb;customParts[part]=true;if((part<8||part==10))customText[0]=false;save();}
    public static void resetPart(int part){customParts[part]=false;if((part<8||part==10))customText[0]=false;save();}
    public static int background(int type){return type==0?part(8):backgrounds[type];}
    public static void background(int type,int value){if(type==0)part(8,value);else{backgrounds[type]=value;save();}}
    public static boolean backgroundShown(int type){return type==0?BridgeConfig.backgroundEnabled:showBackground[type];}
    public static void backgroundShown(int type,boolean value){if(type==0){BridgeConfig.backgroundEnabled=value;BridgeConfig.save();}else showBackground[type]=value;save();}
    public static boolean shadow(int type){return type==0?BridgeConfig.textShadow:shadow[type];}
    public static void shadow(int type,boolean value){if(type==0){BridgeConfig.textShadow=value;BridgeConfig.save();}else shadow[type]=value;save();}
    public static void shadowColor(int type,int value){shadowColor[type]=value;customShadow[type]=true;save();}
    public static void text(int type,int value){text[type]=value|0xFF000000;customText[type]=true;save();}
    public static String styled(int type,String line){return customText[type]?HudText.token(text[type])+HudText.unformatted(line):line;}
    public static void reset(int type){customText[type]=false;customShadow[type]=false;fullShadow[type]=false;shadowColor[type]=0xFF000000;text[type]=type==2?0xFFABB2B9:0xFFFFFFFF;shadow[type]=type!=2;showBackground[type]=type!=1;backgrounds[type]=type==2?0xC0080F18:0xA0000000;
        if(type==0){Arrays.fill(customParts,false);BridgeConfig.textShadow=true;BridgeConfig.backgroundEnabled=true;BridgeConfig.save();}
        if(type==2){pressedBackground=0xE0275665;pressedText=0xFFF1FFFF;accent=0xFF7AE2E3;}save();}
    private static int read(Properties p,String key,int fallback){try{return (int)Long.parseLong(p.getProperty(key,Integer.toHexString(fallback)),16);}catch(NumberFormatException invalid){return fallback;}}
    public static void load(){try{if(!Files.exists(FILE)||Files.size(FILE)>65536)return;Properties p=new Properties();try(InputStream in=Files.newInputStream(FILE)){p.load(in);}
        for(int i=0;i<parts.length;i++){customParts[i]=Boolean.parseBoolean(p.getProperty("part."+i+".custom","false"));parts[i]=read(p,"part."+i,0xFFFFFFFF);}
        for(int i=0;i<3;i++){text[i]=read(p,"text."+i,text[i]);customText[i]=Boolean.parseBoolean(p.getProperty("text."+i+".custom","false"));backgrounds[i]=read(p,"background."+i,backgrounds[i]);showBackground[i]=Boolean.parseBoolean(p.getProperty("background."+i+".shown",String.valueOf(showBackground[i])));shadow[i]=Boolean.parseBoolean(p.getProperty("shadow."+i,String.valueOf(shadow[i])));shadowColor[i]=read(p,"shadowColor."+i,shadowColor[i]);customShadow[i]=Boolean.parseBoolean(p.getProperty("customShadow."+i,"false"));fullShadow[i]=Boolean.parseBoolean(p.getProperty("fullShadow."+i,"false"));}
        pressedBackground=read(p,"pressedBackground",pressedBackground);pressedText=read(p,"pressedText",pressedText);accent=read(p,"accent",accent);
    }catch(IOException|IllegalArgumentException invalid){System.err.println("[BridgeTap] Overlay style: "+invalid.getMessage());}}
    public static void save(){Properties p=new Properties();for(int i=0;i<parts.length;i++){p.setProperty("part."+i+".custom",String.valueOf(customParts[i]));p.setProperty("part."+i,Integer.toHexString(parts[i]));}
        for(int i=0;i<3;i++){p.setProperty("text."+i,Integer.toHexString(text[i]));p.setProperty("text."+i+".custom",String.valueOf(customText[i]));p.setProperty("background."+i,Integer.toHexString(backgrounds[i]));p.setProperty("background."+i+".shown",String.valueOf(showBackground[i]));p.setProperty("shadow."+i,String.valueOf(shadow[i]));p.setProperty("shadowColor."+i,Integer.toHexString(shadowColor[i]));p.setProperty("customShadow."+i,String.valueOf(customShadow[i]));p.setProperty("fullShadow."+i,String.valueOf(fullShadow[i]));}
        p.setProperty("pressedBackground",Integer.toHexString(pressedBackground));p.setProperty("pressedText",Integer.toHexString(pressedText));p.setProperty("accent",Integer.toHexString(accent));
        try{Files.createDirectories(FILE.getParent());try(OutputStream out=Files.newOutputStream(FILE)){p.store(out,"BridgeTap RGB overlay styles");}}catch(IOException ex){System.err.println("[BridgeTap] Could not save overlay style: "+ex.getMessage());}}
}
