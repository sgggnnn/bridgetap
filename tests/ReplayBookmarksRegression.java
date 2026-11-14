import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
public final class ReplayBookmarksRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  Path folder=Files.createTempDirectory("replay-bookmarks-");String replay=UUID.randomUUID().toString();ReplayBookmarks marks=new ReplayBookmarks(folder,replay);
  ReplayBookmarks.Bookmark late=marks.add("Turn",60,80),early=marks.add("Flat é",3,80);check(marks.list().get(0).id.equals(early.id),"Bookmarks not sorted by tick");
  marks.rename(late.id,"Check turn");ReplayBookmarks reloaded=new ReplayBookmarks(folder,replay);check(reloaded.find(late.id).name.equals("Check turn")&&reloaded.find(early.id).tick==3,"Bookmarks lost after restart");
  check(new ReplayBookmarks(folder,UUID.randomUUID().toString()).list().isEmpty(),"Replay IDs share bookmarks");
  for(int tick:new int[]{-1,81})try{marks.add("bad",tick,80);throw new AssertionError("Out-of-range tick accepted");}catch(IllegalArgumentException good){}
  for(String name:new String[]{"","  ","a\nb\nc","a\u00a7bb"})try{marks.rename(early.id,name);throw new AssertionError("Invalid name accepted");}catch(IllegalArgumentException good){}
  check(marks.find(early.id).name.equals("Flat é"),"Failed rename modified bookmark");
  reloaded.delete(early.id);check(new ReplayBookmarks(folder,replay).find(early.id)==null,"Deleted bookmark reappears after restart");
  ReplayBookmarks many=new ReplayBookmarks(folder,UUID.randomUUID().toString());for(int i=0;i<128;i++)many.add("Tick "+i,i,128);try{many.add("Overflow",0,128);throw new AssertionError("Bookmark limit ignored");}catch(IllegalArgumentException good){}
  Path blocker=Files.createTempFile("bookmark-blocker-",".tmp");ReplayBookmarks blocked=new ReplayBookmarks(blocker.resolve("folder"),UUID.randomUUID().toString());try{blocked.add("Cannot write",0,1);throw new AssertionError("Unwritable destination accepted");}catch(java.io.IOException good){}check(blocked.list().isEmpty(),"Failed save left an in-memory bookmark");
  Path file=folder.resolve(replay+".properties");Files.write(file,Arrays.asList("version=1","tick.not-a-uuid=2","name.not-a-uuid=Bad"));try{new ReplayBookmarks(folder,replay);throw new AssertionError("Malformed metadata accepted");}catch(java.io.IOException good){}
  List<ReplayData.Frame> frames=new ArrayList<>();for(int i=0;i<=80;i++)frames.add(new ReplayData.Frame(i,64,0));ReplayData data=new ReplayData(frames,Arrays.asList(new ReplayData.Block(0,63,0,24,0,5,true)),false);ReplayTimeline timeline=new ReplayTimeline(data);timeline.pause();timeline.seek(60);check(!timeline.playing()&&timeline.pose().x==60&&timeline.blocks().size()==1,"Bookmark seeking does not reconstruct exact paused scene");timeline.seek(3);check(timeline.blocks().isEmpty(),"Bookmark rewind leaves future blocks");
  System.out.println("PASS: named bookmarks persist, sort, rename/delete, isolate recordings, enforce bounds and reconstruct exact paused scenes");
 }
}
