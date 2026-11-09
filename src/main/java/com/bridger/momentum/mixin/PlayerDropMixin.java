package com.bridger.momentum.mixin;
import com.bridger.momentum.ReplaySession;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C_40996800.class)
public class PlayerDropMixin {
 @Inject(method="m_15592321",at=@At("HEAD"),cancellable=true)
 private void bridgetap$noLiveDrops(boolean stack,CallbackInfoReturnable<C_06774341> cir){if(ReplaySession.active())cir.setReturnValue(null);}
}
