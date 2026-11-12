import com.bridger.momentum.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
public final class UpdateRegression {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static UpdateInstaller.Plan plan(Path root)throws Exception{Path mods=root.resolve("mods"),job=root.resolve("job");Files.createDirectories(mods);Files.createDirectories(job);Path old=mods.resolve("BridgeTap-old.jar"),next=job.resolve("candidate.jar");Files.write(old,new byte[]{1,2,3});Files.write(next,new byte[]{4,5,6,7});return new UpdateInstaller.Plan(old,next,"BridgeTap-9.0.0-OneClient.jar",UpdateInstaller.hash(old),UpdateInstaller.hash(next),0,"nonce","");}
 static void rejected(UpdateInstaller.Plan p)throws Exception{byte[] before=Files.readAllBytes(p.old);try{UpdateInstaller.apply(p);throw new AssertionError("Invalid replacement accepted");}catch(IOException expected){}check(Arrays.equals(before,Files.readAllBytes(p.old)),"Failed installation removed or modified the old JAR");}
 public static void main(String[] args)throws Exception{
  check(UpdateRelease.newer("1.10.0","1.9.9")&&!UpdateRelease.newer("1.7.0","1.7.2")&&!UpdateRelease.newer("1.7.2","1.7.2")&&UpdateRelease.newer("1.7.2","1.7.2-dev.10"),"Version order allows downgrades or mishandles previews");
  try{new UpdateRelease("9.0.0","../../bad.jar",String.join("",Collections.nCopies(64,"a")),10,false);throw new AssertionError("Unsafe filename accepted");}catch(IllegalArgumentException good){}
  Path root=Files.createTempDirectory("bridgetap-update-test-");UpdateInstaller.Plan p=plan(root.resolve("success"));Path unrelated=p.old.resolveSibling("OtherMod.jar");Files.write(unrelated,new byte[]{9});UpdateInstaller.apply(p);check(!Files.exists(p.old)&&Files.exists(p.target)&&UpdateInstaller.hash(p.target).equals(p.newHash)&&UpdateInstaller.hash(p.backup()).equals(p.oldHash)&&Files.readAllBytes(unrelated)[0]==9,"Replacement/backup changed unrelated files");
  p=plan(root.resolve("tampered"));Files.write(p.staged,new byte[]{0});rejected(p);
  p=plan(root.resolve("collision"));Files.write(p.target,new byte[]{8});rejected(p);check(Files.readAllBytes(p.target)[0]==8,"Existing target overwritten");
  p=plan(root.resolve("changed-original"));Files.write(p.old,new byte[]{7});rejected(p);
  try{new UpdateInstaller.Plan(root.resolve("foreign.jar"),p.staged,"BridgeTap-9.0.0.jar",p.oldHash,p.newHash,0,"","");throw new AssertionError("Installer escaped mods folder");}catch(IOException good){}
  check(UpdateInstaller.oneClientProfile(root.resolve("clusters/1.8.9 Fabric/mods/BridgeTap.jar")).equals("1.8.9 Fabric")&&UpdateInstaller.oneClientProfile(root.resolve("instance/mods/BridgeTap.jar")).isEmpty(),"Restart profile guessed for unsupported launchers");
  if(System.getProperty("os.name").toLowerCase().contains("win")){p=plan(root.resolve("locked"));try(FileInputStream locked=new FileInputStream(p.old.toFile())){rejected(p);}UpdateInstaller.apply(p);}
  p=plan(root.resolve("deferred"));Path source=Paths.get(UpdateInstaller.class.getProtectionDomain().getCodeSource().getLocation().toURI()),helper=p.job.resolve("helper.jar");UpdateInstaller.helperJar(source,helper);
  try(ServerSocket lifetime=new ServerSocket(0,1,InetAddress.getByName("127.0.0.1"))){
   p=new UpdateInstaller.Plan(p.old,p.staged,p.target.getFileName().toString(),p.oldHash,p.newHash,lifetime.getLocalPort(),"test-parent","");p.write(false);
   String java=Paths.get(System.getProperty("java.home"),"bin",System.getProperty("os.name").toLowerCase().contains("win")?"java.exe":"java").toString();
   Process process=new ProcessBuilder(java,"-cp",helper.toString(),"com.bridger.momentum.UpdateInstaller",p.file().toString()).redirectErrorStream(true).redirectOutput(p.job.resolve("test-helper.log").toFile()).start();
   lifetime.setSoTimeout(10000);try(Socket child=lifetime.accept()){DataOutputStream out=new DataOutputStream(child.getOutputStream());out.writeUTF("test-parent");out.flush();check(!process.waitFor(400,TimeUnit.MILLISECONDS)&&Files.exists(p.old)&&!Files.exists(p.target),"Helper replaced JAR while parent was still alive");}
   check(process.waitFor(15,TimeUnit.SECONDS),"Deferred installer did not finish");Properties result=new Properties();try(InputStream in=Files.newInputStream(p.result())){result.load(in);}check(result.getProperty("status").equals("installed")&&Files.exists(p.target)&&!Files.exists(p.old),"Deferred installation failed: "+result);
  }
  System.out.println("PASS: version selection, checksum rejection, collision protection, Windows locks, backup, unrelated files and real helper waiting for parent exit");
 }
}
