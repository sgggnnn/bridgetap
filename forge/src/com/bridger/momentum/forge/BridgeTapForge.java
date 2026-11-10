package com.bridger.momentum.forge;

import com.bridger.momentum.BridgeConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="bridgetap", name="BridgeTap", version="1.4.4", clientSideOnly=true,
     acceptedMinecraftVersions="[1.8.9]", acceptableRemoteVersions="*")
public final class BridgeTapForge {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BridgeConfig.load();
        System.out.println("[BridgeTap] Forge client initialized");
    }
}
