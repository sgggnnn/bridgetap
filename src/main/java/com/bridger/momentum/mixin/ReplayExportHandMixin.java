package com.bridger.momentum.mixin;
import com.bridger.momentum.*;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(C_03140863.class)
public final class ReplayExportHandMixin {
 // Lunar/ReplayMod can replace this field read in EntityRenderer. Keep their
 // hand rendering when the export-only override has no compatible target.
 @Redirect(method="m_38540407",at=@At(value="FIELD",target="Lnet/minecraft/unmapped/C_76792751;f_06955029:Z",opcode=180),require=0)
 private boolean bridgetap$exportHand(C_76792751 settings){return ReplayVideoPresentation.hideHand(settings.f_06955029,ReplayVideoExport.active()&&ReplaySession.rendering(),ReplaySession.povLocked());}
}
