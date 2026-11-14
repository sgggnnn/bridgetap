package com.bridger.momentum;
/** Inspect original finish platforms, even when a failed run never reached them. */
public final class ServerModeProbe {
 public interface Grid{int block(int x,int y,int z);}
 public static ServerMode.Measurement measure(String server,Grid grid,int x,int y,int z,int dx,int dz){
  if(!ServerMode.mcplay(server)||Math.abs(dx)+Math.abs(dz)!=1)return ServerMode.UNAVAILABLE;
  ServerMode.Measurement found=null;
  for(int gap:new int[]{16,28,51,83}){boolean platform=false;int top=0,actualGap=gap;
   for(int offset=-2;offset<=2&&!platform;offset++){int ex=x+dx*(gap+offset+1),ez=z+dz*(gap+offset+1);
   for(int height=Math.min(254,y+12);height>=Math.max(0,y-8)&&!platform;height--){
    if(!surface(grid,ex,height,ez))continue;
    for(int shift=-1;shift<=1&&!platform;shift++){boolean wide=true,previous=true;for(int side=-1;side<=1;side++){for(int depth=0;depth<2;depth++)if(!surface(grid,ex+dx*depth-dz*(side+shift),height,ez+dz*depth+dx*(side+shift)))wide=false;if(!surface(grid,ex-dx-dz*(side+shift),height,ez-dz+dx*(side+shift)))previous=false;}if(wide&&!previous){platform=true;top=height;actualGap=gap+offset;}}
   }
   }if(platform){if(found!=null)return ServerMode.UNAVAILABLE;found=new ServerMode.Measurement(true,actualGap,top-y);}
  }return found==null?ServerMode.UNAVAILABLE:found;
 }
 private static boolean surface(Grid grid,int x,int y,int z){return BridgeGeometry.surface(grid.block(x,y,z))&&!BridgeGeometry.surface(grid.block(x,y+1,z));}
}
