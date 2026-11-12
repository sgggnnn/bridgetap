import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.*;

public class TelemetryRegression {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static void near(double actual,double expected,String why){check(Math.abs(actual-expected)<.000001,why);}
 public static void main(String[] args)throws Exception{
  TickTelemetry meter=new TickTelemetry();meter.beginTick();
  meter.event(1,true);meter.event(256,true);meter.click(1,100000000L);meter.event(256,false);
  meter.click(0,120000000L);meter.click(0,140000000L);meter.click(2,150000000L);
  ReplayData.TickStats a=meter.snapshot(1,.2,-.0784,-.3,160000000L);
  check(a.held==1&&a.pressed==257&&a.released==256,"Press and release within one tick must survive even when the button ends up released");
  check(a.leftClicks==2&&a.rightClicks==1&&a.middleClicks==1&&a.leftCps==2&&a.rightCps==1&&a.middleCps==1,"Physical click counts and independent CPS channels");
  meter.beginTick();ReplayData.TickStats b=meter.snapshot(256,.1,-.2,.3,300000000L);
  check(b.rightClicks==0&&b.leftClicks==0&&b.pressed==0&&b.released==0&&b.rightCps==1,"Held use must not manufacture click presses; per-tick counters reset");
  ReplayData.TickStats expired=meter.snapshot(0,0,0,0,1120000000L);
  check(expired.rightCps==0&&expired.leftCps==1&&expired.middleCps==1,"CPS uses real trailing one-second windows, with exact expiry boundaries");
  ReplayData.TickStats empty=meter.snapshot(0,0,0,0,1150000000L);
  check(empty.leftCps==0&&empty.middleCps==0,"Expired clicks are removed without depending on frame rate");
  List<ReplayData.Frame> frames=new ArrayList<>();
  for(int i=0;i<61;i++)frames.add(new ReplayData.Frame(i,70,-i,0,0,0,0,false,true,0,0,0,24,0,i==0?a:b));
  ReplayData data=new ReplayData(frames,Collections.emptyList(),false);ReplayTimeline timeline=new ReplayTimeline(data);
  check(timeline.skip(1)==20&&timeline.tick()==20&&timeline.playing(),"Playing stick moves one recorded second and keeps playback running");
  check(timeline.skip(-1)==20&&timeline.tick()==0,"Playing backward stick moves one second");
  timeline.pause();check(timeline.skip(1)==1&&timeline.tick()==1&&!timeline.playing(),"Paused stick moves one tick without resuming playback");
  timeline.seek(20.75);timeline.skip(1);check(timeline.tick()==21,"Paused stepping must select an exact recorded sample rather than retain an interpolated pose");
  timeline.seek(1);
  timeline.skip(-1);timeline.seek(.75);check(timeline.sample().stats==a&&timeline.pose().stats==a,"Metrics use the selected recorded tick, never interpolated input/click values");
  timeline.seek(1);check(timeline.sample().stats==b,"Seeking updates metrics to the newly selected tick");
  Path file=Files.createTempDirectory("bridgetap-telemetry-").resolve("run.btr");data.write(file);ReplayData read=ReplayData.read(file);
  check(read.formatVersion==5,"Telemetry format version 5");ReplayData.TickStats s=read.frames.get(0).stats;
  check(s.held==a.held&&s.pressed==a.pressed&&s.released==a.released&&s.leftClicks==2&&s.rightClicks==1&&s.middleClicks==1&&s.leftCps==2&&s.rightCps==1&&s.middleCps==1,"All telemetry fields survive disk round trip");
  near(s.vx,.2,"Velocity X round trip");near(s.vy,-.0784,"Velocity Y round trip");near(s.vz,-.3,"Velocity Z round trip");
  Path legacy=file.resolveSibling("version2.btr");
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(legacy)))){
   out.writeInt(0x42545250);out.writeInt(2);out.writeBoolean(false);out.writeInt(1);out.writeInt(0);
   out.writeUTF("00000000-0000-0000-0000-000000000001");out.writeUTF("sggn");out.writeUTF("VIP+ sggn");out.writeUTF("");out.writeUTF("");out.writeUTF("default");out.writeInt(127);out.writeInt(0);out.writeLong(6000);
   out.writeDouble(10);out.writeDouble(70);out.writeDouble(-10);
   for(int i=0;i<4;i++)out.writeFloat(0);out.writeBoolean(false);out.writeBoolean(true);
   for(int i=0;i<3;i++)out.writeFloat(0);out.writeInt(24);out.writeInt(2);
  }
  ReplayData old=ReplayData.read(legacy);check(old.formatVersion==2&&old.frames.get(0).stats==null&&old.frames.get(0).itemDamage==2,"1.2.x replays remain readable and report missing telemetry honestly");
  try{new ReplayData.TickStats(1024,0,0,0,0,0,0,0,0,0,0,0);throw new AssertionError("Invalid input mask accepted");}catch(IllegalArgumentException expected){}
  try{new ReplayData.TickStats(0,0,0,0,0,0,0,0,0,Double.NaN,0,0);throw new AssertionError("NaN velocity accepted");}catch(IllegalArgumentException expected){}
  System.out.println("PASS: raw click/CPS windows, input edges, exact-tick metrics, velocity persistence, legacy format and context-sensitive skipping");
 }
}
