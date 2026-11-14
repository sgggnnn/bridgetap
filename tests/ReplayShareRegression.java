import com.bridger.momentum.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public final class ReplayShareRegression {
 interface Work{void run()throws Exception;}
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 static void rejected(Work w)throws Exception{try{w.run();throw new AssertionError("Unsafe input accepted");}catch(IOException expected){}}
 static byte[] raw(Path p)throws Exception{try(InputStream in=new GZIPInputStream(Files.newInputStream(p))){return read(in);}}
 static byte[] read(InputStream in)throws Exception{ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)bytes.write(b,0,n);return bytes.toByteArray();}
 static byte[] comment(byte[] archive,int size)throws Exception{
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(ZipInputStream in=new ZipInputStream(new ByteArrayInputStream(archive));ZipOutputStream out=new ZipOutputStream(bytes)){ZipEntry e;while((e=in.getNextEntry())!=null){ZipEntry n=new ZipEntry(e.getName());n.setTime(0);out.putNextEntry(n);out.write(read(in));out.closeEntry();}out.setComment(new String(new char[size]).replace('\0','x'));}return bytes.toByteArray();
 }
 public static void main(String[] args)throws Exception{
  Path root=Files.createTempDirectory("share-regression-");RunHistory source=new RunHistory(root.resolve("source/saved"));
  ReplayData.Identity identity=new ReplayData.Identity("12345678-1234-1234-1234-123456789012","PrivatePlayer","PrivatePlayer","skin-value","signature","default",127);
  ReplayData recording=new ReplayData(Arrays.asList(new ReplayData.Frame(1,65,2),new ReplayData.Frame(1.2,65.4,2.2)),Collections.singletonList(new ReplayData.Block(1,64,2,24,0,1,true)),false,identity,0,6000);
  source.add("SUCCESS",.05,1,"Z",Arrays.asList("Tap Speed #1: 123", "private C:/Users/secret"),recording,"private.example");RunHistory.Run run=source.recent().get(0);source.rename(run,"PrivatePlayer personal path");Path original=source.exportReplay(run);String hash=UpdateInstaller.hash(original);
  byte[] shared=ReplaySharing.prepare(source,run,false);ReplayArchive.Content content=ReplayArchive.read(shared);String meta=content.metadata.toString();
  check(content.replay.identity.name.equals("Player")&&content.replay.identity.textureValue.isEmpty()&&!meta.contains("PrivatePlayer")&&!meta.contains("C:/")&&!meta.contains("private.example"),"Anonymous sharing leaked identity or metadata");
  check(ReplayArchive.read(ReplaySharing.prepare(source,run,true)).replay.identity.name.equals("PrivatePlayer"),"Explicit identity inclusion lost recorded identity");
  String code=ReplayShareCode.encode(shared);ReplayArchive.Content decoded=ReplayShareCode.decode(code);check(decoded.replay.frames.get(1).x==1.2&&decoded.replay.blocks.get(0).placed,"Share code lost frames/placement provenance");
  check(ReplayShareCode.decode("```\n"+code+"\n```").replay.frames.size()==2,"Messenger code fences failed");
  rejected(()->ReplayShareCode.decode(code.substring(0,code.length()-2)));rejected(()->ReplayShareCode.decode(code.substring(0,6)+(code.charAt(6)=='a'?'b':'a')+code.substring(7)));rejected(()->ReplayShareCode.decode(code.replace("BTR1:","BTR2:")));rejected(()->ReplayShareCode.decode(new String(new char[ReplayShareCode.MAX_CHARS+33])));
  byte[] base=comment(shared,0);int pad=1372-base.length;check(pad>=0,"Boundary fixture too large");byte[] limit=comment(shared,pad);check(ReplayShareCode.encode(limit).length()==1900,"Exact 1900-character boundary failed");rejected(()->ReplayShareCode.encode(comment(shared,pad+1)));
  RunHistory friend=new RunHistory(root.resolve("friend/saved"));ReplayFolders folders=new ReplayFolders(root.resolve("friend/folders.properties"));RunHistory.previewImport(decoded);check(friend.saved().isEmpty()&&folders.folders().isEmpty(),"Preview imported or created folders before confirmation");
  RunHistory.Run imported=ReplayImportFlow.confirm(friend,decoded,folders);check(imported.isImported()&&folders.label(folders.folderOf(imported.id)).equals("Imported"),"Confirmation did not file replay into Imported");check(UpdateInstaller.hash(original).equals(hash),"Source archive was modified");
  for(int version:new int[]{4,5})for(boolean tags:new boolean[]{false,true}){
   Path legacy=root.resolve("legacy-"+version+"-"+tags+".btr");recording.write(legacy);byte[] bytes=raw(legacy);bytes[7]=(byte)version;bytes[17]=(byte)(tags?1:0);try(OutputStream out=new GZIPOutputStream(Files.newOutputStream(legacy))){out.write(bytes);}String originalHash=UpdateInstaller.hash(legacy);ReplayData data=ReplayData.read(legacy);
   Properties properties=new Properties();properties.putAll(content.metadata);byte[] portable=ReplayArchive.bytes(properties,data);ReplayData result=ReplayShareCode.decode(ReplayShareCode.encode(portable)).replay;
   check(result.formatVersion==version&&result.placementTags==tags&&result.frames.get(1).x==1.2&&UpdateInstaller.hash(legacy).equals(originalHash),"Legacy v4/v5 phase or source changed");
  }
  ByteArrayOutputStream bad=new ByteArrayOutputStream();try(ZipOutputStream zip=new ZipOutputStream(bad)){zip.putNextEntry(new ZipEntry("../recording.btr"));zip.write(1);zip.closeEntry();}rejected(()->ReplayArchive.read(bad.toByteArray()));
  byte[] enormous=new byte[ReplayLimits.ARCHIVE+1];rejected(()->ReplayArchive.read(enormous));
  ByteArrayOutputStream bomb=new ByteArrayOutputStream();try(GZIPOutputStream out=new GZIPOutputStream(bomb)){byte[] zeros=new byte[8192];for(int i=0;i<ReplayLimits.INFLATED/8192+2;i++)out.write(zeros);}rejected(()->{try(InputStream in=ReplayLimits.bounded(new GZIPInputStream(new ByteArrayInputStream(bomb.toByteArray())),ReplayLimits.INFLATED)){read(in);}});
  Properties traversal=new Properties();traversal.putAll(content.metadata);traversal.setProperty("id","../../escape");rejected(()->RunHistory.previewImport(ReplayArchive.read(ReplayArchive.bytes(traversal,recording))));
  System.out.println("PASS: bounded offline codes, SHA-256 corruption/truncation, exact size boundary, privacy, preview/confirmation, Imported folder, v4/v5 provenance and decompression/path defenses");
 }
}
