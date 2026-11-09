package com.bridger.momentum.mixin;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C_34267874.class)
public interface ReplayControllerAccessor {
 // setGameMode applies abilities to Minecraft.player, which is the live player
 // when a replay is constructed. Configure only this private controller's field.
 @Accessor("f_14768439") void bridgetap$setReplayMode(C_06007608.C_36868821 mode);
}
