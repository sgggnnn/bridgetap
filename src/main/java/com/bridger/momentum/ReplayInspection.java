package com.bridger.momentum;

/** Includes skin/arm geometry and a small near-plane margin around the actor. */
public final class ReplayInspection {
 private ReplayInspection(){}
 public static boolean eyeInside(ReplayData.Frame actor,double x,double y,double z){
  return Math.abs(x-actor.x)<=.55&&Math.abs(z-actor.z)<=.45&&y>=actor.y-.1&&y<=actor.y+1.95;
 }
}
