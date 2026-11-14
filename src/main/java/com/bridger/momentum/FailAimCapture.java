package com.bridger.momentum;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Capture the pose at Minecraft's use action, rather than a later replay tick. */
public final class FailAimCapture {
 private static final FailAttemptEvidence evidence=new FailAttemptEvidence();private static Object world;
 private FailAimCapture(){}
 public static void reset(){world=null;evidence.clear();}
 public static void placed(Object current,C_32594356 pos){if(world!=current){evidence.clear();world=current;}evidence.placed(pos.m_32364273(),pos.m_62825721(),pos.m_23514936());}
 public static void use(Minecraft mc){if(!BridgeConfig.modEnabled||ReplaySession.active()||!BridgeTracker.isBridging()||mc==null||mc.f_26407825!=world||mc.f_28396371==null||mc.f_70816363!=null)return;C_57778754 p=mc.f_28396371;C_88708284 stack=p.f_29439708.m_27760697();boolean block=stack!=null&&stack.m_23235460() instanceof C_71827890;evidence.use(new ReplayData.Frame(p.f_78826675,p.f_31030949,p.f_97691941,p.f_80130373,p.f_03549461,p.f_50412996,p.f_21972499,p.m_40164672(),p.f_47825615,0,0,0,stack==null?0:C_98888256.m_18004261(stack.m_23235460()),0),block);}
 public static void target(C_34399650 current,C_32594356 target,C_28786548 face){if(BridgeConfig.modEnabled&&!ReplaySession.active()&&BridgeTracker.isBridging()&&current==world)evidence.target(target.m_32364273(),target.m_62825721(),target.m_23514936(),face.m_29389506(),BridgeGeometry.surface(C_81592558.m_63461719(current.m_77852500(target).m_41958956())));}
 public static void result(boolean accepted){if(!ReplaySession.active()&&BridgeTracker.isBridging())evidence.result(accepted);}
 public static String summary(){return evidence.summary();}
}
