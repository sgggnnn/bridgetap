import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.zip.ZipFile;

/** Original paired floating sanctuary, authored from geometry rather than a copied island. */
public class CreateAetherIslands {
 static Map<String,IslandTemplate.Block> cells=new LinkedHashMap<>();
 static void block(int x,int y,int z,String key,int meta,int part){cells.put(x+","+y+","+z,new IslandTemplate.Block(x,y,z,"minecraft:"+key,meta,part));}
 static boolean octagon(int x,int z,int r){return Math.max(Math.abs(x),Math.abs(z))<=r&&Math.abs(x)+Math.abs(z)<=r+3;}
 static void island(int cz,int floor,int part){
  int toward=part==0?1:-1;
  // A hollow, faceted stone hull with four ribs: less mesh than a filled boulder.
  for(int y=floor-7;y<floor;y++){
   int r=Math.min(6,2+(y-(floor-7))*2/3);
   for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(octagon(dx,dz,r)){
    boolean rim=!octagon(dx+1,dz,r)||!octagon(dx-1,dz,r)||!octagon(dx,dz+1,r)||!octagon(dx,dz-1,r);
    if(!rim&&dx!=0&&dz!=0&&y!=floor-7)continue;
    String material=y==floor-2?"quartz_block":Math.abs(dx)==Math.abs(dz)?"stonebrick":"prismarine";
    int meta=material.equals("prismarine")?2:material.equals("quartz_block")?0:0;
    if(rim&&y==floor-4&&(dx==0||dz==0)){material="sea_lantern";meta=0;}
    block(8+dx,y,cz+dz,material,meta,part);
   }
  }
  // Octagonal marble platform, turquoise compass rose and four brass inlays.
  for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++)if(octagon(dx,dz,6)){
   boolean rim=!octagon(dx+1,dz,6)||!octagon(dx-1,dz,6)||!octagon(dx,dz+1,6)||!octagon(dx,dz-1,6);
   String material=rim?"quartz_block":Math.abs(dx)==Math.abs(dz)&&Math.abs(dx)<=3?"stained_hardened_clay":Math.abs(dx)+Math.abs(dz)<=2?"prismarine":"quartz_block";
   int meta=material.equals("stained_hardened_clay")?9:material.equals("prismarine")?2:0;
   if(Math.abs(dx)+Math.abs(dz)==4&&(dx==0||dz==0)){material="gold_block";meta=0;}
   block(8+dx,floor,cz+dz,material,meta,part);
  }
  // Five-wide runway; the exact two edge coordinates define a 28-block gap.
  for(int dx=-2;dx<=2;dx++)for(int depth=4;depth<=7;depth++)block(8+dx,floor,cz+toward*depth,dx==0?"quartz_block":"prismarine",dx==0?0:2,part);
  if(part==1)for(int dx=-2;dx<=2;dx++)block(8+dx,floor+1,cz-7,"light_weighted_pressure_plate",0,part);
  // Suspended diamond, attached to a narrow gold collar beneath the hull.
  int crystal=floor-9;
  block(8,crystal+2,cz,"gold_block",0,part);
  for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(Math.abs(dx)+Math.abs(dz)+Math.abs(dy)<=1)block(8+dx,crystal+dy,cz,dy==0&&dx==0&&dz==0?"sea_lantern":"stained_glass",dy==0&&dx==0&&dz==0?0:9,part);
  block(8,crystal-2,cz,"stained_glass",3,part);
  // The celestial gate sits at the rear. Nothing crosses the launch/arrival lane.
  int rear=cz-toward*4,radius=part==0?4:5,cy=floor+radius+2;
  for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++)if(Math.abs(Math.hypot(dx,dy)-radius)<.48)block(8+dx,cy+dy,rear,"quartz_block",Math.abs(dx)==radius?2:0,part);
  for(int x:new int[]{8-radius,8+radius})for(int y=floor+1;y<=cy;y++)block(x,y,rear,"quartz_block",2,part);
  block(8,cy+radius+1,rear,"gold_block",0,part);
  block(8,cy+radius+2,rear,"stone_slab",7,part);
  for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(Math.abs(dx)+Math.abs(dy)<=1)block(8+dx,cy+dy,rear,dx==0&&dy==0?"sea_lantern":"stained_glass",dx==0&&dy==0?0:3,part);
  // Small clipped gardens and side lights: low enough to leave the gate readable.
  for(int dx:new int[]{-5,5}){
   int z=cz+toward*2;
   block(8+dx,floor+1,z,"quartz_block",1,part);
   block(8+dx,floor+2,z,"leaves",4,part);
   block(8+dx,floor+2,z-toward,"leaves",4,part);
   block(8+dx,floor+3,z,"red_flower",1,part);
   block(8+dx,floor+1,cz-toward,"sea_lantern",0,part);
   block(8+dx,floor+2,cz-toward,"stone_slab",7,part);
  }
 }
 static IslandTemplate build(){
  island(8,11,0);island(51,14,1);
  IslandTemplate t=new IslandTemplate("Aether Gate",17,30,60,0,32,0,new ArrayList<>(cells.values()),8,12,15,true);
  IslandParts p=new IslandParts(t);
  if(!p.paired||p.axis!=1||p.direction!=1)throw new AssertionError("Paired island roles changed");
  int[] a=IslandAutoPlacement.deck(t,p,0),b=IslandAutoPlacement.deck(t,p,1);
  if(a[4]!=12||b[4]!=15)throw new AssertionError("Deck elevation changed");
  for(int group=0;group<2;group++)for(int x=6;x<=10;x++)for(int z=group==0?10:44;z<=(group==0?15:51);z++)for(int y=group==0?12:15;y<=(group==0?13:16);y++){
   IslandTemplate.Block cell=cells.get(x+","+y+","+z);
   if(cell!=null&&!cell.key.endsWith("pressure_plate"))throw new AssertionError("Obstructed runway");
  }
  for(int turn=0;turn<4;turn++)for(int gap:new int[]{16,28,51,83}){
   int[] d=IslandRotation.point(0,1,0,0,turn);
   IslandAutoPlacement.Plan fit=IslandAutoPlacement.fit(t,new IslandAutoPlacement.Point(100,65,100),new IslandAutoPlacement.Point(100+d[0]*(gap+1),68,100+d[1]*(gap+1)));
   int[] start=IslandRotation.point(8,15,8,15,fit.rotation),end=IslandRotation.point(8+fit.finishX,44+fit.finishZ,8,15,fit.rotation);
   if(start[0]+fit.x!=100||start[1]+fit.z!=100||fit.y+12!=65||end[0]+fit.x!=100+d[0]*(gap+1)||end[1]+fit.z!=100+d[1]*(gap+1)||fit.y+15+fit.finishY!=68)throw new AssertionError("Attachment mismatch: "+turn+" / "+gap);
  }
  return t;
 }
 public static void main(String[] args)throws Exception{
  Path out=Paths.get(args[0]);Files.createDirectories(out);IslandTemplate t=build();Path file=out.resolve("Aether Gate.btisland");Files.deleteIfExists(file);t.write(file);IslandTemplate loaded=IslandTemplate.read(file);if(loaded.blocks.size()!=t.blocks.size())throw new AssertionError("Round trip changed block data");
  CreateIslandVariants.assets=new ZipFile(args[1]);
  BufferedImage image=new BufferedImage(1700,1120,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  g.setColor(new Color(10,20,30));g.fillRect(0,0,1700,1120);g.setFont(new Font("SansSerif",Font.BOLD,42));g.setColor(new Color(226,241,246));g.drawString("AETHER GATE",54,72);
  g.setFont(new Font("SansSerif",Font.PLAIN,18));g.setColor(new Color(125,200,210));g.drawString("Original floating sanctuary / Quartz, dark prismarine, brass and suspended crystals",56,111);
  for(int part=0;part<2;part++){
   int x=part*840+20;g.setColor(new Color(20,34,47));g.fillRoundRect(x,145,820,903,20,20);g.setColor(new Color(221,234,242));g.setFont(new Font("SansSerif",Font.BOLD,19));g.drawString(part==0?"DEPARTURE / Compass terrace":"ARRIVAL / Celestial gate",x+26,184);
   CreateIslandVariants.model(g,t,part,x+10,205,800,810);
  }
  g.setFont(new Font("SansSerif",Font.PLAIN,15));g.setColor(new Color(148,170,187));g.drawString(t.blocks.size()+" blocks / two paired islands / clear five-wide runways / Minecraft 1.8.9",54,1081);g.drawString("Voxel model preview, not an in-game screenshot",1130,1081);g.dispose();ImageIO.write(image,"png",out.resolve("Aether Gate - preview.png").toFile());CreateIslandVariants.assets.close();
  System.out.println("PASS: original paired model, "+t.blocks.size()+" blocks, clear runways, exact attachments in 4 rotations x 4 gaps, portable-file round trip");
 }
}
