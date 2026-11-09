package com.bridger.momentum.mixin;
import com.bridger.momentum.ReplaySession;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_08381405.class)
public class KeyboardInputMixin {
 @Inject(method="m_81342827",at=@At("RETURN"))
 private void bridgetap$holdLivePlayerStill(CallbackInfo ci){
  if(ReplaySession.active()){
   C_54206210 input=(C_54206210)(Object)this;
   if(ReplaySession.captureMovement(input)){
    input.f_54482307=input.f_81168741=0;input.f_92317429=input.f_28813338=false;
   }
  }
 }
}
