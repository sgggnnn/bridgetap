package com.bridger.momentum.mixin;
import com.bridger.momentum.ModernAppearance;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(C_83717615.class)
public final class ModernTextureMixin {
 @Inject(method="m_46857130",at=@At("HEAD")) private void bridgetap$modernTextures(C_52500310 resources,CallbackInfo ci){ModernAppearance.stitch((C_83717615)(Object)this);}
}
