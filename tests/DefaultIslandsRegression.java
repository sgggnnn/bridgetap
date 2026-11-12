import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
public final class DefaultIslandsRegression {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  int savedBefore=IslandLibrary.list("saved").size();
  Path personal=IslandLibrary.save(new IslandTemplate("Winter",1,1,1,0,64,0,Collections.singletonList(new IslandTemplate.Block(0,0,0,"minecraft:stone",0))),"saved");byte[] original=Files.readAllBytes(personal);
  List<Path> files=IslandLibrary.list("defaults");check(files.size()==6,"Fresh installation must include six default islands");
  Map<String,Integer> expected=new HashMap<>();expected.put("Default",715);expected.put("Fall Island",703);expected.put("Halloween",703);expected.put("Speedcubing",665);expected.put("Summer",703);expected.put("Winter",701);
  for(Path file:files){
   IslandTemplate island=IslandTemplate.read(file);check(expected.containsKey(island.name)&&expected.get(island.name)==island.blocks.size(),"Missing or altered bundled model");
   IslandParts parts=new IslandParts(island);check(island.bothIslands&&parts.paired,"Start/finish split lost");int finish=0;for(boolean end:parts.finish)if(end)finish++;check(finish>0&&finish<island.blocks.size(),"Empty start or finish island");
   try{IslandLibrary.delete(file,"defaults");throw new AssertionError("Default template can be deleted");}catch(java.io.IOException expectedFailure){}
  }
  check(Arrays.equals(original,Files.readAllBytes(personal))&&IslandLibrary.list("saved").size()==savedBefore+1,"Defaults overwrite or duplicate personal library");
  Path first=files.get(0);byte[] bytes=Files.readAllBytes(first);check(IslandLibrary.list("defaults").size()==6&&Arrays.equals(bytes,Files.readAllBytes(first)),"Refresh duplicates or rewrites defaults");
  Path copy=IslandLibrary.save(IslandTemplate.read(first),"saved");IslandLibrary.rename(copy,"My custom default");IslandLibrary.delete(copy,"saved");check(Arrays.equals(bytes,Files.readAllBytes(first)),"Editing a personal copy changed the bundled model");
  Path sameName=IslandLibrary.folder("defaults").resolve("Winter.btisland");Files.write(sameName,original);IslandLibrary.list("defaults");check(Arrays.equals(original,Files.readAllBytes(sameName)),"Existing local default file overwritten");
  System.out.println("PASS: six bundled paired islands, fresh-install/refresh, separate personal files, protected originals and editable copies");
 }
}
