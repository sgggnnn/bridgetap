package com.bridger.momentum;
/** Twelve-tick turns subtract 100 thousandths, never 100 world blocks. */
public final class TurnCoordinates {
 private TurnCoordinates(){}
 public static double decimal(double coordinate,int ticks){
  double fraction=Math.abs(coordinate)-Math.floor(Math.abs(coordinate));
  double adjusted=fraction-(ticks==12?.100:0);
  return adjusted-Math.floor(adjusted);
 }
}
