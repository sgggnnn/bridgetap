package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.C_85740840;

public final class ReplayFailureScreen extends BridgeScreen {
    private final C_85740840 parent;private final RunHistory.Run run;private final ReplayData data;private final List<ReplayFailureAnalysis.Finding> findings;private int offset;private String message="Observed events are clues, not proof of the cause of a fail.";
    public ReplayFailureScreen(C_85740840 parent,RunHistory.Run run)throws java.io.IOException{this.parent=parent;this.run=run;data=BridgeTracker.history().replay(run);findings=ReplayFailureAnalysis.analyze(run.status,run.axis,data);}
    @Override protected void paint(){header("Fail analysis","Recorded evidence around the end of the run");button("Back",752,15,88,this::back);panel(20,74,820,372);clipped(run.displayName(),38,94,770,TEXT);label("Click a tick to inspect the recorded scene while paused.",38,125);
        for(int i=offset;i<Math.min(findings.size(),offset+7);i++){ReplayFailureAnalysis.Finding finding=findings.get(i);int y=157+(i-offset)*37;button("Tick "+finding.tick,38,y,88,26,data!=null,false,()->{try{if(BridgeTracker.isBridging())throw new IllegalArgumentException("Finish the current bridge first");if(ReplaySession.start(run,data))ReplaySession.inspectTick(finding.tick);}catch(Exception ex){message=ex.getMessage();}});clipped(finding.text,142,y+9,678,TEXT);}
        button("Previous",38,415,102,24,offset>0,false,()->offset=Math.max(0,offset-7));button("Next",152,415,102,24,offset+7<findings.size(),false,()->offset+=7);footer(message);
    }
    @Override protected void back(){open(parent);}
}
