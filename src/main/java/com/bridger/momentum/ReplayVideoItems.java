package com.bridger.momentum;

/** Resolve a construction block rather than the spectator's replay controls. */
public final class ReplayVideoItems {
    public static ReplayData.Block block(ReplayData data,double tick){if(data==null)return null;ReplayData.Block previous=null,first=null;for(ReplayData.Block b:data.blocks)if(b.placed&&b.id>0){if(first==null||b.tick<first.tick)first=b;if(ReplayTimeline.eventTick(data,b)<=tick&&(previous==null||b.tick>=previous.tick))previous=b;}return previous==null?first:previous;}
    public static String time(double savedSeconds,double duration){return String.format(java.util.Locale.US,"%.3f",Double.isFinite(savedSeconds)&&savedSeconds>=0?savedSeconds:duration);}
}
