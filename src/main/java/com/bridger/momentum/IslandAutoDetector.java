package com.bridger.momentum;

/** Bounded cardinal lane search. Finish plates outrank neighbouring start platforms. */
public final class IslandAutoDetector {
 public interface Grid {int block(int x,int y,int z);}
 public static final class Route {
  public final IslandAutoPlacement.Point start,finish;public final boolean plate;
  Route(IslandAutoPlacement.Point start,IslandAutoPlacement.Point finish,boolean plate){this.start=start;this.finish=finish;this.plate=plate;}
 }
 private static boolean floor(Grid g,int x,int y,int z){return y>=0&&y<255&&BridgeGeometry.surface(g.block(x,y,z))&&!BridgeGeometry.surface(g.block(x,y+1,z));}
 public static Route find(Grid g,int minX,int maxX,int minZ,int maxZ,int surface,int preferredX,int preferredZ){
  return find(g,minX,maxX,minZ,maxZ,surface,preferredX,preferredZ,Math.floorDiv(minX+maxX,2),Math.floorDiv(minZ+maxZ,2));
 }
 public static Route find(Grid g,int minX,int maxX,int minZ,int maxZ,int surface,int preferredX,int preferredZ,int centerX,int centerZ){
  Route best=null;double bestScore=-Double.MAX_VALUE,second=-Double.MAX_VALUE;
  for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
   int dx=d[0],dz=d[1],sx=dx>0?maxX:dx<0?minX:centerX,sz=dz>0?maxZ:dz<0?minZ:centerZ;
   if(!floor(g,sx,surface-1,sz))continue;
   Route candidate=null;int distance=0;double candidateScore=-Double.MAX_VALUE;
   for(int n=5;n<=160;n++)for(int y=Math.min(254,surface+15);y>=Math.max(0,surface-9);y--){
    int ex=sx+dx*n,ez=sz+dz*n;boolean deck=true;
    for(int across=-1;across<=1&&deck;across++)for(int depth=0;depth<2;depth++)if(!floor(g,ex-dz*across+dx*depth,y,ez+dx*across+dz*depth)){deck=false;break;}
    if(!deck)continue;boolean plate=false;
    for(int depth=0;depth<=12&&!plate;depth++){if(!floor(g,ex+dx*depth,y,ez+dz*depth))break;for(int across=-3;across<=3;across++){int id=g.block(ex+dx*depth-dz*across,y+1,ez+dz*depth+dx*across);if(id==147||id==148){plate=true;break;}}}
    double score=(plate?10000:0)-n-Math.abs(y+1-surface)*.01;
    if(score>candidateScore){candidateScore=score;candidate=new Route(new IslandAutoPlacement.Point(sx,surface,sz),new IslandAutoPlacement.Point(ex,y+1,ez),plate);distance=n;}
   }
   if(candidate!=null){double score=(candidate.plate?10000:0)+(dx==preferredX&&dz==preferredZ?1000:0)-distance*.01;if(score>bestScore){second=bestScore;bestScore=score;best=candidate;}else second=Math.max(second,score);}
  }
  return best!=null&&bestScore-second>1?best:null;
 }
}
