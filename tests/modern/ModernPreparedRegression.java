import com.bridger.momentum.*;import java.nio.file.*;import java.util.*;import java.lang.reflect.*;
public class ModernPreparedRegression {
 public static void main(String[] args)throws Exception{ModernBlockCatalog catalog=new ModernBlockCatalog(Paths.get(args[0]));Field c=ModernAppearance.class.getDeclaredField("catalog"),st=ModernAppearance.class.getDeclaredField("stitched"),r=ModernAppearance.class.getDeclaredField("reload"),ready=ModernSounds.class.getDeclaredField("ready");for(Field f:new Field[]{c,st,r,ready})f.setAccessible(true);c.set(null,catalog);((Set<String>)st.get(null)).addAll(catalog.blockTextures());r.setBoolean(null,false);
  for(String key:new String[]{"modern:amethyst_block","modern:cherry_planks","modern:pale_oak_planks","modern:resin_bricks","modern:diamond_block","modern:stone"}){((Set<String>)ready.get(null)).add(ModernSounds.event(key));if(!ModernAppearance.readyForRandom(key))throw new AssertionError("Random block textures not prepared: "+key);ModernAppearance.select(key);if(r.getBoolean(null))throw new AssertionError("Block selection requested full resource reload: "+key);}
  System.out.println("PASS six modern material switches use prestitched textures without a full resource reload; "+catalog.blockTextures().size()+" block textures registered");
 }
}
