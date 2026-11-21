package com.bridger.momentum;
import java.awt.Color;import java.awt.image.BufferedImage;import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;import javax.imageio.*;import javax.imageio.stream.ImageInputStream;
/** Bounded local PNG transforms shared by export and preview. Alpha/animation layout stay unchanged. */
public final class OverlayRecolor {
 public static final class Settings {
  public final boolean enabled;public final int hue,saturation,brightness;public final Integer color;public final Selection selection;public final String texture;
  public Settings(boolean enabled,int hue,int saturation,int brightness){this.selection=null;this.texture=null;this.color=null;this.enabled=enabled;this.hue=Math.max(-180,Math.min(180,hue));this.saturation=Math.max(0,Math.min(200,saturation));this.brightness=Math.max(0,Math.min(200,brightness));}
  private Settings(int color){this.selection=null;this.texture=null;this.enabled=true;this.color=color&0xFFFFFF;this.hue=0;this.saturation=this.brightness=100;}
  private Settings(Settings source,String texture,Selection selection){this.enabled=source.enabled;this.color=source.color;this.hue=source.hue;this.saturation=source.saturation;this.brightness=source.brightness;this.texture=canonical(texture);this.selection=selection;}
  public Settings selected(String path,Selection mask){return new Settings(this,path,mask);}
  public Settings forTexture(String path){return texture==null||texture.equals(canonical(path))?this:original();}
  public static Settings color(int rgb){return new Settings(rgb);}
  public boolean active(){return color!=null||enabled&&(hue!=0||saturation!=100||brightness!=100);}
  public static Settings original(){return new Settings(false,0,100,100);}
 }
 public static final class Selection {
  public final int width,height;private final BitSet cells;
  public Selection(int width,int height,BitSet cells){if(width<1||height<1||width>512||height>256)throw new IllegalArgumentException("Invalid selection size");this.width=width;this.height=height;this.cells=(BitSet)cells.clone();this.cells.clear(width*height,Math.max(width*height,this.cells.length()));}
  public boolean includes(int x,int y,int w,int h){return cells.get(Math.min(height-1,(int)((long)y*height/h))*width+Math.min(width-1,(int)((long)x*width/w)));}
  public BitSet cells(){return (BitSet)cells.clone();}
  public int count(){return cells.cardinality();}
 }
 private static String canonical(String path){String mapped=SandstoneOverlayCreator.mapped(path,true);return mapped!=null?mapped:SkyOverlayCreator.normalizeSkyPath(path);}
 public static BufferedImage apply(BufferedImage image,Settings settings){
  BufferedImage out=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);float[] hsb=new float[3];
  for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
   int c=image.getRGB(x,y);if(settings.active()&&(settings.selection==null||settings.selection.includes(x,y,image.getWidth(),image.getHeight()))){Color.RGBtoHSB((c>>16)&255,(c>>8)&255,c&255,hsb);float hue,sat,value;if(settings.color!=null){float originalValue=hsb[2];Color.RGBtoHSB((settings.color>>16)&255,(settings.color>>8)&255,settings.color&255,hsb);hue=hsb[0];sat=hsb[1];value=originalValue*hsb[2];}else{hue=(hsb[0]+settings.hue/360f+1)%1;sat=Math.min(1,hsb[1]*settings.saturation/100f);value=Math.min(1,hsb[2]*settings.brightness/100f);}c=(c&0xFF000000)|(Color.HSBtoRGB(hue,sat,value)&0xFFFFFF);}out.setRGB(x,y,c);
  }return out;
 }
 private static BufferedImage decode(InputStream input,boolean thumbnail)throws IOException{
  try(ImageInputStream in=ImageIO.createImageInputStream(input)){
   Iterator<ImageReader> readers=ImageIO.getImageReaders(in);if(!readers.hasNext())throw new IOException("The selected image is not a readable PNG.");ImageReader r=readers.next();
   try{if(!r.getFormatName().equalsIgnoreCase("png"))throw new IOException("Overlay textures must be PNG images.");r.setInput(in);int w=r.getWidth(0),h=r.getHeight(0);if(w<1||h<1||w>16384||h>16384||(long)w*h>(thumbnail?268435456L:16777216L))throw new IOException(thumbnail?"Image exceeds the safe preview dimensions.":"Image too large for full-resolution recoloring (16 million pixels maximum). The thumbnail preview can still be displayed.");ImageReadParam param=r.getDefaultReadParam();if(thumbnail){int scale=Math.max(1,Math.max((w+511)/512,(h+255)/256));param.setSourceSubsampling(scale,scale,0,0);}return r.read(0,param);}finally{r.dispose();}
  }
 }
 static byte[] bounded(InputStream in,int limit)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("Overlay image exceeds the 64 MiB file limit.");out.write(b,0,n);}return out.toByteArray();}
 public static byte[] png(byte[] source,Settings settings)throws IOException{
  if(!settings.active())return source;if(source.length>64*1024*1024)throw new IOException("Overlay image exceeds the recolor size limit.");
  BufferedImage image=decode(new ByteArrayInputStream(source),false),colored=apply(image,settings);ByteArrayOutputStream out=new ByteArrayOutputStream();if(!ImageIO.write(colored,"png",out))throw new IOException("Could not encode recolored PNG.");return out.toByteArray();
 }
 public static void copy(InputStream in,OutputStream out,String path,Settings settings)throws IOException{
  settings=settings.forTexture(path);
  if(settings.active()&&path.toLowerCase(Locale.ROOT).endsWith(".png")){out.write(png(bounded(in,64*1024*1024),settings.forTexture(path)));return;}
  byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
 }
 public static final class Preview {public final BufferedImage original,colored;public final String path;public final int index,total;Preview(BufferedImage image,String path,int index,int total,Settings settings){this.original=image;this.colored=apply(image,settings.forTexture(path));this.path=path;this.index=index;this.total=total;}}
 private static boolean candidate(String path,boolean sandstone,boolean red){return sandstone?SandstoneOverlayCreator.texturePath(path,red):path.toLowerCase(Locale.ROOT).endsWith(".png")&&SkyOverlayCreator.isSkyPath(path,red);}
 public static Preview recolor(Preview preview,Settings settings){return new Preview(preview.original,preview.path,preview.index,preview.total,settings);}
 private static void sortLayers(List<String> names,boolean sandstone){names.sort(Comparator.comparingInt((String name)->sandstone?0:name.replace('\\','/').toLowerCase(Locale.ROOT).contains("/sky/")?0:1).thenComparing(String::compareTo));}
 /** Enumerates textures without decoding them. The browser and preview share this exact inventory. */
 public static List<String> textures(File pack,boolean sandstone,boolean red)throws IOException{
  List<String> names=new ArrayList<>();
  if(pack==null||!pack.exists())throw new IOException("This source pack no longer exists. Refresh the pack list.");
  if(pack.isDirectory()){
   Path root=pack.toPath().toRealPath();try(java.util.stream.Stream<Path> stream=Files.walk(root,16)){Iterator<Path> it=stream.iterator();int visited=0;while(it.hasNext()){
    Path file=it.next();if(++visited>100000)throw new IOException("Too many files to preview this pack.");String name=root.relativize(file).toString().replace('\\','/');
    if(Files.isRegularFile(file)&&candidate(name,sandstone,red)&&file.toRealPath().startsWith(root)&&names.size()<128)names.add(name);
   }}
  }else try(ZipFile zip=new ZipFile(pack)){Enumeration<? extends ZipEntry> entries=zip.entries();int visited=0;while(entries.hasMoreElements()){
   ZipEntry entry=entries.nextElement();if(++visited>100000)throw new IOException("Too many files to preview this pack.");if(!entry.isDirectory()&&candidate(entry.getName(),sandstone,red)&&names.size()<128)names.add(entry.getName());
  }}
  sortLayers(names,sandstone);return names;
 }
 public static Preview preview(File pack,boolean sandstone,boolean red,int index,Settings settings)throws IOException{
  List<String> names=textures(pack,sandstone,red);
  if(names.isEmpty())throw new IOException("No "+(sandstone?"sandstone":"custom sky")+" textures in this source. Choose a compatible pack from the list.");
  int picked=Math.floorMod(index,names.size());String name=names.get(picked);
  if(pack.isDirectory()){
   Path root=pack.toPath().toRealPath(),file=root.resolve(name).toRealPath();if(!file.startsWith(root))throw new IOException("Texture is outside the source pack.");
   try(InputStream in=Files.newInputStream(file)){return new Preview(decode(new ByteArrayInputStream(bounded(in,64*1024*1024)),true),name,picked,names.size(),settings);}
  }
  try(ZipFile zip=new ZipFile(pack);InputStream in=zip.getInputStream(zip.getEntry(name))){return new Preview(decode(new ByteArrayInputStream(bounded(in,64*1024*1024)),true),name,picked,names.size(),settings);}
 }
 private OverlayRecolor(){}
}
