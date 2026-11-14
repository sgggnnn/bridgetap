package com.bridger.momentum.mixin;
import com.bridger.momentum.BridgeTracker;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shared single- and multi-block server update path, after main-thread dispatch. */
@Mixin(C_34399650.class)
public class PlacementConfirmationMixin {
 @Inject(method="m_21075141",at=@At("RETURN"))
 private void bridgetap$confirm(C_32594356 pos,C_15815210 state,CallbackInfoReturnable<Boolean> cir){
  BridgeTracker.onAuthoritativePlacement((C_34399650)(Object)this,pos,C_81592558.m_63461719(state.m_41958956()));
 }
}
