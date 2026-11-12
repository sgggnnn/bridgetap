import com.bridger.momentum.*;

public final class IslandEditorRegression {
    private static IslandLayout state(int x){return new IslandLayout(x,0,0,0,0,0,x,0,0,Math.floorMod(x,4),false);}
    private static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args){IslandEditHistory edits=new IslandEditHistory(state(0));edits.begin();for(int i=1;i<10;i++)edits.record(state(i));edits.end(state(10));check(edits.undo().x==0,"One drag required multiple undo operations");check(edits.redo().x==10,"Redo lost drag position");edits.undo();edits.record(state(11));check(!edits.canRedo(),"New edit retained obsolete redo branch");for(int i=12;i<250;i++)edits.record(state(i));int count=0;while(edits.canUndo()){edits.undo();count++;}check(count==100,"Positioning history is not bounded");System.out.println("PASS: grouped drag undo, redo, branch replacement and bounded island edit history");}
}
