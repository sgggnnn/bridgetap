package com.bridger.momentum.mixin;
import com.bridger.momentum.PlacementAppearance;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Cached models, item tinting and built-in chest/skull renderers share this path. */
@Mixin(C_84615110.class)
public abstract class AppearanceRenderItemMixin {
 @Unique private static final ThreadLocal<Boolean> bridgetap$rendering=ThreadLocal.withInitial(()->false);
 @Shadow public abstract void m_86893883(C_88708284 stack,C_41102159 model);
 @Inject(method="m_86893883",at=@At("HEAD"),cancellable=true)
 private void bridgetap$renderAppearance(C_88708284 stack,C_41102159 supplied,CallbackInfo ci){if(!PlacementAppearance.enabled()||bridgetap$rendering.get())return;C_41102159 model=PlacementAppearance.itemModel(Minecraft.m_20213497().m_84598725().m_08567970(),stack);if(model==null)return;bridgetap$rendering.set(true);try{m_86893883(PlacementAppearance.visualStack(stack),model);}finally{bridgetap$rendering.set(false);}ci.cancel();}
}
