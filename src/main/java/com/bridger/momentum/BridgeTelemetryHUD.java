package com.bridger.momentum;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.GL11;

/** Replay-only telemetry presentation. Replay metrics always come from one recorded tick. */
public final class BridgeTelemetryHUD {
 public static String coordinates(double x,double y,double z,String label){return String.format(Locale.US,"%s%s XYZ: \u00a7f%.4f  %.4f  %.4f",BridgeConfig.color(1),label,x,y,z);}
 private static final String[] INPUTS={"Fwd","Back","Left","Right","Jump","Sneak","Sprint","LMB","RMB","MMB"};
 private static String names(int mask,boolean showAll){
  StringBuilder text=new StringBuilder();
  for(int i=0;i<INPUTS.length;i++)if(showAll||(mask&(1<<i))!=0){
   if(text.length()>0)text.append(' ');text.append((mask&(1<<i))!=0?"\u00a7a":"\u00a78").append(INPUTS[i]);
  }
  return text.length()==0?"\u00a78-":text.toString();
 }
 public static List<String> lines(double x,double y,double z,ReplayData.TickStats s,String label){
  List<String> lines=new ArrayList<>();
  lines.add(coordinates(x,y,z,label));
  if(s==null){lines.add("\u00a77Inputs / clicks / velocity unavailable in this old recording");return lines;}
  lines.add("\u00a77Inputs: "+names(s.held,true));
  lines.add("\u00a77Pressed: "+names(s.pressed,false)+" \u00a77| Released: "+names(s.released,false));
  lines.add(String.format(Locale.US,"\u00a77Clicks L/R/M: \u00a7f%d/%d/%d \u00a77| CPS (1s): \u00a7f%d/%d/%d",s.leftClicks,s.rightClicks,s.middleClicks,s.leftCps,s.rightCps,s.middleCps));
  lines.add(String.format(Locale.US,"\u00a77Velocity b/t: \u00a7fX %.4f Y %.4f Z %.4f \u00a77| H %.4f",s.vx,s.vy,s.vz,Math.hypot(s.vx,s.vz)));
  return lines;
 }
 public static void draw(List<String> lines){
  Minecraft mc=Minecraft.m_20213497();C_23014920 fr=mc.f_21497584;C_43718703 window=new C_43718703(mc);int width=0;
  for(String line:lines)width=Math.max(width,fr.m_09235706(line));
  float scale=Math.min(1f,(window.m_39119333()-12f)/Math.max(1,width));
  int lineHeight=fr.f_30706543+2;
  GL11.glPushMatrix();GL11.glTranslatef(window.m_39119333()/2f,window.m_16103615()-88-lines.size()*lineHeight*scale,0);GL11.glScalef(scale,scale,1);
  for(int i=0;i<lines.size();i++)fr.m_11445413(lines.get(i),-fr.m_09235706(lines.get(i))/2,i*lineHeight,0xFFFFFF,BridgeConfig.textShadow);
  GL11.glPopMatrix();
 }
}
