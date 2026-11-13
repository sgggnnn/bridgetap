package com.bridger.momentum.mixin;
import com.bridger.momentum.*;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(C_03832013.class)
public final class AppearanceItemMixin {
 @Inject(method="m_24478184",at=@At("RETURN"),cancellable=true) private void bridgetap$appearanceItem(C_88708284 stack,CallbackInfoReturnable<C_41102159> ci){C_41102159 model=PlacementAppearance.itemModel((C_03832013)(Object)this,stack);if(model!=null)ci.setReturnValue(model);}
}
