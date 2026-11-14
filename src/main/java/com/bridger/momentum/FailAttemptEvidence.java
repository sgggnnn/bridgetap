package com.bridger.momentum;
import java.util.*;

/** View poses captured at actual game use actions, cleared by each later placement. */
public final class FailAttemptEvidence {
 private ReplayData.Block support;private final List<Click> clicks=new ArrayList<>();
 private static final class Click{final ReplayData.Frame pose;final boolean block;boolean targeted,occupied,collision;int faceY;Boolean accepted;Click(ReplayData.Frame pose,boolean block){this.pose=pose;this.block=block;}}
 public void clear(){support=null;clicks.clear();}
 public void placed(int x,int y,int z){support=new ReplayData.Block(x,y,z,1,0,0,true);clicks.clear();}
 public void use(ReplayData.Frame pose,boolean holdingBlock){if(support!=null&&clicks.size()<32)clicks.add(new Click(pose,holdingBlock));}
 public void target(int x,int y,int z,int faceY,boolean occupied){if(clicks.isEmpty())return;Click click=clicks.get(clicks.size()-1);click.targeted=true;click.faceY=faceY;click.occupied=occupied;ReplayData.Frame p=click.pose;click.collision=p.x+.3>x&&p.x-.3<x+1&&p.z+.3>z&&p.z-.3<z+1&&p.y+1.8>y&&p.y<y+1;}
 public void result(boolean accepted){if(!clicks.isEmpty())clicks.get(clicks.size()-1).accepted=accepted;}
 public String summary(){if(support==null)return "Not enough placement data.";for(Click click:clicks){if(click.pose.y<support.y-1.5)break;if(!click.block)return "Not holding a block.";if(click.targeted){if(click.occupied)return "Placement target occupied.";if(click.collision)return "Likely player collision.";if(click.faceY>0)return "Top face hit.";if(click.faceY<0)return "Bottom face hit.";if(Boolean.FALSE.equals(click.accepted))return "No client placement.";if(Boolean.TRUE.equals(click.accepted))return "Click used; no block placed.";}return ReplayAimAnalysis.aim(click.pose,support);}return clicks.isEmpty()?"No placement input after last block.":"Click too late; cause uncertain.";}
}
