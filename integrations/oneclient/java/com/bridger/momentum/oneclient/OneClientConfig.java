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
    @Button(title="Bibliothèque des replays",text="Ouvrir",category="Replays") public void menuReplays(){BridgeTapMenus.open("replays");}
    @Button(title="Tous les outils",text="Ouvrir",category="Replays") public void menuTools(){BridgeTapMenus.open("tools");}
    @Button(title="Importer des replays",text="Ouvrir",category="Replays") public void menuImports(){BridgeTapMenus.open("imports");}
    @Button(title="Détails du replay",text="Ouvrir",category="Replays") public void menuReplayDetails(){BridgeTapMenus.open("replay-details");}
    @Button(title="Partager un replay",text="Ouvrir",category="Replays") public void menuShare(){BridgeTapMenus.open("share");}
    @Button(title="Signets du replay",text="Ouvrir",category="Replays") public void menuBookmarks(){BridgeTapMenus.open("bookmarks");}
    @Button(title="Exporter un replay en vidéo",text="Ouvrir",category="Replays") public void menuVideoExport(){BridgeTapMenus.open("video-export");}
    @Button(title="Analyser un échec",text="Ouvrir",category="Replays") public void menuFailAnalysis(){BridgeTapMenus.open("fail-analysis");}
    @Button(title="Statistiques",text="Ouvrir",category="Replays") public void menuStatistics(){BridgeTapMenus.open("statistics");}
    @Button(title="Stockage et sauvegarde automatique",text="Ouvrir",category="Replays") public void menuReplayStorage(){BridgeTapMenus.open("replay-storage");}
    @Button(title="Bibliothèque des îles",text="Ouvrir",category="Îles") public void menuIslands(){BridgeTapMenus.open("islands");}
    @Button(title="Presets des îles",text="Ouvrir",category="Îles") public void menuIslandPresets(){BridgeTapMenus.open("island-presets");}
    @Button(title="Importer un preset",text="Ouvrir",category="Îles") public void menuIslandImport(){BridgeTapMenus.open("island-import");}
    @Button(title="Copier une île dans le monde",text="Ouvrir",category="Îles") public void menuIslandCopy(){BridgeTapMenus.open("island-copy");}
    @Button(title="Matériaux de l’île active",text="Ouvrir",category="Îles") public void menuIslandMaterials(){BridgeTapMenus.open("island-materials");}
    @Button(title="Positionner l’île active",text="Ouvrir",category="Îles") public void menuIslandPlacement(){BridgeTapMenus.open("island-placement");}
    @Button(title="Apparence des blocs et sons",text="Ouvrir",category="Apparence") public void menuBlocks(){BridgeTapMenus.open("blocks");}
    @Button(title="Créer un overlay de ciel",text="Ouvrir",category="Apparence") public void menuSky(){BridgeTapMenus.open("sky");}
    @Button(title="Créer un overlay de sandstone",text="Ouvrir",category="Apparence") public void menuSandstone(){BridgeTapMenus.open("sandstone");}
    @Button(title="Thème et apparence des menus",text="Ouvrir",category="Apparence") public void menuTheme(){BridgeTapMenus.open("theme");}
    @Button(title="Clips vidéo, audio et microphone",text="Ouvrir",category="Enregistrement") public void menuClips(){BridgeTapMenus.open("clips");}
    @Button(title="Profils de touches",text="Ouvrir",category="Réglages") public void menuKeybinds(){BridgeTapMenus.open("keybinds");}
    @Button(title="Affichage des échecs",text="Ouvrir",category="HUD") public void menuFailureDisplay(){BridgeTapMenus.open("failure-display");}
    @Button(title="Couleurs détaillées des overlays",text="Ouvrir",category="HUD") public void menuColors(){BridgeTapMenus.open("colors");}
    @Button(title="Éditeur HUD natif",text="Ouvrir",category="HUD") public void menuHudEditor(){BridgeTapMenus.open("hud-editor");}
    @Button(title="Placement HUD BridgeTap",text="Ouvrir",category="HUD") public void menuClassicLayout(){BridgeTapMenus.open("classic-layout");}
    @Button(title="Tous les réglages BridgeTap",text="Ouvrir",category="Réglages") public void menuSettings(){BridgeTapMenus.open("settings");}
    @Button(title="Guide et tutoriels",text="Ouvrir",category="Aide") public void menuGuide(){BridgeTapMenus.open("guide");}
    @Button(title="Mises à jour du mod en jeu",text="Ouvrir",category="Aide") public void menuUpdates(){BridgeTapMenus.open("updates");}
    @Button(title="Communauté Discord",text="Ouvrir",category="Aide") public void menuDiscord(){BridgeTapMenus.open("discord");}
}
