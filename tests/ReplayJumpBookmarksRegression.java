import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
public final class ReplayJumpBookmarksRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static ReplayData.Frame frame(double y,boolean ground,boolean jump){return new ReplayData.Frame(0,y,0,0,0,0,0,false,ground,0,0,0,0,0,new ReplayData.TickStats(jump?16:0,0,0,0,0,0,0,0,0,0,0,0));}
 static ReplayData data(ReplayData.Frame...frames){return new ReplayData(Arrays.asList(frames),Collections.emptyList(),false);}
 public static void main(String[] args)throws Exception{
  ReplayData replay=data(frame(64,true,true),frame(64.42,false,true),frame(64.75,false,true),frame(64,true,true),frame(64.42,false,true),frame(64,false,true),frame(64,true,false),frame(63.9,false,false));
  List<ReplayBookmarks.Bookmark> jumps=ReplayJumpBookmarks.detect(replay);check(jumps.size()==2&&jumps.get(0).tick==0&&jumps.get(1).tick==3,"Held jump duplicates markers, falling creates a jump, or takeoff boundary is off by one");check(jumps.get(1).name.equals("Jump #2")&&jumps.get(1).automatic,"Automatic marker label/type missing");
  check(ReplayJumpBookmarks.detect(data(frame(64,true,false),frame(64.42,false,false))).isEmpty(),"Upward motion without jump input misclassified");check(ReplayJumpBookmarks.detect(data(frame(64,true,true),frame(64.5,true,true))).isEmpty(),"Walking up a step misclassified");check(ReplayJumpBookmarks.detect(data(frame(64,true,true),frame(70,false,true))).isEmpty(),"Teleport misclassified");check(ReplayJumpBookmarks.detect(data(frame(64,true,true),frame(64.1,false,true))).size()==1,"Low-ceiling takeoff missed");
  ReplayData.Frame oldGround=new ReplayData.Frame(0,64,0,0,0,0,0,false,true,0,0,0,0,0),oldAir=new ReplayData.Frame(0,64.42,0,0,0,0,0,false,false,0,0,0,0,0);check(ReplayJumpBookmarks.detect(data(oldGround,oldAir)).size()==1,"Old recording without telemetry lost a grounded takeoff");check(ReplayJumpBookmarks.detect(data(oldAir,oldAir)).isEmpty(),"Invented initial jump in an incomplete recording");
  Path folder=Files.createTempDirectory("auto-jump-bookmarks-");String id=UUID.randomUUID().toString();ReplayBookmarks marks=new ReplayBookmarks(folder,id);ReplayBookmarks.Bookmark manual=marks.add("Inspect",2,10);marks.automatic(jumps);List<ReplayBookmarks.Bookmark> first=marks.list();check(first.size()==3&&first.get(1)==manual&&marks.manualCount()==1,"Automatic/manual lists not merged by tick");check(first==marks.list(),"Painting re-sorts bookmark list");marks.automatic(jumps);check(marks.list().size()==3,"Reopening duplicated automatic markers");try{marks.delete(jumps.get(0).id);throw new AssertionError("Automatic marker treated as manual");}catch(IllegalArgumentException good){}check(new ReplayBookmarks(folder,id).list().size()==1,"Automatic jump markers written as manual sidecars");
  ReplayTimeline timeline=new ReplayTimeline(replay);timeline.pause();timeline.seek(jumps.get(1).tick);check(timeline.tick()==3&&!timeline.playing()&&timeline.sample().onGround,"Bookmark seek does not reach exact takeoff boundary");
  System.out.println("PASS: automatic takeoff markers, held-jump deduplication, old recordings, fall/step/teleport rejection, exact paused seeking and cached manual/auto merging");
 }
}
