import com.bridger.momentum.*;
public class DisplayRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  check("No Gain".equals(JumpSignals.gain(true,true,false,false)),"No Gain spelling");
  check("Gain".equals(JumpSignals.gain(true,true,true,false)),"Gain detection");
  check(JumpSignals.gain(true,true,false,true)==null,"Backwards input stays unlabelled");
  JumpEntry entry=new JumpEntry("Tap #1",.46,.46,-.044,9,JumpEntry.EntryType.TAP);entry.setGainType("No Gain");
  for(String mode:new String[]{"ALL","COORDS","COORDS_ONLY","TICKS"}){
   BridgeConfig.displayMode=mode;BridgeConfig.showGainType=false;
   check(!EntryFormatter.format(entry).contains("Gain"),"Gain OFF in "+mode);
   BridgeConfig.showGainType=true;String text=EntryFormatter.format(entry);
   check(text.indexOf("No Gain")==text.lastIndexOf("No Gain")&&text.contains("No Gain"),"Gain exactly once in "+mode);
  }
  BridgeConfig.displayMode="ALL";BridgeConfig.colors[0]="d";BridgeConfig.colors[1]="f";BridgeConfig.colors[3]="4";BridgeConfig.colors[4]="2";BridgeConfig.colors[6]="5";
  String line=EntryFormatter.format(entry);
  check(line.contains("\u00a7d")&&line.contains("\u00a7f.460")&&line.contains("\u00a74-.044")&&line.contains("\u00a72(9t)")&&line.contains("\u00a75[No Gain]"),"Independent compartment colors");
  BridgeConfig.chatOutput=true;BridgeConfig.showGainType=true;BridgeConfig.save();
  BridgeConfig.chatOutput=false;BridgeConfig.showGainType=false;BridgeConfig.colors[1]="e";BridgeConfig.load();
  check(BridgeConfig.chatOutput&&BridgeConfig.showGainType&&"f".equals(BridgeConfig.colors[1]),"Chat, gain and colors survive reload");
  System.out.println("PASS: gain spelling and toggles, independent colors, chat/settings persistence");
 }
}
