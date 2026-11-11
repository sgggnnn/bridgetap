import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.concurrent.*;

/** Informational throughput check; includes native RGB conversion and encoder IO. */
public final class ClipStreamBenchmark {
    public static void main(String[] args)throws Exception{
        int w=1920,h=1080,fps=120,frames=300;byte[] base=new byte[w*h*3];for(int y=0;y<h;y++)for(int x=0;x<w;x++){int p=(y*w+x)*3;base[p]=(byte)(x/4);base[p+1]=(byte)(y/4);base[p+2]=(byte)(x^y);}
        try(ClipStreamBuffer stream=new ClipStreamBuffer(Files.createTempDirectory("bridgetap-1080p-"))){stream.reset(1,30);long time=System.nanoTime(),wall=0;for(int i=0;i<frames;i++){if(i==60)wall=System.nanoTime();byte[] rgb;while((rgb=ClipRgbPool.acquire(base.length))==null)Thread.sleep(1);System.arraycopy(base,0,rgb,0,base.length);rgb[i*100%rgb.length]=(byte)i;while(!stream.offer(1,time+Math.round(i*1e9/fps),rgb,w,h,w,h,fps)){if(!stream.error().isEmpty())throw new AssertionError(stream.error());Thread.sleep(1);}}
            try(ClipStreamBuffer.Snapshot snapshot=stream.snapshot(time+Math.round(frames*1e9/fps)).get(25,TimeUnit.SECONDS)){double seconds=(System.nanoTime()-wall)/1e9;System.out.printf(java.util.Locale.US,"1080p native RGB throughput after warm-up: %.1f frames/second; %s; %.1fs retained%n",240/seconds,stream.encoder(),snapshot.duration());}
        }
    }
}
