package com.bridger.momentum.mixin;

import com.bridger.momentum.SkyOverlayCreator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.unmapped.C_02398648;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(C_02398648.class)
public class ResourcePackRepositoryMixin {
    @Shadow private File f_30799771;

    @Inject(method = "m_30972366", at = @At("HEAD"), cancellable = true)
    private void bridgetap$discoverSubfolderPacks(CallbackInfoReturnable<List<File>> cir) {
        if (this.f_30799771 != null && this.f_30799771.isDirectory()) {
            List<File> list = new ArrayList<>();
            SkyOverlayCreator.findPacksRecursively(this.f_30799771, list, 0);
            SkyOverlayCreator.sortPacksByFolder(this.f_30799771, list);
            cir.setReturnValue(list);
        }
    }
}
