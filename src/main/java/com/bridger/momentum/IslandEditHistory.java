package com.bridger.momentum;

import java.util.*;

/** Bounded positioning history; one drag gesture is one undo step. */
public final class IslandEditHistory {
    private final List<IslandLayout> states=new ArrayList<>();private int index;private boolean gesture;
    public IslandEditHistory(IslandLayout initial){states.add(initial);}
    public void begin(){gesture=true;}
    public void end(IslandLayout current){gesture=false;record(current);}
    public void record(IslandLayout current){if(gesture||same(states.get(index),current))return;while(states.size()>index+1)states.remove(states.size()-1);states.add(current);if(states.size()>101)states.remove(0);index=states.size()-1;}
    public boolean canUndo(){return index>0;}public boolean canRedo(){return index+1<states.size();}
    public IslandLayout undo(){gesture=false;return states.get(canUndo()?--index:index);}public IslandLayout redo(){gesture=false;return states.get(canRedo()?++index:index);}
    private static boolean same(IslandLayout a,IslandLayout b){return a.x==b.x&&a.y==b.y&&a.z==b.z&&a.startX==b.startX&&a.startY==b.startY&&a.startZ==b.startZ&&a.finishX==b.finishX&&a.finishY==b.finishY&&a.finishZ==b.finishZ&&a.rotation==b.rotation&&a.focus==b.focus;}
}
