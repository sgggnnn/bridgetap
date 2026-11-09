package com.bridger.momentum.mixin;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C_35412243.class)
public interface ItemInHandAccessor {
 @Accessor("f_85016326") C_88708284 bridgetap$getItem();
 @Accessor("f_85016326") void bridgetap$setItem(C_88708284 item);
 @Accessor("f_47136511") float bridgetap$getHeight();
 @Accessor("f_47136511") void bridgetap$setHeight(float value);
 @Accessor("f_46631024") float bridgetap$getLastHeight();
 @Accessor("f_46631024") void bridgetap$setLastHeight(float value);
}
