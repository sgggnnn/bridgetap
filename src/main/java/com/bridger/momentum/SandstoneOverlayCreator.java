package com.bridger.momentum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import com.google.gson.*;

/** Local-only extraction of vanilla sandstone textures. Never modifies a source pack. */
public final class SandstoneOverlayCreator {
 public static final String OUTPUT_FOLDER="BridgeTap Sandstone Overlays";
 private static final int FILE_LIMIT=8*1024*1024,TOTAL_LIMIT=64*1024*1024,ENTRY_LIMIT=100000;
 private static final Map<String,String> PATHS=new LinkedHashMap<>();
 static {
  for(String prefix:new String[]{"","red_"}){
   for(String face:new String[]{"normal","top","bottom","carved","smooth"}){
    String legacy=prefix+"sandstone_"+face+".png";
    PATHS.put("assets/minecraft/textures/blocks/"+legacy,legacy);
   }
   String[][] modern={{"sandstone","sandstone_normal"},{"sandstone_top","sandstone_top"},{"sandstone_bottom","sandstone_bottom"},{"chiseled_sandstone","sandstone_carved"},{"cut_sandstone","sandstone_smooth"}};
   for(String[] pair:modern)PATHS.put("assets/minecraft/textures/block/"+pair[0].replace("sandstone",prefix+"sandstone")+".png",prefix+pair[1]+".png");
  }
 }
 static String mapped(String name,boolean red){
  if(name.indexOf('\\')>=0||name.startsWith("/")||Arrays.asList(name.split("/")).contains(".."))return null;
  boolean meta=name.endsWith(".png.mcmeta");String png=meta?name.substring(0,name.length()-7):name;
  String target=PATHS.get(png);if(target==null||(!red&&target.startsWith("red_")))return null;
  return "assets/minecraft/textures/blocks/"+target+(meta?".mcmeta":"");
 }
 public static boolean texturePath(String path,boolean red){return path.endsWith(".png")&&mapped(path,red)!=null;}
 public static String defaultName(File source){return source.getName().replaceFirst("(?i)\\.zip$","")+" [Sandstone Overlay]";}
 private static byte[] read(InputStream in,int limit)throws IOException{
  ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;
  while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("A sandstone texture is too large (8 MiB maximum).");out.write(b,0,n);}return out.toByteArray();
 }
 private static void validate(String name,byte[] bytes)throws IOException{
  if(name.endsWith(".mcmeta")){
   try{JsonElement root=new JsonParser().parse(new String(bytes,StandardCharsets.UTF_8));if(!root.isJsonObject())throw new IOException("Invalid texture animation metadata.");}
   catch(RuntimeException|StackOverflowError ex){throw new IOException("Invalid texture animation metadata.");}return;
  }
  try(ImageInputStream stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
   Iterator<ImageReader> readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw new IOException("A sandstone texture is not a valid PNG.");
   ImageReader reader=readers.next();try{if(!reader.getFormatName().equalsIgnoreCase("png"))throw new IOException("Sandstone textures must be PNG images.");reader.setInput(stream);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>4096||h>16384||(long)w*h>16777216)throw new IOException("A sandstone texture exceeds the safe image size.");}finally{reader.dispose();}
  }
 }
 private static Map<String,byte[]> collect(File source,boolean red)throws IOException{
  Map<String,byte[]> result=new TreeMap<>();Map<String,Integer> priorities=new HashMap<>();Set<String> seen=new HashSet<>();int[] total={0},entries={0};
  if(source==null||!source.exists())throw new IOException("Select an existing resource pack first.");
  if(source.isDirectory()){
   Path root=source.toPath().toRealPath();
   for(Map.Entry<String,String> entry:PATHS.entrySet())for(String suffix:new String[]{"",".mcmeta"}){
    String path=entry.getKey()+suffix,target=mapped(path,red);if(target==null)continue;
    Path file=root.resolve(path);if(!Files.isRegularFile(file))continue;if(!file.toRealPath().startsWith(root))throw new IOException("Texture links outside the source pack are not supported.");
    try(InputStream in=Files.newInputStream(file)){add(result,priorities,target,path,read(in,suffix.isEmpty()?FILE_LIMIT:65536),total);}
   }
  }else{
   try(ZipFile zip=new ZipFile(source)){
    Enumeration<? extends ZipEntry> it=zip.entries();while(it.hasMoreElements()){
     ZipEntry entry=it.nextElement();if(++entries[0]>ENTRY_LIMIT)throw new IOException("This resource pack has too many files.");
     String path=entry.getName(),target=mapped(path,red);if(target==null||entry.isDirectory())continue;
     if(!seen.add(path))throw new IOException("The source pack contains duplicate sandstone files.");
     int limit=path.endsWith(".mcmeta")?65536:FILE_LIMIT;if(entry.getSize()>limit)throw new IOException("A sandstone file exceeds the size limit.");
     try(InputStream in=zip.getInputStream(entry)){add(result,priorities,target,path,read(in,limit),total);}
    }
   }
  }
  result.entrySet().removeIf(e->e.getKey().endsWith(".mcmeta")&&!result.containsKey(e.getKey().substring(0,e.getKey().length()-7)));
  return result;
 }
 private static void add(Map<String,byte[]> result,Map<String,Integer> priorities,String target,String source,byte[] bytes,int[] total)throws IOException{
  total[0]+=bytes.length;if(total[0]>TOTAL_LIMIT)throw new IOException("Sandstone textures exceed the 64 MiB extraction limit.");validate(target,bytes);
  int priority=source.contains("/blocks/")?2:1;if(priority>=priorities.getOrDefault(target,0)){result.put(target,bytes);priorities.put(target,priority);}
 }
 public static int scan(File source,boolean red)throws IOException{int n=0;for(String path:collect(source,red).keySet())if(path.endsWith(".png"))n++;return n;}
 public static SkyOverlayCreator.CreateResult createOverlay(File source,File packDirectory,String title,boolean red){
  return createOverlay(source,packDirectory,title,red,OverlayRecolor.Settings.original());
 }
 public static SkyOverlayCreator.CreateResult createOverlay(File source,File packDirectory,String title,boolean red,OverlayRecolor.Settings recolor){
  Path temporary=null;
  try{
   Map<String,byte[]> files=collect(source,red);int count=0;for(String path:files.keySet())if(path.endsWith(".png"))count++;
   if(count==0)return new SkyOverlayCreator.CreateResult(false,0,null,"No sandstone textures found. Choose another pack.");
   if(title==null||title.trim().isEmpty())title=defaultName(source);
   String stem=SkyOverlayCreator.sanitizeFilename(title.trim()).replaceFirst("(?i)\\.zip$","");stem=stem.replaceAll("[\\p{Cntrl}]","").trim();if(stem.length()>100)stem=stem.substring(0,100);if(stem.isEmpty()||stem.equals(".")||stem.equals(".."))stem="Sandstone Overlay";
   Path folder=packDirectory.toPath().resolve(OUTPUT_FOLDER);Files.createDirectories(folder);temporary=Files.createTempFile(folder,".sandstone-",".tmp");
   JsonObject meta=new JsonObject(),pack=new JsonObject();pack.addProperty("pack_format",1);pack.addProperty("description","[Sandstone Overlay] "+stem);meta.add("pack",pack);
   try(ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(temporary))){
    out.putNextEntry(new ZipEntry("pack.mcmeta"));out.write(meta.toString().getBytes(StandardCharsets.UTF_8));out.closeEntry();
    for(Map.Entry<String,byte[]> entry:files.entrySet()){out.putNextEntry(new ZipEntry(entry.getKey()));out.write(entry.getKey().endsWith(".png")?OverlayRecolor.png(entry.getValue(),recolor.forTexture(entry.getKey())):entry.getValue());out.closeEntry();}
   }
   Path target=folder.resolve(stem+".zip");int suffix=2;while(true){try{Files.move(temporary,target);temporary=null;break;}catch(FileAlreadyExistsException collision){target=folder.resolve(stem+" ("+suffix+++ ").zip");}}
   return new SkyOverlayCreator.CreateResult(true,count,target.toFile(),"Sandstone overlay created. Activate it when ready.");
  }catch(IOException|RuntimeException ex){return new SkyOverlayCreator.CreateResult(false,0,null,ex instanceof IOException?ex.getMessage():"Could not read this pack. Existing packs were kept.");}
  finally{if(temporary!=null)try{Files.deleteIfExists(temporary);}catch(IOException ignored){}}
 }
 private SandstoneOverlayCreator(){}
}
