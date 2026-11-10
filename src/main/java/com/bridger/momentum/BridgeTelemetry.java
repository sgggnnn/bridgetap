package com.bridger.momentum;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.input.Mouse;

/** F/B/L/R/Jump/Sneak/Sprint and physical LMB/RMB/MMB, sampled after each client tick. */
public final class BridgeTelemetry {
 private static final TickTelemetry telemetry=new TickTelemetry();
 private static long actionSequence,lastActionPress;
 private static boolean actionPressed;
 private static C_23708450[] keys(Minecraft mc){C_76792751 k=mc.f_58088711;return new C_23708450[]{k.f_15607202,k.f_46759128,k.f_20678369,k.f_25268871,k.f_94470987,k.f_89071996,k.f_50204069};}
 public static void beginTick(){telemetry.beginTick();actionPressed=false;}
 public static long actionSequence(){return actionSequence;}
 public static boolean actionPressedAfter(long time){return actionPressed&&lastActionPress>time;}
 public static void input(int code,boolean down){
  Minecraft mc=Minecraft.m_20213497();if(code==0||mc.f_28396371==null||mc.f_70816363!=null)return;
  if(down&&(code==mc.f_58088711.f_16700522.m_31350030()||code==mc.f_58088711.f_49149367.m_31350030())){actionPressed=true;lastActionPress=System.nanoTime();actionSequence++;}
  if(ReplaySession.active())return;
  C_23708450[] keys=keys(mc);for(int i=0;i<keys.length;i++)if(keys[i].m_31350030()==code)telemetry.event(1<<i,down);
  int button=code+100;if(button>=0&&button<=2){telemetry.event(1<<(7+button),down);if(down)telemetry.click(button,System.nanoTime());}
 }
 public static ReplayData.TickStats sample(C_57778754 player){
  Minecraft mc=Minecraft.m_20213497();int held=0;C_23708450[] keys=keys(mc);
  for(int i=0;i<keys.length;i++)if(keys[i].m_70603120())held|=1<<i;
  for(int i=0;i<3;i++)if(Mouse.isButtonDown(i))held|=1<<(7+i);
  return telemetry.snapshot(held,player.f_24826402,player.f_35148123,player.f_21201587,System.nanoTime());
 }
}
