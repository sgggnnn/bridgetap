package com.bridger.momentum;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Lazily import original placement samples from Mojang's verified asset index. */
public final class ModernSounds {
 private static final Path ROOT=Paths.get("config/bridgetap/modern-blocks/audio");
 private static final String INDEX_HASH="d7b16b1133c9b47a173f3a780cafda615b80d059";
 private static final Properties events=new Properties();
 private static final Map<String,String> samples=new java.util.concurrent.ConcurrentHashMap<>();
 private static final Object importLock=new Object();
 private static final JsonObject definitions=new JsonObject();
 private static JsonObject index,source;private static String requested="";
 private static final Set<String> ready=new HashSet<>(),loading=new HashSet<>();
 private static volatile String status="";
 static{try(InputStream in=ModernSounds.class.getResourceAsStream("/assets/bridgetap/modern-block-sounds.properties")){if(in!=null)events.load(in);}catch(IOException ex){System.err.println("[BridgeTap] Modern sound mapping: "+ex.getMessage());}}
 public static String status(){return status;}
 public static String event(String block){return events.getProperty(block.startsWith("modern:")?block.substring(7):block);}
 public static synchronized String sound(String block){String event=event(block);return ready.contains(event)?"bridgetap:modern."+event:null;}
 public static synchronized void select(String block){String event=event(block);if(event==null||ready.contains(event)||loading.contains(event))return;requested=event;loading.add(event);status="Loading original 26.2 placement sounds...";
  Thread worker=new Thread(()->{try{prepare(event);status="Original 26.2 placement sounds ready.";ModernAppearance.resourcesChanged();}catch(Exception ex){status="26.2 audio unavailable: "+ex.getMessage();System.err.println("[BridgeTap] "+status);}finally{synchronized(ModernSounds.class){loading.remove(event);}}},"BridgeTap modern placement sounds");worker.setDaemon(true);worker.start();
 }
 private static byte[] fetch(String address,int max)throws IOException{URL url=new URL(address);if(!url.getProtocol().equals("https")||!(url.getHost().equals("piston-meta.mojang.com")||url.getHost().equals("resources.download.minecraft.net")))throw new IOException("Unexpected audio asset host");HttpURLConnection connection=(HttpURLConnection)url.openConnection();connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(10000);connection.setReadTimeout(20000);connection.setRequestProperty("User-Agent","BridgeTap/1.8.0 (Minecraft audio import)");try(InputStream in=connection.getInputStream()){return UpdateSource.read(in,max);}finally{connection.disconnect();}}
 private static String hash(byte[] bytes)throws IOException{try{MessageDigest digest=MessageDigest.getInstance("SHA-1");StringBuilder text=new StringBuilder();for(byte b:digest.digest(bytes))text.append(String.format(Locale.ROOT,"%02x",b&255));return text.toString();}catch(NoSuchAlgorithmException ex){throw new IOException(ex);}}
 private static byte[] verified(String url,String hash,int size)throws IOException{if(!hash.matches("[a-f0-9]{40}")||size<1||size>4*1024*1024)throw new IOException("Invalid audio asset metadata");Files.createDirectories(ROOT);Path target=ROOT.resolve(hash);if(Files.isRegularFile(target)&&Files.size(target)==size){byte[] bytes=Files.readAllBytes(target);if(hash(bytes).equals(hash))return bytes;}
  byte[] bytes=fetch(url,size);if(bytes.length!=size||!hash(bytes).equals(hash))throw new IOException("Audio asset checksum mismatch");Path temp=Files.createTempFile(ROOT,"audio-",".tmp");try{Files.write(temp,bytes);Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}return bytes;
 }
 private static byte[] asset(JsonObject entry)throws IOException{String h=entry.get("hash").getAsString();return verified("https://resources.download.minecraft.net/"+h.substring(0,2)+"/"+h,h,entry.get("size").getAsInt());}
 private static void metadata()throws IOException{if(index!=null&&source!=null)return;JsonObject loaded=UpdateSource.json(verified("https://piston-meta.mojang.com/v1/packages/"+INDEX_HASH+"/32.json",INDEX_HASH,586366)).getAsJsonObject("objects");JsonObject sounds=UpdateSource.json(asset(loaded.getAsJsonObject("minecraft/sounds.json")));index=loaded;source=sounds;}
 private static void prepare(String event)throws IOException{synchronized(importLock){metadata();JsonArray variants=variants(event,new HashSet<>());JsonObject definition=new JsonObject();definition.add("sounds",variants);definition.addProperty("category","block");synchronized(ModernSounds.class){definitions.add("modern."+event,definition);ready.add(event);}}}
 private static JsonArray variants(String event,Set<String> visited)throws IOException{if(visited.size()>16||!visited.add(event))throw new IOException("Cyclic sound event");JsonArray result=new JsonArray();JsonObject value=source.getAsJsonObject(event);if(value==null){if(event.equals("intentionally_empty"))return result;throw new IOException("Missing placement event: "+event);}
  for(JsonElement element:value.getAsJsonArray("sounds")){JsonObject variant=element.isJsonObject()?new JsonParser().parse(element.toString()).getAsJsonObject():new JsonObject();String name=element.isJsonObject()?variant.get("name").getAsString():element.getAsString();if(variant.has("type")&&variant.get("type").getAsString().equals("event")){JsonArray children=variants(name,new HashSet<>(visited));for(JsonElement child:children)result.add(child);continue;}
   if(name.startsWith("minecraft:"))name=name.substring(10);if(!name.matches("[a-z0-9_/.-]+")||name.contains(".."))throw new IOException("Invalid sound path");JsonObject object=index.getAsJsonObject("minecraft/sounds/"+name+".ogg");if(object==null)throw new IOException("Missing placement sample: "+name);asset(object);samples.put("sounds/modern/"+name+".ogg",object.get("hash").getAsString());variant.addProperty("name","bridgetap:modern/"+name);variant.remove("stream");variant.remove("type");variant.remove("attenuation_distance");result.add(variant);
  }return result;
 }
 public static synchronized byte[] resource(String path)throws IOException{if(path.equals("sounds.json"))return definitions.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);String hash=samples.get(path);if(hash==null)throw new FileNotFoundException(path);return Files.readAllBytes(ROOT.resolve(hash));}
}
