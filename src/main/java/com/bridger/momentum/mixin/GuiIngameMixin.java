package com.bridger.momentum.mixin;

import com.bridger.momentum.BridgeHUD;
import net.minecraft.unmapped.C_97866173;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C_97866173.class)
public class GuiIngameMixin {

    @Inject(method = "m_94668477", at = @At("RETURN"))
    private void bridgetap$onRenderOverlay(float partialTicks, CallbackInfo ci) {
        BridgeHUD.render((C_97866173) (Object) this);
    }
}
