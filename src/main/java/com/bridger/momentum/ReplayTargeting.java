package com.bridger.momentum;

/** Ray against the recorded player's collision box, in Minecraft yaw/pitch coordinates. */
public final class ReplayTargeting {
 public static double[] direction(float yaw,float pitch){double y=Math.toRadians(yaw),p=Math.toRadians(pitch);return new double[]{-Math.sin(y)*Math.cos(p),-Math.sin(p),Math.cos(y)*Math.cos(p)};}
 public static double hit(ReplayData.Frame actor,double eyeX,double eyeY,double eyeZ,float yaw,float pitch,double reach){
  double[] origin={eyeX,eyeY,eyeZ},dir=direction(yaw,pitch),min={actor.x-.3,actor.y,actor.z-.3},max={actor.x+.3,actor.y+1.8,actor.z+.3};
  double near=0,far=reach;
  for(int i=0;i<3;i++){
   if(Math.abs(dir[i])<1e-9){if(origin[i]<min[i]||origin[i]>max[i])return Double.POSITIVE_INFINITY;continue;}
   double a=(min[i]-origin[i])/dir[i],b=(max[i]-origin[i])/dir[i];if(a>b){double swap=a;a=b;b=swap;}
   near=Math.max(near,a);far=Math.min(far,b);if(far<near)return Double.POSITIVE_INFINITY;
  }
  return near;
 }
}
