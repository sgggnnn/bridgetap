import com.bridger.momentum.*;
import java.util.*;
public final class BothAxesRegression {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static String plain(JumpEntry e){return HudText.unformatted(EntryFormatter.format(e));}
 public static void main(String[] args){
  BridgeConfig.displayMode="ALL";BridgeConfig.showBothAxes=true;
  JumpEntry jump=new JumpEntry("Tap #1",-23.584,.584,0,9,JumpEntry.EntryType.TAP);jump.setHorizontalCoordinates(-12.289,-23.584);
  check(plain(jump).contains("X .289 Z .584 [\u0394 X-Z: -0.295]"),"Negative world coordinates or signed axis difference");
  JumpVelocity history=new JumpVelocity();history.record(10,12.111,70,-23.456);jump.setPreJumpZ(history.beforeJump(11));BridgeConfig.showBridgeVelocity=true;
  check(plain(jump).contains("X 289/111 Z 584/456"),"Previous tick decimals mixed between axes");
  for(String mode:new String[]{"COORDS","COORDS_ONLY","LAST_TURN"}){BridgeConfig.displayMode=mode;check(HudText.unformatted(EntryFormatter.lines(Arrays.asList(jump)).get(0)).contains("X 289/111 Z 584/456"),"Dual-axis mode missing in "+mode);}
  BridgeConfig.displayMode="ALL";BridgeConfig.showBridgeVelocity=false;
  jump.setHorizontalCoordinates(1.5844,2.2896);check(plain(jump).contains("+0.294"),"Difference disagrees with rounded coordinates");
  jump.setHorizontalCoordinates(1.9999,2.0001);check(plain(jump).contains("X .000 Z .000")&&plain(jump).contains("+0.000"),"Rounded boundary mismatch");
  jump.setHorizontalCoordinates(1.9,2.1);check(plain(jump).contains("+0.800"),"Difference should be X minus Z, not a wrapped movement delta");
  JumpEntry legacy=new JumpEntry("Turn",.123,.123,0,15,JumpEntry.EntryType.TURN);check(!plain(legacy).contains("X "),"Unknown axes fabricated");
  BridgeConfig.showBothAxes=false;check(!plain(jump).contains("X ")&&plain(jump).contains(".584"),"Single-axis display changed");
  BridgeConfig.showBothAxes=true;BridgeConfig.save();BridgeConfig.showBothAxes=false;BridgeConfig.load();check(BridgeConfig.showBothAxes,"Both-axes option not persisted");BridgeConfig.showBothAxes=false;BridgeConfig.save();
  System.out.println("PASS: dual-axis decimals, exact independent prior samples, signed difference, rounding, legacy fallback and persistence");
 }
}
