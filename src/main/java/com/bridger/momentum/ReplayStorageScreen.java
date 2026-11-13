package com.bridger.momentum;
import net.minecraft.unmapped.C_85740840;
public final class ReplayStorageScreen extends BridgeScreen {
 private final C_85740840 parent;private String error="";private long version=-1;private int total,automatic;
 public ReplayStorageScreen(C_85740840 parent){this.parent=parent;}
 @Override protected void paint(){RunHistory history=BridgeTracker.history();long observed=history.catalogVersion();java.util.List<RunHistory.Run> saved=history.savedCached();if(version!=observed){version=observed;total=saved.size();automatic=0;for(RunHistory.Run run:saved)try{if(!run.isProtected()&&!run.isImported()&&!history.isFavorite(run))automatic++;}catch(Exception ignored){}}
  header("Replay storage","Control automatic saves and keep large libraries responsive");panel(20,74,820,375);
  text("NEW RECORDINGS",40,94,TEXT);label("The latest 10 runs stay in memory. Save replay protects a run permanently.",40,120);
  String[] modes={"Recent only - no automatic disk saves","Auto-save successful runs","Auto-save all runs"};dropdown("auto-save",modes[ReplayStorage.mode()],modes,ReplayStorage.mode(),40,154,778,ReplayStorage::mode);
  stepper("Automatic replay limit",String.valueOf(ReplayStorage.limit()),40,201,778,()->ReplayStorage.limit(ReplayStorage.limit()-25),()->ReplayStorage.limit(ReplayStorage.limit()+25));
  label("Manual saves, favorites and imports are never counted toward this limit.",40,241);line(40,267,778);
  text("EXISTING LIBRARY",40,287,TEXT);label(history.loadingSaved()?"Loading library in the background...":total+" recordings on disk / "+automatic+" unprotected automatic recordings",40,310);
  button(ReplayStorage.archiving()?"Archiving...":"Archive older automatic replays",40,341,480,30,!ReplayStorage.archiving(),true,()->ReplayStorage.archive(history));button("Open archive",536,341,282,30,true,false,()->{try{error=BridgeFileActions.open(history.archiveFolder());}catch(Exception ex){error=ex.getMessage();}});
  label("Moves excess automatic files to an archive. Nothing is deleted.",40,391);clipped(ReplayStorage.message(),40,416,778,MUTED);button("Back",696,442,144,this::back);footer(error.isEmpty()?"Automatic saving defaults OFF / Protected replays are kept / Archive is reversible":error);
 }
 @Override protected void back(){open(parent);}
}
