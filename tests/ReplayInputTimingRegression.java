import com.bridger.momentum.*;
import java.util.*;
public class ReplayInputTimingRegression {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static ReplayData.Frame frame(int held,int press,int release){return new ReplayData.Frame(0,64,0).withStats(new ReplayData.TickStats(held,press,release,0,press==256?1:0,0,0,0,0,0,0,0));}
 public static void main(String[] args){
  ReplayData.Block block=new ReplayData.Block(0,63,0,24,0,2,true);
  ReplayData data=new ReplayData(Arrays.asList(frame(0,0,0),frame(16|256,256,0),frame(0,0,16|256)),Collections.singletonList(block),false);
  ReplayTimeline t=new ReplayTimeline(data);t.pause();
  check(t.inputs().held==0&&t.inputs().rightClicks==0,"Inputs advanced before their recorded tick");
  check(t.sample().stats.held==0&&t.blocks().isEmpty(),"Display correction changed source telemetry or placement clock");
  t.seek(.99);check(t.inputs().held==0,"Fractional playback advanced input phase");
  t.skip(1);check(t.inputs().held==(16|256)&&t.inputs().rightClicks==1,"Recorded press tick changed");
  t.seek(2);check(t.inputs().pressed==0&&t.inputs().released==(16|256)&&t.blocks().size()==1,"Recorded release or placement moved");
  t.seek(0);check(t.inputs().rightClicks==0,"Rewind leaked a future click");
  System.out.println("PASS: original recorded keystroke timing, releases, seeking and unchanged placement clock");
 }
}
