package com.bridger.momentum.mixin;
import com.bridger.momentum.IslandReplacement;
import com.bridger.momentum.PlacementAppearance;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mask before cached terrain lookups, including OptiFine's render-region path. */
@Mixin(C_81669619.class)
public final class IslandRenderCacheMaskMixin {
    @Inject(method="m_77852500",at=@At("HEAD"),cancellable=true)
    private void bridgetap$maskCache(C_32594356 pos,CallbackInfoReturnable<C_15815210> ci){if(IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))ci.setReturnValue(C_81592558.m_64320102(0).m_52891069());}
    @Inject(method="m_77852500",at=@At("RETURN"),cancellable=true)
    private void bridgetap$placedAppearance(C_32594356 pos,CallbackInfoReturnable<C_15815210> ci){C_15815210 state=PlacementAppearance.replacement(pos,ci.getReturnValue());if(state!=null)ci.setReturnValue(state);}
}
