package com.bridger.momentum.mixin;

import com.bridger.momentum.BridgeTracker;
import net.minecraft.unmapped.C_57778754;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_57778754.class)
public class EntityPlayerSPMixin {

    @Inject(method = "m_83442988", at = @At("HEAD"), cancellable = true)
    private void bridgetap$onSendChatMessage(String message, CallbackInfo ci) {
        if (BridgeTracker.onChatMessage(message)) {
            ci.cancel();
        }
    }
}
