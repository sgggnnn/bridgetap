package com.bridger.momentum;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Reuse large RGB arrays instead of allocating hundreds of MiB every second. */
public final class ClipRgbPool {
    private static final int LIMIT=8;private static final AtomicInteger allocated=new AtomicInteger();
    private static final ConcurrentHashMap<byte[],Boolean> owned=new ConcurrentHashMap<>();private static final ConcurrentLinkedQueue<byte[]> free=new ConcurrentLinkedQueue<>();
    public static byte[] acquire(int length){byte[] bytes;while((bytes=free.poll())!=null){if(bytes.length!=length){if(owned.remove(bytes,Boolean.FALSE))allocated.decrementAndGet();continue;}if(owned.replace(bytes,Boolean.FALSE,Boolean.TRUE))return bytes;}if(allocated.incrementAndGet()>LIMIT){allocated.decrementAndGet();return null;}try{bytes=new byte[length];owned.put(bytes,Boolean.TRUE);return bytes;}catch(Error ex){allocated.decrementAndGet();throw ex;}}
    public static void release(byte[] bytes){if(bytes!=null&&owned.replace(bytes,Boolean.TRUE,Boolean.FALSE))free.offer(bytes);}
}
