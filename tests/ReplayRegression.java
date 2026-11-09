import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.util.zip.*;

public class ReplayRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  Path root=Files.createTempDirectory("bridgetap-replay-");
  List<ReplayData.Frame> frames=new ArrayList<>(Arrays.asList(new ReplayData.Frame(-.5,70,3),new ReplayData.Frame(.25,71,4)));
  List<ReplayData.Block> blocks=new ArrayList<>(Arrays.asList(new ReplayData.Block(0,69,3,24,0),new ReplayData.Block(0,70,4,24,1)));
  ReplayData data=new ReplayData(frames,blocks,true);frames.clear();blocks.clear();
  check(data.frames.size()==2&&data.blocks.size()==2,"Recording snapshots must be immutable copies");
  RunHistory history=new RunHistory(root);
  history.add("SUCCESS",.1,1,"Z",Collections.singletonList("Tap .250"),data);
  RunHistory.Run run=history.recent().get(0);history.save(run);history.save(run);
  RunHistory reload=new RunHistory(root);RunHistory.Run saved=reload.saved().get(0);
  check(reload.hasReplay(saved),"Replay must survive restart");
  ReplayData restored=reload.replay(saved);
  check(restored.frames.get(0).x==-.5&&restored.frames.get(1).y==71,"Player positions must round trip exactly");
  check(restored.blocks.get(1).tick==1&&restored.blocks.get(1).id==24&&restored.truncated,"Block timing and partial flag must survive restart");
  history.add("FAILED",1,0,"X",Collections.singletonList("Old summary"));history.save(history.recent().get(0));
  check(history.replay(history.recent().get(0))==null,"Old summaries must remain readable without replay");
  Path bad=root.resolve("bad.btr");
  try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(bad)))){
   out.writeInt(0x42545250);out.writeInt(1);out.writeBoolean(false);out.writeInt(Integer.MAX_VALUE);out.writeInt(0);
  }
  try{ReplayData.read(bad);throw new AssertionError("Corrupt replay counts accepted");}catch(IOException expected){}
  try{new ReplayData.Frame(Double.NaN,0,0);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
  System.out.println("PASS: replay persistence, exact positions, block timing, old saves and malformed input");
 }
}
