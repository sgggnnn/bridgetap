import com.bridger.momentum.*;import java.util.*;
public class IslandNarrowFloorRegression {
 static long key(int x,int y,int z){return ((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|(y&4095L);}
 public static void main(String[] args){List<ReplayData.Block> cells=new ArrayList<>();List<IslandTemplate.Block> copy=new ArrayList<>();Map<Long,Integer> map=new HashMap<>();
  for(int x=0;x<7;x++)for(int z=0;z<7;z++){cells.add(new ReplayData.Block(x,63,z,1,0,0,false));copy.add(new IslandTemplate.Block(x,0,z,"minecraft:stone",0));}
  for(int x=1;x<=5;x++)for(int z=1;z<=5;z++){cells.add(new ReplayData.Block(x,64,z,1,0,0,false));copy.add(new IslandTemplate.Block(x,1,z,"minecraft:stone",0));if(x!=3){cells.add(new ReplayData.Block(x,65,z,1,0,0,false));copy.add(new IslandTemplate.Block(x,2,z,"minecraft:stone",0));}}
  for(ReplayData.Block b:cells)map.put(b.key(),b.id);IslandPlatformDeck deck=IslandPlatformDeck.find((x,y,z)->map.getOrDefault(key(x,y,z),0),cells,65,3,3);if(deck.surface!=65)throw new AssertionError("Actual corridor floor rejected in favour of lower exposed decorative rim: "+deck.surface);
  IslandTemplate t=new IslandTemplate("Narrow exposed corridor",7,3,7,0,63,0,copy,3,2,3);if(IslandAutoPlacement.deck(t,new IslandParts(t),0)[4]!=2)throw new AssertionError("Copied corridor floor selected one block too low");System.out.println("PASS native and copied floor heights with only five exposed tiles and a wider decorative underside");
 }
}
