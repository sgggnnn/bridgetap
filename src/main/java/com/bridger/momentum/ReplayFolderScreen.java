package com.bridger.momentum;

import java.util.*;
import java.text.SimpleDateFormat;

/** File-manager view: folder navigation, replay table and selected-file details. */
public final class ReplayFolderScreen extends BridgeScreen {
    private final BridgeHistoryScreen parent;private final boolean home;private ReplayFolders library;
    private List<RunHistory.Run> all=new ArrayList<>(),rows=new ArrayList<>();private List<String> folders=new ArrayList<>();
    private String folder="",selected="",search="",message="Drag a replay onto a folder to move it.",modal="",name="";
    private int page,folderOffset,sortKey;private boolean ascending=false,moving,dragging;private int pressX,pressY;
    private ReplayQuery.Status status=ReplayQuery.parse(ReplayQuery.Status.class,BridgeConfig.replayStatus,ReplayQuery.Status.ALL);
    private int source="IMPORTED".equals(BridgeConfig.replaySource)?1:"LOCAL".equals(BridgeConfig.replaySource)?2:0;
    private int indexedModes=-1;private ServerMode.Filter mode=ReplayQuery.parse(ServerMode.Filter.class,BridgeConfig.replayMode,ServerMode.Filter.ALL);
    private final Map<String,int[]> folderHits=new LinkedHashMap<>();
    public static ReplayFolderScreen home(){return new ReplayFolderScreen(new BridgeHistoryScreen(),null,true);}
    public ReplayFolderScreen(BridgeHistoryScreen parent,RunHistory.Run run){this(parent,run,false);}
    private ReplayFolderScreen(BridgeHistoryScreen parent,RunHistory.Run run,boolean home){this.parent=parent;this.home=home;if(run!=null)selected=run.id;refresh();}
    public void reload(){refresh();}
    private void watch(){action(()->{RunHistory.Run run=current();if(run==null)throw new IllegalArgumentException("Select a replay");if(BridgeTracker.isBridging())throw new IllegalArgumentException("Finish the current bridge first");ReplayData data=BridgeTracker.history().replay(run);if(data==null)throw new IllegalArgumentException("This save contains statistics only");if(!ReplaySession.start(run,data))throw new IllegalArgumentException("Join a world before watching a replay");});}
    private interface Operation{void run()throws Exception;}
    private void action(Operation op){try{op.run();}catch(Exception ex){message="Folders: "+ex.getMessage();}}
    private void refresh(){action(()->{
        library=ReplayFolders.local();Map<String,RunHistory.Run> unique=new LinkedHashMap<>();for(RunHistory.Run run:BridgeTracker.history().recent())unique.put(run.id,run);for(RunHistory.Run run:BridgeTracker.history().saved())unique.put(run.id,run);all=new ArrayList<>(unique.values());BridgeTracker.history().indexModes(all);for(RunHistory.Run run:all)library.fileByServer(BridgeTracker.history(),run);folders=new ArrayList<>(library.folders().keySet());filter();
    });}
    private void filter(){
        rows=new ArrayList<>();for(RunHistory.Run run:all)if((mode==ServerMode.Filter.ALL||(run.modeReady()&&mode.matches(run.mode())))&&(source==0||run.isImported()==(source==1))&&(status==ReplayQuery.Status.ALL||run.status.equals(status.name()))&&(folder.equals(ReplayFolders.SAVED)?run.isProtected():library.matches(run.id,folder))&&(run.displayName()+" "+run.id).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))rows.add(run);
        Comparator<RunHistory.Run> comparator=sortKey==1?Comparator.comparing(RunHistory.Run::displayName,String.CASE_INSENSITIVE_ORDER):sortKey==2?Comparator.comparingDouble(r->r.time):Comparator.comparingLong(r->r.created);if(!ascending)comparator=comparator.reversed();rows.sort(comparator.thenComparing(r->r.id));page=Math.min(page,Math.max(0,(rows.size()-1)/9));boolean found=false;for(RunHistory.Run run:rows)if(run.id.equals(selected))found=true;if(!found)selected=rows.isEmpty()?"":rows.get(0).id;
    }
    private RunHistory.Run current(){for(RunHistory.Run run:all)if(run.id.equals(selected))return run;return null;}
    private void choose(String id){if(moving){if(!id.equals(ReplayFolders.ALL)&&!id.equals(ReplayFolders.SAVED))move(id);return;}folder=id;page=0;filter();selected=rows.isEmpty()?"":rows.get(0).id;}
    private void move(String id){action(()->{RunHistory.Run run=current();if(run==null)throw new IllegalArgumentException("Select a replay first");BridgeTracker.history().save(run);library.move(run.id,id);moving=false;dragging=false;message="Moved "+run.displayName()+" to "+library.label(id)+".";refresh();});}
    private void sort(int key){if(sortKey==key)ascending=!ascending;else{sortKey=key;ascending=key==1;}filter();}
    @Override protected void paint(){
        int ready=0;for(RunHistory.Run run:all)if(run.modeReady())ready++;if(ready!=indexedModes){indexedModes=ready;action(()->{for(RunHistory.Run run:all)library.fileByServer(BridgeTracker.history(),run);folders=new ArrayList<>(library.folders().keySet());});filter();}
        header("Replay files","Folders, recordings and details");button("Back",752,15,88,this::back);
        button("+ New folder",20,60,154,()->{modal="create";name="";});
        button("Rename folder",186,60,146,26,!folder.isEmpty()&&!folder.equals(ReplayFolders.UNFILED)&&!folder.equals(ReplayFolders.SAVED),false,()->{modal="folder";name=library.label(folder);});
        button("Delete folder",344,60,146,26,!folder.isEmpty()&&!folder.equals(ReplayFolders.UNFILED)&&!folder.equals(ReplayFolders.SAVED),false,()->modal="delete");
        dropdown("tools","Tools",new String[]{"Island library","Video clips","Import replays","Export selected replay","Settings","Auto server folders: "+(library.autoServers()?"ON":"OFF"),"Delete fails (2d): "+(library.cleanupFailures()?"ON":"OFF"),"Block appearance","Export replay video","Analyze fail"},-1,510,60,182,i->{if(i==0)open(new IslandScreen(this,current()));else if(i==1)open(new ClipScreen(this));else if(i==2)open(new BridgeImportScreen(parent,this));else if(i==4)open(new BridgeSettingsScreen(this));else if(i==5)action(()->{library.toggleAutoServers();refresh();});else if(i==6)action(()->{library.toggleCleanupFailures();library.cleanupDue(BridgeTracker.history(),System.currentTimeMillis());refresh();});else if(i==7)open(new PlacementAppearanceScreen(this));else if(i==8)action(()->{if(current()==null)throw new IllegalArgumentException("Select a replay first");open(new ReplayVideoExportScreen(this,current()));});else if(i==9)action(()->{if(current()==null)throw new IllegalArgumentException("Select a replay first");open(new ReplayFailureScreen(this,current()));});else action(()->{if(current()==null)throw new IllegalArgumentException("Select a replay first");java.nio.file.Path file=BridgeTracker.history().exportReplay(current());message="Exported: "+file.getFileName();BridgeFileActions.open(BridgeTracker.history().exportsFolder());});});
        button("Refresh",704,60,136,this::refresh);
        panel(20,105,166,359);panel(198,140,422,324);panel(632,105,208,359);
        text("FOLDERS",32,116,MUTED);folderHits.clear();folderRow(ReplayFolders.ALL,"All replays",140);folderRow(ReplayFolders.SAVED,"Saved replays",171);folderRow(ReplayFolders.UNFILED,"Unfiled",202);line(32,234,142);
        for(int i=folderOffset;i<Math.min(folders.size(),folderOffset+6);i++)folderRow(folders.get(i),library.label(folders.get(i)),247+(i-folderOffset)*30);
        button("Up",32,435,64,20,folderOffset>0,false,()->folderOffset=Math.max(0,folderOffset-1));button("Down",102,435,72,20,folderOffset+6<folders.size(),false,()->folderOffset++);
        input("search",search,"Search replays...",198,105,142,64,value->{search=value;page=0;filter();});
        String[] statuses={"All runs","Successful only","Failed only"};dropdown("run-status",statuses[status.ordinal()],statuses,status.ordinal(),350,105,130,i->{status=ReplayQuery.Status.values()[i];BridgeConfig.replayStatus=status.name();BridgeConfig.save();page=0;filter();});
        String[] sources={"All sources","Imported only","Recorded here"};dropdown("run-source",sources[source],sources,source,490,105,130,i->{source=i;BridgeConfig.replaySource=new String[]{"ALL","IMPORTED","LOCAL"}[i];BridgeConfig.save();page=0;filter();});
        String[] modes=java.util.Arrays.stream(ServerMode.Filter.values()).map(value->value.label).toArray(String[]::new);dropdown("run-mode","Mode: "+mode.label,modes,mode.ordinal(),208,140,190,i->{mode=ServerMode.Filter.values()[i];BridgeConfig.replayMode=mode.name();BridgeConfig.save();page=0;filter();});if(ready<all.size())label("Detecting modes...",412,149);
        button("Name"+(sortKey==1?(ascending?" +":" -"):""),208,180,148,20,true,false,()->sort(1));label("Mode",365,186);button("Time",430,180,72,20,true,false,()->sort(2));button("Date",514,180,94,20,true,false,()->sort(0));
        for(int i=page*9;i<Math.min(rows.size(),page*9+9);i++)row(i);
        if(rows.isEmpty())label("This folder has no matching replays.",214,218);
        button("Previous",210,438,92,20,page>0,false,()->page--);button("Next",314,438,76,20,(page+1)*9<rows.size(),false,()->page++);label((page+1)+" / "+Math.max(1,(rows.size()+8)/9),420,445);
        details();footer(moving?"Choose a destination folder on the left. Esc cancels the move.":message);
        if(dragging){rect(mouseX+9,mouseY+8,150,20,0xEE203B4E);clipped("Move replay to folder",mouseX+15,mouseY+14,140,ACCENT);}
        if(!modal.isEmpty())modal();
    }
    private void folderRow(String id,String title,int y){
        if(folder.equals(id))rect(26,y,154,26,0xFF244052);rect(33,y+8,11,9,0xFFD9BD6B);rect(34,y+5,5,4,0xFFD9BD6B);int indent=Math.min(24,library.depth(id)*12);clipped((indent>0?"  ":"")+title,51+indent,y+9,121-indent,folder.equals(id)?ACCENT:TEXT);
        region(26,y,154,26,()->choose(id));folderHits.put(id,new int[]{26,y,154,26});
    }
    private void row(int index){RunHistory.Run run=rows.get(index);int y=210+(index-page*9)*25;
        if(run.id.equals(selected))rect(204,y,410,24,0xFF294256);else if(over(204,y,410,24))rect(204,y,410,24,0xFF202E40);
        rect(212,y+8,5,7,"SUCCESS".equals(run.status)?GREEN:RED);clipped(run.displayName(),226,y+8,131,TEXT);clipped(run.modeReady()?run.mode().label:"...",365,y+8,64,run.mode()==ServerMode.Mode.UNKNOWN?MUTED:ACCENT);text(String.format(Locale.US,"%.2fs",run.time),437,y+8,MUTED);label(new SimpleDateFormat("MM/dd HH:mm",Locale.US).format(new Date(run.created)),519,y+8);
        region(204,y,410,24,()->{selected=run.id;pressX=mouseX;pressY=mouseY;dragging=false;});
    }
    private void details(){
        text("DETAILS",646,121,MUTED);RunHistory.Run run=current();
        if(run==null){label("Select a replay.",646,156);return;}
        clipped(run.displayName(),646,150,180,TEXT);clipped("Folder: "+library.pathLabel(library.folderOf(run.id)),646,181,180,MUTED);clipped("Mode: "+(run.modeReady()?run.mode().label:"Detecting..."),646,166,180,ACCENT);
        text(String.format(Locale.US,"%.3f seconds",run.time),646,213,TEXT);label(run.jumps+" jumps / Axis "+run.axis,646,238);label(run.status,646,263);button(run.isProtected()?"Saved":"Save replay",740,257,86,24,true,run.isProtected(),()->action(()->{BridgeTracker.history().save(run);message="Replay saved and protected from automatic cleanup.";refresh();}));
        label(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date(run.created)),646,294);
        clipped("Server: "+run.serverLabel(),646,310,180,MUTED);
        boolean playable=BridgeTracker.history().hasReplay(run);button("PLAY REPLAY",646,330,180,30,playable,true,this::watch);
        button("Rename",646,371,86,26,true,false,()->{modal="replay";name=run.displayName();});
        button("Details",740,371,86,26,true,false,()->open(new BridgeReplayDetailsScreen(this,run)));
        button("Move to folder...",646,411,180,30,true,false,()->{moving=true;message="Choose the destination folder.";});
    }
    private void modal(){
        rect(0,0,WIDTH,HEIGHT,0xCC080E18);region(0,0,WIDTH,HEIGHT,()->{});panel(200,164,460,174);
        text(modal.equals("delete")?"DELETE FOLDER?":modal.equals("create")?"NEW FOLDER":modal.equals("folder")?"RENAME FOLDER":"RENAME REPLAY",220,184,TEXT);
        if(modal.equals("delete"))label("This folder and subfolders will be removed. Replays are kept.",220,223);else input("name",name,"Name",220,214,420,64,value->name=value);
        button("Cancel",220,284,196,30,true,false,()->modal="");button(modal.equals("delete")?"Delete folder":"Save",440,284,196,30,true,true,()->action(()->{
            if(modal.equals("create")){folder=library.create(name);folderOffset=Math.max(0,new ArrayList<>(library.folders().keySet()).indexOf(folder)-6);}
            else if(modal.equals("folder"))library.rename(folder,name);
            else if(modal.equals("delete")){library.delete(folder);folder=ReplayFolders.UNFILED;}
            else if(current()!=null)BridgeTracker.history().rename(current(),name);
            modal="";refresh();message="Folder library updated.";
        }));
    }
    @Override public void dragPreview(int x,int y){super.dragPreview(x,y);if(modal.isEmpty()&&current()!=null&&pressX>=204&&pressX<614&&(Math.abs(x-pressX)>6||Math.abs(y-pressY)>6))dragging=true;}
    @Override public void releasePreview(){if(dragging){for(Map.Entry<String,int[]> e:folderHits.entrySet()){int[] b=e.getValue();if(!e.getKey().equals(ReplayFolders.ALL)&&!e.getKey().equals(ReplayFolders.SAVED)&&mouseX>=b[0]&&mouseX<b[0]+b[2]&&mouseY>=b[1]&&mouseY<b[1]+b[3]){move(e.getKey());break;}}}dragging=false;pressX=0;}
    @Override protected void scroll(int x,int y,int direction){if(x<190)folderOffset=Math.max(0,Math.min(Math.max(0,folders.size()-6),folderOffset+direction));else page=Math.max(0,Math.min(Math.max(0,(rows.size()-1)/9),page+direction));}
    @Override protected boolean escapeModal(){if(!modal.isEmpty()){modal="";return true;}if(moving){moving=false;return true;}return false;}
    @Override protected void back(){if(home)open(null);else{parent.refreshFolders();open(parent);}}
}
