package com.bridger.momentum;

import net.minecraft.unmapped.C_85740840;
import java.util.Locale;

/** Categorized settings with consistent toggles and numeric steppers. */
public class BridgeSettingsScreen extends BridgeScreen {
    private final C_85740840 parent;
    public BridgeSettingsScreen(){this(null);}
    public BridgeSettingsScreen(C_85740840 parent){this.parent=parent;}
    @Override protected void paint(){
        header("Settings","Make BridgeTap work your way");
        panel(20,72,400,358);panel(438,72,402,230);panel(438,310,402,120);
        text("HUD & POSITIONING",36,88,TEXT);label("Your regular bridge coordinates display",36,103);
        toggle("HUD enabled",BridgeConfig.enabled,36,122,368,()->BridgeConfig.enabled=!BridgeConfig.enabled);
        toggle("Hide while F3 is open",BridgeConfig.hideOnF3,36,154,368,()->BridgeConfig.hideOnF3=!BridgeConfig.hideOnF3);
        line(36,187,368);
        button("Edit HUD layout",36,199,368,30,true,true,()->open(new BridgeLayoutScreen(this)));
        label("Drag coordinates, replay info and recorded inputs.",36,240);
        stepper("Scale",String.format(Locale.US,"%.1fx",BridgeConfig.hudScale),36,263,368,
            ()->BridgeConfig.hudScale=Math.max(.5,Math.round((BridgeConfig.hudScale-.1)*10)/10.0),
            ()->BridgeConfig.hudScale=Math.min(2,Math.round((BridgeConfig.hudScale+.1)*10)/10.0));
        toggle("Text shadow",BridgeConfig.textShadow,36,301,368,()->BridgeConfig.textShadow=!BridgeConfig.textShadow);
        toggle("Background",BridgeConfig.backgroundEnabled,36,333,368,()->BridgeConfig.backgroundEnabled=!BridgeConfig.backgroundEnabled);
        label("Display mode",36,375);
        String[] modes={"ALL","COORDS","COORDS_ONLY","LAST_TURN","TICKS","TIME"};
        String[] modeLabels={"All statistics","Coordinates with labels","Coordinates only","Last + Turn coordinates","Ticks only","Run time only"};
        int mode=java.util.Arrays.asList(modes).indexOf(BridgeConfig.displayMode);
        dropdown("display",modeLabels[Math.max(0,mode)],modeLabels,mode,188,366,216,i->{BridgeConfig.displayMode=modes[i];BridgeConfig.save();});

        text("DETECTION & CALCULATIONS",454,88,TEXT);
        button("Reset offsets",718,82,106,22,true,false,()->{BridgeConfig.tickOffset=BridgeConfig.plateTickOffset=0;BridgeConfig.save();});
        stepper("Jump offset",BridgeConfig.tickOffset+" ticks",454,119,370,()->BridgeConfig.tickOffset=Math.max(-40,BridgeConfig.tickOffset-1),()->BridgeConfig.tickOffset=Math.min(40,BridgeConfig.tickOffset+1));
        stepper("Plate offset",BridgeConfig.plateTickOffset+" ticks",454,151,370,()->BridgeConfig.plateTickOffset=Math.max(-40,BridgeConfig.plateTickOffset-1),()->BridgeConfig.plateTickOffset=Math.min(40,BridgeConfig.plateTickOffset+1));
        stepper("Turn detection",BridgeConfig.turnTicks+" ticks",454,183,370,()->BridgeConfig.turnTicks=12,()->BridgeConfig.turnTicks=15);
        toggle("Show Gain / No Gain",BridgeConfig.showGainType,454,217,370,()->BridgeConfig.showGainType=!BridgeConfig.showGainType);
        label("Indicator label",454,264);
        dropdown("gain",BridgeConfig.useNoGayLabel?"No Gay":"No Gain",new String[]{"No Gain","No Gay"},BridgeConfig.useNoGayLabel?1:0,654,255,170,i->{BridgeConfig.useNoGayLabel=i==1;BridgeConfig.save();});

        text("OVERLAY & REPLAY",454,324,TEXT);
        toggle("Replay information",BridgeConfig.showReplayInfo,454,340,370,()->BridgeConfig.showReplayInfo=!BridgeConfig.showReplayInfo);
        toggle("Recorded input overlay",BridgeConfig.showReplayInputs,454,368,370,()->BridgeConfig.showReplayInputs=!BridgeConfig.showReplayInputs);
        dropdown("output",BridgeConfig.chatOutput?"Output: Chat":"Output: Overlay",new String[]{"Overlay","Chat at end of run"},BridgeConfig.chatOutput?1:0,454,398,174,i->{BridgeConfig.chatOutput=i==1;BridgeConfig.save();});
        button("Customize colors",640,398,184,()->open(new BridgeColorsScreen(this)));
        label("Default offsets: 0 ticks / New-run delay: 5 ticks",22,450);
        button("Back to replays",706,442,134,26,true,true,this::back);
        footer("Changes are saved automatically  /  Esc: back");
    }
    @Override protected void back(){BridgeConfig.save();open(parent==null?new BridgeHistoryScreen():parent);}
}
