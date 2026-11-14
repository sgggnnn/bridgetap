import com.bridger.momentum.*;
import java.util.*;
public class IslandAutoPlacementRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static long key(int x,int y,int z){return ((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|(y&4095L);}
 static IslandTemplate template(){List<IslandTemplate.Block> b=new ArrayList<>();for(int x=2;x<=6;x++)for(int z=2;z<=6;z++)b.add(new IslandTemplate.Block(x,1,z,"minecraft:stone",0,0));for(int x=35;x<=39;x++)for(int z=2;z<=6;z++)b.add(new IslandTemplate.Block(x,3,z,"minecraft:stone",0,1));b.add(new IslandTemplate.Block(0,0,4,"minecraft:stone",0,0));b.add(new IslandTemplate.Block(48,7,4,"minecraft:stone",0,1));return new IslandTemplate("Decorated pair",49,8,9,0,0,0,b,4,2,4,true);}
 static IslandTemplate rotate(IslandTemplate t){List<IslandTemplate.Block> b=new ArrayList<>();for(IslandTemplate.Block v:t.blocks)b.add(new IslandTemplate.Block(t.depth-1-v.z,v.y,v.x,v.key,v.meta,v.part));return new IslandTemplate(t.name,t.depth,t.height,t.width,0,0,0,b,t.depth-1-t.anchorZ,t.anchorY,t.anchorX,true);}
 public static void main(String[] args){
  IslandTemplate t=template();int ax=6,az=4,bx=35,bz=4;
  for(int source=0;source<4;source++){
   for(int turn=0;turn<4;turn++)for(int gap:new int[]{16,28,51,83})for(int rise:new int[]{-2,0,3,4}){
    int[] direction=IslandRotation.point(1,0,0,0,turn);IslandAutoPlacement.Point start=new IslandAutoPlacement.Point(-100,65,20),end=new IslandAutoPlacement.Point(-100+direction[0]*(gap+1),65+rise,20+direction[1]*(gap+1));
    IslandAutoPlacement.Plan plan=IslandAutoPlacement.fit(t,start,end);int[] a=IslandRotation.point(ax,az,t.anchorX,t.anchorZ,plan.rotation),b=IslandRotation.point(bx+plan.finishX,bz+plan.finishZ,t.anchorX,t.anchorZ,plan.rotation);
    check(a[0]+plan.x==start.x&&a[1]+plan.z==start.z&&2+plan.y==start.y,"Start edge drift after rotation");
    check(b[0]+plan.x==end.x&&b[1]+plan.z==end.z&&4+plan.y+plan.finishY==end.y,"Finish edge/gap/height mismatch");
   }
   int nextAx=t.depth-1-az,nextBx=t.depth-1-bz;az=ax;bz=bx;ax=nextAx;bx=nextBx;t=rotate(t);
  }
  for(int q=0;q<4;q++)for(int gap:new int[]{16,28,51,83}){
   Map<Long,Integer> grid=new HashMap<>();int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minZ=Integer.MAX_VALUE,maxZ=Integer.MIN_VALUE;
   for(int x=0;x<=4;x++)for(int z=0;z<=4;z++){int[] p=IslandRotation.point(x,z,0,0,q);int wx=p[0]-100,wz=p[1]+20;grid.put(key(wx,64,wz),1);minX=Math.min(minX,wx);maxX=Math.max(maxX,wx);minZ=Math.min(minZ,wz);maxZ=Math.max(maxZ,wz);}
   for(int x=gap+5;x<=gap+9;x++)for(int z=0;z<=4;z++){int[] p=IslandRotation.point(x,z,0,0,q);grid.put(key(p[0]-100,67,p[1]+20),1);}
   for(int x=0;x<=4;x++)for(int z=12;z<=16;z++){int[] p=IslandRotation.point(x,z,0,0,q);grid.put(key(p[0]-100,64,p[1]+20),1);}
   int[] plate=IslandRotation.point(gap+7,2,0,0,q);grid.put(key(plate[0]-100,68,plate[1]+20),q%2==0?147:148);
   IslandAutoDetector.Route route=IslandAutoDetector.find((x,y,z)->grid.getOrDefault(key(x,y,z),0),minX,maxX,minZ,maxZ,65,0,0);
   int[] expected=IslandRotation.point(gap+5,2,0,0,q);check(route!=null&&route.plate&&route.finish.x==expected[0]-100&&route.finish.z==expected[1]+20&&route.finish.y==68,"Native finish detection picked neighbouring start or wrong edge");
  }
  check(IslandAutoDetector.find((x,y,z)->0,0,4,0,4,65,0,0)==null,"Missing terrain must not produce an invented placement");
  IslandTemplate original=template();List<IslandTemplate.Block> layered=new ArrayList<>(original.blocks);for(int x=35;x<=48;x++)for(int z=0;z<=8;z++)layered.add(new IslandTemplate.Block(x,0,z,"minecraft:stone",0,1));layered.add(new IslandTemplate.Block(37,4,4,"minecraft:heavy_weighted_pressure_plate",0,1));IslandTemplate plated=new IslandTemplate("Broad underside",49,8,9,0,0,0,layered,4,2,4,true);check(IslandAutoPlacement.deck(plated,new IslandParts(plated),1)[4]==4,"Finish plate must identify the deck above a wider decorative underside");
  System.out.println("PASS 256 decorated deck fits across all template/world orientations, native plate lanes and 16/28/51/83 gaps");
 }
}
