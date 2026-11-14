package com.bridger.momentum;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Small per-recording sidecars. Opening/painting a list never loads replay frames. */
public final class ReplayBookmarks {
 public static final int LIMIT=128;
 public static final class Bookmark {
  public final String id,name;public final int tick;public final boolean automatic;
  private Bookmark(String id,String name,int tick){this(id,name,tick,false);}
  private Bookmark(String id,String name,int tick,boolean automatic){this.id=id;this.name=name;this.tick=tick;this.automatic=automatic;}
  static Bookmark jump(int index,int tick){return new Bookmark("auto-jump-"+tick,"Jump #"+index,tick,true);}
 }
 private final Path file;private final Map<String,Bookmark> entries=new LinkedHashMap<>();
 private final Map<String,Bookmark> automatic=new LinkedHashMap<>();
 private List<Bookmark> ordered;
 public void automatic(List<Bookmark> jumps){ordered=null;automatic.clear();for(Bookmark b:jumps)if(b.automatic)automatic.put(b.id,b);}
 public ReplayBookmarks(Path folder,String recording)throws IOException{
  UUID.fromString(recording);file=folder.resolve(recording+".properties");
  if(!Files.exists(file))return;if(Files.size(file)>65536)throw new IOException("Bookmark file exceeds 64 KiB");
  Properties data=new Properties();try(InputStream in=Files.newInputStream(file)){data.load(in);}
  if(!"1".equals(data.getProperty("version")))throw new IOException("Unsupported bookmark version");
  try{for(String key:data.stringPropertyNames())if(key.startsWith("tick.")){
   String id=key.substring(5);UUID.fromString(id);int tick=Integer.parseInt(data.getProperty(key));String name=validName(data.getProperty("name."+id));
   if(tick<0||tick>2000000||entries.size()>=LIMIT)throw new IllegalArgumentException("Invalid bookmark tick/count");entries.put(id,new Bookmark(id,name,tick));
  }}catch(IllegalArgumentException ex){throw new IOException("Invalid replay bookmarks",ex);}
 }
 public int manualCount(){return entries.size();}
 public List<Bookmark> list(){if(ordered==null){List<Bookmark> result=new ArrayList<>(entries.values());result.addAll(automatic.values());result.sort(Comparator.comparingInt((Bookmark b)->b.tick).thenComparing(b->b.name).thenComparing(b->b.id));ordered=Collections.unmodifiableList(result);}return ordered;}
 public Bookmark find(String id){Bookmark b=entries.get(id);return b==null?automatic.get(id):b;}
 public Bookmark add(String name,int tick,int lastTick)throws IOException{
  if(entries.size()>=LIMIT)throw new IllegalArgumentException("Maximum 128 bookmarks per replay");if(tick<0||tick>lastTick||tick>2000000)throw new IllegalArgumentException("Tick outside replay");
  Bookmark b=new Bookmark(UUID.randomUUID().toString(),validName(name),tick);ordered=null;entries.put(b.id,b);try{save();}catch(IOException|RuntimeException ex){entries.remove(b.id);throw ex;}return b;
 }
 public void rename(String id,String name)throws IOException{Bookmark old=require(id);String valid=validName(name);ordered=null;entries.put(id,new Bookmark(id,valid,old.tick));try{save();}catch(IOException|RuntimeException ex){entries.put(id,old);throw ex;}}
 public void delete(String id)throws IOException{Bookmark old=require(id);ordered=null;entries.remove(id);try{save();}catch(IOException|RuntimeException ex){entries.put(id,old);throw ex;}}
 private Bookmark require(String id){Bookmark b=find(id);if(b==null)throw new IllegalArgumentException("Select a bookmark");if(b.automatic)throw new IllegalArgumentException("Automatic jump bookmarks are generated from the recording");return b;}
 private static String validName(String name){if(name==null||name.trim().isEmpty()||name.length()>64||name.chars().anyMatch(Character::isISOControl)||name.indexOf('\u00a7')>=0)throw new IllegalArgumentException("Use a bookmark name of 1-64 printable characters");return name.trim();}
 private void save()throws IOException{
  Files.createDirectories(file.toAbsolutePath().getParent());Properties p=new Properties();p.setProperty("version","1");for(Bookmark b:entries.values()){p.setProperty("tick."+b.id,Integer.toString(b.tick));p.setProperty("name."+b.id,b.name);}
  Path temp=Files.createTempFile(file.toAbsolutePath().getParent(),"bookmarks-",".part");try{try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"BridgeTap replay bookmarks");}Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}
 }
}
