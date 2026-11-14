import com.bridger.momentum.*;
import java.util.*;
public final class IslandPlatformDeckRegression {
 static long key(int x,int y,int z){return ((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|(y&4095L);}
 public static void main(String[] args){
  List<ReplayData.Block> blocks=new ArrayList<>();Map<Long,Integer> grid=new HashMap<>();
  for(int x=0;x<5;x++)for(int z=0;z<5;z++)blocks.add(new ReplayData.Block(x,64,z,1,0,0,false));
  for(int x=0;x<2;x++)for(int z=0;z<5;z++)blocks.add(new ReplayData.Block(x,65,z,1,0,0,false));
  blocks.add(new ReplayData.Block(3,65,2,1,0,0,false));blocks.add(new ReplayData.Block(5,64,2,1,0,0,false));
  for(ReplayData.Block b:blocks)grid.put(b.key(),b.id);IslandAutoDetector.Grid access=(x,y,z)->grid.getOrDefault(key(x,y,z),0);
  for(int i=0;i<10;i++){Collections.shuffle(blocks,new Random(i));IslandPlatformDeck deck=IslandPlatformDeck.find(access,blocks,66.2,2,2);if(deck.surface!=65||deck.centerX!=2||deck.centerZ!=2||deck.edge(1,0,2).x!=4)throw new AssertionError("Decorations changed floor height, centre or platform edge");}
  List<IslandTemplate.Block> custom=new ArrayList<>();for(int x=0;x<5;x++)for(int z=0;z<5;z++)custom.add(new IslandTemplate.Block(x,0,z,"minecraft:stone",0));for(int z=0;z<5;z++)custom.add(new IslandTemplate.Block(0,1,z,"minecraft:stone",0));IslandTemplate t=new IslandTemplate("Deck with wall",5,2,5,0,64,0,custom,2,2,2);int[] box=IslandAutoPlacement.deck(t,new IslandParts(t),0);if(box[0]!=0||box[3]!=5||box[4]!=1)throw new AssertionError("Copied wall shifted or raised the deck");
  System.out.println("PASS stable floor, centre and broad edges despite walls, raised seed, thin protrusions and shuffled cells");
 }
}
