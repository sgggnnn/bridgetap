import com.bridger.momentum.*;
public class IslandModeChangeRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){IslandModeChange m=new IslandModeChange(28,3,65);check(m.observe(28,3,65)==null,"Respawn drifted");check(m.observe(51,4,65)==null,"Single transient sample accepted");m.unavailable();check(m.observe(51,4,65)==null,"Unloaded interval retained certainty");int[] d=m.observe(51,4,65);check(d[0]==23&&d[1]==1&&d[2]==0,"Wrong mode geometry delta");for(int i=0;i<10;i++)check(m.observe(51,4,65)==null,"Mode change accumulated repeatedly");check(m.observe(16,0,66)==null,"Immediate transition accepted");d=m.observe(16,0,66);check(d[0]==-35&&d[1]==-4&&d[2]==1,"Extra Short / floor delta wrong");System.out.println("PASS stable mode changes, transient/unloaded samples, height deltas and no repeated accumulation");}
}
