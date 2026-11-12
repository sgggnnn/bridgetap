package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.C_85740840;

/** Drag-and-drop HUD studio; stored positions are actual Minecraft GUI pixels. */
public final class BridgeLayoutScreen extends BridgeScreen {
    private final C_85740840 parent;
    private int selected;private boolean dragging,resizing,snap=true,inspector=true;
    private int resizeX,resizeY,resizeWidth,resizeHeight;private double resizeScale;
    private float sceneScale;private int sceneX,sceneY,gameWidth,gameHeight;
    private int dragX,dragY;
    private final int[][] bounds=new int[3][4];
    public BridgeLayoutScreen(C_85740840 parent){this.parent=parent;selected=ReplaySession.active()?2:0;}
    @Override protected int canvasWidth(){return f_05230747>0?f_05230747:WIDTH;}
    @Override protected int canvasHeight(){return f_07021018>0?f_07021018:HEIGHT;}
    @Override protected float scale(){return 1;}
    @Override protected int originX(float value){return 0;}
    @Override protected int originY(float value){return 0;}
    @Override protected int backgroundColor(){return 0x60101722;}
    @Override protected int backdropColor(){return 0x50080D15;}
    private double valueScale(){return selected==0?BridgeConfig.hudScale:selected==1?BridgeConfig.replayHudScale:BridgeConfig.inputHudScale;}
    private void scaleValue(double value){value=Math.max(.5,Math.min(2,Math.round(value*10)/10.0));if(selected==0)BridgeConfig.hudScale=value;else if(selected==1)BridgeConfig.replayHudScale=value;else BridgeConfig.inputHudScale=value;}
    private void position(int x,int y){if(selected==0){BridgeConfig.hudX=x;BridgeConfig.hudY=y;}else if(selected==1){BridgeConfig.replayHudX=x;BridgeConfig.replayHudY=y;}else{BridgeConfig.inputHudX=x;BridgeConfig.inputHudY=y;}}
    @Override protected void paint(){
        gameWidth=canvasWidth();gameHeight=canvasHeight();sceneScale=1;sceneX=sceneY=0;
        if(snap){rect(gameWidth/2,0,1,gameHeight,0x553C6374);rect(0,gameHeight/2,gameWidth,1,0x553C6374);}
        if(selected!=0)for(int i=1;i<3;i++)if(i!=selected)overlay(i);
        overlay(selected);
        if(inspector){
            float size=Math.min(1f,(gameHeight-24f)/440f);int x=gameWidth-12-Math.round(224*size);
            controlsAt(x,12,size,this::inspector);
            label("HUD layout / Tab: hide controls",10,gameHeight-13);
        }else label("Tab: show controls / Esc: save and return",10,gameHeight-13);
    }
    private void inspector(){
        panel(0,0,224,440);region(0,0,224,440,()->{});text("HUD LAYOUT",16,16,TEXT);
        String[] names={"Bridge coordinates","Replay information","Recorded inputs"};
        for(int i=0;i<3;i++){final int index=i;button(names[i],16,43+i*30,192,24,true,selected==i,()->{selected=index;dragging=false;});}
        boolean enabled=selected==0?BridgeConfig.enabled:selected==1?BridgeConfig.showReplayInfo:BridgeConfig.showReplayInputs;
        toggle("Visible",enabled,16,142,192,()->{if(selected==0)BridgeConfig.enabled=!BridgeConfig.enabled;else if(selected==1)BridgeConfig.showReplayInfo=!BridgeConfig.showReplayInfo;else BridgeConfig.showReplayInputs=!BridgeConfig.showReplayInputs;});
        stepper("Scale",String.format(Locale.US,"%.1fx",valueScale()),16,181,192,()->scaleValue(valueScale()-.1),()->scaleValue(valueScale()+.1));
        label("Position "+bounds[selected][0]+", "+bounds[selected][1],16,219);
        toggle("Snap to guides",snap,16,240,192,()->snap=!snap);
        button("Center horizontally",16,279,192,()->{int[] b=bounds[selected];position((gameWidth-b[2])/2,b[1]);BridgeConfig.save();});
        button("Reset position",16,313,192,()->{if(selected==1){BridgeConfig.replayHudX=BridgeConfig.replayHudY=-1;}else position(10,10);BridgeConfig.save();});
        button("Hide controls (Tab)",16,347,192,()->inspector=false);
        button("Save and return",16,393,192,26,true,true,this::back);
    }
    private List<String> lines(int type){
        if(type==1)return Arrays.asList("Bridge replay 1.25 / 3.85s | Tick 25/77 | 1.00x | Pause","Ghost XYZ: 1.4366  66.1661  8.9902","Inputs: Back Left Jump RMB","Pressed: RMB | Released: Right","Clicks L/R/M: 0/1/0 | CPS: 0/19/0","Velocity X -0.0003  Y 0.0831  Z 0.2874","Platform gap: 28.00b | Height: +4.00b","Sticks: 1 tick | Left-click player: POV");
        List<JumpEntry> entries=Arrays.asList(new JumpEntry("1.0 Flat",.504,.504,Double.NaN,3,JumpEntry.EntryType.FLAT),new JumpEntry("5b Late",.460,.460,-.044,9,JumpEntry.EntryType.TAP),new JumpEntry("Turn",.216,.216,.475,15,JumpEntry.EntryType.TURN));
        List<String> result=new ArrayList<>();
        if(BridgeConfig.displayMode.equals("ALL")||BridgeConfig.displayMode.equals("TIME"))result.add(OverlayStyle.color(7)+"Success 3.85s / 3 jumps / Axis Z");
        if(!BridgeConfig.displayMode.equals("TIME"))result.addAll(EntryFormatter.hudLines(entries));
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
        if(type==2)ReplayInputOverlay.paint(target,0,0,ReplaySession.active()?ReplaySession.recordedInputs():new ReplayData.TickStats(1|4|16|256,0,0,0,1,0,0,19,0,0,.08,.28));
        else{
            if(type==0&&BridgeConfig.backgroundEnabled)target.rect(0,0,naturalWidth,naturalHeight,OverlayStyle.background(0));
            for(int i=0;i<lines.size();i++)target.text(OverlayStyle.styled(type,lines.get(i)),4,4+i*11,TEXT);
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
    @Override protected void key(int key){if(key==15){inspector=!inspector;return;}int[] b=bounds[selected];int x=b[0],y=b[1];if(key==203)x--;else if(key==205)x++;else if(key==200)y--;else if(key==208)y++;else return;position(OverlayPlacement.clamp(x,b[2],gameWidth),OverlayPlacement.clamp(y,b[3],gameHeight));BridgeConfig.save();}
    @Override protected void back(){dragging=resizing=false;BridgeConfig.save();open(parent);}
}
