package com.bridger.momentum.mixin;
import net.minecraft.unmapped.C_97866173;
import net.minecraft.unmapped.C_51094326;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(C_97866173.class)
public interface ReplayGuiAccessor {
 @Accessor("f_86797423") void bridgetap$setTicks(int tick);
 @Mutable @Accessor("f_31347527") void bridgetap$setChat(C_51094326 chat);
}
