package com.bridger.momentum;

import java.util.*;
import java.text.SimpleDateFormat;
import net.minecraft.unmapped.C_85740840;

/** Expanded, scrollable original bridge samples, available without starting playback. */
public final class BridgeReplayDetailsScreen extends BridgeGlassScreen {
    private final C_85740840 parent;private final RunHistory.Run run;private int offset;private String message="";
    public BridgeReplayDetailsScreen(C_85740840 parent,RunHistory.Run run){this.parent=parent;this.run=run;BridgeTracker.history().indexModes(java.util.Collections.singletonList(run));}
    @Override protected void paint(){
        windowHeader("Replay details");button("Export video",700,25,160,32,true,false,()->open(new ReplayVideoExportScreen(this,run)));button("Analyze fail",872,25,140,32,true,false,()->{try{open(new ReplayFailureScreen(this,run));}catch(Exception ex){message="Could not analyze this recording.";}});button("Back",1024,25,64,32,true,false,this::back);
        clipped(run.displayName(),32,106,640,TEXT);clipped(run.serverLabel(),32,134,640,MUTED);
        ui.strongText(String.format(Locale.US,"%.3fs",run.time),32,168,TEXT,1.5f);
        label(run.jumps+" jumps / Axis "+run.axis+" / "+(run.modeReady()?run.mode().label:"Detecting..."),200,174);
        label(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(run.created)),840,134);
        rect(32,214,1056,1,UiTheme.alpha(TEXT,.06));text("BRIDGE COORDINATES",32,232,UiTheme.alpha(TEXT,.5));
        if(run.details.isEmpty())label("No bridge coordinate samples were saved for this run.",32,280);
        for(int i=0;i<10&&offset+i<run.details.size();i++)clipped(run.details.get(offset+i),32,268+i*24,1056,TEXT);
        button("Previous",32,550,106,32,offset>0,false,()->scroll(0,0,-10));button("Next",150,550,106,32,offset+10<run.details.size(),false,()->scroll(0,0,10));label(Math.min(offset+1,run.details.size())+" - "+Math.min(offset+10,run.details.size())+" / "+run.details.size(),280,560);
        button("Export file",712,550,164,32,true,false,()->{try{BridgeTracker.history().exportReplay(run);message=BridgeFileActions.open(BridgeTracker.history().exportsFolder());}catch(Exception ex){message="Could not export this replay file.";}});
        button("Copy share code",888,550,200,32,true,false,()->open(new ReplayShareScreen(this,run)));
        windowStatus(message.isEmpty()?"Scroll to review jump samples / Your recording stays unchanged":message);
    }
    @Override protected void scroll(int x,int y,int direction){offset=Math.max(0,Math.min(Math.max(0,run.details.size()-10),offset+direction));}
    @Override protected void back(){open(parent);}
}
