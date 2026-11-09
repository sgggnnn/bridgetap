import com.bridger.momentum.RunHistory;
import java.nio.file.*;
import java.util.*;
public class HistoryRegression {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception {
  Path folder=Files.createTempDirectory("bridgetap-history-");
  RunHistory history=new RunHistory(folder);
  List<String> details=new ArrayList<>(Arrays.asList("Jump .500"));
  history.add("SUCCESS",3.5,2,"Z",details);
  details.set(0,"changed");
  check(history.recent().get(0).details.get(0).equals("Jump .500"),"history must copy mutable entries");
  for(int i=0;i<11;i++)history.add("FAILED",i,1,"X",Collections.singletonList("Tap"));
  check(history.recent().size()==10,"recent runs limited to ten");
  history.save(history.recent().get(0));
  RunHistory reload=new RunHistory(folder);
  check(reload.saved().size()==1,"saved run survives reload");
  check(reload.saved().get(0).status.equals("FAILED") && reload.saved().get(0).time==10,"saved data retained");
  history.save(history.recent().get(0));
  check(new RunHistory(folder).saved().size()==1,"saving twice does not duplicate");
  System.out.println("PASS: recent snapshots, limit and persistent save");
 }
}
