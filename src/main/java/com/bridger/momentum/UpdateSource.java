package com.bridger.momentum;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import com.google.gson.*;

/** Public GitHub release checks only. No account, server or gameplay data is uploaded. */
public final class UpdateSource {
 public static final String LATEST="https://api.github.com/repos/sgggnnn/bridgetap/releases/latest";
 public static JsonObject json(byte[] data)throws IOException{try{return new JsonParser().parse(new String(data,StandardCharsets.UTF_8)).getAsJsonObject();}catch(RuntimeException ex){throw new IOException("Invalid update metadata",ex);}}
 public static byte[] read(InputStream in,int limit)throws IOException{ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(bytes.size()+n>limit)throw new IOException("Update response exceeds its size limit");bytes.write(b,0,n);}return bytes.toByteArray();}
 public static HttpURLConnection connect(String address)throws IOException{
  URL url=new URL(address);
  for(int i=0;i<6;i++){
   String host=url.getHost();if(!url.getProtocol().equals("https")||!(host.equals("github.com")||host.equals("api.github.com")||host.equals("release-assets.githubusercontent.com")||host.equals("objects.githubusercontent.com")))throw new IOException("Untrusted update download host");
   HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(10000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","BridgeTap-update-check (github.com/sgggnnn/bridgetap)");
   int status=c.getResponseCode();if(status>=300&&status<400){String next=c.getHeaderField("Location");c.disconnect();if(next==null)throw new IOException("Missing update redirect");url=new URL(url,next);continue;}
   if(status!=200){c.disconnect();throw new IOException(status==403||status==429?"GitHub rate limit reached; try again later":"Update server returned HTTP "+status);}
   return c;
  }throw new IOException("Too many update redirects");
 }
 public static UpdateRelease select(JsonObject release,String installed,boolean forge)throws IOException{
  try{
   if(release.get("draft").getAsBoolean()||release.get("prerelease").getAsBoolean())return null;
   String tag=release.get("tag_name").getAsString();if(!tag.startsWith("v"))throw new IOException("Unsupported release tag");String version=tag.substring(1);
   if(version.contains("-")||!UpdateRelease.newer(version,installed))return null;
   JsonObject fallback=null;
   for(JsonElement item:release.getAsJsonArray("assets")){JsonObject a=item.getAsJsonObject();String name=a.get("name").getAsString();String expected="BridgeTap-"+version+(forge?"-Forge-1.8.9.jar":"-OneClient.jar");if(name.equals(expected))return asset(a,version,forge);if(!forge&&name.equals("BridgeTap-"+version+".jar"))fallback=a;}
   return fallback==null?null:asset(fallback,version,false);
  }catch(RuntimeException ex){throw new IOException("Unsupported or incomplete release metadata",ex);}
 }
 private static UpdateRelease asset(JsonObject a,String version,boolean forge)throws IOException{String digest=a.has("digest")&&!a.get("digest").isJsonNull()?a.get("digest").getAsString():"";if(!digest.startsWith("sha256:"))throw new IOException("This release does not provide a verified SHA-256 digest");return new UpdateRelease(version,a.get("name").getAsString(),digest.substring(7),a.get("size").getAsLong(),forge);}
 public static UpdateRelease latest(String installed,boolean forge)throws IOException{HttpURLConnection c=connect(LATEST);try(InputStream in=c.getInputStream()){return select(json(read(in,4*1024*1024)),installed,forge);}finally{c.disconnect();}}
 public static String version(Path jar,boolean forge)throws IOException{
  try(JarFile file=new JarFile(jar.toFile())){JarEntry entry=file.getJarEntry(forge?"mcmod.info":"fabric.mod.json");if(entry==null)throw new IOException("Missing mod metadata");JsonElement parsed;try(InputStream in=file.getInputStream(entry)){parsed=new JsonParser().parse(new String(read(in,65536),StandardCharsets.UTF_8));}
   JsonObject meta=forge?parsed.getAsJsonArray().get(0).getAsJsonObject():parsed.getAsJsonObject();if(!meta.get(forge?"modid":"id").getAsString().equals("bridgetap"))throw new IOException("The downloaded file is not BridgeTap");
   String mc=forge?meta.get("mcversion").getAsString():meta.getAsJsonObject("depends").get("minecraft").getAsString();if(!mc.equals("1.8.9"))throw new IOException("Update targets another Minecraft version");String v=meta.get("version").getAsString();UpdateRelease.newer(v,v);return v;
  }catch(RuntimeException ex){throw new IOException("Invalid BridgeTap JAR metadata",ex);}
 }
 public static void validate(Path jar,UpdateRelease release)throws IOException{if(Files.size(jar)!=release.size||!UpdateInstaller.hash(jar).equals(release.sha256))throw new IOException("Downloaded update checksum or size mismatch");if(!version(jar,release.forge).equals(release.version))throw new IOException("Downloaded update version does not match the release");}
 public static void download(UpdateRelease release,Path target,java.util.function.LongConsumer progress)throws IOException{
  HttpURLConnection c=connect(release.url);boolean success=false,created=false;
  try(InputStream in=c.getInputStream();OutputStream out=Files.newOutputStream(target,StandardOpenOption.CREATE_NEW)){
   created=true;byte[] b=new byte[65536];long total=0;int n;while((n=in.read(b))!=-1){total+=n;if(total>release.size)throw new IOException("Update download exceeds the release size");out.write(b,0,n);progress.accept(total);}success=true;
  }finally{c.disconnect();if(!success&&created)Files.deleteIfExists(target);}
  try{validate(target,release);}catch(IOException ex){Files.deleteIfExists(target);throw ex;}
 }
}
