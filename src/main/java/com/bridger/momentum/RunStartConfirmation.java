package com.bridger.momentum;

/** Only authoritative updates can promote the first predicted block to a run. */
public final class RunStartConfirmation {
 private Object world;private long position;private int block,tick;private boolean pending;
 public void begin(Object world,long position,int block,int tick){this.world=world;this.position=position;this.block=block;this.tick=tick;pending=true;}
 public boolean pending(){return pending;}
 /** 0: unrelated update, 1: confirmed, -1: rejected. */
 public int update(Object world,long position,int block){if(!pending||this.world!=world||this.position!=position)return 0;pending=false;this.world=null;return this.block==block&&block!=0?1:-1;}
 public boolean expired(Object world,int tick){return pending&&(this.world!=world||(long)tick-this.tick>=100);}
 public void clear(){pending=false;world=null;}
}
