package com.bridger.momentum.mixin;
import com.bridger.momentum.IslandReplacement;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_73390696.class)
public final class IslandBlockEntityMaskMixin {
    @Inject(method="m_95164519",at=@At("HEAD"),cancellable=true)
    private void bridgetap$maskBlockEntity(C_40735137 entity,double x,double y,double z,float partial,int stage,CallbackInfo ci){C_32594356 pos=entity.m_98430793();if(IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))ci.cancel();}
}
