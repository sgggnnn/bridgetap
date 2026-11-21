package com.bridger.momentum;

import net.minecraft.unmapped.C_85740840;
import java.util.Locale;

/** Category navigation keeps the control panel focused and readable. */
public class BridgeSettingsScreen extends BridgeGlassScreen {
    private final C_85740840 parent;private int category;
    private static final String[] CATEGORIES={"HUD & layout","Detection","Replay","Visuals","Tools & support"};
    private static final String[] DESCRIPTIONS={"Place your HUD and choose how it looks.","Coordinates, timing and jump calculations.","Keep the replay information you actually need.","Customize your blocks, islands and sky.","Clips, controls, updates and help."};
    public BridgeSettingsScreen(){this(null);}
    public BridgeSettingsScreen(C_85740840 parent){this.parent=parent;}
    @Override protected void paint(){
        windowHeader("BridgeTap Settings");button("BridgeGlass",716,25,128,32,true,false,()->open(new MenuThemeScreen(this)));button("Guide",856,25,104,32,true,false,()->open(new BridgeTutorialScreen(this)));button("Back",972,25,116,32,true,false,this::back);
        sidebar("PERSONALIZE");
        for(int i=0;i<CATEGORIES.length;i++){final int index=i;sidebarItem(CATEGORIES[i],138+i*44,category==i,()->category=index);}
        button("Join Discord",32,558,184,32,true,false,()->CommunityLinks.discord());
        ui.strongText(CATEGORIES[category],264,104,TEXT,1.2f);label(DESCRIPTIONS[category],264,132);
        if(category==0)hud();else if(category==1)detection();else if(category==2)replay();else if(category==3)visuals();else tools();
        windowStatus("Saved automatically / Esc: back");
    }
    private void hud(){
        toggle("HUD enabled",BridgeConfig.enabled,264,146,824,()->BridgeConfig.enabled=!BridgeConfig.enabled);
        toggle("Hide while F3 is open",BridgeConfig.hideOnF3,264,182,824,()->BridgeConfig.hideOnF3=!BridgeConfig.hideOnF3);
        button("Edit HUD layout",264,222,824,32,true,true,()->open(new BridgeLayoutScreen(this)));
        stepper("Scale",String.format(Locale.US,"%.1fx",BridgeConfig.hudScale),264,270,824,
            ()->BridgeConfig.hudScale=Math.max(.5,Math.round((BridgeConfig.hudScale-.1)*10)/10.0),
            ()->BridgeConfig.hudScale=Math.min(2,Math.round((BridgeConfig.hudScale+.1)*10)/10.0));
        toggle("Text shadow",BridgeConfig.textShadow,264,306,824,()->BridgeConfig.textShadow=!BridgeConfig.textShadow);
        toggle("Background",BridgeConfig.backgroundEnabled,264,342,824,()->BridgeConfig.backgroundEnabled=!BridgeConfig.backgroundEnabled);
        String[] modes={"ALL","COORDS","COORDS_ONLY","LAST_TURN","TICKS","TIME"};
        String[] labels={"All statistics","Coordinates with labels","Coordinates only","Last + Turn coordinates","Ticks only","Run time only"};
        int index=Math.max(0,java.util.Arrays.asList(modes).indexOf(BridgeConfig.displayMode));
        label("Display mode",264,392);dropdown("display",labels[index],labels,index,740,384,348,i->{BridgeConfig.displayMode=modes[i];BridgeConfig.save();});
        button("Customize overlay colors",264,422,824,26,true,false,()->open(new BridgeColorsScreen(this)));
    }
    private void detection(){
        stepper("Jump offset",BridgeConfig.tickOffset+" ticks",264,146,824,()->BridgeConfig.tickOffset=Math.max(-40,BridgeConfig.tickOffset-1),()->BridgeConfig.tickOffset=Math.min(40,BridgeConfig.tickOffset+1));
        stepper("Plate offset",BridgeConfig.plateTickOffset+" ticks",264,182,824,()->BridgeConfig.plateTickOffset=Math.max(-40,BridgeConfig.plateTickOffset-1),()->BridgeConfig.plateTickOffset=Math.min(40,BridgeConfig.plateTickOffset+1));
        stepper("Turn detection",BridgeConfig.turnTicks+" ticks",264,218,824,()->BridgeConfig.turnTicks=12,()->BridgeConfig.turnTicks=15);
        toggle("Show Gain / No Gain",BridgeConfig.showGainType,264,254,824,()->BridgeConfig.showGainType=!BridgeConfig.showGainType);
        label("Indicator label",264,298);dropdown("gain",BridgeConfig.useNoGayLabel?"No Gay":"No Gain",new String[]{"No Gain","No Gay"},BridgeConfig.useNoGayLabel?1:0,918,290,170,i->{BridgeConfig.useNoGayLabel=i==1;BridgeConfig.save();});
        toggle("Pre-jump coordinate (jump -1t)",BridgeConfig.showBridgeVelocity,264,326,824,()->BridgeConfig.showBridgeVelocity=!BridgeConfig.showBridgeVelocity);
        toggle("Both axes + X-Z difference",BridgeConfig.showBothAxes,264,362,824,()->BridgeConfig.showBothAxes=!BridgeConfig.showBothAxes);
        toggle("Jump viewing angle (yaw)",BridgeConfig.showJumpAngle,264,398,824,()->BridgeConfig.showJumpAngle=!BridgeConfig.showJumpAngle);
        button("Reset offsets",934,431,154,22,true,false,()->{BridgeConfig.tickOffset=BridgeConfig.plateTickOffset=0;BridgeConfig.save();});
    }
    private void replay(){
        label("Replay information",264,156);
        dropdown("replay-info",!BridgeConfig.showReplayInfo?"Hidden":BridgeConfig.detailedReplayInfo?"Detailed":"Compact",new String[]{"Compact","Detailed","Hidden"},!BridgeConfig.showReplayInfo?2:BridgeConfig.detailedReplayInfo?1:0,740,148,348,i->{BridgeConfig.showReplayInfo=i!=2;BridgeConfig.detailedReplayInfo=i==1;BridgeConfig.save();});
        toggle("Keystrokes in replays",BridgeConfig.showReplayInputs,264,200,824,()->BridgeConfig.showReplayInputs=!BridgeConfig.showReplayInputs);
        toggle("Keystrokes outside replays",BridgeConfig.showLiveInputs,264,242,824,()->BridgeConfig.showLiveInputs=!BridgeConfig.showLiveInputs);
        label("Coordinates output",264,298);dropdown("output",BridgeConfig.chatOutput?"Chat at end of run":"Overlay",new String[]{"Overlay","Chat at end of run"},BridgeConfig.chatOutput?1:0,740,290,348,i->{BridgeConfig.chatOutput=i==1;BridgeConfig.save();});
        button("Edit overlay layout",264,346,824,32,true,true,()->open(new BridgeLayoutScreen(this)));
        button("Storage & auto-save",264,398,400,32,true,false,()->open(new ReplayStorageScreen(this)));
        button("Failed bridge display",688,398,400,32,true,false,()->open(new FailureDisplayScreen(this)));
    }
    private void feature(String title,String description,String icon,int x,int y,Runnable action){
        MenuIcons.paint(ui,icon,x+18,y+16,ACCENT);clipped(description,x+66,y+27,198,MUTED);button(title,x+12,y+88,376,32,true,false,action);
    }
    private void visuals(){
        feature("Island library","Client scenery & materials","islands",264,148,()->open(new IslandScreen(this)));
        feature("Block appearance","Textures & placement sounds","blocks",688,148,()->open(new PlacementAppearanceScreen(this)));
        feature("Custom sky overlays","Create from your packs","sky",264,300,()->open(new SkyOverlayScreen(this)));
        feature("Overlay colors","Fine-tune every indicator","layout",688,300,()->open(new BridgeColorsScreen(this)));
    }
    private void tools(){
        button("Video clips & audio",264,150,400,40,true,false,()->open(new ClipScreen(this)));
        button("Keybind profiles",688,150,400,40,true,false,()->open(new KeybindProfilesScreen(this)));
        button("Statistics",264,208,400,40,true,false,()->open(new StatisticsScreen(this)));
        button("Check for updates",688,208,400,40,true,false,()->UpdateManager.open(this));
        button("Failed bridge settings",264,266,400,40,true,false,()->open(new FailureDisplayScreen(this)));
        button("Replay storage",688,266,400,40,true,false,()->open(new ReplayStorageScreen(this)));
        text("LEARN BY WATCHING",280,354,ACCENT);label("Step-by-step guides for BridgeTap features.",280,378);
        button("Explore tutorials",908,394,164,28,true,true,()->open(new BridgeTutorialScreen(this)));
    }
    @Override protected void back(){BridgeConfig.save();open(parent==null?ReplayFolderScreen.home():parent);}
}
