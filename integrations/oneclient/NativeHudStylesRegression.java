import com.bridger.momentum.*;
import com.bridger.momentum.oneclient.NativeHudStyles;
import java.util.*;
import org.polyfrost.oneconfig.api.config.v1.*;
public final class NativeHudStylesRegression {
 static final class TestHud extends org.polyfrost.oneconfig.api.hud.v1.Hud {
  TestHud(){super("test-style","Style test",org.polyfrost.oneconfig.api.hud.v1.Hud.Category.getINFO());}
  @Override public boolean update(){return false;}
  @Override public void Content(androidx.compose.runtime.Composer composer,int flags){}
 }
 @SuppressWarnings({"unchecked","rawtypes"}) static void set(List<Property<?>> props,String id,Object value){for(Property p:props)if(p.getID().equals(id)){p.set(value);return;}throw new AssertionError("Missing HUD property: "+id);}
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args){
  set(NativeHudStyles.properties(0),"componentColor0",0xFF2468AC);check(OverlayStyle.part(0)==0xFF2468AC,"Coordinate picker did not reach HUD");
  set(NativeHudStyles.properties(0),"componentColor10",0xFF987654);check(OverlayStyle.part(10)==0xFF987654,"Velo picker did not reach HUD");
  for(int part:new int[]{11,12,13}){set(NativeHudStyles.properties(0),"componentColor"+part,0x0013579B);check(OverlayStyle.part(part)==0xFF13579B,"New component picker did not reach HUD: "+part);}
  set(NativeHudStyles.properties(0),"componentColor8",0x48102030);check(OverlayStyle.background(0)==0x48102030,"Coordinate background alpha lost");
  set(NativeHudStyles.properties(1),"textColorRgb",0xFF112233);check(OverlayStyle.styled(1,"Text").equals("§#112233Text"),"Replay text picker did not reach renderer");
  set(NativeHudStyles.properties(2),"pressedBackgroundRgb",0x68456789);check(OverlayStyle.pressedBackground==0x68456789,"Pressed background picker failed");
  set(NativeHudStyles.properties(2),"pressedTextRgb",0xFF345678);check(OverlayStyle.pressedText==0xFF345678,"Pressed text picker failed");
  for(int t=0;t<3;t++)for(Property<?> prop:NativeHudStyles.properties(t))if(!prop.getID().equals("resetOverlayStyle"))check(prop.getMetadata("visualizer")==Visualizer.ColorVisualizer.class,"Picker is not the native OneClient color control");
  TestHud hud=new TestHud();NativeHudStyles.bind(1,hud);hud.setTextColor(0xFF13579B);hud.setBgColor(0x642468AC);hud.setShowBackground(true);hud.setShowShadow(false);NativeHudStyles.sync();
  check(OverlayStyle.text[1]==0xFF13579B&&OverlayStyle.background(1)==0x642468AC&&OverlayStyle.backgroundShown(1)&&!OverlayStyle.shadow(1),"Native Style tab did not reach renderer");
  OverlayStyle.background(1,0x74224466);NativeHudStyles.sync();check(hud.getBgColor()==0x74224466,"Style property did not synchronize back into native UI");
  for(int t=0;t<3;t++)for(Property<?> prop:NativeHudStyles.properties(t))if(prop.getID().equals("resetOverlayStyle"))((Runnable)prop.getMetadata("runnable")).run();
  check(!OverlayStyle.customText[1]&&OverlayStyle.pressedBackground==0xE0275665,"Reset did not restore native default styling");
  System.out.println("PASS: native OneClient Style tab and RGB/alpha pickers update coordinate, replay and pressed-key colors; reset restores defaults");
 }
}
