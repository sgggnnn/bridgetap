package com.bridger.momentum;
import java.util.*;

/** Measures the gap between platform block faces, excluding every player placement. */
public final class BridgeGeometry {
 public static final class Result {
  public final boolean available;
  public final double gap,rise,startHeight,endHeight;
  public final String reason;
  private Result(double gap,double start,double end,String reason){this.available=reason.isEmpty();this.gap=gap;this.startHeight=start;this.endHeight=end;this.rise=end-start;this.reason=reason;}
  public static Result unavailable(String reason){return new Result(0,0,0,reason);}
 }
 private static long key(int x,int y,int z){return ((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|(long)y;}
 static boolean surface(int id){
  switch(id){
   case 0:case 6:case 8:case 9:case 10:case 11:case 26:case 27:case 28:case 30:case 31:case 32:
   case 37:case 38:case 39:case 40:case 50:case 51:case 53:case 54:case 55:case 59:case 63:case 64:case 65:case 66:case 67:case 68:case 69:case 70:case 71:case 72:case 75:case 76:case 77:case 78:case 81:case 83:case 85:case 90:case 92:case 93:case 94:case 96:case 101:case 102:case 104:case 105:case 106:case 107:case 108:case 109:case 111:case 113:case 114:case 115:case 116:case 117:case 118:case 119:case 120:case 127:case 128:case 130:case 131:case 132:case 134:case 135:case 136:case 139:case 140:case 141:case 142:case 143:case 144:case 145:case 146:case 147:case 148:case 149:case 150:case 151:case 154:case 156:case 157:case 160:case 163:case 164:case 167:case 171:case 175:case 176:case 177:case 178:case 180:case 183:case 184:case 185:case 186:case 187:case 188:case 189:case 190:case 191:case 192:case 193:case 194:case 195:case 196:case 197:return false;
   default:return id>0&&id<=197;
  }
 }
 private static double top(ReplayData.Block b){return b.y+((b.id==44||b.id==126||b.id==182)&&(b.metadata&8)==0?.5:1);}
 private static ReplayData.Block endpoint(ReplayData.Block anchor,Map<Long,ReplayData.Block> terrain,double dx,double dz,int sign){
  ReplayData.Block best=null;double bestScore=Double.MAX_VALUE;
  int[][] neighbors={{1,0},{-1,0},{0,1},{0,-1}};
  for(int[] offset:neighbors){
   double projection=sign*(offset[0]*dx+offset[1]*dz);if(projection<.25)continue;
   for(int y=Math.max(0,anchor.y-1);y<=Math.min(255,anchor.y+1);y++){
    ReplayData.Block b=terrain.get(key(anchor.x+offset[0],y,anchor.z+offset[1]));if(b==null||!surface(b.id))continue;
    ReplayData.Block above=terrain.get(key(b.x,b.y+1,b.z));if(above!=null&&surface(above.id))continue;
    double score=Math.abs(top(b)-(anchor.y+1))*10+(1-projection);
    if(score<bestScore){bestScore=score;best=b;}
   }
  }
  return best;
 }
 public static Result measure(ReplayData data){
  if(!data.placementTags)return Result.unavailable("New recording needed for platform edges");
  Set<Long> placedKeys=new HashSet<>();ReplayData.Block first=null,last=null;
  for(ReplayData.Block b:data.blocks)if(b.placed&&b.id!=0){placedKeys.add(b.key());if(first==null||b.tick<first.tick)first=b;if(last==null||b.tick>=last.tick)last=b;}
  if(first==null)return Result.unavailable("No recorded bridge placements");
  double dx=last.x-first.x,dz=last.z-first.z;
  if(dx==0&&dz==0){ReplayData.Frame a=data.frames.get(0),b=data.frames.get(data.frames.size()-1);dx=b.x-a.x;dz=b.z-a.z;}
  double length=Math.hypot(dx,dz);if(length<.1)return Result.unavailable("Bridge direction unavailable");dx/=length;dz/=length;
  Map<Long,ReplayData.Block> terrain=new HashMap<>();
  for(ReplayData.Block b:data.blocks)if(!b.placed&&b.tick==0&&!placedKeys.contains(b.key())&&b.id!=0)terrain.put(b.key(),b);
  ReplayData.Block start=endpoint(first,terrain,dx,dz,-1),end=endpoint(last,terrain,dx,dz,1);
  if(start==null||end==null)return Result.unavailable("Both platform edges were not captured");
  if((end.x-start.x)*dx+(end.z-start.z)*dz<1)return Result.unavailable("Distinct platforms unavailable");
  double gap=Math.hypot(Math.max(0,Math.abs(end.x-start.x)-1),Math.max(0,Math.abs(end.z-start.z)-1));
  return new Result(gap,top(start),top(end),"");
 }
}
