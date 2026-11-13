package com.bridger.momentum.mixin;
import com.bridger.momentum.*;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(C_57103744.class)
public final class ModernBlockRenderMixin {
 @Inject(method="m_79220206",at=@At("RETURN"),cancellable=true) private void bridgetap$modernModel(C_15815210 state,C_46108969 access,C_32594356 pos,CallbackInfoReturnable<C_41102159> ci){
  if(PlacementAppearance.enabled()&&PlacementAppearance.block().startsWith("modern:")&&PlacementAppearance.matches(pos,state)&&!IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936())){C_41102159 model=ModernAppearance.model();if(model!=null)ci.setReturnValue(model);}
 }
 @Inject(method="m_04667098",at=@At("HEAD"),cancellable=true) private void bridgetap$modernBlock(C_15815210 state,C_32594356 pos,C_46108969 access,C_93396801 buffer,CallbackInfoReturnable<Boolean> ci){
  if(!PlacementAppearance.enabled()||!PlacementAppearance.block().startsWith("modern:")||!PlacementAppearance.matches(pos,state)||IslandReplacement.hidden(pos.m_32364273(),pos.m_62825721(),pos.m_23514936()))return;
  C_41102159 model=ModernAppearance.model();if(model!=null)ci.setReturnValue(((C_57103744)(Object)this).m_52219152().m_10351618(access,model,state,pos,buffer,true));
 }
}
