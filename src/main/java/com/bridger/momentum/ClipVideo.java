package com.bridger.momentum;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

/** H.264 MP4 export. Recorded timestamps are resampled to a constant frame rate. */
public final class ClipVideo {
    public interface Progress { void frame(int done,int total); }
    public static void write(List<ClipFrames.Frame> frames,int fps,Path target,Progress progress)throws Exception{
        write(frames,fps,target,null,progress);
    }
    public static void write(List<ClipFrames.Frame> frames,int fps,Path target,Path audio,Progress progress)throws Exception{
        if(frames.size()<2||fps<1||fps>120)throw new IllegalArgumentException("Not enough buffered video");
        if(Files.exists(target))throw new IOException("Output already exists");
        if(NativeClipTools.windows()&&!Boolean.getBoolean("bridgetap.test.javaEncoder")){writeNative(frames,fps,target,audio,progress);return;}
        if(audio!=null)throw new IOException("Audio export requires Windows x64");
        long start=frames.get(0).time,end=frames.get(frames.size()-1).time;
        int count=(int)Math.min(7320,Math.round((end-start)/1e9*fps)+1);
        Path temp=target.resolveSibling(target.getFileName()+"."+UUID.randomUUID()+".part.mp4");
        Class<?> type=Class.forName("com.bridger.momentum.shaded.jcodec.api.awt.AWTSequenceEncoder");
        Method create=type.getMethod("createSequenceEncoder",File.class,int.class),encode=type.getMethod("encodeImage",BufferedImage.class),finish=type.getMethod("finish");
        Object encoder=null;boolean finished=false;
        try{
            encoder=create.invoke(null,temp.toFile(),fps);configureQuality(encoder,fps);int index=0,decoded=-1;BufferedImage image=null;int width=0,height=0;
            for(int i=0;i<count;i++){
                long time=start+Math.round(i*1e9/fps);
                while(index+1<frames.size()&&frames.get(index+1).time<=time)index++;
                if(decoded!=index){image=ImageIO.read(new ByteArrayInputStream(frames.get(index).image()));decoded=index;
                    if(image==null)throw new IOException("Invalid buffered image");
                    if(width==0){width=image.getWidth();height=image.getHeight();}
                    if(width!=image.getWidth()||height!=image.getHeight())throw new IOException("Buffered image size changed");
                }
                encode.invoke(encoder,image);if(progress!=null)progress.frame(i+1,count);
            }
            finish.invoke(encoder);finished=true;Files.move(temp,target);
        }finally{
            if(encoder!=null&&!finished)try{finish.invoke(encoder);}catch(Exception ignored){}
            Files.deleteIfExists(temp);
        }
    }
    private static void writeNative(List<ClipFrames.Frame> frames,int fps,Path target,Path audio,Progress progress)throws Exception{
        long start=frames.get(0).time,end=frames.get(frames.size()-1).time;int count=(int)Math.min(7320,Math.round((end-start)/1e9*fps)+1);
        Path temp=target.resolveSibling(target.getFileName()+"."+UUID.randomUUID()+".part.mp4"),log=target.resolveSibling(".encode-"+UUID.randomUUID()+".log");Process process=null;
        try{
            List<String> command=new ArrayList<>(Arrays.asList(NativeClipTools.tool("ffmpeg.exe").toString(),"-hide_banner","-loglevel","error","-nostdin","-n","-f","image2pipe","-vcodec","png","-framerate",String.valueOf(fps),"-i","pipe:0"));
            if(audio!=null)command.addAll(Arrays.asList("-i",audio.toAbsolutePath().toString()));
            command.addAll(Arrays.asList("-map","0:v:0","-c:v","libx264","-threads","2","-preset","fast","-crf","16","-pix_fmt","yuv420p"));
            if(audio!=null)command.addAll(Arrays.asList("-map","1:a:0","-c:a","aac","-b:a","192k","-af","apad","-shortest"));
            command.addAll(Arrays.asList("-movflags","+faststart",temp.toAbsolutePath().toString()));
            process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            try(OutputStream input=new BufferedOutputStream(process.getOutputStream(),256*1024)){
                int index=0,previous=-1;byte[] encoded=null;
                for(int i=0;i<count;i++){long time=start+Math.round(i*1e9/fps);while(index+1<frames.size()&&frames.get(index+1).time<=time)index++;
                    if(index!=previous){encoded=frames.get(index).image();previous=index;}input.write(encoded);if(progress!=null)progress.frame(i+1,count);
                }
            }
            if(!process.waitFor(120,TimeUnit.SECONDS)){process.destroyForcibly();throw new IOException("Video encoder timed out");}
            if(process.exitValue()!=0)throw new IOException("Video encoder failed; "+new String(Files.readAllBytes(log),java.nio.charset.StandardCharsets.UTF_8));
            Files.move(temp,target);
        }finally{if(process!=null&&process.isAlive())process.destroyForcibly();Files.deleteIfExists(temp);Files.deleteIfExists(log);}
    }
    static void configureQuality(Object sequence,int fps)throws Exception{
        String pkg="com.bridger.momentum.shaded.jcodec.";
        // This is a pinned, privately bundled runtime. Its default rate control
        // raises P-frame quantization to 26; use a constant QP 16 for fine HUD text.
        Class<?> rate=Class.forName(pkg+"codecs.h264.encode.RateControl");
        Object control=Proxy.newProxyInstance(rate.getClassLoader(),new Class<?>[]{rate},(p,m,a)->{
            if(m.getName().equals("startPicture"))return 16;
            if(m.getName().equals("initialQpDelta")||m.getName().equals("accept"))return 0;
            if(m.getName().equals("toString"))return "BridgeTap QP16";
            if(m.getName().equals("hashCode"))return System.identityHashCode(p);
            if(m.getName().equals("equals"))return p==a[0];throw new UnsupportedOperationException(m.getName());
        });
        Class<?> h264=Class.forName(pkg+"codecs.h264.H264Encoder");Object video=h264.getConstructor(rate).newInstance(control);h264.getMethod("setKeyInterval",int.class).invoke(video,fps*2);
        Field sinkField=Class.forName(pkg+"api.SequenceEncoder").getDeclaredField("sink");sinkField.setAccessible(true);Object sink=sinkField.get(sequence);
        Field videoField=sink.getClass().getDeclaredField("videoEncoder");videoField.setAccessible(true);videoField.set(sink,video);
    }
}
