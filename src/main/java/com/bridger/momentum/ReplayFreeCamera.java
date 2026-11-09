package com.bridger.momentum;

/** Authoritative camera movement, advanced by client ticks independently of replay time. */
public final class ReplayFreeCamera {
 public static final double SPEED=12,SPRINT_SPEED=36;
 private double x,y,z,previousX,previousY,previousZ;
 public void reset(double x,double y,double z){this.x=previousX=x;this.y=previousY=y;this.z=previousZ=z;}
 public void step(double seconds,float yaw,double forward,double right,double up,boolean fast){
  previousX=x;previousY=y;previousZ=z;
  double length=Math.sqrt(forward*forward+right*right+up*up);
  if(length>1){forward/=length;right/=length;up/=length;}
  double distance=Math.max(0,Math.min(.25,seconds))*(fast?SPRINT_SPEED:SPEED),angle=Math.toRadians(yaw);
  // At yaw 0 the viewer faces south (+Z), so screen-right is west (-X).
  x+=(-Math.sin(angle)*forward-Math.cos(angle)*right)*distance;
  y+=up*distance;
  z+=(Math.cos(angle)*forward-Math.sin(angle)*right)*distance;
 }
 public double x(float alpha){return lerp(previousX,x,alpha);}
 public double y(float alpha){return lerp(previousY,y,alpha);}
 public double z(float alpha){return lerp(previousZ,z,alpha);}
 private static double lerp(double a,double b,float alpha){return a+(b-a)*Math.max(0,Math.min(1,alpha));}
}
