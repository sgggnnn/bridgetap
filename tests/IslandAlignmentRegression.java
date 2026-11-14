import com.bridger.momentum.*;import java.util.*;
public class IslandAlignmentRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  List<IslandTemplate.Block> cells=new ArrayList<>();
  for(int x=0;x<5;x++)for(int z=0;z<5;z++){cells.add(new IslandTemplate.Block(x,1,z,"minecraft:stone",0,0));cells.add(new IslandTemplate.Block(x,3,24+z,"minecraft:stone",0,1));cells.add(new IslandTemplate.Block(x,4,24+z,"minecraft:light_weighted_pressure_plate",0,1));}
  // A large side decoration must not move the attachment row.
  for(int x=5;x<14;x++)for(int z=0;z<5;z++)cells.add(new IslandTemplate.Block(x,1,z,"minecraft:stone",0,0));
  IslandTemplate t=new IslandTemplate("Asymmetric deck",14,5,29,0,60,0,cells,2,2,4,true);
  for(int rotation=0;rotation<4;rotation++){
   int[] v=IslandRotation.point(0,1,0,0,rotation);IslandAutoPlacement.Point start=new IslandAutoPlacement.Point(100,65,-30),end=new IslandAutoPlacement.Point(100+v[0]*29,68,-30+v[1]*29);
   IslandAutoPlacement.Plan p=IslandAutoPlacement.fit(t,start,end);
   int[] a=IslandRotation.point(2,4,t.anchorX,t.anchorZ,p.rotation),b=IslandRotation.point(2+p.finishX,24+p.finishZ,t.anchorX,t.anchorZ,p.rotation);
   check(a[0]+p.x==start.x&&a[1]+p.z==start.z,"Asymmetric decoration shifted the start sideways");
   check(b[0]+p.x==end.x&&b[1]+p.z==end.z,"Finish plates shifted sideways");
   check(p.y+2==65&&p.y+p.finishY+4==68,"Pressure plates moved the floor height");
  }
  List<ReplayData.Block> nativeCells=new ArrayList<>();Map<Long,Integer> grid=new HashMap<>();
  for(int x=0;x<5;x++)for(int z=0;z<5;z++){ReplayData.Block b=new ReplayData.Block(x,64,z,1,0,0,false);nativeCells.add(b);grid.put(b.key(),1);}
  for(int x=1;x<4;x++){ReplayData.Block b=new ReplayData.Block(x,65,2,147,0,0,false);nativeCells.add(b);grid.put(b.key(),147);}
  nativeCells.add(new ReplayData.Block(7,60,2,1,0,0,false));nativeCells.add(new ReplayData.Block(7,61,2,148,0,0,false));for(ReplayData.Block b:nativeCells)grid.put(b.key(),b.id);
  for(int i=0;i<20;i++){Collections.shuffle(nativeCells,new Random(i));IslandPlatformDeck d=IslandPlatformDeck.find((x,y,z)->grid.getOrDefault(((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|(y&4095L),0),nativeCells,65,2,2);check(d.surface==65,"Decorative lower plate selected the wrong native floor");}
  System.out.println("PASS asymmetric side decorations and plate-covered floors in four orientations");
 }
}
