package com.bridger.momentum;

import java.util.*;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;

/** File browser: presentation and input only; recordings still belong to RunHistory. */
public final class ReplayFolderScreen extends BridgeGlassScreen {
    public static final int VIEW_WIDTH=1120,VIEW_HEIGHT=640,PAGE_SIZE=7;
    private static final int LEFT=16,LEFT_WIDTH=216,CENTER=244,CENTER_WIDTH=544,RIGHT=800,RIGHT_WIDTH=304;
    private static final int ROW_TOP=218,ROW_HEIGHT=48,FOLDER_TOP=360,FOLDER_VISIBLE=6;
    private static final String TRASH="browser-trash";
    private final BridgeHistoryScreen parent;private final boolean home;private ReplayFolders library;
    private ReplayBrowserPreferences preferences;
    private List<RunHistory.Run> all=new ArrayList<>(),rows=new ArrayList<>();private List<String> folders=new ArrayList<>();
    private Map<String,Integer> counts=new HashMap<>();
    private String folder="",selected="",search="",message="Drag a replay onto a folder to move it.",modal="",name="",modalRun="",context="";
    private String requestedTool="";
    private int page,folderOffset,sortKey,contextX,contextY;private boolean ascending,moving,dragging;private int pressX,pressY;
    private String lastClick="";private long clickTime;
    private ReplayQuery.Status status=ReplayQuery.parse(ReplayQuery.Status.class,BridgeConfig.replayStatus,ReplayQuery.Status.ALL);
    private int source="IMPORTED".equals(BridgeConfig.replaySource)?1:"LOCAL".equals(BridgeConfig.replaySource)?2:0;
    private long catalogVersion=-1,folderVersion=-1,nextIndex;private int indexCursor,indexedModes=-1;
    private ServerMode.Filter mode=ReplayQuery.parse(ServerMode.Filter.class,BridgeConfig.replayMode,ServerMode.Filter.ALL);
    private final Map<String,int[]> folderHits=new LinkedHashMap<>();
    public static ReplayFolderScreen home(){return new ReplayFolderScreen(new BridgeHistoryScreen(),null,true);}
    public static ReplayFolderScreen forTool(String tool){ReplayFolderScreen screen=home();screen.requestedTool=tool;screen.message="Select a replay, then click Open selected tool.";return screen;}
    public ReplayFolderScreen(BridgeHistoryScreen parent,RunHistory.Run run){this(parent,run,false);}
    private ReplayFolderScreen(BridgeHistoryScreen parent,RunHistory.Run run,boolean home){
        this.parent=parent;this.home=home;
        try{preferences=new ReplayBrowserPreferences(Paths.get("config/bridgetap/replay-browser.properties"));folder=preferences.folder;search=preferences.search;sortKey=preferences.sort;ascending=preferences.ascending;
            if(java.nio.file.Files.isRegularFile(Paths.get("config/bridgetap/replay-browser.properties"))){status=ReplayQuery.parse(ReplayQuery.Status.class,preferences.status,ReplayQuery.Status.ALL);source="IMPORTED".equals(preferences.source)?1:"LOCAL".equals(preferences.source)?2:0;mode=ReplayQuery.parse(ServerMode.Filter.class,preferences.mode,ServerMode.Filter.ALL);}
        }catch(Exception ex){message="Browser preferences could not be read. Your replays are unchanged.";}
        if(run!=null)selected=run.id;refresh();
    }
    @Override protected int canvasWidth(){return VIEW_WIDTH;}
    @Override protected int canvasHeight(){return VIEW_HEIGHT;}
    @Override protected boolean keyboardNavigation(){return true;}
    private interface Operation{void run()throws Exception;}
    private void action(Operation op){try{op.run();}catch(Exception ex){message=ex instanceof java.nio.file.AccessDeniedException?"This file is busy. Try again in a moment.":ex instanceof java.io.IOException?"Could not update the library. Check folder permissions and try again.":ex.getMessage()==null?"This action could not be completed.":ex.getMessage();}}
    private boolean trashed(RunHistory.Run run){return preferences!=null&&preferences.trash.contains(run.id);}
    private void remember(){
        BridgeConfig.replayStatus=status.name();BridgeConfig.replaySource=new String[]{"ALL","IMPORTED","LOCAL"}[source];BridgeConfig.replayMode=mode.name();
        if(preferences!=null){preferences.folder=folder;preferences.search=search;preferences.status=status.name();preferences.source=BridgeConfig.replaySource;preferences.mode=mode.name();preferences.sort=sortKey;preferences.ascending=ascending;preferences.changed();}
    }
    private void persist(){if(preferences!=null)action(preferences::flush);BridgeConfig.save();}
    public void reload(){refresh();}
    private void refresh(){action(()->{
        library=ReplayFolders.local();RunHistory history=BridgeTracker.history();long observed=history.catalogVersion();Map<String,RunHistory.Run> unique=new LinkedHashMap<>();
        for(RunHistory.Run run:history.savedCached())unique.put(run.id,run);for(RunHistory.Run run:history.recent())unique.put(run.id,run);all=new ArrayList<>(unique.values());catalogVersion=observed;
        ReplayStorage.organize(history,all);folders=new ArrayList<>(library.folders().keySet());
        if(!folder.isEmpty()&&!folder.equals(ReplayFolders.SAVED)&&!folder.equals(ReplayFolders.UNFILED)&&!folder.equals(TRASH)&&!folders.contains(folder))folder="";
        recount();filter();
    });}
    private void recount(){counts=ReplayBrowserModel.counts(all,library,preferences==null?Collections.emptySet():preferences.trash);}
    private void filter(){
        rows=new ArrayList<>();String query=search.trim().toLowerCase(Locale.ROOT);
        for(RunHistory.Run run:all)if((folder.equals(TRASH)?trashed(run):!trashed(run))&&(mode==ServerMode.Filter.ALL||(run.modeReady()&&mode.matches(run.mode())))&&(source==0||run.isImported()==(source==1))&&(status==ReplayQuery.Status.ALL||run.status.equals(status.name()))&&(folder.equals(TRASH)||folder.equals(ReplayFolders.SAVED)?folder.equals(TRASH)||run.isProtected():library.matches(run.id,folder))&&(run.displayName()+" "+run.id+" "+run.serverLabel()).toLowerCase(Locale.ROOT).contains(query))rows.add(run);
        rows.sort(ReplayBrowserModel.order(sortKey,ascending));int selectedIndex=-1;for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(selected))selectedIndex=i;if(selectedIndex<0){selected=rows.isEmpty()?"":rows.get(0).id;selectedIndex=0;}page=selectedIndex/PAGE_SIZE;
    }
    private void goToPage(int value){page=Math.max(0,Math.min(lastPage(),value));if(!rows.isEmpty())selected=rows.get(page*PAGE_SIZE).id;lastClick="";pressX=0;}
    private int lastPage(){return Math.max(0,(rows.size()-1)/PAGE_SIZE);}
    private RunHistory.Run current(){for(RunHistory.Run run:rows)if(run.id.equals(selected))return run;return null;}
    private void changed(){page=0;filter();if(!rows.isEmpty())selected=rows.get(0).id;page=0;lastClick="";remember();}
    private void choose(String id){if(moving){if(validDestination(id))move(id);return;}folder=id;changed();}
    private boolean validDestination(String id){return !id.equals(ReplayFolders.ALL)&&!id.equals(ReplayFolders.SAVED)&&!id.equals(TRASH);}
    private boolean customFolder(){return library!=null&&library.folders().containsKey(folder);}
    private void move(String id){action(()->{RunHistory.Run run=current();if(run==null||trashed(run))throw new IllegalArgumentException("Select an active replay first.");BridgeTracker.history().save(run);library.move(run.id,id);moving=false;dragging=false;message="Moved replay to "+library.pathLabel(id)+".";refresh();});}
    private void sort(int key){if(sortKey==key)ascending=!ascending;else{sortKey=key;ascending=key==1||key==3;}filter();remember();}
    private void watch(){action(()->{RunHistory.Run run=current();if(run==null||trashed(run))throw new IllegalArgumentException("Select an active replay first.");if(BridgeTracker.isBridging())throw new IllegalArgumentException("Finish the current bridge first.");ReplayData data=BridgeTracker.history().replay(run);if(data==null)throw new IllegalArgumentException("This recording has statistics only.");net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.m_20213497();if(mc==null||mc.f_26407825==null||mc.f_28396371==null)throw new IllegalArgumentException("Join a world before watching a replay.");persist();if(!ReplaySession.start(run,data))throw new IllegalArgumentException("Join a world before watching a replay.");});}
    private void save(){action(()->{RunHistory.Run run=current();if(run!=null&&!trashed(run)){BridgeTracker.history().save(run);message="Replay saved and protected from cleanup.";recount();}});}
    private void rename(){RunHistory.Run run=current();if(run!=null){modal="replay";modalRun=run.id;name=run.displayName();focus="";}}
    private void requestDelete(){RunHistory.Run run=current();if(run!=null&&preferences!=null&&!trashed(run)){modal="trash";modalRun=run.id;focus="";}}
    private void restore(){RunHistory.Run run=current();if(run!=null&&preferences!=null){preferences.trash.remove(run.id);remember();persist();recount();filter();message="Replay restored to the library.";}}
    private void clearFilters(){search="";status=ReplayQuery.Status.ALL;source=0;mode=ServerMode.Filter.ALL;focus="";changed();}
    private boolean filtered(){return !search.isEmpty()||status!=ReplayQuery.Status.ALL||source!=0||mode!=ServerMode.Filter.ALL;}
    private void tick(){
        if(preferences!=null)action(preferences::tick);RunHistory history=BridgeTracker.history();if(catalogVersion!=history.catalogVersion())refresh();
        long now=System.currentTimeMillis();if(now<nextIndex)return;nextIndex=now+500;
        history.indexModes(rows.subList(Math.min(page*PAGE_SIZE,rows.size()),Math.min(page*PAGE_SIZE+PAGE_SIZE*2,rows.size())));
        if(mode!=ServerMode.Filter.ALL&&!all.isEmpty()){indexCursor=Math.min(indexCursor,all.size()-1);history.indexModes(all.subList(indexCursor,Math.min(indexCursor+16,all.size())));indexCursor=(indexCursor+16)%all.size();}
        int ready=0;for(RunHistory.Run run:all)if(run.modeReady())ready++;if(ready!=indexedModes){indexedModes=ready;ReplayStorage.organize(history,all);filter();}
        if(library!=null&&folderVersion!=library.revision()){folderVersion=library.revision();folders=new ArrayList<>(library.folders().keySet());recount();filter();}
    }
    private void icon(String kind,String hint,int x,Runnable action){
        button("",x,25,32,32,true,false,false,hint,action);
        int c=MUTED,cx=x+16,cy=41;
        if(kind.equals("close")){for(int i=-5;i<=5;i++){rect(cx+i,cy+i,1,1,c);rect(cx+i,cy-i,1,1,c);}}
        else if(kind.equals("refresh")){
            for(int i=0;i<24;i++){double a=Math.PI*.3+i*Math.PI*1.65/24;ui.roundedRect(cx+6*Math.cos(a)-.8,cy+6*Math.sin(a)-.8,1.6,1.6,.8,c);}
            ui.polygon(new double[]{cx+7,cx+7,cx+2},new double[]{cy-7,cy-1,cy-3},c);
        }else{for(int i=0;i<3;i++){ui.roundedRect(x+8,cy-6+i*6,16,1,0.5,c);ui.roundedRect(x+11+(i%2)*7,cy-8+i*6,3,5,1,c);}}
    }
    @Override protected void paint(){
        tick();windowHeader("Replays");
        button("BridgeTap Settings",472,25,172,32,true,false,()->open(new BridgeSettingsScreen(this)));
        input("search",search,"Search replays...",660,28,306,64,value->{search=value;changed();});
        icon("theme","BridgeGlass appearance",980,()->open(new MenuThemeScreen(this)));
        icon("refresh","Refresh library",1020,()->{BridgeTracker.history().reloadSaved();refresh();});
        icon("close","Close",1060,this::back);
        if(library==null){label("The library is unavailable. Try Refresh.",36,128);return;}
        rect(232,96,1,508,UiTheme.alpha(TEXT,.06));rect(796,96,1,508,UiTheme.alpha(TEXT,.06));
        folderPanel();filters();table();details();
        clipped(moving?"Choose a destination folder on the left. Esc cancels the move.":message,32,622,1056,UiTheme.alpha(TEXT,.5));
        if(dragging){suppressTooltips();RunHistory.Run run=current();if(run!=null){int gx=Math.max(8,Math.min(mouseX+12,VIEW_WIDTH-232)),gy=Math.max(8,Math.min(mouseY+10,VIEW_HEIGHT-56));panel(gx,gy,224,48);clipped(run.displayName(),gx+12,gy+8,200,TEXT);label(run.jumps+" jumps / Axis "+run.axis,gx+12,gy+25);}}
        if(!context.isEmpty())contextMenu();if(!modal.isEmpty())modal();
    }
    private void folderPanel(){
        text("LIBRARY",32,104,UiTheme.alpha(TEXT,.5));
        button("New",32,130,44,28,true,false,()->{modal="create";name="";focus="";});
        button("Rename",84,130,72,28,customFolder(),false,false,customFolder()?"Rename this folder":"Select a custom folder first",()->{modal="folder";name=library.label(folder);focus="";});
        dangerButton("Delete",164,130,52,28,customFolder(),customFolder()?"Remove folder; keep its replays":"Select a custom folder first",()->{modal="delete";focus="";});
        folderHits.clear();folderRow(ReplayFolders.ALL,"All replays",178);folderRow(ReplayFolders.SAVED,"Saved replays",214);folderRow(ReplayFolders.UNFILED,"Unfiled",250);folderRow(TRASH,"Trash",286);
        text("SERVERS & FOLDERS",32,334,MUTED);folderOffset=Math.max(0,Math.min(folderOffset,Math.max(0,folders.size()-FOLDER_VISIBLE)));
        for(int i=folderOffset;i<Math.min(folders.size(),folderOffset+FOLDER_VISIBLE);i++)folderRow(folders.get(i),library.label(folders.get(i)),FOLDER_TOP+(i-folderOffset)*34);
        button("Previous",32,582,88,26,folderOffset>0,false,()->folderOffset=Math.max(0,folderOffset-1));button("More",128,582,88,26,folderOffset+FOLDER_VISIBLE<folders.size(),false,()->folderOffset++);
    }
    private void folderRow(String id,String title,int y){
        boolean target=dragging&&validDestination(id)&&over(24,y,200,32),selectedFolder=folder.equals(id);
        if(target||selectedFolder||over(24,y,200,32))rounded(24,y,200,32,target?UiTheme.alpha(ACCENT,.3):selectedFolder?UiTheme.alpha(ACCENT,.12):UiTheme.alpha(TEXT,.07));
        if(selectedFolder)rect(24,y+6,3,20,ACCENT);int indent=Math.min(24,library.depth(id)*12);rect(34+indent,y+11,11,9,0xFFD9BD6B);rect(35+indent,y+8,5,4,0xFFD9BD6B);
        clipped(title,52+indent,y+9,126-indent,selectedFolder&&ThemeManager.local().current().appearance!=ThemeManager.Appearance.LIGHT_GLASS?ACCENT:TEXT);String number=String.valueOf(counts.getOrDefault(id,0));text(number,210-ui.width(number),y+9,MUTED);
        region(24,y,200,32,()->choose(id));folderHits.put(id,new int[]{24,y,200,32});
    }
    private void filters(){
        button("Tools",256,88,84,26,true,false,()->open(new ReplayToolsScreen(this,parent,current())));
        button("Discord",348,88,66,26,true,false,()->message=CommunityLinks.discord());
        String[] statuses={"All runs","Success","Failed"};dropdown("run-status",statuses[status.ordinal()],statuses,status.ordinal(),422,88,106,i->{status=ReplayQuery.Status.values()[i];changed();});
        String[] sources={"All sources","Imported","Local"};dropdown("run-source",sources[source],sources,source,536,88,114,i->{source=i;changed();});
        String[] modes=Arrays.stream(ServerMode.Filter.values()).map(value->value.label).toArray(String[]::new);dropdown("run-mode",mode.label,modes,mode.ordinal(),658,88,130,i->{mode=ServerMode.Filter.values()[i];changed();});
        label(rows.size()+" replays",CENTER,134);if(BridgeTracker.history().loadingSaved())label("Loading...",CENTER+104,134);else if(mode!=ServerMode.Filter.ALL&&indexedModes<all.size())label("Indexing modes...",CENTER+104,134);
        if(filtered())button("Clear filters",658,126,130,26,true,false,this::clearFilters);
    }
    private void heading(String title,int key,int x,int width){
        ui.textScaled(title,x,188,UiTheme.alpha(TEXT,sortKey==key?.8:.5),11f/12f);
        region(x,178,width,30,()->sort(key));tooltip("Sort by "+title.toLowerCase(Locale.ROOT),x,178,width,30);
        if(sortKey==key){int cx=x+width-10,cy=191;for(int i=0;i<3;i++)rect(cx-i,ascending?cy+i:cy+2-i,i*2+1,1,MUTED);}
    }
    private void table(){
        heading("TIME",2,268,106);heading("SERVER / REPLAY",1,388,194);heading("MODE",3,598,94);heading("DATE",0,704,72);
        for(int i=page*PAGE_SIZE;i<Math.min(rows.size(),page*PAGE_SIZE+PAGE_SIZE);i++)row(i);
        if(rows.isEmpty()){text("No matching replays",268,250,TEXT);label("Choose another folder or clear your filters.",268,278);}
        button("<",458,580,32,28,page>0,false,()->goToPage(page-1));
        String pages=(page+1)+" / "+(lastPage()+1);text(pages,522-ui.width(pages)/2,588,MUTED);
        button(">",554,580,32,28,page<lastPage(),false,()->goToPage(page+1));
    }
    private void row(int index){
        RunHistory.Run run=rows.get(index);int y=ROW_TOP+(index-page*PAGE_SIZE)*ROW_HEIGHT;boolean selectedRow=run.id.equals(selected);
        float amount=animate("row:"+run.id,selectedRow?1:0,14),hover=animate("row-hover:"+run.id,over(252,y,528,44)?1:0,18);
        ui.roundedRect(252,y,528,44,8,UiTheme.alpha(selectedRow?ACCENT:TEXT,selectedRow?.10:.03+hover*.04));
        if(selectedRow)ui.roundedRect(252,y+12,2,20,1,UiTheme.alpha(ACCENT,Math.max(.5,amount)));
        String time=String.format(Locale.US,"%.3fs",run.time);ui.strongText(time,268,y+13,TEXT,1.22f);
        ui.roundedRect(366,y+18,6,6,3,run.status.equals("SUCCESS")?GREEN:RED);
        tooltip(run.status.equals("SUCCESS")?"Completed":"Failed",360,y,22,44);
        clipped(run.serverLabel(),388,y+7,194,TEXT);
        String name=run.displayName();clipped(name.equalsIgnoreCase("Bridge replay")?run.jumps+" jumps / Axis "+run.axis:name,388,y+25,194,UiTheme.alpha(TEXT,.5));
        clipped(run.modeReady()?run.mode().label:"Detecting...",598,y+15,94,MUTED);
        String date=new SimpleDateFormat("MM/dd",Locale.US).format(new Date(run.created));text(date,704,y+15,UiTheme.alpha(TEXT,.5));
        tooltip(new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(new Date(run.created)),700,y,76,44);
        region(252,y,528,44,()->{long now=System.nanoTime();boolean twice=run.id.equals(lastClick)&&now-clickTime<400000000L;selected=run.id;pressX=mouseX;pressY=mouseY;dragging=false;lastClick=run.id;clickTime=now;if(twice){lastClick="";pressX=0;if(requestedTool.isEmpty())watch();else message=BridgeTapMenus.openReplayTool(requestedTool,this,run);}});
    }
    private void stat(String title,String value,int x,int y){label(title,x,y);if(title.equals("Duration"))ui.textScaled(value,x,y+18,TEXT,1.25f);else clipped(value,x,y+19,124,TEXT);}
    private void details(){
        text("DETAILS",816,108,MUTED);RunHistory.Run run=current();if(run==null){text("No replay selected",816,168,TEXT);clipped("Select a replay to see details.",816,200,272,MUTED);return;}
        clipped(run.displayName(),816,144,178,TEXT);statusBadge(run.status.equals("SUCCESS"),1006,140,false);
        stat("Duration",String.format(Locale.US,"%.3fs",run.time),832,198);stat("Mode",run.modeReady()?run.mode().label:"Detecting...",964,198);stat("Jumps",String.valueOf(run.jumps),832,250);stat("Axis",run.axis,964,250);
        label("Date",816,324);clipped(new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(new Date(run.created)),892,324,196,TEXT);
        label("Server",816,358);clipped(run.serverLabel(),892,358,196,TEXT);label("Folder",816,392);clipped(folder.equals(TRASH)?"Trash":library.pathLabel(library.folderOf(run.id)),892,392,196,TEXT);
        if(trashed(run)){button("Restore replay",816,434,272,40,true,true,this::restore);clipped("Files are kept unchanged while in Trash.",816,496,272,MUTED);return;}
        if(!requestedTool.isEmpty())button("Open selected tool",816,434,272,44,true,true,()->message=BridgeTapMenus.openReplayTool(requestedTool,this,run));
        else button("Play Replay",816,434,272,44,BridgeTracker.history().hasReplay(run),true,false,"Statistics-only recordings cannot be played",this::watch);
        button("Rename",816,492,130,36,true,false,this::rename);button(run.isProtected()?"Saved":"Save replay",958,492,130,36,!run.isProtected(),false,false,run.isProtected()?"This replay is already saved and protected":"Protect this replay from cleanup",this::save);
        button("Move",816,540,130,32,true,false,()->{moving=true;context="";});dangerButton("Delete",958,540,130,32,true,"Move to reversible library Trash",this::requestDelete);
        button("View full details",816,582,272,26,true,false,()->open(new BridgeReplayDetailsScreen(this,run)));
    }
    @Override protected void rightClick(int x,int y){
        if(!modal.isEmpty())return;for(int i=page*PAGE_SIZE;i<Math.min(rows.size(),page*PAGE_SIZE+PAGE_SIZE);i++){int y0=ROW_TOP+(i-page*PAGE_SIZE)*ROW_HEIGHT;if(x>=252&&x<780&&y>=y0&&y<y0+44){selected=rows.get(i).id;context=selected;contextX=Math.min(x,VIEW_WIDTH-204);contextY=Math.min(y,VIEW_HEIGHT-204);focus="";pressX=0;return;}}context="";
    }
    private void contextMenu(){
        suppressTooltips();region(0,0,VIEW_WIDTH,VIEW_HEIGHT,()->context="");panel(contextX,contextY,196,198);RunHistory.Run run=current();if(run==null||!run.id.equals(context)){context="";return;}
        if(trashed(run)){button("Restore replay",contextX+8,contextY+8,180,30,true,true,()->{context="";restore();});return;}
        button("Play",contextX+8,contextY+8,180,30,BridgeTracker.history().hasReplay(run),false,()->{context="";watch();});
        button("Rename",contextX+8,contextY+46,180,30,true,false,()->{context="";rename();});button("Save replay",contextX+8,contextY+84,180,30,!run.isProtected(),false,()->{context="";save();});
        button("Move to folder",contextX+8,contextY+122,180,30,true,false,()->{context="";moving=true;});dangerButton("Delete...",contextX+8,contextY+160,180,30,preferences!=null,"Move to reversible library Trash",()->{context="";requestDelete();});
    }
    private void modal(){
        suppressTooltips();rect(0,0,VIEW_WIDTH,VIEW_HEIGHT,UiTheme.alpha(ThemeManager.local().current().appearance==ThemeManager.Appearance.LIGHT_GLASS?0x66758A:0x05080F,.65));region(0,0,VIEW_WIDTH,VIEW_HEIGHT,()->{});panel(312,218,496,210);
        boolean deleting=modal.equals("delete"),trash=modal.equals("trash");text(deleting?"Delete folder?":trash?"Move replay to Trash?":modal.equals("create")?"New folder":modal.equals("folder")?"Rename folder":"Rename replay",336,242,TEXT);
        if(deleting)clipped("Subfolders are removed. Replays are kept in Unfiled.",336,286,448,MUTED);
        else if(trash){clipped("The replay is hidden from this library. Files stay intact.",336,280,448,MUTED);clipped("Restore it from Trash. Unsaved recent runs stay temporary.",336,307,448,MUTED);}
        else input("name",name,"Name",336,282,448,64,value->name=value);
        button("Cancel",336,370,208,34,true,false,()->{modal="";focus="";});
        if(deleting||trash)dangerButton(deleting?"Delete folder":"Move to Trash",568,370,216,34,true,"",this::confirmModal);else button("Save",568,370,216,34,!name.trim().isEmpty(),true,this::confirmModal);
    }
    private void confirmModal(){action(()->{
        if(modal.equals("create")){folder=library.create(name);folders=new ArrayList<>(library.folders().keySet());folderOffset=Math.max(0,folders.indexOf(folder)-FOLDER_VISIBLE+1);}
        else if(modal.equals("folder"))library.rename(folder,name);else if(modal.equals("delete")){library.delete(folder);folder=ReplayFolders.UNFILED;}
        else if(modal.equals("trash")){preferences.trash.add(modalRun);message="Moved to library Trash. Replay files were kept.";}
        else{RunHistory.Run chosen=null;for(RunHistory.Run run:all)if(run.id.equals(modalRun))chosen=run;if(chosen!=null)BridgeTracker.history().rename(chosen,name);}
        modal="";focus="";remember();persist();refresh();
    });}
    @Override public void dragPreview(int x,int y){super.dragPreview(x,y);if(modal.isEmpty()&&context.isEmpty()&&current()!=null&&!trashed(current())&&pressX>=252&&pressX<780&&(Math.abs(x-pressX)>6||Math.abs(y-pressY)>6)){dragging=true;lastClick="";}}
    @Override public void releasePreview(){if(dragging){for(Map.Entry<String,int[]> e:folderHits.entrySet()){int[] b=e.getValue();if(validDestination(e.getKey())&&mouseX>=b[0]&&mouseX<b[0]+b[2]&&mouseY>=b[1]&&mouseY<b[1]+b[3]){move(e.getKey());break;}}}dragging=false;pressX=0;}
    @Override protected void scroll(int x,int y,int direction){if(!modal.isEmpty()||!context.isEmpty())return;if(x<232)folderOffset=Math.max(0,Math.min(Math.max(0,folders.size()-FOLDER_VISIBLE),folderOffset+direction));else if(x<CENTER+CENTER_WIDTH)goToPage(page+direction);}
    @Override protected boolean shortcut(int key,boolean control){
        if(!modal.isEmpty()||!context.isEmpty())return false;
        if(control&&key==33){clearButtonFocus();focus="search";return true;}if(!focus.isEmpty())return false;
        if(key==200||key==208){clearButtonFocus();int at=-1;for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(selected))at=i;at=Math.max(0,Math.min(rows.size()-1,at+(key==200?-1:1)));if(!rows.isEmpty()){selected=rows.get(at).id;page=at/PAGE_SIZE;}return true;}
        if((key==28||key==156)&&!hasButtonFocus()){watch();return true;}if(key==211){requestDelete();return true;}return false;
    }
    @Override protected boolean escapeModal(){if(!modal.isEmpty()){modal="";focus="";return true;}if(!context.isEmpty()){context="";return true;}if(moving){moving=false;return true;}if(!focus.isEmpty()){focus="";return true;}return false;}
    @Override public void m_47002234(){persist();}
    @Override protected void back(){persist();if(home)open(null);else{parent.refreshFolders();open(parent);}}
}
