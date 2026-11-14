package com.bridger.momentum;
import java.io.*;
import java.util.*;

/** Recorded sky context only. The short-lived v6 chat section is discarded on import. */
public final class ReplayPresentation {
 public static final int MAX_BIOMES=2048;
 public final long worldTime;public final float rain,thunder;
 private final Map<Long,byte[]> biomes;
 public ReplayPresentation(long worldTime,float rain,float thunder,Map<Long,byte[]> biomes){
  if(!Float.isFinite(rain)||!Float.isFinite(thunder)||rain<0||rain>1||thunder<0||thunder>1||biomes.size()>MAX_BIOMES)throw new IllegalArgumentException("Invalid replay presentation");
  this.worldTime=worldTime;this.rain=rain;this.thunder=thunder;
  Map<Long,byte[]> copy=new HashMap<>();for(Map.Entry<Long,byte[]> e:biomes.entrySet()){if(e.getValue().length!=256)throw new IllegalArgumentException("Invalid biome array");copy.put(e.getKey(),e.getValue().clone());}this.biomes=Collections.unmodifiableMap(copy);
 }
 public byte[] biomes(int x,int z){byte[] b=biomes.get(key(x,z));return b==null?null:b.clone();}
 public static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
 public void write(DataOutputStream out)throws IOException{
  out.writeLong(worldTime);out.writeFloat(rain);out.writeFloat(thunder);out.writeInt(biomes.size());
  for(Map.Entry<Long,byte[]> e:biomes.entrySet()){out.writeLong(e.getKey());out.write(e.getValue());}
 }
 public static ReplayPresentation read(DataInputStream in,int frames,boolean legacyChat)throws IOException{
  long time=in.readLong();float rain=in.readFloat(),thunder=in.readFloat();int n=in.readInt();if(n<0||n>MAX_BIOMES)throw new IOException("Invalid biome count");
  Map<Long,byte[]> biomes=new HashMap<>();for(int i=0;i<n;i++){long key=in.readLong();byte[] b=new byte[256];in.readFully(b);if(biomes.put(key,b)!=null)throw new IOException("Duplicate biome chunk");}
  if(legacyChat){n=in.readInt();if(n<0||n>256)throw new IOException("Invalid legacy chat count");for(int i=0;i<n;i++){int tick=in.readInt();in.readInt();String ignored=in.readUTF();if(tick<0||tick>=frames||ignored.length()>8192)throw new IOException("Invalid legacy chat");}}
  return new ReplayPresentation(time,rain,thunder,biomes);
 }
}
