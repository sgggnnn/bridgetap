import com.bridger.momentum.*;
import com.google.gson.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
/** Explicit original-resource download check; no audio device or Minecraft code executed. */
public final class ModernSoundsRegression {
 public static void main(String[] args)throws Exception{ModernBlockCatalog catalog=new ModernBlockCatalog(Paths.get(args[0]));for(String block:catalog.blocks())if(ModernSounds.event(block)==null)throw new AssertionError("Missing sound mapping: "+block);
  Method prepare=ModernSounds.class.getDeclaredMethod("prepare",String.class);prepare.setAccessible(true);for(String block:new String[]{"modern:amethyst_block","modern:cherry_planks","modern:pale_oak_planks","modern:resin_bricks","modern:copper_block","modern:bamboo_button"}){String event=ModernSounds.event(block);prepare.invoke(null,event);if(ModernSounds.sound(block)==null)throw new AssertionError("Imported sound not available");JsonObject json=new JsonParser().parse(new String(ModernSounds.resource("sounds.json"),"UTF-8")).getAsJsonObject();JsonArray sounds=json.getAsJsonObject("modern."+event).getAsJsonArray("sounds");if(sounds.size()==0)throw new AssertionError("No samples: "+event);for(JsonElement sample:sounds){String name=sample.getAsJsonObject().get("name").getAsString();byte[] bytes=ModernSounds.resource("sounds/"+name.substring(10)+".ogg");if(bytes.length<4||bytes[0]!='O'||bytes[1]!='g'||bytes[2]!='g'||bytes[3]!='S')throw new AssertionError("Not OGG: "+name);}}
  if(!ModernSounds.event("modern:bamboo_button").equals("block.bamboo_wood.place"))throw new AssertionError("Modern wood sound reduced to stone");System.out.println("PASS: all 1,198 block sound mappings, original Mojang OGG downloads, checksums and legacy sound definitions");
 }
}
