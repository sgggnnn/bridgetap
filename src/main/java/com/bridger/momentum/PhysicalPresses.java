package com.bridger.momentum;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
/** Repeated native callbacks and held-key repeats are not additional physical clicks. */
public final class PhysicalPresses {
 private final Set<Integer> held=new HashSet<>();
 private final Map<Integer,Integer> queued=new HashMap<>();
 public boolean event(int code,boolean down){if(!down){held.remove(code);return false;}return held.add(code);}
 public void enqueue(int code){queued.put(code,Math.min(512,queued.getOrDefault(code,0)+1));}
 public boolean consume(int code){int n=queued.getOrDefault(code,0);if(n==0)return false;if(n==1)queued.remove(code);else queued.put(code,n-1);return true;}
 public void clear(){held.clear();queued.clear();}
}
