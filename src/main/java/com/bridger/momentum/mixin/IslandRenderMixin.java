package com.bridger.momentum.mixin;

import com.bridger.momentum.IslandOverlay;
import com.bridger.momentum.IslandCopySelection;
import net.minecraft.unmapped.C_03140863;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_03140863.class)
public final class IslandRenderMixin {
    @Inject(method="m_63866511",at=@At(value="INVOKE",target="Lnet/minecraft/unmapped/C_24384787;m_32240019(Lnet/minecraft/unmapped/C_77523700;DILnet/minecraft/unmapped/C_42232651;)I",ordinal=0,shift=At.Shift.AFTER))
    private void bridgetap$scenery(int pass,float partial,long deadline,CallbackInfo ci){IslandOverlay.render(partial);IslandCopySelection.render(partial);}
}
