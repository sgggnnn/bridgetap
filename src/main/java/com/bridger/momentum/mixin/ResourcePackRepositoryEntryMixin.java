package com.bridger.momentum.mixin;

import com.bridger.momentum.SkyOverlayCreator;
import java.io.File;
import net.minecraft.unmapped.C_02398648;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C_02398648.C_53553981.class)
public class ResourcePackRepositoryEntryMixin {
    @Shadow private File f_17750045;
    @Shadow private C_02398648 f_70116942;

    @Inject(method = "m_29195402", at = @At("RETURN"), cancellable = true)
    private void bridgetap$prefixFolderCategory(CallbackInfoReturnable<String> cir) {
        if (this.f_17750045 != null && this.f_70116942 != null && this.f_70116942.m_08141491() != null) {
            String folder = SkyOverlayCreator.getFolderPrefix(this.f_70116942.m_08141491(), this.f_17750045);
            if (folder != null && !folder.isEmpty()) {
                String original = cir.getReturnValue();
                cir.setReturnValue("\u00a76[\u00a7e" + folder + "\u00a76] \u00a77" + (original == null ? "" : original));
            }
        }
    }
}
