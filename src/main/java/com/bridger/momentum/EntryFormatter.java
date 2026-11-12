package com.bridger.momentum;
import java.util.Locale;

/** One formatter for chat and HUD: gain visibility is applied exactly once. */
public final class EntryFormatter {
 private EntryFormatter(){}
 public static java.util.List<String> lines(java.util.List<JumpEntry> entries){return lines(entries,BridgeConfig::color);}
 public static java.util.List<String> hudLines(java.util.List<JumpEntry> entries){return lines(entries,OverlayStyle::color);}
 private static java.util.List<String> lines(java.util.List<JumpEntry> entries,java.util.function.IntFunction<String> color){
  java.util.List<String> result=new java.util.ArrayList<>();
  if("LAST_TURN".equals(BridgeConfig.displayMode)){
   JumpEntry last=null,turn=null;
   for(JumpEntry entry:entries){
    switch(entry.getType()){
     case FLAT: case TAP: case FAIL_TAP: last=entry;break;
     case TURN: turn=entry;break;
     default: break;
    }
   }
   if(last!=null)result.add(compact("Last",last,color));
   if(turn!=null)result.add(compact("Turn",turn,color));
  }else for(JumpEntry entry:entries){String line=format(entry,color);if(!line.isEmpty())result.add(line);}
  return result;
 }
 private static String compact(String label,JumpEntry entry,java.util.function.IntFunction<String> color){
  int dec=Math.floorMod((int)Math.round(entry.getDecimal()*1000),1000);
  return color.apply(0)+"\u00bb "+label+": "+color.apply(1)+String.format(Locale.US,".%03d",dec)+velocity(entry,color);
 }
 private static String velocity(JumpEntry entry,java.util.function.IntFunction<String> color){
  if(!BridgeConfig.showBridgeVelocity||(entry.getType()!=JumpEntry.EntryType.FLAT&&entry.getType()!=JumpEntry.EntryType.TAP))return "";
  return " "+color.apply(10)+"[Velo Z (-1t): "+(Double.isNaN(entry.getPreJumpZ())?"n/a":JumpVelocity.decimalZ(entry.getPreJumpZ()))+"]";
 }
 public static String format(JumpEntry entry){return format(entry,BridgeConfig::color);}
 private static String format(JumpEntry entry,java.util.function.IntFunction<String> color){
  String mode=BridgeConfig.displayMode;
  int dec=Math.floorMod((int)Math.round(entry.getDecimal()*1000),1000);
  String coords=color.apply(1)+String.format(Locale.US,".%03d",dec);
  String line;
  boolean plate=entry.getType()==JumpEntry.EntryType.PLATES||entry.getType()==JumpEntry.EntryType.PLATES_MINUS_1;
  if("COORDS_ONLY".equals(mode))line=coords;
  else if("TICKS".equals(mode)){
   if(plate)return "";
   line=color.apply(0)+"\u00bb "+entry.getLabel()+" "+color.apply(4)+"("+entry.getTicksElapsed()+"t)";
  }else{
   line=color.apply(0)+"\u00bb "+entry.getLabel()+": "+coords;
   if("ALL".equals(mode)&&!plate&&entry.getType()!=JumpEntry.EntryType.HEAD_HITTER){
    double d=entry.getDelta();
    String delta=Double.isNaN(d)?"+.n/a":String.format(Locale.US,"%s.%03d",d>=0?"+":"-",(int)Math.round(Math.abs(d)*1000));
    line+=" "+color.apply(d<0?3:2)+delta+" "+color.apply(4)+"("+entry.getTicksElapsed()+"t)";
   }
  }
  if(BridgeConfig.showGainType&&entry.getGainType()!=null){
   String gain=entry.getGainType().replace("#", "").replace("No Gay", "No Gain");
   if("No Gain".equals(gain)&&BridgeConfig.useNoGayLabel)gain="No Gay";
   line+=" "+color.apply("Gain".equals(gain)?5:6)+"["+gain+"]";
  }
  return line+velocity(entry,color);
 }
}
