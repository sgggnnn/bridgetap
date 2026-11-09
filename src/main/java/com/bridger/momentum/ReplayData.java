package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Bounded, versioned client-side recording; never applies changes to a live world. */
public final class ReplayData {
 public static final int MAX_FRAMES=12000, MAX_BLOCKS=24000;
 public static final class Frame {
  public final double x,y,z;
  public Frame(double x,double y,double z){
   if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>30000000||Math.abs(z)>30000000||Math.abs(y)>4096)throw new IllegalArgumentException("Invalid replay position");
   this.x=x;this.y=y;this.z=z;
  }
 }
 public static final class Block {
  public final int x,y,z,id,tick;
  public Block(int x,int y,int z,int id,int tick){this.x=x;this.y=y;this.z=z;this.id=id;this.tick=tick;}
 }
 public final List<Frame> frames;
 public final List<Block> blocks;
 public final boolean truncated;
 public ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated){
  this.frames=Collections.unmodifiableList(new ArrayList<>(frames));
  this.blocks=Collections.unmodifiableList(new ArrayList<>(blocks));this.truncated=truncated;
 }
 public void write(Path file)throws IOException {
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file,StandardOpenOption.CREATE_NEW)))){
   out.writeInt(0x42545250);out.writeInt(1);out.writeBoolean(truncated);out.writeInt(frames.size());out.writeInt(blocks.size());
   for(Frame f:frames){out.writeDouble(f.x);out.writeDouble(f.y);out.writeDouble(f.z);}
   for(Block b:blocks){out.writeInt(b.x);out.writeInt(b.y);out.writeInt(b.z);out.writeInt(b.id);out.writeInt(b.tick);}
  }
 }
 public static ReplayData read(Path file)throws IOException {
  if(Files.size(file)>4*1024*1024)throw new IOException("Replay too large");
  try(DataInputStream in=new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))){
   if(in.readInt()!=0x42545250||in.readInt()!=1)throw new IOException("Unsupported replay version");
   boolean truncated=in.readBoolean();int nf=in.readInt(),nb=in.readInt();
   if(nf<1||nf>MAX_FRAMES||nb<0||nb>MAX_BLOCKS)throw new IOException("Invalid replay counts");
   List<Frame> frames=new ArrayList<>();List<Block> blocks=new ArrayList<>();
   for(int i=0;i<nf;i++)frames.add(new Frame(in.readDouble(),in.readDouble(),in.readDouble()));
   for(int i=0;i<nb;i++){
    int x=in.readInt(),y=in.readInt(),z=in.readInt(),id=in.readInt(),tick=in.readInt();
    if(Math.abs((long)x)>30000000||Math.abs((long)z)>30000000||y<0||y>255||id<0||id>4095||tick<0||tick>=nf)throw new IOException("Invalid replay block");
    blocks.add(new Block(x,y,z,id,tick));
   }
   return new ReplayData(frames,blocks,truncated);
  }catch(IllegalArgumentException ex){throw new IOException("Invalid replay",ex);}
 }
}
