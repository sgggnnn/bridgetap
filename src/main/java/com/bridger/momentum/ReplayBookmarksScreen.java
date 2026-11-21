package com.bridger.momentum;
import java.nio.file.Paths;
import java.util.*;
import net.minecraft.unmapped.C_85740840;

public final class ReplayBookmarksScreen extends BridgeScreen {
 private static final java.util.concurrent.ExecutorService WORKER=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"BridgeTap jump bookmarks");t.setDaemon(true);return t;});
 private java.util.concurrent.CompletableFuture<List<ReplayBookmarks.Bookmark>> jumps;private int automaticCount;
 private final C_85740840 parent;private final RunHistory.Run run;private ReplayBookmarks bookmarks;private String selected="",name="",message="";private int page;
 @Override protected int canvasHeight(){return 620;}
 public ReplayBookmarksScreen(C_85740840 parent,RunHistory.Run run){this.parent=parent;this.run=run;try{bookmarks=new ReplayBookmarks(Paths.get("config/bridgetap/replay-bookmarks"),run.id);ReplayData active=watching()?ReplaySession.recording():run.replay;jumps=java.util.concurrent.CompletableFuture.supplyAsync(()->{try{return ReplayJumpBookmarks.detect(active!=null?active:BridgeTracker.history().replay(run));}catch(Exception ex){throw new java.util.concurrent.CompletionException(ex);}},WORKER);}catch(Exception ex){message=ex.getMessage();}}
 private boolean watching(){return run.id.equals(ReplaySession.recordingId());}
 private interface Work{void run()throws Exception;}
 private void action(Work work){try{work.run();}catch(Exception ex){message=ex.getMessage();}}
 private static String time(int tick){return String.format(Locale.US,"%.2fs / Tick %d",tick/20.0,tick);}
 private void go(ReplayBookmarks.Bookmark picked)throws Exception{if(!watching()){if(BridgeTracker.isBridging())throw new IllegalArgumentException("Finish your bridge first");ReplayData data=BridgeTracker.history().replay(run);if(data==null||!ReplaySession.start(run,data))throw new IllegalArgumentException("Join a world to watch this replay");}ReplaySession.inspectTick(picked.tick);open(null);}
 @Override protected void paint(){header("Replay bookmarks","Automatic jumps / Named moments / Exact-tick navigation");button(watching()?"Return to replay":"Back",710,15,130,this::back);
  if(!watching())button("Watch replay",550,15,148,()->action(()->{if(BridgeTracker.isBridging())throw new IllegalArgumentException("Finish your bridge first");ReplayData recording=BridgeTracker.history().replay(run);if(recording==null||!ReplaySession.start(run,recording))throw new IllegalArgumentException("Join a world to watch this replay");ReplaySession.inspectTick(0);open(new ReplayBookmarksScreen(null,run));}));
  panel(20,74,450,364);panel(486,74,354,364);clipped(run.displayName(),36,90,420,ACCENT);
  if(bookmarks==null){footer(message);return;}if(jumps!=null&&jumps.isDone()){try{List<ReplayBookmarks.Bookmark> found=jumps.join();bookmarks.automatic(found);automaticCount=found.size();}catch(Exception ex){message="Could not read automatic jumps. Manual bookmarks remain available.";}finally{jumps=null;}}List<ReplayBookmarks.Bookmark> rows=bookmarks.list();page=Math.min(page,Math.max(0,(rows.size()-1)/8));
  if(rows.isEmpty())label("No bookmarks yet. Add one while watching this replay.",36,145);
  for(int i=page*8;i<Math.min(rows.size(),page*8+8);i++){ReplayBookmarks.Bookmark b=rows.get(i);int y=122+(i-page*8)*33;button((b.automatic?"Auto: ":"")+b.name,36,y,220,27,true,b.id.equals(selected),()->{selected=b.id;name=b.name;});clipped(time(b.tick),270,y+10,126,MUTED);button("Go",404,y,50,27,true,true,()->action(()->go(b)));}
  button("Previous",36,401,110,24,page>0,false,()->page--);button("Next",158,401,110,24,(page+1)*8<rows.size(),false,()->page++);clipped(jumps!=null?"Detecting jumps...":automaticCount+" auto / "+bookmarks.manualCount()+" manual",284,408,172,MUTED);
  text("BOOKMARK",504,93,ACCENT);input("bookmark-name",name,"Bookmark name",504,120,318,64,value->name=value);
  boolean live=watching();label(live?"Current: "+time(ReplaySession.currentTick()):"Start the replay to bookmark its current tick.",504,165);
  button("Add current tick",504,193,318,30,live,true,()->action(()->{int tick=ReplaySession.currentTick();ReplayBookmarks.Bookmark b=bookmarks.add(name.trim().isEmpty()?"Tick "+tick:name,tick,ReplaySession.lastTick());selected=b.id;name=b.name;page=bookmarks.list().indexOf(b)/8;message="Bookmark saved. Save the replay in the library to keep its recording.";}));
  ReplayBookmarks.Bookmark picked=bookmarks.find(selected);button("Go to bookmark",504,241,318,30,picked!=null,true,()->action(()->go(picked)));
  button("Rename selected",504,289,318,28,picked!=null&&!picked.automatic,false,()->action(()->{bookmarks.rename(selected,name);message="Bookmark renamed.";}));
  button("Delete selected",504,331,318,28,picked!=null&&!picked.automatic,false,()->action(()->{bookmarks.delete(selected);selected="";name="";message="Bookmark deleted.";}));
  panel(20,450,820,112);
  boolean watching=watching();int tick=watching?ReplaySession.currentTick():0,last=watching?ReplaySession.lastTick():1;
  slider("replay-scrub","Timeline",tick,0,Math.max(1,last)," ticks",36,462,788,watching,v->ReplaySession.inspectTick(v));
  button("Set A",36,512,94,28,watching,false,()->{ReplaySession.markLoopStart();message="Loop start set at tick "+ReplaySession.currentTick();});
  button("Set B",140,512,94,28,watching,false,()->action(()->{ReplaySession.markLoopEnd();message="Loop end set.";}));
  button("Play A-B",244,512,116,28,watching&&ReplaySession.loopStart()>=0&&ReplaySession.loopEnd()>ReplaySession.loopStart(),true,()->action(()->{ReplaySession.playLoop();open(null);}));
  button("Clear loop",370,512,116,28,watching&&ReplaySession.loopStart()>=0,false,()->{ReplaySession.clearLoop();message="Loop cleared.";});
  clipped(watching&&ReplaySession.loopStart()>=0?"A: "+ReplaySession.loopStart()+" / B: "+(ReplaySession.loopEnd()<0?"Not set":ReplaySession.loopEnd())+(ReplaySession.looping()?" / Loop ON":""):"Pause, scrub, set A and B to study a passage.",500,521,320,MUTED);
  label(picked!=null&&picked.automatic?"Automatic jump marker / Read only":"Opening a bookmark pauses at that tick.",504,389);footer(message.isEmpty()?"Click Go beside any jump. In playback, use the Bookmarks book in hotbar slot 5. Esc returns to replay.":message);
 }
 @Override protected void back(){open(watching()?null:parent);}
}
