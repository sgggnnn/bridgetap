package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Versioned local recording, including identity, pose and exact block metadata. */
public final class ReplayData {
 public static final int MAX_FRAMES=12000,MAX_BLOCKS=48000;
 public static final class Frame {
  public final double x,y,z;
  public final float yaw,pitch,headYaw,bodyYaw,swing,walkSpeed,walkProgress;
  public final boolean sneaking,onGround;
  public final int itemId,itemDamage;
  public Frame(double x,double y,double z){this(x,y,z,0,0,0,0,false,false,0,0,0,0,0);}
  public Frame(double x,double y,double z,float yaw,float pitch,float headYaw,float bodyYaw,boolean sneaking,boolean onGround,float swing,float walkSpeed,float walkProgress,int itemId,int itemDamage){
   if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>30000000||Math.abs(z)>30000000||Math.abs(y)>4096)throw new IllegalArgumentException("Invalid replay position");
   for(float a:new float[]{yaw,pitch,headYaw,bodyYaw,swing,walkSpeed,walkProgress})if(!Float.isFinite(a)||Math.abs(a)>1e9)throw new IllegalArgumentException("Invalid replay pose");
   if(itemId<0||itemId>32767||itemDamage<0||itemDamage>65535)throw new IllegalArgumentException("Invalid replay item");
   this.x=x;this.y=y;this.z=z;this.yaw=yaw;this.pitch=pitch;this.headYaw=headYaw;this.bodyYaw=bodyYaw;
   this.sneaking=sneaking;this.onGround=onGround;this.swing=swing;this.walkSpeed=walkSpeed;this.walkProgress=walkProgress;this.itemId=itemId;this.itemDamage=itemDamage;
  }
 }
 public static final class Block {
  public final int x,y,z,id,metadata,tick;
  public Block(int x,int y,int z,int id,int tick){this(x,y,z,id,0,tick);}
  public Block(int x,int y,int z,int id,int metadata,int tick){
   if(Math.abs((long)x)>30000000||Math.abs((long)z)>30000000||y<0||y>255||id<0||id>4095||metadata<0||metadata>15||tick<0||tick>=MAX_FRAMES)throw new IllegalArgumentException("Invalid replay block");
   this.x=x;this.y=y;this.z=z;this.id=id;this.metadata=metadata;this.tick=tick;
  }
  public long key(){return ((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|(long)y;}
  public boolean sameState(Block b){return b!=null&&id==b.id&&metadata==b.metadata;}
 }
 public static final class Identity {
  public final String uuid,name,displayName,textureValue,textureSignature,model;
  public final int modelParts;
  public Identity(String uuid,String name,String displayName,String textureValue,String textureSignature,String model){
   this(uuid,name,displayName,textureValue,textureSignature,model,127);
  }
  public Identity(String uuid,String name,String displayName,String textureValue,String textureSignature,String model,int modelParts){
   UUID.fromString(uuid);
   if(name.length()>64||displayName.length()>512||textureValue.length()>16000||textureSignature.length()>16000)throw new IllegalArgumentException("Invalid identity");
   this.uuid=uuid;this.name=name;this.displayName=displayName;this.textureValue=textureValue;this.textureSignature=textureSignature;this.model="slim".equals(model)?"slim":"default";
   if(modelParts<0||modelParts>127)throw new IllegalArgumentException("Invalid skin layers");this.modelParts=modelParts;
  }
  public static Identity unknown(){return new Identity("00000000-0000-0000-0000-000000000000","Player","Player","","","default");}
 }
 public final List<Frame> frames;
 public final List<Block> blocks;
 public final Identity identity;
 public final int dimension,formatVersion;
 public final long dayTime;
 public final boolean truncated;
 public ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated){this(frames,blocks,truncated,Identity.unknown(),0,6000,2);}
 public ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated,Identity identity,int dimension,long dayTime){this(frames,blocks,truncated,identity,dimension,dayTime,2);}
 private ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated,Identity identity,int dimension,long dayTime,int version){
  if(frames.isEmpty()||frames.size()>MAX_FRAMES||blocks.size()>MAX_BLOCKS)throw new IllegalArgumentException("Invalid recording size");
  for(Block b:blocks)if(b.tick>=frames.size())throw new IllegalArgumentException("Block outside recording");
  this.frames=Collections.unmodifiableList(new ArrayList<>(frames));this.blocks=Collections.unmodifiableList(new ArrayList<>(blocks));
  this.truncated=truncated;this.identity=identity;this.dimension=dimension;this.dayTime=dayTime;this.formatVersion=version;
 }
 public void write(Path file)throws IOException {
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file,StandardOpenOption.CREATE_NEW)))){
   out.writeInt(0x42545250);out.writeInt(2);out.writeBoolean(truncated);out.writeInt(frames.size());out.writeInt(blocks.size());
   out.writeUTF(identity.uuid);out.writeUTF(identity.name);out.writeUTF(identity.displayName);out.writeUTF(identity.textureValue);out.writeUTF(identity.textureSignature);out.writeUTF(identity.model);
   out.writeInt(identity.modelParts);
   out.writeInt(dimension);out.writeLong(dayTime);
   for(Frame f:frames){
    out.writeDouble(f.x);out.writeDouble(f.y);out.writeDouble(f.z);out.writeFloat(f.yaw);out.writeFloat(f.pitch);out.writeFloat(f.headYaw);out.writeFloat(f.bodyYaw);
    out.writeBoolean(f.sneaking);out.writeBoolean(f.onGround);out.writeFloat(f.swing);out.writeFloat(f.walkSpeed);out.writeFloat(f.walkProgress);out.writeInt(f.itemId);out.writeInt(f.itemDamage);
   }
   for(Block b:blocks){out.writeInt(b.x);out.writeInt(b.y);out.writeInt(b.z);out.writeInt(b.id);out.writeInt(b.metadata);out.writeInt(b.tick);}
  }
 }
 public static ReplayData read(Path file)throws IOException {
  if(Files.size(file)>8*1024*1024)throw new IOException("Replay too large");
  try(DataInputStream in=new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))){
   if(in.readInt()!=0x42545250)throw new IOException("Invalid replay header");
   int version=in.readInt();if(version!=1&&version!=2)throw new IOException("Unsupported replay version");
   boolean truncated=in.readBoolean();int nf=in.readInt(),nb=in.readInt();
   if(nf<1||nf>MAX_FRAMES||nb<0||nb>MAX_BLOCKS)throw new IOException("Invalid replay counts");
   Identity who=version==1?Identity.unknown():new Identity(in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readInt());
   int dimension=version==1?0:in.readInt();long dayTime=version==1?6000:in.readLong();
   if(dimension!=-1&&dimension!=0&&dimension!=1)throw new IOException("Invalid dimension");
   List<Frame> frames=new ArrayList<>();List<Block> blocks=new ArrayList<>();
   for(int i=0;i<nf;i++){
    double x=in.readDouble(),y=in.readDouble(),z=in.readDouble();
    frames.add(version==1?new Frame(x,y,z):new Frame(x,y,z,in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readBoolean(),in.readBoolean(),in.readFloat(),in.readFloat(),in.readFloat(),in.readInt(),in.readInt()));
   }
   for(int i=0;i<nb;i++){
    int x=in.readInt(),y=in.readInt(),z=in.readInt(),id=in.readInt(),metadata=version==1?0:in.readInt(),tick=in.readInt();
    if(tick<0||tick>=nf)throw new IOException("Invalid block timing");
    blocks.add(new Block(x,y,z,id,metadata,tick));
   }
   return new ReplayData(frames,blocks,truncated,who,dimension,dayTime,version);
  }catch(IllegalArgumentException ex){throw new IOException("Invalid replay",ex);}
 }
}
