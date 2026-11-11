package com.bridger.momentum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Private WASAPI helper captures the default Windows output, never a microphone. */
public final class ClipAudio {
    private static boolean desired;private static long epoch;private static Process process;private static Writer input;
    private static CompletableFuture<Boolean> ready;
    private static final Map<String,CompletableFuture<Boolean>> requests=new ConcurrentHashMap<>();
    private static volatile String status="Audio off";
    public static String status(){return status;}
    public static synchronized void capture(boolean enabled){
        if(desired==enabled)return;desired=enabled;long version=++epoch;
        if(!enabled){status="Audio off";if(process!=null){process.destroy();process=null;input=null;}if(ready!=null)ready.complete(false);for(CompletableFuture<Boolean> f:requests.values())f.complete(false);requests.clear();return;}
        ready=new CompletableFuture<>();status="Starting Windows output capture...";
        Thread launch=new Thread(()->{
            try{
                Path helper=NativeClipTools.tool("bridgetap-loopback.exe");final Process opened;
                synchronized(ClipAudio.class){if(!desired||epoch!=version)return;opened=new ProcessBuilder(helper.toString()).redirectErrorStream(true).start();process=opened;input=new OutputStreamWriter(opened.getOutputStream(),StandardCharsets.UTF_8);}
                try(BufferedReader reader=new BufferedReader(new InputStreamReader(opened.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=reader.readLine())!=null){
                    synchronized(ClipAudio.class){if(epoch!=version)break;
                        if(line.equals("READY")){status="Audio: default Windows output / No microphone";ready.complete(true);}
                        else if(line.startsWith("FAILED "))throw new IOException(new String(Base64.getDecoder().decode(line.substring(7)),StandardCharsets.UTF_8));
                        else if(line.startsWith("SAVED ")){CompletableFuture<Boolean> f=requests.remove(line.substring(6));if(f!=null)f.complete(true);}
                        else if(line.startsWith("ERROR ")){String[] p=line.split(" ",3);CompletableFuture<Boolean> f=requests.remove(p[1]);if(f!=null)f.completeExceptionally(new IOException(new String(Base64.getDecoder().decode(p[2]),StandardCharsets.UTF_8)));}
                    }
                }}
                synchronized(ClipAudio.class){if(epoch==version&&desired)throw new IOException("Windows output capture stopped");}
            }catch(Exception ex){synchronized(ClipAudio.class){if(epoch!=version)return;status="Audio unavailable: "+ex.getMessage();ready.complete(false);for(CompletableFuture<Boolean> f:requests.values())f.complete(false);requests.clear();if(process!=null)process.destroy();process=null;input=null;}}
        },"BridgeTap Windows audio");launch.setDaemon(true);launch.start();
    }
    public static Path snapshot(long first,long last,Path folder)throws Exception{
        CompletableFuture<Boolean> started;synchronized(ClipAudio.class){started=ready;if(!desired||started==null)throw new IOException(status);}
        if(!started.get(5,TimeUnit.SECONDS))throw new IOException(status);
        String id=UUID.randomUUID().toString();Path target=folder.resolve(".audio-"+id+".wav").toAbsolutePath();CompletableFuture<Boolean> result=new CompletableFuture<>();
        try{
            synchronized(ClipAudio.class){
                if(!desired||input==null)throw new IOException(status);requests.put(id,result);
                long now=System.nanoTime();double wall=System.currentTimeMillis();double start=wall-(now-first)/1e6,end=wall-(now-last)/1e6;
                input.write("SAVE "+id+" "+String.format(Locale.US,"%.3f %.3f",start,end)+" "+Base64.getEncoder().encodeToString(target.toString().getBytes(StandardCharsets.UTF_8))+"\n");input.flush();
            }
            if(!result.get(5,TimeUnit.SECONDS))throw new IOException(status);return target;
        }catch(Exception ex){Files.deleteIfExists(target);throw ex;}finally{requests.remove(id);}
    }
}
