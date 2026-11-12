package com.bridger.momentum;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_85740840;
import net.minecraft.unmapped.C_95462098;

/** One asynchronous stable-release check per startup; all UI changes happen on the game thread. */
public final class UpdateManager {
 public enum State{IDLE,CHECKING,CURRENT,AVAILABLE,DOWNLOADING,READY,FAILED}
 public static volatile State state=State.IDLE;public static volatile String error="",installed="",message="";public static volatile UpdateRelease release;public static volatile long downloaded;
 private static Path own;private static boolean forge,started,dismissed,previousFailure;private static UpdateInstaller.Plan plan;private static Path helper;
 private static final AtomicBoolean launched=new AtomicBoolean();private static ServerSocket lifetime;
 private static final Path PREFS=Paths.get("config","bridgetap","updates.properties");public static boolean automatic=true;
 private UpdateManager(){}
 private static void prefs(){try{if(Files.isRegularFile(PREFS)){Properties p=new Properties();try(InputStream in=Files.newInputStream(PREFS)){p.load(in);}automatic=Boolean.parseBoolean(p.getProperty("checkOnStartup","true"));}}catch(IOException ignored){}}
 public static void toggle(){automatic=!automatic;Properties p=new Properties();p.setProperty("checkOnStartup",String.valueOf(automatic));try{Files.createDirectories(PREFS.getParent());try(OutputStream out=Files.newOutputStream(PREFS)){p.store(out,"BridgeTap updates: checks GitHub, no game data uploaded");}}catch(IOException ex){error=ex.getMessage();}}
 private static void locate()throws Exception{
  own=Paths.get(UpdateManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();if(!Files.isRegularFile(own)||!own.toString().endsWith(".jar"))throw new IOException("Updates are available only in a packaged mod JAR");
  try(JarFile jar=new JarFile(own.toFile())){forge=jar.getJarEntry("mcmod.info")!=null&&jar.getJarEntry("fabric.mod.json")==null;}installed=UpdateSource.version(own,forge);
 }
 private static void previousResult(){
  Path root=Paths.get("config","bridgetap","updates");if(!Files.isDirectory(root))return;
  try(java.nio.file.DirectoryStream<Path> dirs=Files.newDirectoryStream(root)){int count=0;for(Path job:dirs){if(++count>1000)break;Path result=job.resolve("result.properties"),seen=job.resolve("notified");if(!Files.isRegularFile(result)||Files.exists(seen)||Files.size(result)>65536)continue;Properties p=new Properties();try(InputStream in=Files.newInputStream(result)){p.load(in);}Files.write(seen,new byte[]{1});if(p.getProperty("status","").equals("failed")||p.containsKey("restartError")){error="Previous update: "+p.getProperty("error",p.getProperty("restartError","Installation failed"));previousFailure=true;state=State.FAILED;}}}catch(IOException ignored){}
 }
 public static void tick(Minecraft mc){
  if(!started){started=true;prefs();try{locate();previousResult();if(automatic&&!previousFailure)check();}catch(Exception ex){state=State.FAILED;error=String.valueOf(ex.getMessage());}}
  if((state==State.AVAILABLE||previousFailure)&&!dismissed&&mc.f_28396371==null&&mc.f_26407825==null&&mc.f_70816363 instanceof C_95462098){dismissed=true;mc.m_52715402(new UpdateScreen(mc.f_70816363));}
 }
 private static void async(Runnable task){Thread worker=new Thread(task,"BridgeTap update check");worker.setDaemon(true);worker.start();}
 public static synchronized void check(){if(state==State.CHECKING||state==State.DOWNLOADING||state==State.READY)return;state=State.CHECKING;previousFailure=false;error="";async(()->{try{if(own==null)locate();UpdateRelease found=UpdateSource.latest(installed,forge);release=found;state=found==null?State.CURRENT:State.AVAILABLE;}catch(Exception ex){error=String.valueOf(ex.getMessage());state=State.FAILED;}});}
 public static void open(C_85740840 parent){prefs();if(state==State.IDLE||state==State.FAILED||state==State.CURRENT)check();Minecraft.m_20213497().m_52715402(new UpdateScreen(parent));}
 public static void dismiss(){dismissed=true;}
 public static synchronized void install(){
  if(state!=State.AVAILABLE||release==null)return;state=State.DOWNLOADING;downloaded=0;error="";final UpdateRelease candidate=release;
  async(()->{ServerSocket socket=null;try{
   Path job=Paths.get("config","bridgetap","updates",UUID.randomUUID().toString()).toAbsolutePath();Files.createDirectories(job);Path file=job.resolve("candidate.jar");
   // Validate the installation boundary before downloading anything.
   new UpdateInstaller.Plan(own,file,candidate.name,UpdateInstaller.hash(own),candidate.sha256,0,"","");
   UpdateSource.download(candidate,file,n->downloaded=n);socket=new ServerSocket(0,8,InetAddress.getByName("127.0.0.1"));lifetime=socket;final String nonce=UUID.randomUUID().toString();final ServerSocket server=socket;
   Thread gate=new Thread(()->{while(!server.isClosed())try{final Socket accepted=server.accept();Thread hold=new Thread(()->{try{DataOutputStream out=new DataOutputStream(accepted.getOutputStream());out.writeUTF(nonce);out.flush();while(accepted.getInputStream().read()!=-1){}accepted.close();}catch(IOException ignored){}},"BridgeTap update lifetime");hold.setDaemon(true);hold.start();}catch(IOException end){return;}},"BridgeTap update lifetime server");gate.setDaemon(true);gate.start();
   plan=new UpdateInstaller.Plan(own,file,candidate.name,UpdateInstaller.hash(own),candidate.sha256,socket.getLocalPort(),nonce,UpdateInstaller.oneClientProfile(own));helper=job.resolve("installer-helper.jar");UpdateInstaller.helperJar(own,helper);plan.write(false);
   Runtime.getRuntime().addShutdownHook(new Thread(()->{try{Files.write(plan.job.resolve("shutdown"),new byte[]{1});launch(false);}catch(Exception ex){writeFailure(ex);}},"BridgeTap update installation"));
   state=State.READY;
  }catch(Exception ex){if(socket!=null)try{socket.close();}catch(IOException ignored){}error=String.valueOf(ex.getMessage());state=State.FAILED;}});
 }
 private static void writeFailure(Exception ex){if(plan==null)return;try{Properties p=new Properties();p.setProperty("status","failed");p.setProperty("error",String.valueOf(ex.getMessage()));try(OutputStream out=Files.newOutputStream(plan.result())){p.store(out,"BridgeTap update result");}}catch(IOException ignored){}}
 private static synchronized void launch(boolean restart)throws IOException{
  if(launched.get())return;plan.write(restart);String executable=System.getProperty("os.name","").toLowerCase(Locale.ROOT).contains("win")?"javaw.exe":"java";Path java=Paths.get(System.getProperty("java.home"),"bin",executable);
  if(!Files.isRegularFile(java))throw new IOException("Java runtime executable not found");
  new ProcessBuilder(java.toString(),"-cp",helper.toString(),"com.bridger.momentum.UpdateInstaller",plan.file().toString()).redirectErrorStream(true).redirectOutput(plan.job.resolve("helper.log").toFile()).start();launched.set(true);
 }
 public static boolean canRestart(){return plan!=null&&!plan.profile.isEmpty();}
 public static void restart(){try{launch(canRestart());Minecraft.m_20213497().m_12353564();}catch(Exception ex){error=String.valueOf(ex.getMessage());state=State.FAILED;}}
 public static void releasePage(){try{java.awt.Desktop.getDesktop().browse(new URI("https://github.com/sgggnnn/bridgetap/releases/latest"));}catch(Exception ex){error="Open github.com/sgggnnn/bridgetap/releases/latest";}}
}
