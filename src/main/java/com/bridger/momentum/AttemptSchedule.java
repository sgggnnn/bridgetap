package com.bridger.momentum;

/** Completed-run events only. Deferred work waits until the next safe idle state. */
public final class AttemptSchedule {
 private String last="";private int count;private boolean blocks,island,reapply;
 public void completed(String id,boolean randomBlocks,boolean randomIsland,boolean auto){
  if(id==null||id.equals(last))return;last=id;
  blocks|=randomBlocks;island|=randomIsland;
  if(auto&&++count>=1){count=0;reapply=true;}
 }
 public int count(){return count;}
 public boolean pending(){return blocks||island||reapply;}
 public boolean blocksPending(){return blocks;}
 public boolean islandPending(){return island;}
 public boolean reapplyPending(){return reapply;}
 public void consumeBlocks(){blocks=false;}
 public void consumeIsland(){island=false;}
 public void consumeReapply(){reapply=false;}
 public void resetAuto(){count=0;reapply=false;}
 public void reset(){last="";count=0;blocks=island=reapply=false;}
}
