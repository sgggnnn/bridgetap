import com.bridger.momentum.*;
import java.util.*;

public class ReplayControlsRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void near(double actual,double expected,String why){check(Math.abs(actual-expected)<.000001,why+": "+actual+" != "+expected);}
 public static void main(String[] args){
  List<ReplayData.Frame> frames=new ArrayList<>();for(int i=0;i<=60;i++)frames.add(new ReplayData.Frame(i,70,0));
  ReplayTimeline timeline=new ReplayTimeline(new ReplayData(frames,Collections.emptyList(),false));
  check(Arrays.equals(ReplayControls.slots(),new int[]{0,1,2,3,8}),"Navigation, center, speed and exit occupy slots");
  int occupied=0;for(int i=0;i<9;i++){if(ReplayControls.item(i,timeline)!=0)occupied++;check(ReplayControls.isTool(i)==(ReplayControls.item(i,timeline)!=0),"Unused slots cannot activate tools");}
  check(occupied==5,"Replay hotbar must contain exactly five items");
  check(ReplayControls.item(0,timeline)==280&&ReplayControls.item(2,timeline)==280&&ReplayControls.item(8,timeline)==166,"Two real sticks and one barrier");
  check(ReplayControls.center(timeline)==ReplayControls.Center.PAUSE,"Playing center is Pause");
  timeline.toggle();check(ReplayControls.center(timeline)==ReplayControls.Center.PLAY,"Paused center is Play");
  timeline.restart();timeline.advance(3);
  check(timeline.finished()&&!timeline.playing()&&ReplayControls.center(timeline)==ReplayControls.Center.REPLAY,"Finishing playback automatically changes center into Replay");
  check(ReplayControls.item(1,timeline)==262&&ReplayControls.label(1,timeline).contains("Replay"),"Finished center has a distinct restart item and label");
  timeline.restart();near(timeline.tick(),0,"Replay starts from zero");check(timeline.playing()&&ReplayControls.center(timeline)==ReplayControls.Center.PAUSE,"Replay restores the Pause item");
  timeline.seek(60);timeline.pause();timeline.skip(-1);check(ReplayControls.center(timeline)==ReplayControls.Center.PLAY,"Rewinding off the end restores normal Play");
  int slot=1;for(int i=0;i<100;i++){slot=ReplayControls.scroll(slot,-1);check(ReplayControls.isTool(slot),"Mouse wheel skips unused cells");}
  check(ReplayControls.scroll(2,-1)==3&&ReplayControls.scroll(3,-1)==8&&ReplayControls.scroll(8,-1)==0&&ReplayControls.scroll(0,1)==8,"Tool wraparound");
  timeline.restart();timeline.cycleSpeed();near(timeline.speed(),2,"Speed cycles to 2x");
  timeline.advance(.5);near(timeline.tick(),20,"Speed affects timeline advancement");
  check(ReplayControls.label(3,timeline).contains("2.00x"),"Speed item label tracks current speed");
  for(int i=0;i<4;i++)timeline.cycleSpeed();near(timeline.speed(),1,"All five speeds wrap back to 1x");
  ReplayData.Frame actor=new ReplayData.Frame(0,70,0);
  near(ReplayTargeting.hit(actor,0,71.62,-8,0,0,64),7.7,"Looking at the player's body picks it");
  check(!Double.isFinite(ReplayTargeting.hit(actor,0,71.62,-8,180,0,64)),"Player behind the camera must not be picked");
  check(!Double.isFinite(ReplayTargeting.hit(actor,2,71.62,-8,0,0,64)),"Clicking beside the player must not enter POV");
  check(!Double.isFinite(ReplayTargeting.hit(actor,0,71.62,-8,0,0,4)),"Targeting respects reach");
  near(ReplayTargeting.hit(actor,0,76,0,0,90,64),4.2,"Looking down at the player");
  near(ReplayTargeting.hit(actor,0,71.62,0,0,0,64),0,"Camera already inside the player can target POV");
  near(ReplayTargeting.hit(new ReplayData.Frame(-8,70,0),0,71.62,0,90,0,64),7.7,"Minecraft west-facing player targeting");
  System.out.println("PASS: exactly five controls, Pause/Play/Replay states, restart, tool-only scrolling and player POV ray targeting");
 }
}
