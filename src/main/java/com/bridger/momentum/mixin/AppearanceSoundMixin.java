package com.bridger.momentum.mixin;
import com.bridger.momentum.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(C_71827890.class)
public final class AppearanceSoundMixin {
 @Redirect(method="m_93921076",at=@At(value="INVOKE",target="Lnet/minecraft/unmapped/C_64041439;m_14911682(DDDLjava/lang/String;FF)V"))
 private void bridgetap$placementSound(C_64041439 world,double x,double y,double z,String original,float volume,float pitch){if(!PlacementAppearance.enabled()||!PlacementAppearance.placing())world.m_14911682(x,y,z,original,volume,pitch);}
}
