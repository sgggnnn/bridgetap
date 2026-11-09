package com.bridger.momentum.mixin;
import net.minecraft.unmapped.*;
import java.util.List;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C_24384787.class)
public interface ReplayRendererAccessor {
 @Accessor("f_46495713") C_61390454 bridgetap$getChunkStorage();
 @Accessor("f_46495713") void bridgetap$setChunkStorage(C_61390454 storage);
 @Accessor("f_43838812") C_17334338 bridgetap$getDispatcher();
 @Accessor("f_68578288") List<?> bridgetap$getCompiledChunks();
 @Accessor("f_84097449") Set<?> bridgetap$getDirtyChunks();
 @Accessor("f_00648327") Set<?> bridgetap$getBlockEntities();
}
