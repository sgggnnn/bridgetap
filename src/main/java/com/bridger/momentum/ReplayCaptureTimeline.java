package com.bridger.momentum;
import java.util.*;
/** Tick-boundary poses and in-tick events share one origin; frame zero is never overwritten by tick end. */
public final class ReplayCaptureTimeline {
 private final List<ReplayData.Frame> frames=new ArrayList<>();private int origin;private boolean active,truncated;
 public List<ReplayData.Frame> frames(){return Collections.unmodifiableList(frames);}
 public void clear(){frames.clear();active=false;truncated=false;}
 public void start(int tick){clear();origin=tick;active=true;}
 public int eventTick(int tick){if(!active||tick<origin)throw new IllegalStateException("Recording clock is not active");return tick-origin;}
 public boolean truncated(){return truncated;}
 private boolean write(int index,ReplayData.Frame frame){
  if(index>=ReplayData.MAX_FRAMES){truncated=true;return false;}
  if(index>frames.size()){truncated=true;while(frames.size()<index)frames.add(frames.isEmpty()?frame:frames.get(frames.size()-1));}
  if(index==frames.size())frames.add(frame);else frames.set(index,frame);return true;
 }
 public boolean boundary(int tick,ReplayData.Frame frame){return write(eventTick(tick),frame);}
 public boolean endTick(int tick,ReplayData.Frame frame){
  int index=eventTick(tick);if(index>=ReplayData.MAX_FRAMES-1){truncated=true;return false;}
  if(index>=frames.size()){truncated=true;write(index,frames.isEmpty()?frame:frames.get(frames.size()-1));}
  ReplayData.Frame start=frames.get(index);frames.set(index,start.withStats(frame.stats));
  ReplayData.TickStats s=frame.stats;
  ReplayData.TickStats idle=s==null?null:s.withoutEdges();
  return write(index+1,frame.withStats(idle));
 }
}
