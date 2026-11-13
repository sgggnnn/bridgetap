package com.bridger.momentum;

/** Small grouped tool hub; replay actions remain scoped to the selected recording. */
public final class ReplayToolsScreen extends BridgeScreen {
 private final ReplayFolderScreen parent;private final BridgeHistoryScreen history;private final RunHistory.Run run;private String message="";private ReplayFolders folders;
 public ReplayToolsScreen(ReplayFolderScreen parent,BridgeHistoryScreen history,RunHistory.Run run){this.parent=parent;this.history=history;this.run=run;try{folders=ReplayFolders.local();}catch(Exception ex){message=ex.getMessage();}}
 private interface Work{void run()throws Exception;}
 private void action(Work work){try{work.run();}catch(Exception ex){message=ex.getMessage();}}
 @Override protected void paint(){header("Replay tools","Choose a category / Actions for the selected replay");button("Back",752,15,88,this::back);
  panel(20,74,400,174);panel(440,74,400,174);panel(20,266,400,190);panel(440,266,400,190);
  text("SELECTED REPLAY",38,93,ACCENT);clipped(run==null?"Select a recording in the library first.":run.displayName(),38,115,360,MUTED);
  button("Export replay file",38,143,364,26,run!=null,false,()->action(()->{java.nio.file.Path file=BridgeTracker.history().exportReplay(run);message="Exported: "+file.getFileName();BridgeFileActions.open(BridgeTracker.history().exportsFolder());}));
  button("Export video",38,177,176,26,run!=null,false,()->open(new ReplayVideoExportScreen(this,run)));button("Analyze fail",226,177,176,26,run!=null,false,()->action(()->open(new ReplayFailureScreen(this,run))));
  text("ISLANDS & APPEARANCE",458,93,ACCENT);label("Decorations and client-side block visuals",458,115);
  button("Island library",458,143,364,26,true,false,()->open(new IslandScreen(this,run)));button("Block appearance",458,177,364,26,true,false,()->open(new PlacementAppearanceScreen(this)));
  text("RECORDINGS",38,285,ACCENT);button("Video clips & audio",38,315,364,28,true,false,()->open(new ClipScreen(this)));button("Import replay files",38,355,364,28,true,false,()->open(new BridgeImportScreen(history,parent)));button("Replay storage / auto-save",38,395,364,28,true,false,()->open(new ReplayStorageScreen(this)));
  text("SETTINGS & ORGANIZATION",458,285,ACCENT);button("BridgeTap settings",458,315,364,28,true,false,()->open(new BridgeSettingsScreen(this)));if(folders==null){footer(message);return;}
  button("Server folders: "+(folders.autoServers()?"ON":"OFF"),458,355,364,28,true,folders.autoServers(),()->action(()->{folders.toggleAutoServers();parent.reload();}));
  button("Delete unprotected fails after 2 days: "+(folders.cleanupFailures()?"ON":"OFF"),458,395,364,28,true,folders.cleanupFailures(),()->action(()->{folders.toggleCleanupFailures();ReplayStorage.organize(BridgeTracker.history(),BridgeTracker.history().savedCached());parent.reload();}));
  footer(message.isEmpty()?"Saved replays and favorites stay protected from automatic cleanup.":message);
 }
 @Override protected void back(){parent.reload();open(parent);}
}
