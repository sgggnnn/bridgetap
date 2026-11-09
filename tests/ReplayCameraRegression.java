import com.bridger.momentum.*;
import java.util.*;

public class ReplayCameraRegression {
 static void near(double actual,double expected,String why){if(Math.abs(actual-expected)>.00001)throw new AssertionError(why+": "+actual+" != "+expected);}
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  near(ReplayFreeCamera.SPEED,12,"Normal flight speed in blocks/second");near(ReplayFreeCamera.SPRINT_SPEED,36,"Fast flight speed in blocks/second");
  ReplayFreeCamera camera=new ReplayFreeCamera();camera.reset(100,70,-20);
  ReplayData data=new ReplayData(Arrays.asList(new ReplayData.Frame(0,70,0),new ReplayData.Frame(1,70,0)),Collections.emptyList(),false);
  ReplayTimeline replay=new ReplayTimeline(data);replay.pause();
  for(int tick=0;tick<20;tick++){camera.step(.05,0,1,0,0,false);replay.advance(.05);}
  near(camera.z(1),-8,"Forward motion must work while replay is paused");near(replay.tick(),0,"Camera movement must not advance the paused actor");
  near(camera.x(1),100,"Forward must not strafe");near(camera.y(1),70,"Forward must not change height");
  // Minecraft yaw: 0 faces south, 90 west, 180 north, 270 east.
  // Screen-right is therefore west, north, east and south respectively.
  double[][] forwards={{0,1},{-1,0},{0,-1},{1,0}},rights={{-1,0},{0,-1},{1,0},{0,1}};
  for(int heading=0;heading<4;heading++){
   float yaw=heading*90;
   camera.reset(0,70,0);camera.step(.05,yaw,1,0,0,false);
   near(camera.x(1),forwards[heading][0]*.6,"Forward X at yaw "+yaw);near(camera.z(1),forwards[heading][1]*.6,"Forward Z at yaw "+yaw);
   camera.reset(0,70,0);camera.step(.05,yaw,0,1,0,false);
   near(camera.x(1),rights[heading][0]*.6,"Right X at yaw "+yaw);near(camera.z(1),rights[heading][1]*.6,"Right Z at yaw "+yaw);
   camera.reset(0,70,0);camera.step(.05,yaw,0,-1,0,false);
   near(camera.x(1),-rights[heading][0]*.6,"Left X at yaw "+yaw);near(camera.z(1),-rights[heading][1]*.6,"Left Z at yaw "+yaw);
  }
  camera.reset(0,70,0);camera.step(.05,0,0,0,1,false);near(camera.y(1),70.6,"Jump ascends");
  camera.step(.05,0,0,0,-1,false);near(camera.y(1),70,"Sneak descends");
  camera.reset(0,0,0);camera.step(.05,0,1,1,1,false);
  near(Math.sqrt(camera.x(1)*camera.x(1)+camera.y(1)*camera.y(1)+camera.z(1)*camera.z(1)),.6,"Diagonal and vertical movement must not multiply flight speed");
  camera.reset(0,0,0);camera.step(.05,0,1,0,0,true);near(camera.z(1),1.8,"Sprint flight multiplier");
  near(camera.z(0),0,"Interpolation starts at previous tick position");near(camera.z(.5f),.9,"Frame interpolation is smooth");near(camera.z(1),1.8,"Interpolation ends at current tick position");
  camera.step(.05,0,0,0,0,false);near(camera.z(.5f),1.8,"Releasing movement must stop drifting");
  camera.reset(-100,120,200);near(camera.x(0),-100,"Camera reset must reset interpolation history");near(camera.z(1),200,"Camera reset must not snap back to previous coordinates");
  ReplayFreeCamera a=new ReplayFreeCamera(),b=new ReplayFreeCamera();a.reset(0,0,0);b.reset(0,0,0);
  for(int i=0;i<20;i++)a.step(.05,0,1,0,0,false);
  for(int i=0;i<40;i++)b.step(.025,0,1,0,0,false);
  near(a.z(1),b.z(1),"Travel distance must depend on elapsed time, not rendered frame count");
  ReplayData.Frame actor=new ReplayData.Frame(-10.25,70,5.75);
  check(ReplayInspection.eyeInside(actor,-10.25,71.62,5.75),"Ghost POV must hide the head/body model");
  check(ReplayInspection.eyeInside(actor,-9.8,70.9,5.75),"Arm geometry must not obstruct the close camera");
  check(!ReplayInspection.eyeInside(actor,-8,71.62,5.75),"Ghost must remain visible from outside");
  check(!ReplayInspection.eyeInside(actor,-10.25,73,5.75),"Ghost must remain visible when looking down from above");
  check(!ReplayInspection.eyeInside(actor,-10.25,69,5.75),"Ghost must remain visible when looking up from below");
  camera.reset(-11.25,70,5.75);camera.step(.1,0,0,-1,0,false);
  check(ReplayInspection.eyeInside(actor,camera.x(1),camera.y(1)+1.62,camera.z(1)),"Free camera can pass directly into the ghost without collision");
  near(actor.x,-10.25,"Camera movement must never move the recorded ghost");
  System.out.println("PASS: all four Minecraft strafe directions, faster paused flight, interpolation and unobstructed ghost inspection");
 }
}
