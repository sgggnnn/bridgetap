package com.bridger.momentum;

import net.minecraft.unmapped.C_85740840;

public final class ClipScreen extends BridgeScreen {
    private final C_85740840 parent;private String error="";
    public ClipScreen(C_85740840 parent){this.parent=parent;}
    @Override protected void paint(){
        header("Video clip buffer","High-quality MP4 / Windows output audio");button("Back",752,15,88,this::back);
        panel(20,74,820,375);text("RECORD RECENT GAMEPLAY",40,95,TEXT);
        label("Enable before playing. F9 saves the recent buffer without stopping recording.",40,119);
        label("System audio includes other apps. Optional microphone uses your default input.",40,139);
        toggle("Video buffer",ClipCapture.enabled(),40,169,230,ClipCapture::toggle);
        toggle("System audio",ClipCapture.sound(),300,169,230,ClipCapture::toggleSound);
        toggle("Microphone",ClipCapture.microphone(),560,169,258,ClipCapture::toggleMicrophone);
        dropdown("duration","Buffer: "+ClipCapture.seconds()+" seconds",new String[]{"15 seconds","30 seconds","60 seconds"},ClipCapture.seconds()==15?0:ClipCapture.seconds()==30?1:2,40,220,230,i->ClipCapture.duration(new int[]{15,30,60}[i]));
        int[] rates={10,15,20,30,60,120};int selected=java.util.Arrays.binarySearch(rates,ClipCapture.fps());
        dropdown("fps","Capture: "+ClipCapture.fps()+" FPS",new String[]{"10 FPS","15 FPS","20 FPS","30 FPS","60 FPS","120 FPS"},selected,286,220,230,i->ClipCapture.rate(rates[i]));
        dropdown("quality",ClipCapture.height()+"p / High quality",new String[]{"360p / Compact","720p / High quality","1080p / High quality"},ClipCapture.height()==360?0:ClipCapture.height()==720?1:2,532,220,286,i->ClipCapture.quality(new int[]{360,720,1080}[i]));
        button(ClipCapture.saving()?"Export in progress...":"Save recent video (F9)",40,274,300,32,!ClipCapture.saving(),true,ClipCapture::save);
        button("Open clips folder",354,274,220,32,true,false,()->{try{error=BridgeFileActions.open(ClipCapture.folder());}catch(Exception ex){error="Could not open clips folder.";}});
        button("Clear buffer",588,274,230,32,true,false,ClipCapture::clear);
        text(ClipCapture.stats(),40,331,TEXT);clipped(ClipCapture.message(),40,352,770,MUTED);clipped(ClipAudio.status()+" / "+ClipMicrophone.status(),40,372,770,MUTED);
        clipped(ClipCapture.backend(),40,389,778,MUTED);
        clipped(ClipCapture.storage(),40,425,778,MUTED);
        label("Changing settings, resizing or leaving a world clears buffered video.",40,409);
        footer(error.isEmpty()?"Buffer starts OFF each launch / MP4 export runs in the background / F9 can be rebound":error);
    }
    @Override protected void back(){open(parent);}
}
