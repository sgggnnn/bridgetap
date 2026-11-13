package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.unmapped.*;

/** Client-render substitution for blocks actually placed by the local player. */
public final class PlacementAppearance {
    private static final Map<Long,Integer> placed=new ConcurrentHashMap<>();private static C_34399650 world;
    private static volatile Reservation reservation;
    private static final class Reservation{final long first,second;final boolean a,b;final Integer id;Reservation(long first,boolean a,long second,boolean b,int id){this.first=first;this.a=a;this.second=second;this.b=b;this.id=id;}}
    public static void reserve(C_32594356 first,int beforeFirst,C_32594356 second,int beforeSecond,int id){reservation=enabled?new Reservation(key(first.m_32364273(),first.m_62825721(),first.m_23514936()),beforeFirst!=id,key(second.m_32364273(),second.m_62825721(),second.m_23514936()),beforeSecond!=id,id):null;}
    private static Integer tracked(long key){Reservation r=reservation;if(r!=null&&(r.a&&r.first==key||r.b&&r.second==key))return r.id;return placed.get(key);}
    private static volatile boolean enabled;private static volatile String block="minecraft:glass";private static volatile int metadata;private static volatile C_15815210 selected;private static volatile C_41102159 itemCached;
    public static void invalidateItemModel(){itemCached=null;}
    static{try(InputStream in=Files.newInputStream(path())){Properties p=new Properties();p.load(in);enabled=Boolean.parseBoolean(p.getProperty("enabled","false"));block=p.getProperty("block",block);metadata=Math.max(0,Math.min(15,Integer.parseInt(p.getProperty("metadata","0"))));}catch(IOException|IllegalArgumentException ignored){}}
    private static Path path(){return Paths.get("config","bridgetap","placed-block-appearance.properties");}
    public static boolean enabled(){return enabled;}public static String block(){return block;}public static int metadata(){return metadata;}
    public static void tick(){ModernAppearance.tick();C_34399650 current=ReplaySession.sourceWorld();if(world!=current){world=current;placed.clear();reservation=null;originalSounds.clear();}}
    private static long key(int x,int y,int z){return ((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|(y&4095L);}
    public static void track(C_32594356 pos,int id){long k=key(pos.m_32364273(),pos.m_62825721(),pos.m_23514936());placed.put(k,id);if(enabled){C_81592558 actual=C_81592558.m_64320102(id);originalSounds.put(k,new OriginalSound(System.nanoTime(),actual.f_13889746.m_49812403()));if(originalSounds.size()>128){long now=System.nanoTime();originalSounds.entrySet().removeIf(e->now-e.getValue().time>350000000L);}}}
    public static boolean isPlaced(int x,int y,int z){long key=key(x,y,z);return ReplaySession.active()?ReplaySession.placedId(key)!=null:tracked(key)!=null;}
    public static C_15815210 replacement(C_32594356 pos,C_15815210 actual){
        if(block.startsWith("modern:"))return null;
        if(!enabled||actual==null||IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))return null;int id=C_81592558.m_63461719(actual.m_41958956());if(id==0)return null;
        long key=key(pos.m_32364273(),pos.m_62825721(),pos.m_23514936());Integer original=ReplaySession.active()?ReplaySession.placedId(key):tracked(key);if(original==null||original!=id)return null;
        if(selected==null){C_81592558 chosen=C_81592558.m_58693857(block);if(chosen==null||!block.equals(C_81592558.f_83036452.m_55287479(chosen).toString()))return null;try{selected=chosen.m_28027445(metadata);}catch(RuntimeException badVariant){selected=chosen.m_52891069();}}return selected;
    }
    public static void apply(String key,int variant,boolean active)throws IOException{itemCached=null;if(key.startsWith("modern:")){if(active)ModernAppearance.select(key);block=key;metadata=0;enabled=active;selected=null;store();IslandReplacement.requestTerrainRebuild();return;}C_81592558 chosen=C_81592558.m_58693857(key);if(chosen==null||!key.equals(C_81592558.f_83036452.m_55287479(chosen).toString()))throw new IOException("Choose a registered block");C_15815210 resolved;try{resolved=chosen.m_28027445(Math.max(0,Math.min(15,variant)));}catch(RuntimeException badVariant){throw new IOException("This block does not support that variant");}block=key;metadata=Math.max(0,Math.min(15,variant));enabled=active;selected=resolved;store();IslandReplacement.requestTerrainRebuild();}
    private static void store()throws IOException{Properties p=new Properties();p.setProperty("enabled",String.valueOf(enabled));p.setProperty("block",block);p.setProperty("metadata",String.valueOf(metadata));Files.createDirectories(path().getParent());try(OutputStream out=Files.newOutputStream(path())){p.store(out,"BridgeTap client-only placed block appearance");}}
    private static final ThreadLocal<String> placementBase=ThreadLocal.withInitial(()->"");
    private static final ThreadLocal<Boolean> placement=ThreadLocal.withInitial(()->false);
    public static void beginPlacement(){placementBase.set("");placement.set(true);}
    public static void beginPlacement(C_88708284 stack){beginPlacement();if(stack!=null&&stack.m_23235460() instanceof C_71827890)placementBase.set(((C_71827890)stack.m_23235460()).m_96251309().f_13889746.m_49812403());}
    public static void endPlacement(){placement.set(false);placementBase.set("");reservation=null;}
    public static boolean placing(){return placement.get();}
    private static final ThreadLocal<Boolean> itemLookup=ThreadLocal.withInitial(()->false);
    public static boolean matches(C_32594356 pos,C_15815210 actual){if(actual==null||!ReplaySession.active()&&placed.isEmpty()&&reservation==null)return false;long k=key(pos.m_32364273(),pos.m_62825721(),pos.m_23514936());Integer id=ReplaySession.active()?ReplaySession.placedId(k):tracked(k);return id!=null&&id==C_81592558.m_63461719(actual.m_41958956());}
    public static C_41102159 itemModel(C_03832013 mesher,C_88708284 stack){if(!enabled||itemLookup.get()||stack==null||!(stack.m_23235460() instanceof C_71827890))return null;net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.m_20213497();if(mc.f_28396371==null)return null;C_88708284 held=mc.f_28396371.f_29439708.m_27760697();if(!ReplaySession.active()&&(held==null||held.m_23235460()!=stack.m_23235460()||held.m_30227787()!=stack.m_30227787()))return null;if(block.startsWith("modern:"))return ModernAppearance.model();C_41102159 cached=itemCached;if(cached!=null)return cached;try{itemLookup.set(true);C_81592558 chosen=C_81592558.m_58693857(block);if(chosen==null||C_81592558.m_63461719(chosen)==0)return null;C_88708284 visual=new C_88708284(chosen,1,metadata);C_41102159 missing=mesher.m_94634856().m_03630986();C_41102159 item=visual.m_23235460()==null?null:mesher.m_24478184(visual);if(item!=null&&item!=missing){itemCached=item;return item;}C_41102159 stateModel=mc.m_01427411()==null?null:mc.m_01427411().m_35215294().m_50076884(chosen.m_28027445(metadata));C_41102159 resolved=stateModel!=null&&stateModel!=missing?stateModel:item;if(resolved!=missing)itemCached=resolved;return resolved;}finally{itemLookup.set(false);}}
    /** Presentation copy only: native block-entity renderers and item tinting inspect the stack. */
    public static C_88708284 visualStack(C_88708284 actual){if(block.startsWith("modern:"))return actual;C_81592558 chosen=C_81592558.m_58693857(block);if(chosen==null||C_98888256.m_11873116(chosen)==null)return actual;return new C_88708284(chosen,actual.f_60602012,metadata);}
    private static final ThreadLocal<Boolean> customSound=ThreadLocal.withInitial(()->false);
    private static final Map<Long,OriginalSound> originalSounds=new ConcurrentHashMap<>();
    private static final class OriginalSound{final long time;final String event;OriginalSound(long time,String event){this.time=time;this.event=event;}}
    public static boolean suppressOriginal(C_53396390 sound){if(!enabled||customSound.get()||sound==null)return false;String event=sound.m_45862396().toString();if(event.startsWith("minecraft:"))event=event.substring(10);if(placing()&&event.equals(placementBase.get()))return true;int x=(int)Math.floor(sound.m_25912748()),y=(int)Math.floor(sound.m_37489221()),z=(int)Math.floor(sound.m_34342839());OriginalSound recent=originalSounds.get(key(x,y,z));return recent!=null&&System.nanoTime()-recent.time<350000000L&&recent.event.equals(event);}
    public static void playPlacement(C_32594356 pos){if(!enabled)return;net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.m_20213497();C_81592558 material=soundBlock();String event=placeSound(material==null?"step.stone":material.f_13889746.m_49812403());float volume=material==null?1:(material.f_13889746.m_48828997()+1)/2,pitch=material==null?.8f:material.f_13889746.m_35890820()*.8f;C_53396390 voice=new C_70106630(new C_22168528(event),volume,pitch,pos.m_32364273()+.5f,pos.m_62825721()+.5f,pos.m_23514936()+.5f);customSound.set(true);try{mc.m_08508000().m_76360080(voice);}finally{customSound.set(false);}}
    public static String placeSound(String original){if(!enabled)return original;if(block.startsWith("modern:")){String imported=ModernSounds.sound(block);if(imported!=null)return imported;}C_81592558 chosen=soundBlock();return chosen==null?original:chosen.f_13889746.m_49812403();}
    private static C_81592558 soundBlock(){if(!block.startsWith("modern:"))return C_81592558.m_58693857(block);String b=block.toLowerCase(Locale.ROOT);int id=b.contains("glass")?20:1;if(b.contains("planks")||b.contains("wood")||b.contains("log")||b.contains("bamboo")||b.contains("stem")||b.contains("chest"))id=5;else if(b.contains("wool")||b.contains("carpet"))id=35;else if(b.contains("sand"))id=12;return C_81592558.m_64320102(id);}

}
