package com.bridger.momentum;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_43718703;

/** Live controls share the replay keystrokes style and HUD placement. */
final class LiveInputOverlay {
 static void render(Minecraft mc){
  if(!BridgeConfig.showLiveInputs||mc.f_21497584==null||mc.f_58088711.f_06955029||BridgeConfig.hideOnF3&&mc.f_58088711.f_63869242)return;
  ReplayData.TickStats inputs=BridgeTelemetry.sample(mc.f_28396371);
  C_43718703 window=new C_43718703(mc);int h=ReplayInputOverlay.height(inputs);
  OneClientIntegration.recordSize(2,76,h);
  float scale=(float)Math.min(BridgeConfig.inputHudScale,Math.min((window.m_39119333()-8f)/76,(window.m_16103615()-8f)/h));
  int x=OverlayPlacement.clamp(BridgeConfig.inputHudX,(int)Math.ceil(76*scale),window.m_39119333());
  int y=OverlayPlacement.clamp(BridgeConfig.inputHudY,(int)Math.ceil(h*scale),window.m_16103615());
  ReplayInputOverlay.paint(new ScaledUiCanvas(new MinecraftUiCanvas(mc.f_21497584,2),x,y,scale),0,0,inputs);
 }
}
