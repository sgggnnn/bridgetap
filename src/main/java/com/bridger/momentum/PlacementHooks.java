package com.bridger.momentum;
import net.minecraft.unmapped.*;
public class PlacementHooks {
 private static C_81592558 beforeHit,beforeAdjacent;
 private static C_32594356 hit,adjacent;
 public static void capture(C_34399650 world,C_32594356 pos,C_28786548 face){
  if(world==null||pos==null||face==null){hit=null;return;}
  hit=pos;adjacent=pos.m_39481470(face);
  beforeHit=world.m_77852500(hit).m_41958956();beforeAdjacent=world.m_77852500(adjacent).m_41958956();
 }
 public static void finish(C_34399650 world,C_57778754 player){
  if(ReplaySession.active()){hit=null;return;}
  if(world==null||player==null||hit==null)return;
  C_81592558 after=world.m_77852500(hit).m_41958956(),afterAdjacent=world.m_77852500(adjacent).m_41958956();
  C_32594356 placed=null;
  if(after!=beforeHit && after!=null && C_81592558.m_63461719(after)!=0)placed=hit;
  else if(afterAdjacent!=beforeAdjacent && afterAdjacent!=null && C_81592558.m_63461719(afterAdjacent)!=0)placed=adjacent;
  hit=null;
  if(placed!=null){
   PlacementAppearance.track(placed,C_81592558.m_63461719(world.m_77852500(placed).m_41958956()));
   IslandReplacement.allowPlacement(placed.m_32364273(),placed.m_62825721(),placed.m_23514936());
   if(!BridgeTracker.isBridging())return;
   PlacementEvidence.record(BridgeTracker.bridgetap$getTick(),placed.m_32364273(),placed.m_23514936(),player.f_78826675,player.f_97691941);
   ReplayRecorder.placed(world,placed);
  }
 }
}
