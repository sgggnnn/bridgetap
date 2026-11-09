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
  private Run(String id,String status,double time,int jumps,String axis,long created,List<String> details) {
   this.id=id; this.status=status; this.time=time; this.jumps=jumps; this.axis=axis; this.created=created;
   this.details=Collections.unmodifiableList(new ArrayList<>(details));
  }
 }
 private final Path folder;
 private final List<Run> recent=new ArrayList<>();
 public RunHistory(Path folder) {this.folder=folder;}
 public void add(String status,double time,int jumps,String axis,List<String> details) {
  recent.add(0,new Run(UUID.randomUUID().toString(),status,time,jumps,axis,System.currentTimeMillis(),details));
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
     int count=Integer.parseInt(p.getProperty("count","0"));
     if(count<0 || count>2000) continue;
     List<String> details=new ArrayList<>();
     for(int i=0;i<count;i++) details.add(p.getProperty("line."+i,""));
     runs.add(new Run(p.getProperty("id"),p.getProperty("status"),Double.parseDouble(p.getProperty("time")),Integer.parseInt(p.getProperty("jumps")),p.getProperty("axis"),Long.parseLong(p.getProperty("created")),details));
    } catch(IllegalArgumentException | NullPointerException ex) {System.err.println("[BridgeTap] Invalid saved run: "+file);}
   }
  }
  runs.sort((a,b)->Long.compare(b.created,a.created));return runs;
 }
 public void save(Run run) throws IOException {
  Files.createDirectories(folder);Path dest=folder.resolve(run.id+".properties");
  if(Files.exists(dest))return;
  Properties p=new Properties();p.setProperty("id",run.id);p.setProperty("status",run.status);p.setProperty("time",String.valueOf(run.time));p.setProperty("jumps",String.valueOf(run.jumps));p.setProperty("axis",run.axis);p.setProperty("created",String.valueOf(run.created));p.setProperty("count",String.valueOf(run.details.size()));
  for(int i=0;i<run.details.size();i++)p.setProperty("line."+i,run.details.get(i));
  try(OutputStream out=Files.newOutputStream(dest,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){p.store(out,"BridgeTap run summary");}
 }
}
