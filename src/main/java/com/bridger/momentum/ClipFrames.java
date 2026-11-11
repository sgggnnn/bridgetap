package com.bridger.momentum;

import java.util.*;

/** Bounded lossless-image ring, shared between capture and export snapshots. */
public final class ClipFrames {
    public static final long MAX_BYTES=128L*1024*1024;
    public static final class Frame {
        public final long time;private final byte[] jpeg;
        public Frame(long time,byte[] jpeg){this.time=time;this.jpeg=jpeg.clone();}
        private Frame(long time,byte[] image,boolean owned){this.time=time;this.jpeg=image;}
        static Frame owned(long time,byte[] image){return new Frame(time,image,true);}
        public byte[] image(){return jpeg.clone();}
        public int size(){return jpeg.length;}
    }
    private final Deque<Frame> frames=new ArrayDeque<>();private long bytes;private int seconds=30;
    public synchronized void window(int value){if(value!=15&&value!=30&&value!=60)throw new IllegalArgumentException("Invalid buffer duration");seconds=value;clear();}
    public synchronized int window(){return seconds;}
    public synchronized void clear(){frames.clear();bytes=0;}
    private void trim(long now){while(!frames.isEmpty()&&(now-frames.peekFirst().time>seconds*1000000000L||bytes>MAX_BYTES)){bytes-=frames.removeFirst().size();}}
    public synchronized void add(long time,byte[] jpeg){
        if(jpeg==null||jpeg.length==0||jpeg.length>MAX_BYTES)return;
        if(!frames.isEmpty()&&time<=frames.peekLast().time)return;
        add(new Frame(time,jpeg));
    }
    synchronized void add(Frame frame){if(frame.size()==0||frame.size()>MAX_BYTES||(!frames.isEmpty()&&frame.time<=frames.peekLast().time))return;frames.addLast(frame);bytes+=frame.size();trim(frame.time);}
    public synchronized double rate(){return frames.size()<2?0:(frames.size()-1)/duration();}
    public synchronized List<Frame> snapshot(long now){trim(now);return Collections.unmodifiableList(new ArrayList<>(frames));}
    public synchronized long bytes(){return bytes;}
    public synchronized double duration(){return frames.size()<2?0:(frames.peekLast().time-frames.peekFirst().time)/1e9;}
}
