import com.bridger.momentum.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ReplayTimelineRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void near(double a,double b,String why){check(Math.abs(a-b)<.0001,why+": "+a+" != "+b);}
 public static void main(String[] args)throws Exception{
  ReplayData.Frame first=new ReplayData.Frame(-3.25,70,4,179,10,170,160,true,true,.5f,.3f,1,24,2);
  ReplayData.Frame last=new ReplayData.Frame(-2.25,71,5,-179,20,-170,-160,false,false,.75f,.5f,2,280,0);
  List<ReplayData.Block> blocks=Arrays.asList(new ReplayData.Block(-4,69,4,35,5,0),new ReplayData.Block(-3,70,5,24,2,1));
  ReplayData.Identity who=new ReplayData.Identity(UUID.randomUUID().toString(),"sggn","\u00a79VIP\u00a7a+ \u00a79sggn","recorded-texture-property","recorded-signature","slim",95);
  ReplayData data=new ReplayData(Arrays.asList(first,last),blocks,false,who,0,12345);
  ReplayTimeline timeline=new ReplayTimeline(data);
  check(timeline.blocks().size()==1,"Future placed blocks must be absent at the start");
  timeline.seek(.5);ReplayData.Frame half=timeline.pose();near(half.x,-2.75,"Position interpolation");near(half.yaw,180,"Yaw crosses +/-180 by shortest rotation");near(half.pitch,15,"Pitch interpolation");
  check(half.sneaking&&half.itemId==24&&half.itemDamage==2,"Pose flags and held item stay at recorded tick until next sample");
  timeline.seek(1);check(timeline.blocks().size()==2&&!timeline.pose().sneaking&&timeline.pose().itemId==280,"Forward applies blocks and new pose");
  timeline.seek(0);check(timeline.blocks().size()==1,"Backward removes future blocks");
  timeline.toggle();check(!timeline.playing(),"Pause");timeline.advance(5);near(timeline.tick(),0,"Pause freezes replay time");
  timeline.toggle();timeline.advance(.025);near(timeline.tick(),.5,"Normal-speed time");
  timeline.cycleSpeed();near(timeline.speed(),2,"2x playback");timeline.restart();timeline.advance(.0125);near(timeline.tick(),.5,"Playback speed changes time");
  timeline.seek(-200);near(timeline.tick(),0,"Rewind clamps at start");timeline.seek(200);near(timeline.tick(),1,"Forward clamps at end");
  Path path=Files.createTempDirectory("bridgetap-v2-").resolve("recording.btr");data.write(path);ReplayData read=ReplayData.read(path);
  check(read.formatVersion==4&&read.identity.displayName.equals(who.displayName)&&read.identity.textureValue.equals(who.textureValue)&&read.identity.textureSignature.equals(who.textureSignature)&&read.identity.model.equals("slim"),"Identity, formatted rank and skin survive restart");
  check(read.identity.modelParts==95,"Configured skin layers survive restart");
  near(read.frames.get(0).bodyYaw,160,"Body yaw round trip");near(read.frames.get(0).swing,.5,"Arm animation round trip");
  check(read.frames.get(0).sneaking&&read.frames.get(0).onGround&&read.frames.get(0).itemDamage==2&&read.blocks.get(0).metadata==5&&read.dayTime==12345,"Pose, colored blocks and world time round trip");
  Path old=path.resolveSibling("legacy.btr");
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(old)))){
   out.writeInt(0x42545250);out.writeInt(1);out.writeBoolean(false);out.writeInt(1);out.writeInt(1);out.writeDouble(1);out.writeDouble(70);out.writeDouble(-1);
   out.writeInt(1);out.writeInt(69);out.writeInt(-1);out.writeInt(24);out.writeInt(0);
  }
  ReplayData legacy=ReplayData.read(old);check(legacy.formatVersion==1&&legacy.frames.get(0).z==-1&&legacy.blocks.get(0).metadata==0,"1.1.0 position-only replays stay readable");
  RunHistory history=new RunHistory(path.getParent());history.add("SUCCESS",.05,1,"Z",Collections.singletonList("tap"),data);
  RunHistory.Run run=history.recent().get(0);check(run.replayId().matches("[a-z0-9]{5}-[a-z0-9]{5}-[a-z0-9]{15}"),"Alphanumeric ID retains the full UUID without truncating uniqueness");
  check(history.toggleFavorite(run),"Right-click adds a favourite");
  RunHistory fresh=new RunHistory(path.getParent());check(fresh.favorites().size()==1&&fresh.hasReplay(fresh.favorites().get(0)),"Favouriting a recent run saves it with its replay");
  check(!fresh.toggleFavorite(fresh.favorites().get(0)),"Right-click removes a favourite");
  check(new RunHistory(path.getParent()).favorites().isEmpty(),"Favourite removal survives restart");
  check(new RunHistory(path.getParent()).saved().size()==1,"Unfavouriting keeps the saved replay");
  System.out.println("PASS: native replay timeline, pose, skin/rank persistence, block rewind, legacy recordings and persistent favourites");
 }
}
