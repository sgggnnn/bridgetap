import com.bridger.momentum.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.jar.*;
import java.io.*;
public final class UpdateMetadataRegression {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  JsonObject release=new JsonObject();release.addProperty("tag_name","v9.0.0");release.addProperty("draft",false);release.addProperty("prerelease",false);JsonArray assets=new JsonArray();release.add("assets",assets);
  for(String edition:new String[]{".jar","-Forge-1.8.9.jar","-OneClient.jar"}){JsonObject a=new JsonObject();a.addProperty("name","BridgeTap-9.0.0"+edition);a.addProperty("digest","sha256:"+new String(new char[64]).replace('\0','a'));a.addProperty("size",100);a.addProperty("browser_download_url","https://untrusted.example/evil.jar");assets.add(a);}
  check(UpdateSource.select(release,"1.7.2",true).name.endsWith("Forge-1.8.9.jar")&&UpdateSource.select(release,"1.7.2",false).name.endsWith("OneClient.jar"),"Wrong loader selected or preferred edition ignored");
  check(UpdateSource.select(release,"1.7.2",false).url.startsWith("https://github.com/sgggnnn/bridgetap/releases/download/v9.0.0/"),"Remote asset URL was trusted");
  release.addProperty("prerelease",true);check(UpdateSource.select(release,"1.7.2",false)==null,"Prerelease auto-installed");release.addProperty("prerelease",false);check(UpdateSource.select(release,"9.1.0",false)==null,"Newer installed version downgraded");
  try{UpdateSource.connect("http://github.com/unsafe");throw new AssertionError("HTTP accepted");}catch(IOException expected){}try{UpdateSource.connect("https://untrusted.example/file");throw new AssertionError("Untrusted host accepted");}catch(IOException expected){}
  Path jar=Files.createTempFile("bridgetap-update-fixture-",".jar");try(JarOutputStream out=new JarOutputStream(Files.newOutputStream(jar))){out.putNextEntry(new JarEntry("fabric.mod.json"));out.write("{\"id\":\"bridgetap\",\"version\":\"9.0.0\",\"depends\":{\"minecraft\":\"1.8.9\"}}".getBytes("UTF-8"));out.closeEntry();}
  UpdateRelease good=new UpdateRelease("9.0.0","BridgeTap-9.0.0-OneClient.jar",UpdateInstaller.hash(jar),Files.size(jar),false);UpdateSource.validate(jar,good);
  try{UpdateSource.validate(jar,new UpdateRelease("9.0.1","BridgeTap-9.0.1-OneClient.jar",good.sha256,good.size,false));throw new AssertionError("Mismatched JAR version accepted");}catch(IOException expected){}
  try{UpdateSource.read(new ByteArrayInputStream(new byte[20]),10);throw new AssertionError("Oversized metadata accepted");}catch(IOException expected){}
  System.out.println("PASS: loader selection, stable-only checks, canonical download URLs, HTTPS allowlist, metadata bounds and JAR identity/version checks");
 }
}
