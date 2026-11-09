package com.bridger.momentum;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

public final class ReplayRecorder {
 private static final List<ReplayData.Frame> frames=new ArrayList<>();
 private static final List<ReplayData.Block> blocks=new ArrayList<>();
 private static final Map<Long,Integer> seen=new HashMap<>();
 private static boolean truncated;
 private static int lastX=Integer.MIN_VALUE,lastY,lastZ;
 public static void reset(){frames.clear();blocks.clear();seen.clear();truncated=false;lastX=Integer.MIN_VALUE;}
 public static void start(){reset();sample();}
 public static void sample(){
  Minecraft mc=Minecraft.m_20213497();
  if(mc==null||mc.f_28396371==null||mc.f_26407825==null)return;
  if(frames.size()>=ReplayData.MAX_FRAMES){truncated=true;return;}
  C_57778754 p=mc.f_28396371;
  frames.add(new ReplayData.Frame(p.f_78826675,p.f_31030949,p.f_97691941));
  int x=(int)Math.floor(p.f_78826675),y=(int)Math.floor(p.f_31030949),z=(int)Math.floor(p.f_97691941);
  if(x!=lastX||y!=lastY||z!=lastZ){
   int radius=frames.size()==1?6:3;
   for(int bx=x-radius;bx<=x+radius;bx++)for(int bz=z-radius;bz<=z+radius;bz++)for(int by=Math.max(0,y-7);by<=Math.min(255,y+3);by++){
    C_32594356 pos=new C_32594356(bx,by,bz);long key=pos.m_14165896();
    if(seen.containsKey(key))continue;
    int id=C_81592558.m_63461719(mc.f_26407825.m_77852500(pos).m_41958956());
    if(id!=0)add(pos,id,0);
   }
   lastX=x;lastY=y;lastZ=z;
  }
 }
 private static void add(C_32594356 pos,int id,int tick){
  if(blocks.size()>=ReplayData.MAX_BLOCKS){truncated=true;return;}
  Integer previous=seen.put(pos.m_14165896(),id);
  if(previous!=null&&previous==id)return;
  blocks.add(new ReplayData.Block(pos.m_32364273(),pos.m_62825721(),pos.m_23514936(),id,tick));
 }
 public static void placed(C_34399650 world,C_32594356 pos){
  if(frames.isEmpty())return;
  add(pos,C_81592558.m_63461719(world.m_77852500(pos).m_41958956()),frames.size()-1);
 }
 public static ReplayData snapshot(){return frames.isEmpty()?null:new ReplayData(frames,blocks,truncated);}
}
