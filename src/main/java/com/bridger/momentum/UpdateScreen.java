package com.bridger.momentum;
import net.minecraft.unmapped.C_85740840;

/** Consent is required before download, and separately before closing/restarting Minecraft. */
public final class UpdateScreen extends BridgeScreen {
 private final C_85740840 parent;
 public UpdateScreen(C_85740840 parent){this.parent=parent;}
 @Override protected void paint(){
  header("BridgeTap updates","Stable releases / Minecraft 1.8.9");panel(100,90,660,330);
  text("Installed: "+(UpdateManager.installed.isEmpty()?"development build":UpdateManager.installed),122,111,MUTED);
  UpdateManager.State state=UpdateManager.state;
  String title=state==UpdateManager.State.AVAILABLE?"A NEW VERSION IS AVAILABLE":state==UpdateManager.State.READY?"UPDATE READY - RESTART REQUIRED":state==UpdateManager.State.DOWNLOADING?"DOWNLOADING AND VERIFYING":state==UpdateManager.State.CURRENT?"YOU ARE UP TO DATE":state==UpdateManager.State.FAILED?"UPDATE COULD NOT BE COMPLETED":"CHECKING FOR UPDATES";
  text(title,122,151,state==UpdateManager.State.FAILED?RED:ACCENT);
  if(UpdateManager.release!=null){label("Latest: "+UpdateManager.release.version,122,187);clipped(UpdateManager.release.name,122,209,610,TEXT);}
  if(state==UpdateManager.State.AVAILABLE){label("Install the matching edition from the official GitHub release?",122,245);label("The old JAR is replaced after Minecraft closes. A backup is kept.",122,268);button("Yes, install update",122,322,290,34,true,true,UpdateManager::install);button("Not now",438,322,298,34,true,false,this::back);}
  else if(state==UpdateManager.State.READY){label("Download verified. Installation finishes when Minecraft closes.",122,245);label(UpdateManager.canRestart()?"Restart through OneClient using the same profile?":"Close Minecraft, then reopen this profile from your launcher.",122,268);button(UpdateManager.canRestart()?"Yes, restart now":"Close Minecraft to install",122,322,290,34,true,true,UpdateManager::restart);button("Later",438,322,298,34,true,false,this::back);}
  else if(state==UpdateManager.State.DOWNLOADING){label((UpdateManager.downloaded/1024)+" KiB downloaded",122,251);label("Keep Minecraft open until verification finishes.",122,275);}
  else if(state==UpdateManager.State.FAILED){clipped(UpdateManager.error,122,245,610,RED);button("Try again",122,322,290,34,true,true,()->{UpdateManager.check();});button("Close",438,322,298,34,true,false,this::back);}
  else if(state==UpdateManager.State.CURRENT){label("No newer stable release is available for this edition.",122,245);button("Close",122,322,614,34,true,true,this::back);}
  toggle("Check for updates at startup",UpdateManager.automatic,122,377,614,UpdateManager::toggle);
  button("View GitHub releases",100,442,260,UpdateManager::releasePage);button("Back",640,442,120,this::back);
  footer("Checks GitHub only / No account or gameplay data uploaded / Java installer helper");
 }
 @Override protected void back(){if(UpdateManager.state==UpdateManager.State.DOWNLOADING)return;UpdateManager.dismiss();open(parent);}
}
