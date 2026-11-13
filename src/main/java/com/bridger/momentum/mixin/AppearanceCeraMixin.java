package com.bridger.momentum.mixin;
import com.bridger.momentum.PlacementAppearance;
import net.minecraft.unmapped.*;
import java.util.List;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** CTM rules match the server's sandstone and otherwise replace our imported quads. */
@Pseudo
@Mixin(targets="dev.rdh.cera.modules.ctm.ConnectedTextures",remap=false)
public final class AppearanceCeraMixin {
 @Inject(method="transform",at=@At("HEAD"),cancellable=true,remap=false)
 private void bridgetap$preserveAppearance(C_46108969 access,C_15815210 state,C_32594356 pos,List<C_85259637> quads,List<?> overlays,@Coerce Object context,CallbackInfoReturnable<List<C_85259637>> ci){if(PlacementAppearance.enabled()&&PlacementAppearance.matches(pos,state))ci.setReturnValue(quads);}
}
