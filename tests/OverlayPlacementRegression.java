import com.bridger.momentum.*;

public class OverlayPlacementRegression {
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    static final class Canvas implements UiCanvas {
        int held;String text="";
        public void rect(int x,int y,int w,int h,int color){if(color==0xE0275665)held++;}
        public void text(String s,int x,int y,int color){text+=s;}
        public int width(String s){return s.length()*6;}
        public void textScaled(String s,int x,int y,int color,float scale){text(s,x,y,color);}
    }
    public static void main(String[] args){
        check(OverlayPlacement.clamp(-30,76,427)==0,"Drag clamps to left/top");
        check(OverlayPlacement.clamp(500,76,427)==351,"Drag clamps to right/bottom");
        check(OverlayPlacement.snap(174,76,427,true)==175,"Snap to center guide");
        check(OverlayPlacement.snap(348,76,427,true)==351,"Snap to far edge");
        check(OverlayPlacement.snap(348,76,427,false)==348,"Snapping is optional");
        Canvas c=new Canvas();ReplayInputOverlay.paint(c,0,0,new ReplayData.TickStats(1|4|16|256,0,0,0,0,0,0,0,0,0,0,0));
        check(c.held==4&&c.text.contains("LMB")&&c.text.contains("RMB"),"Recorded movement, jump and mouse bits highlight the correct input boxes");
        Canvas paused=new Canvas();ReplayInputOverlay.paint(paused,0,0,new ReplayData.TickStats(0,0,0,0,0,0,0,0,0,0,0,0));check(paused.held==0,"No unrelated spectator inputs are highlighted");
        Canvas quick=new Canvas();ReplayInputOverlay.paint(quick,0,0,new ReplayData.TickStats(0,128,128,1,0,0,1,0,0,0,0,0));check(quick.held==1,"Short recorded clicks remain visible even when released within the same tick");
        Canvas legacy=new Canvas();ReplayInputOverlay.paint(legacy,0,0,null);check(legacy.text.contains("No input data"),"Old recordings do not invent inputs");
        BridgeConfig.inputHudX=81;BridgeConfig.inputHudY=72;BridgeConfig.inputHudScale=1.5;BridgeConfig.replayHudX=12;BridgeConfig.replayHudY=54;BridgeConfig.showReplayInputs=false;BridgeConfig.save();
        BridgeConfig.inputHudX=0;BridgeConfig.replayHudY=0;BridgeConfig.showReplayInputs=true;BridgeConfig.load();
        check(BridgeConfig.inputHudX==81&&BridgeConfig.inputHudY==72&&BridgeConfig.inputHudScale==1.5&&BridgeConfig.replayHudY==54&&!BridgeConfig.showReplayInputs,"HUD studio positions, scale and visibility persist");
        System.out.println("PASS: bounded/snap HUD placement, recorded input display, legacy data and layout persistence");
    }
}
