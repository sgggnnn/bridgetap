package com.bridger.momentum;

/** Fixed route-relative follow offset: looking around does not spin the camera. */
public final class ReplayVideoCamera {
    public static double[] follow(ReplayData.Frame actor,ReplayData.Frame first,ReplayData.Frame last){double dx=last.x-first.x,dz=last.z-first.z,length=Math.hypot(dx,dz);if(length<.1){dx=-Math.sin(Math.toRadians(first.yaw));dz=Math.cos(Math.toRadians(first.yaw));}else{dx/=length;dz/=length;}double x=actor.x-dx*3.5-dz*4,z=actor.z-dz*3.5+dx*4,y=actor.y+3;double tx=actor.x-x,tz=actor.z-z;return new double[]{x,y,z,Math.toDegrees(Math.atan2(-tx,tz)),Math.toDegrees(Math.atan2(1.7,Math.hypot(tx,tz)))};}
}
