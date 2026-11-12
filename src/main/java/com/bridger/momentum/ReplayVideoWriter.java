package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;
import java.awt.image.BufferedImage;

/** Two-frame mailbox; slow encoding delays rendering rather than dropping frames. */
public final class ReplayVideoWriter {
    private final ArrayBlockingQueue<byte[]> queue=new ArrayBlockingQueue<>(2);
    private volatile boolean finishing,cancelled;private volatile Process process;private volatile int written;
    private final CompletableFuture<Path> result=new CompletableFuture<>();
    public ReplayVideoWriter(Path target,int width,int height,int outWidth,int outHeight,int fps){Thread thread=new Thread(()->write(target,width,height,outWidth,outHeight,fps),"BridgeTap replay video");thread.setDaemon(true);thread.start();}
    public boolean ready(){return !cancelled&&!finishing&&!result.isDone()&&queue.remainingCapacity()>0;}
    public boolean offer(byte[] rgb){return ready()&&queue.offer(rgb);}
    public void finish(){finishing=true;}
    public void cancel(){cancelled=true;Process p=process;if(p!=null)p.destroyForcibly();}
    public CompletableFuture<Path> result(){return result;}public int written(){return written;}
    private void write(Path target,int w,int h,int ow,int oh,int fps){
        Path temp=target.resolveSibling(target.getFileName()+"."+UUID.randomUUID()+".part.mp4"),log=target.resolveSibling(".replay-video-"+UUID.randomUUID()+".log");
        Path completed=null;Throwable failure=null;
        Object javaEncoder=null;Method encode=null,finish=null;boolean javaFinished=false;OutputStream pipe=null;
        try{
            if(Files.exists(target))throw new FileAlreadyExistsException(target.toString());Files.createDirectories(target.toAbsolutePath().getParent());
            if(NativeClipTools.windows()&&!Boolean.getBoolean("bridgetap.test.javaEncoder")){
                Path tool=NativeClipTools.tool("ffmpeg.exe");String codec=ow<144||oh<64?"libx264":ClipStreamBuffer.chooseEncoder(tool);
                List<String> cmd=new ArrayList<>(Arrays.asList(tool.toString(),"-hide_banner","-loglevel","error","-nostdin","-n","-filter_threads",fps>=120?"4":"1","-f","rawvideo","-pixel_format","rgb24","-video_size",w+"x"+h,"-framerate",String.valueOf(fps),"-i","pipe:0","-vf","vflip,scale="+ow+":"+oh+":flags=fast_bilinear","-an"));
                ClipStreamBuffer.encoderOptions(cmd,codec,14);cmd.addAll(Arrays.asList("-pix_fmt","yuv420p","-bf","0","-movflags","+faststart",temp.toAbsolutePath().toString()));
                if(cancelled)throw new CancellationException();process=new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile()).start();pipe=new BufferedOutputStream(process.getOutputStream(),256*1024);
            }else{
                Class<?> type=Class.forName("com.bridger.momentum.shaded.jcodec.api.awt.AWTSequenceEncoder");javaEncoder=type.getMethod("createSequenceEncoder",File.class,int.class).invoke(null,temp.toFile(),fps);ClipVideo.configureQuality(javaEncoder,fps);encode=type.getMethod("encodeImage",BufferedImage.class);finish=type.getMethod("finish");
            }
            while(!cancelled){byte[] rgb=queue.poll(100,TimeUnit.MILLISECONDS);if(rgb==null){if(finishing&&queue.isEmpty())break;continue;}try{if(rgb.length!=(long)w*h*3)throw new IOException("Replay frame size changed");if(pipe!=null)pipe.write(rgb);else encode.invoke(javaEncoder,ClipPixels.image(rgb,w,h,ow,oh));written++;}finally{ClipRgbPool.release(rgb);}}
            if(cancelled)throw new CancellationException();if(written==0)throw new IOException("Replay video has no frames");
            if(pipe!=null){pipe.close();pipe=null;if(!process.waitFor(30,TimeUnit.SECONDS)){process.destroyForcibly();throw new IOException("Replay encoder timed out");}if(process.exitValue()!=0)throw new IOException("Replay encoder failed: "+new String(Files.readAllBytes(log),java.nio.charset.StandardCharsets.UTF_8));}
            else{finish.invoke(javaEncoder);javaFinished=true;}
            if(cancelled)throw new CancellationException();Files.move(temp,target);completed=target;
        }catch(Throwable error){failure=error;if(!(error instanceof CancellationException))try{if(Files.exists(log)&&Files.size(log)>0)failure=new IOException("Replay encoder: "+new String(Files.readAllBytes(log),java.nio.charset.StandardCharsets.UTF_8),error);}catch(IOException ignored){}}finally{
            if(pipe!=null)try{pipe.close();}catch(IOException ignored){}Process p=process;if(p!=null&&p.isAlive())p.destroyForcibly();
            if(javaEncoder!=null&&!javaFinished)try{finish.invoke(javaEncoder);}catch(Exception ignored){}
            byte[] bytes;while((bytes=queue.poll())!=null)ClipRgbPool.release(bytes);
            try{Files.deleteIfExists(temp);Files.deleteIfExists(log);}catch(IOException ignored){}
        }
        if(failure==null)result.complete(completed);else result.completeExceptionally(failure);
    }
}
