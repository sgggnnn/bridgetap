package com.bridger.momentum;
import net.minecraft.unmapped.C_85740840;
public final class FailureDisplayScreen extends BridgeScreen {
 private final C_85740840 parent;
 public FailureDisplayScreen(C_85740840 parent){this.parent=parent;}
 private void choose(String value){BridgeConfig.failedDisplay=value;BridgeConfig.save();}
 @Override protected void paint(){header("Failed bridge display","Choose what remains visible after a failed attempt");button("Back",752,15,88,this::back);
  option("ALL","Show all failed bridge information","Keep the time, flat, taps, turn and fail samples.",88);
  option("HIDE","Hide failed bridges","Hide failed-run information from the coordinate HUD and end-of-run chat.",200);
  option("FAIL_TAPS_ONLY","Show Fail Tap / Fail 4b only","Show only Fail Tap and Fail 4b entries, without the failed-run header or other jumps.",312);
  toggle("Auto fail analysis in chat",BridgeConfig.autoFailAnalysis,38,430,784,()->{BridgeConfig.autoFailAnalysis=!BridgeConfig.autoFailAnalysis;BridgeConfig.save();});
  footer("Successful runs are unchanged. Replays, saved details and statistics keep all samples.");
 }
 private void option(String value,String title,String description,int y){panel(20,y,820,96);button(title,38,y+15,784,30,true,value.equals(BridgeConfig.failedDisplay),()->choose(value));label(description,38,y+62);}
 @Override protected void back(){open(parent);}
}
