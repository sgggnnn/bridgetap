package com.bridger.momentum;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import com.bridger.momentum.mixin.AppearanceResourceAccessor;

/** Imports modern JSON geometry into the existing 1.8.9 renderer, without registry changes. */
public final class ModernAppearance {
 private static ModernBlockCatalog catalog;private static boolean attempted,packAdded;private static volatile boolean reload,audioReload;private static volatile ModernBlockCatalog pending;private static volatile String status="Import Minecraft 26.2 to browse its block appearances.";private static volatile boolean importing;
 private static volatile List<ModernBlockCatalog.Model> models=Collections.emptyList();private static volatile Set<String> textures=Collections.emptySet();private static volatile C_41102159 baked;private static String selected="";private static C_74688326 sprite;
 public static String status(){return status;}public static void resourcesChanged(){audioReload=true;}public static boolean importing(){return importing;}
 public static List<String> blocks(){return catalog==null?Collections.emptyList():catalog.blocks();}
 public static void download(){if(importing)return;importing=true;status="Downloading verified Minecraft 26.2 resources...";Thread thread=new Thread(()->{try{pending=ModernBlockCatalog.download(percent->status="Downloading Minecraft 26.2: "+percent+"%");status="26.2 resources verified. Loading block catalog...";}catch(Exception ex){status="Import failed: "+ex.getMessage();}finally{importing=false;}},"BridgeTap modern block import");thread.setDaemon(true);thread.start();}
 public static void browse(){if(importing)return;importing=true;Thread worker=new Thread(()->{try{final Path[] chosen={null};javax.swing.SwingUtilities.invokeAndWait(()->{javax.swing.JFileChooser dialog=new javax.swing.JFileChooser();dialog.setDialogTitle("Select Minecraft Java 26.2 client JAR");dialog.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Minecraft client archive (*.jar)","jar"));if(dialog.showOpenDialog(null)==javax.swing.JFileChooser.APPROVE_OPTION)chosen[0]=dialog.getSelectedFile().toPath();});if(chosen[0]!=null){pending=ModernBlockCatalog.importFile(chosen[0]);status="Local 26.2 archive imported.";}else status="Local import cancelled.";}catch(Exception ex){status="Local import failed: "+ex.getMessage();}finally{importing=false;}},"BridgeTap local model import");worker.setDaemon(true);worker.start();}
 public static void tick(){if(!attempted){attempted=true;if(Files.isRegularFile(ModernBlockCatalog.FILE))try{catalog=new ModernBlockCatalog(ModernBlockCatalog.FILE);}catch(Exception ex){status="Modern resource import: "+ex.getMessage();}}
  if(pending!=null){catalog=pending;pending=null;status="Imported "+catalog.blocks().size()+" block appearances from 26.2.";reload=true;}
  if(catalog==null)return;Minecraft mc=Minecraft.m_20213497();if(!packAdded){((AppearanceResourceAccessor)mc).bridgetap$resourcePacks().add(new Pack());packAdded=true;reload=true;}
  if(PlacementAppearance.enabled()&&PlacementAppearance.block().startsWith("modern:")&&!selected.equals(PlacementAppearance.block()))try{select(PlacementAppearance.block());}catch(Exception ex){status=ex.getMessage();}
  if(reload){reload=false;audioReload=false;baked=null;mc.m_16950819();baked=null;IslandReplacement.requestTerrainRebuild();}else if(audioReload){audioReload=false;mc.m_08508000().m_94505059(mc.m_11332062());}
 }
 public static void select(String key)throws IOException{if(catalog==null)throw new IOException("Import Minecraft 26.2 resources first");if(key.equals(selected)&&!models.isEmpty()){ModernSounds.select(key);return;}models=catalog.models(key);textures=ModernBlockCatalog.textures(models);selected=key;baked=null;reload=true;ModernSounds.select(key);boolean fallback=false;for(ModernBlockCatalog.Model model:models)fallback|=model.fallback;status=fallback?"Special block: cube approximation (1.8.9 renderer).":"26.2 model selected. Reloading textures...";}
 public static synchronized void stitch(C_83717615 atlas){PlacementAppearance.invalidateItemModel();for(String texture:textures)atlas.m_75854831(new C_22168528(texture));baked=null;}
 public static C_41102159 model(){if(!PlacementAppearance.enabled()||!PlacementAppearance.block().startsWith("modern:")||catalog==null||models.isEmpty())return null;C_41102159 cached=baked;if(cached!=null)return cached;synchronized(ModernAppearance.class){if(baked!=null)return baked;return bake();}}
 private static C_41102159 bake(){Minecraft mc=Minecraft.m_20213497();C_83717615 atlas=mc.m_62864302();String first=textures.isEmpty()?"missingno":textures.iterator().next();C_74688326 current=atlas.m_09307214(first);
  try{List<C_85259637> quads=new ArrayList<>();List<List<C_85259637>> faces=new ArrayList<>();for(int i=0;i<6;i++)faces.add(new ArrayList<>());C_48507627 bakery=new C_48507627();for(ModernBlockCatalog.Model source:models){C_68226429 model=C_68226429.m_42974913(source.json.toString());C_35838118 rotation=C_35838118.m_85094362(source.x,source.y);if(rotation==null)rotation=C_35838118.values()[0];for(C_82588014 part:model.m_78666412())for(Map.Entry<C_28786548,C_02019182> face:part.f_56000675.entrySet()){C_02019182 f=face.getValue();C_02019182 untinted=new C_02019182(null,-1,f.f_07511399,f.f_61711268);C_74688326 texture=atlas.m_09307214(ModernBlockCatalog.texture(source,f.f_07511399));C_85259637 quad=bakery.m_30248643(part.f_60895659,part.f_64587733,untinted,texture,face.getKey(),rotation,part.f_60744254,false,part.f_42387058);if(f.f_06937421==null)quads.add(quad);else faces.get(quad.m_22904726().ordinal()).add(quad);}}
   C_41102159 vanilla=mc.m_01427411().m_35215294().m_50076884(C_81592558.m_64320102(1).m_52891069());baked=new C_25236929(quads,faces,true,true,current,vanilla.m_61455214());sprite=current;return baked;
  }catch(RuntimeException ex){status="This model cannot be rendered by 1.8.9: "+ex.getClass().getSimpleName();return null;}
 }
 private static byte[] resource(C_22168528 location)throws IOException{if(!location.m_28565097().equals("bridgetap"))throw new FileNotFoundException();String path=location.m_88974028();return path.startsWith("sounds/")||path.equals("sounds.json")?ModernSounds.resource(path):catalog.resource("bridgetap",path);}
 private static final class Pack implements C_81137139 {
  public InputStream m_53481189(C_22168528 location)throws IOException{return new ByteArrayInputStream(resource(location));}
  public boolean m_07271527(C_22168528 location){try{resource(location);return true;}catch(IOException ex){return false;}}
  public Set<String> m_04072986(){return Collections.singleton("bridgetap");}
  public <T extends C_63452286>T m_02779181(C_97766904 serializer,String section){return null;}
  public BufferedImage m_56983715()throws IOException{try(InputStream in=ModernAppearance.class.getResourceAsStream("/assets/bridgetap/icon.png")){return ImageIO.read(in);}}
  public String m_72023005(){return "BridgeTap imported 26.2 resources";}
 }
}
