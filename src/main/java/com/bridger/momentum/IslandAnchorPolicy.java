package com.bridger.momentum;

/** Reject a different bridge lane while allowing observation from the finish. */
public final class IslandAnchorPolicy {
    public static boolean inLane(double x,double z,double dx,double dz,double radius){double length=Math.hypot(dx,dz);double along=length<.1?0:(x*dx+z*dz)/length;return length<.1?Math.hypot(x,z)<=radius:Math.abs(x*dz-z*dx)/length<=radius&&along>=-radius&&along<=length+radius;}
}
