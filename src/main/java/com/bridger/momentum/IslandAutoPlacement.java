package com.bridger.momentum;
import java.util.*;

/** Align walkable deck edges, not decorative bounds or the block under the camera. */
public final class IslandAutoPlacement {
 public static final class Point {public final int x,y,z;public Point(int x,int y,int z){this.x=x;this.y=y;this.z=z;}}
 public static final class Plan {
  public final int x,y,z,rotation,finishX,finishY,finishZ;
  Plan(int x,int y,int z,int rotation,int fx,int fy,int fz){this.x=x;this.y=y;this.z=z;this.rotation=rotation;finishX=fx;finishY=fy;finishZ=fz;}
 }
 private static boolean floorBlock(IslandTemplate.Block b){
  String k=b.key;return !(k.endsWith("pressure_plate")||k.endsWith("_button")||k.endsWith("_torch")||k.equals("minecraft:torch")||k.equals("minecraft:standing_sign")||k.equals("minecraft:wall_sign")||k.equals("minecraft:carpet")||k.equals("minecraft:tallgrass")||k.equals("minecraft:flower_pot")||k.equals("minecraft:yellow_flower")||k.equals("minecraft:red_flower")||k.equals("minecraft:vine")||k.equals("minecraft:tripwire"));
 }
 private static boolean broad(List<IslandTemplate.Block> cells){Set<Long> occupied=new HashSet<>();for(IslandTemplate.Block b:cells)occupied.add(((long)b.x<<32)^(b.z&0xffffffffL));for(IslandTemplate.Block b:cells)for(int turn=0;turn<2;turn++){boolean complete=true;for(int x=0;x<3&&complete;x++)for(int z=0;z<2;z++)if(!occupied.contains(((long)(b.x+(turn==0?x:z))<<32)^((b.z+(turn==0?z:x))&0xffffffffL))){complete=false;break;}if(complete)return true;}return false;}
 public static int[] deck(IslandTemplate t,IslandParts parts,int group){
  Set<Integer> occupied=new HashSet<>();for(int i=0;i<t.blocks.size();i++)if((parts.finish[i]?1:0)==group&&floorBlock(t.blocks.get(i))){IslandTemplate.Block b=t.blocks.get(i);occupied.add((b.x*512+b.z)*256+b.y);}
  int[] counts=new int[256];for(int i=0;i<t.blocks.size();i++)if((parts.finish[i]?1:0)==group&&floorBlock(t.blocks.get(i))){IslandTemplate.Block b=t.blocks.get(i);if(b.y==255||!occupied.contains((b.x*512+b.z)*256+b.y+1))counts[b.y]++;}
  int level=0;for(int i=1;i<256;i++)if(counts[i]>counts[level])level=i;
  if(group==1){int[] plates=new int[256];for(int i=0;i<t.blocks.size();i++){IslandTemplate.Block b=t.blocks.get(i);if(parts.finish[i]&&(b.key.equals("minecraft:light_weighted_pressure_plate")||b.key.equals("minecraft:heavy_weighted_pressure_plate"))&&b.y>0&&counts[b.y-1]>0)plates[b.y-1]++;}int plateLevel=0;for(int i=1;i<256;i++)if(plates[i]>plates[plateLevel])plateLevel=i;if(plates[plateLevel]>0)level=plateLevel;}
  if(group==0){double distance=Double.MAX_VALUE;for(int i=0;i<256;i++)if(counts[i]>0){List<IslandTemplate.Block> layer=new ArrayList<>();int horizontal=Integer.MAX_VALUE;for(int k=0;k<t.blocks.size();k++){IslandTemplate.Block b=t.blocks.get(k);if(!parts.finish[k]&&b.y==i&&floorBlock(b)){layer.add(b);horizontal=Math.min(horizontal,Math.abs(b.x-t.anchorX)+Math.abs(b.z-t.anchorZ));}}if(!broad(layer))continue;double d=Math.abs(i+1-t.anchorY);if(d<distance){distance=d;level=i;}}}
  int[] box={Integer.MAX_VALUE,level,Integer.MAX_VALUE,Integer.MIN_VALUE,level+1,Integer.MIN_VALUE};
  for(int i=0;i<t.blocks.size();i++)if((parts.finish[i]?1:0)==group){IslandTemplate.Block b=t.blocks.get(i);if(b.y==level&&floorBlock(b)){box[0]=Math.min(box[0],b.x);box[2]=Math.min(box[2],b.z);box[3]=Math.max(box[3],b.x+1);box[5]=Math.max(box[5],b.z+1);}}
  return box;
 }
 private static Point edge(IslandTemplate t,IslandParts parts,int group,int axis,int direction){
  int[] box=deck(t,parts,group);List<IslandTemplate.Block> floor=new ArrayList<>();List<Integer> transverse=new ArrayList<>();Set<Long> occupied=new HashSet<>();
  for(int i=0;i<t.blocks.size();i++)if((parts.finish[i]?1:0)==group&&t.blocks.get(i).y==box[1]&&floorBlock(t.blocks.get(i))){IslandTemplate.Block b=t.blocks.get(i);floor.add(b);transverse.add(axis==0?b.z:b.x);occupied.add(((long)b.x<<32)^(b.z&0xffffffffL));}
  Collections.sort(transverse);int row=axis==0?t.anchorZ:t.anchorX;
  if(parts.paired){int arrivalSurface=group==1?box[4]:deck(t,parts,1)[4];List<Integer> plates=new ArrayList<>();for(int i=0;i<t.blocks.size();i++){IslandTemplate.Block b=t.blocks.get(i);if(parts.finish[i]&&b.y==arrivalSurface&&(b.key.equals("minecraft:light_weighted_pressure_plate")||b.key.equals("minecraft:heavy_weighted_pressure_plate")))plates.add(axis==0?b.z:b.x);}if(!plates.isEmpty()){Collections.sort(plates);row=plates.get((plates.size()-1)/2);}}
  int dx=axis==0?direction:0,dz=axis==1?direction:0;Set<Integer> supportedRows=new TreeSet<>();
  for(IslandTemplate.Block b:floor){boolean wide=true;for(int across=-1;across<=1;across++)for(int depth=0;depth<2;depth++)if(!occupied.contains(((long)(b.x-dx*depth-dz*across)<<32)^((b.z-dz*depth+dx*across)&0xffffffffL)))wide=false;if(wide)supportedRows.add(axis==0?b.z:b.x);}
  Collection<Integer> rows=supportedRows.isEmpty()?transverse:supportedRows;int nearest=row,distance=Integer.MAX_VALUE;for(int candidate:rows)if(Math.abs(candidate-row)<distance){nearest=candidate;distance=Math.abs(candidate-row);}row=nearest;
  IslandTemplate.Block best=null,fallback=null;
  for(IslandTemplate.Block b:floor)if((axis==0?b.z:b.x)==row){if(fallback==null||b.x*dx+b.z*dz>fallback.x*dx+fallback.z*dz)fallback=b;boolean wide=true;for(int a=-1;a<=1;a++)for(int depth=0;depth<2;depth++){int x=b.x-dx*depth-dz*a,z=b.z-dz*depth+dx*a;if(!occupied.contains(((long)x<<32)^(z&0xffffffffL)))wide=false;}if(wide&&(best==null||b.x*dx+b.z*dz>best.x*dx+best.z*dz))best=b;}
  if(best==null)best=fallback;return new Point(best.x,box[4],best.z);
 }
 public static Plan fit(IslandTemplate t,Point start,Point finish){
  IslandParts parts=new IslandParts(t);int dx=finish.x-start.x,dz=finish.z-start.z;
  if(dx==0&&dz==0)throw new IllegalArgumentException("Distinct bridge platforms are required");
  int wx=Math.abs(dx)>=Math.abs(dz)?Integer.signum(dx):0,wz=wx==0?Integer.signum(dz):0;
  int axis=parts.paired?parts.axis:1,dir=parts.paired?parts.direction:1,turn=0;
  for(;turn<4;turn++){int[] vector=IslandRotation.point(axis==0?dir:0,axis==1?dir:0,0,0,turn);if(vector[0]==wx&&vector[1]==wz)break;}
  Point a=edge(t,parts,0,axis,dir);int[] ra=IslandRotation.point(a.x,a.z,t.anchorX,t.anchorZ,turn);
  int x=start.x-ra[0],y=start.y-a.y,z=start.z-ra[1];
  if(!parts.paired)return new Plan(x,y,z,turn,0,0,0);
  Point b=edge(t,parts,1,axis,-dir);int[] rb=IslandRotation.point(b.x,b.z,t.anchorX,t.anchorZ,turn);
  int[] offset=IslandRotation.point(finish.x-x-rb[0],finish.z-z-rb[1],0,0,-turn);
  return new Plan(x,y,z,turn,offset[0],finish.y-y-b.y,offset[1]);
 }
}
