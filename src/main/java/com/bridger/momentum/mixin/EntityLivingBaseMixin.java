package com.bridger.momentum.mixin;

import com.bridger.momentum.BridgeTracker;
import net.minecraft.unmapped.C_42232651;
import net.minecraft.unmapped.C_74425583;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_74425583.class)
public class EntityLivingBaseMixin {

    @Inject(method = "m_47773948", at = @At("HEAD"))
    private void bridgetap$onJump(CallbackInfo ci) {
        BridgeTracker.onJump((C_42232651) (Object) this);
    }
}
