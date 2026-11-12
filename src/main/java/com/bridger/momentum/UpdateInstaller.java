package com.bridger.momentum;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;
import java.awt.Desktop;

/** Standalone Java helper. Replaces only the running BridgeTap JAR after Minecraft exits. */
public final class UpdateInstaller {
 public static final class Plan {
  public final Path old,staged,target,job;public final String oldHash,newHash,profile;public final int port;public final String nonce;
  public Plan(Path old,Path staged,String target,String oldHash,String newHash,int port,String nonce,String profile)throws IOException{
   this.old=old.toAbsolutePath().normalize();this.staged=staged.toAbsolutePath().normalize();job=this.staged.getParent();this.target=this.old.resolveSibling(target).normalize();this.oldHash=oldHash;this.newHash=newHash;this.port=port;this.nonce=nonce;this.profile=profile;
   Path parent=this.old.getParent();boolean mods=parent!=null&&(parent.getFileName().toString().equals("mods")||(parent.getFileName().toString().equals("1.8.9")&&parent.getParent()!=null&&parent.getParent().getFileName().toString().equals("mods")));
   if(!mods||!this.old.toString().endsWith(".jar")||Files.isSymbolicLink(this.old)||!this.target.getParent().equals(parent)||!target.matches("BridgeTap-[0-9A-Za-z.\\-]+(?:-Forge-1\\.8\\.9|-OneClient)?\\.jar")||this.old.equals(this.staged)||!oldHash.matches("[a-f0-9]{64}")||!newHash.matches("[a-f0-9]{64}")||port<0||port>65535||nonce==null||nonce.length()>100||profile==null||profile.length()>256||profile.matches(".*[\\p{Cntrl}].*"))throw new IOException("Automatic installation requires a regular BridgeTap JAR in this profile's mods folder");
  }
  public Path backup(){return job.resolve("previous.jar.bak");}
  public Path result(){return job.resolve("result.properties");}
  public Path file(){return job.resolve("install.properties");}
  public void write(boolean restart)throws IOException{Properties p=new Properties();p.setProperty("old",old.toString());p.setProperty("staged",staged.toString());p.setProperty("target",target.getFileName().toString());p.setProperty("oldHash",oldHash);p.setProperty("newHash",newHash);p.setProperty("port",String.valueOf(port));p.setProperty("nonce",nonce);p.setProperty("profile",profile);p.setProperty("restart",String.valueOf(restart));try(OutputStream out=Files.newOutputStream(file())){p.store(out,"BridgeTap approved update installation");}}
  static Plan read(Properties p)throws IOException{return new Plan(Paths.get(p.getProperty("old")),Paths.get(p.getProperty("staged")),p.getProperty("target"),p.getProperty("oldHash"),p.getProperty("newHash"),Integer.parseInt(p.getProperty("port")),p.getProperty("nonce"),p.getProperty("profile",""));}
 }
 public static String hash(Path file)throws IOException{try{MessageDigest md=MessageDigest.getInstance("SHA-256");byte[] b=new byte[65536];try(InputStream in=Files.newInputStream(file)){int n;while((n=in.read(b))!=-1)md.update(b,0,n);}StringBuilder s=new StringBuilder();for(byte v:md.digest())s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}catch(NoSuchAlgorithmException ex){throw new IOException(ex);}}
 public static void apply(Plan p)throws IOException{
  if(!Files.isRegularFile(p.old,LinkOption.NOFOLLOW_LINKS)||!hash(p.old).equals(p.oldHash))throw new IOException("The installed JAR changed; update cancelled");
  if(!Files.isRegularFile(p.staged,LinkOption.NOFOLLOW_LINKS)||!hash(p.staged).equals(p.newHash))throw new IOException("Update checksum mismatch");
  if(!p.target.equals(p.old)&&Files.exists(p.target,LinkOption.NOFOLLOW_LINKS))throw new IOException("The new filename already exists; no files replaced");
  if(Files.exists(p.backup(),LinkOption.NOFOLLOW_LINKS))throw new IOException("A previous installation already owns this backup");
  Path pending=Files.createTempFile(p.old.getParent(),".bridgetap-update-",".pending");boolean moved=false;
  try{
   Files.copy(p.staged,pending,StandardCopyOption.REPLACE_EXISTING);if(!hash(pending).equals(p.newHash))throw new IOException("Copied update checksum mismatch");
   Files.move(p.old,p.backup());moved=true;
   try{Files.move(pending,p.target);}catch(IOException ex){Files.move(p.backup(),p.old);moved=false;throw ex;}
  }finally{Files.deleteIfExists(pending);}
  if(!moved||!hash(p.target).equals(p.newHash))throw new IOException("Installed update verification failed");
 }
 public static void helperJar(Path source,Path target)throws IOException{
  try(JarFile in=new JarFile(source.toFile());JarOutputStream out=new JarOutputStream(Files.newOutputStream(target,StandardOpenOption.CREATE_NEW))){Enumeration<JarEntry> entries=in.entries();while(entries.hasMoreElements()){JarEntry e=entries.nextElement();String n=e.getName();if((n.startsWith("com/bridger/momentum/UpdateInstaller")&&n.endsWith(".class"))||n.equals("META-INF/licenses/BridgeTap-GPL-3.0-only.txt")){out.putNextEntry(new JarEntry(n));try(InputStream data=in.getInputStream(e)){byte[] b=new byte[8192];int count;while((count=data.read(b))!=-1)out.write(b,0,count);}out.closeEntry();}}}
 }
 public static String oneClientProfile(Path jar){Path mods=jar.toAbsolutePath().normalize().getParent();if(mods!=null&&mods.getFileName().toString().equals("mods")){Path profile=mods.getParent();if(profile!=null&&profile.getParent()!=null&&profile.getParent().getFileName().toString().equals("clusters"))return profile.getFileName().toString();}return "";}
 public static void launchOneClient(String profile)throws Exception{
  if(profile.isEmpty())throw new IOException("Restart Minecraft from your launcher");
  String local=System.getenv("LOCALAPPDATA");Path exe=local==null?null:Paths.get(local,"OneClient","oneclient_app.exe");
  if(exe!=null&&Files.isRegularFile(exe)){new ProcessBuilder(exe.toString(),"--launch",profile).start();return;}
  if(Desktop.isDesktopSupported()&&Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){Desktop.getDesktop().browse(new URI("oneclient://launch/"+URLEncoder.encode(profile,"UTF-8").replace("+","%20")));return;}
  throw new IOException("Open OneClient and launch "+profile);
 }
 private static void waitForGame(Plan p)throws IOException{
  boolean authenticated=false;
  try(Socket socket=new Socket()){
   socket.connect(new InetSocketAddress("127.0.0.1",p.port),3000);socket.setSoTimeout(15000);DataInputStream in=new DataInputStream(socket.getInputStream());if(!in.readUTF().equals(p.nonce))throw new IOException("Update parent handshake failed");authenticated=true;socket.setSoTimeout(0);while(in.read()!=-1){}
  }catch(ConnectException|EOFException ex){if(!Files.exists(p.job.resolve("shutdown")))throw new IOException("Minecraft exit could not be verified",ex);}
  catch(SocketException ex){
   // Windows may reset a socket instead of delivering EOF when the JVM exits.
   // Accept that closure only after this helper authenticated its parent.
   if(!authenticated)throw new IOException("Minecraft exit could not be verified",ex);
  }
 }
 public static void main(String[] args)throws Exception{
  Properties props=new Properties();try(InputStream in=Files.newInputStream(Paths.get(args[0]))){props.load(in);}Plan p=Plan.read(props);Properties result=new Properties();
  try{
   waitForGame(p);Thread.sleep(1000);
   IOException last=null;for(int i=0;i<120;i++){try{apply(p);last=null;break;}catch(FileSystemException locked){last=locked;Thread.sleep(1000);}}
   if(last!=null)throw last;result.setProperty("status","installed");
   if(Boolean.parseBoolean(props.getProperty("restart"))){try{launchOneClient(p.profile);}catch(Exception ex){result.setProperty("restartError",ex.getMessage());}}
  }catch(Exception ex){result.setProperty("status","failed");result.setProperty("error",String.valueOf(ex.getMessage()));}
  try(OutputStream out=Files.newOutputStream(p.result())){result.store(out,"BridgeTap update result");}
 }
}
