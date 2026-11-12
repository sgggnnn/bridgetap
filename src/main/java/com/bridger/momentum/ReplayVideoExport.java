package com.bridger.momentum;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.Display;

/** Main-thread render coordinator. An output frame advances only after acceptance. */
public final class ReplayVideoExport {
    private static ReplayVideoExport current;private static String message="";
    private final ReplayVideoPlan plan;private final int height;private final ReplayVideoOptions options;private final Object session;
    private final ReplaySession.VideoState saved;private final ReplayVideoExportScreen screen;private java.util.function.Consumer<String> nativeCompletion;
    private ReplayVideoWriter writer;private int frame,warmup=15,width,inputHeight;private boolean finishing,statusWarned;
    private final Path target;private final double resultSeconds;private byte[] endingImage;private int endOutWidth,endOutHeight,windowWidth,windowHeight;
    private ReplayVideoExport(RunHistory.Run run,ReplayData data,int fps,int height,ReplayVideoOptions options,ReplayVideoExportScreen screen)throws Exception{
        if(!ReplaySession.active()||!ReplaySession.recordingId().equals(run.id))if(!ReplaySession.start(run,data))throw new IllegalArgumentException("Join a world before exporting");
        this.screen=screen;this.height=options.height;this.options=options;session=ReplaySession.token();plan=new ReplayVideoPlan(ReplaySession.lastTick(),options.fps,options.ending?2:0);resultSeconds=run.time;
        target=folder().resolve(IslandLibrary.safeFileName(run.displayName())+"-"+run.id.substring(0,Math.min(8,run.id.length()))+"-"+UUID.randomUUID().toString().substring(0,8)+".mp4");
        saved=ReplaySession.beginVideo(options.camera,options.cleanHud);try{ReplaySession.videoTick(0);ClipCapture.beginReplayExport();}catch(RuntimeException|LinkageError error){ReplaySession.endVideo(saved);throw error;}
    }
    public static Path folder()throws java.io.IOException{Path path=Paths.get("bridgetap-clips","replays");Files.createDirectories(path);return path;}
    public static boolean active(){return current!=null;}
    public static boolean overlays(){return current==null;}
    public static boolean cleanHud(){return current!=null&&current.options.cleanHud;}
    public static boolean ending(){return current!=null&&current.plan.ending(current.frame);}
    public static String resultTime(){return current==null?"":ReplayVideoItems.time(current.resultSeconds,current.plan.lastTick/20.0);}
    public static String message(){return message;}
    public static void start(RunHistory.Run run,ReplayData data,int fps,int height,boolean pov,boolean overlays,ReplayVideoExportScreen screen)throws Exception{
        start(run,data,new ReplayVideoOptions(fps,height,pov?0:2,true,true,overlays,true),screen);
    }
    public static void start(RunHistory.Run run,ReplayData data,ReplayVideoOptions options,ReplayVideoExportScreen screen)throws Exception{
        if(current!=null)throw new IllegalArgumentException("A replay video is already exporting");if(ClipCapture.saving())throw new IllegalArgumentException("Wait for the current clip export");
        current=new ReplayVideoExport(run,data,options.fps,options.height,options,screen);message="Preparing replay video...";Minecraft.m_20213497().m_52715402(null);
    }
    public static void startNative(RunHistory.Run run,ReplayData data,ReplayVideoOptions options,java.util.function.Consumer<String> completion)throws Exception{start(run,data,options,null);current.nativeCompletion=completion;}
    public static void capture(){ReplayVideoExport job=current;if(job==null)return;try{
        Minecraft mc=Minecraft.m_20213497();if(ReplaySession.token()!=job.session||mc.f_70816363!=null||!Display.isActive()||!Display.isVisible()){cancel();return;}
        if(job.writer!=null&&job.writer.result().isDone()){tick();return;}if(job.finishing)return;
        if(job.writer!=null&&(mc.f_31800787!=job.windowWidth||mc.f_74192110!=job.windowHeight))throw new IllegalStateException("Window resized during export");
        if(job.warmup-->0)return;if(job.writer!=null&&!job.writer.ready())return;
        ReplayVideoHud.frame(job.options);
        ClipCapture.VideoFrame image;
        if(job.plan.ending(job.frame)&&job.endingImage!=null){byte[] rgb=ClipRgbPool.acquire(job.endingImage.length);if(rgb==null)return;System.arraycopy(job.endingImage,0,rgb,0,rgb.length);image=new ClipCapture.VideoFrame(rgb,job.width,job.inputHeight,job.endOutWidth,job.endOutHeight);}else image=ClipCapture.replayFrame(job.height);if(image==null)return;
        if(job.plan.ending(job.frame)&&job.endingImage==null){job.endingImage=image.rgb.clone();job.endOutWidth=image.outWidth;job.endOutHeight=image.outHeight;}
        if(job.writer==null){job.windowWidth=mc.f_31800787;job.windowHeight=mc.f_74192110;job.width=image.width;job.inputHeight=image.height;job.writer=new ReplayVideoWriter(job.target,image.width,image.height,image.outWidth,image.outHeight,job.plan.fps);}
        if(image.width!=job.width||image.height!=job.inputHeight){ClipRgbPool.release(image.rgb);throw new IllegalStateException("Window resized during export");}
        if(!job.writer.offer(image.rgb)){ClipRgbPool.release(image.rgb);return;}
        job.frame++;message="Rendering replay video: "+job.frame*100/job.plan.frames+"% (Esc cancels)";
        if(job.frame>=job.plan.frames){job.finishing=true;job.writer.finish();message="Finishing replay MP4...";}
        else{double next=job.plan.tick(job.frame),previous=job.plan.tick(job.frame-1);job.warmup=(int)next!=(int)previous?2:0;ReplaySession.videoTick(next);}
    }catch(Exception|LinkageError error){fail(error);}finally{if(current==job)try{ReplayVideoHud.status(message,job.frame*100/job.plan.frames);}catch(RuntimeException|LinkageError statusError){if(!job.statusWarned){job.statusWarned=true;ClipCapture.exportError(statusError);}}}}
    public static void tick(){ReplayVideoExport job=current;if(job==null||job.writer==null||!job.writer.result().isDone())return;try{Path saved=job.writer.result().join();complete("Saved: "+saved.getFileName(),true);}catch(Exception error){fail(error);}}
    public static void cancel(){ReplayVideoExport job=current;if(job==null)return;if(job.writer!=null)job.writer.cancel();complete("Replay video cancelled.",false);}
    private static void fail(Throwable error){ReplayVideoExport job=current;if(job!=null&&job.writer!=null)job.writer.cancel();Throwable cause=error instanceof java.util.concurrent.CompletionException&&error.getCause()!=null?error.getCause():error;ClipCapture.exportError(cause);complete("Replay video failed: "+(cause.getMessage()==null?cause.getClass().getSimpleName():cause.getMessage()),false);}
    private static void complete(String text,boolean showScreen){ReplayVideoExport job=current;if(job==null)return;current=null;message=text;if(ReplaySession.token()==job.session)ReplaySession.endVideo(job.saved);ClipCapture.endReplayExport();if(job.screen!=null)job.screen.result(text);Minecraft mc=Minecraft.m_20213497();if(showScreen&&mc.f_26407825!=null&&job.nativeCompletion!=null)job.nativeCompletion.accept(text);else if(showScreen&&mc.f_26407825!=null&&job.screen!=null)mc.m_52715402(job.screen);else if(mc.f_28396371!=null)mc.f_28396371.m_83523863(new C_69136080("\u00a7b[BridgeTap] \u00a7f"+text));}
}
