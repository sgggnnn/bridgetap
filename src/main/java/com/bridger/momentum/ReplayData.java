package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Versioned local recording, including identity, pose and exact block metadata. */
public final class ReplayData {
 public static final int MAX_FRAMES=12000,MAX_BLOCKS=48000;
 public static final class TickStats {
  public final int held,pressed,released,leftClicks,rightClicks,middleClicks,leftCps,rightCps,middleCps;
  public final double vx,vy,vz;
  public TickStats withoutEdges(){return new TickStats(held,0,0,0,0,0,leftCps,rightCps,middleCps,vx,vy,vz);}
  public TickStats(int held,int pressed,int released,int leftClicks,int rightClicks,int middleClicks,int leftCps,int rightCps,int middleCps,double vx,double vy,double vz){
   if((held|pressed|released)<0||((held|pressed|released)&~1023)!=0)throw new IllegalArgumentException("Invalid input mask");
   for(int count:new int[]{leftClicks,rightClicks,middleClicks,leftCps,rightCps,middleCps})if(count<0||count>1000000)throw new IllegalArgumentException("Invalid click count");
   for(double v:new double[]{vx,vy,vz})if(!Double.isFinite(v)||Math.abs(v)>1000000)throw new IllegalArgumentException("Invalid velocity");
   this.held=held;this.pressed=pressed;this.released=released;this.leftClicks=leftClicks;this.rightClicks=rightClicks;this.middleClicks=middleClicks;
   this.leftCps=leftCps;this.rightCps=rightCps;this.middleCps=middleCps;this.vx=vx;this.vy=vy;this.vz=vz;
  }
 }
 public static final class Frame {
  public final double x,y,z;
  public final float yaw,pitch,headYaw,bodyYaw,swing,walkSpeed,walkProgress;
  public final boolean sneaking,onGround;
  public final int itemId,itemDamage;
  public final TickStats stats;
  public Frame withStats(TickStats stats){return new Frame(x,y,z,yaw,pitch,headYaw,bodyYaw,sneaking,onGround,swing,walkSpeed,walkProgress,itemId,itemDamage,stats);}
  public Frame(double x,double y,double z){this(x,y,z,0,0,0,0,false,false,0,0,0,0,0);}
  public Frame(double x,double y,double z,float yaw,float pitch,float headYaw,float bodyYaw,boolean sneaking,boolean onGround,float swing,float walkSpeed,float walkProgress,int itemId,int itemDamage){
   this(x,y,z,yaw,pitch,headYaw,bodyYaw,sneaking,onGround,swing,walkSpeed,walkProgress,itemId,itemDamage,null);
  }
  public Frame(double x,double y,double z,float yaw,float pitch,float headYaw,float bodyYaw,boolean sneaking,boolean onGround,float swing,float walkSpeed,float walkProgress,int itemId,int itemDamage,TickStats stats){
   if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>30000000||Math.abs(z)>30000000||Math.abs(y)>4096)throw new IllegalArgumentException("Invalid replay position");
   for(float a:new float[]{yaw,pitch,headYaw,bodyYaw,swing,walkSpeed,walkProgress})if(!Float.isFinite(a)||Math.abs(a)>1e9)throw new IllegalArgumentException("Invalid replay pose");
   if(itemId<0||itemId>32767||itemDamage<0||itemDamage>65535)throw new IllegalArgumentException("Invalid replay item");
   this.x=x;this.y=y;this.z=z;this.yaw=yaw;this.pitch=pitch;this.headYaw=headYaw;this.bodyYaw=bodyYaw;
   this.sneaking=sneaking;this.onGround=onGround;this.swing=swing;this.walkSpeed=walkSpeed;this.walkProgress=walkProgress;this.itemId=itemId;this.itemDamage=itemDamage;
   this.stats=stats;
  }
 }
 public static final class Block {
  public final int x,y,z,id,metadata,tick;
  public final boolean placed;
  public Block(int x,int y,int z,int id,int tick){this(x,y,z,id,0,tick);}
  public Block(int x,int y,int z,int id,int metadata,int tick){
   this(x,y,z,id,metadata,tick,tick>0);
  }
  public Block(int x,int y,int z,int id,int metadata,int tick,boolean placed){
   if(Math.abs((long)x)>30000000||Math.abs((long)z)>30000000||y<0||y>255||id<0||id>4095||metadata<0||metadata>15||tick<0||tick>=MAX_FRAMES)throw new IllegalArgumentException("Invalid replay block");
   this.x=x;this.y=y;this.z=z;this.id=id;this.metadata=metadata;this.tick=tick;
   this.placed=placed;
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
 public final boolean placementTags;
 private BridgeGeometry.Result geometry;
 public ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated){this(frames,blocks,truncated,Identity.unknown(),0,6000,5,true);}
 public ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated,Identity identity,int dimension,long dayTime){this(frames,blocks,truncated,identity,dimension,dayTime,5,true);}
 private ReplayData(List<Frame> frames,List<Block> blocks,boolean truncated,Identity identity,int dimension,long dayTime,int version,boolean placementTags){
  if(frames.isEmpty()||frames.size()>MAX_FRAMES||blocks.size()>MAX_BLOCKS)throw new IllegalArgumentException("Invalid recording size");
  for(Block b:blocks)if(b.tick>=frames.size())throw new IllegalArgumentException("Block outside recording");
  this.frames=Collections.unmodifiableList(new ArrayList<>(frames));this.blocks=Collections.unmodifiableList(new ArrayList<>(blocks));
  this.truncated=truncated;this.identity=identity;this.dimension=dimension;this.dayTime=dayTime;this.formatVersion=version;
  this.placementTags=placementTags;
 }
 public BridgeGeometry.Result geometry(){if(geometry==null)geometry=BridgeGeometry.measure(this);return geometry;}
 public void write(Path file)throws IOException {
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file,StandardOpenOption.CREATE_NEW)))){
   out.writeInt(0x42545250);out.writeInt(formatVersion>=5?5:4);out.writeBoolean(truncated);out.writeInt(frames.size());out.writeInt(blocks.size());out.writeBoolean(placementTags);
   out.writeUTF(identity.uuid);out.writeUTF(identity.name);out.writeUTF(identity.displayName);out.writeUTF(identity.textureValue);out.writeUTF(identity.textureSignature);out.writeUTF(identity.model);
   out.writeInt(identity.modelParts);
   out.writeInt(dimension);out.writeLong(dayTime);
   for(Frame f:frames){
    out.writeDouble(f.x);out.writeDouble(f.y);out.writeDouble(f.z);out.writeFloat(f.yaw);out.writeFloat(f.pitch);out.writeFloat(f.headYaw);out.writeFloat(f.bodyYaw);
    out.writeBoolean(f.sneaking);out.writeBoolean(f.onGround);out.writeFloat(f.swing);out.writeFloat(f.walkSpeed);out.writeFloat(f.walkProgress);out.writeInt(f.itemId);out.writeInt(f.itemDamage);
    out.writeBoolean(f.stats!=null);
    if(f.stats!=null){
     TickStats s=f.stats;out.writeInt(s.held);out.writeInt(s.pressed);out.writeInt(s.released);
     out.writeInt(s.leftClicks);out.writeInt(s.rightClicks);out.writeInt(s.middleClicks);out.writeInt(s.leftCps);out.writeInt(s.rightCps);out.writeInt(s.middleCps);
     out.writeDouble(s.vx);out.writeDouble(s.vy);out.writeDouble(s.vz);
    }
   }
   for(Block b:blocks){out.writeInt(b.x);out.writeInt(b.y);out.writeInt(b.z);out.writeInt(b.id);out.writeInt(b.metadata);out.writeInt(b.tick);out.writeBoolean(b.placed);}
  }
 }
 public static ReplayData read(Path file)throws IOException {
  if(Files.size(file)>8*1024*1024)throw new IOException("Replay too large");
  try(DataInputStream in=new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))){
   if(in.readInt()!=0x42545250)throw new IOException("Invalid replay header");
   int version=in.readInt();if(version<1||version>5)throw new IOException("Unsupported replay version");
   boolean truncated=in.readBoolean();int nf=in.readInt(),nb=in.readInt();
   if(nf<1||nf>MAX_FRAMES||nb<0||nb>MAX_BLOCKS)throw new IOException("Invalid replay counts");
   boolean tags=version>=4&&in.readBoolean();
   Identity who=version==1?Identity.unknown():new Identity(in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readInt());
   int dimension=version==1?0:in.readInt();long dayTime=version==1?6000:in.readLong();
   if(dimension!=-1&&dimension!=0&&dimension!=1)throw new IOException("Invalid dimension");
   List<Frame> frames=new ArrayList<>();List<Block> blocks=new ArrayList<>();
   for(int i=0;i<nf;i++){
    double x=in.readDouble(),y=in.readDouble(),z=in.readDouble();
    if(version==1){frames.add(new Frame(x,y,z));continue;}
    float yaw=in.readFloat(),pitch=in.readFloat(),head=in.readFloat(),body=in.readFloat();boolean sneak=in.readBoolean(),ground=in.readBoolean();
    float swing=in.readFloat(),walk=in.readFloat(),progress=in.readFloat();int item=in.readInt(),damage=in.readInt();
    TickStats stats=version>=3&&in.readBoolean()?new TickStats(in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readDouble(),in.readDouble(),in.readDouble()):null;
    frames.add(new Frame(x,y,z,yaw,pitch,head,body,sneak,ground,swing,walk,progress,item,damage,stats));
   }
   for(int i=0;i<nb;i++){
    int x=in.readInt(),y=in.readInt(),z=in.readInt(),id=in.readInt(),metadata=version==1?0:in.readInt(),tick=in.readInt();
    if(tick<0||tick>=nf)throw new IOException("Invalid block timing");
    blocks.add(new Block(x,y,z,id,metadata,tick,version>=4?in.readBoolean():tick>0));
   }
   if(in.read()!=-1)throw new IOException("Unexpected trailing replay data");
   return new ReplayData(frames,blocks,truncated,who,dimension,dayTime,version,tags);
  }catch(IllegalArgumentException ex){throw new IOException("Invalid replay",ex);}
 }
}
