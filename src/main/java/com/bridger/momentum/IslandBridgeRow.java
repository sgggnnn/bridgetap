package com.bridger.momentum;
import java.util.*;

/** Share one measured bridge row across both native platforms. */
public final class IslandBridgeRow {
 private IslandBridgeRow(){}
 public static int resolve(IslandPlatformDeck start,IslandPlatformDeck finish,Collection<ReplayData.Block> finishCells,IslandAutoDetector.Grid grid,int dx,int dz,Integer recordedRow){
  List<Integer> plates=new ArrayList<>();for(ReplayData.Block b:finishCells){int id=grid.block(b.x,b.y,b.z);if(b.y==finish.surface&&(id==147||id==148))plates.add(dx!=0?b.z:b.x);}
  Collections.sort(plates);int target=!plates.isEmpty()?plates.get((plates.size()-1)/2):recordedRow!=null?recordedRow:dx!=0?start.centerZ:start.centerX;
  // Validate both broad edges, so a side ornament cannot select another lane.
  for(int distance=0;distance<=3;distance++)for(int sign:new int[]{-1,1}){
   if(distance==0&&sign==1)continue;int row=target+distance*sign;
   try{start.edge(dx,dz,row);finish.edge(-dx,-dz,row);return row;}catch(IllegalArgumentException outside){}
  }
  throw new IllegalArgumentException("Bridge row is ambiguous. Record a bridge or use Preview / Adjust");
 }
}
