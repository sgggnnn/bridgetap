import com.bridger.momentum.*;import java.nio.file.*;import java.util.*;
public class IslandSharedAxisRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  List<IslandTemplate.Block> cells=new ArrayList<>();for(int x=0;x<7;x++)for(int z=0;z<5;z++){cells.add(new IslandTemplate.Block(x,0,z,"minecraft:stone",0,0));cells.add(new IslandTemplate.Block(x,2,z+33,"minecraft:stone",0,1));}for(int x=1;x<=3;x++)cells.add(new IslandTemplate.Block(x,3,35,"minecraft:light_weighted_pressure_plate",0,1));
  for(int offset:new int[]{-1,1,2}){IslandTemplate t=new IslandTemplate("Off-centre copy",7,4,38,0,60,0,cells,2+offset,1,4,true);verify(t,2);}
  for(String name:new String[]{"Default","Summer","Winter","Fall Island","Halloween","Anniversary","Speedcubing"}){
   Path file=Files.createTempFile("shared-axis-",".btisland");try{try(java.io.InputStream in=IslandTemplate.class.getResourceAsStream("/assets/bridgetap/islands/defaults/"+name+".btisland")){Files.copy(in,file,StandardCopyOption.REPLACE_EXISTING);}verify(IslandTemplate.read(file),4);}finally{Files.deleteIfExists(file);}
  }
  System.out.println("PASS off-centre copy anchors and all seven real default islands share one source bridge axis");
 }
 static void verify(IslandTemplate t,int sourceRow){for(int turn=0;turn<4;turn++){
  int[] v=IslandRotation.point(0,1,0,0,turn);IslandAutoPlacement.Plan p=IslandAutoPlacement.fit(t,new IslandAutoPlacement.Point(20,65,-50),new IslandAutoPlacement.Point(20+29*v[0],68,-50+29*v[1]));
  int[] a=IslandRotation.point(sourceRow,0,t.anchorX,t.anchorZ,p.rotation);int lateral=v[0]!=0?a[1]+p.z:a[0]+p.x;check(lateral==(v[0]!=0?-50:20),t.name+": start/finish use different source axes");
 }}
}
