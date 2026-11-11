package com.bridger.momentum;

/** Integer quarter turns around the attachment block's centre; no voxel rounding. */
public final class IslandRotation {
    public static int[] point(int x,int z,int anchorX,int anchorZ,int turns){int dx=x-anchorX,dz=z-anchorZ;for(int i=0;i<Math.floorMod(turns,4);i++){int next=-dz;dz=dx;dx=next;}return new int[]{anchorX+dx,anchorZ+dz};}
    public static int[] bounds(int[] box,int ax,int az,int turns){int[] result={Integer.MAX_VALUE,box[1],Integer.MAX_VALUE,Integer.MIN_VALUE,box[4],Integer.MIN_VALUE};for(int x:new int[]{box[0],box[3]-1})for(int z:new int[]{box[2],box[5]-1}){int[] p=point(x,z,ax,az,turns);result[0]=Math.min(result[0],p[0]);result[2]=Math.min(result[2],p[1]);result[3]=Math.max(result[3],p[0]+1);result[5]=Math.max(result[5],p[1]+1);}return result;}
}
