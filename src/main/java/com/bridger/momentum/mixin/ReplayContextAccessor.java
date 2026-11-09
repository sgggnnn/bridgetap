package com.bridger.momentum.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_42232651;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface ReplayContextAccessor {
 // Minecraft.setCamera also deletes/reloads post-processing shaders. A render-only
 // context swap must change only the entity reference and restore it in finally.
 @Accessor("f_63700667") void bridgetap$setRenderCamera(C_42232651 camera);
}
