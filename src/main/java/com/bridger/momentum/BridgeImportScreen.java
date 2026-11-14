package com.bridger.momentum;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.unmapped.C_85740840;

/** Preview a validated in-memory snapshot before adding anything to the library. */
public final class BridgeImportScreen extends BridgeScreen {
 private final BridgeHistoryScreen parent;private final C_85740840 returnTo;
 private List<Path> files=new ArrayList<>();private int page;private String message="Select a file or import a BTR1: code from the clipboard.";
 private CompletableFuture<ReplayArchive.Content> reading;private CompletableFuture<RunHistory.Run> adding;
 private ReplayArchive.Content preview;private RunHistory.Run summary;
 public BridgeImportScreen(BridgeHistoryScreen parent){this(parent,parent);}
 public BridgeImportScreen(BridgeHistoryScreen parent,C_85740840 returnTo){this.parent=parent;this.returnTo=returnTo;refresh();}
 private void refresh(){try{Path root=BridgeTracker.history().importsFolder();Files.createDirectories(root);List<Path> found=new ArrayList<>();try(DirectoryStream<Path> stream=Files.newDirectoryStream(root,"*.btreplay")){for(Path p:stream)if(Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)&&found.size()<1000)found.add(p);}found.sort(Comparator.comparing(p->p.getFileName().toString(),String.CASE_INSENSITIVE_ORDER));files=found;page=Math.min(page,Math.max(0,(files.size()-1)/7));}catch(IOException ex){message="Could not list imports folder.";}}
 private void read(Path file){preview=null;summary=null;reading=CompletableFuture.supplyAsync(()->{try{ReplayArchive.Content c=ReplayArchive.read(file);RunHistory.previewImport(c);return c;}catch(IOException ex){throw new CompletionException(ex);}},ReplaySharing.WORKER);}
 private void clipboard(){String text;try{text=m_38845775();}catch(RuntimeException ex){message="Clipboard unavailable.";return;}preview=null;summary=null;reading=CompletableFuture.supplyAsync(()->{try{return ReplayShareCode.decode(text);}catch(IOException ex){throw new CompletionException(ex);}},ReplaySharing.WORKER);}
 private void confirm(){final ReplayArchive.Content content=preview;adding=CompletableFuture.supplyAsync(()->{try{return ReplayImportFlow.confirm(BridgeTracker.history(),content,ReplayFolders.local());}catch(IOException ex){throw new CompletionException(ex);}},ReplaySharing.WORKER);}
 @Override protected void paint(){
  if(reading!=null&&reading.isDone()){try{preview=reading.join();summary=RunHistory.previewImport(preview);message="Preview ready. Nothing is imported until you confirm.";}catch(Exception ex){message=ReplaySharing.error(ex);}reading=null;}
  if(adding!=null&&adding.isDone()){try{RunHistory.Run run=adding.join();if(parent!=null)parent.imported(run);message="Replay added to Imported.";preview=null;summary=null;}catch(Exception ex){message=ReplaySharing.error(ex);}adding=null;}
  header("Import replays","Files and offline share codes");button("Back",752,15,88,this::back);
  boolean idle=reading==null&&adding==null;
  button("Import from clipboard",20,64,266,28,idle,true,this::clipboard);button("Open imports",300,64,176,28,true,false,()->{try{message=BridgeFileActions.open(BridgeTracker.history().importsFolder());}catch(Exception ex){message="Could not open imports folder.";}});button("Refresh",490,64,122,28,idle,false,this::refresh);
  panel(20,112,442,324);panel(478,112,362,324);
  text("REPLAY FILES",38,132,ACCENT);
  for(int i=page*7;i<Math.min(files.size(),page*7+7);i++){final Path file=files.get(i);button(file.getFileName().toString(),38,163+(i-page*7)*32,406,26,idle,false,()->read(file));}
  if(files.isEmpty())label("No .btreplay files in imports.",38,175);
  button("Previous",38,402,110,24,page>0&&idle,false,()->page--);button("Next",160,402,110,24,(page+1)*7<files.size()&&idle,false,()->page++);label((page+1)+" / "+Math.max(1,(files.size()+6)/7),294,410);
  text("IMPORT PREVIEW",496,132,ACCENT);
  if(summary==null){label(reading!=null?"Reading and validating replay...":"Select a replay or paste a code.",496,175);}
  else{clipped(summary.displayName(),496,168,326,TEXT);label(String.format(Locale.US,"%.3fs / Axis %s / %d jumps",summary.time,summary.axis,summary.jumps),496,201);clipped("Technique: "+ReplaySharing.techniques(summary),496,233,326,MUTED);label("Recording version: "+preview.replay.formatVersion,496,265);label("Status: "+summary.status,496,292);label("Destination: Imported",496,320);button("Confirm import",496,364,326,32,idle,true,this::confirm);}
  footer(message);
 }
 @Override protected void back(){if(adding!=null){message="Finishing the confirmed import...";return;}if(returnTo instanceof ReplayFolderScreen)((ReplayFolderScreen)returnTo).reload();open(returnTo);}
}
