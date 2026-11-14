import com.bridger.momentum.AttemptSchedule;
public class AttemptScheduleRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  AttemptSchedule s=new AttemptSchedule();check(!s.pending(),"Work before any completed run");
  s.completed("success-1",true,true,true);s.completed("success-1",true,true,true);
  check(s.count()==0&&s.blocksPending()&&s.islandPending()&&s.reapplyPending(),"Completion deduplication or per-run random changes");
  s.consumeBlocks();s.consumeIsland();s.consumeReapply();s.completed("fail-2",false,false,true);check(s.count()==0&&s.reapplyPending(),"Second completed attempt did not schedule apply");
  s.completed("fail-3",false,false,true);check(s.count()==0&&s.reapplyPending(),"Completed attempt did not schedule apply");s.consumeReapply();
  for(int i=4;i<=6;i++)s.completed("run-"+i,false,false,true);check(s.reapplyPending(),"Repeated cycle lost");
  s.resetAuto();check(!s.pending()&&s.count()==0,"Disabling did not clear scheduled auto action");
  s.completed("disabled",false,false,false);check(!s.pending()&&s.count()==0,"Disabled features scheduled work");
  s.reset();s.completed("success-1",false,true,false);check(s.islandPending()&&!s.reapplyPending(),"New server/reset or independent random toggle failed");
  System.out.println("PASS: finished-run scheduling, success/fail equivalence, deduplication, per-run scheduling and independent random toggles");
 }
}
