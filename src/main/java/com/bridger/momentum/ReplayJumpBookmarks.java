package com.bridger.momentum;
import java.util.*;

/** Detect takeoff boundaries, not repeated jump-key presses while airborne. */
public final class ReplayJumpBookmarks {
 private ReplayJumpBookmarks(){}
 public static List<ReplayBookmarks.Bookmark> detect(ReplayData data){
  List<ReplayBookmarks.Bookmark> result=new ArrayList<>();if(data==null)return result;
  for(int i=1;i<data.frames.size();i++){
   ReplayData.Frame a=data.frames.get(i-1),b=data.frames.get(i);double rise=b.y-a.y;
   if(!a.onGround||b.onGround||rise<=.001||rise>.65||Math.abs(b.x-a.x)>2||Math.abs(b.z-a.z)>2)continue;
   boolean telemetry=a.stats!=null||b.stats!=null;
   if(telemetry&&!jump(a.stats)&&!jump(b.stats))continue;
   // Older recordings without telemetry need an ordinary jump-sized rise.
   if(!telemetry&&rise<.2)continue;
   result.add(ReplayBookmarks.Bookmark.jump(result.size()+1,i-1));
  }
  return Collections.unmodifiableList(result);
 }
 private static boolean jump(ReplayData.TickStats s){return s!=null&&((s.held|s.pressed)&16)!=0;}
}
