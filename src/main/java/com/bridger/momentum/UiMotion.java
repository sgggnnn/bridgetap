package com.bridger.momentum;
import java.util.*;
/** Bounded per-screen animation state, frame-rate independent and gameplay-free. */
final class UiMotion {
 private final Map<String,Float> values=new HashMap<>();private long previous;private float dt=1f/60;
 void frame(long now){if(previous!=0)dt=(float)Math.max(0,Math.min(.05,(now-previous)/1e9));previous=now;if(values.size()>2048)values.clear();}
 float value(String key,float target,float speed){float old=values.getOrDefault(key,0f),next=old+(target-old)*(1-(float)Math.exp(-speed*dt));if(Math.abs(next-target)<.001)next=target;values.put(key,next);return next;}
}
