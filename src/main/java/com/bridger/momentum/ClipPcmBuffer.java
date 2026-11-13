package com.bridger.momentum;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.sound.sampled.*;
/** Bounded timestamped mono PCM16 ring; never opens a recording device. */
public final class ClipPcmBuffer {
 private static final class Packet{final long end;final byte[] data;Packet(long end,byte[] data){this.end=end;this.data=data;}}
 private final int rate;private final ArrayDeque<Packet> packets=new ArrayDeque<>();private int bytes;
 public ClipPcmBuffer(int rate){if(rate<8000||rate>192000)throw new IllegalArgumentException("Invalid sample rate");this.rate=rate;}
 public synchronized void clear(){packets.clear();bytes=0;}
 public synchronized void add(long end,byte[] pcm,int length){if(length<=0||length>rate*2||length>pcm.length||(length&1)!=0)throw new IllegalArgumentException("Invalid PCM packet");
  // read() completion measures thread scheduling, not the audio sample clock. Placing
  // each packet at that timestamp cuts/duplicates samples and makes speech robotic.
  if(!packets.isEmpty()){long expected=packets.peekLast().end+Math.round(length/2.0/rate*1e9);if(Math.abs(end-expected)<100000000L)end=expected;}
  packets.add(new Packet(end,Arrays.copyOf(pcm,length)));bytes+=length;while(!packets.isEmpty()&&(packets.peek().end<end-65000000000L||bytes>rate*2*66)){bytes-=packets.remove().data.length;}}
 public synchronized byte[] snapshot(long first,long last)throws IOException{if(last<=first||last-first>62000000000L)throw new IOException("Invalid microphone interval");int frames=(int)Math.ceil((last-first)/1e9*rate);byte[] result=new byte[frames*2];boolean found=false;for(Packet p:packets){long start=p.end-Math.round(p.data.length/2.0/rate*1e9);int offset=(int)Math.round((start-first)/1e9*rate),source=Math.max(0,-offset),target=Math.max(0,offset),take=Math.min(p.data.length/2-source,frames-target);if(take>0){System.arraycopy(p.data,source*2,result,target*2,take*2);found=true;}}if(!found)throw new IOException("No microphone audio recorded for this clip");return result;}
 public void wave(long first,long last,Path file)throws IOException{byte[] pcm=snapshot(first,last);try(AudioInputStream audio=new AudioInputStream(new ByteArrayInputStream(pcm),new AudioFormat(rate,16,1,true,false),pcm.length/2)){if(Files.exists(file))throw new IOException("Audio target already exists");AudioSystem.write(audio,AudioFileFormat.Type.WAVE,file.toFile());}}
}
