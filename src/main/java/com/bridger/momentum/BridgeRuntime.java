package com.bridger.momentum;

/** Master switch: keep user configuration, stop active client-only sessions. */
public final class BridgeRuntime {
 private static boolean applied=true;
 public static void setEnabled(boolean enabled){BridgeConfig.modEnabled=enabled;tick();BridgeConfig.save();}
 public static void tick(){if(applied==BridgeConfig.modEnabled)return;applied=BridgeConfig.modEnabled;
  if(!applied){ReplaySession.stop();BridgeTracker.reset();IslandOverlay.disable();BridgeTelemetry.clearActionPresses();if(ClipCapture.enabled())ClipCapture.toggle();}
  IslandReplacement.requestTerrainRebuild();IslandReplacement.refreshTerrain();
 }
}
