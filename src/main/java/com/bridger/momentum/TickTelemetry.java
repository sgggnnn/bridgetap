package com.bridger.momentum;
import java.util.ArrayDeque;

/** Raw click presses, not successful placements or Minecraft's held-use repeats. */
public final class TickTelemetry {
 private final ArrayDeque<Long> left=new ArrayDeque<>(),right=new ArrayDeque<>(),middle=new ArrayDeque<>();
 private int pressed,released,lc,rc,mc;
 public void beginTick(){pressed=released=lc=rc=mc=0;}
 public void event(int bit,boolean down){if(down)pressed|=bit;else released|=bit;}
 public void click(int button,long nanos){
  if(button==0){lc++;left.addLast(nanos);}if(button==1){rc++;right.addLast(nanos);}if(button==2){mc++;middle.addLast(nanos);}
 }
 private static int cps(ArrayDeque<Long> clicks,long now){while(!clicks.isEmpty()&&now-clicks.peekFirst()>=1000000000L)clicks.removeFirst();return clicks.size();}
 public ReplayData.TickStats snapshot(int held,double vx,double vy,double vz,long now){return new ReplayData.TickStats(held,pressed,released,lc,rc,mc,cps(left,now),cps(right,now),cps(middle,now),vx,vy,vz);}
}
