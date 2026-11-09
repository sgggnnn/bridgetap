package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Recent summaries are transient; saved summaries each have their own file. */
public class RunHistory {
 public static final class Run {
  public final String id, status, axis;
  public final double time;
  public final int jumps;
  public final long created;
  public final List<String> details;
  public final ReplayData replay;
  public String replayId() {
   String value=new java.math.BigInteger(id.replace("-",""),16).toString(36);
   while(value.length()<25)value="0"+value;
   return value.substring(0,5)+"-"+value.substring(5,10)+"-"+value.substring(10);
  }
  private Run(String id,String status,double time,int jumps,String axis,long created,List<String> details) {
   this(id,status,time,jumps,axis,created,details,null);
  }
  private Run(String id,String status,double time,int jumps,String axis,long created,List<String> details,ReplayData replay) {
   this.id=id; this.status=status; this.time=time; this.jumps=jumps; this.axis=axis; this.created=created;
   this.details=Collections.unmodifiableList(new ArrayList<>(details));
   this.replay=replay;
  }
 }
 private final Path folder;
 private final List<Run> recent=new ArrayList<>();
 private Set<String> favorites;
 public RunHistory(Path folder) {this.folder=folder;}
 public void add(String status,double time,int jumps,String axis,List<String> details) {
  add(status,time,jumps,axis,details,null);
 }
 public void add(String status,double time,int jumps,String axis,List<String> details,ReplayData replay) {
  recent.add(0,new Run(UUID.randomUUID().toString(),status,time,jumps,axis,System.currentTimeMillis(),details,replay));
  if(recent.size()>10) recent.remove(10);
 }
 public List<Run> recent(){return Collections.unmodifiableList(new ArrayList<>(recent));}
 public List<Run> saved() throws IOException {
  List<Run> runs=new ArrayList<>();
  if(!Files.isDirectory(folder))return runs;
  try(DirectoryStream<Path> files=Files.newDirectoryStream(folder,"*.properties")) {
   for(Path file:files) {
    if(Files.size(file)>1024*1024) continue;
    try(InputStream in=Files.newInputStream(file)) {
     Properties p=new Properties();p.load(in);
     UUID.fromString(p.getProperty("id"));
     int count=Integer.parseInt(p.getProperty("count","0"));
     if(count<0 || count>2000) continue;
     List<String> details=new ArrayList<>();
     for(int i=0;i<count;i++) details.add(p.getProperty("line."+i,"").replace("No Gay","No Gain"));
     runs.add(new Run(p.getProperty("id"),p.getProperty("status"),Double.parseDouble(p.getProperty("time")),Integer.parseInt(p.getProperty("jumps")),p.getProperty("axis"),Long.parseLong(p.getProperty("created")),details));
    } catch(IllegalArgumentException | NullPointerException ex) {System.err.println("[BridgeTap] Invalid saved run: "+file);}
   }
  }
  runs.sort((a,b)->Long.compare(b.created,a.created));return runs;
 }
 public void save(Run run) throws IOException {
  UUID.fromString(run.id);
  Files.createDirectories(folder);Path dest=folder.resolve(run.id+".properties");
  if(Files.exists(dest))return;
  Properties p=new Properties();p.setProperty("id",run.id);p.setProperty("status",run.status);p.setProperty("time",String.valueOf(run.time));p.setProperty("jumps",String.valueOf(run.jumps));p.setProperty("axis",run.axis);p.setProperty("created",String.valueOf(run.created));p.setProperty("count",String.valueOf(run.details.size()));
  for(int i=0;i<run.details.size();i++)p.setProperty("line."+i,run.details.get(i));
  Path replayFile=folder.resolve(run.id+".btr");
  if(run.replay!=null){
   Files.deleteIfExists(replayFile);
   run.replay.write(replayFile);
  }
  try(OutputStream out=Files.newOutputStream(dest,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){p.store(out,"BridgeTap run summary and replay");}
 }
 public ReplayData replay(Run run)throws IOException {
  if(run.replay!=null)return run.replay;
  UUID.fromString(run.id);
  Path file=folder.resolve(run.id+".btr");
  return Files.exists(file)?ReplayData.read(file):null;
 }
 public boolean hasReplay(Run run){
  try{UUID.fromString(run.id);return run.replay!=null||Files.isRegularFile(folder.resolve(run.id+".btr"));}
  catch(IllegalArgumentException ex){return false;}
 }
 private Set<String> favoriteIds() throws IOException {
  if(favorites==null){
   Set<String> loaded=new HashSet<>();Path file=folder.resolve("favorites.prefs");
   if(Files.exists(file))for(String id:Files.readAllLines(file,java.nio.charset.StandardCharsets.UTF_8)){
    try{UUID.fromString(id);loaded.add(id);}catch(IllegalArgumentException ignored){}
   }
   favorites=loaded;
  }
  return favorites;
 }
 public boolean isFavorite(Run run)throws IOException{return favoriteIds().contains(run.id);}
 public List<Run> favorites()throws IOException {
  List<Run> result=new ArrayList<>();for(Run run:saved())if(isFavorite(run))result.add(run);return result;
 }
 public boolean toggleFavorite(Run run)throws IOException {
  Set<String> updated=new HashSet<>(favoriteIds());boolean added=updated.add(run.id);if(!added)updated.remove(run.id);
  if(added)save(run);Files.createDirectories(folder);
  Path temp=Files.createTempFile(folder,"favorites-",".tmp");
  try{
   Files.write(temp,updated,java.nio.charset.StandardCharsets.UTF_8);
   try{Files.move(temp,folder.resolve("favorites.prefs"),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(AtomicMoveNotSupportedException ex){Files.move(temp,folder.resolve("favorites.prefs"),StandardCopyOption.REPLACE_EXISTING);}
   favorites=updated;return added;
  }finally{Files.deleteIfExists(temp);}
 }
}
