package com.bridger.momentum;

import java.util.*;
import java.util.function.Predicate;

/** Selects one connected island from recorded terrain, excluding the built bridge. */
public final class ReplayIslandSelection {
    public static List<ReplayData.Block> both(ReplayData data,Predicate<ReplayData.Block> supported){
        List<ReplayData.Block> start=select(data,false,supported);Set<Long> used=new HashSet<>(),placed=new HashSet<>();for(ReplayData.Block b:start)used.add(b.key());for(ReplayData.Block b:data.blocks)if(b.placed)placed.add(b.key());
        List<ReplayData.Block> end=null;try{List<ReplayData.Block> candidate=select(data,true,supported);if(!candidate.isEmpty()&&!used.contains(candidate.get(0).key()))end=candidate;}catch(IllegalArgumentException ignored){}
        if(end==null){
            Map<Long,ReplayData.Block> remaining=new HashMap<>();for(ReplayData.Block b:data.blocks)if(!b.placed&&b.tick==0&&b.id>0&&!placed.contains(b.key())&&!used.contains(b.key())&&supported.test(b))remaining.put(b.key(),b);
            ReplayData.Frame first=data.frames.get(0),last=data.frames.get(data.frames.size()-1);double dx=last.x-first.x,dz=last.z-first.z;ReplayData.Block far=null;for(ReplayData.Block b:data.blocks)if(b.placed&&(far==null||b.tick>=far.tick))far=b;
            if(far!=null&&Math.hypot(dx,dz)<4){dx=far.x+.5-first.x;dz=far.z+.5-first.z;}double length=Math.hypot(dx,dz);if(length<.1)throw new IllegalArgumentException("Bridge direction was not recorded; use a completed replay");dx/=length;dz/=length;
            double best=Double.MAX_VALUE;int[][] steps={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
            while(!remaining.isEmpty()){
                ReplayData.Block seed=remaining.values().iterator().next();List<ReplayData.Block> part=new ArrayList<>();Deque<ReplayData.Block> todo=new ArrayDeque<>();todo.add(seed);remaining.remove(seed.key());double score=Double.MAX_VALUE;
                while(!todo.isEmpty()){ReplayData.Block b=todo.removeFirst();part.add(b);double ax=b.x+.5-first.x,az=b.z+.5-first.z,along=ax*dx+az*dz,across=Math.abs(ax*dz-az*dx);if(along>3&&across<8)score=Math.min(score,Math.pow(along-length-3,2)+across*across*4);
                    for(int[] step:steps){int y=b.y+step[1];if(y<0||y>255)continue;long key=((long)(b.x+step[0])&0x3FFFFFF)<<38|((long)(b.z+step[2])&0x3FFFFFF)<<12|y;ReplayData.Block next=remaining.remove(key);if(next!=null)todo.add(next);}
                }
                if(part.size()>=3&&score<best){best=score;end=part;}
            }
        }
        if(end==null)throw new IllegalArgumentException("Finish island is missing from this recording. Use a completed replay or copy in the world.");
        List<ReplayData.Block> result=new ArrayList<>(start);result.addAll(end);return result;
    }
    public static List<ReplayData.Block> select(ReplayData replay,boolean finish,Predicate<ReplayData.Block> supported){
        if(replay==null)throw new IllegalArgumentException("Select a playable replay first");
        if(!replay.placementTags)throw new IllegalArgumentException("This older replay cannot separate terrain from placed blocks");
        Set<Long> placed=new HashSet<>();for(ReplayData.Block b:replay.blocks)if(b.placed)placed.add(b.key());
        Map<Long,ReplayData.Block> terrain=new HashMap<>();
        for(ReplayData.Block b:replay.blocks)if(!b.placed&&b.tick==0&&b.id!=0&&!placed.contains(b.key())&&supported.test(b))terrain.put(b.key(),b);
        ReplayData.Frame frame=replay.frames.get(finish?replay.frames.size()-1:0);
        ReplayData.Block seed=nearest(terrain.values(),frame.x,frame.y-1,frame.z);
        if(seed==null&&finish){
            ReplayData.Block last=null;for(ReplayData.Block b:replay.blocks)if(b.placed&&(last==null||b.tick>last.tick))last=b;
            if(last!=null)seed=nearest(terrain.values(),last.x+.5,last.y,last.z+.5);
        }
        if(seed==null)throw new IllegalArgumentException((finish?"Finish":"Start")+" island was not captured in this replay");
        List<ReplayData.Block> result=new ArrayList<>();Deque<ReplayData.Block> todo=new ArrayDeque<>();Set<Long> visited=new HashSet<>();todo.add(seed);visited.add(seed.key());
        int[][] offsets={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        while(!todo.isEmpty()){
            ReplayData.Block b=todo.removeFirst();result.add(b);if(result.size()>IslandTemplate.MAX_BLOCKS)throw new IllegalArgumentException("Recorded island exceeds 32768 blocks");
            for(int[] d:offsets){int y=b.y+d[1];if(y<0||y>255)continue;long key=((long)(b.x+d[0])&0x3FFFFFF)<<38|((long)(b.z+d[2])&0x3FFFFFF)<<12|(long)y;ReplayData.Block neighbor=terrain.get(key);if(neighbor!=null&&visited.add(key))todo.addLast(neighbor);}
        }
        result.sort(Comparator.comparingInt((ReplayData.Block b)->b.x).thenComparingInt(b->b.y).thenComparingInt(b->b.z));
        return result;
    }
    private static ReplayData.Block nearest(Collection<ReplayData.Block> blocks,double x,double y,double z){
        ReplayData.Block result=null;double score=Double.MAX_VALUE;
        for(ReplayData.Block b:blocks){double dx=b.x+.5-x,dy=b.y-y,dz=b.z+.5-z;
            if(Math.abs(dx)>4||Math.abs(dz)>4||Math.abs(dy)>6)continue;
            double distance=dx*dx+dz*dz+dy*dy*2;if(distance<score||(distance==score&&(result==null||b.key()<result.key()))){score=distance;result=b;}
        }return result;
    }
}
