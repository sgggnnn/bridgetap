package com.bridger.momentum;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

public final class ServerPresets {
    private static PresetServers bindings;private static Object world;private static int wait;private static boolean done,applying;
    public static PresetServers bindings()throws java.io.IOException{if(bindings==null)bindings=new PresetServers(Paths.get("config","bridgetap","preset-servers.properties"));return bindings;}
    public static String address(){Minecraft mc=Minecraft.m_20213497();if(mc==null||mc.f_26407825==null)return "";C_91543234 entry=mc.m_73689431();return mc.m_10057689()?"Singleplayer":entry==null?"":entry.f_01631523;}
    public static void manual(){if(!applying)done=true;}
    public static void bindHere(String server,IslandPresets.Preset preset)throws java.io.IOException{PresetServers map=bindings();if(map.preset(server).equals(preset.id))map.assign(server,"");else map.assign(server,preset.id,new PresetServers.Appearance(PlacementAppearance.block(),PlacementAppearance.metadata(),PlacementAppearance.enabled()));}
    public static void tick(){
        Minecraft mc=Minecraft.m_20213497();if(mc==null)return;IslandOverlay.tick();
        if(world!=mc.f_26407825){world=mc.f_26407825;wait=0;done=false;}
        if(world==null||done||ReplaySession.active()||BridgeTracker.isBridging()||mc.f_28396371==null||mc.f_70816363!=null)return;
        if(++wait<40||wait%20!=0)return;
        try{
            String id=bindings().preset(address());if(id.isEmpty()){done=true;return;}
            if(IslandOverlay.enabled()){done=true;return;}
            if(!IslandStartAnchors.available())return;
            for(IslandPresets.Preset preset:IslandPresets.local().list())if(preset.id.equals(id)){
                applying=true;try{IslandOverlay.applyPreset(preset.template(),preset.layout);}finally{applying=false;}
                PresetServers.Appearance appearance=bindings().appearance(address());if(appearance!=null)PlacementAppearance.apply(appearance.block,appearance.variant,appearance.enabled);
                done=true;mc.f_28396371.m_83523863(new C_69136080("\u00a7b[BridgeTap] \u00a7fServer preset: "+preset.name));return;
            }
            done=true;
        }catch(Exception ex){done=true;System.err.println("[BridgeTap] Server preset: "+ex.getMessage());}
    }
}
