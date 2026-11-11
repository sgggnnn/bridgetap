package com.bridger.momentum;

import java.util.*;

/** Separates a paired scenery at its empty bridge corridor, including detached decorations. */
public final class IslandParts {
    public final boolean[] finish;public final int axis,direction;public final int[][] bounds=new int[2][6];public final boolean paired;
    public IslandParts(IslandTemplate template){
        finish=new boolean[template.blocks.size()];int best=0,cut=-1,chosen=0;
        for(int a=0;a<2;a++){
            boolean[] occupied=new boolean[a==0?template.width:template.depth];for(IslandTemplate.Block b:template.blocks)occupied[a==0?b.x:b.z]=true;
            int previous=-1;for(int p=0;p<occupied.length;p++)if(occupied[p]){if(previous>=0&&p-previous-1>best){best=p-previous-1;cut=previous;chosen=a;}previous=p;}
        }
        boolean taggedStart=false,taggedFinish=false;double sx=0,sz=0,ex=0,ez=0;int sc=0,ec=0;
        for(IslandTemplate.Block b:template.blocks){if(b.part==0){taggedStart=true;sx+=b.x;sz+=b.z;sc++;}if(b.part==1){taggedFinish=true;ex+=b.x;ez+=b.z;ec++;}}
        boolean explicit=taggedStart&&taggedFinish;int lower=0,upper=0;if(cut>=0)for(IslandTemplate.Block b:template.blocks){if((chosen==0?b.x:b.z)<=cut)lower++;else upper++;}
        boolean inferred=best>=4&&Math.min(lower,upper)>=Math.max(16,template.blocks.size()*.15);
        boolean fallbackSplit=true;
        if(explicit){chosen=Math.abs(ex/ec-sx/sc)>=Math.abs(ez/ec-sz/sc)?0:1;cut=0;}else if(template.bothIslands&&cut<0){int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;for(IslandTemplate.Block b:template.blocks){minX=Math.min(minX,b.x);maxX=Math.max(maxX,b.x);minZ=Math.min(minZ,b.z);maxZ=Math.max(maxZ,b.z);}chosen=maxX-minX>=maxZ-minZ?0:1;int min=chosen==0?minX:minZ,max=chosen==0?maxX:maxZ;cut=(min+max)/2;fallbackSplit=min<max;}
        axis=chosen;int anchor=axis==0?template.anchorX:template.anchorZ;direction=explicit?((axis==0?ex/ec-sx/sc:ez/ec-sz/sc)>=0?1:-1):anchor<=cut?1:-1;paired=explicit||(template.bothIslands||inferred)&&cut>=0&&fallbackSplit;
        for(int[] box:bounds){Arrays.fill(box,0,3,Integer.MAX_VALUE);Arrays.fill(box,3,6,Integer.MIN_VALUE);}
        for(int i=0;i<finish.length;i++){IslandTemplate.Block b=template.blocks.get(i);finish[i]=explicit?b.part==1:paired&&((axis==0?b.x:b.z)>cut)==(direction>0);int[] box=bounds[finish[i]?1:0];box[0]=Math.min(box[0],b.x);box[1]=Math.min(box[1],b.y);box[2]=Math.min(box[2],b.z);box[3]=Math.max(box[3],b.x+1);box[4]=Math.max(box[4],b.y+1);box[5]=Math.max(box[5],b.z+1);}
    }
    public int gap(int dx,int dz){if(!paired)return 0;int offset=axis==0?dx:dz;return direction>0?bounds[1][axis==0?0:2]+offset-bounds[0][axis==0?3:5]:bounds[0][axis==0?0:2]-bounds[1][axis==0?3:5]-offset;}
}
