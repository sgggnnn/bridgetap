package com.bridger.momentum;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Render-only camera; its mode must not follow the live player's tab-list game mode. */
public final class ReplayCamera extends C_57778754 {
 public ReplayCamera(Minecraft mc,C_34399650 world,C_88014908 handler){super(mc,world,handler,new C_00535600());}
 @Override public boolean m_13836848(){return false;}
}
