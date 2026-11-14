package com.bridger.momentum;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.unmapped.C_85740840;

/** Explicit privacy review before copying or exporting a prepared shared replay. */
public final class ReplayShareScreen extends BridgeScreen {
 private final C_85740840 parent;private final RunHistory.Run run;private boolean identity;
 private CompletableFuture<Prepared> preparing;private CompletableFuture<Path> exporting;private Prepared prepared;private String message="";
 private static final class Prepared {final byte[] archive;final String code,warning;final int version;Prepared(byte[] bytes)throws IOException{archive=bytes;version=ReplayArchive.read(bytes).replay.formatVersion;String text=null,error="";try{text=ReplayShareCode.encode(bytes);}catch(IOException tooLarge){error=tooLarge.getMessage();}code=text;warning=error;}}
 public ReplayShareScreen(C_85740840 parent,RunHistory.Run run){this.parent=parent;this.run=run;prepare();}
 private void prepare(){prepared=null;message="";final boolean include=identity;preparing=CompletableFuture.supplyAsync(()->{try{return new Prepared(ReplaySharing.prepare(BridgeTracker.history(),run,include));}catch(IOException ex){throw new CompletionException(ex);}},ReplaySharing.WORKER);}
 private void copy(){try{m_80874759(prepared.code);message="Share code copied. Paste it into your message.";}catch(RuntimeException ex){message="Clipboard unavailable. Export a file instead.";}}
 private void export(){final byte[] bytes=prepared.archive;exporting=CompletableFuture.supplyAsync(()->{try{Path file=BridgeTracker.history().exportsFolder().resolve("shared-replay-"+UUID.randomUUID()+".btreplay");Files.createDirectories(file.getParent());Files.write(file,bytes,StandardOpenOption.CREATE_NEW);return file;}catch(IOException ex){throw new CompletionException(ex);}},ReplaySharing.WORKER);}
 @Override protected void paint(){
  if(preparing!=null&&preparing.isDone()){try{prepared=preparing.join();}catch(Exception ex){message=ReplaySharing.error(ex);}preparing=null;}
  if(exporting!=null&&exporting.isDone()){try{exporting.join();message="Shared replay exported. Use Open exports to find it.";}catch(Exception ex){message=ReplaySharing.error(ex);}exporting=null;}
  header("Share replay","Offline code / Complete recording / Privacy review");button("Back",752,15,88,this::back);
  panel(20,74,820,160);clipped(run.displayName(),38,92,780,TEXT);
  label(String.format(Locale.US,"%.3fs / Axis %s / %d jumps",run.time,run.axis,run.jumps),38,121);
  clipped("Technique: "+ReplaySharing.techniques(run),38,148,780,ACCENT);
  label(prepared==null?"Preparing full replay...":"Recording v"+prepared.version+" / "+prepared.archive.length+" bytes / Code limit: 1900 characters",38,184);
  panel(20,250,820,154);text("WHAT WILL BE SHARED",38,270,ACCENT);
  label("Movement, rotations, blocks, items, inputs and world/sky context.",38,298);
  label("Custom name, server, date and free-text notes are removed.",38,321);
  toggle("Include recorded player name, UUID and skin",identity,38,349,780,()->{identity=!identity;prepare();});
  text(identity?"Player identity WILL be shared when you confirm copy/export.":"Recorded player identity is removed. Your original replay is unchanged.",38,384,identity?RED:MUTED);
  boolean ready=prepared!=null&&exporting==null;
  button("Copy share code",20,420,246,30,ready&&prepared.code!=null,true,this::copy);
  button("Export shared file",286,420,246,30,ready,false,this::export);
  button("Open exports",552,420,288,30,true,false,()->{try{message=BridgeFileActions.open(BridgeTracker.history().exportsFolder());}catch(Exception ex){message="Could not open exports folder.";}});
  footer(!message.isEmpty()?message:prepared!=null&&!prepared.warning.isEmpty()?prepared.warning:identity?"WARNING: Copy/export will include the recorded player identity. Continue only if you agree.":"Offline: no upload. Player identity is removed by default. The original replay is unchanged.");
 }
 @Override protected void back(){open(parent);}
}
