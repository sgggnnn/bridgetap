package com.bridger.momentum;
import java.util.Locale;

/** One formatter for chat and HUD: gain visibility is applied exactly once. */
public final class EntryFormatter {
 private EntryFormatter(){}
 public static String format(JumpEntry entry){
  String mode=BridgeConfig.displayMode;
  int dec=Math.floorMod((int)Math.round(entry.getDecimal()*1000),1000);
  String coords=BridgeConfig.color(1)+String.format(Locale.US,".%03d",dec);
  String line;
  boolean plate=entry.getType()==JumpEntry.EntryType.PLATES||entry.getType()==JumpEntry.EntryType.PLATES_MINUS_1;
  if("COORDS_ONLY".equals(mode))line=coords;
  else if("TICKS".equals(mode)){
   if(plate)return "";
   line=BridgeConfig.color(0)+"\u00bb "+entry.getLabel()+" "+BridgeConfig.color(4)+"("+entry.getTicksElapsed()+"t)";
  }else{
   line=BridgeConfig.color(0)+"\u00bb "+entry.getLabel()+": "+coords;
   if("ALL".equals(mode)&&!plate&&entry.getType()!=JumpEntry.EntryType.HEAD_HITTER){
    double d=entry.getDelta();
    String delta=Double.isNaN(d)?"+.n/a":String.format(Locale.US,"%s.%03d",d>=0?"+":"-",(int)Math.round(Math.abs(d)*1000));
    line+=" "+BridgeConfig.color(d<0?3:2)+delta+" "+BridgeConfig.color(4)+"("+entry.getTicksElapsed()+"t)";
   }
  }
  if(BridgeConfig.showGainType&&entry.getGainType()!=null){
   String gain=entry.getGainType().replace("#", "").replace("No Gay", "No Gain");
   line+=" "+BridgeConfig.color("Gain".equals(gain)?5:6)+"["+gain+"]";
  }
  return line;
 }
}
