package com.bridger.momentum.forge;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

/** Boots the bundled LaunchWrapper Mixin service before Minecraft is loaded. */
@IFMLLoadingPlugin.Name("BridgeTap")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"com.bridger.momentum.forge."})
public final class BridgeTapLoadingPlugin implements IFMLLoadingPlugin {
    public BridgeTapLoadingPlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("bridgetap.forge.mixins.json");
    }
    @Override public String[] getASMTransformerClass() { return new String[0]; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String,Object> data) {}
    @Override public String getAccessTransformerClass() { return null; }
}
