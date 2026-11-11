import com.bridger.momentum.*;
import java.util.*;

/** Informational CPU comparison; not a hardware-dependent pass/fail test. */
public final class ClipCompressionBenchmark {
    public static void main(String[] args)throws Exception{
        int width=1280,height=720;byte[] rgb=new byte[width*height*3];Random noise=new Random(41);int[] tile=new int[16*16];for(int i=0;i<tile.length;i++)tile[i]=noise.nextInt(64);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++){int p=(y*width+x)*3,v=tile[(y%16)*16+x%16];rgb[p]=(byte)(x/64*7+v);rgb[p+1]=(byte)(y/64*9+v);rgb[p+2]=(byte)v;}
        for(int i=0;i<5;i++){ClipPng.encode(rgb,width,height);ClipQuality.lossless(ClipPixels.image(rgb,width,height,width,height));}
        int rounds=20;long before=System.nanoTime();int oldBytes=0,newBytes=0;
        for(int i=0;i<rounds;i++)oldBytes=ClipQuality.lossless(ClipPixels.image(rgb,width,height,width,height)).length;
        long middle=System.nanoTime();for(int i=0;i<rounds;i++)newBytes=ClipPng.encode(rgb,width,height).length;long end=System.nanoTime();
        System.out.printf(Locale.US,"720p CPU compression: previous %.2f ms/frame; fast %.2f ms/frame; PNG sizes %d / %d bytes%n",(middle-before)/1e6/rounds,(end-middle)/1e6/rounds,oldBytes,newBytes);
    }
}
