import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.zip.ZipFile;

/** McPlay-shaped woodland ruins, with small additions behind the bridge lanes. */
public class CreateWillowRuins {
 static Map<String,IslandTemplate.Block> blocks=new LinkedHashMap<>();
 static void put(int x,int y,int z,String k,int m,int part){blocks.put(x+","+y+","+z,new IslandTemplate.Block(x,y,z,"minecraft:"+k,m,part));}
 public static void main(String[] args)throws Exception{
  IslandTemplate base=IslandTemplate.read(Paths.get(args[0]));IslandParts parts=new IslandParts(base);
  for(int i=0;i<base.blocks.size();i++){
   IslandTemplate.Block b=base.blocks.get(i);String k=b.key.substring(10);int m=b.meta,part=parts.finish[i]?1:0;
   if(k.equals("clay")){if(b.y<(part==0?8:11)&&(b.x==3||b.x==6)){k="log";m=1;}else{k="stained_hardened_clay";m=13;}}
   else if(k.equals("stained_hardened_clay")){if(m==11){k="log";m=1;}else if(m==5)m=13;else{k="stonebrick";m=1;}}
   else if(k.equals("cobblestone")){k="mossy_cobblestone";m=0;}
   else if(k.equals("stone")){k="stonebrick";m=(b.x+b.y+b.z)%5==0?2:1;}
   else if(k.equals("prismarine")&&m==0){k="mossy_cobblestone";m=0;}
   else if(k.equals("stonebrick")&&m!=3)m=(b.x+b.z)%3==0?2:1;
   else if(k.equals("stained_glass_pane")||k.equals("wool")||k.equals("carpet"))m=13;
   else if(k.equals("planks"))m=1;
   else if(k.equals("red_flower"))m=8;
   else if(k.equals("sapling"))m=1;
   put(b.x,b.y,b.z,k,m,part);
  }
  // One warm lantern and one overgrown timber marker, behind the start deck.
  for(int x:new int[]{2,7})put(x,10,2,"stonebrick",1,0);
  for(int y=11;y<=12;y++)put(2,y,2,"spruce_fence",0,0);
  put(2,13,2,"glowstone",0,0);put(2,14,2,"stone_slab",5,0);
  for(int y=11;y<=13;y++)put(7,y,2,"log",1,0);
  put(7,14,2,"leaves",4,0);put(8,13,2,"leaves",4,0);
  // A weathered timber pergola at the far rear of the arrival island.
  for(int x:new int[]{2,6})for(int y=14;y<=16;y++)put(x,y,43,"log",1,1);
  for(int x=2;x<=6;x++)put(x,17,43,"planks",1,1);
  put(2,18,43,"wooden_slab",1,1);put(6,18,43,"wooden_slab",1,1);
  put(4,16,43,"glowstone",0,1);put(4,15,43,"spruce_fence",0,1);
  for(int[] p:new int[][]{{1,17,43},{1,18,42},{2,18,42},{3,18,42},{3,18,43},{6,17,44},{7,16,43},{1,11,4},{8,11,5},{1,14,40},{7,14,43}})put(p[0],p[1],p[2],"leaves",4,p[2]>20?1:0);
  IslandTemplate t=new IslandTemplate("Willow Ruins",base.width,base.height,base.depth,base.originX,base.originY,base.originZ,new ArrayList<>(blocks.values()),base.anchorX,base.anchorY,base.anchorZ,true);
  for(IslandTemplate.Block b:base.blocks){IslandTemplate.Block n=blocks.get(b.x+","+b.y+","+b.z);if(n==null)throw new AssertionError("Original silhouette removed");if(b.key.endsWith("pressure_plate")&&(!b.key.equals(n.key)||b.meta!=n.meta))throw new AssertionError("Finish plate changed");}
  for(int turn=0;turn<4;turn++)for(int gap:new int[]{16,28,51,83}){int[] d=IslandRotation.point(0,1,0,0,turn);IslandAutoPlacement.Point start=new IslandAutoPlacement.Point(100,65,100),end=new IslandAutoPlacement.Point(100+d[0]*(gap+1),68,100+d[1]*(gap+1));IslandAutoPlacement.Plan a=IslandAutoPlacement.fit(base,start,end),b=IslandAutoPlacement.fit(t,start,end);if(a.x!=b.x||a.y!=b.y||a.z!=b.z||a.rotation!=b.rotation||a.finishX!=b.finishX||a.finishY!=b.finishY||a.finishZ!=b.finishZ)throw new AssertionError("Bridge attachment changed");}
  Path out=Paths.get(args[1]);Files.createDirectories(out);Path file=out.resolve("Willow Ruins.btisland");Files.deleteIfExists(file);t.write(file);if(IslandTemplate.read(file).blocks.size()!=t.blocks.size())throw new AssertionError("Round trip changed model");
  CreateIslandVariants.assets=new ZipFile(args[2]);BufferedImage image=new BufferedImage(1640,1060,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  g.setColor(new Color(13,24,23));g.fillRect(0,0,1640,1060);g.setColor(new Color(177,214,159));g.setFont(new Font("SansSerif",Font.BOLD,40));g.drawString("WILLOW RUINS",40,61);
  g.setFont(new Font("SansSerif",Font.PLAIN,17));g.setColor(new Color(165,185,169));g.drawString("McPlay-shaped woodland ruins / Mossy stone, spruce roots, overgrowth and warm lanterns",42,99);
  for(int part=0;part<2;part++){int x=part*810+16;g.setColor(new Color(25,40,35));g.fillRoundRect(x,128,794,866,18,18);g.setColor(new Color(223,231,218));g.setFont(new Font("SansSerif",Font.BOLD,18));g.drawString(part==0?"DEPARTURE / Woodland terrace":"ARRIVAL / Overgrown timber pergola",x+24,168);CreateIslandVariants.model(g,t,part,x+12,194,770,770);}
  g.setFont(new Font("SansSerif",Font.PLAIN,14));g.setColor(new Color(155,179,161));g.drawString(t.blocks.size()+" blocks / original bridge anchors preserved / voxel model preview, not an in-game screenshot",40,1030);g.dispose();ImageIO.write(image,"png",out.resolve("Willow Ruins - preview.png").toFile());CreateIslandVariants.assets.close();
  System.out.println("PASS: "+t.blocks.size()+" blocks, original silhouette retained, finish plates unchanged, 16 attachment comparisons and portable-file round trip");
 }
}
