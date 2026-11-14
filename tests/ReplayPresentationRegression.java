import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.*;
public class ReplayPresentationRegression {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static byte[] unpack(Path path)throws Exception{try(InputStream in=new GZIPInputStream(Files.newInputStream(path));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);return out.toByteArray();}}
 public static void main(String[] args)throws Exception{
  byte[] biome=new byte[256];Arrays.fill(biome,(byte)21);Map<Long,byte[]> map=new HashMap<>();map.put(ReplayPresentation.key(-2,3),biome);
  ReplayPresentation p=new ReplayPresentation(123456,.5f,.25f,map);biome[0]=0;check(p.biomes(-2,3)[0]==21,"Biome input must be copied");byte[] copy=p.biomes(-2,3);copy[0]=1;check(p.biomes(-2,3)[0]==21,"Biome output must be copied");
  ReplayData data=new ReplayData(Arrays.asList(new ReplayData.Frame(0,64,0),new ReplayData.Frame(1,64,0),new ReplayData.Frame(2,64,0)),Collections.emptyList(),false).withPresentation(p);
  Path dir=Files.createTempDirectory("replay-presentation-");Path path=dir.resolve("v7.btreplay");data.write(path);ReplayData read=ReplayData.read(path);
  check(read.formatVersion==7&&read.presentation.worldTime==123456&&read.presentation.rain==.5f&&read.presentation.biomes(-2,3)[0]==21,"Sky roundtrip");
  byte[] raw=unpack(path);raw[7]=6;Path legacy=dir.resolve("v6.btreplay");
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(legacy)))){out.write(raw);out.writeInt(1);out.writeInt(0);out.writeInt(0);out.writeUTF("PRIVATE-LEGACY-CHAT");}
  ReplayData imported=ReplayData.read(legacy);check(imported.formatVersion==7,"Legacy chat file must normalize to sky-only format");Path shared=dir.resolve("shared.btreplay");imported.write(shared);byte[] clean=unpack(shared);
  check(clean.length==raw.length&&!new String(clean,java.nio.charset.StandardCharsets.ISO_8859_1).contains("PRIVATE-LEGACY-CHAT"),"Legacy chat leaked through re-export");check(ReplayData.read(shared).presentation.worldTime==123456,"Sky lost while stripping chat");
  try{new ReplayPresentation(0,Float.NaN,0,map);throw new AssertionError("NaN weather accepted");}catch(IllegalArgumentException expected){}
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);out.writeLong(0);out.writeFloat(0);out.writeFloat(0);out.writeInt(ReplayPresentation.MAX_BIOMES+1);
  try{ReplayPresentation.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())),3,false);throw new AssertionError("Oversized biome data accepted");}catch(IOException expected){}
  System.out.println("PASS v7 sky-only serialization, legacy chat discarded on re-export, immutable biomes and malformed bounds");
 }
}
