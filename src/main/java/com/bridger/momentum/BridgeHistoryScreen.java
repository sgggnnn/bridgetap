package com.bridger.momentum;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

/** Replay library with independent tabs, composable filters and stable sorting. */
public class BridgeHistoryScreen extends BridgeScreen {
    private static final int GRID_X=20, GRID_Y=150, CARD_W=263, ROW=76, PAGE_SIZE=8;
    private int view,selected,page,detailOffset;
    private String folder=ReplayFolders.ALL;
    private String message="";
    private ReplaySort sort=ReplaySort.parse(BridgeConfig.replaySort);
    private ReplayQuery query=ReplayQuery.load(),draft;
    private boolean filtersOpen;
    private List<RunHistory.Run> all=Collections.emptyList(),rows=Collections.emptyList();
    private final Map<String,BridgeGeometry.Result> geometry=new HashMap<>();

    public BridgeHistoryScreen(){refresh();}
    private RunHistory.Run current(){return selected>=0&&selected<rows.size()?rows.get(selected):null;}
    private void refresh(){
        try {all=view==2?BridgeTracker.history().favorites():view==1?BridgeTracker.history().saved():BridgeTracker.history().recent();}
        catch(IOException ex){all=Collections.emptyList();message="Could not read saved runs.";}
        filter();
    }
    private void filter(){
        RunHistory.Run previous=current();String id=previous==null?"":previous.id;
        List<RunHistory.Run> result=new ArrayList<>();
        ReplayFolders folders=null;
        if(!folder.equals(ReplayFolders.ALL))try{folders=ReplayFolders.local();}catch(IOException ex){message="Could not read replay folders.";}
        for(RunHistory.Run run:all)if((folder.equals(ReplayFolders.ALL)||(folders!=null&&folders.matches(run.id,folder)))&&query.matches(run,BridgeTracker.history().hasReplay(run)))result.add(run);
        result.sort(sort.comparator());rows=result;selected=0;
        for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(id)){selected=i;break;}
        page=selected/PAGE_SIZE;detailOffset=0;query.store();BridgeConfig.replaySort=sort.name();
    }
    private void changed(){filter();BridgeConfig.save();}
    public void folder(String id){folder=id;refresh();}
    public void refreshFolders(){refresh();}
    private String folderLabel(){try{return ReplayFolders.local().label(folder);}catch(IOException ex){return "Folders unavailable";}}
    private void switchView(int value){view=value;message="";selected=page=0;refresh();}
    public void refreshFor(String id,String status){refresh();for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(id)){selected=i;page=i/PAGE_SIZE;break;}message=status;}
    public void imported(RunHistory.Run run){view=1;query=new ReplayQuery();refreshFor(run.id,"Imported: "+run.displayName());}
    private boolean favorite(RunHistory.Run run){try{return BridgeTracker.history().isFavorite(run);}catch(IOException ex){return false;}}
    private BridgeGeometry.Result dimensions(RunHistory.Run run){
        if(!geometry.containsKey(run.id)){
            BridgeGeometry.Result result;
            try{ReplayData data=BridgeTracker.history().replay(run);result=data==null?BridgeGeometry.Result.unavailable("Summary only"):data.geometry();}
            catch(IOException|IllegalArgumentException ex){result=BridgeGeometry.Result.unavailable("Replay unavailable");}
            geometry.put(run.id,result);
        }
        return geometry.get(run.id);
    }
    private void play(int index){
        if(index<0||index>=rows.size())return;selected=index;
        playReplay(rows.get(index));
    }
    public void playReplay(RunHistory.Run run){
        try{
            ReplayData data=BridgeTracker.history().replay(run);
            if(data==null){message="This older save contains statistics only.";return;}
            if(BridgeTracker.isBridging()){message="Finish the current attempt or use /bt reset first.";return;}
            query.store();BridgeConfig.save();ReplaySession.start(run,data);
        }catch(IOException|IllegalArgumentException ex){message="The replay could not be read.";System.err.println("[BridgeTap] "+ex);}
    }
    @Override protected void paint(){
        header("Replay library","Record. Review. Improve.");
        button("Settings",752,15,88,()->open(new BridgeSettingsScreen(this)));
        tab("Recent",20,59,78,view==0,()->switchView(0));
        tab("Saved",104,59,76,view==1,()->switchView(1));
        tab("Favorites",186,59,96,view==2,()->switchView(2));
        button("Folders: "+folderLabel(),298,60,244,()->open(new ReplayFolderScreen(this,current())));
        dropdown("tools","Tools",new String[]{"Import replays","Open exports folder","Island scenery","Video clips"},-1,660,60,180,i->{if(i==0)open(new BridgeImportScreen(this));else if(i==1)openFiles();else if(i==2)open(new IslandScreen(this,current()));else open(new ClipScreen(this));});
        input("search",query.search,"Search name, ID or technique...",20,99,260,64,value->{query.search=value;filter();});
        dropdown("status",query.status==ReplayQuery.Status.ALL?"All results":query.status==ReplayQuery.Status.SUCCESS?"Success":"Failed",new String[]{"All results","Success","Failed"},query.status.ordinal(),292,99,110,i->{query.status=ReplayQuery.Status.values()[i];changed();});
        String[] keys={"Date","Run time","Name","Jump count"};
        dropdown("sort","Sort: "+keys[sort.key().ordinal()],keys,sort.key().ordinal(),414,99,146,i->{ReplaySort.Key key=ReplaySort.Key.values()[i];sort=ReplaySort.of(key,key!=ReplaySort.Key.DATE);changed();});
        button(sort.ascending()?"Ascending":"Descending",572,99,92,26,true,false,()->{sort=ReplaySort.of(sort.key(),!sort.ascending());changed();});
        button("Filters"+(query.activeFilters()>0?" ("+query.activeFilters()+")":""),676,99,164,26,true,false,()->{draft=copy(query);filtersOpen=true;});
        label(rows.size()+" matching "+(rows.size()==1?"run":"runs")+"  /  "+all.size()+" total",22,135);
        if(query.activeFilters()>0||!query.search.isEmpty()){
            clipped("Clear filters",470,135,88,ACCENT);region(466,130,92,17,()->{query=new ReplayQuery();changed();});
        }
        if(rows.isEmpty()){
            panel(20,150,538,286);text("No matching runs",40,245,TEXT);label("Try another tab or clear your search and filters.",40,264);
            button("Clear search and filters",40,292,190,()->{query=new ReplayQuery();changed();});
        }
        for(int index=page*PAGE_SIZE;index<Math.min(rows.size(),(page+1)*PAGE_SIZE);index++)card(index);
        int pages=Math.max(1,(rows.size()+PAGE_SIZE-1)/PAGE_SIZE);
        button("Previous",20,450,82,20,page>0,false,()->changePage(-1));
        button("Next",110,450,70,20,page+1<pages,false,()->changePage(1));
        label("Page "+(page+1)+" of "+pages,196,456);
        label("Right-click a card to favorite",344,456);
        details();
        footer(message.isEmpty()?"B: library  /  Mouse wheel: pages or performance details  /  Esc: close":message);
        if(filtersOpen)paintFilters();
    }
    private void card(int index){
        final int rowIndex=index;int slot=index-page*PAGE_SIZE,x=GRID_X+(slot%2)*(CARD_W+12),y=GRID_Y+(slot/2)*ROW;
        RunHistory.Run run=rows.get(index);boolean success="SUCCESS".equals(run.status),playable=BridgeTracker.history().hasReplay(run);
        rect(x,y,CARD_W,68,selected==index?0xFF25394D:over(x,y,CARD_W,68)?0xFF243245:PANEL);
        if(selected==index)rect(x,y,2,68,ACCENT);
        region(x,y,CARD_W,68,()->{selected=rowIndex;detailOffset=0;message="";});
        label("#"+run.id.substring(0,8),x+12,y+9);
        if(favorite(run))star(x+CARD_W-21,y+10);
        clipped(run.name().isEmpty()&&!playable?"Saved summary":run.displayName(),x+12,y+26,CARD_W-24,TEXT);
        rect(x+12,y+53,3,3,success?GREEN:RED);
        clipped((success?"Success":"Fail")+" / "+String.format(Locale.US,"%.3fs",run.time),x+21,y+50,CARD_W-104,success?GREEN:RED);
        button(playable?"Watch":"Summary",x+CARD_W-82,y+43,70,20,playable,false,()->play(rowIndex));
    }
    private void details(){
        panel(574,150,266,320);text("REPLAY DETAILS",590,165,TEXT);
        RunHistory.Run run=current();
        if(run==null){label("Select a run to inspect it.",590,204);button("PLAY REPLAY",590,442,234,28,false,true,()->{});return;}
        label("METADATA",590,188);clipped(run.displayName(),590,206,234,TEXT);
        label("ID",590,223);clipped(run.replayId(),616,223,208,MUTED);
        label(new SimpleDateFormat("MMM dd, yyyy  HH:mm:ss",Locale.US).format(new Date(run.created)),590,239);
        text(String.format(Locale.US,"%.3f seconds",run.time),590,255,TEXT);label(run.jumps+" jumps",748,255);
        clipped("Server: "+run.serverLabel(),590,270,234,MUTED);
        line(590,286,234);label("MAP INFO",590,297);
        BridgeGeometry.Result bridge=dimensions(run);
        text("Axis "+run.axis,590,315,TEXT);clipped(bridge.available?String.format(Locale.US,"Gap %.2fb",bridge.gap):"Gap N/A",674,315,150,TEXT);
        clipped(bridge.available?String.format(Locale.US,"Height delta %+.2fb",bridge.rise):bridge.reason,590,331,234,MUTED);
        line(590,346,234);label("PERFORMANCE BREAKDOWN",590,355);
        int count=Math.min(4,Math.max(0,run.details.size()-detailOffset));
        if(run.details.isEmpty())label("No jump samples in this run.",590,366);
        for(int i=0;i<count;i++)clipped(run.details.get(detailOffset+i).replaceAll("\u00a7.",""),590,365+i*12,222,TEXT);
        if(run.details.size()>4){
            rect(831,363,2,46,BORDER);int thumb=Math.max(5,46*4/run.details.size());
            rect(831,363+(46-thumb)*detailOffset/Math.max(1,run.details.size()-4),2,thumb,ACCENT);
        }
        button("Save",590,415,70,20,true,false,()->save(run));
        button("Rename",668,415,72,20,true,false,()->open(new BridgeRenameScreen(this,run)));
        boolean playable=BridgeTracker.history().hasReplay(run);
        button("Export",748,415,76,20,playable,false,()->export(run));
        button("PLAY REPLAY",590,442,234,28,playable,true,()->play(selected));
    }
    private void save(RunHistory.Run run){try{BridgeTracker.history().save(run);message="Replay saved.";}catch(IOException ex){message="Could not save this run.";}}
    private void export(RunHistory.Run run){try{Path file=BridgeTracker.history().exportReplay(run);message="Exported: "+file.getFileName()+" / Tools opens exports.";}catch(IOException ex){message="Export failed: "+ex.getMessage();}}
    private void openFiles(){try{message=BridgeFileActions.open(BridgeTracker.history().exportsFolder());}catch(IOException|RuntimeException ex){message="Could not open the exports folder.";}}
    private void changePage(int direction){int pages=Math.max(1,(rows.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(pages-1,page+direction));selected=page*PAGE_SIZE;detailOffset=0;}
    private int cardAt(int x,int y){
        if(x<GRID_X||x>=558||y<GRID_Y||y>=GRID_Y+ROW*4)return -1;
        int col=(x-GRID_X)/(CARD_W+12),row=(y-GRID_Y)/ROW;
        if((x-GRID_X)%(CARD_W+12)>=CARD_W||(y-GRID_Y)%ROW>=68)return -1;
        int index=page*PAGE_SIZE+row*2+col;return col<2&&index<rows.size()?index:-1;
    }
    @Override protected void rightClick(int x,int y){
        if(filtersOpen||!dropdown.isEmpty())return;int index=cardAt(x,y);if(index<0)return;
        RunHistory.Run run=rows.get(index);selected=index;
        try{boolean value=BridgeTracker.history().toggleFavorite(run);message=value?"Added to favorites and saved.":"Removed from favorites.";if(view==2)refresh();}
        catch(IOException ex){message="Could not update this favorite.";}
    }
    @Override protected void scroll(int x,int y,int direction){
        if(filtersOpen)return;
        if(x>=574){RunHistory.Run run=current();if(run!=null)detailOffset=Math.max(0,Math.min(Math.max(0,run.details.size()-4),detailOffset+direction));}
        else changePage(direction);
    }
    @Override protected void key(int key){if(key==200)scroll(600,370,-1);if(key==208)scroll(600,370,1);}
    @Override protected boolean escapeModal(){if(filtersOpen){filtersOpen=false;focus="";return true;}return false;}
    @Override protected void back(){query.store();BridgeConfig.save();super.back();}
    @Override public void m_47002234(){query.store();BridgeConfig.save();}
    private static ReplayQuery copy(ReplayQuery value){ReplayQuery c=new ReplayQuery();c.search=value.search;c.status=value.status;c.axis=value.axis;c.content=value.content;c.technique=value.technique;c.minimum=value.minimum;c.maximum=value.maximum;return c;}
    private void paintFilters(){
        rect(0,54,WIDTH,422,0xCC080E18);region(0,54,WIDTH,422,()->{filtersOpen=false;focus="";dropdown="";});
        panel(168,140,524,258);region(168,140,524,258,()->{});
        text("FILTER REPLAYS",184,157,TEXT);label("Combine filters to find a specific bridge.",184,175);
        label("Axis",184,198);label("Recording",326,198);label("Technique",506,198);
        dropdown("axis",draft.axis==ReplayQuery.Axis.ALL?"Any axis":draft.axis.name()+" axis",new String[]{"Any axis","X axis","Z axis"},draft.axis.ordinal(),184,213,130,i->draft.axis=ReplayQuery.Axis.values()[i]);
        String[] content={"All recordings","Playable replays","Summaries only"};
        dropdown("content",content[draft.content.ordinal()],content,draft.content.ordinal(),326,213,168,i->draft.content=ReplayQuery.Content.values()[i]);
        String[] techniques=new String[ReplayQuery.Technique.values().length];for(int i=0;i<techniques.length;i++)techniques[i]=ReplayQuery.Technique.values()[i].label;
        dropdown("technique",draft.technique.label,techniques,draft.technique.ordinal(),506,213,170,i->draft.technique=ReplayQuery.Technique.values()[i]);
        label("Minimum run time",184,257);label("Maximum run time",438,257);
        input("minimum",draft.minimum,"No minimum (seconds)",184,273,238,9,value->draft.minimum=value);
        input("maximum",draft.maximum,"No maximum (seconds)",438,273,238,9,value->draft.maximum=value);
        String error=draft.error();clipped(error==null?"Time limits are inclusive. Empty fields mean no limit.":error,184,314,492,error==null?MUTED:RED);
        button("Reset",184,354,96,()->{String search=draft.search;draft=new ReplayQuery();draft.search=search;});
        button("Cancel",440,354,104,()->{filtersOpen=false;focus="";});
        button("Apply filters",556,354,120,26,error==null,true,()->{query=draft;filtersOpen=false;focus="";changed();});
    }
}
