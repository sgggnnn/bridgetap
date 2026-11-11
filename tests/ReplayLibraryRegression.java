import com.bridger.momentum.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.*;

public class ReplayLibraryRegression {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 static ReplayData recording(){return new ReplayData(Arrays.asList(new ReplayData.Frame(0,70,0),new ReplayData.Frame(1,70,1)),Collections.singletonList(new ReplayData.Block(0,69,1,24,0,0,true)),false);}
 static RunHistory.Run add(RunHistory history,double time){history.add("SUCCESS",time,2,"Z",Arrays.asList("Flat .500","Tap .440"),recording(),"mc.example.net:25566");return history.recent().get(0);}
 public static void main(String[] args)throws Exception{
  Path root=Files.createTempDirectory("bridgetap-library-");RunHistory history=new RunHistory(root.resolve("saved"));
  RunHistory.Run a=add(history,4.2);history.rename(a,"Mon bridge été");check(a.name().equals("Mon bridge été"),"Renamed recent row updates in place");
  RunHistory reload=new RunHistory(root.resolve("saved"));RunHistory.Run saved=reload.saved().get(0);
  check(saved.name().equals("Mon bridge été")&&reload.hasReplay(saved),"Naming saves a recent replay and survives restart with Unicode");
  check(saved.server.equals("mc.example.net:25566"),"Server address and non-default port must survive saving and renaming");
  reload.rename(saved,"Alpha");check(new RunHistory(root.resolve("saved")).saved().get(0).name().equals("Alpha"),"Saved replay can be renamed again atomically");
  history.rename(a,"Zulu");RunHistory.Run b=add(history,3.5);history.rename(b,"beta");RunHistory.Run c=add(history,5.1);history.rename(c,"Alpha");
  List<RunHistory.Run> rows=new ArrayList<>(history.recent());rows.sort(ReplaySort.NAME.comparator());check(rows.get(0).id.equals(c.id)&&rows.get(1).id.equals(b.id)&&rows.get(2).id.equals(a.id),"Name sort ignores case");
  rows.sort(ReplaySort.FASTEST.comparator());check(rows.get(0).id.equals(b.id)&&rows.get(2).id.equals(c.id),"Fastest sort");rows.sort(ReplaySort.SLOWEST.comparator());check(rows.get(0).id.equals(c.id),"Slowest sort");
  check(ReplaySort.NEWEST.next()==ReplaySort.OLDEST&&ReplaySort.parse("bad")==ReplaySort.NEWEST,"Sort cycling and config fallback");
  Path export=history.exportReplay(a);check(Files.exists(export)&&export.getFileName().toString().endsWith(".btreplay"),"One portable export file is created");
  Path secondExport=history.exportReplay(a);check(!secondExport.equals(export)&&Files.exists(export),"Export does not overwrite an existing file");
  RunHistory friend=new RunHistory(root.resolve("friend/saved"));RunHistory.Run imported=friend.importReplay(export);
  check(imported.id.equals(a.id)&&imported.name().equals("Zulu")&&imported.time==4.2,"Friend import preserves name, original recording ID and metadata");
  check(imported.server.equals(a.server),"Friend export/import lost the recording's original server address");
  check(imported.isImported()&&new RunHistory(root.resolve("friend/saved")).saved().get(0).isImported(),"Imported origin must survive reload for library filtering");
  ReplayData data=friend.replay(imported);check(data.frames.get(1).x==1&&data.blocks.get(0).placed&&data.placementTags,"Portable replay is self-contained including tagged blocks");
  check(new RunHistory(root.resolve("friend/saved")).saved().size()==1,"Import survives restart");
  RunHistory.Run duplicate=friend.importReplay(export);check(!duplicate.id.equals(imported.id)&&friend.saved().size()==2,"Import collision creates a distinct saved run without overwriting");
  check(duplicate.server.equals(a.server),"Import collision lost the original server metadata");
  ReplayArchive.Content content=ReplayArchive.read(export);byte[] before=Files.readAllBytes(export);
  try{ReplayArchive.write(export,content.metadata,content.replay);throw new AssertionError("Existing export overwritten");}catch(FileAlreadyExistsException expected){}
  check(Arrays.equals(before,Files.readAllBytes(export)),"Failed CREATE_NEW must not delete the original export");
  Path unsafe=root.resolve("unsafe.btreplay");try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(unsafe))){zip.putNextEntry(new ZipEntry("../outside.properties"));zip.write(1);zip.closeEntry();}
  try{friend.importReplay(unsafe);throw new AssertionError("Path traversal archive accepted");}catch(IOException expected){}
  check(!Files.exists(root.getParent().resolve("outside.properties")),"Archive entry names are never extracted as paths");
  try{history.rename(a,"bad\nname");throw new AssertionError("Control-code name accepted");}catch(IllegalArgumentException expected){}
  check(a.name().equals("Zulu"),"Invalid rename leaves the name intact");
  history.rename(a,"");check(a.name().isEmpty()&&a.displayName().equals("Bridge replay"),"Names can be cleared");
  BridgeConfig.replaySort="FASTEST";BridgeConfig.save();BridgeConfig.replaySort="NEWEST";BridgeConfig.load();check(BridgeConfig.replaySort.equals("FASTEST"),"Sort preference survives restart");
  System.out.println("PASS: names, Unicode persistence, sorting, portable friend import/export, collision handling and invalid archives");
 }
}
