package com.bridger.momentum.mixin;

import com.bridger.momentum.IslandPlacementScreen;
import com.bridger.momentum.IslandCopyScreen;
import com.bridger.momentum.ReplayVideoExport;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_85740840;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional LegacyGuiBlur integration; never disables game shaders globally. */
@Pseudo
@Mixin(targets="eu.midnightdust.blur.BlurRenderer",remap=false)
public abstract class IslandGuiBlurMixin {
    private static boolean bridgetap$clearWorld(){Minecraft mc=Minecraft.m_20213497();return mc!=null&&(mc.f_70816363 instanceof IslandPlacementScreen||mc.f_70816363 instanceof IslandCopyScreen||ReplayVideoExport.active());}
    @Inject(method="render",at=@At("HEAD"),cancellable=true,require=0,remap=false)
    private static void bridgetap$skipEditorBlur(CallbackInfo ci){if(bridgetap$clearWorld())ci.cancel();}
    @Inject(method="renderBackground",at=@At("HEAD"),cancellable=true,require=0,remap=false)
    private static void bridgetap$skipEditorGradient(C_85740840 screen,CallbackInfoReturnable<Boolean> cir){if(bridgetap$clearWorld())cir.setReturnValue(false);}
}
