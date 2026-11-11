package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

public final class FocusAudio {
    public static boolean suppress(C_53396390 sound){
        if(!IslandReplacement.focus()||!IslandOverlay.enabled()||sound==null||sound.m_85273192()==C_53396390.C_94767121.f_76649173)return false;
        Minecraft mc=Minecraft.m_20213497();if(mc==null||mc.f_28396371==null||mc.f_26407825==null)return false;
        double x=sound.m_25912748(),y=sound.m_37489221(),z=sound.m_34342839();
        C_40996800 own=mc.f_28396371;double distance=distance(x,y,z,own),other=Double.POSITIVE_INFINITY;
        if(mc.f_26407825.f_51003174!=null)for(Object candidate:mc.f_26407825.f_51003174){
            if(candidate instanceof C_40996800&&candidate!=own&&!(candidate instanceof ReplayGhost)&&candidate!=ReplaySession.viewer())other=Math.min(other,distance(x,y,z,(C_40996800)candidate));
        }
        return FocusAudioRule.mute(true,true,distance,other);
    }
    private static double distance(double x,double y,double z,C_40996800 player){double dx=x-player.f_78826675,dy=y-player.f_31030949,dz=z-player.f_97691941;return dx*dx+dy*dy+dz*dz;}
}
