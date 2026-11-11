import com.bridger.momentum.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

public final class ClipRegression {
    public static final class Fence{}
    public static final class LegacyGpu {
        static Fence created;static boolean deleted;
        public static Fence glFenceSync(int condition,int flags){check(condition==0x9117&&flags==0,"Legacy fence arguments");return created=new Fence();}
        public static int glClientWaitSync(Fence fence,int flags,long timeout){check(fence==created&&flags==0&&timeout==0,"Legacy readback must poll without blocking");return 37146;}
        public static void glDeleteSync(Fence fence){check(fence==created,"Legacy fence cleanup");deleted=true;}
    }
    public static final class ModernGpu {
        static boolean deleted;
        public static long glFenceSync(int condition,int flags){check(condition==0x9117&&flags==0,"Modern fence arguments");return 0x123456789L;}
        public static int glClientWaitSync(long fence,int flags,long timeout){check(fence==0x123456789L&&flags==0&&timeout==0,"Modern readback must preserve 64-bit handles and poll without blocking");return 37146;}
        public static void glDeleteSync(long fence){check(fence==0x123456789L,"Modern fence cleanup");deleted=true;}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static byte[] image(int color)throws IOException{byte[] rgb=new byte[64*36*3];for(int p=0;p<rgb.length;p+=3){rgb[p]=(byte)(color>>16);rgb[p+1]=(byte)(color>>8);rgb[p+2]=(byte)color;}return ClipPng.encode(rgb,64,36);}
    public static void main(String[] args)throws Exception{
        byte[][] pooled=new byte[8][];for(int i=0;i<8;i++){pooled[i]=ClipRgbPool.acquire(12);check(pooled[i]!=null,"RGB pool exhausted before its bounded capacity");pooled[i][0]=(byte)i;}check(ClipRgbPool.acquire(12)==null,"RGB pool grew without a bound");ClipRgbPool.release(pooled[0]);byte[] reused=ClipRgbPool.acquire(12);check(reused==pooled[0]&&pooled[1][0]==1,"RGB pool reused a buffer still owned by the encoder");ClipRgbPool.release(reused);ClipRgbPool.release(reused);for(int i=1;i<8;i++)ClipRgbPool.release(pooled[i]);byte[] resized=ClipRgbPool.acquire(24);check(resized!=null&&resized.length==24,"RGB pool did not adapt to a window resize");ClipRgbPool.release(resized);
        for(Class<?> api:new Class<?>[]{LegacyGpu.class,ModernGpu.class}){ClipGpuSync sync=new ClipGpuSync(api);Object fence=sync.create();check(sync.poll(fence)==37146,"GPU API adapter changed the poll result");sync.delete(fence);}check(LegacyGpu.deleted&&ModernGpu.deleted,"Both LWJGL fence signatures must clean up");
        ClipCaptureMode modes=new ClipCaptureMode();check(modes.async(true)&&modes.framebuffer(true),"GPU paths should be enabled initially");
        check(modes.failed(true,true)&&!modes.async(true)&&modes.framebuffer(true),"An incompatible async API must fall back without stopping the buffer");
        check(modes.failed(false,true)&&!modes.framebuffer(true),"An incompatible framebuffer API must fall back to legacy capture");
        check(!modes.failed(false,false)&&!modes.async(false),"Only failure of the final compatibility path should stop capture");
        byte[] rgb={(byte)255,0,0,(byte)255,0,0,0,0,(byte)255,0,0,(byte)255};
        BufferedImage upright=ClipPixels.image(rgb,2,2,2,2);
        check((upright.getRGB(0,0)&0xFFFFFF)==0x0000FF&&(upright.getRGB(0,1)&0xFFFFFF)==0xFF0000,"OpenGL bottom-up rows must become an upright video");
        check(ClipPixels.image(rgb,2,2,4,4).getWidth()==4,"Legacy readback resizing");
        BufferedImage fast=ImageIO.read(new ByteArrayInputStream(ClipPng.encode(rgb,2,2)));check(fast.getRGB(0,0)==upright.getRGB(0,0)&&fast.getRGB(0,1)==upright.getRGB(0,1),"Fast PNG changed OpenGL image orientation");
        byte[] random=new byte[127*71*3];new Random(42).nextBytes(random);BufferedImage original=ClipPixels.image(random,127,71,127,71),actual=ImageIO.read(new ByteArrayInputStream(ClipPng.encode(random,127,71)));
        for(int y=0;y<71;y++)for(int x=0;x<127;x++)check(original.getRGB(x,y)==actual.getRGB(x,y),"Fast PNG changed captured pixels or row stride");
        try{ClipPng.encode(random,128,71);throw new AssertionError("Bad capture dimensions accepted");}catch(IllegalArgumentException good){}
        ClipFrames ring=new ClipFrames();byte[] input={1,2,3};ring.add(100,input);input[0]=9;check(ring.snapshot(100).get(0).image()[0]==1,"Ring must own image bytes");
        byte[] result=ring.snapshot(100).get(0).image();result[0]=8;check(ring.snapshot(100).get(0).image()[0]==1,"Snapshots must not expose mutable image arrays");
        ring.add(100,input);check(ring.snapshot(100).size()==1,"Duplicate/out-of-order timestamps");ring.add(31000000000L,input);check(ring.snapshot(31000000000L).size()==1,"Time eviction");
        java.util.List<ClipFrames.Frame> snapshot=ring.snapshot(31000000000L);ring.clear();check(snapshot.size()==1&&ring.bytes()==0,"Exports retain a detached snapshot");
        byte[] large=new byte[2*1024*1024];for(int i=0;i<80;i++)ring.add(i+1,large);check(ring.bytes()<=ClipFrames.MAX_BYTES&&ring.snapshot(80).size()==64,"RAM cap eviction");
        ring.window(15);check(ring.bytes()==0&&ring.window()==15,"Changing settings clears old frames");
        ClipFrames parallel=new ClipFrames();ClipFrameOrder order=new ClipFrameOrder(parallel);order.reset(7);order.complete(7,1,200,new byte[]{2});check(parallel.snapshot(200).isEmpty(),"Parallel compression must wait for earlier frames");order.complete(7,0,100,new byte[]{1});check(parallel.snapshot(200).size()==2,"Parallel compression reordered or dropped a frame");order.reset(8);order.complete(7,2,300,new byte[]{3});check(parallel.snapshot(300).size()==2,"Old compression generation leaked after reset");
        ring.add(0,image(0x0033EE));ring.add(650000000L,image(0xEE2200));ring.add(1000000000L,image(0xEE2200));
        Path video=Paths.get("clip-regression.mp4");int[] progress={0};ClipVideo.write(ring.snapshot(1000000000L),10,video,(done,total)->{progress[0]=done;check(total==11,"Timestamp resampling must preserve real duration");});
        check(Files.size(video)>1000&&progress[0]==11,"MP4 output and progress");
        Class<?> grab=Class.forName("com.bridger.momentum.shaded.jcodec.api.awt.AWTFrameGrab");Method frame=grab.getMethod("getFrame",File.class,int.class);
        BufferedImage blue=(BufferedImage)frame.invoke(null,video.toFile(),4),red=(BufferedImage)frame.invoke(null,video.toFile(),8);
        check(blue.getWidth()==64&&blue.getHeight()==36&&red.getWidth()==64,"Decoded video dimensions");
        Color b=new Color(blue.getRGB(32,18)),r=new Color(red.getRGB(32,18));check(b.getBlue()>b.getRed()+80&&r.getRed()>r.getBlue()+80,"Decoded H.264 frames preserve held frames and later changes");
        try{ClipVideo.write(ring.snapshot(1000000000L),10,video,null);throw new AssertionError("Existing video overwritten");}catch(IOException expected){}
        try(DirectoryStream<Path> files=Files.newDirectoryStream(Paths.get("."),"*.part.mp4")){check(!files.iterator().hasNext(),"Partial export files left behind");}
        check(Arrays.equals(ClipQuality.size(1920,1080,720),new int[]{1280,720}),"Default capture must preserve 720p detail");
        check(Arrays.equals(ClipQuality.size(2560,1440,1080),new int[]{1920,1080}),"1080p quality preset");
        BufferedImage detail=new BufferedImage(128,72,BufferedImage.TYPE_INT_RGB);for(int y=0;y<72;y++)for(int x=0;x<128;x++){int c=((x/2+y/2)%2)==0?0xE0E0E0:0x303030;detail.setRGB(x,y,c);}
        byte[] lossless=ClipQuality.lossless(detail);BufferedImage roundtrip=ImageIO.read(new ByteArrayInputStream(lossless));check(roundtrip.getRGB(3,4)==detail.getRGB(3,4),"Capture must not add JPEG loss before video encoding");
        ClipFrames sharp=new ClipFrames();sharp.add(0,lossless);sharp.add(100000000L,lossless);Path sharpVideo=Paths.get("sharp.mp4");ClipVideo.write(sharp.snapshot(100000000L),10,sharpVideo,null);
        BufferedImage decoded=(BufferedImage)frame.invoke(null,sharpVideo.toFile(),0);double mse=0;for(int y=0;y<72;y++)for(int x=0;x<128;x++){double delta=(decoded.getRGB(x,y)&255)-(detail.getRGB(x,y)&255);mse+=delta*delta;}mse/=128*72;
        if(!Boolean.getBoolean("bridgetap.test.javaEncoder"))check(mse<80,"High-quality Windows MP4 loses too much fine detail: "+mse);
        // The Java compatibility codec uses a different luma conversion. Check
        // that two-pixel detail survives independently of its absolute levels.
        check((decoded.getRGB(0,0)&255)>180&&(decoded.getRGB(2,0)&255)<80,"Fine checkerboard detail was blurred away");
        if(NativeClipTools.windows()&&!Boolean.getBoolean("bridgetap.test.javaEncoder"))testAudio(ring.snapshot(1000000000L));
        if(NativeClipTools.windows()&&!Boolean.getBoolean("bridgetap.test.javaEncoder")){
            Path fastVideo=Paths.get("120fps.mp4");final int[] exported={0};ClipVideo.write(ring.snapshot(1000000000L),120,fastVideo,(done,total)->{check(total==121,"120 FPS export changed the clip duration");exported[0]=done;});check(exported[0]==121,"120 FPS export truncated its frames");
            Process p=new ProcessBuilder(NativeClipTools.tool("ffmpeg.exe").toString(),"-v","error","-i",fastVideo.toString(),"-map","0:v:0","-pix_fmt","rgb24","-f","rawvideo","-").redirectError(new File("120fps-decode.log")).start();long bytes=0;byte[] chunk=new byte[8192];try(InputStream in=p.getInputStream()){int n;while((n=in.read(chunk))>=0)bytes+=n;}check(p.waitFor()==0&&bytes==121L*64*36*3,"120 FPS video does not decode to the expected real frame count");
        }
        System.out.println("PASS: bounded video ring, timestamps, snapshot isolation, real H.264 MP4 encoding/decoding and safe export");
    }
    private static void testAudio(java.util.List<ClipFrames.Frame> frames)throws Exception{
        Path wave=Paths.get("tone.wav");try(DataOutputStream out=new DataOutputStream(Files.newOutputStream(wave))){int samples=48000;out.writeBytes("RIFF");le(out,36+samples*2,4);out.writeBytes("WAVEfmt ");le(out,16,4);le(out,1,2);le(out,1,2);le(out,48000,4);le(out,96000,4);le(out,2,2);le(out,16,2);out.writeBytes("data");le(out,samples*2,4);for(int i=0;i<samples;i++)le(out,(int)(Math.sin(i*2*Math.PI*440/48000)*10000),2);}
        Path video=Paths.get("with-audio.mp4");ClipVideo.write(frames,10,video,wave,null);
        Process p=new ProcessBuilder(NativeClipTools.tool("ffmpeg.exe").toString(),"-v","error","-i",video.toString(),"-map","0:a:0","-f","s16le","-").redirectError(new File("audio-decode.log")).start();ByteArrayOutputStream pcm=new ByteArrayOutputStream();byte[] chunk=new byte[8192];try(InputStream in=p.getInputStream()){int n;while((n=in.read(chunk))>=0)pcm.write(chunk,0,n);}check(p.waitFor()==0&&pcm.size()>80000,"AAC audio track is missing or truncated");byte[] data=pcm.toByteArray();long energy=0;for(int i=0;i+1<data.length;i+=2){short value=(short)((data[i]&255)|(data[i+1]<<8));energy+=(long)value*value;}check(energy/(data.length/2)>1000000,"AAC export contains silence instead of the supplied audio");
    }
    private static void le(DataOutputStream out,int value,int bytes)throws IOException{for(int i=0;i<bytes;i++)out.writeByte(value>>(i*8));}
}
