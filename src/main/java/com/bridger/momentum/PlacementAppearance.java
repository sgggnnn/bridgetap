package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.unmapped.*;

/** Client-render substitution for blocks actually placed by the local player. */
public final class PlacementAppearance {
    private static final Map<Long,Integer> placed=new ConcurrentHashMap<>();private static C_34399650 world;
    private static volatile boolean enabled;private static volatile String block="minecraft:glass";private static volatile int metadata;private static volatile C_15815210 selected;
    static{try(InputStream in=Files.newInputStream(path())){Properties p=new Properties();p.load(in);enabled=Boolean.parseBoolean(p.getProperty("enabled","false"));block=p.getProperty("block",block);metadata=Math.max(0,Math.min(15,Integer.parseInt(p.getProperty("metadata","0"))));}catch(IOException|IllegalArgumentException ignored){}}
    private static Path path(){return Paths.get("config","bridgetap","placed-block-appearance.properties");}
    public static boolean enabled(){return enabled;}public static String block(){return block;}public static int metadata(){return metadata;}
    public static void tick(){C_34399650 current=ReplaySession.sourceWorld();if(world!=current){world=current;placed.clear();}}
    private static long key(int x,int y,int z){return ((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|(y&4095L);}
    public static void track(C_32594356 pos,int id){placed.put(key(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()),id);}
    public static boolean isPlaced(int x,int y,int z){long key=key(x,y,z);return ReplaySession.active()?ReplaySession.placedId(key)!=null:placed.containsKey(key);}
    public static C_15815210 replacement(C_32594356 pos,C_15815210 actual){
        if(!enabled||actual==null||IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))return null;int id=C_81592558.m_63461719(actual.m_41958956());if(id==0)return null;
        long key=key(pos.m_32364273(),pos.m_62825721(),pos.m_23514936());Integer original=ReplaySession.active()?ReplaySession.placedId(key):placed.get(key);if(original==null||original!=id)return null;
        if(selected==null){C_81592558 chosen=C_81592558.m_58693857(block);if(chosen==null||!block.equals(C_81592558.f_83036452.m_55287479(chosen).toString()))return null;try{selected=chosen.m_28027445(metadata);}catch(RuntimeException badVariant){selected=chosen.m_52891069();}}return selected;
    }
    public static void apply(String key,int variant,boolean active)throws IOException{C_81592558 chosen=C_81592558.m_58693857(key);if(chosen==null||!key.equals(C_81592558.f_83036452.m_55287479(chosen).toString()))throw new IOException("Choose a registered block");C_15815210 resolved;try{resolved=chosen.m_28027445(Math.max(0,Math.min(15,variant)));}catch(RuntimeException badVariant){throw new IOException("This block does not support that variant");}block=key;metadata=Math.max(0,Math.min(15,variant));enabled=active;selected=resolved;Properties p=new Properties();p.setProperty("enabled",String.valueOf(enabled));p.setProperty("block",block);p.setProperty("metadata",String.valueOf(metadata));Files.createDirectories(path().getParent());try(OutputStream out=Files.newOutputStream(path())){p.store(out,"BridgeTap client-only placed block appearance");}IslandReplacement.requestTerrainRebuild();}
}
