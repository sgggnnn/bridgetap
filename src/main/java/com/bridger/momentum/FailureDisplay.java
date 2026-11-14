package com.bridger.momentum;
import java.util.*;
/** Presentation only: saved samples, replay capture and statistics stay complete. */
public final class FailureDisplay {
 private FailureDisplay(){}
 public static boolean onlyTaps(boolean failed){return failed&&"FAIL_TAPS_ONLY".equals(BridgeConfig.failedDisplay);}
 public static boolean header(boolean failed){return !failed||"ALL".equals(BridgeConfig.failedDisplay);}
 public static List<JumpEntry> entries(List<JumpEntry> entries,boolean failed){
  if(!failed||"ALL".equals(BridgeConfig.failedDisplay))return entries;
  List<JumpEntry> result=new ArrayList<>();if(onlyTaps(failed))for(JumpEntry e:entries)if(e.getType()==JumpEntry.EntryType.FAIL_TAP)result.add(e);return result;
 }
}
