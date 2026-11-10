import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;

public class ReplayQueryRegression {
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        RunHistory history=new RunHistory(Files.createTempDirectory("query-").resolve("saved"));
        history.add("SUCCESS",3.85,7,"Z",Arrays.asList("\u00a7b1.0 Flat .500","5b Late .460"));
        RunHistory.Run success=history.recent().get(0);history.rename(success,"Summer café bridge");
        history.add("FAILED",4.2,5,"X",Collections.singletonList("1.05 Flat .450"));RunHistory.Run fail=history.recent().get(0);
        ReplayQuery q=new ReplayQuery();q.search="CAFE 5b";check(q.matches(success,true),"All terms match case/accent-insensitively across name and technique");
        q.status=ReplayQuery.Status.SUCCESS;q.axis=ReplayQuery.Axis.Z;q.technique=ReplayQuery.Technique.LATE_5;q.content=ReplayQuery.Content.REPLAYS;q.minimum="3,85";q.maximum="3.85";
        check(q.error()==null&&q.matches(success,true),"Combined filters and inclusive comma/dot time bounds");
        check(!q.matches(success,false)&&!q.matches(fail,true),"Content, result and axis filters exclude other runs");
        q.minimum="4";check(q.error()!=null,"Reversed time range is rejected before apply");
        for(String value:new String[]{"NaN","Infinity","-1","abc"}){q.minimum=value;check(q.error()!=null,"Invalid time cannot be saved: "+value);}
        q.minimum="";q.maximum="";q.store();BridgeConfig.replaySort="NAME_DESC";BridgeConfig.save();
        BridgeConfig.replayQuery="";BridgeConfig.replayAxis="ALL";BridgeConfig.replayTechnique="ALL";BridgeConfig.load();
        ReplayQuery restored=ReplayQuery.load();check(restored.search.equals("CAFE 5b")&&restored.axis==ReplayQuery.Axis.Z&&restored.technique==ReplayQuery.Technique.LATE_5,"Library filters survive restart");
        check(ReplaySort.parse(BridgeConfig.replaySort)==ReplaySort.NAME_DESC,"Descending sort persists");
        List<RunHistory.Run> rows=new ArrayList<>(history.recent());rows.sort(ReplaySort.MOST_JUMPS.comparator());check(rows.get(0)==success,"Jump-count sort");
        for(ReplaySort.Key key:ReplaySort.Key.values())for(boolean ascending:new boolean[]{true,false}){
            ReplaySort order=ReplaySort.of(key,ascending);check(order.key()==key&&order.ascending()==ascending,"Every criterion has both directions");
            rows.sort(order.comparator());List<String> before=Arrays.asList(rows.get(0).id,rows.get(1).id);Collections.reverse(rows);rows.sort(order.comparator());check(before.equals(Arrays.asList(rows.get(0).id,rows.get(1).id)),"Sort order is deterministic");
        }
        history.add("FAILED",0,0,"Z",Collections.emptyList());
        for(ReplaySort order:new ReplaySort[]{ReplaySort.FASTEST,ReplaySort.SLOWEST}){rows=new ArrayList<>(history.recent());rows.sort(order.comparator());check(rows.get(rows.size()-1).time==0,"Unknown/zero durations stay at the end in either direction");}
        System.out.println("PASS: combined replay filters, time validation, multi-term search, persistence and deterministic sort directions");
    }
}
