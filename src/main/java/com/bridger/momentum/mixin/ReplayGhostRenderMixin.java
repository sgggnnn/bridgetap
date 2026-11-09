package com.bridger.momentum.mixin;
import com.bridger.momentum.ReplaySession;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_34859172.class)
public class ReplayGhostRenderMixin {
 @Inject(method="m_69546567",at=@At("HEAD"),cancellable=true)
 private void bridgetap$clearGhostView(C_23527099 actor,double x,double y,double z,float yaw,float partial,CallbackInfo ci){
  // Skip only the skin/model. The dispatcher's white debug hitbox still renders.
  if(ReplaySession.hideGhostModel(actor))ci.cancel();
 }
}
