package com.bridger.momentum.oneclient;

import com.bridger.momentum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_85740840;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.annotations.*;

/** Native OneConfig settings and entry points to every BridgeTap tool. */
public final class OneClientConfig extends Config {
    @MasterToggle @Switch(title="Enable BridgeTap",category="General") public boolean modEnabled=BridgeConfig.modEnabled;
    @Switch(title="Bridge coordinates",category="HUD") public boolean coordinatesVisible=BridgeConfig.enabled;
    @Switch(title="Velo Z decimals (jump -1t)",category="HUD") public boolean showBridgeVelocity=BridgeConfig.showBridgeVelocity;
    @Switch(title="Both axes + X-Z difference",category="HUD") public boolean showBothAxes=BridgeConfig.showBothAxes;
    @Switch(title="Jump viewing angle (yaw)",category="HUD") public boolean showJumpAngle=BridgeConfig.showJumpAngle;
    @Switch(title="Replay information",category="HUD") public boolean showReplayInfo=BridgeConfig.showReplayInfo;
    @Switch(title="Detailed replay information",category="HUD") public boolean detailedReplayInfo=BridgeConfig.detailedReplayInfo;
    @Switch(title="Keystrokes outside replays",category="HUD") public boolean showLiveInputs=BridgeConfig.showLiveInputs;
    @Switch(title="Replay keystrokes",category="HUD") public boolean showReplayInputs=BridgeConfig.showReplayInputs;
    @Switch(title="Background",category="HUD") public boolean backgroundEnabled=BridgeConfig.backgroundEnabled;
    @Switch(title="Text shadow",category="HUD") public boolean textShadow=BridgeConfig.textShadow;
    @Switch(title="Coordinates in chat",category="HUD") public boolean chatOutput=BridgeConfig.chatOutput;
    @Switch(title="Hide coordinates on F3",category="HUD") public boolean hideOnF3=BridgeConfig.hideOnF3;
    @Switch(title="Show gain labels",category="Detection") public boolean showGainType=BridgeConfig.showGainType;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Jump offset",min=-20,max=20,category="Detection") public int tickOffset=BridgeConfig.tickOffset;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Plate offset",min=-20,max=20,category="Detection") public int plateTickOffset=BridgeConfig.plateTickOffset;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Turn detection",min=1,max=60,category="Detection") public int turnTicks=BridgeConfig.turnTicks;
    public OneClientConfig(){super("bridgetap-oneconfig.json","assets/bridgetap/icon.png","BridgeTap",Category.UTILITY);initialize(false);}
    @Override protected void onInitialized(Tree tree){
        super.onInitialized(tree);tree.addMetadata("mod_card_authors","sggn");
        for(String field:new String[]{"modEnabled","coordinatesVisible","showBridgeVelocity","showJumpAngle","showBothAxes","showReplayInfo","detailedReplayInfo","showReplayInputs","showLiveInputs","backgroundEnabled","textShadow","chatOutput","hideOnF3","showGainType","tickOffset","plateTickOffset","turnTicks"}){
            Runnable apply=()->{try{if(field.equals("modEnabled"))BridgeRuntime.setEnabled(modEnabled);else{BridgeConfig.class.getField(field.equals("coordinatesVisible")?"enabled":field).set(null,getClass().getField(field).get(this));BridgeConfig.save();}}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}};
            apply.run();addCallback(field,apply);
        }
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public void syncFromBridge(){
        NativeHudStyles.sync();
        for(String field:new String[]{"modEnabled","coordinatesVisible","showBridgeVelocity","showJumpAngle","showBothAxes","showReplayInfo","detailedReplayInfo","showReplayInputs","showLiveInputs","backgroundEnabled","textShadow","chatOutput","hideOnF3","showGainType","tickOffset","plateTickOffset","turnTicks"}){
            try{org.polyfrost.oneconfig.api.config.v1.Property property=getProperty(field);Object value=BridgeConfig.class.getField(field.equals("coordinatesVisible")?"enabled":field).get(null);if(!java.util.Objects.equals(property.get(),value))property.set(value);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        }
    }
    public java.util.List<org.polyfrost.oneconfig.api.config.v1.Property<?>> hudProperties(int type){return NativeHudStyles.properties(type);}
    public void bindHudStyle(int type,Object hud){NativeHudStyles.bind(type,hud);}
    @Button(title="Replay library",text="Open",category="Replays") public void menuReplays(){BridgeTapMenus.open("replays");}
    @Button(title="All tools",text="Open",category="Replays") public void menuTools(){BridgeTapMenus.open("tools");}
    @Button(title="Import replays",text="Open",category="Replays") public void menuImports(){BridgeTapMenus.open("imports");}
    @Button(title="Replay details",text="Open",category="Replays") public void menuReplayDetails(){BridgeTapMenus.open("replay-details");}
    @Button(title="Share a replay",text="Open",category="Replays") public void menuShare(){BridgeTapMenus.open("share");}
    @Button(title="Replay bookmarks",text="Open",category="Replays") public void menuBookmarks(){BridgeTapMenus.open("bookmarks");}
    @Button(title="Export replay to video",text="Open",category="Replays") public void menuVideoExport(){BridgeTapMenus.open("video-export");}
    @Button(title="Failure analysis",text="Open",category="Replays") public void menuFailAnalysis(){BridgeTapMenus.open("fail-analysis");}
    @Button(title="Statistics",text="Open",category="Replays") public void menuStatistics(){BridgeTapMenus.open("statistics");}
    @Button(title="Storage and autosave",text="Open",category="Replays") public void menuReplayStorage(){BridgeTapMenus.open("replay-storage");}
    @Button(title="Island library",text="Open",category="Islands") public void menuIslands(){BridgeTapMenus.open("islands");}
    @Button(title="Island presets",text="Open",category="Islands") public void menuIslandPresets(){BridgeTapMenus.open("island-presets");}
    @Button(title="Import a preset",text="Open",category="Islands") public void menuIslandImport(){BridgeTapMenus.open("island-import");}
    @Button(title="Copy an island from the world",text="Open",category="Islands") public void menuIslandCopy(){BridgeTapMenus.open("island-copy");}
    @Button(title="Active island materials",text="Open",category="Islands") public void menuIslandMaterials(){BridgeTapMenus.open("island-materials");}
    @Button(title="Place the active island",text="Open",category="Islands") public void menuIslandPlacement(){BridgeTapMenus.open("island-placement");}
    @Button(title="Block appearance and sounds",text="Open",category="Appearance") public void menuBlocks(){BridgeTapMenus.open("blocks");}
    @Button(title="Create a sky overlay",text="Open",category="Appearance") public void menuSky(){BridgeTapMenus.open("sky");}
    @Button(title="Create a sandstone overlay",text="Open",category="Appearance") public void menuSandstone(){BridgeTapMenus.open("sandstone");}
    @Button(title="Menu theme and appearance",text="Open",category="Appearance") public void menuTheme(){BridgeTapMenus.open("theme");}
    @Button(title="Video clips, audio and microphone",text="Open",category="Recording") public void menuClips(){BridgeTapMenus.open("clips");}
    @Button(title="Keybind profiles",text="Open",category="Settings") public void menuKeybinds(){BridgeTapMenus.open("keybinds");}
    @Button(title="Failure display",text="Open",category="HUD") public void menuFailureDisplay(){BridgeTapMenus.open("failure-display");}
    @Button(title="Detailed overlay colors",text="Open",category="HUD") public void menuColors(){BridgeTapMenus.open("colors");}
    @Button(title="Native HUD editor",text="Open",category="HUD") public void menuHudEditor(){BridgeTapMenus.open("hud-editor");}
    @Button(title="BridgeTap HUD layout",text="Open",category="HUD") public void menuClassicLayout(){BridgeTapMenus.open("classic-layout");}
    @Button(title="All BridgeTap settings",text="Open",category="Settings") public void menuSettings(){BridgeTapMenus.open("settings");}
    @Button(title="Guide and tutorials",text="Open",category="Help") public void menuGuide(){BridgeTapMenus.open("guide");}
    @Button(title="In-game mod updates",text="Open",category="Help") public void menuUpdates(){BridgeTapMenus.open("updates");}
    @Button(title="Discord community",text="Open",category="Help") public void menuDiscord(){BridgeTapMenus.open("discord");}
}
