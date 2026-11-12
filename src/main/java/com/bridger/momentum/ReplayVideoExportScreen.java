package com.bridger.momentum;

import net.minecraft.unmapped.C_85740840;

public final class ReplayVideoExportScreen extends BridgeScreen {
    private final C_85740840 parent;private final RunHistory.Run run;private int fps,height,camera;private boolean inputs,title,metrics,cleanHud,hotbar,ending;private String message="Keystrokes use recorded inputs and your saved replay HUD position.";
    public ReplayVideoExportScreen(C_85740840 parent,RunHistory.Run run){this.parent=parent;this.run=run;ReplayVideoOptions saved=ReplayVideoOptions.load(ReplayVideoOptions.path());fps=saved.fps;height=saved.height;camera=saved.camera;inputs=saved.inputs;title=saved.title;metrics=saved.metrics;cleanHud=saved.cleanHud;hotbar=saved.hotbar;ending=saved.ending;}
    private ReplayVideoOptions options(){return new ReplayVideoOptions(fps,height,camera,inputs,title,metrics,cleanHud,hotbar,ending);}
    private void store(){try{options().save(ReplayVideoOptions.path());}catch(Exception error){message="Could not save export preferences.";}}
    public void result(String message){this.message=message;}
    @Override protected void paint(){
        header("Export replay video","Recorded keystrokes / Clean presentation");button("Back",752,15,88,this::back);
        panel(20,74,820,374);clipped(run==null?"Select a replay first":run.displayName(),40,95,770,TEXT);
        label("Full replay / Silent MP4 / Progress stays outside the exported video",40,125);
        label("The live clip buffer is cleared and suspended while exporting.",40,146);
        String[] cameras={"Player POV","Follow camera","Fixed overview"};
        dropdown("camera","Camera: "+cameras[camera],cameras,camera,40,170,360,i->camera=i);
        dropdown("fps","Video: "+fps+" FPS",new String[]{"30 FPS","60 FPS","120 FPS"},fps==30?0:fps==60?1:2,430,170,388,i->fps=new int[]{30,60,120}[i]);
        dropdown("height","Up to "+height+"p / High quality",new String[]{"360p","720p","1080p"},height==360?0:height==720?1:2,40,218,360,i->height=new int[]{360,720,1080}[i]);
        toggle("Recorded keystrokes + CPS",inputs,430,218,388,()->inputs=!inputs);
        toggle("Run title and timer",title,40,266,360,()->title=!title);
        toggle("Coordinates and velocity",metrics,430,266,388,()->metrics=!metrics);
        toggle("Hide Minecraft HUD",cleanHud,40,302,360,()->cleanHud=!cleanHud);
        toggle("Block stack hotbar",hotbar,430,302,388,()->hotbar=!hotbar);
        toggle("Final time (2 seconds)",ending,40,336,360,()->ending=!ending);
        button("EXPORT MP4",40,378,360,34,run!=null&&!ReplayVideoExport.active(),true,()->{try{if(BridgeTracker.isBridging())throw new IllegalArgumentException("Finish the current bridge first");ReplayData data=BridgeTracker.history().replay(run);if(data==null)throw new IllegalArgumentException("This replay contains statistics only");store();ReplayVideoExport.start(run,data,options(),this);}catch(Exception ex){message=ex.getMessage();}});
        button("Open exported videos",430,378,388,34,true,false,()->{try{message=BridgeFileActions.open(ReplayVideoExport.folder());}catch(Exception ex){message=ex.getMessage();}});
        label("Keep the window active. Esc cancels; replay, camera and HUD settings are restored.",40,425);footer(message);
    }
    @Override protected void back(){store();open(parent);}
}
