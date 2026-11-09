package com.bridger.momentum;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/** Client-observed placements used to qualify bridging labels. */
public class PlacementEvidence {
 private static final List<Event> events=new ArrayList<>();
 private static final class Event {
  int tick,x,z; double px,pz;
  Event(int tick,int x,int z,double px,double pz){this.tick=tick;this.x=x;this.z=z;this.px=px;this.pz=pz;}
 }
 public static void reset(){events.clear();}
 public static void record(int tick,int x,int z,double playerX,double playerZ){
  events.removeIf(e -> tick-e.tick>81);
  events.add(new Event(tick,x,z,playerX,playerZ));
 }
 public static boolean qualifies(int from,int to,double start,double end,boolean xAxis,int count){
  double distance=end-start;if(Math.abs(distance)<0.1)return false;
  Set<String> blocks=new HashSet<>();
  for(Event e:events){
   if(e.tick<from||e.tick>to)continue;
   double block=(xAxis?e.x:e.z)+0.5, player=xAxis?e.px:e.pz;
   if((block-player)*Math.signum(distance)>0.3)return false;
   blocks.add(e.x+":"+e.z);
  }
  return blocks.size()>=count;
 }
}
