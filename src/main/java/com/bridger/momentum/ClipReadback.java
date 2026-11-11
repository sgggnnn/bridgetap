package com.bridger.momentum;

import java.nio.ByteBuffer;
import java.util.*;
import org.lwjgl.opengl.*;

/** Three GPU transfer slots. Never wait for a fence on Minecraft's render thread. */
final class ClipReadback {
    interface Receiver{void frame(long epoch,long time,byte[] rgb,int width,int height);}
    private static final class Slot{int buffer,width,height;long epoch,time;Object fence;}
    private ClipGpuSync sync;
    private final Deque<Slot> transfers=new ArrayDeque<>(),free=new ArrayDeque<>();
    boolean available(){return transfers.size()<3;}
    void enqueue(int width,int height,long epoch,long time,Runnable read){
        if(sync==null)sync=new ClipGpuSync(GL32.class);
        if(!available())return;int previous=GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);Slot slot=free.pollFirst();if(slot==null){slot=new Slot();slot.buffer=GL15.glGenBuffers();}
        try{
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,slot.buffer);if(slot.width!=width||slot.height!=height){GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER,(long)width*height*3,GL15.GL_STREAM_READ);slot.width=width;slot.height=height;}
            read.run();slot.fence=sync.create();slot.epoch=epoch;slot.time=time;transfers.addLast(slot);
        }catch(RuntimeException|LinkageError ex){GL15.glDeleteBuffers(slot.buffer);throw ex;}
        finally{GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,previous);}
    }
    void poll(long epoch,Receiver receiver){
        while(!transfers.isEmpty()){
            Slot slot=transfers.peekFirst();int result=sync.poll(slot.fence);
            if(result==GL32.GL_TIMEOUT_EXPIRED)return;
            if(result==GL32.GL_WAIT_FAILED)throw new IllegalStateException("Video GPU transfer failed");
            byte[] rgb=slot.epoch==epoch?ClipRgbPool.acquire(slot.width*slot.height*3):null;if(slot.epoch==epoch&&rgb==null)return;
            transfers.removeFirst();sync.delete(slot.fence);slot.fence=null;
            boolean delivered=false;
            try{
                if(slot.epoch!=epoch)continue;
                int previous=GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);
                try{GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,slot.buffer);ByteBuffer mapped=GL15.glMapBuffer(GL21.GL_PIXEL_PACK_BUFFER,GL15.GL_READ_ONLY,rgb.length,null);if(mapped==null)throw new IllegalStateException("Video transfer could not be mapped");
                    try{mapped.get(rgb);}finally{if(!GL15.glUnmapBuffer(GL21.GL_PIXEL_PACK_BUFFER))throw new IllegalStateException("Video transfer was corrupted");}
                }finally{GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,previous);}
                receiver.frame(slot.epoch,slot.time,rgb,slot.width,slot.height);delivered=true;return;
            }finally{if(!delivered)ClipRgbPool.release(rgb);free.addLast(slot);}
        }
    }
    void release(){try{for(Slot slot:transfers){try{if(slot.fence!=null)sync.delete(slot.fence);}finally{GL15.glDeleteBuffers(slot.buffer);}}for(Slot slot:free)GL15.glDeleteBuffers(slot.buffer);}finally{transfers.clear();free.clear();}}
}
