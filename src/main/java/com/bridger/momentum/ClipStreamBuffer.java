package com.bridger.momentum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Bounded raw-frame mailbox feeding a native rolling H.264 segment buffer. */
public final class ClipStreamBuffer implements AutoCloseable {
    public static final long MAX_DISK_BYTES=2L*1024*1024*1024;
    private interface Work{void run()throws Exception;}
    private final BlockingQueue<Work> queue=new LinkedBlockingQueue<>();private final AtomicInteger pending=new AtomicInteger();
    private final Path root;private final Thread worker,shutdown;private volatile long desiredEpoch;private long epoch=-1;
    private volatile int seconds=30;private volatile double duration,rate;private volatile long bytes;private volatile String error="",encoder="Starting video encoder";private volatile boolean closed;
    private final List<Segment> segments=new ArrayList<>();private final Set<Path> groups=new HashSet<>();private final Deque<Long> samples=new ArrayDeque<>();
    private Process process;private OutputStream input;private Path group,manifest,log,tool;private long manifestOffset,start,lastTime,written,lastScan;private int fps,rw,rh,ow,oh;private byte[] previous;private String codec;
    private static final class Segment{final Path file;final long first,last;final long bytes;Segment(Path file,long first,long last)throws IOException{this.file=file;this.first=first;this.last=last;bytes=Files.size(file);}}
    public ClipStreamBuffer(Path root){this.root=root.toAbsolutePath().normalize();worker=new Thread(this::loop,"BridgeTap rolling video");worker.setDaemon(true);worker.setPriority(Thread.NORM_PRIORITY-1);shutdown=new Thread(this::close,"BridgeTap video shutdown");Runtime.getRuntime().addShutdownHook(shutdown);worker.start();}
    public boolean canAccept(){return !closed&&error.isEmpty()&&pending.get()<3;}
    public String error(){return error;}public String encoder(){return encoder;}public double duration(){return duration;}public double rate(){return rate;}public long bytes(){return bytes;}
    public void reset(long version,int window){desiredEpoch=version;seconds=window;duration=rate=0;bytes=0;error="";queue.offer(()->{if(version==desiredEpoch)resetWorker(version);});}
    /** Ownership of RGB transfers to this buffer only if accepted. */
    public boolean offer(long version,long time,byte[] rgb,int width,int height,int outWidth,int outHeight,int framesPerSecond){
        if(!canAccept()||version!=desiredEpoch)return false;int count=pending.incrementAndGet();if(count>3){pending.decrementAndGet();return false;}
        queue.offer(()->{try{if(version!=desiredEpoch||!error.isEmpty())return;if(epoch!=version)resetWorker(version);frame(time,rgb,width,height,outWidth,outHeight,framesPerSecond);}finally{if(rgb!=previous)ClipRgbPool.release(rgb);pending.decrementAndGet();}});return true;
    }
    public CompletableFuture<Snapshot> snapshot(long time){CompletableFuture<Snapshot> result=new CompletableFuture<>();if(closed){result.completeExceptionally(new IOException("Video buffer is closed"));return result;}long requested=desiredEpoch;queue.offer(()->{try{if(result.isCancelled())return;if(epoch!=requested||requested!=desiredEpoch)throw new IOException("No video yet. Enable the buffer and play for a few seconds.");if(!error.isEmpty())throw new IOException(error);if(previous!=null)fill(time);finishEncoder();refreshSegments();trim(time);Snapshot copy=copySnapshot(time);if(!result.complete(copy))copy.close();}catch(Exception ex){result.completeExceptionally(ex);}});return result;}
    private void loop(){while(!closed){try{queue.take().run();}catch(InterruptedException ex){Thread.currentThread().interrupt();break;}catch(Exception|LinkageError ex){error="Video encoder failed: "+ex.getMessage();try{finishEncoder();}catch(Exception ignored){}System.err.println("[BridgeTap] "+error);}}}
    private void resetWorker(long version)throws Exception{try{finishEncoder();}finally{ClipRgbPool.release(previous);for(Segment s:segments)Files.deleteIfExists(s.file);segments.clear();for(Path dir:new ArrayList<>(groups))cleanupGroup(dir);groups.clear();samples.clear();previous=null;duration=rate=0;bytes=0;epoch=version;}}
    private void frame(long time,byte[] rgb,int width,int height,int outWidth,int outHeight,int framesPerSecond)throws Exception{
        if(rgb.length!=(long)width*height*3)throw new IOException("Invalid raw video frame");
        if(process==null){startEncoder(time,width,height,outWidth,outHeight,framesPerSecond);previous=rgb;lastTime=time;if(time>start)fill(time);}
        else{if(width!=rw||height!=rh||outWidth!=ow||outHeight!=oh||framesPerSecond!=fps)throw new IOException("Capture dimensions changed without a reset");fill(time);if(previous!=rgb)ClipRgbPool.release(previous);previous=rgb;lastTime=time;}
        samples.addLast(time);while(samples.size()>1&&time-samples.peekFirst()>2000000000L)samples.removeFirst();rate=samples.size()<2?0:(samples.size()-1)*1e9/(time-samples.peekFirst());
        if(time-lastScan>500000000L){refreshSegments();trim(time);lastScan=time;}updateDuration();
    }
    private void fill(long time)throws IOException{if(previous==null||input==null)return;long wanted=Math.max(1,Math.round((time-start)*fps/1e9));if(wanted-written>fps*3L)throw new IOException("Video encoder fell more than 3 seconds behind");while(written<wanted){input.write(previous);written++;}input.flush();}
    private void startEncoder(long time,int width,int height,int outWidth,int outHeight,int framesPerSecond)throws Exception{
        if(tool==null)tool=NativeClipTools.tool("ffmpeg.exe");if(codec==null)codec=chooseEncoder(tool);encoder=codec.equals("libx264")?"CPU H.264 / ultrafast":codec.replace("h264_","").toUpperCase(Locale.ROOT)+" hardware H.264";
        Files.createDirectories(root);group=Files.createTempDirectory(root,"session-");groups.add(group);manifest=group.resolve("segments.csv");log=group.resolve("encoder.log");manifestOffset=0;
        rw=width;rh=height;ow=outWidth;oh=outHeight;fps=framesPerSecond;start=time;if(!segments.isEmpty()&&Math.abs(time-segments.get(segments.size()-1).last)<3000000000L)start=segments.get(segments.size()-1).last;written=0;lastScan=0;
        List<String> cmd=new ArrayList<>(Arrays.asList(tool.toString(),"-hide_banner","-loglevel","error","-nostdin","-y","-filter_threads",fps>=120?"4":"1","-f","rawvideo","-pixel_format","rgb24","-video_size",rw+"x"+rh,"-framerate",String.valueOf(fps),"-i","pipe:0","-vf","vflip,scale="+ow+":"+oh+":flags=fast_bilinear","-an"));
        encoderOptions(cmd,codec);cmd.addAll(Arrays.asList("-pix_fmt","yuv420p","-g",String.valueOf(fps),"-bf","0","-force_key_frames","expr:gte(t,n_forced)","-f","segment","-segment_time","1","-reset_timestamps","1","-segment_format","mpegts","-segment_list","segments.csv","-segment_list_type","csv","part-%08d.ts"));
        process=new ProcessBuilder(cmd).directory(group.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();input=new BufferedOutputStream(process.getOutputStream(),256*1024);
    }
    static void encoderOptions(List<String> args,String codec){encoderOptions(args,codec,16);}
    static void encoderOptions(List<String> args,String codec,int quality){args.addAll(Arrays.asList("-c:v",codec));if(codec.equals("h264_nvenc"))args.addAll(Arrays.asList("-preset","p1","-tune","ll","-rc","constqp","-qp",String.valueOf(quality)));else if(codec.equals("h264_qsv"))args.addAll(Arrays.asList("-preset","veryfast","-global_quality",String.valueOf(quality)));else if(codec.equals("h264_amf"))args.addAll(Arrays.asList("-quality","speed","-rc","cqp","-qp_i",String.valueOf(quality),"-qp_p",String.valueOf(quality)));else args.addAll(Arrays.asList("-preset","ultrafast","-tune","zerolatency","-crf",String.valueOf(quality),"-threads","2"));}
    static String chooseEncoder(Path tool)throws Exception{
        if(Boolean.getBoolean("bridgetap.test.cpuStream"))return "libx264";
        for(String candidate:new String[]{"h264_nvenc","h264_qsv","h264_amf"}){Path log=Files.createTempFile("bridgetap-encoder-",".log");Process probe=null;try{List<String> cmd=new ArrayList<>(Arrays.asList(tool.toString(),"-hide_banner","-loglevel","error","-f","lavfi","-i","color=size=320x180:rate=60","-frames:v","1"));encoderOptions(cmd,candidate);cmd.addAll(Arrays.asList("-f","null","-"));probe=new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile()).start();if(probe.waitFor(5,TimeUnit.SECONDS)&&probe.exitValue()==0)return candidate;}finally{if(probe!=null&&probe.isAlive())probe.destroyForcibly();Files.deleteIfExists(log);}}
        return "libx264";
    }
    private void finishEncoder()throws Exception{
        Process opened=process;if(opened==null)return;process=null;IOException pipeError=null;try{if(input!=null)input.close();}catch(IOException ex){pipeError=ex;}finally{input=null;}
        if(!opened.waitFor(10,TimeUnit.SECONDS)){opened.destroyForcibly();throw new IOException("Rolling encoder timed out");}
        refreshSegments();ClipRgbPool.release(previous);previous=null;
        if(opened.exitValue()!=0||pipeError!=null){String detail=Files.exists(log)?new String(Files.readAllBytes(log),StandardCharsets.UTF_8):"";if(detail.length()>1200)detail=detail.substring(detail.length()-1200);throw new IOException("Rolling encoder: "+detail,pipeError);}
    }
    private void refreshSegments()throws IOException{
        if(manifest==null||!Files.exists(manifest))return;
        try(RandomAccessFile csv=new RandomAccessFile(manifest.toFile(),"r")){csv.seek(manifestOffset);String line;while((line=csv.readLine())!=null){String[] fields=line.split(",");if(fields.length!=3||!fields[0].matches("part-[0-9]{8}\\.ts"))break;Path file=group.resolve(fields[0]);long first=start+Math.round(Double.parseDouble(fields[1])*1e9),last=start+Math.round(Double.parseDouble(fields[2])*1e9);if(Files.exists(file))segments.add(new Segment(file,first,last));manifestOffset=csv.getFilePointer();}}
    }
    private void trim(long now)throws IOException{long total=0;for(Segment s:segments)total+=s.bytes;while(!segments.isEmpty()&&(segments.get(0).last<=now-seconds*1000000000L||total>MAX_DISK_BYTES)){Segment old=segments.remove(0);Files.deleteIfExists(old.file);total-=old.bytes;}bytes=total;updateDuration();}
    private void updateDuration(){long first=segments.isEmpty()?start:segments.get(0).first,last=process==null?(segments.isEmpty()?first:segments.get(segments.size()-1).last):start+Math.round(written*1e9/Math.max(1,fps));duration=Math.min(seconds,Math.max(0,(last-first)/1e9));}
    private Snapshot copySnapshot(long time)throws IOException{
        if(segments.isEmpty())throw new IOException("No video yet. Play for a few seconds before saving.");long last=Math.min(time,segments.get(segments.size()-1).last),wanted=last-seconds*1000000000L;List<Segment> selected=new ArrayList<>();for(Segment s:segments)if(s.last>wanted&&s.first<last)selected.add(s);if(selected.isEmpty())throw new IOException("No recent video");
        Path copy=Files.createTempDirectory(root,"export-");List<Path> files=new ArrayList<>();try{int index=0;for(Segment s:selected){Path target=copy.resolve(String.format(Locale.ROOT,"part-%08d.ts",index++));try{Files.createLink(target,s.file);}catch(IOException|UnsupportedOperationException ex){Files.copy(s.file,target);}files.add(target);}long first=selected.get(0).first;return new Snapshot(copy,files,first,last,0,tool);}catch(IOException ex){for(Path f:files)Files.deleteIfExists(f);Files.deleteIfExists(copy);throw ex;}
    }
    public static final class Snapshot implements AutoCloseable{
        private final Path folder,tool;private final List<Path> files;public final long first,last;private final double skip;
        Snapshot(Path folder,List<Path> files,long first,long last,double skip,Path tool){this.folder=folder;this.files=files;this.first=first;this.last=last;this.skip=skip;this.tool=tool;}
        public double duration(){return (last-first)/1e9;}
        public void write(Path target,Path audio)throws Exception{
            if(Files.exists(target))throw new FileAlreadyExistsException(target.toString());Path list=folder.resolve("concat.txt"),log=folder.resolve("mux.log"),temp=folder.resolve("video.mp4");List<String> entries=new ArrayList<>();for(Path file:files)entries.add("file '"+file.getFileName()+"'");Files.write(list,entries,StandardCharsets.UTF_8);
            List<String> cmd=new ArrayList<>(Arrays.asList(tool.toString(),"-hide_banner","-loglevel","error","-nostdin","-y","-ss",String.format(Locale.ROOT,"%.9f",skip),"-f","concat","-safe","1","-i",list.toString()));if(audio!=null)cmd.addAll(Arrays.asList("-i",audio.toAbsolutePath().toString()));cmd.addAll(Arrays.asList("-map","0:v:0","-c:v","copy","-t",String.format(Locale.ROOT,"%.9f",duration())));if(audio!=null)cmd.addAll(Arrays.asList("-map","1:a:0","-c:a","aac","-b:a","192k","-ar","48000","-ac","2","-af","apad"));cmd.addAll(Arrays.asList("-movflags","+faststart",temp.toString()));
            Process mux=new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile()).start();try{if(!mux.waitFor(30,TimeUnit.SECONDS)){mux.destroyForcibly();throw new IOException("Clip mux timed out");}if(mux.exitValue()!=0)throw new IOException("Clip mux failed: "+new String(Files.readAllBytes(log),StandardCharsets.UTF_8));Files.move(temp,target);}finally{if(mux.isAlive())mux.destroyForcibly();Files.deleteIfExists(temp);}
        }
        @Override public void close()throws IOException{for(Path p:files)Files.deleteIfExists(p);for(String name:new String[]{"concat.txt","mux.log","video.mp4"})Files.deleteIfExists(folder.resolve(name));Files.deleteIfExists(folder);}
    }
    private void cleanupGroup(Path dir)throws IOException{if(!dir.normalize().getParent().equals(root)||!dir.getFileName().toString().startsWith("session-"))throw new IOException("Invalid capture cache directory");try(DirectoryStream<Path> files=Files.newDirectoryStream(dir)){for(Path p:files){String n=p.getFileName().toString();if(n.matches("part-[0-9]{8}\\.ts")||n.equals("segments.csv")||n.equals("encoder.log"))Files.deleteIfExists(p);}}Files.deleteIfExists(dir);}
    @Override public void close(){if(closed)return;CompletableFuture<Void> done=new CompletableFuture<>();queue.offer(()->{try{resetWorker(desiredEpoch);}finally{closed=true;done.complete(null);}});try{done.get(15,TimeUnit.SECONDS);}catch(Exception ex){if(process!=null)process.destroyForcibly();closed=true;worker.interrupt();}finally{if(Thread.currentThread()!=shutdown)try{Runtime.getRuntime().removeShutdownHook(shutdown);}catch(IllegalStateException ignored){}}}
}
