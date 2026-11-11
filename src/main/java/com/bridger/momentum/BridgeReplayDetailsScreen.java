package com.bridger.momentum;

import java.util.*;
import java.text.SimpleDateFormat;
import net.minecraft.unmapped.C_85740840;

/** Expanded, scrollable original bridge samples, available without starting playback. */
public final class BridgeReplayDetailsScreen extends BridgeScreen {
    private final C_85740840 parent;private final RunHistory.Run run;private int offset;
    public BridgeReplayDetailsScreen(C_85740840 parent,RunHistory.Run run){this.parent=parent;this.run=run;}
    @Override protected void paint(){
        header("Replay details","Bridge coordinates and jump breakdown");button("Back",752,15,88,this::back);
        panel(20,74,820,104);clipped(run.displayName(),38,92,780,TEXT);clipped("Server: "+run.serverLabel(),38,116,780,MUTED);
        label(String.format(Locale.US,"%.3fs / %d jumps / Axis %s",run.time,run.jumps,run.axis),38,143);label(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date(run.created)),540,143);
        panel(20,194,820,256);text("BRIDGE COORDINATES",38,211,ACCENT);
        if(run.details.isEmpty())label("No bridge coordinate samples were saved for this run.",38,250);
        for(int i=0;i<10&&offset+i<run.details.size();i++)clipped(run.details.get(offset+i),38,240+i*19,770,TEXT);
        button("Previous",38,460,102,24,offset>0,false,()->scroll(0,0,-10));button("Next",152,460,102,24,offset+10<run.details.size(),false,()->scroll(0,0,10));label((Math.min(offset+1,run.details.size()))+" - "+Math.min(offset+10,run.details.size())+" / "+run.details.size(),290,468);
    }
    @Override protected void scroll(int x,int y,int direction){offset=Math.max(0,Math.min(Math.max(0,run.details.size()-10),offset+direction));}
    @Override protected void back(){open(parent);}
}
