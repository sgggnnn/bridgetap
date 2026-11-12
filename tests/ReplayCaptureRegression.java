import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.*;
public final class ReplayCaptureRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static ReplayData.Frame frame(double x,int clicks){return new ReplayData.Frame(x,70,0,0,0,0,0,false,false,0,0,0,24,0,new ReplayData.TickStats(256,clicks>0?256:0,0,0,clicks,0,0,clicks,0,.28,0,0));}
 public static void main(String[] args)throws Exception{
  ReplayCaptureTimeline capture=new ReplayCaptureTimeline();capture.start(100);capture.boundary(100,frame(0,0));capture.endTick(100,frame(1,1));
  check(capture.frames().get(0).x==0&&capture.frames().get(1).x==1,"First tick overwrote the starting pose");
  check(capture.frames().get(0).stats.rightClicks==1&&capture.frames().get(1).stats.rightClicks==0,"Inputs moved to the next tick or were duplicated at the boundary");
  capture.endTick(101,frame(2,0));int event=capture.eventTick(102);capture.endTick(102,frame(3,1));
  check(event==2&&capture.frames().size()==4,"Placement and pose clocks use different origins");
  ReplayData.Block placed=new ReplayData.Block(2,69,0,24,0,event,true);ReplayData data=new ReplayData(capture.frames(),Collections.singletonList(placed),false);ReplayTimeline timeline=new ReplayTimeline(data);
  timeline.seek(1.999);check(timeline.blocks().isEmpty(),"New placement appears early");timeline.seek(2);check(timeline.blocks().containsKey(placed.key())&&timeline.pose().x==2&&timeline.sample().stats.rightClicks==1,"Placement appears a tick late or differs from recorded input");
  timeline.pause();timeline.skip(1);timeline.skip(-1);check(timeline.tick()==2&&timeline.blocks().containsKey(placed.key()),"Paused seek changed the placement boundary");
  Path path=Files.createTempDirectory("replay-phase-").resolve("v5.btr");data.write(path);check(ReplayData.read(path).formatVersion==5,"New timing phase was not versioned");
  path=path.resolveSibling("v4-fixture.btr");
  ReplayData oldPayload=new ReplayData(Arrays.asList(frame(1,0),frame(2,0),frame(3,0),frame(4,1)),Collections.singletonList(new ReplayData.Block(2,69,0,24,0,3,true)),false);oldPayload.write(path);
  byte[] bytes;try(InputStream in=new GZIPInputStream(Files.newInputStream(path));ByteArrayOutputStream raw=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)raw.write(buffer,0,n);bytes=raw.toByteArray();}bytes[7]=4;try(OutputStream out=new GZIPOutputStream(Files.newOutputStream(path))){out.write(bytes);}
  ReplayData legacy=ReplayData.read(path);ReplayTimeline old=new ReplayTimeline(legacy);old.seek(1.999);check(old.blocks().isEmpty(),"Legacy placement appeared too early");old.seek(2);check(old.blocks().size()==1&&old.sample().stats.rightClicks==1,"Legacy v4 placements/inputs are still one tick late");
  old.seek(3);check(old.sample().stats.rightClicks==0,"Legacy final pose duplicated the last click");
  check(ReplayFailureAnalysis.analyze("FAILED","Z",legacy).stream().anyMatch(f->f.text.equals("Last confirmed block placement.")&&f.tick==2),"Legacy analysis seeks a late placement tick");
  check(legacy.blocks.get(0).tick==3,"Playback correction modified the source recording");path=path.resolveSibling("shared.btr");legacy.write(path);ReplayData shared=ReplayData.read(path);check(shared.formatVersion==4&&shared.blocks.get(0).tick==3,"Sharing lost legacy timing provenance");
  capture.clear();capture.start(500);capture.boundary(500,frame(10,0));check(capture.eventTick(500)==0&&capture.frames().size()==1,"New runs inherited the previous timing origin");
  System.out.println("PASS: preserved starting pose, exact 2-tick placement/input synchronization, legacy v4 playback repair and provenance-safe sharing");
 }
}
