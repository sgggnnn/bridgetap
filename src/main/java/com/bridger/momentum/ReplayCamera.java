package com.bridger.momentum;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Render-only camera; its mode must not follow the live player's tab-list game mode. */
public final class ReplayCamera extends C_57778754 {
 public ReplayCamera(Minecraft mc,C_34399650 world,C_88014908 handler){super(mc,world,handler,new C_00535600());}
 @Override public boolean m_13836848(){return false;}
 @Override public void m_74052865(float yaw,float pitch){if(!ReplaySession.povLocked())super.m_74052865(yaw,pitch);}
 @Override public C_22168528 m_68930603(){ReplayGhost actor=ReplaySession.povActor();return actor==null?super.m_68930603():actor.m_68930603();}
 @Override public String m_63406370(){ReplayGhost actor=ReplaySession.povActor();return actor==null?super.m_63406370():actor.m_63406370();}
}
