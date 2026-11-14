package com.bridger.momentum;
import java.util.*;

/** The broad floor plane beneath feet; decorations must not move its centre or height. */
public final class IslandPlatformDeck {
 public final int surface,centerX,centerZ,minX,maxX,minZ,maxZ;
 private final List<ReplayData.Block> floor;
 private IslandPlatformDeck(List<ReplayData.Block> floor,int surface,int cx,int cz){
  this.floor=floor;this.surface=surface;int best=Integer.MAX_VALUE;ReplayData.Block nearest=null;
  for(ReplayData.Block b:floor){int d=Math.abs(b.x-cx)+Math.abs(b.z-cz);if(d<best||d==best&&(nearest==null||b.x<nearest.x||b.x==nearest.x&&b.z<nearest.z)){best=d;nearest=b;}}
  centerX=nearest.x;centerZ=nearest.z;int ax=Integer.MAX_VALUE,bx=Integer.MIN_VALUE,az=Integer.MAX_VALUE,bz=Integer.MIN_VALUE;
  for(ReplayData.Block b:floor){if(b.z==centerZ){ax=Math.min(ax,b.x);bx=Math.max(bx,b.x);}if(b.x==centerX){az=Math.min(az,b.z);bz=Math.max(bz,b.z);}}
  minX=ax;maxX=bx;minZ=az;maxZ=bz;
 }
 public IslandPlatformDeck recenter(int x,int z){return new IslandPlatformDeck(floor,surface,x,z);}

 public IslandAutoPlacement.Point edge(int dx,int dz,int row){Set<Long> occupied=new HashSet<>();for(ReplayData.Block b:floor)occupied.add(((long)b.x<<32)^(b.z&0xffffffffL));ReplayData.Block best=null;for(ReplayData.Block b:floor)if((dx!=0?b.z:b.x)==row){boolean wide=true;for(int a=-1;a<=1;a++)for(int depth=0;depth<2;depth++){int x=b.x-dx*depth-dz*a,z=b.z-dz*depth+dx*a;if(!occupied.contains(((long)x<<32)^(z&0xffffffffL)))wide=false;}if(wide&&(best==null||b.x*dx+b.z*dz>best.x*dx+best.z*dz))best=b;}if(best==null)throw new IllegalArgumentException("Bridge row is outside the platform deck");return new IslandAutoPlacement.Point(best.x,surface,best.z);}
 private static boolean broad(List<ReplayData.Block> cells){Set<Long> occupied=new HashSet<>();for(ReplayData.Block b:cells)occupied.add(((long)b.x<<32)^(b.z&0xffffffffL));for(ReplayData.Block b:cells)for(int turn=0;turn<2;turn++){boolean complete=true;for(int x=0;x<3&&complete;x++)for(int z=0;z<2;z++)if(!occupied.contains(((long)(b.x+(turn==0?x:z))<<32)^((b.z+(turn==0?z:x))&0xffffffffL))){complete=false;break;}if(complete)return true;}return false;}
 public static IslandPlatformDeck find(IslandAutoDetector.Grid grid,Collection<ReplayData.Block> cells,double feet,int referenceX,int referenceZ){
  Map<Integer,List<ReplayData.Block>> layers=new TreeMap<>();Map<Integer,Integer> exposed=new HashMap<>();Map<Integer,Integer> plates=new TreeMap<>();
  for(ReplayData.Block b:cells){int id=grid.block(b.x,b.y,b.z);if(id==147||id==148)plates.merge(b.y,1,Integer::sum);
   if(!BridgeGeometry.surface(id))continue;layers.computeIfAbsent(b.y,k->new ArrayList<>()).add(b);if(!BridgeGeometry.surface(grid.block(b.x,b.y+1,b.z)))exposed.merge(b.y,1,Integer::sum);
  }
  int level=Integer.MIN_VALUE;double score=Double.MAX_VALUE;
  for(Map.Entry<Integer,List<ReplayData.Block>> e:layers.entrySet())if(exposed.getOrDefault(e.getKey(),0)>0&&broad(e.getValue())){int horizontal=Integer.MAX_VALUE;for(ReplayData.Block b:e.getValue())horizontal=Math.min(horizontal,Math.abs(b.x-referenceX)+Math.abs(b.z-referenceZ));double d=Math.abs(e.getKey()+1-feet)+horizontal*4;if(d<score){score=d;level=e.getKey();}}
  // Prefer the directly supported standing floor over decorative levels nearby.
  for(Map.Entry<Integer,List<ReplayData.Block>> e:layers.entrySet())if(Math.abs(e.getKey()+1-feet)<.12&&BridgeGeometry.surface(grid.block(referenceX,e.getKey(),referenceZ))&&!BridgeGeometry.surface(grid.block(referenceX,e.getKey()+1,referenceZ))&&broad(e.getValue())){level=e.getKey();break;}
  int plateCount=0;for(Map.Entry<Integer,Integer> plate:plates.entrySet())if(layers.containsKey(plate.getKey()-1)&&plate.getValue()>plateCount){level=plate.getKey()-1;plateCount=plate.getValue();}
  if(level==Integer.MIN_VALUE)throw new IllegalArgumentException("Platform floor not found");
  List<ReplayData.Block> floor=layers.get(level);List<Integer> xs=new ArrayList<>(),zs=new ArrayList<>();for(ReplayData.Block b:floor){xs.add(b.x);zs.add(b.z);}Collections.sort(xs);Collections.sort(zs);
  return new IslandPlatformDeck(floor,level+1,xs.get((xs.size()-1)/2),zs.get((zs.size()-1)/2));
 }
}
