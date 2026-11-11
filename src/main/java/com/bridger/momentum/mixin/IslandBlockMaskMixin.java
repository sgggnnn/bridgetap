package com.bridger.momentum.mixin;
import com.bridger.momentum.IslandReplacement;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C_57103744.class)
public final class IslandBlockMaskMixin {
    @Inject(method="m_04667098",at=@At("HEAD"),cancellable=true)
    private void bridgetap$hideOriginal(C_15815210 state,C_32594356 pos,C_46108969 world,C_93396801 buffer,CallbackInfoReturnable<Boolean> ci){if(IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))ci.setReturnValue(false);}
}
