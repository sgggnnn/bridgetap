import com.bridger.momentum.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class ReplayRetentionRegression {
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    static RunHistory.Run add(RunHistory h,Path dir,String status,boolean protect,long created)throws Exception{h.add(status,4,3,"Z",Collections.emptyList());RunHistory.Run r=h.recent().get(0);if(protect)h.save(r);else h.saveAutomatic(r);Path file=dir.resolve(r.id+".properties");Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}p.setProperty("created",String.valueOf(created));try(OutputStream out=Files.newOutputStream(file)){p.store(out,"test age");}return r;}
    public static void main(String[] args)throws Exception{
        Path root=Files.createTempDirectory("bridgetap-retention-"),dir=root.resolve("saved");RunHistory history=new RunHistory(dir);long now=System.currentTimeMillis(),day=86400000L;
        RunHistory.Run expired=add(history,dir,"FAILED",false,now-3*day),manual=add(history,dir,"FAILED",true,now-3*day),success=add(history,dir,"SUCCESS",false,now-3*day),young=add(history,dir,"FAILED",false,now-day),favorite=add(history,dir,"FAILED",false,now-3*day);history.toggleFavorite(favorite);
        RunHistory.Run promoted=add(history,dir,"FAILED",false,now-3*day);history.save(promoted);check(promoted.isProtected(),"Save replay must protect an existing automatic recording");
        RunHistory.Run legacy=add(history,dir,"FAILED",false,now-3*day);Path file=dir.resolve(legacy.id+".properties");Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}p.remove("protected");try(OutputStream out=Files.newOutputStream(file)){p.store(out,"legacy");}
        ReplayFolders folders=new ReplayFolders(root.resolve("folders.properties"));check(folders.cleanupDue(history,now)==0,"Cleanup must default to OFF");folders.toggleCleanupFailures();check(folders.cleanupDue(history,now)==1,"Only an old unprotected failure should be removed");
        check(!Files.exists(dir.resolve(expired.id+".properties")),"Expired failure survived cleanup");for(RunHistory.Run kept:Arrays.asList(manual,success,young,favorite,promoted,legacy))check(Files.exists(dir.resolve(kept.id+".properties")),"Cleanup removed a protected, successful or recent replay");
        check(new ReplayFolders(root.resolve("folders.properties")).cleanupFailures(),"Cleanup preference did not persist");
        System.out.println("PASS: 48-hour opt-in failed-run cleanup; manual saves, favorites, legacy saves and successes are protected");
    }
}
