package com.bridger.momentum;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public final class ReplayRecorder {
 private static final List<ReplayData.Frame> frames=new ArrayList<>();
 private static final List<ReplayData.Block> blocks=new ArrayList<>();
 private static final Set<Long> scanned=new HashSet<>();
 private static final Map<Long,Integer> states=new HashMap<>();
 private static final Set<Long> placements=new HashSet<>();
 private static boolean truncated;
 private static int lastX=Integer.MIN_VALUE,lastY,lastZ,dimension;
 private static int lastFrameTick=Integer.MIN_VALUE;
 private static long dayTime;
 private static ReplayData.Identity identity=ReplayData.Identity.unknown();
 public static void reset(){frames.clear();blocks.clear();scanned.clear();states.clear();placements.clear();truncated=false;lastX=Integer.MIN_VALUE;lastFrameTick=Integer.MIN_VALUE;}
 public static void start(){
  reset();Minecraft mc=Minecraft.m_20213497();C_57778754 p=mc.f_28396371;
  GameProfile profile=p.m_37763343();String value="",signature="";
  C_97147141 info=mc.m_55181026().m_00890679(profile.getId());
  Collection<Property> textures=(info==null?profile:info.m_15269411()).getProperties().get("textures");
  if(!textures.isEmpty()){Property texture=textures.iterator().next();value=texture.getValue();signature=texture.hasSignature()?texture.getSignature():"";}
  int parts=0;for(C_88812351 part:C_88812351.values())if(p.m_05104748(part))parts|=part.m_23895741();
  identity=new ReplayData.Identity(profile.getId().toString(),profile.getName(),p.m_33641726().m_42269114(),value,signature,p.m_63406370(),parts);
  dimension=p.f_11819711;dayTime=mc.f_26407825.m_08839739();sample();
 }
 public static void sample(){
  Minecraft mc=Minecraft.m_20213497();
  if(ReplaySession.active()||mc==null||mc.f_28396371==null||mc.f_26407825==null)return;
  int tick=BridgeTracker.bridgetap$getTick();boolean replace=tick==lastFrameTick;
  if(!replace&&frames.size()>=ReplayData.MAX_FRAMES){truncated=true;return;}
  C_57778754 p=mc.f_28396371;C_88708284 held=p.f_29439708.m_27760697();
  ReplayData.Frame frame=new ReplayData.Frame(p.f_78826675,p.f_31030949,p.f_97691941,p.f_80130373,p.f_03549461,p.f_50412996,p.f_21972499,p.m_40164672(),p.f_47825615,p.f_19026405,p.f_18020014,p.f_00715684,held==null?0:C_98888256.m_18004261(held.m_23235460()),held==null?0:held.m_30227787(),BridgeTelemetry.sample(p));
  if(replace)frames.set(frames.size()-1,frame);else frames.add(frame);lastFrameTick=tick;
  int x=(int)Math.floor(p.f_78826675),y=(int)Math.floor(p.f_31030949),z=(int)Math.floor(p.f_97691941);
  if(x!=lastX||y!=lastY||z!=lastZ){
   int radius=frames.size()==1?12:8;
   for(int bx=x-radius;bx<=x+radius;bx++)for(int bz=z-radius;bz<=z+radius;bz++)for(int by=Math.max(0,y-24);by<=Math.min(255,y+12);by++){
    C_32594356 pos=new C_32594356(bx,by,bz);long key=pos.m_14165896();
    if(scanned.size()>=400000){truncated=true;break;}
    if(!scanned.add(key)||states.containsKey(key))continue;
    C_15815210 state=mc.f_26407825.m_77852500(pos);
    if(C_81592558.m_63461719(state.m_41958956())!=0)add(pos,state,0,false);
   }
   lastX=x;lastY=y;lastZ=z;
  }
 }
 private static void add(C_32594356 pos,C_15815210 state,int tick,boolean placed){
  if(blocks.size()>=ReplayData.MAX_BLOCKS){truncated=true;return;}
  C_81592558 block=state.m_41958956();int id=C_81592558.m_63461719(block),metadata=block.m_14593609(state),combined=id<<4|metadata;
  Integer previous=states.put(pos.m_14165896(),combined);
  boolean firstPlacement=placed&&placements.add(pos.m_14165896());
  if(previous!=null&&previous==combined&&!firstPlacement)return;
  blocks.add(new ReplayData.Block(pos.m_32364273(),pos.m_62825721(),pos.m_23514936(),id,metadata,tick,placed));
 }
 public static void placed(C_34399650 world,C_32594356 pos){
  if(frames.isEmpty()||ReplaySession.active())return;
  int tick=BridgeTracker.bridgetap$getTick()==lastFrameTick?frames.size()-1:frames.size();
  if(tick>=ReplayData.MAX_FRAMES){truncated=true;return;}
  // Mark the placement before scanning this tick so it cannot become a tick-0 background block.
  add(pos,world.m_77852500(pos),tick,true);sample();
 }
 public static ReplayData snapshot(){return frames.isEmpty()?null:new ReplayData(frames,blocks,truncated,identity,dimension,dayTime);}
}
