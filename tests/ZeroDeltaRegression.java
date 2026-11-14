import com.bridger.momentum.*;
import java.util.*;
public final class ZeroDeltaRegression {
 static JumpEntry entry(double delta){return new JumpEntry("Tap Speed #1",.5,.5,delta,9,JumpEntry.EntryType.TAP);}
 public static void main(String[] args){
  BridgeConfig.displayMode="ALL";BridgeConfig.colors[14]="d";
  for(double delta:new double[]{0,-0.0,1e-12,-1e-12,.0004,-.0004})if(!EntryFormatter.format(entry(delta)).contains("\u00a7d+.000"))throw new AssertionError("Visible zero delta lost its neutral colour: "+delta);
  if(!EntryFormatter.format(entry(.001)).contains(BridgeConfig.color(2)+"+.001")||!EntryFormatter.format(entry(-.001)).contains(BridgeConfig.color(3)+"-.001"))throw new AssertionError("Signed delta colours changed");
  if(EntryFormatter.format(entry(Double.NaN)).contains("\u00a7d+.n/a"))throw new AssertionError("Unavailable delta classified as zero");
  OverlayStyle.part(14,0xFF123456);String hud=EntryFormatter.hudLines(Arrays.asList(entry(0))).get(0);if(!hud.contains(OverlayStyle.color(14)+"+.000"))throw new AssertionError("RGB zero colour ignored by HUD");
  BridgeConfig.save();BridgeConfig.colors[14]="f";BridgeConfig.load();if(!"d".equals(BridgeConfig.colors[14]))throw new AssertionError("Zero colour not persisted");
  System.out.println("PASS independent zero delta palette/RGB colour, rounded zeros, signed values, unavailable data and persistence");
 }
}
