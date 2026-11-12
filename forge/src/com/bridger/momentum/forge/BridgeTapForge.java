package com.bridger.momentum.forge;

import com.bridger.momentum.BridgeConfig;
import com.bridger.momentum.BridgeHUD;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid="bridgetap", name="BridgeTap", version="1.7.3", clientSideOnly=true,
     acceptedMinecraftVersions="[1.8.9]", acceptableRemoteVersions="*")
public final class BridgeTapForge {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BridgeConfig.load();
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("[BridgeTap] Forge client initialized");
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        // GuiIngameForge overrides renderGameOverlay without calling the vanilla
        // method targeted by GuiIngameMixin. The private replay GUI still uses
        // that vanilla hook; normal Forge gameplay uses this final overlay event.
        if(event.type==RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft mc=Minecraft.func_71410_x();
            if(mc!=null)BridgeHUD.render(mc.field_71456_v);
        }
    }
}
