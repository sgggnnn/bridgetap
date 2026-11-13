package com.bridger.momentum.mixin;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_81137139;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Minecraft.class)
public interface AppearanceResourceAccessor {
 @Accessor("f_78193359") List<C_81137139> bridgetap$resourcePacks();
}
