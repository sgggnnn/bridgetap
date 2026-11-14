import com.bridger.momentum.*;import java.util.*;
public class IslandBridgeRowRegression {
 static long key(int x,int y,int z){return ((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|(y&4095L);}
 public static void main(String[] args){
  for(int q=0;q<4;q++)for(int shift:new int[]{-3,-2,-1,1,2,3}){
   List<ReplayData.Block> a=new ArrayList<>(),b=new ArrayList<>();Map<Long,Integer> map=new HashMap<>();
   for(int x=-5;x<=5;x++)for(int z=0;z<=4;z++){int[] p=IslandRotation.point(x,z,0,0,q);a.add(new ReplayData.Block(p[0]-100,64,p[1]+20,1,0,0,false));p=IslandRotation.point(x,z+33,0,0,q);b.add(new ReplayData.Block(p[0]-100,67,p[1]+20,1,0,0,false));}
   for(int x=shift-1;x<=shift+1;x++){int[] p=IslandRotation.point(x,35,0,0,q);b.add(new ReplayData.Block(p[0]-100,68,p[1]+20,147,0,0,false));}
   for(ReplayData.Block c:a)map.put(c.key(),c.id);for(ReplayData.Block c:b)map.put(c.key(),c.id);IslandAutoDetector.Grid grid=(x,y,z)->map.getOrDefault(key(x,y,z),0);
   IslandPlatformDeck start=IslandPlatformDeck.find(grid,a,65,-100,20),end=IslandPlatformDeck.find(grid,b,68,-100,20);int[] v=IslandRotation.point(0,1,0,0,q),plate=IslandRotation.point(shift,35,0,0,q);
   int expected=v[0]!=0?plate[1]+20:plate[0]-100;int row=IslandBridgeRow.resolve(start,end,b,grid,v[0],v[1],null);if(row!=expected)throw new AssertionError("Plate row drift "+shift+" rotation "+q);
   for(int side=-3;side<=3;side++)if(IslandBridgeRow.resolve(start,end,b,grid,v[0],v[1],expected+side)!=expected)throw new AssertionError("Previous off-centre bridge moved both islands");
   List<ReplayData.Block> withoutPlates=new ArrayList<>();for(ReplayData.Block c:b)if(c.id!=147)withoutPlates.add(c);IslandAutoDetector.Grid noPlates=(x,y,z)->{int id=grid.block(x,y,z);return id==147?0:id;};if(IslandBridgeRow.resolve(start,end,withoutPlates,noPlates,v[0],v[1],expected)!=expected)throw new AssertionError("Recorded row fallback lost");
   IslandAutoPlacement.Point first=start.edge(v[0],v[1],row),last=end.edge(-v[0],-v[1],row);if((v[0]!=0?first.z:first.x)!=(v[0]!=0?last.z:last.x))throw new AssertionError("Different endpoint rows");
  }
  System.out.println("PASS native bridge row offsets up to +/-3, four rotations, stable plate priority and recorded fallback");
 }
}
