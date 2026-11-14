package com.bridger.momentum;
import java.util.*;

/** Diagnose the first unfulfilled click after the last placement using recorded view rays. */
public final class ReplayAimAnalysis {
 private ReplayAimAnalysis(){}
 public static String colored(String text){String lower=text.toLowerCase(Locale.ROOT);String color=lower.contains("too high")||lower.contains("top face")?"a":lower.contains("too low")||lower.contains("bottom face")?"c":lower.contains("sideways")?"e":lower.contains("out of reach")||lower.contains("holding a block")||lower.contains("empty hand")?"c":"7";return "\u00a7"+color+text;}

 public static String summary(ReplayData data){
  if(data==null||data.frames.isEmpty())return "Not enough data.";
  if(data.truncated)return "Incomplete recording.";
  ReplayData.Block support=null;int placed=-1;
  for(ReplayData.Block b:data.blocks)if(b.placed&&b.id>0&&ReplayTimeline.eventTick(data,b)>=placed){placed=ReplayTimeline.eventTick(data,b);support=b;}
  if(support==null)return "No support block recorded.";
  boolean telemetry=false,observedInput=false;for(int i=Math.max(0,placed+1);i<data.frames.size();i++){
   ReplayData.Frame frame=data.frames.get(i);if(frame.stats==null)continue;telemetry=true;int tick=ReplayTimeline.eventFrameTick(data,i);
   boolean clicked=frame.stats.rightClicks>0,held=(frame.stats.held&256)!=0;observedInput|=clicked||held;
   if(tick<=placed||!clicked&&!(held&&tick>=placed+4)||i+3>=data.frames.size())continue;
   // Ignore clicks made after the fall is already well underway.
   if(frame.y<support.y-1.5)break;
   if(frame.itemId==0)return "Empty hand.";
   return aim(frame,support);
  }
  return observedInput?"Click recorded; cause uncertain.":telemetry?"No right-click after last block.":"No click data.";
 }
 static String aim(ReplayData.Frame f,ReplayData.Block b){
  double eye=f.y+(f.sneaking?1.54:1.62),yaw=Math.toRadians(f.yaw),pitch=Math.toRadians(f.pitch),dx=-Math.sin(yaw),dz=Math.cos(yaw);
  double distance=0;double[] p={f.x,eye,f.z},low={b.x,b.y,b.z},high={b.x+1.,b.y+1.,b.z+1.};for(int a=0;a<3;a++){double d=Math.max(low[a]-p[a],Math.max(0,p[a]-high[a]));distance+=d*d;}
  if(distance>25)return "Support out of reach (>5b).";
  double enter=0,exit=Double.POSITIVE_INFINITY;double[] origin={f.x,f.z},dir={dx,dz},min={b.x,b.z},max={b.x+1.,b.z+1.};
  for(int a=0;a<2;a++){if(Math.abs(dir[a])<1e-8){if(origin[a]<min[a]||origin[a]>max[a])return "Likely sideways aim.";}else{double a0=(min[a]-origin[a])/dir[a],a1=(max[a]-origin[a])/dir[a];enter=Math.max(enter,Math.min(a0,a1));exit=Math.min(exit,Math.max(a0,a1));}}
  if(exit<enter||exit<0)return "Likely sideways aim.";
  double y0=eye-enter*Math.tan(pitch),y1=eye-exit*Math.tan(pitch);
  if(Math.min(y0,y1)>b.y+1.02)return "Likely aim too high.";
  if(Math.max(y0,y1)<b.y-.02)return "Likely aim too low.";
  if(y0>b.y+1.02)return "Top face hit.";if(y0<b.y-.02)return "Bottom face hit.";
  return "Cause uncertain.";
 }
}
