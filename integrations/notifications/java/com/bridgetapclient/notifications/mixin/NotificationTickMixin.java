package com.bridgetapclient.notifications.mixin;

import com.bridgetapclient.notifications.Notifications;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class NotificationTickMixin {
    @Inject(method="m_67975642",at=@At("RETURN"))
    private void bridgetapclient$notifications(CallbackInfo info){Notifications.tick();}
}
