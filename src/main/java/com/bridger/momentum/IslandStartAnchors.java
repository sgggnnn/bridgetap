package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** A known start location, never whichever island is closest to the spectator. */
public final class IslandStartAnchors {
    private static Object world;private static PresetServers.Anchor current;
    public static void runStart(){Minecraft mc=Minecraft.m_20213497();if(mc==null||mc.f_28396371==null)return;world=mc.f_26407825;current=new PresetServers.Anchor((int)Math.floor(mc.f_28396371.f_78826675),(int)Math.max(0,Math.min(255,Math.floor(mc.f_28396371.f_31030949)-1)),(int)Math.floor(mc.f_28396371.f_97691941));}
    private static PresetServers.Anchor known()throws java.io.IOException{Minecraft mc=Minecraft.m_20213497();if(mc==null||mc.f_26407825==null)return null;if(world==mc.f_26407825&&current!=null)return current;return ServerPresets.bindings().anchor(ServerPresets.address());}
    public static boolean available(){try{return ReplaySession.active()||known()!=null;}catch(java.io.IOException error){return false;}}
    public static void mark()throws java.io.IOException{Minecraft mc=Minecraft.m_20213497();if(ReplaySession.active())throw new java.io.IOException("Leave the replay before marking a live start island");if(mc==null||mc.f_28396371==null||mc.f_26407825==null)throw new java.io.IOException("Join a world first");if(BridgeTracker.isPlayerOnGoldPlate(mc))throw new java.io.IOException("This is the finish plate. Stand on the start island.");IslandReplacement.Mask start=IslandReplacement.captureStartAt((int)Math.floor(mc.f_28396371.f_78826675),(int)Math.floor(mc.f_28396371.f_31030949)-1,(int)Math.floor(mc.f_28396371.f_97691941));if(start.empty())throw new java.io.IOException("Stand on the start island first");current=new PresetServers.Anchor(start.attachX,start.surface-1,start.attachZ);world=mc.f_26407825;ServerPresets.bindings().setAnchor(ServerPresets.address(),current);}
    public static IslandReplacement.Mask resolve(IslandTemplate template,IslandLayout layout){
        PresetServers.Anchor anchor;
        if(ReplaySession.active()){ReplayData.Frame first=ReplaySession.recording().frames.get(0);anchor=new PresetServers.Anchor((int)Math.floor(first.x),(int)Math.max(0,Math.min(255,Math.floor(first.y)-1)),(int)Math.floor(first.z));}
        else try{anchor=known();}catch(java.io.IOException error){throw new IllegalArgumentException("Could not load the start-island anchor",error);}
        if(anchor==null)throw new IllegalArgumentException("Start island unknown. Stand there and click Set start island.");
        IslandReplacement.Mask start=IslandReplacement.captureStartAt(anchor.x,anchor.y,anchor.z);
        if(start.empty())throw new IllegalArgumentException("The marked start island is not loaded. No preset was placed.");
        if(!ReplaySession.active()){
            C_57778754 viewer=ReplaySession.viewer();IslandParts parts=new IslandParts(template);double dx=0,dz=0;
            if(parts.paired){int[] a=parts.bounds[0],b=parts.bounds[1];dx=(b[0]+b[3]-a[0]-a[3])/2.0+layout.finishX-layout.startX;dz=(b[2]+b[5]-a[2]-a[5])/2.0+layout.finishZ-layout.startZ;for(int i=0;i<layout.rotation;i++){double next=-dz;dz=dx;dx=next;}}
            double cx=(start.minX+start.maxX+1)/2.0,cz=(start.minZ+start.maxZ+1)/2.0,radius=Math.max(8,(start.maxX-start.minX+start.maxZ-start.minZ+2)/2.0+4);
            if(!IslandAnchorPolicy.inLane(viewer.f_78826675-cx,viewer.f_97691941-cz,dx,dz,radius))throw new IllegalArgumentException("This is another lane. Mark its start island before applying.");
        }
        if(!ReplaySession.active())try{ServerPresets.bindings().setAnchor(ServerPresets.address(),new PresetServers.Anchor(start.attachX,start.surface-1,start.attachZ));}catch(java.io.IOException ignored){}
        return IslandReplacement.presetScene(start);
    }
    public static boolean inLane(double x,double z,double dx,double dz,double radius){double length=Math.hypot(dx,dz);return length<.1?Math.hypot(x,z)<=radius:Math.abs(x*dz-z*dx)/length<=radius;}
}
