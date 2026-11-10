package com.bridger.momentum;
import java.util.Comparator;

public enum ReplaySort {
 NEWEST("Newest"),OLDEST("Oldest"),FASTEST("Fastest"),SLOWEST("Slowest"),NAME("Name A-Z");
 public final String label;ReplaySort(String label){this.label=label;}
 public ReplaySort next(){return values()[(ordinal()+1)%values().length];}
 public static ReplaySort parse(String value){try{return valueOf(value);}catch(IllegalArgumentException|NullPointerException ex){return NEWEST;}}
 public Comparator<RunHistory.Run> comparator(){
  Comparator<RunHistory.Run> c;
  switch(this){
   case OLDEST:c=Comparator.comparingLong(r->r.created);break;
   case FASTEST:c=Comparator.comparingDouble(r->r.time);break;
   case SLOWEST:c=Comparator.<RunHistory.Run>comparingDouble(r->r.time).reversed();break;
   case NAME:c=Comparator.comparing(r->r.displayName(),String.CASE_INSENSITIVE_ORDER);break;
   default:c=Comparator.<RunHistory.Run>comparingLong(r->r.created).reversed();
  }
  return c.thenComparing(r->r.id);
 }
}
