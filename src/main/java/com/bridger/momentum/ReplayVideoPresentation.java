package com.bridger.momentum;
/** Keep the first-person item animation even when the export hides other HUD elements. */
public final class ReplayVideoPresentation {
 private ReplayVideoPresentation(){}
 public static boolean hideHand(boolean nativeHidden,boolean exporting,boolean pov){return exporting?!pov:nativeHidden;}
}
