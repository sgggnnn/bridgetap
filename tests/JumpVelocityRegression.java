import com.bridger.momentum.*;
public final class JumpVelocityRegression {
 public static void main(String[] args){
  JumpVelocity history=new JumpVelocity();history.record(40,12.111,70,-23.456);history.record(41,12.222,70,-23.789);
  JumpVelocity.Sample sample=history.beforeJump(41);if(sample==null||sample.tick!=40||sample.z!=-23.456)throw new AssertionError("Not previous-tick position");
  if(!JumpVelocity.decimalZ(sample).equals(".456")||!JumpVelocity.decimalZ(23.456).equals(".456")||!JumpVelocity.decimalZ(-23.9999).equals(".000"))throw new AssertionError("Z fractional coordinate formatting");
  BridgeConfig.displayMode="ALL";BridgeConfig.showBridgeVelocity=true;
  JumpEntry first=new JumpEntry("Tap Speed #1",23.789,.789,0,9,JumpEntry.EntryType.TAP);first.setPreJumpZ(sample);
  JumpEntry second=new JumpEntry("Tap Speed #2",24.123,.123,0,9,JumpEntry.EntryType.TAP);second.setPreJumpZ(history.beforeJump(42));
  if(!EntryFormatter.format(first).contains("/456")||!EntryFormatter.format(second).contains("/789"))throw new AssertionError("Each speed must keep its own prior Z");
  BridgeConfig.showBridgeVelocity=false;if(EntryFormatter.format(first).contains("/456"))throw new AssertionError("Disabled option still visible");
  first.setJumpYaw(541);BridgeConfig.showJumpAngle=true;
  if(!EntryFormatter.format(first).contains("[Yaw: -179.0\u00b0]"))throw new AssertionError("Wrapped jump yaw missing");
  BridgeConfig.showJumpAngle=false;if(EntryFormatter.format(first).contains("Yaw"))throw new AssertionError("Angle toggle ignored");
  BridgeConfig.showJumpAngle=true;BridgeConfig.save();BridgeConfig.showJumpAngle=false;BridgeConfig.load();if(!BridgeConfig.showJumpAngle)throw new AssertionError("Angle option not persisted");BridgeConfig.showJumpAngle=false;
  for(String mode:new String[]{"ALL","COORDS","COORDS_ONLY","LAST_TURN"}){
   BridgeConfig.displayMode=mode;BridgeConfig.showBridgeVelocity=true;
   String line=EntryFormatter.lines(java.util.Arrays.asList(first)).get(0).replaceAll("\u00a7.", "");
   if(!line.contains("789/456")||line.contains("Velo Z"))throw new AssertionError("Adjacent velocity in "+mode+": "+line);
  }
  BridgeConfig.displayMode="ALL";
  if(history.beforeJump(43)!=null)throw new AssertionError("Missing sample guessed");history.record(168,0,0,0);if(history.beforeJump(41)!=null)throw new AssertionError("Stale ring slot reused");
  history.clear();if(history.beforeJump(169)!=null)throw new AssertionError("Reset leaked sample");
  BridgeConfig.showBridgeVelocity=true;BridgeConfig.save();BridgeConfig.showBridgeVelocity=false;BridgeConfig.load();if(!BridgeConfig.showBridgeVelocity)throw new AssertionError("Option not persisted");BridgeConfig.showBridgeVelocity=false;BridgeConfig.save();
  System.out.println("PASS: exact previous-tick Z decimals per speed, negative coordinates, rounding, toggle, persistence and reset");
 }
}
