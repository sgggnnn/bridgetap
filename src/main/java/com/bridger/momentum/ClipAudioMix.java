package com.bridger.momentum;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
/** Mix recorded snapshots at export time, leaving the recording thread lightweight. */
public final class ClipAudioMix {
 public static Path snapshot(long first,long last,Path folder,boolean system,boolean mic)throws Exception{Path output=null,input=null;try{
  Exception error=null;if(system)try{output=ClipAudio.snapshot(first,last,folder);}catch(Exception ex){error=ex;}if(mic)try{input=ClipMicrophone.snapshot(first,last,folder);}catch(Exception ex){error=ex;}
  if(output==null&&input==null)throw new IOException(error==null?"No audio enabled":error.getMessage());
  if(error!=null)System.err.println("[BridgeTap] One audio source unavailable: "+error.getMessage());
  if(output==null){Path result=input;input=null;return result;}if(input==null){Path result=output;output=null;return result;}
  return mix(output,input,folder);
 }finally{if(output!=null)Files.deleteIfExists(output);if(input!=null)Files.deleteIfExists(input);}}
 public static Path mix(Path output,Path input,Path folder)throws Exception{
  Path mixed=folder.resolve(".mixed-"+UUID.randomUUID()+".wav"),log=folder.resolve(".mix-"+UUID.randomUUID()+".log");Process process=null;boolean success=false;try{
   process=new ProcessBuilder(NativeClipTools.tool("ffmpeg.exe").toString(),"-hide_banner","-loglevel","error","-nostdin","-n","-i",output.toAbsolutePath().toString(),"-i",input.toAbsolutePath().toString(),"-filter_complex","[0:a]aresample=48000,aformat=sample_fmts=fltp:channel_layouts=stereo[game];[1:a]aresample=48000,aformat=sample_fmts=fltp:channel_layouts=stereo[mic];[game][mic]amix=inputs=2:duration=longest:normalize=0,alimiter=limit=0.95:level=0:latency=1","-ar","48000","-ac","2","-c:a","pcm_s16le",mixed.toAbsolutePath().toString()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
   if(!process.waitFor(20,TimeUnit.SECONDS)||process.exitValue()!=0)throw new IOException("Audio mix failed");success=true;return mixed;
  }finally{if(process!=null&&process.isAlive())process.destroyForcibly();Files.deleteIfExists(log);if(!success)Files.deleteIfExists(mixed);}
 }
}
