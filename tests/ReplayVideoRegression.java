import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public final class ReplayVideoRegression {
    private static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        for(int fps:new int[]{30,60,120}){ReplayVideoPlan plan=new ReplayVideoPlan(77,fps);check(plan.tick(0)==0&&plan.tick(plan.frames-1)==77,"Video omitted an endpoint");double previous=-1;for(int i=0;i<plan.frames;i++){check(plan.tick(i)>previous,"Video samples repeated or went backwards");previous=plan.tick(i);}}
        for(int fps:new int[]{30,60,120}){ReplayVideoPlan end=new ReplayVideoPlan(79,fps,2);check(end.frames-end.playbackFrames==2*fps,"Final result is not held for two seconds");for(int i=end.playbackFrames;i<end.frames;i++)check(end.ending(i)&&end.tick(i)==79,"Ending advanced the replay or lost its final pose");check(!end.ending(end.playbackFrames-1),"Result started before playback ended");}
        Path root=Files.createTempDirectory("replay-video-");int fps=args.length==0?60:120,w=320,h=180,count=fps+1;Path output=root.resolve("full-replay.mp4");ReplayVideoWriter writer=new ReplayVideoWriter(output,w,h,w,h,fps);
        for(int i=0;i<count;i++){byte[] rgb;while((rgb=ClipRgbPool.acquire(w*h*3))==null)Thread.sleep(1);Arrays.fill(rgb,(byte)(i*3));while(!writer.offer(rgb)){if(writer.result().isCompletedExceptionally())writer.result().join();Thread.sleep(1);}}
        writer.finish();check(writer.result().get(30,TimeUnit.SECONDS).equals(output)&&writer.written()==count&&Files.size(output)>1000,"Replay export lost frames");
        if(NativeClipTools.windows()){Path progress=root.resolve("decoded.txt");Process decode=new ProcessBuilder(NativeClipTools.tool("ffmpeg.exe").toString(),"-v","error","-i",output.toString(),"-progress","pipe:1","-f","null","-").redirectError(root.resolve("error.txt").toFile()).redirectOutput(progress.toFile()).start();check(decode.waitFor(20,TimeUnit.SECONDS)&&decode.exitValue()==0,"Replay MP4 cannot be decoded");int decoded=0;for(String line:Files.readAllLines(progress))if(line.startsWith("frame="))decoded=Integer.parseInt(line.substring(6).trim());check(decoded==count,"MP4 frame count differs from deterministic plan");}
        Path cancelled=root.resolve("cancelled.mp4");ReplayVideoWriter stopped=new ReplayVideoWriter(cancelled,w,h,w,h,fps);stopped.cancel();try{stopped.result().get(15,TimeUnit.SECONDS);throw new AssertionError("Cancelled export reported success");}catch(ExecutionException|CancellationException expected){}check(!Files.exists(cancelled)&&Files.list(root).noneMatch(p->p.toString().contains("part.mp4")),"Cancelled export leaked an output or partial file");
        System.out.println("PASS: replay endpoint/frame plan, lossless frame scheduling, playable MP4 and cancellation cleanup");
    }
}
