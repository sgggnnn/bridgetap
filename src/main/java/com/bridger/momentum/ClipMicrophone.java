package com.bridger.momentum;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
/** Opt-in default microphone. Closed whenever the buffer or game capture stops. */
public final class ClipMicrophone {
 private static boolean desired;private static long epoch;private static TargetDataLine line;private static final ClipPcmBuffer buffer=new ClipPcmBuffer(48000);private static volatile String status="Microphone off";
 static{Runtime.getRuntime().addShutdownHook(new Thread(()->capture(false),"BridgeTap microphone shutdown"));}
 public static String status(){return status;}
 public static synchronized void capture(boolean enabled){if(enabled==desired)return;desired=enabled;long generation=++epoch;buffer.clear();if(line!=null){line.stop();line.close();line=null;}if(!enabled){status="Microphone off";return;}status="Starting default microphone...";
  Thread worker=new Thread(()->{TargetDataLine opened=null;try{AudioFormat format=new AudioFormat(48000,16,1,true,false);opened=(TargetDataLine)AudioSystem.getLine(new DataLine.Info(TargetDataLine.class,format));opened.open(format,9600);synchronized(ClipMicrophone.class){if(!desired||epoch!=generation){opened.close();return;}line=opened;opened.start();status="Microphone: default input / recording";}byte[] packet=new byte[1920];while(opened.isOpen()){int n=opened.read(packet,0,packet.length);synchronized(ClipMicrophone.class){if(epoch!=generation||!desired)break;if(n>0)buffer.add(System.nanoTime(),packet,n-(n&1));}}}catch(Exception ex){synchronized(ClipMicrophone.class){if(epoch==generation)status="Microphone unavailable: "+ex.getMessage();}}finally{if(opened!=null)opened.close();}},"BridgeTap microphone");worker.setDaemon(true);worker.start();
 }
 public static Path snapshot(long first,long last,Path folder)throws IOException{synchronized(ClipMicrophone.class){if(!desired)throw new IOException("Microphone is off");}Path file=folder.resolve(".microphone-"+UUID.randomUUID()+".wav");try{buffer.wave(first,last,file);return file;}catch(IOException ex){Files.deleteIfExists(file);throw ex;}}
}
