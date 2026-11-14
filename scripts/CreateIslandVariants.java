import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.zip.*;

/** Deterministic, editable scenery variants. Preview images render the actual voxel files. */
public class CreateIslandVariants {
 static String[] names={"Jade Sanctuary","Ember Ruins","Frost Observatory"};
 static Map<String,IslandTemplate.Block> blocks;
 static String key(int x,int y,int z){return x+","+y+","+z;}
 static void put(int x,int y,int z,String material,int meta,int part){blocks.put(key(x,y,z),new IslandTemplate.Block(x,y,z,"minecraft:"+material,meta,part));}
 static IslandTemplate.Block recolor(IslandTemplate.Block b,int theme,int part){
  String k=b.key.substring(10);int m=b.meta;
  if(theme==0){
   if(k.equals("clay")){k="stone";m=1;}
   else if(k.equals("stained_hardened_clay")){k="prismarine";m=b.meta==11?2:0;}
   else if(k.equals("cobblestone")){k="mossy_cobblestone";m=0;}
   else if(k.equals("stonebrick")&&m!=3)m=(b.x+b.z)%3==0?1:0;
   else if(k.equals("stained_glass_pane"))m=9;
   else if(k.equals("wool")||k.equals("carpet"))m=13;
   else if(k.equals("red_flower"))m=8;
   else if(k.equals("planks"))m=1;
  }else if(theme==1){
   if(k.equals("clay")){k="stained_hardened_clay";m=15;}
   else if(k.equals("stained_hardened_clay"))m=b.meta==9?14:b.meta==11?15:1;
   else if(k.equals("prismarine")){k="nether_brick";m=0;}
   else if(k.equals("stone")&&m==5)m=0;
   else if(k.equals("stonebrick")&&m!=3)m=2;
   else if(k.equals("grass")){k="dirt";m=1;}
   else if(k.equals("tallgrass")||k.equals("sapling")){k="deadbush";m=0;}
   else if(k.equals("wool")||k.equals("carpet")||k.equals("stained_glass_pane"))m=1;
   else if(k.equals("red_flower"))m=0;
  }else{
   if(k.equals("clay")||k.equals("stained_hardened_clay")){k=(b.x+b.y+b.z)%4==0?"packed_ice":"quartz_block";m=0;}
   else if(k.equals("prismarine")){k=b.meta==2?"lapis_block":"packed_ice";m=0;}
   else if(k.equals("cobblestone")||k.equals("stone")){k="stone";m=(b.x+b.z)%3==0?5:3;}
   else if(k.equals("stone_brick_stairs")){k="quartz_stairs";}
   else if(k.equals("stone_slab"))m=(m&8)|7;
   else if(k.equals("grass")){k="snow";m=0;}
   else if(k.equals("wool")||k.equals("carpet"))m=3;
   else if(k.equals("stained_glass_pane"))m=3;
   else if(k.equals("red_flower"))m=1;
   else if(k.equals("planks"))m=1;
  }
  return new IslandTemplate.Block(b.x,b.y,b.z,"minecraft:"+k,m,part);
 }
 static IslandTemplate make(IslandTemplate base,int theme){
  blocks=new LinkedHashMap<>();IslandParts parts=new IslandParts(base);
  for(int i=0;i<base.blocks.size();i++){IslandTemplate.Block b=recolor(base.blocks.get(i),theme,parts.finish[i]?1:0);blocks.put(key(b.x,b.y,b.z),b);}
  // Two small lamps behind the launch area. The centre/front lane stays open.
  for(int x:new int[]{2,7}){
   put(x,10,2,theme==2?"quartz_block":"stonebrick",theme==0?1:0,0);
   put(x,11,2,theme==1?"nether_brick_fence":"spruce_fence",0,0);
   put(x,12,2,theme==1?"nether_brick_fence":"spruce_fence",0,0);
   put(x,13,2,theme==1?"glowstone":"sea_lantern",0,0);
   put(x,14,2,"stone_slab",theme==2?7:5,0);
  }
  // Architecture belongs at the far back of the finish, away from the plates.
  if(theme==0){
   for(int x:new int[]{2,6})for(int y=14;y<=17;y++)put(x,y,43,"stonebrick",y==16?3:1,1);
   for(int x=2;x<=6;x++)put(x,18,43,"stone_slab",5,1);
   put(4,17,43,"sea_lantern",0,1);
   for(int x:new int[]{1,2,6,7}){put(x,18,42,"leaves",4,1);if(x==1||x==7)put(x,17,43,"leaves",4,1);}
   for(int[] p:new int[][]{{1,11,4},{8,11,5},{1,14,41},{7,14,43}})put(p[0],p[1],p[2],"leaves",4,p[2]>20?1:0);
  }else if(theme==1){
   for(int y=14;y<=17;y++)put(2,y,43,"stonebrick",2,1);
   for(int y=14;y<=16;y++)put(6,y,43,"nether_brick",0,1);
   for(int x=2;x<=4;x++)put(x,18,43,"stone_slab",6,1);
   put(3,17,43,"glowstone",0,1);put(4,17,43,"stained_glass",1,1);
   put(6,17,43,"stone_slab",6,1);
   for(int y=7;y<=10;y++)put(8,y,40,"iron_bars",0,1);
   put(8,6,40,"glowstone",0,1);put(8,5,40,"stone_slab",6,1);
   put(1,14,41,"deadbush",0,1);
  }else{
   for(int x:new int[]{2,6})for(int y=14;y<=17;y++)put(x,y,43,y==16?"packed_ice":"quartz_block",y==16?0:2,1);
   for(int x=2;x<=6;x++)put(x,18,43,"stone_slab",7,1);
   put(4,17,43,"sea_lantern",0,1);put(4,19,43,"stained_glass",3,1);
   put(1,14,41,"packed_ice",0,1);put(1,15,41,"stained_glass",3,1);
   put(7,14,44,"packed_ice",0,1);put(7,15,44,"stained_glass",3,1);
  }
  IslandTemplate result=new IslandTemplate("Default - "+names[theme],base.width,base.height,base.depth,base.originX,base.originY,base.originZ,new ArrayList<>(blocks.values()),base.anchorX,base.anchorY,base.anchorZ,true);
  // Prove that decoration changes do not move either measured attachment edge.
  for(int turn=0;turn<4;turn++){
   int[] dir=IslandRotation.point(0,1,0,0,turn);
   IslandAutoPlacement.Point start=new IslandAutoPlacement.Point(100,65,100),end=new IslandAutoPlacement.Point(100+dir[0]*29,68,100+dir[1]*29);
   IslandAutoPlacement.Plan a=IslandAutoPlacement.fit(base,start,end),b=IslandAutoPlacement.fit(result,start,end);
   if(a.x!=b.x||a.y!=b.y||a.z!=b.z||a.finishX!=b.finishX||a.finishY!=b.finishY||a.finishZ!=b.finishZ||a.rotation!=b.rotation)throw new AssertionError(names[theme]+" changed the bridge attachment");
  }
  return result;
 }
 static ZipFile assets;static Map<String,BufferedImage> textures=new HashMap<>();
 static BufferedImage texture(IslandTemplate.Block b){
  String k=b.key.substring(10);int m=b.meta;
  if(k.equals("stone"))k=m==1?"stone_granite":m==3?"stone_diorite":m==5?"stone_andesite":m==6?"stone_andesite_smooth":"stone";
  else if(k.equals("stonebrick"))k=m==1?"stonebrick_mossy":m==2?"stonebrick_cracked":m==3?"stonebrick_carved":"stonebrick";
  else if(k.equals("prismarine"))k=m==2?"prismarine_dark":"prismarine_rough";
  else if(k.equals("stained_hardened_clay"))k="hardened_clay_stained_"+colors[m];
  else if(k.equals("stained_glass")||k.equals("stained_glass_pane"))k="glass_"+colors[m];
  else if(k.equals("carpet")||k.equals("wool"))k="wool_colored_"+colors[m];
  else if(k.equals("grass"))k="grass_top";
  else if(k.equals("leaves"))k="leaves_oak";
  else if(k.equals("quartz_block"))k="quartz_block_side";
  else if(k.equals("stone_slab"))k=(m&7)==7?"quartz_block_side":(m&7)==6?"nether_brick":(m&7)==5?"stonebrick":"cobblestone";
  else if(k.equals("stone_brick_stairs"))k="stonebrick";else if(k.equals("stone_stairs")||k.equals("cobblestone_wall"))k="cobblestone";
  else if(k.equals("quartz_stairs"))k="quartz_block_side";
  else if(k.equals("spruce_fence"))k="planks_spruce";else if(k.equals("nether_brick_fence"))k="nether_brick";
  else if(k.equals("log"))k="log_spruce";else if(k.equals("wooden_slab"))k="planks_spruce";
  else if(k.equals("snow"))k="snow";else if(k.equals("packed_ice"))k="ice_packed";
  else if(k.equals("mossy_cobblestone"))k="cobblestone_mossy";
  else if(k.equals("light_weighted_pressure_plate"))k="gold_block";
  else if(k.equals("red_flower"))k=m==8?"flower_tulip_pink":m==1?"flower_blue_orchid":"flower_rose";
  else if(k.equals("sapling"))k="sapling_jungle";else if(k.equals("planks"))k="planks_spruce";
  if(!textures.containsKey(k))try{ZipEntry e=assets.getEntry("assets/minecraft/textures/blocks/"+k+".png");BufferedImage image=e==null?null:ImageIO.read(assets.getInputStream(e));if(image==null){image=new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(new Color(126,137,145));g.fillRect(0,0,16,16);g.dispose();}
   if(k.equals("leaves_oak")||k.equals("grass_top")||k.equals("tallgrass"))for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y);image.setRGB(x,y,(c&0xff000000)|(((c>>16)&255)*104/255<<16)|(((c>>8)&255)*174/255<<8)|(c&255)*74/255);}
   textures.put(k,image);}catch(Exception error){throw new RuntimeException(error);}
  return textures.get(k);
 }
 static String[] colors={"white","orange","magenta","light_blue","yellow","lime","pink","gray","silver","cyan","purple","blue","brown","green","red","black"};
 static double scale,ox,oy;static double[] proj(double x,double y,double z){return new double[]{ox+(x-z)*scale,oy+((x+z)*.5-y)*scale};}
 static void face(Graphics2D g,BufferedImage tex,double[][] v,float shade){
  Path2D polygon=new Path2D.Double();for(int i=0;i<4;i++){double[] p=proj(v[i][0],v[i][1],v[i][2]);if(i==0)polygon.moveTo(p[0],p[1]);else polygon.lineTo(p[0],p[1]);}polygon.closePath();
  double[] a=proj(v[0][0],v[0][1],v[0][2]),b=proj(v[1][0],v[1][1],v[1][2]),d=proj(v[3][0],v[3][1],v[3][2]);
  Shape clip=g.getClip();g.clip(polygon);g.drawImage(tex,new AffineTransform((b[0]-a[0])/tex.getWidth(),(b[1]-a[1])/tex.getWidth(),(d[0]-a[0])/tex.getHeight(),(d[1]-a[1])/tex.getHeight(),a[0],a[1]),null);
  if(shade>0){g.setColor(new Color(0,0,0,shade));g.fill(polygon);}g.setClip(clip);
 }
 static void model(Graphics2D g,IslandTemplate t,int part,int left,int top,int width,int height){
  List<IslandTemplate.Block> list=new ArrayList<>();for(IslandTemplate.Block b:t.blocks)if(b.part==part)list.add(b);
  int z0=list.stream().mapToInt(b->b.z).min().getAsInt();double minX=1e6,maxX=-1e6,minY=1e6,maxY=-1e6;
  for(IslandTemplate.Block b:list)for(int a=0;a<2;a++)for(int c=0;c<2;c++)for(int h=0;h<2;h++){double x=b.x+a,z=b.z-z0+c,y=b.y+h;minX=Math.min(minX,x-z);maxX=Math.max(maxX,x-z);minY=Math.min(minY,(x+z)*.5-y);maxY=Math.max(maxY,(x+z)*.5-y);}
  scale=Math.min(20,Math.min((width-48)/(maxX-minX),(height-32)/(maxY-minY)));ox=left+width/2.0-(minX+maxX)*scale/2;oy=top+height/2.0-(minY+maxY)*scale/2;
  list.sort(Comparator.comparingDouble(b->b.x+(b.z-z0)+b.y*.6));
  for(IslandTemplate.Block b:list){double x=b.x,y=b.y,z=b.z-z0,lo=0,hi=1,inset=0;String k=b.key;
   if(k.contains("slab")){lo=(b.meta&8)==0?0:.5;hi=lo+.5;}else if(k.contains("pressure_plate")||k.contains("carpet")){hi=.07;inset=.06;}else if(k.contains("fence")||k.contains("wall")){inset=.28;}else if(k.contains("pane")||k.contains("bars")){inset=.37;}else if(k.contains("flower")||k.contains("tallgrass")||k.contains("deadbush")||k.contains("sapling")){inset=.23;hi=.7;}else if(k.contains("button")){inset=.3;hi=.2;}
   double x1=x+inset,x2=x+1-inset,z1=z+inset,z2=z+1-inset,y1=y+lo,y2=y+hi;BufferedImage tex=texture(b);
   face(g,tex,new double[][]{{x1,y2,z2},{x2,y2,z2},{x2,y1,z2},{x1,y1,z2}},.24f);
   face(g,tex,new double[][]{{x2,y2,z2},{x2,y2,z1},{x2,y1,z1},{x2,y1,z2}},.4f);
   face(g,tex,new double[][]{{x1,y2,z1},{x2,y2,z1},{x2,y2,z2},{x1,y2,z2}},0);
  }
 }
 public static void main(String[] args)throws Exception{
  IslandTemplate base=IslandTemplate.read(Paths.get(args[0]));Path out=Paths.get(args[1]);Files.createDirectories(out);assets=new ZipFile(args[2]);
  BufferedImage preview=new BufferedImage(1560,1080,BufferedImage.TYPE_INT_RGB);Graphics2D g=preview.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.setColor(new Color(13,22,32));g.fillRect(0,0,1560,1080);
  for(int i=0;i<3;i++){IslandTemplate t=make(base,i);Path file=out.resolve(t.name+".btisland");Files.deleteIfExists(file);t.write(file);IslandTemplate checked=IslandTemplate.read(file);if(checked.blocks.size()!=t.blocks.size())throw new AssertionError("Round trip failed");
   int x=i*520;g.setColor(new Color(24,37,49));g.fillRoundRect(x+12,12,496,1056,18,18);g.setColor(new Color(i==0?0x80D5AA:i==1?0xF4AE66:0xA8DDF5));g.setFont(new Font("SansSerif",Font.BOLD,25));g.drawString(names[i],x+32,56);g.setFont(new Font("SansSerif",Font.PLAIN,13));g.setColor(new Color(163,180,193));g.drawString("DEFAULT VARIANT / ACTUAL VOXEL MODEL PREVIEW",x+32,83);
   g.setColor(new Color(219,230,236));g.drawString("START ISLAND",x+32,121);model(g,t,0,x+16,137,488,365);g.setColor(new Color(219,230,236));g.drawString("FINISH ISLAND",x+32,548);model(g,t,1,x+16,564,488,459);
   g.setColor(new Color(163,180,193));g.drawString(t.blocks.size()+" blocks / original anchors preserved",x+32,1047);System.out.println(t.name+": "+t.blocks.size()+" blocks; 4 orientation fits unchanged");
  }g.dispose();ImageIO.write(preview,"png",out.resolve("island-variants-preview.png").toFile());assets.close();
 }
}
