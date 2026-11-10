package com.bridger.momentum;

/** Replay navigation, playback speed and exit; all other slots are unused. */
public final class ReplayControls {
 public static final int BACK=0,CENTER=1,FORWARD=2,SPEED=3,EXIT=8;
 public enum Center {PAUSE,PLAY,REPLAY}
 public static int[] slots(){return new int[]{BACK,CENTER,FORWARD,SPEED,EXIT};}
 public static boolean isTool(int slot){return slot==BACK||slot==CENTER||slot==FORWARD||slot==SPEED||slot==EXIT;}
 public static Center center(ReplayTimeline timeline){return timeline.finished()?Center.REPLAY:timeline.playing()?Center.PAUSE:Center.PLAY;}
 public static int item(int slot,ReplayTimeline timeline){if(slot==BACK||slot==FORWARD)return 280;if(slot==SPEED)return 347;if(slot==EXIT)return 166;if(slot==CENTER)return center(timeline)==Center.REPLAY?262:341;return 0;}
 public static String label(int slot,ReplayTimeline timeline){
  if(slot==BACK)return "\u00a7eBackward: 1s playing / 1 tick paused";
  if(slot==FORWARD)return "\u00a7eForward: 1s playing / 1 tick paused";
  if(slot==EXIT)return "\u00a7cExit replay";
  if(slot==SPEED)return String.format(java.util.Locale.US,"\u00a7bPlayback speed: %.2fx",timeline.speed());
  return "\u00a7a"+(center(timeline)==Center.REPLAY?"Replay from beginning":center(timeline)==Center.PAUSE?"Pause":"Play");
 }
 public static int scroll(int current,int delta){
  if(delta==0)return current;int step=delta>0?-1:1;
  for(int i=0;i<9;i++){current=Math.floorMod(current+step,9);if(isTool(current))return current;}return CENTER;
 }
}
