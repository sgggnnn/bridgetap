package com.bridger.momentum.mixin;

import com.bridger.momentum.FocusAudio;
import net.minecraft.unmapped.C_03111506;
import net.minecraft.unmapped.C_53396390;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_03111506.class)
public abstract class FocusSoundMixin {
    @Inject(method="m_38883552",at=@At("HEAD"),cancellable=true)
    private void bridgetap$focusSound(C_53396390 sound,CallbackInfo ci){if(FocusAudio.suppress(sound))ci.cancel();}
}
