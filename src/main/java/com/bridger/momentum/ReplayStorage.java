package com.bridger.momentum;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Explicit, bounded auto-save policy. File classification never implies saving. */
public final class ReplayStorage {
 private static final Path PREFS=Paths.get("config/bridgetap/replay-storage.properties");
 private static volatile int mode,limit=100;private static volatile String message="Recent runs stay in memory; Save replay keeps one permanently.";private static final AtomicBoolean archiving=new AtomicBoolean(),organizing=new AtomicBoolean();
 private static final ThreadPoolExecutor IO=new ThreadPoolExecutor(1,1,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(16),r->{Thread thread=new Thread(r,"BridgeTap replay storage");thread.setDaemon(true);return thread;},new ThreadPoolExecutor.AbortPolicy());
 static{try(InputStream in=Files.newInputStream(PREFS)){Properties p=new Properties();p.load(in);mode=Math.max(0,Math.min(2,Integer.parseInt(p.getProperty("autoSave","0"))));limit=Math.max(25,Math.min(500,Integer.parseInt(p.getProperty("automaticLimit","100"))));}catch(IOException|IllegalArgumentException ignored){}}
 private static void submit(Runnable task,AtomicBoolean flag){try{IO.execute(task);}catch(RejectedExecutionException busy){if(flag!=null)flag.set(false);message="Replay storage is busy. Recent runs remain available for manual saving.";}}
 static{Runtime.getRuntime().addShutdownHook(new Thread(()->{IO.shutdown();try{IO.awaitTermination(5,TimeUnit.SECONDS);}catch(InterruptedException ex){Thread.currentThread().interrupt();}},"BridgeTap replay storage shutdown"));}
 public static int mode(){return mode;}public static int limit(){return limit;}public static boolean archiving(){return archiving.get();}public static String message(){return message;}
 public static void mode(int value){mode=Math.max(0,Math.min(2,value));store();}
 public static void limit(int value){limit=Math.max(25,Math.min(500,value));store();}
 private static void store(){try{Files.createDirectories(PREFS.getParent());Properties p=new Properties();p.setProperty("autoSave",String.valueOf(mode));p.setProperty("automaticLimit",String.valueOf(limit));try(OutputStream out=Files.newOutputStream(PREFS)){p.store(out,"BridgeTap replay storage: automatic saving defaults OFF");}}catch(IOException ex){message=ex.getMessage();}}
 public static void completed(RunHistory history,RunHistory.Run run){final int policy=mode;submit(()->{try{if(policy==2||(policy==1&&run.status.equals("SUCCESS"))){history.saveAutomatic(run);history.archiveAutomatic(limit);}organizeNow(history,Collections.singletonList(run));}catch(IOException|RuntimeException ex){message="Replay storage: "+ex.getMessage();}},null);}
 public static void organize(RunHistory history,Collection<RunHistory.Run> runs){if(!organizing.compareAndSet(false,true))return;List<RunHistory.Run> snapshot=new ArrayList<>(runs);submit(()->{try{organizeNow(history,snapshot);}catch(IOException|RuntimeException ex){message="Replay folders: "+ex.getMessage();}finally{organizing.set(false);}},organizing);}
 private static void organizeNow(RunHistory history,Collection<RunHistory.Run> runs)throws IOException{ReplayFolders folders=ReplayFolders.local();folders.beginBatch();try{for(RunHistory.Run run:runs)folders.fileByServer(history,run);Set<String> retained=new HashSet<>();for(RunHistory.Run run:history.savedSnapshot())retained.add(run.id);for(RunHistory.Run run:history.recent())retained.add(run.id);folders.retainRuns(retained);}finally{folders.endBatch();}}
 public static void archive(RunHistory history){if(!archiving.compareAndSet(false,true))return;message="Archiving older automatic replays in the background...";submit(()->{try{int count=history.archiveAutomatic(limit);organizeNow(history,history.recent());message="Archived "+count+" automatic replays. Saved, favorite and imported runs were kept.";}catch(IOException|RuntimeException ex){message="Replay archive: "+ex.getMessage();}finally{archiving.set(false);}},archiving);}
 public static void cleanup(RunHistory history,long now){submit(()->{try{ReplayFolders.local().cleanupDue(history,now);}catch(IOException ex){message="Replay cleanup: "+ex.getMessage();}},null);}
}
