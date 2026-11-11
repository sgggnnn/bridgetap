package com.bridger.momentum;
import java.util.*;

/** Commits parallel compression results in capture order, discarding old generations. */
public final class ClipFrameOrder {
    private final ClipFrames buffer;private final Map<Long,ClipFrames.Frame> completed=new TreeMap<>();private long generation,next;
    public ClipFrameOrder(ClipFrames buffer){this.buffer=buffer;}
    public synchronized void reset(long epoch){generation=epoch;next=0;completed.clear();}
    public synchronized void complete(long epoch,long sequence,long time,byte[] image){if(epoch!=generation)return;completed.put(sequence,new ClipFrames.Frame(time,image));while(completed.containsKey(next))buffer.add(completed.remove(next++));}
    synchronized void owned(long epoch,long sequence,long time,byte[] image){if(epoch!=generation)return;completed.put(sequence,ClipFrames.Frame.owned(time,image));while(completed.containsKey(next))buffer.add(completed.remove(next++));}
}
