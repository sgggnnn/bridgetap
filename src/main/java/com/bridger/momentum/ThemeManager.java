package com.bridger.momentum;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Local BridgeGlass settings, independent of replay data and OneClient configuration. */
public final class ThemeManager {
    public enum Appearance {DARK_GLASS,LIGHT_GLASS,OLED}
    public enum Surface {SEE_THROUGH,FROSTED,FLAT}
    public static final int DEFAULT_ACCENT=0xFFFF6B00;
    public static final class Settings {
        public final Appearance appearance;public final Surface surface;
        public final int accent,blurRadius,glassOpacity,cornerRadius;
        public Settings(Appearance appearance,Surface surface,int accent,int blur,int opacity,int radius){
            this.appearance=appearance==null?Appearance.DARK_GLASS:appearance;this.surface=surface==null?Surface.SEE_THROUGH:surface;
            this.accent=0xFF000000|accent;blurRadius=clamp(blur,0,30);glassOpacity=clamp(opacity,10,95);cornerRadius=clamp(radius,4,16);
        }
        public static Settings defaults(){return new Settings(Appearance.DARK_GLASS,Surface.SEE_THROUGH,DEFAULT_ACCENT,14,48,12);}
        public Settings appearance(Appearance value){return new Settings(value,surface,accent,blurRadius,glassOpacity,cornerRadius);}
        public Settings surface(Surface value){return new Settings(appearance,value,accent,blurRadius,glassOpacity,cornerRadius);}
        public Settings accent(int value){return new Settings(appearance,surface,value,blurRadius,glassOpacity,cornerRadius);}
        public Settings blur(int value){return new Settings(appearance,surface,accent,value,glassOpacity,cornerRadius);}
        public Settings opacity(int value){return new Settings(appearance,surface,accent,blurRadius,value,cornerRadius);}
        public Settings radius(int value){return new Settings(appearance,surface,accent,blurRadius,glassOpacity,value);}
        public boolean blurEnabled(){return appearance!=Appearance.OLED&&surface!=Surface.FLAT&&blurRadius>0;}
        public int effectiveBlur(){return blurEnabled()?Math.min(30,surface==Surface.FROSTED?(int)Math.round(blurRadius*1.35):blurRadius):0;}
    }
    private static int clamp(int value,int low,int high){return Math.max(low,Math.min(high,value));}
    private static final class Local {static final ThemeManager INSTANCE=new ThemeManager(Paths.get("config/bridgetap/bridgeglass-theme.json"));}
    public static ThemeManager local(){return Local.INSTANCE;}
    private final Path file;private volatile Settings current=Settings.defaults();private boolean dirty;private long lastChange;private String status="";
    public ThemeManager(Path file){this.file=file;load();}
    public Settings current(){return current;}
    public String status(){return status;}
    public synchronized void set(Settings value){if(value==null)throw new IllegalArgumentException("Missing theme");current=value;dirty=true;lastChange=System.nanoTime();status="Preview updated. Changes save automatically.";}
    private static <T extends Enum<T>> T choice(Class<T> type,String value,T fallback){try{return Enum.valueOf(type,value);}catch(IllegalArgumentException|NullPointerException invalid){return fallback;}}
    private static String string(JsonObject json,String key,String fallback){try{return json.get(key).getAsString();}catch(RuntimeException invalid){return fallback;}}
    private static int number(JsonObject json,String key,int fallback){try{double value=json.get(key).getAsDouble();return Double.isFinite(value)?(int)Math.max(Integer.MIN_VALUE,Math.min(Integer.MAX_VALUE,value)):fallback;}catch(RuntimeException invalid){return fallback;}}
    private void load(){
        if(!Files.isRegularFile(file)){migrate();return;}
        try{
            if(Files.size(file)>16384)throw new IOException("Theme too large");
            JsonElement parsed=new JsonParser().parse(new String(Files.readAllBytes(file),StandardCharsets.UTF_8));if(!parsed.isJsonObject())throw new IOException("Expected object");JsonObject json=parsed.getAsJsonObject();
            if(number(json,"version",1)!=1)throw new IOException("Unsupported theme version");Settings d=Settings.defaults();String hex=string(json,"accent","FF6B00");if(hex.startsWith("#"))hex=hex.substring(1);
            int accent=hex.matches("[0-9a-fA-F]{6}")?(int)Long.parseLong(hex,16):d.accent;
            current=new Settings(choice(Appearance.class,string(json,"appearance",""),d.appearance),choice(Surface.class,string(json,"surface",""),d.surface),accent,number(json,"blurRadius",d.blurRadius),number(json,"glassOpacity",d.glassOpacity),number(json,"cornerRadius",d.cornerRadius));
        }catch(IOException|RuntimeException|StackOverflowError invalid){status="Invalid theme settings; defaults are active.";}
    }
    private void migrate(){
        Path previous=file.resolveSibling("polyglass-theme.json");
        if(!previous.equals(file)&&Files.isRegularFile(previous)){ThemeManager old=new ThemeManager(previous);if(old.status.isEmpty()){current=old.current;dirty=true;return;}}
        Path legacy=file.resolveSibling("menu-theme.properties");try{
            if(!Files.isRegularFile(legacy)||Files.size(legacy)>65536)return;Properties p=new Properties();try(InputStream in=Files.newInputStream(legacy)){p.load(in);}String hex=p.getProperty("accent","");
            if(hex.matches("[0-9a-fA-F]{6}")){int color=Integer.parseInt(hex,16);if(color!=0xFF851B)current=current.accent(color);dirty=true;}
        }catch(IOException|IllegalArgumentException ignored){}
    }
    public synchronized void tick(){if(dirty&&System.nanoTime()-lastChange>500000000L)try{flush();}catch(IOException ex){status="Could not save theme settings.";}}
    public synchronized void flush()throws IOException{
        if(!dirty)return;Settings s=current;JsonObject json=new JsonObject();json.addProperty("version",1);json.addProperty("appearance",s.appearance.name());json.addProperty("surface",s.surface.name());json.addProperty("accent",String.format(Locale.ROOT,"#%06X",s.accent&0xFFFFFF));json.addProperty("blurRadius",s.blurRadius);json.addProperty("glassOpacity",s.glassOpacity);json.addProperty("cornerRadius",s.cornerRadius);
        byte[] bytes=new GsonBuilder().setPrettyPrinting().create().toJson(json).getBytes(StandardCharsets.UTF_8);Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"bridgeglass-",".tmp");
        try{Files.write(temp,bytes);try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException unsupported){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}dirty=false;status="Theme saved.";}finally{Files.deleteIfExists(temp);}
    }
}
