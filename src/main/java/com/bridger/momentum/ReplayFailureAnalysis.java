package com.bridger.momentum;

import java.util.*;

/** Facts from recorded samples, never an asserted explanation of why a run failed. */
public final class ReplayFailureAnalysis {
    public static final class Finding{public final int tick;public final String text;Finding(int tick,String text){this.tick=tick;this.text=text;}}
    public static List<Finding> analyze(String status,String axis,ReplayData data){
        List<Finding> result=new ArrayList<>();if(!"FAILED".equals(status)){result.add(new Finding(0,"This run is not marked as failed."));return result;}
        if(data==null||data.frames.isEmpty()){result.add(new Finding(0,"No recorded samples: statistics alone cannot explain a fail."));return result;}
        int last=data.frames.size()-1,from=Math.max(0,last-10),placement=-1,click=-1;boolean telemetry=false;
        for(ReplayData.Block block:data.blocks)if(block.placed&&block.id>0&&block.tick<=last)placement=Math.max(placement,ReplayTimeline.eventTick(data,block));
        for(int i=0;i<=last;i++){ReplayData.TickStats stats=data.frames.get(i).stats;if(stats!=null){telemetry=true;if(stats.rightClicks>0)click=ReplayTimeline.eventFrameTick(data,i);}}
        if(placement>=0)result.add(new Finding(placement,"Last confirmed block placement."));else result.add(new Finding(last,"No confirmed placed block was recorded."));
        if(!telemetry)result.add(new Finding(last,"This recording has no click/input telemetry."));
        if(click>=0)result.add(new Finding(click,"Last recorded right-click."));
        for(int i=from;i<=last-(data.formatVersion>=5?4:3);i++){ReplayData.TickStats stats=data.frames.get(i).stats;if(stats==null||stats.rightClicks==0)continue;boolean placed=false;for(ReplayData.Block b:data.blocks)if(b.placed&&b.id>0&&b.tick>=i-1&&b.tick<=i+3){placed=true;break;}if(!placed)result.add(new Finding(ReplayTimeline.eventFrameTick(data,i),"Right-click observed; no placement recorded within the following 3 ticks."));}
        double drop=data.frames.get(from).y-data.frames.get(last).y;if(drop>.5)result.add(new Finding(last,String.format(Locale.US,"Descended %.2fb over the last %d ticks.",drop,last-from)));
        if(axis.equals("X")||axis.equals("Z")){ReplayData.Frame a=data.frames.get(from),b=data.frames.get(last);double side=axis.equals("X")?Math.abs(b.z-a.z):Math.abs(b.x-a.x);if(side>.2)result.add(new Finding(last,String.format(Locale.US,"Moved %.3fb sideways over the last %d ticks.",side,last-from)));}
        if(data.truncated)result.add(new Finding(last,"Recording was truncated; the actual ending may be missing."));
        result.sort(Comparator.comparingInt(f->f.tick));return Collections.unmodifiableList(result);
    }
}
