package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.C_85740840;

/** Drag-and-drop HUD studio; stored positions are actual Minecraft GUI pixels. */
public final class BridgeLayoutScreen extends BridgeScreen {
    private final C_85740840 parent;
    private int selected;private boolean dragging,resizing,snap=true;
    private int resizeX,resizeY,resizeWidth,resizeHeight;private double resizeScale;
    private float sceneScale;private int sceneX,sceneY,gameWidth,gameHeight;
    private int dragX,dragY;
    private final int[][] bounds=new int[3][4];
    public BridgeLayoutScreen(C_85740840 parent){this.parent=parent;}
    @Override protected int backgroundColor(){return 0x60101722;}
    @Override protected int backdropColor(){return 0x50080D15;}
    private double valueScale(){return selected==0?BridgeConfig.hudScale:selected==1?BridgeConfig.replayHudScale:BridgeConfig.inputHudScale;}
    private void scaleValue(double value){value=Math.max(.5,Math.min(2,Math.round(value*10)/10.0));if(selected==0)BridgeConfig.hudScale=value;else if(selected==1)BridgeConfig.replayHudScale=value;else BridgeConfig.inputHudScale=value;}
    private void position(int x,int y){if(selected==0){BridgeConfig.hudX=x;BridgeConfig.hudY=y;}else if(selected==1){BridgeConfig.replayHudX=x;BridgeConfig.replayHudY=y;}else{BridgeConfig.inputHudX=x;BridgeConfig.inputHudY=y;}}
    @Override protected void paint(){
        header("HUD layout studio","Drag overlays directly into position");
        // The letterboxed scene represents the whole current game viewport.
        gameWidth=f_05230747>0?f_05230747:427;gameHeight=f_07021018>0?f_07021018:240;
        sceneScale=Math.min(574f/gameWidth,358f/gameHeight);
        sceneX=20+(574-Math.round(gameWidth*sceneScale))/2;sceneY=74+(358-Math.round(gameHeight*sceneScale))/2;
        rect(sceneX,sceneY,Math.round(gameWidth*sceneScale),Math.round(gameHeight*sceneScale),0x50101722);
        rect(sceneX+Math.round(gameWidth*sceneScale/2),sceneY,1,Math.round(gameHeight*sceneScale),0x553C6374);
        rect(sceneX,sceneY+Math.round(gameHeight*sceneScale/2),Math.round(gameWidth*sceneScale),1,0x553C6374);
        label("GAME VIEWPORT",22,61);
        if(selected!=0)for(int i=1;i<3;i++)if(i!=selected)overlay(i);
        overlay(selected);
        panel(616,72,224,358);text("SELECTED OVERLAY",632,88,TEXT);
        String[] names={"Bridge coordinates","Replay information","Recorded inputs"};
        dropdown("overlay",names[selected],names,selected,632,108,192,i->{selected=i;dragging=false;});
        label(selected==0?"Visible during normal gameplay":"Visible during replays only",632,147);
        boolean enabled=selected==0?BridgeConfig.enabled:selected==1?BridgeConfig.showReplayInfo:BridgeConfig.showReplayInputs;
        toggle("Visible",enabled,632,167,192,()->{if(selected==0)BridgeConfig.enabled=!BridgeConfig.enabled;else if(selected==1)BridgeConfig.showReplayInfo=!BridgeConfig.showReplayInfo;else BridgeConfig.showReplayInputs=!BridgeConfig.showReplayInputs;});
        line(632,205,192);
        stepper("Scale",String.format(Locale.US,"%.1fx",valueScale()),632,218,192,()->scaleValue(valueScale()-.1),()->scaleValue(valueScale()+.1));
        label("Position  "+bounds[selected][0]+", "+bounds[selected][1],632,257);
        toggle("Snap to guides",snap,632,276,192,()->snap=!snap);
        button("Center horizontally",632,313,192,()->{int[] b=bounds[selected];position((gameWidth-b[2])/2,b[1]);BridgeConfig.save();});
        button("Reset position",632,347,192,()->{if(selected==1){BridgeConfig.replayHudX=BridgeConfig.replayHudY=-1;}else position(10,10);BridgeConfig.save();});
        button("Overlay colors",632,381,192,()->open(new BridgeColorsScreen(this)));
        label("Drag to move. Drag the lower-right handle to resize.",22,450);
        button("Save and return",700,442,140,26,true,true,this::back);
        footer((selected==0?"Preview: normal gameplay":"Preview: replay overlays")+"  /  Arrow keys: nudge  /  Esc: save and return");
    }
    private List<String> lines(int type){
        if(type==1)return Arrays.asList("Bridge replay 1.25 / 3.85s | Tick 25/77 | 1.00x | Pause","Ghost XYZ: 1.4366  66.1661  8.9902","Inputs: Back Left Jump RMB","Pressed: RMB | Released: Right","Clicks L/R/M: 0/1/0 | CPS: 0/19/0","Velocity X -0.0003  Y 0.0831  Z 0.2874","Platform gap: 28.00b | Height: +4.00b","Sticks: 1 tick | Left-click player: POV");
        List<JumpEntry> entries=Arrays.asList(new JumpEntry("1.0 Flat",.504,.504,Double.NaN,3,JumpEntry.EntryType.FLAT),new JumpEntry("5b Late",.460,.460,-.044,9,JumpEntry.EntryType.TAP),new JumpEntry("Turn",.216,.216,.475,15,JumpEntry.EntryType.TURN));
        List<String> result=new ArrayList<>();
        if(BridgeConfig.displayMode.equals("ALL")||BridgeConfig.displayMode.equals("TIME"))result.add(BridgeConfig.color(7)+"Success 3.85s / 3 jumps / Axis Z");
        if(!BridgeConfig.displayMode.equals("TIME"))result.addAll(EntryFormatter.lines(entries));
        return result;
    }
    private void overlay(int type){
        List<String> lines=type==2?Collections.emptyList():lines(type);
        int naturalWidth=type==2?76:0,naturalHeight=type==2?92:lines.size()*11+8;
        for(String line:lines)naturalWidth=Math.max(naturalWidth,ui.width(line)+8);
        double configuredScale=type==0?BridgeConfig.hudScale:type==1?BridgeConfig.replayHudScale:BridgeConfig.inputHudScale;
        float scale=(float)Math.min(configuredScale,Math.min((gameWidth-8.0)/naturalWidth,(gameHeight-8.0)/naturalHeight));
        int width=(int)Math.ceil(naturalWidth*scale),height=(int)Math.ceil(naturalHeight*scale);
        int x=type==0?BridgeConfig.hudX:type==1?BridgeConfig.replayHudX:BridgeConfig.inputHudX;
        int y=type==0?BridgeConfig.hudY:type==1?BridgeConfig.replayHudY:BridgeConfig.inputHudY;
        if(type==1){if(x<0)x=(gameWidth-width)/2;if(y<0)y=gameHeight-88-height;}
        x=OverlayPlacement.clamp(x,width,gameWidth);y=OverlayPlacement.clamp(y,height,gameHeight);bounds[type]=new int[]{x,y,width,height};
        int sx=sceneX+Math.round(x*sceneScale),sy=sceneY+Math.round(y*sceneScale),sw=Math.round(width*sceneScale),sh=Math.round(height*sceneScale);
        UiCanvas target=new ScaledUiCanvas(ui,sx,sy,scale*sceneScale);
        if(type==2)ReplayInputOverlay.paint(target,0,0,new ReplayData.TickStats(1|4|16|256,0,0,0,1,0,0,19,0,0,.08,.28));
        else{
            if(type==0&&BridgeConfig.backgroundEnabled)target.rect(0,0,naturalWidth,naturalHeight,0xB0000000|BridgeConfig.rgb(8));
            for(int i=0;i<lines.size();i++)target.text(lines.get(i),4,4+i*11,TEXT);
        }
        int color=selected==type?ACCENT:0x887C91AB;
        rect(sx-2,sy-2,sw+4,1,color);rect(sx-2,sy+sh+1,sw+4,1,color);rect(sx-2,sy-2,1,sh+4,color);rect(sx+sw+1,sy-2,1,sh+4,color);
        if(selected==type){for(int dx:new int[]{-3,sw-1})for(int dy:new int[]{-3,sh-1})rect(sx+dx,sy+dy,4,4,ACCENT);}
        final int index=type;
        region(sx-4,sy-4,sw+8,sh+8,()->{selected=index;dragging=true;dragX=Math.round((mouseX-sceneX)/sceneScale)-bounds[index][0];dragY=Math.round((mouseY-sceneY)/sceneScale)-bounds[index][1];});
        if(selected==type)region(sx+sw-5,sy+sh-5,10,10,()->{resizing=true;dragging=false;resizeX=mouseX;resizeY=mouseY;resizeWidth=sw;resizeHeight=sh;resizeScale=valueScale();});
    }
    @Override public void dragPreview(int x,int y){
        super.dragPreview(x,y);
        if(resizing){scaleValue(resizeScale*(1+(x-resizeX+y-resizeY)/(double)(resizeWidth+resizeHeight)));return;}
        if(!dragging)return;int[] b=bounds[selected];
        int px=Math.round((x-sceneX)/sceneScale)-dragX,py=Math.round((y-sceneY)/sceneScale)-dragY;
        position(OverlayPlacement.snap(px,b[2],gameWidth,snap),OverlayPlacement.snap(py,b[3],gameHeight,snap));
    }
    @Override public void releasePreview(){dragging=resizing=false;BridgeConfig.save();}
    @Override protected void key(int key){int[] b=bounds[selected];int x=b[0],y=b[1];if(key==203)x--;else if(key==205)x++;else if(key==200)y--;else if(key==208)y++;else return;position(OverlayPlacement.clamp(x,b[2],gameWidth),OverlayPlacement.clamp(y,b[3],gameHeight));BridgeConfig.save();}
    @Override protected void back(){dragging=resizing=false;BridgeConfig.save();open(parent);}
}
