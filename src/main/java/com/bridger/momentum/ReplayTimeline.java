package com.bridger.momentum;
import java.util.*;

/** Pure playback state: seeking reconstructs the environment as well as the actor. */
public final class ReplayTimeline {
 private final ReplayData data;
 private double tick,speed=1;
 private boolean playing=true;
 public ReplayTimeline(ReplayData data){this.data=data;}
 public double tick(){return tick;}
 public double seconds(){return tick/20;}
 public double duration(){return (data.frames.size()-1)/20.0;}
 public boolean playing(){return playing;}
 public void pause(){playing=false;}
 public double speed(){return speed;}
 public void advance(double seconds){if(playing){seek(tick+Math.max(0,seconds)*20*speed);if(tick>=data.frames.size()-1)playing=false;}}
 public void seek(double tick){this.tick=Math.max(0,Math.min(data.frames.size()-1,tick));}
 public void toggle(){if(!playing&&tick>=data.frames.size()-1)seek(0);playing=!playing;}
 public void restart(){tick=0;playing=true;}
 public void cycleSpeed(){double[] speeds={.25,.5,1,2,4};for(int i=0;i<speeds.length;i++)if(speed==speeds[i]){speed=speeds[(i+1)%speeds.length];return;}}
 public ReplayData.Frame pose(){
  int i=(int)tick;ReplayData.Frame a=data.frames.get(i),b=data.frames.get(Math.min(i+1,data.frames.size()-1));double t=tick-i;
  return new ReplayData.Frame(lerp(a.x,b.x,t),lerp(a.y,b.y,t),lerp(a.z,b.z,t),angle(a.yaw,b.yaw,t),(float)lerp(a.pitch,b.pitch,t),angle(a.headYaw,b.headYaw,t),angle(a.bodyYaw,b.bodyYaw,t),a.sneaking,a.onGround,(float)lerp(a.swing,b.swing< a.swing?b.swing+1:b.swing,t)%1,(float)lerp(a.walkSpeed,b.walkSpeed,t),(float)lerp(a.walkProgress,b.walkProgress,t),a.itemId,a.itemDamage);
 }
 private static double lerp(double a,double b,double t){return a+(b-a)*t;}
 private static float angle(float a,float b,double t){double d=(b-a)%360;if(d>180)d-=360;if(d< -180)d+=360;return (float)(a+d*t);}
 public Map<Long,ReplayData.Block> blocks(){
  Map<Long,ReplayData.Block> result=new LinkedHashMap<>();
  for(ReplayData.Block b:data.blocks)if(b.tick<=tick)result.put(b.key(),b);
  return result;
 }
}
