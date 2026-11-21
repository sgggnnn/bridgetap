package com.bridger.momentum;

/** One task category at a time; general settings are separate from library tools. */
public final class ReplayToolsScreen extends BridgeGlassScreen {
 private final ReplayFolderScreen parent;private final BridgeHistoryScreen history;private final RunHistory.Run run;private String message="";private ReplayFolders folders;private int category;
 private static final String[] CATEGORIES={"Share & review","Islands & overlays","Clips & statistics","Library management"};
 private static final String[] DESCRIPTIONS={"Work with the replay selected in your library.","Create your look without changing the server world.","Record clips and track your progress.","Import, organize and manage your recordings."};
 public ReplayToolsScreen(ReplayFolderScreen parent,BridgeHistoryScreen history,RunHistory.Run run){this.parent=parent;this.history=history;this.run=run;try{folders=ReplayFolders.local();}catch(Exception ex){message="Library settings are unavailable.";}}
 private interface Work{void run()throws Exception;}
 private void action(Work work){try{work.run();}catch(Exception ex){message=ex.getMessage();}}
 @Override protected boolean flatControls(){return true;}
 private void item(String title,String description,int row,boolean enabled,Runnable action){
  int y=176+row*68;button(title,264,y,310,36,enabled,false,false,enabled?"":"Select a replay in the library first",action);clipped(description,600,y+12,480,MUTED);
 }
 @Override protected void paint(){
  windowHeader("Tools");button("Guide",856,25,104,32,true,false,()->open(new BridgeTutorialScreen(this)));button("Back",972,25,116,32,true,false,this::back);
  sidebar("WORKSPACE");
  for(int i=0;i<CATEGORIES.length;i++){final int selected=i;sidebarItem(CATEGORIES[i],138+i*44,category==i,()->{category=selected;message="";});}
  button("BridgeTap Settings",32,514,184,32,true,false,()->open(new BridgeSettingsScreen(this)));button("Join Discord",32,558,184,32,true,false,()->message=CommunityLinks.discord());
  ui.strongText(CATEGORIES[category],264,104,TEXT,1.2f);clipped(DESCRIPTIONS[category],264,132,824,MUTED);
  if(category==0){
   item("Copy share code","Share a replay through a message.",0,run!=null,()->open(new ReplayShareScreen(this,run)));
   item("Export replay file","Save a .btreplay to send to a friend.",1,run!=null,()->action(()->{java.nio.file.Path file=BridgeTracker.history().exportReplay(run);message="Exported: "+file.getFileName();BridgeFileActions.open(BridgeTracker.history().exportsFolder());}));
   item("Export video","Render a video of this replay.",2,run!=null,()->open(new ReplayVideoExportScreen(this,run)));
   item("Bookmarks","Jump to key moments in this replay.",3,run!=null,()->open(new ReplayBookmarksScreen(this,run)));
   item("Analyze fail","Review the last placement of this run.",4,run!=null,()->action(()->open(new ReplayFailureScreen(this,run))));
  }else if(category==1){
   item("Island library","Choose, place or customize an island.",0,true,()->open(new IslandScreen(this,run)));
   item("Block appearance","Change the blocks you see and hold.",1,true,()->open(new PlacementAppearanceScreen(this)));
   item("Sky overlay","Extract and recolor a pack's sky.",2,true,()->open(new SkyOverlayScreen(this)));
   item("Sandstone overlay","Extract and recolor sandstone textures.",3,true,()->open(new SkyOverlayScreen(this,true)));
  }else if(category==2){
   item("Video clips & audio","Set up recording, sound and microphone.",0,true,()->open(new ClipScreen(this)));
   item("Statistics","See times, success rate and trends.",1,true,()->open(new StatisticsScreen(this)));
  }else{
   item("Import replay files","Add recordings shared by other players.",0,true,()->open(new BridgeImportScreen(history,parent)));
   item("Storage & auto-save","Choose what to save and keep.",1,true,()->open(new ReplayStorageScreen(this)));
   if(folders!=null){
    toggle("Group replays by server",folders.autoServers(),264,340,824,()->action(()->{folders.toggleAutoServers();parent.reload();}));
    toggle("Delete unprotected fails after 2 days",folders.cleanupFailures(),264,394,824,()->action(()->{folders.toggleCleanupFailures();ReplayStorage.organize(BridgeTracker.history(),BridgeTracker.history().savedCached());parent.reload();}));
    label("Saved replays and favorites stay protected.",264,450);
   }
  }
  windowStatus(message.isEmpty()?(category==0?(run==null?"Select a replay in the library to use these actions.":"Selected: "+run.displayName()):"Choose a category on the left / Esc: return to replays"):message);
 }
 @Override protected void back(){parent.reload();open(parent);}
}
