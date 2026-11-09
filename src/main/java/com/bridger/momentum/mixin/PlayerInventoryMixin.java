package com.bridger.momentum.mixin;
import com.bridger.momentum.ReplaySession;
import net.minecraft.unmapped.C_87542771;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_87542771.class)
public class PlayerInventoryMixin {
 @Inject(method="m_49669981",at=@At("HEAD"),cancellable=true)
 private void bridgetap$replayHotbar(int delta,CallbackInfo ci){if(ReplaySession.scroll((C_87542771)(Object)this,delta))ci.cancel();}
}
