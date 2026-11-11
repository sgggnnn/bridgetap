package com.bridger.momentum;

import java.io.*;
import java.util.zip.*;

/** Fast lossless RGB PNGs: no AWT pixel conversion or expensive PNG filtering. */
public final class ClipPng {
    private static final ThreadLocal<Deflater> compressors=ThreadLocal.withInitial(()->new Deflater(Deflater.BEST_SPEED));
    public static byte[] encode(byte[] rgb,int width,int height)throws IOException{
        if(width<1||height<1||rgb.length!=(long)width*height*3)throw new IllegalArgumentException("Invalid RGB frame");
        Deflater compressor=compressors.get();compressor.reset();ByteArrayOutputStream compressed=new ByteArrayOutputStream(Math.min(rgb.length,262144));
        try{
            DeflaterOutputStream zip=new DeflaterOutputStream(compressed,compressor,32768);byte[] filter={0};
            for(int y=height-1;y>=0;y--){zip.write(filter);zip.write(rgb,y*width*3,width*3);}zip.finish();
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(compressed.size()+100);DataOutputStream out=new DataOutputStream(bytes);
            out.writeLong(0x89504E470D0A1A0AL);ByteArrayOutputStream header=new ByteArrayOutputStream(13);DataOutputStream h=new DataOutputStream(header);
            h.writeInt(width);h.writeInt(height);h.write(new byte[]{8,2,0,0,0});chunk(out,0x49484452,header.toByteArray());
            ChunkOutput data=new ChunkOutput(out,compressed.size());compressed.writeTo(data);data.finish();chunk(out,0x49454E44,new byte[0]);return bytes.toByteArray();
        }finally{compressor.reset();}
    }
    private static void chunk(DataOutputStream out,int type,byte[] bytes)throws IOException{out.writeInt(bytes.length);out.writeInt(type);out.write(bytes);CRC32 crc=new CRC32();for(int shift=24;shift>=0;shift-=8)crc.update(type>>>shift&255);crc.update(bytes);out.writeInt((int)crc.getValue());}
    /** Writes the compressed image without making another full-frame byte array. */
    private static final class ChunkOutput extends OutputStream{
        private final DataOutputStream out;private final CRC32 crc=new CRC32();
        ChunkOutput(DataOutputStream out,int size)throws IOException{this.out=out;out.writeInt(size);out.writeInt(0x49444154);for(byte b:new byte[]{73,68,65,84})crc.update(b&255);}
        @Override public void write(int value)throws IOException{out.write(value);crc.update(value);}
        @Override public void write(byte[] bytes,int offset,int length)throws IOException{out.write(bytes,offset,length);crc.update(bytes,offset,length);}
        void finish()throws IOException{out.writeInt((int)crc.getValue());}
    }
}
