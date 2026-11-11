package com.bridger.momentum.mixin;

import com.bridger.momentum.BridgeHUD;
import com.bridger.momentum.BridgeTracker;
import com.bridger.momentum.ReplaySession;
import com.bridger.momentum.BridgeHistoryScreen;
import com.bridger.momentum.BridgeTelemetry;
import com.bridger.momentum.ReplayRecorder;
import com.bridger.momentum.IslandOverlay;
import com.bridger.momentum.ClipCapture;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import net.minecraft.unmapped.*;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "m_67975642", at = @At("HEAD"))
    private void bridgetap$onRunTick(CallbackInfo ci) {
        BridgeTelemetry.beginTick();
        ReplaySession.tickHead();
        IslandOverlay.tick();
        BridgeHUD.onClientTick();
        BridgeTracker.onTick((Minecraft) (Object) this);
    }

    @Inject(method = "m_67975642", at = @At("RETURN"))
    private void bridgetap$afterTick(CallbackInfo ci) {
        ReplaySession.tickTail();
        if(!ReplaySession.active() && BridgeTracker.isBridging())ReplayRecorder.sample();
    }

    @Inject(method="m_67975642",at=@At(value="INVOKE",target="Lorg/lwjgl/input/Mouse;getEventButton()I",ordinal=0))
    private void bridgetap$rawMouse(CallbackInfo ci) {
        int button=Mouse.getEventButton();if(button>=0)BridgeTelemetry.input(button-100,Mouse.getEventButtonState());
    }

    @Inject(method="m_67975642",at=@At(value="INVOKE",target="Lorg/lwjgl/input/Keyboard;getEventKey()I",ordinal=0))
    private void bridgetap$rawKeyboard(CallbackInfo ci) { BridgeTelemetry.input(Keyboard.getEventKey(),Keyboard.getEventKeyState()); }

    @Redirect(method = "m_97212553", at = @At(value = "INVOKE", target = "Lnet/minecraft/unmapped/C_03140863;m_08357947(FJ)V"))
    private void bridgetap$renderReplay(C_03140863 renderer,float partial,long deadline) {
        try { ReplaySession.beginRender(partial); renderer.m_08357947(partial,deadline); }
        finally { ReplaySession.endRender(); }
    }

    @Inject(method = "m_16966855", at = @At("HEAD"), cancellable = true)
    private void bridgetap$replayAttack(CallbackInfo ci) {
        if (ReplaySession.active()) { ReplaySession.action(true); ci.cancel(); }
    }
    @Inject(method = "m_61857590", at = @At("HEAD"), cancellable = true)
    private void bridgetap$replayUse(CallbackInfo ci) {
        if (ReplaySession.active()) { ReplaySession.action(false); ci.cancel(); }
    }

    @Inject(method = "m_77225877", at = @At("HEAD"), cancellable = true)
    private void bridgetap$noMining(boolean down,CallbackInfo ci) { if (ReplaySession.active()) ci.cancel(); }

    @Inject(method = "m_20685309", at = @At("HEAD"), cancellable = true)
    private void bridgetap$noPicking(CallbackInfo ci) { if (ReplaySession.active()) ci.cancel(); }

    @Inject(method = "m_52715402", at = @At("HEAD"), cancellable = true)
    private void bridgetap$keepReplayOpen(C_85740840 screen,CallbackInfo ci) {
        // Keep the native pause menu available. Only the live inventory must be
        // blocked: replay controls belong to the private replay camera inventory.
        if (ReplaySession.active() && screen instanceof C_87711245) {
            ci.cancel();
        }
    }

    @Inject(method="m_66101954",at=@At("HEAD"))
    private void bridgetap$captureVideo(CallbackInfo ci){ClipCapture.capture();}

    @Inject(method = "m_49232044", at = @At("HEAD"))
    private void bridgetap$leaveWorld(C_34399650 world,String message,CallbackInfo ci) { ReplaySession.stop(); }
}
