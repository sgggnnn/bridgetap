package com.bridger.momentum.mixin;

import com.bridger.momentum.BridgeHUD;
import com.bridger.momentum.BridgeTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "m_67975642", at = @At("HEAD"))
    private void bridgetap$onRunTick(CallbackInfo ci) {
        BridgeHUD.onClientTick();
        BridgeTracker.onTick((Minecraft) (Object) this);
    }
}
