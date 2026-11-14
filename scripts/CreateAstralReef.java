import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.zip.ZipFile;

/** New material composition on the exact McPlay Default voxel silhouette. */
public class CreateAstralReef {
 static IslandTemplate.Block design(IslandTemplate.Block b,int part){
  String k=b.key.substring(10);int m=b.meta,floor=part==0?10:13;
  boolean full=k.equals("stone")||k.equals("clay")||k.equals("cobblestone")||k.equals("stonebrick")||k.equals("stained_hardened_clay")||k.equals("prismarine")||k.equals("sand");
  if(full){
   boolean carved=k.equals("stonebrick")&&m==3;
   boolean light=k.equals("prismarine")&&m==2;
   if(carved){k="gold_block";m=0;}
   else if(light){k="sea_lantern";m=0;}
   else if(b.y==floor){
    boolean inlay=(b.x==4||b.x==5)&&(b.z%3==0);
    k=inlay?"prismarine":"quartz_block";m=inlay?2:0;
   }else if(b.y==floor-2){k="quartz_block";m=0;}
   else if(b.y>floor){
    if(b.y==floor+3){k="gold_block";m=0;}
    else{k="quartz_block";m=2;}
   }else{
    boolean rib=(b.x==3||b.x==6)&&b.y>=floor-6;
    if(rib){k="quartz_block";m=2;}
    else{k="prismarine";m=(b.x+b.z)%5==0?0:2;}
   }
  }else if(k.equals("stone_brick_stairs")||k.equals("stone_stairs")){k="quartz_stairs";}
  else if(k.equals("stone_slab")){m=(m&8)|7;}
  else if(k.equals("stained_glass_pane")){m=9;}
  else if(k.equals("wool")||k.equals("carpet")){m=3;}
  else if(k.equals("red_flower")){m=1;}
  else if(k.equals("planks")){m=1;}
  return new IslandTemplate.Block(b.x,b.y,b.z,"minecraft:"+k,m,part);
 }
 static IslandTemplate copy(IslandTemplate t,String name,java.util.List<IslandTemplate.Block> cells){return new IslandTemplate(name,t.width,t.height,t.depth,t.originX,t.originY,t.originZ,cells,t.anchorX,t.anchorY,t.anchorZ,t.bothIslands);}
 public static void main(String[] args)throws Exception{
  IslandTemplate source=IslandTemplate.read(Paths.get(args[0]));IslandParts parts=new IslandParts(source);
  java.util.List<IslandTemplate.Block> original=new ArrayList<>(),changed=new ArrayList<>();
  for(int i=0;i<source.blocks.size();i++){
   IslandTemplate.Block b=source.blocks.get(i);int part=parts.finish[i]?1:0;
   original.add(new IslandTemplate.Block(b.x,b.y,b.z,b.key,b.meta,part));changed.add(design(b,part));
  }
  IslandTemplate base=copy(source,"McPlay Default",original),t=copy(source,"Astral Reef",changed);
  for(int i=0;i<source.blocks.size();i++){
   IslandTemplate.Block a=source.blocks.get(i),b=t.blocks.get(i);
   if(a.x!=b.x||a.y!=b.y||a.z!=b.z)throw new AssertionError("Voxel silhouette changed");
   if(a.key.endsWith("pressure_plate")&&(!a.key.equals(b.key)||a.meta!=b.meta))throw new AssertionError("Pressure plate changed");
   if(a.key.contains("stairs")&&((a.meta&7)!=(b.meta&7)||!b.key.contains("stairs")))throw new AssertionError("Stair orientation changed");
   if(a.key.contains("slab")&&((a.meta&8)!=(b.meta&8)||!b.key.contains("slab")))throw new AssertionError("Slab half changed");
  }
  for(int turn=0;turn<4;turn++)for(int gap:new int[]{16,28,51,83}){
   int[] d=IslandRotation.point(0,1,0,0,turn);IslandAutoPlacement.Point a=new IslandAutoPlacement.Point(100,65,100),b=new IslandAutoPlacement.Point(100+d[0]*(gap+1),68,100+d[1]*(gap+1));
   IslandAutoPlacement.Plan old=IslandAutoPlacement.fit(source,a,b),now=IslandAutoPlacement.fit(t,a,b);
   if(old.x!=now.x||old.y!=now.y||old.z!=now.z||old.rotation!=now.rotation||old.finishX!=now.finishX||old.finishY!=now.finishY||old.finishZ!=now.finishZ)throw new AssertionError("Attachment changed");
  }
  Path out=Paths.get(args[1]);Files.createDirectories(out);Path file=out.resolve("Astral Reef.btisland");Files.deleteIfExists(file);t.write(file);IslandTemplate read=IslandTemplate.read(file);if(read.blocks.size()!=source.blocks.size())throw new AssertionError("Round trip changed blocks");
  CreateIslandVariants.assets=new ZipFile(args[2]);
  BufferedImage image=new BufferedImage(1640,1320,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  g.setColor(new Color(10,20,30));g.fillRect(0,0,1640,1320);g.setFont(new Font("SansSerif",Font.BOLD,38));g.setColor(new Color(230,241,246));g.drawString("ASTRAL REEF",40,58);
  g.setFont(new Font("SansSerif",Font.PLAIN,17));g.setColor(new Color(135,200,212));g.drawString("Exact McPlay silhouette / new quartz ribs, midnight prismarine, cyan glass and gold accents",42,94);
  for(int side=0;side<2;side++)for(int part=0;part<2;part++){
   int x=side*810+16,y=128+part*568;
   g.setColor(new Color(side==0?0x18232D:0x1B303D));g.fillRoundRect(x,y,794,552,16,16);
   g.setColor(new Color(side==0?0xAEBCC8:0x89DFDA));g.setFont(new Font("SansSerif",Font.BOLD,18));g.drawString((side==0?"MCPLAY DEFAULT":"ASTRAL REEF")+" / "+(part==0?"Departure":"Arrival"),x+24,y+34);
   CreateIslandVariants.model(g,side==0?base:t,part,x+16,y+48,762,490);
  }
  g.setFont(new Font("SansSerif",Font.PLAIN,15));g.setColor(new Color(154,177,192));g.drawString("715 / 715 block positions preserved. Stair/slab orientations and bridge anchors unchanged.",40,1295);g.dispose();ImageIO.write(image,"png",out.resolve("Astral Reef - comparison.png").toFile());CreateIslandVariants.assets.close();
  System.out.println("PASS: exact 715-voxel silhouette, unchanged plates and partial-block orientations, 16 placement comparisons, portable-file round trip");
 }
}
