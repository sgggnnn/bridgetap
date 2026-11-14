import com.bridger.momentum.*;
public final class ReplayClickRegression {
 static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  PhysicalPresses presses=new PhysicalPresses();int code=-99;
  check(presses.event(code,true));presses.enqueue(code);check(!presses.event(code,true));
  check(presses.consume(code));check(!presses.consume(code));
  presses.event(code,false);check(presses.event(code,true));presses.enqueue(code);presses.event(code,false);check(presses.event(code,true));presses.enqueue(code);
  check(presses.consume(code)&&presses.consume(code)&&!presses.consume(code));
  check(ReplayControls.acceptAction(ReplayControls.FORWARD,true,1,1));check(!ReplayControls.acceptAction(ReplayControls.FORWARD,false,1,1));
  presses.clear();check(!presses.consume(code));
  System.out.println("PASS one step per physical click, duplicate/held callbacks rejected, rapid queued clicks retained without cooldown");
 }
}
