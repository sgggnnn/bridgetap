import com.bridger.momentum.RunStartConfirmation;
public class RunStartConfirmationRegression {
 static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  RunStartConfirmation gate=new RunStartConfirmation();Object world=new Object();
  gate.begin(world,123,24,10);check(gate.pending());
  check(gate.update(new Object(),123,24)==0&&gate.pending());
  check(gate.update(world,124,24)==0&&gate.pending());
  check(gate.update(world,123,0)==-1&&!gate.pending());
  gate.begin(world,123,24,10);check(!gate.expired(world,109));check(gate.expired(world,110));
  check(gate.update(world,123,24)==1&&!gate.pending());
  check(gate.update(world,123,0)==0);
  gate.begin(world,123,24,10);check(gate.expired(new Object(),11));gate.clear();check(!gate.pending());
  gate.begin(world,123,24,10);check(gate.update(world,123,1)==-1);
  System.out.println("PASS authoritative run starts, rejection, unrelated updates and timeout");
 }
}
