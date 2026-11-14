import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
public class IslandLegacyRolesRegression {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  Path root=Paths.get(args.length>0?args[0]:"src/main/resources/assets/bridgetap/islands/defaults");
  for(String name:new String[]{"Default","Summer","Winter","Fall Island","Halloween","Anniversary","Speedcubing"}){
   Path file=root.resolve(name+".btisland");if(!Files.exists(file)){file=Files.createTempFile("island-role-",".btisland");file.toFile().deleteOnExit();try(java.io.InputStream in=IslandTemplate.class.getResourceAsStream("/assets/bridgetap/islands/defaults/"+name+".btisland")){Files.copy(in,file,StandardCopyOption.REPLACE_EXISTING);}}IslandTemplate t=IslandTemplate.read(file);IslandParts p=new IslandParts(t);
   check(p.paired&&p.axis==1&&p.direction==1,name+": start/finish reversed");
   for(int i=0;i<t.blocks.size();i++)if(t.blocks.get(i).key.equals("minecraft:light_weighted_pressure_plate"))check(p.finish[i],name+": finish plate assigned to start");
   int[] a=IslandAutoPlacement.deck(t,p,0),b=IslandAutoPlacement.deck(t,p,1);
   check(a[2]<b[2],name+": deck order reversed");
   check(a[4]==(name.equals("Speedcubing")?15:11),name+": copied walkway elevation drifted into decoration");
   for(int q=0;q<4;q++){int[] v=IslandRotation.point(0,1,0,0,q);IslandAutoPlacement.Plan plan=IslandAutoPlacement.fit(t,new IslandAutoPlacement.Point(-120,65,40),new IslandAutoPlacement.Point(-120+v[0]*29,68,40+v[1]*29));check(plan.rotation==q,name+": wrong orientation");check(a[4]+plan.y==65&&b[4]+plan.y+plan.finishY==68,name+": wrong floor height");}
  }
  System.out.println("PASS real default island roles, four orientations and deck elevations");
 }
}
