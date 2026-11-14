import com.bridger.momentum.*;
import java.util.*;
public final class FailureDisplayRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  JumpEntry tap=new JumpEntry("Tap #1",.5,.5,0,9,JumpEntry.EntryType.TAP),turn=new JumpEntry("Turn",.2,.2,0,12,JumpEntry.EntryType.TURN),fail=new JumpEntry("Fail Tap/4b",.4,.4,-.1,9,JumpEntry.EntryType.FAIL_TAP);List<JumpEntry> entries=Arrays.asList(tap,turn,fail);
  BridgeConfig.failedDisplay="ALL";BridgeConfig.displayMode="ALL";check(EntryFormatter.runLines(entries,true,false).size()==3&&FailureDisplay.header(true),"Default failed display changed");
  BridgeConfig.failedDisplay="HIDE";check(EntryFormatter.runLines(entries,true,false).isEmpty()&&EntryFormatter.runLines(entries,true,true).isEmpty()&&!FailureDisplay.header(true),"Failed bridge still visible");check(EntryFormatter.runLines(entries,false,false).size()==3&&FailureDisplay.header(false),"Successful bridge filtered");
  BridgeConfig.failedDisplay="FAIL_TAPS_ONLY";for(String mode:new String[]{"ALL","COORDS_ONLY","LAST_TURN","TIME","TICKS"}){BridgeConfig.displayMode=mode;List<String> chat=EntryFormatter.runLines(entries,true,false),hud=EntryFormatter.runLines(entries,true,true);check(chat.size()==1&&hud.size()==1&&!FailureDisplay.header(true),"Fail-only HUD/chat shows other entries/header in "+mode);if(!mode.equals("COORDS_ONLY"))check(chat.get(0).contains("Fail Tap/4b")&&!chat.get(0).contains("Last:"),"Fail sample relabelled in "+mode);}
  check(FailureDisplay.entries(Arrays.asList(tap,turn),true).isEmpty(),"Invented fail-tap sample");check(entries.size()==3,"Original recording samples modified");BridgeConfig.save();BridgeConfig.failedDisplay="ALL";BridgeConfig.load();check(BridgeConfig.failedDisplay.equals("FAIL_TAPS_ONLY"),"Failed display preference lost after restart");
  System.out.println("PASS: failed-run hide/fail-only filtering for HUD/chat, successful-run isolation, compact/time modes and setting persistence");
 }
}
