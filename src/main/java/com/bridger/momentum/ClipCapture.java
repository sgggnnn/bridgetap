package com.bridger.momentum;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_69136080;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Captures Minecraft's completed back buffer, never a desktop window. */
public final class ClipCapture {
    private static final ClipFrames buffer=new ClipFrames();
    private static final ClipFrameOrder ordered=new ClipFrameOrder(buffer);
    private static final int WORKERS=Math.max(1,Math.min(2,Runtime.getRuntime().availableProcessors()/4));
    private static final ClipReadback readback=new ClipReadback();
    private static final ClipCaptureMode mode=new ClipCaptureMode();
    private static final ClipStreamBuffer stream=NativeClipTools.windows()?new ClipStreamBuffer(Paths.get("config","bridgetap","clip-buffer")):null;
    private static final ExecutorService compression=Executors.newFixedThreadPool(WORKERS,r->{Thread t=new Thread(r,"BridgeTap frame compression");t.setDaemon(true);t.setPriority(Thread.NORM_PRIORITY-1);return t;}),exporter=worker("BridgeTap MP4 export");
    private static final AtomicInteger pending=new AtomicInteger();private static final AtomicBoolean saving=new AtomicBoolean();private static long sequence;
    private static volatile boolean enabled;private static volatile long generation;
    private static boolean sound=NativeClipTools.windows();
    private static volatile String message="Buffer off. Enable it before playing; F9 saves the recent video.";
    private static volatile String backend="Lossless RGB buffer / Waiting for capture";
    private static long next,last;private static int fps=15,maxHeight=ClipQuality.DEFAULT_HEIGHT,texture,fbo,outputW,outputH,inputW,inputH;
    private static Object world;
    private static ByteBuffer pixels;
    private static final Queue<String> notices=new ConcurrentLinkedQueue<>();
    static{
        try(InputStream in=Files.newInputStream(Paths.get("config","bridgetap-clips.properties"))){Properties p=new Properties();p.load(in);int f=Integer.parseInt(p.getProperty("fps","15")),s=Integer.parseInt(p.getProperty("seconds","30")),h=Integer.parseInt(p.getProperty("height","720"));if(validRate(f))fps=f;if(s==15||s==30||s==60)buffer.window(s);if(h==360||h==720||h==1080)maxHeight=h;sound=NativeClipTools.windows()&&Boolean.parseBoolean(p.getProperty("sound","true"));}catch(IOException|IllegalArgumentException ignored){}
    }
    private static void settings(){
        try{Path file=Paths.get("config","bridgetap-clips.properties");Files.createDirectories(file.getParent());Properties p=new Properties();p.setProperty("fps",String.valueOf(fps));p.setProperty("seconds",String.valueOf(buffer.window()));p.setProperty("height",String.valueOf(maxHeight));p.setProperty("sound",String.valueOf(sound));try(OutputStream out=Files.newOutputStream(file)){p.store(out,"BridgeTap video buffer (recording starts OFF)");}}catch(IOException ex){message="Could not save clip preferences.";}
    }
    public static void tick(){Minecraft mc=Minecraft.m_20213497();if(mc.f_28396371==null)return;String notice;while((notice=notices.poll())!=null)mc.f_28396371.m_83523863(new C_69136080("\u00a7b[BridgeTap] \u00a7f"+notice));}
    private static ExecutorService worker(String name){return Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,name);t.setDaemon(true);t.setPriority(Thread.NORM_PRIORITY-1);return t;});}
    public static boolean enabled(){return enabled;}
    public static boolean sound(){return sound;}
    public static void toggleSound(){sound=NativeClipTools.windows()&&!sound;if(!sound)ClipAudio.capture(false);clear();settings();}
    public static boolean saving(){return saving.get();}
    public static int fps(){return fps;}
    public static int height(){return maxHeight;}
    public static void quality(int height){if(height!=360&&height!=720&&height!=1080)throw new IllegalArgumentException("Invalid video resolution");maxHeight=height;clear();settings();}
    public static int seconds(){return buffer.window();}
    public static String message(){return message;}
    public static String backend(){return backend;}
    public static String stats(){return stream==null?String.format(Locale.US,"%.1fs / %.1f MiB / %.0f captured FPS (target %d) / %dp",buffer.duration(),buffer.bytes()/1048576.0,buffer.rate(),fps,maxHeight):String.format(Locale.US,"%.1fs / %.1f MiB disk / %.0f captured FPS (target %d) / %dp",stream.duration(),stream.bytes()/1048576.0,stream.rate(),fps,maxHeight);}
    public static String storage(){return stream==null?"128 MiB image buffer":"Rolling H.264 on disk / 2 GiB limit";}
    public static Path folder()throws IOException{Path p=Paths.get("bridgetap-clips");Files.createDirectories(p);return p;}
    public static void toggle(){enabled=!enabled;if(!enabled)ClipAudio.capture(false);clear();message=enabled?"Recording video. Press F9 to save the recent buffer.":"Buffer stopped and cleared.";notices.add(message);}
    public static synchronized void clear(){generation++;ordered.reset(generation);sequence=0;buffer.clear();if(stream!=null)stream.reset(generation,buffer.window());next=last=0;}
    public static void duration(int seconds){synchronized(ClipCapture.class){buffer.window(seconds);clear();}settings();}
    public static boolean validRate(int value){return value==10||value==15||value==20||value==30||value==60||value==120;}
    public static void rate(int value){if(!validRate(value))throw new IllegalArgumentException("Invalid capture rate");fps=value;clear();settings();}
    public static void save(){
        if(ReplayVideoExport.active()){message="Wait for the replay video export.";notices.add(message);return;}
        if(saving.get()){message="An MP4 export is already running.";notices.add(message);return;}
        if(stream!=null){saveStream();return;}
        java.util.List<ClipFrames.Frame> frames=buffer.snapshot(System.nanoTime());
        if(frames.size()<2){message="No video yet. Enable the buffer and play for a few seconds.";notices.add(message);return;}
        if(!saving.compareAndSet(false,true))return;final int rate=fps;
        message="Exporting MP4 in the background...";
        notices.add(message);
        final boolean audio=sound&&enabled;exporter.execute(()->{Path wave=null;try{
            String name="BridgeTap-"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+"-"+UUID.randomUUID().toString().substring(0,8)+".mp4";
            Path target=folder().resolve(name);
            if(audio)try{wave=ClipAudio.snapshot(frames.get(0).time,frames.get(frames.size()-1).time+1000000000L/rate,folder());}catch(Exception ex){notices.add("Audio unavailable; this clip will be silent. "+ex.getMessage());}
            ClipVideo.write(frames,rate,target,wave,(done,total)->message="Exporting MP4: "+done*100/total+"%");
            message="Saved: "+name;
            notices.add(message);
        }catch(Exception|LinkageError ex){message="Clip export failed: "+ex.getClass().getSimpleName();notices.add(message);System.err.println("[BridgeTap] "+ex);}
        finally{if(wave!=null)try{Files.deleteIfExists(wave);}catch(IOException ignored){}saving.set(false);}});
    }
    private static void saveStream(){
        if(!saving.compareAndSet(false,true))return;CompletableFuture<ClipStreamBuffer.Snapshot> prepared=stream.snapshot(System.nanoTime());final boolean audio=sound&&enabled;message="Preparing recent video...";notices.add(message);
        exporter.execute(()->{Path wave=null;try(ClipStreamBuffer.Snapshot snapshot=prepared.get(20,TimeUnit.SECONDS)){
            Path target=folder().resolve("BridgeTap-"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())+"-"+UUID.randomUUID().toString().substring(0,8)+".mp4");
            if(audio)try{wave=ClipAudio.snapshot(snapshot.first,snapshot.last,folder());}catch(Exception ex){notices.add("Audio unavailable; this clip will be silent. "+ex.getMessage());}
            message="Saving MP4 without recompressing...";snapshot.write(target,wave);message="Saved: "+target.getFileName();notices.add(message);
        }catch(Exception|LinkageError ex){prepared.cancel(false);Throwable cause=ex instanceof ExecutionException?ex.getCause():ex;message="Clip save failed: "+cause.getMessage();notices.add(message);diagnostic(cause);}finally{if(wave!=null)try{Files.deleteIfExists(wave);}catch(IOException ignored){}saving.set(false);}});
    }
    public static void capture(){
        if(ReplayVideoExport.active()){ClipAudio.capture(false);ReplayVideoExport.capture();return;}
        Minecraft mc=Minecraft.m_20213497();
        if(mc.f_26407825!=world){world=mc.f_26407825;clear();}
        ClipAudio.capture(enabled&&sound&&mc.f_26407825!=null&&Display.isActive()&&Display.isVisible());
        if(!enabled||mc.f_26407825==null||mc.f_28396371==null||!Display.isActive()||!Display.isVisible()){
            if(last!=0)clear();if(!enabled)releaseFramebuffer();return;
        }
        long now=System.nanoTime();if(last!=0&&now-last>2000000000L)clear();
        if(stream!=null&&!stream.error().isEmpty()){enabled=false;message=stream.error();notices.add(message);return;}
        if(pending.get()>=WORKERS||(stream!=null&&!stream.canAccept()))return;
        boolean fast=mode.framebuffer(GLContext.getCapabilities().OpenGL30),async=mode.async(GLContext.getCapabilities().OpenGL32);
        backend=async?"Async GPU readback / "+(stream==null?"Fast lossless compression":stream.encoder()):fast?"Compatibility readback / Adaptive capture (up to 30 FPS)":"Legacy readback / Up to 5 captured FPS";
        try{
            if(async)readback.poll(generation,(epoch,time,rgb,w,h)->submit(epoch,time,rgb,w,h,w,h));
            if(now<next||pending.get()>=WORKERS||(stream!=null&&!stream.canAccept())||(async&&!readback.available()))return;
            long interval=1000000000L/(async?fps:Math.min(fast?30:5,fps));next=next==0||now-next>interval?now+interval:next+interval;last=now;
            int w=mc.f_31800787,h=mc.f_74192110;if(w<2||h<2||w>8192||h>8192)return;
            if(!fast&&(long)w*h>3840*2160)throw new IllegalStateException("Legacy capture supports up to 3840 x 2160");
            int[] size=ClipQuality.size(w,h,maxHeight);int ow=size[0],oh=size[1];
            if(inputW!=w||inputH!=h||outputW!=ow||outputH!=oh){clear();releaseFramebuffer();inputW=w;inputH=h;outputW=ow;outputH=oh;}
            final long epoch=generation,time=now;final int rw=fast?ow:w,rh=fast?oh:h,dw=ow,dh=oh;
            if(async){readback.enqueue(ow,oh,epoch,time,()->readPixels(w,h,ow,oh,true,null));return;}
            int length=rw*rh*3;if(pixels==null||pixels.capacity()!=length)pixels=BufferUtils.createByteBuffer(length);pixels.clear();
            byte[] rgb=ClipRgbPool.acquire(length);if(rgb==null)return;boolean delivered=false;try{readPixels(w,h,ow,oh,fast,pixels);pixels.get(rgb);submit(epoch,time,rgb,rw,rh,dw,dh);delivered=true;}finally{if(!delivered)ClipRgbPool.release(rgb);}
            // Compatibility readbacks can stall the GPU. Keep their render-thread
            // cost near 5% instead of repeatedly blocking at the requested FPS.
            next=Math.max(next,now+(System.nanoTime()-now)*20);
        }catch(RuntimeException|LinkageError ex){captureFailed(ex,async,fast);}
    }
    static final class VideoFrame {final byte[] rgb;final int width,height,outWidth,outHeight;VideoFrame(byte[] rgb,int w,int h,int ow,int oh){this.rgb=rgb;width=w;height=h;outWidth=ow;outHeight=oh;}}
    static void exportError(Throwable error){diagnostic(error);}
    static void beginReplayExport(){ClipAudio.capture(false);clear();releaseFramebuffer();inputW=inputH=outputW=outputH=0;}
    static void endReplayExport(){clear();releaseFramebuffer();inputW=inputH=outputW=outputH=0;}
    static VideoFrame replayFrame(int height){Minecraft mc=Minecraft.m_20213497();int w=mc.f_31800787,h=mc.f_74192110;if(w<2||h<2||w>8192||h>8192)throw new IllegalStateException("Invalid game window size");int[] out=ClipQuality.size(w,h,height);boolean fast=mode.framebuffer(GLContext.getCapabilities().OpenGL30);int rw=fast?out[0]:w,rh=fast?out[1]:h;int length=rw*rh*3;byte[] rgb=ClipRgbPool.acquire(length);if(rgb==null)return null;boolean delivered=false;try{if(pixels==null||pixels.capacity()!=length)pixels=BufferUtils.createByteBuffer(length);pixels.clear();readPixels(w,h,out[0],out[1],fast,pixels);pixels.get(rgb);delivered=true;return new VideoFrame(rgb,rw,rh,out[0],out[1]);}finally{if(!delivered)ClipRgbPool.release(rgb);}}
    private static void captureFailed(Throwable error,boolean async,boolean fast){diagnostic(error);clear();releaseFramebuffer();if(mode.failed(async,fast)){message="Capture compatibility fallback enabled. Buffer is still ON.";backend="Retrying with a compatible capture method";}else{enabled=false;message="Video capture failed: "+error.getClass().getSimpleName()+". See config/bridgetap/clip-capture.log";}notices.add(message);}
    private static synchronized void submit(long epoch,long time,byte[] rgb,int rw,int rh,int dw,int dh){
        if(epoch!=generation){ClipRgbPool.release(rgb);return;}
        if(stream!=null){if(!stream.offer(epoch,time,rgb,rw,rh,dw,dh,fps))ClipRgbPool.release(rgb);return;}
        final long id=sequence++;pending.incrementAndGet();
        compression.execute(()->{try{if(epoch!=generation)return;
            byte[] png=rw==dw&&rh==dh?ClipPng.encode(rgb,rw,rh):ClipQuality.lossless(ClipPixels.image(rgb,rw,rh,dw,dh));
            ordered.owned(epoch,id,time,png);
        }catch(Exception|LinkageError ex){diagnostic(ex);message="Video compression failed: "+ex.getClass().getSimpleName()+". See config/bridgetap/clip-capture.log";notices.add(message);enabled=false;clear();}
        finally{ClipRgbPool.release(rgb);pending.decrementAndGet();}});
    }
    private static void readPixels(int w,int h,int ow,int oh,boolean fast,ByteBuffer bytes){
        int packBuffer=GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);if(bytes!=null)GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,0);
        int pack=GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT),readBuffer=GL11.glGetInteger(GL11.GL_READ_BUFFER),row=GL11.glGetInteger(GL11.GL_PACK_ROW_LENGTH),skipRows=GL11.glGetInteger(GL11.GL_PACK_SKIP_ROWS),skipPixels=GL11.glGetInteger(GL11.GL_PACK_SKIP_PIXELS);
        int read=fast?GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING):0,draw=fast?GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING):0,oldTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try{
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,1);
            GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH,0);GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS,0);GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS,0);
            if(fast){
                if(fbo==0){
                    texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGB8,ow,oh,0,GL11.GL_RGB,GL11.GL_UNSIGNED_BYTE,(ByteBuffer)null);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
                    fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);
                    if(GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER)!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Video framebuffer incomplete");
                }
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,0);GL11.glReadBuffer(GL11.GL_BACK);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,fbo);
                GL30.glBlitFramebuffer(0,0,w,h,0,0,ow,oh,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_LINEAR);
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,fbo);GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            }else GL11.glReadBuffer(GL11.GL_BACK);
            if(bytes==null)GL11.glReadPixels(0,0,ow,oh,GL11.GL_RGB,GL11.GL_UNSIGNED_BYTE,0L);
            else GL11.glReadPixels(0,0,fast?ow:w,fast?oh:h,GL11.GL_RGB,GL11.GL_UNSIGNED_BYTE,bytes);
        }finally{
            if(bytes!=null)GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,packBuffer);
            if(fast){GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);}
            GL11.glReadBuffer(readBuffer);GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,pack);GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH,row);GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS,skipRows);GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS,skipPixels);GL11.glBindTexture(GL11.GL_TEXTURE_2D,oldTexture);
        }
    }
    private static void releaseFramebuffer(){try{readback.release();}catch(RuntimeException|LinkageError ex){diagnostic(ex);}try{if(fbo!=0)GL30.glDeleteFramebuffers(fbo);}catch(RuntimeException|LinkageError ex){diagnostic(ex);}finally{fbo=0;}try{if(texture!=0)GL11.glDeleteTextures(texture);}catch(RuntimeException|LinkageError ex){diagnostic(ex);}finally{texture=0;pixels=null;}}
    private static void diagnostic(Throwable error){System.err.println("[BridgeTap] "+error);try{Path file=Paths.get("config","bridgetap","clip-capture.log");Files.createDirectories(file.getParent());try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(file,java.nio.charset.StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND))){out.println(new Date());error.printStackTrace(out);}}catch(IOException ignored){}}
}
