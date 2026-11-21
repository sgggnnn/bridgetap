package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Deferred appearance changes after completed attempts, on the client tick. */
public final class AttemptRandomizer {
 private static final Path FILE=Paths.get("config","bridgetap","attempt-randomizer.properties");
 private static boolean blocks,islands,reapply;private static String kind="defaults",search="";
 private static IslandTemplate rememberedIsland;private static Path rememberedFile;
 private static final AttemptSchedule schedule=new AttemptSchedule();private static String scheduleServer="",status="";
 private static List<Path> islandFiles=Collections.emptyList();private static List<String> blockKeys=Collections.emptyList();
 private static final Map<Path,IslandTemplate> templates=new LinkedHashMap<Path,IslandTemplate>(16,.75f,true){protected boolean removeEldestEntry(Map.Entry<Path,IslandTemplate> e){return size()>8;}};
 private static final Random RANDOM=new Random();
 static{try(InputStream in=Files.newInputStream(FILE)){Properties p=new Properties();p.load(in);blocks=Boolean.parseBoolean(p.getProperty("blocks","false"));islands=Boolean.parseBoolean(p.getProperty("islands","false"));reapply=Boolean.parseBoolean(p.getProperty("reapply","false"));kind=p.getProperty("islandLibrary","defaults");if(!kind.equals("saved"))kind="defaults";search=p.getProperty("blockSearch","");}catch(IOException ignored){}}
 private AttemptRandomizer(){}
 public static boolean blocks(){return blocks;}public static boolean islands(){return islands;}public static boolean reapply(){return reapply;}public static String status(){return status;}
 private static void save()throws IOException{Properties p=new Properties();p.setProperty("blocks",String.valueOf(blocks));p.setProperty("islands",String.valueOf(islands));p.setProperty("reapply",String.valueOf(reapply));p.setProperty("islandLibrary",kind);p.setProperty("blockSearch",search);Files.createDirectories(FILE.getParent());try(OutputStream out=Files.newOutputStream(FILE)){p.store(out,"Random appearances after completed attempts");}}
 public static void toggleBlocks(String filter)throws IOException{blocks=!blocks;search=filter;blockKeys=Collections.emptyList();save();status=blocks?"Random block changes after each completed attempt.":"Random block disabled.";}
 public static void toggleIslands(String library)throws IOException{islands=!islands;kind=library.equals("saved")?"saved":"defaults";islandFiles=Collections.emptyList();templates.clear();save();status=islands?"Random island changes after each completed attempt.":"Random island disabled.";}
 public static void toggleReapply()throws IOException{
  reapply=!reapply;schedule.resetAuto();
  if(reapply&&IslandOverlay.enabled())placementConfirmed(IslandOverlay.activeTemplate(),IslandOverlay.libraryPath(),IslandOverlay.layout());
  save();status=reapply?"Reapply after each completed attempt only when no island is displayed.":"Automatic reapply disabled.";
 }
 /** Called once after a confirmed run finishes, never on a placement. */
 public static void completed(String id){
  String server=PresetServers.canonical(ServerPresets.address());
  if(!server.equals(scheduleServer)){schedule.reset();scheduleServer=server;}
  schedule.completed(id,blocks,islands,reapply);
 }
 public static void placementConfirmed(IslandTemplate template,Path file,IslandLayout ignored){rememberedIsland=template;rememberedFile=file;}
 public static void rememberWorldChange(IslandTemplate template,Path file,IslandLayout ignored){if(reapply){rememberedIsland=template;rememberedFile=file;}}
 public static void forgetIsland(){rememberedIsland=null;rememberedFile=null;}
 public static void reapplySaved(){
  if(!reapply||IslandOverlay.enabled())return;
  IslandTemplate template=IslandOverlay.enabled()?IslandOverlay.activeTemplate():rememberedIsland;
  Path file=IslandOverlay.enabled()?IslandOverlay.libraryPath():rememberedFile;
  if(template==null)throw new IllegalArgumentException("Place an island first");
  if(!IslandOverlay.applyAutomatically(template,file))throw new IllegalArgumentException(IslandOverlay.autoStatus());
 }
 public static void tick(Minecraft mc){
  if(mc==null||mc.f_26407825==null||mc.f_28396371==null||!BridgeConfig.modEnabled||ReplaySession.active())return;
  if(!scheduleServer.isEmpty()&&!scheduleServer.equals(PresetServers.canonical(ServerPresets.address()))){schedule.reset();scheduleServer="";return;}
  if(!schedule.pending()||BridgeTracker.isBridging()||IslandOverlay.previewing()||mc.f_70816363!=null)return;
  C_57778754 p=mc.f_28396371;
  if(!p.f_47825615||BridgeTracker.isPlayerOnFinishPlate(mc))return;
  IslandReplacement.Mask platform=IslandReplacement.captureStartAt((int)Math.floor(p.f_78826675),(int)Math.floor(p.f_31030949)-1,(int)Math.floor(p.f_97691941));
  if(platform.empty()||IslandReplacement.isFinishIsland(platform))return;
  if(schedule.blocksPending()){schedule.consumeBlocks();if(blocks)try{randomBlock();}catch(Exception ex){status="Random block: "+ex.getMessage();}}
  if(schedule.islandPending()){schedule.consumeIsland();if(islands)try{randomIsland();schedule.consumeReapply();}catch(Exception ex){status="Random island: "+ex.getMessage();}}
  if(schedule.reapplyPending()){schedule.consumeReapply();if(reapply&&!IslandOverlay.enabled())try{reapplySaved();status="Missing island automatically applied after the completed attempt.";}catch(Exception ex){status="Auto reapply: "+ex.getMessage();}}
 }
 private static void randomBlock()throws IOException{
  if(blockKeys.isEmpty()){List<String> keys=new ArrayList<>();for(int i=1;i<4096;i++){C_81592558 b=C_81592558.m_64320102(i);if(b!=null&&C_81592558.m_63461719(b)==i)keys.add(C_81592558.f_83036452.m_55287479(b).toString());}keys.addAll(ModernAppearance.blocks());keys.removeIf(k->!k.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)));blockKeys=keys;}
  List<String> choices=new ArrayList<>(blockKeys);choices.remove(PlacementAppearance.block());Collections.shuffle(choices,RANDOM);for(String key:choices)try{if(key.startsWith("modern:")&&!ModernAppearance.readyForRandom(key))continue;PlacementAppearance.applyAttempt(key);return;}catch(IOException unavailable){}throw new IOException("No other available blocks match the search");
 }
 private static void randomIsland()throws IOException{
  if(islandFiles.isEmpty())islandFiles=IslandLibrary.list(kind);List<Path> choices=new ArrayList<>(islandFiles);choices.removeIf(IslandOverlay::isFrom);Collections.shuffle(choices,RANDOM);
  for(Path file:choices){IslandTemplate template;try{template=templates.get(file);if(template==null){template=IslandTemplate.read(file);templates.put(file,template);}}catch(IOException unreadable){continue;}
   try{ModernAppearance.prepareIsland(IslandMaterials.palette(template));}catch(IOException unavailable){continue;}
   if(!IslandOverlay.applyAutomatically(template,file))throw new IOException(IslandOverlay.autoStatus());return;
  }throw new IOException("No other readable islands in this library");
 }
}
