package com.bridger.momentum;
import java.util.Comparator;

public enum ReplaySort {
 NEWEST("Newest"),OLDEST("Oldest"),FASTEST("Fastest"),SLOWEST("Slowest"),NAME("Name A-Z"),NAME_DESC("Name Z-A"),FEWEST_JUMPS("Fewest jumps"),MOST_JUMPS("Most jumps");
 public enum Key { DATE, TIME, NAME, JUMPS }
 public final String label;ReplaySort(String label){this.label=label;}
 public ReplaySort next(){return values()[(ordinal()+1)%values().length];}
 public static ReplaySort parse(String value){try{return valueOf(value);}catch(IllegalArgumentException|NullPointerException ex){return NEWEST;}}
 public Key key(){switch(this){case FASTEST:case SLOWEST:return Key.TIME;case NAME:case NAME_DESC:return Key.NAME;case FEWEST_JUMPS:case MOST_JUMPS:return Key.JUMPS;default:return Key.DATE;}}
 public boolean ascending(){return this==OLDEST||this==FASTEST||this==NAME||this==FEWEST_JUMPS;}
 public static ReplaySort of(Key key,boolean ascending){switch(key){case TIME:return ascending?FASTEST:SLOWEST;case NAME:return ascending?NAME:NAME_DESC;case JUMPS:return ascending?FEWEST_JUMPS:MOST_JUMPS;default:return ascending?OLDEST:NEWEST;}}
 public Comparator<RunHistory.Run> comparator(){
  Comparator<RunHistory.Run> c;
  switch(this){
   case OLDEST:c=Comparator.comparingLong(r->r.created);break;
   case FASTEST:c=timeOrder(true);break;
   case SLOWEST:c=timeOrder(false);break;
   case NAME:c=Comparator.comparing(r->ReplayQuery.normalize(r.displayName()));break;
   case NAME_DESC:c=Comparator.<RunHistory.Run,String>comparing(r->ReplayQuery.normalize(r.displayName())).reversed();break;
   case FEWEST_JUMPS:c=Comparator.comparingInt(r->r.jumps);break;
   case MOST_JUMPS:c=Comparator.<RunHistory.Run>comparingInt(r->r.jumps).reversed();break;
   default:c=Comparator.<RunHistory.Run>comparingLong(r->r.created).reversed();
  }
  return c.thenComparing(Comparator.<RunHistory.Run>comparingLong(r->r.created).reversed()).thenComparing(r->r.id);
 }
 private static Comparator<RunHistory.Run> timeOrder(boolean ascending){Comparator<RunHistory.Run> value=Comparator.comparingDouble(r->r.time);if(!ascending)value=value.reversed();return Comparator.<RunHistory.Run>comparingInt(r->r.time>0?0:1).thenComparing(value);}
}
