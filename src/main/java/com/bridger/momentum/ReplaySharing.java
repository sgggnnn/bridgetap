package com.bridger.momentum;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Local preparation only; outbound network is never invoked here. */
public final class ReplaySharing {
 public static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"BridgeTap replay sharing");t.setDaemon(true);return t;});
 private ReplaySharing(){}
 public static String techniques(RunHistory.Run run){
  String joined=String.join(" ",run.details).replaceAll("\u00a7.","");List<String> names=new ArrayList<>();
  for(ReplayQuery.Technique t:ReplayQuery.Technique.values())if(t!=ReplayQuery.Technique.ALL&&joined.contains(t.match))names.add(t.label);
  return names.isEmpty()?"Unknown":String.join(", ",names);
 }
 public static byte[] prepare(RunHistory history,RunHistory.Run run,boolean identity)throws IOException{
  ReplayData data=history.replay(run);if(data==null)throw new IOException("This summary contains no recording");
  // A fresh identity/manifest prevents run names, paths, servers or free text leaking.
  Properties p=new Properties();p.setProperty("id",UUID.randomUUID().toString());p.setProperty("name","Shared replay");p.setProperty("status",run.status);p.setProperty("time",Double.toString(run.time));p.setProperty("jumps",Integer.toString(run.jumps));p.setProperty("axis",run.axis);p.setProperty("created","0");p.setProperty("count","1");p.setProperty("line.0",techniques(run));p.setProperty("server","");
  return ReplayArchive.bytes(p,identity?data:data.withIdentity(ReplayData.Identity.unknown()));
 }
 public static String error(Throwable error){while(error instanceof CompletionException||error instanceof ExecutionException){if(error.getCause()==null)break;error=error.getCause();}return error instanceof IOException?error.getMessage():"Could not read this replay. Try exporting the file instead.";}
}
