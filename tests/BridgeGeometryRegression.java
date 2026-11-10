import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.*;

public class BridgeGeometryRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void near(double actual,double expected,String why){check(Math.abs(actual-expected)<.000001,why+": "+actual+" != "+expected);}
 static List<ReplayData.Frame> frames(){return Arrays.asList(new ReplayData.Frame(.5,70,.5),new ReplayData.Frame(.5,75,31.5));}
 static ReplayData.Block b(int x,int y,int z,int id,int meta,int tick,boolean placed){return new ReplayData.Block(x,y,z,id,meta,tick,placed);}
 public static void main(String[] args)throws Exception{
  List<ReplayData.Block> blocks=Arrays.asList(b(0,69,0,1,0,0,false),b(0,74,31,1,0,0,false),b(0,69,1,24,0,0,false),b(0,69,1,24,0,0,true),b(0,74,30,24,0,1,true),b(0,75,31,147,0,0,false));
  ReplayData data=new ReplayData(frames(),blocks,false);BridgeGeometry.Result r=data.geometry();
  check(r.available,"Both platform edges must be detected");near(r.gap,30,"Face-to-face gap must count empty block widths, not center distance");near(r.rise,5,"Platform height delta");near(r.startHeight,70,"Starting surface height");near(r.endHeight,75,"Ending surface height");
  check(data.geometry()==r,"Measurements are cached rather than recomputed every render");
  List<ReplayData.Block> negative=Arrays.asList(b(0,69,0,1,0,0,false),b(-31,67,0,1,0,0,false),b(-1,69,0,24,0,0,true),b(-30,67,0,24,0,1,true));
  BridgeGeometry.Result nr=new ReplayData(frames(),negative,false).geometry();check(nr.available,"Negative X bridging direction");near(nr.gap,30,"Negative coordinates gap");near(nr.rise,-2,"Downward height delta retains its sign");
  List<ReplayData.Block> slab=Arrays.asList(b(0,69,0,1,0,0,false),b(0,69,31,44,0,0,false),b(0,69,1,24,0,0,true),b(0,69,30,24,0,1,true));
  BridgeGeometry.Result sr=new ReplayData(frames(),slab,false).geometry();check(sr.available,"Slab platform support");near(sr.rise,-.5,"Bottom slab surface height");
  ReplayData missing=new ReplayData(frames(),Collections.singletonList(b(0,69,1,24,0,0,true)),false);
  check(!missing.geometry().available,"Missing platforms must be N/A, not guessed from player travel");
  Path folder=Files.createTempDirectory("bridgetap-geometry-");Path current=folder.resolve("current.btr");data.write(current);ReplayData loaded=ReplayData.read(current);
  check(loaded.placementTags&&loaded.blocks.get(3).placed&&loaded.blocks.get(3).tick==0,"The first tick-zero placement must survive persistence");near(loaded.geometry().gap,30,"Saved geometry round trip");
  Path old=folder.resolve("old.btr");try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(old)))){
   out.writeInt(0x42545250);out.writeInt(1);out.writeBoolean(false);out.writeInt(1);out.writeInt(0);out.writeDouble(0);out.writeDouble(70);out.writeDouble(0);
  }
  ReplayData legacy=ReplayData.read(old);check(!legacy.placementTags&&!legacy.geometry().available,"Old data must not claim tagged platform boundaries");
  Path reexport=folder.resolve("rewritten.btr");legacy.write(reexport);ReplayData rewritten=ReplayData.read(reexport);
  check(rewritten.formatVersion==4&&!rewritten.placementTags&&!rewritten.geometry().available,"Re-exporting an old replay must not invent placement provenance");
  System.out.println("PASS: platform edge gap, rising/falling bridges, negative axes, slabs, first placement tagging and honest legacy measurements");
 }
}
