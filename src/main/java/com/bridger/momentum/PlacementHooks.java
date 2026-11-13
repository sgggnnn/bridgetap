package com.bridger.momentum;
import net.minecraft.unmapped.*;
public class PlacementHooks {
 private static C_81592558 beforeHit,beforeAdjacent;
 private static C_32594356 hit,adjacent;private static C_28786548 facing;
 public static void capture(C_34399650 world,C_32594356 pos,C_28786548 face,C_88708284 stack){
  if(world==null||pos==null||face==null){hit=null;return;}
  facing=face;hit=new C_32594356(pos.m_32364273(),pos.m_62825721(),pos.m_23514936());adjacent=hit.m_39481470(face);
  beforeHit=world.m_77852500(hit).m_41958956();beforeAdjacent=world.m_77852500(adjacent).m_41958956();
  if(stack!=null&&stack.m_23235460() instanceof C_71827890)PlacementAppearance.reserve(hit,C_81592558.m_63461719(beforeHit),adjacent,C_81592558.m_63461719(beforeAdjacent),C_81592558.m_63461719(((C_71827890)stack.m_23235460()).m_96251309()));
 }
 public static void finish(C_34399650 world,C_57778754 player){
  if(ReplaySession.active()){hit=null;return;}
  if(world==null||player==null||hit==null)return;
  C_81592558 after=world.m_77852500(hit).m_41958956(),afterAdjacent=world.m_77852500(adjacent).m_41958956();
  C_32594356 placed=null;
  if(after!=beforeHit && after!=null && C_81592558.m_63461719(after)!=0)placed=hit;
  else if(afterAdjacent!=beforeAdjacent && afterAdjacent!=null && C_81592558.m_63461719(afterAdjacent)!=0)placed=adjacent;
  if(placed!=null)ReplayRecorder.modeAnchor(world,hit,facing);
  hit=null;
  if(placed!=null){
   PlacementAppearance.track(placed,C_81592558.m_63461719(world.m_77852500(placed).m_41958956()));
   IslandReplacement.allowPlacement(placed.m_32364273(),placed.m_62825721(),placed.m_23514936());
   if(PlacementAppearance.enabled())PlacementAppearance.playPlacement(placed);
   if(!BridgeTracker.isBridging())return;
   PlacementEvidence.record(BridgeTracker.bridgetap$getTick(),placed.m_32364273(),placed.m_23514936(),player.f_78826675,player.f_97691941);
   ReplayRecorder.placed(world,placed);
  }
 }
}
