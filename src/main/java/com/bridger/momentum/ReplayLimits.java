package com.bridger.momentum;
import java.io.*;

/** Bounds apply to bytes actually read, including after decompression. */
public final class ReplayLimits {
 public static final int ARCHIVE=10*1024*1024,RECORDING=8*1024*1024,INFLATED=16*1024*1024;
 private ReplayLimits(){}
 public static InputStream bounded(InputStream source,final int limit){return new FilterInputStream(source){
  private long count;
  private void add(long n)throws IOException{if(n>0&&(count+=n)>limit)throw new IOException("Replay exceeds its uncompressed size limit");}
  @Override public int read()throws IOException{int b=in.read();if(b>=0)add(1);return b;}
  @Override public int read(byte[] b,int o,int n)throws IOException{int r=in.read(b,o,(int)Math.min(n,Math.max(1,limit-count+1)));add(r);return r;}
  @Override public long skip(long n)throws IOException{byte[] b=new byte[(int)Math.min(8192,Math.max(1,n))];long total=0;while(total<n){int r=read(b,0,(int)Math.min(b.length,n-total));if(r<0)break;total+=r;}return total;}
 };}
}
