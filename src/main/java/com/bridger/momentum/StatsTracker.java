package com.bridger.momentum;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/** Bounded lightweight samples; file IO, history import and chart queries stay off the game thread. */
public final class StatsTracker {
 private static final Path FILE=Paths.get("config/bridgetap/statistics.properties");
 private static final ArrayBlockingQueue<Sample> pending=new ArrayBlockingQueue<>(1024);
 private static final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"BridgeTap statistics");t.setDaemon(true);return t;});
 private static BridgeStats stats;private static boolean dirty,fresh;private static long written,lastRevision=-1,lastHour=-1;private static volatile Request processed;
 private static volatile Request requested=new Request("",null,BridgeStats.Period.THIS_WEEK);private static volatile BridgeStats.View view;private static volatile String message="Loading statistics...";private static volatile boolean importRequested,importing;private static Iterator<RunHistory.Run> history;
 private static final class Sample{final String server;final ServerMode.Mode mode;final long created;final double time;final int jumps;final boolean success;Sample(RunHistory.Run run){server=run.server;mode=run.mode();created=run.created;time=run.time;jumps=run.jumps;success="SUCCESS".equals(run.status);}}
 private static final class Request{final String server;final ServerMode.Mode mode;final BridgeStats.Period period;Request(String server,ServerMode.Mode mode,BridgeStats.Period period){this.server=server;this.mode=mode;this.period=period;}}
 static{worker.execute(()->{try{fresh=!Files.exists(FILE);stats=BridgeStats.read(FILE,System.currentTimeMillis());message="Statistics include local attempts; average times use successful runs only.";}catch(Exception ex){message="Cannot load statistics: "+ex.getMessage();}});worker.scheduleWithFixedDelay(StatsTracker::tick,0,200,TimeUnit.MILLISECONDS);Runtime.getRuntime().addShutdownHook(new Thread(()->{worker.execute(()->{drain();save();});worker.shutdown();try{worker.awaitTermination(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}},"BridgeTap statistics save"));}
 public static void record(RunHistory.Run run){if(run.isImported())return;if(!pending.offer(new Sample(run)))message="Statistics queue full; an attempt could not be recorded.";}
 public static void request(String server,ServerMode.Mode mode,BridgeStats.Period period){Request r=requested;if(!r.server.equals(server)||r.mode!=mode||r.period!=period)requested=new Request(server,mode,period);}
 public static BridgeStats.View view(){return view;}public static boolean current(){return processed==requested;}public static String message(){return message;}public static boolean importing(){return importing||importRequested;}
 public static void importHistory(){if(!importing())importRequested=true;}
 private static void drain(){if(stats==null)return;Sample sample;int count=0;while(count++<128&&(sample=pending.poll())!=null){if(fresh){stats.since=Math.min(stats.since,sample.created);fresh=false;}stats.record(sample.server,sample.mode,sample.created,sample.success,sample.time,sample.jumps);dirty=true;}}
 private static void tick(){try{if(stats==null)return;drain();if(importRequested){importRequested=false;if(!stats.imported){importing=true;message="Importing saved local history... Removed recordings cannot be recovered.";history=BridgeTracker.history().savedSnapshot().iterator();}}
  if(importing){for(int n=0;n<8&&history.hasNext();n++){RunHistory.Run run=history.next();if(run.isImported()||run.created>=stats.since||!stats.historicalIds.add(run.id))continue;if(!run.modeReady()||run.mode()==ServerMode.Mode.UNKNOWN)try{run.observeMode(BridgeTracker.history().replay(run));}catch(Exception ignored){}stats.record(run.server,run.mode(),run.created,"SUCCESS".equals(run.status),run.time,run.jumps);dirty=true;}if(!history.hasNext()){stats.imported=true;stats.historicalIds.clear();stats.revision++;dirty=true;importing=false;history=null;message="Saved local history imported once. Deleted/unsaved old attempts are unavailable.";}}
  Request r=requested;long hour=System.currentTimeMillis()/3600000;if(processed!=r||lastRevision!=stats.revision||lastHour!=hour){view=stats.view(r.server,r.mode,r.period,Instant.now(),ZoneId.systemDefault());processed=r;lastRevision=stats.revision;lastHour=hour;}
  if(dirty&&System.currentTimeMillis()-written>=5000)save();
 }catch(Exception ex){if(history==null)importing=false;message="Statistics: "+ex.getMessage();}}
 private static void save(){if(stats==null||!dirty)return;try{stats.write(FILE);written=System.currentTimeMillis();dirty=false;}catch(Exception ex){message="Cannot save statistics: "+ex.getMessage();}}
}
