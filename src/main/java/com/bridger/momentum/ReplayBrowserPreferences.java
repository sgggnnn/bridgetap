package com.bridger.momentum;
import java.io.*;
import java.nio.file.*;
import java.util.*;
/** Separate browser preferences: replay formats and existing APIs are unchanged. */
public final class ReplayBrowserPreferences {
 public String folder="",search="",status="ALL",source="ALL",mode="ALL";public int sort;public boolean ascending;
 public final Set<String> trash=new HashSet<>();private final Path file;private boolean dirty;private long changed;
 public ReplayBrowserPreferences(Path file)throws IOException{
  this.file=file;if(!Files.isRegularFile(file)||Files.size(file)>4*1024*1024)return;Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}catch(IllegalArgumentException invalid){return;}
  folder=p.getProperty("folder","");search=p.getProperty("search","");if(search.length()>64)search="";
  status=ReplayQuery.parse(ReplayQuery.Status.class,p.getProperty("status"),ReplayQuery.Status.ALL).name();
  source=p.getProperty("source","ALL");if(!Arrays.asList("ALL","LOCAL","IMPORTED").contains(source))source="ALL";
  mode=ReplayQuery.parse(ServerMode.Filter.class,p.getProperty("mode"),ServerMode.Filter.ALL).name();
  try{sort=Math.max(0,Math.min(3,Integer.parseInt(p.getProperty("sort","0"))));}catch(NumberFormatException ignored){}ascending=Boolean.parseBoolean(p.getProperty("ascending","false"));
  for(String key:p.stringPropertyNames())if(key.startsWith("trash.")&&key.substring(6).matches("[0-9a-fA-F-]{36}"))trash.add(key.substring(6));
 }
 public void changed(){dirty=true;changed=System.nanoTime();}
 public void tick()throws IOException{if(dirty&&System.nanoTime()-changed>500000000L)flush();}
 public void flush()throws IOException{
  if(!dirty)return;Properties p=new Properties();p.setProperty("folder",folder);p.setProperty("search",search);p.setProperty("status",status);p.setProperty("source",source);p.setProperty("mode",mode);p.setProperty("sort",String.valueOf(sort));p.setProperty("ascending",String.valueOf(ascending));for(String id:trash)p.setProperty("trash."+id,"true");
  Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"browser-",".tmp");try{try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"BridgeTap replay browser / reversible library Trash");}try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException unsupported){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}dirty=false;}finally{Files.deleteIfExists(temp);}
 }
}
