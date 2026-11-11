package com.bridger.momentum.mixin;
import com.bridger.momentum.IslandReplacement;
import com.bridger.momentum.PlacementAppearance;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C_97027864.class)
public final class IslandChunkCacheMaskMixin {
    @Shadow protected C_64041439 f_13964122;
    @Inject(method="m_77852500",at=@At("HEAD"),cancellable=true)
    private void bridgetap$maskClientCache(C_32594356 pos,CallbackInfoReturnable<C_15815210> ci){if(f_13964122!=null&&f_13964122.f_50876584&&IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))ci.setReturnValue(C_81592558.m_64320102(0).m_52891069());}
    @Inject(method="m_77852500",at=@At("RETURN"),cancellable=true)
    private void bridgetap$placedAppearance(C_32594356 pos,CallbackInfoReturnable<C_15815210> ci){if(f_13964122==null||!f_13964122.f_50876584)return;C_15815210 state=PlacementAppearance.replacement(pos,ci.getReturnValue());if(state!=null)ci.setReturnValue(state);}
}
