package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Portable static block scenery. No player, inventory or tile-entity data. */
public final class IslandTemplate {
    public static final int MAX_SIDE=512,MAX_BLOCKS=131072,MAX_VOLUME=1048576;
    public final String name;
    public final int width,height,depth,originX,originY,originZ;
    public final int anchorX,anchorY,anchorZ;
    public final boolean bothIslands;
    public final List<Block> blocks;
    public static final class Block {
        public final int x,y,z,meta,part;public final String key;
        public Block(int x,int y,int z,String key,int meta){this(x,y,z,key,meta,-1);}
        public Block(int x,int y,int z,String key,int meta,int part){this.x=x;this.y=y;this.z=z;this.key=key;this.meta=meta;this.part=part;}
    }
    public IslandTemplate(String name,int w,int h,int d,int ox,int oy,int oz,List<Block> blocks){
        this(name,w,h,d,ox,oy,oz,blocks,w/2,h,d/2);
    }
    public IslandTemplate(String name,int w,int h,int d,int ox,int oy,int oz,List<Block> blocks,int ax,int ay,int az){
        this(name,w,h,d,ox,oy,oz,blocks,ax,ay,az,false);
    }
    public IslandTemplate(String name,int w,int h,int d,int ox,int oy,int oz,List<Block> blocks,int ax,int ay,int az,boolean both){
        if(name==null||name.trim().isEmpty()||name.length()>64||name.matches(".*[\\p{Cntrl}].*"))throw new IllegalArgumentException("Name must contain 1-64 printable characters");
        if(w<1||h<1||d<1||w>MAX_SIDE||h>256||d>MAX_SIDE||(long)w*h*d>MAX_VOLUME)throw new IllegalArgumentException("Selection exceeds 512 horizontal blocks or 1048576 cells");
        if(Math.abs((long)ox)>30000000||Math.abs((long)oz)>30000000||oy<0||oy+h>256)throw new IllegalArgumentException("Invalid original position");
        if(blocks.isEmpty()||blocks.size()>MAX_BLOCKS)throw new IllegalArgumentException("Select 1-131072 static blocks");
        Set<Integer> positions=new HashSet<>();
        for(Block b:blocks){
            if(b.x<0||b.x>=w||b.y<0||b.y>=h||b.z<0||b.z>=d||b.meta<0||b.meta>15||b.part< -1||b.part>1||b.key==null||b.key.length()>128||!b.key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid block entry");
            if(!positions.add((b.x*h+b.y)*d+b.z))throw new IllegalArgumentException("Duplicate block position");
        }
        this.name=name.trim();width=w;height=h;depth=d;originX=ox;originY=oy;originZ=oz;this.blocks=Collections.unmodifiableList(new ArrayList<>(blocks));
        if(ax<0||ax>=w||ay<0||ay>h+12||az<0||az>=d)throw new IllegalArgumentException("Invalid island attachment point");anchorX=ax;anchorY=ay;anchorZ=az;
        bothIslands=both;
    }
    public void write(Path path)throws IOException{
        try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(path,StandardOpenOption.CREATE_NEW)))){
            out.writeInt(0x42544953);out.writeInt(4);out.writeUTF(name);out.writeInt(width);out.writeInt(height);out.writeInt(depth);
            out.writeInt(originX);out.writeInt(originY);out.writeInt(originZ);out.writeInt(anchorX);out.writeInt(anchorY);out.writeInt(anchorZ);out.writeBoolean(bothIslands);out.writeInt(blocks.size());
            for(Block b:blocks){out.writeShort(b.x);out.writeShort(b.y);out.writeShort(b.z);out.writeUTF(b.key);out.writeByte(b.meta);out.writeByte(b.part+1);}
        }
    }
    public static IslandTemplate read(Path path)throws IOException{
        if(Files.size(path)>32*1024*1024)throw new IOException("Island file exceeds 32 MiB");
        try(DataInputStream in=new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))){
            if(in.readInt()!=0x42544953)throw new IOException("Unsupported island format");int version=in.readInt();if(version<1||version>4)throw new IOException("Unsupported island format");
            String name=in.readUTF();int w=in.readInt(),h=in.readInt(),d=in.readInt(),x=in.readInt(),y=in.readInt(),z=in.readInt();
            int ax=version>=2?in.readInt():w/2,ay=version>=2?in.readInt():h,az=version>=2?in.readInt():d/2;boolean both=version>=3&&in.readBoolean();int count=in.readInt();
            if(count<1||count>MAX_BLOCKS)throw new IOException("Invalid block count");
            List<Block> blocks=new ArrayList<>();for(int i=0;i<count;i++){
                int bx=version>=4?in.readUnsignedShort():in.readUnsignedByte(),by=version>=4?in.readUnsignedShort():in.readUnsignedByte(),bz=version>=4?in.readUnsignedShort():in.readUnsignedByte();String key=in.readUTF();int meta=in.readUnsignedByte(),part=version>=4?in.readUnsignedByte()-1:-1;
                if(key.length()>128)throw new IOException("Invalid block key");blocks.add(new Block(bx,by,bz,key,meta,part));
            }
            if(in.read()!=-1)throw new IOException("Trailing island data");
            return new IslandTemplate(name,w,h,d,x,y,z,blocks,ax,ay,az,both);
        }catch(IllegalArgumentException ex){throw new IOException(ex.getMessage(),ex);}
    }
    public static String readName(Path path)throws IOException{
        if(Files.size(path)>32*1024*1024)throw new IOException("Island file exceeds 32 MiB");
        try(DataInputStream in=new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))){
            if(in.readInt()!=0x42544953)throw new IOException("Unsupported island format");int version=in.readInt();if(version<1||version>4)throw new IOException("Unsupported island format");String name=in.readUTF();if(name.length()>64||name.trim().isEmpty())throw new IOException("Invalid island name");return name;
        }
    }
}
