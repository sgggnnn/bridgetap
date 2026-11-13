package com.bridger.momentum.mixin;
import com.bridger.momentum.ReplaySession;
import net.minecraft.unmapped.C_24384787;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Texture/shader reloads may rebuild the offscreen renderer against the live world. */
@Mixin(C_24384787.class)
public final class ReplayRendererReloadMixin {
 @Inject(method="m_12518137",at=@At("HEAD")) private void bridgetap$rebindReplay(CallbackInfo ci){ReplaySession.rendererInvalidated();}
}
