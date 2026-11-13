package com.bridger.momentum;

import java.util.Set;
import java.util.function.BiConsumer;

/** Optimized renderers require a complete neighboring chunk ring before meshing/culling. */
public final class ReplayChunkCoverage {
 private ReplayChunkCoverage(){}
 public static void ensure(Set<Long> loaded,Set<Long> covered,int x,int z,BiConsumer<Integer,Integer> load){
  if(!covered.add(key(x,z)))return;
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
   int cx=x+dx,cz=z+dz;
   if(loaded.add(key(cx,cz)))load.accept(cx,cz);
  }
 }
 private static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
}
