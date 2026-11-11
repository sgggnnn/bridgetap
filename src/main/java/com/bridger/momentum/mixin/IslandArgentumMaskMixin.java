package com.bridger.momentum.mixin;
import com.bridger.momentum.IslandReplacement;
import com.bridger.momentum.PlacementAppearance;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Argentum/Celeritas meshes its own snapshot, bypassing BlockRenderDispatcher. */
@Pseudo
@Mixin(targets="dev.rdh.argentum.impl.world.cloned.ChunkRenderContext",remap=false)
public final class IslandArgentumMaskMixin {
    @Unique private int bridgetap$originX,bridgetap$originY,bridgetap$originZ;
    @Unique private boolean bridgetap$originKnown;
    @Unique private void bridgetap$readOrigin(){
        if(!bridgetap$originKnown)try{
            Object origin=getClass().getMethod("origin").invoke(this);Class<?> type=origin.getClass();
            bridgetap$originX=((Number)type.getMethod("minX").invoke(origin)).intValue();bridgetap$originY=((Number)type.getMethod("minY").invoke(origin)).intValue();bridgetap$originZ=((Number)type.getMethod("minZ").invoke(origin)).intValue();bridgetap$originKnown=true;
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("Unsupported Argentum section coordinates",ex);}
    }
    @Inject(method="getOriginBlockState",at=@At("HEAD"),cancellable=true,remap=false)
    private void bridgetap$maskOrigin(int x,int y,int z,CallbackInfoReturnable<C_15815210> ci){
        if(!IslandReplacement.masking())return;
        bridgetap$readOrigin();
        if(IslandReplacement.hidden(bridgetap$originX+x,bridgetap$originY+y,bridgetap$originZ+z))ci.setReturnValue(C_81592558.m_64320102(0).m_52891069());
    }
    @Inject(method="getBlockState",at=@At("HEAD"),cancellable=true,remap=false)
    private void bridgetap$maskAbsolute(int x,int y,int z,CallbackInfoReturnable<C_15815210> ci){if(IslandReplacement.hidden(x,y,z))ci.setReturnValue(C_81592558.m_64320102(0).m_52891069());}
    @Inject(method="getBlockState",at=@At("RETURN"),cancellable=true,remap=false)
    private void bridgetap$placedAbsolute(int x,int y,int z,CallbackInfoReturnable<C_15815210> ci){if(!PlacementAppearance.enabled())return;C_15815210 replacement=PlacementAppearance.replacement(new C_32594356(x,y,z),ci.getReturnValue());if(replacement!=null)ci.setReturnValue(replacement);}
    @Inject(method="getOriginBlockState",at=@At("RETURN"),cancellable=true,remap=false)
    private void bridgetap$placedLocal(int x,int y,int z,CallbackInfoReturnable<C_15815210> ci){if(!PlacementAppearance.enabled())return;bridgetap$readOrigin();C_15815210 replacement=PlacementAppearance.replacement(new C_32594356(bridgetap$originX+x,bridgetap$originY+y,bridgetap$originZ+z),ci.getReturnValue());if(replacement!=null)ci.setReturnValue(replacement);}
}
