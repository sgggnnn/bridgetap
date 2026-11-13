package com.bridger.momentum;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Named snapshots, not timed inputs or automated gameplay actions. */
public final class KeybindProfiles {
 public static final class Profile{public final String id,name;public final Map<String,Integer> keys;Profile(String id,String name,Map<String,Integer> keys){this.id=id;this.name=name;this.keys=Collections.unmodifiableMap(new LinkedHashMap<>(keys));}}
 private final Path file;private final List<Profile> profiles=new ArrayList<>();private String active="";
 public KeybindProfiles(Path file)throws IOException{this.file=file;if(!Files.exists(file))return;if(Files.size(file)>1024*1024)throw new IOException("Keybind profiles file too large");Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}active=p.getProperty("active","");for(String id:p.getProperty("order","").split(",")){if(!id.matches("[a-f0-9-]{36}"))continue;Map<String,Integer> keys=new LinkedHashMap<>();String prefix=id+".key.";for(String key:p.stringPropertyNames())if(key.startsWith(prefix))try{keys.put(key.substring(prefix.length()),Integer.parseInt(p.getProperty(key)));}catch(NumberFormatException ignored){}profiles.add(new Profile(id,p.getProperty(id+".name","Profile"),keys));if(profiles.size()>=50)break;}}
 public List<Profile> list(){return Collections.unmodifiableList(new ArrayList<>(profiles));}
 public String active(){return active;}
 public Profile save(String id,String name,Map<String,Integer> keys)throws IOException{if(name==null||name.trim().isEmpty()||name.length()>64)throw new IllegalArgumentException("Use a name of 1-64 characters");if(keys.size()>4096)throw new IllegalArgumentException("Too many keybindings");Profile old=find(id);if(old==null&&profiles.size()>=50)throw new IllegalArgumentException("Maximum 50 profiles");Profile next=new Profile(old==null?UUID.randomUUID().toString():id,name.trim(),keys);if(old==null)profiles.add(next);else profiles.set(profiles.indexOf(old),next);store();return next;}
 public Profile find(String id){for(Profile p:profiles)if(p.id.equals(id))return p;return null;}
 public Profile rename(String id,String name)throws IOException{Profile old=find(id);if(old==null)throw new IllegalArgumentException("Select a profile");return save(id,name,old.keys);}
 public void select(String id)throws IOException{if(find(id)==null)throw new IllegalArgumentException("Select a profile");active=id;store();}
 public void delete(String id)throws IOException{Profile p=find(id);if(p!=null)profiles.remove(p);if(active.equals(id))active="";store();}
 public Profile next(){if(profiles.isEmpty())return null;Profile current=find(active);return profiles.get((profiles.indexOf(current)+1)%profiles.size());}
 private void store()throws IOException{Properties p=new Properties();p.setProperty("active",active);List<String> ids=new ArrayList<>();for(Profile profile:profiles){ids.add(profile.id);p.setProperty(profile.id+".name",profile.name);for(Map.Entry<String,Integer> key:profile.keys.entrySet())p.setProperty(profile.id+".key."+key.getKey(),key.getValue().toString());}p.setProperty("order",String.join(",",ids));Files.createDirectories(file.toAbsolutePath().getParent());Path tmp=Files.createTempFile(file.toAbsolutePath().getParent(),"profiles-",".tmp");try{try(OutputStream out=Files.newOutputStream(tmp)){p.store(out,"BridgeTap keybind profiles");}Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}}
}
