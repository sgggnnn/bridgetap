package com.bridger.momentum.mixin;

import com.bridger.momentum.BridgeTracker;
import com.bridger.momentum.PlacementHooks;
import net.minecraft.unmapped.C_28786548;
import net.minecraft.unmapped.C_32594356;
import net.minecraft.unmapped.C_34267874;
import net.minecraft.unmapped.C_34399650;
import net.minecraft.unmapped.C_57778754;
import net.minecraft.unmapped.C_71827890;
import net.minecraft.unmapped.C_88708284;
import net.minecraft.unmapped.C_94584382;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C_34267874.class)
public class PlayerControllerMPMixin {

    @Inject(method = "m_10759796", at = @At("HEAD"))
    private void bridgetap$capturePlacement(C_57778754 player, C_34399650 world, C_88708284 stack, C_32594356 pos, C_28786548 side, C_94584382 hitVec, CallbackInfoReturnable<Boolean> cir) {
        com.bridger.momentum.PlacementAppearance.beginPlacement(stack);
        PlacementHooks.capture(world, pos, side, stack);
    }

    @Inject(method = "m_10759796", at = @At("RETURN"))
    private void bridgetap$onPlayerRightClick(C_57778754 player, C_34399650 world, C_88708284 stack, C_32594356 pos, C_28786548 side, C_94584382 hitVec, CallbackInfoReturnable<Boolean> cir) {
        try{if (cir.getReturnValue() && stack != null && stack.m_23235460() instanceof C_71827890) {
            BridgeTracker.onBlockPlaced();
            PlacementHooks.finish(world, player);
        }}finally{com.bridger.momentum.PlacementAppearance.endPlacement();}
    }
}
