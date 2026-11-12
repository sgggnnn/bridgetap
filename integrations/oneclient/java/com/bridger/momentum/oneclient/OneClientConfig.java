package com.bridger.momentum.oneclient;

import com.bridger.momentum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_85740840;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.annotations.*;

/** Native OneConfig settings and entry points to every BridgeTap tool. */
public final class OneClientConfig extends Config {
    @Switch(title="Bridge coordinates",category="HUD") public boolean coordinatesVisible=BridgeConfig.enabled;
    @Switch(title="Velo Z decimals (jump -1t)",category="HUD") public boolean showBridgeVelocity=BridgeConfig.showBridgeVelocity;
    @Switch(title="Both axes + X-Z difference",category="HUD") public boolean showBothAxes=BridgeConfig.showBothAxes;
    @Switch(title="Jump viewing angle (yaw)",category="HUD") public boolean showJumpAngle=BridgeConfig.showJumpAngle;
    @Switch(title="Replay information",category="HUD") public boolean showReplayInfo=BridgeConfig.showReplayInfo;
    @Switch(title="Replay keystrokes",category="HUD") public boolean showReplayInputs=BridgeConfig.showReplayInputs;
    @Switch(title="Background",category="HUD") public boolean backgroundEnabled=BridgeConfig.backgroundEnabled;
    @Switch(title="Text shadow",category="HUD") public boolean textShadow=BridgeConfig.textShadow;
    @Switch(title="Coordinates in chat",category="HUD") public boolean chatOutput=BridgeConfig.chatOutput;
    @Switch(title="Hide coordinates on F3",category="HUD") public boolean hideOnF3=BridgeConfig.hideOnF3;
    @Switch(title="Show gain labels",category="Detection") public boolean showGainType=BridgeConfig.showGainType;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Jump offset",min=-20,max=20,category="Detection") public int tickOffset=BridgeConfig.tickOffset;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Plate offset",min=-20,max=20,category="Detection") public int plateTickOffset=BridgeConfig.plateTickOffset;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Turn detection",min=1,max=60,category="Detection") public int turnTicks=BridgeConfig.turnTicks;
    public OneClientConfig(){super("bridgetap","BridgeTap",Category.UTILITY);initialize(false);}
    @Override protected void onInitialized(Tree tree){
        super.onInitialized(tree);tree.addMetadata("mod_card_authors","sggn");
        for(String field:new String[]{"coordinatesVisible","showBridgeVelocity","showJumpAngle","showBothAxes","showReplayInfo","showReplayInputs","backgroundEnabled","textShadow","chatOutput","hideOnF3","showGainType","tickOffset","plateTickOffset","turnTicks"}){
            Runnable apply=()->{try{BridgeConfig.class.getField(field.equals("coordinatesVisible")?"enabled":field).set(null,getClass().getField(field).get(this));BridgeConfig.save();}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}};
            apply.run();addCallback(field,apply);
        }
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public void syncFromBridge(){
        NativeHudStyles.sync();
        for(String field:new String[]{"coordinatesVisible","showBridgeVelocity","showJumpAngle","showBothAxes","showReplayInfo","showReplayInputs","backgroundEnabled","textShadow","chatOutput","hideOnF3","showGainType","tickOffset","plateTickOffset","turnTicks"}){
            try{org.polyfrost.oneconfig.api.config.v1.Property property=getProperty(field);Object value=BridgeConfig.class.getField(field.equals("coordinatesVisible")?"enabled":field).get(null);if(!java.util.Objects.equals(property.get(),value))property.set(value);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
        }
    }
    public java.util.List<org.polyfrost.oneconfig.api.config.v1.Property<?>> hudProperties(int type){return NativeHudStyles.properties(type);}
    public void bindHudStyle(int type,Object hud){NativeHudStyles.bind(type,hud);}
    private void open(C_85740840 screen){Minecraft mc=Minecraft.m_20213497();if(mc.f_28396371!=null)mc.m_52715402(screen);}
    @Button(title="Replay library",text="Browse replays",category="Replays") public void replays(){open(ReplayFolderScreen.home());}
    @Button(title="Island library",text="Browse islands",category="Islands") public void islands(){open(new IslandScreen(null));}
    @Button(title="Island presets",text="Browse presets",category="Islands") public void presets(){open(new IslandPresetsScreen(null));}
    @Button(title="BridgeTap updates",text="Check updates",category="Tools") public void updates(){UpdateManager.open(null);}
    @Button(title="Video clips",text="Open",category="Tools") public void clips(){open(new ClipScreen(null));}
    @Button(title="Block appearance",text="Open",category="Tools") public void blocks(){open(new PlacementAppearanceScreen(null));}
    @Button(title="Overlay colors",text="Open",category="HUD") public void colors(){NativeHudStyles.edit(0);}
    @Button(title="OneClient HUD editor",text="Edit HUDs",category="HUD") public void editor(){org.polyfrost.oneconfig.api.hud.v1.HudManager.INSTANCE.openEditor();}
    @Button(title="All BridgeTap settings",text="Open",category="Tools") public void settings(){open(new BridgeSettingsScreen(null));}
}
