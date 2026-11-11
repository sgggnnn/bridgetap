import com.bridger.momentum.*;
import java.nio.file.*;

public final class ReplayFoldersRegression {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Path path=Paths.get("folder-test","folders.properties");ReplayFolders f=new ReplayFolders(path);
        String practice=f.create("Practice"),favorites=f.create("Îles rapides");f.move("replay-1",practice);f.move("replay-2",favorites);
        check(f.matches("replay-1",practice)&&!f.matches("replay-2",practice)&&f.matches("replay-1",ReplayFolders.ALL),"Folder filtering");
        check(f.matches("missing",ReplayFolders.UNFILED),"Unassigned runs belong to Unfiled");
        f.rename(practice,"Warmup");f=new ReplayFolders(path);check(f.label(practice).equals("Warmup")&&f.folderOf("replay-1").equals(practice),"Rename and membership persist");
        try{f.create("warmup");throw new AssertionError("Duplicate folder name accepted");}catch(IllegalArgumentException expected){}
        f.move("replay-1",favorites);check(f.folderOf("replay-1").equals(favorites),"Moving changes the existing membership");
        f.delete(favorites);f=new ReplayFolders(path);check(f.folderOf("replay-1").equals(ReplayFolders.UNFILED)&&f.folderOf("replay-2").equals(ReplayFolders.UNFILED),"Deleting a folder unfiles its members");
        check(f.folders().size()==1&&f.label(practice).equals("Warmup"),"Other folders survive deletion");
        f.move("replay-1",practice);f.move("replay-1",ReplayFolders.UNFILED);check(f.folderOf("replay-1").equals(ReplayFolders.UNFILED),"Explicit unfile");
        RunHistory history=new RunHistory(Paths.get("folder-test","saved"));history.add("SUCCESS",3.85,3,"Z",java.util.Collections.emptyList(),null,"MC.Example.net:25565");RunHistory.Run one=history.recent().get(0);f.fileByServer(history,one);String serverFolder=f.folderOf(one.id);check(!serverFolder.equals(ReplayFolders.UNFILED)&&history.saved().size()==1,"Automatic filing must save the run in its server folder");
        f.rename(serverFolder,"My practice server");f=new ReplayFolders(path);history.add("SUCCESS",4.0,3,"Z",java.util.Collections.emptyList(),null,"mc.example.net");RunHistory.Run two=history.recent().get(0);f.fileByServer(history,two);check(f.folderOf(two.id).equals(serverFolder),"Case, default port and folder rename must not create duplicate server folders");
        history.add("SUCCESS",4.0,3,"Z",java.util.Collections.emptyList(),null,"other.example.net");f.fileByServer(history,history.recent().get(0));check(!f.folderOf(history.recent().get(0).id).equals(serverFolder),"Different servers must have separate folders");
        f.toggleAutoServers();history.add("SUCCESS",4.0,3,"Z",java.util.Collections.emptyList(),null,"disabled.example.net");f.fileByServer(history,history.recent().get(0));check(f.folderOf(history.recent().get(0).id).equals(ReplayFolders.UNFILED)&&!new ReplayFolders(path).autoServers(),"Automatic filing OFF must persist");
        System.out.println("PASS: create/rename/delete replay folders, Unicode persistence, deterministic views and non-destructive moves");
    }
}
