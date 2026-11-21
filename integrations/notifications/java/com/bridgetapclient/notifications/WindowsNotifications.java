package com.bridgetapclient.notifications;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** Windows APIs run inside Minecraft's JVM. No helper process, port or external app. */
public final class WindowsNotifications {
    private static boolean loaded,attempted;
    private static String failure="",lastStatus="";
    private WindowsNotifications(){}
    static synchronized boolean load(){
        if(loaded)return true;
        if(!System.getProperty("os.name","").toLowerCase(Locale.ROOT).contains("windows")||!System.getProperty("os.arch","").contains("64")){failure="Windows x64 is required";return false;}
        try(InputStream input=WindowsNotifications.class.getResourceAsStream("/native/windows-x64/BridgeTapNotifications.dll")){
            if(input==null)throw new IOException("Native library is missing");
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
            while((n=input.read(buffer))!=-1){if(out.size()+n>8*1024*1024)throw new IOException("Native library is too large");out.write(buffer,0,n);}
            byte[] bytes=out.toByteArray();String hash=hash(bytes);
            Path directory=Paths.get(System.getProperty("java.io.tmpdir"),"bridgetapclient-notifications",hash);Files.createDirectories(directory);
            Path library=directory.resolve("BridgeTapNotifications.dll");
            if(!Files.isRegularFile(library)||!hash(Files.readAllBytes(library)).equals(hash)){
                Path temp=directory.resolve("library-"+UUID.randomUUID()+".tmp");Files.write(temp,bytes);Files.move(temp,library,StandardCopyOption.REPLACE_EXISTING);
            }
            System.load(library.toAbsolutePath().toString());loaded=true;return true;
        }catch(Exception|LinkageError error){failure=error.getClass().getSimpleName();return false;}
    }
    private static String hash(byte[] bytes)throws Exception{StringBuilder text=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))text.append(String.format(Locale.ROOT,"%02x",b&255));return text.toString();}
    public static void requestAccess(){attempted=true;if(load())nStart(true);else Notifications.system("error","Windows notifications",failure);}
    public static void stop(){attempted=false;if(loaded)nStop();Notifications.SETTINGS.clearWindows();}
    static void tick(){
        NoticeQueue settings=Notifications.SETTINGS;
        if(!settings.enabled||!settings.windows){if(attempted)stop();return;}
        if(!attempted){
            attempted=true;
            if(load()){
                String access=nProbe();
                if(access.equals("allowed"))nStart(false);
                else if(access.equals("denied"))Notifications.system("info","Allow Windows notifications","In this mod's Windows settings, click Allow access to authorize Minecraft.");
                else Notifications.system("error","Windows notification access unavailable",access);
            }else Notifications.system("error","Windows notifications",failure);
        }
        if(!loaded)return;
        String status=nStatus();
        if(!status.equals(lastStatus)){
            lastStatus=status;
            if(status.equals("denied"))Notifications.system("warning","Allow Windows notifications","Open this mod's settings and click Allow access. Windows must authorize Minecraft to read notifications.");
            else if(status.startsWith("unavailable"))Notifications.system("error","Windows notification access unavailable",status);
        }
        String[] batch=nDrain();if(batch==null)return;long now=System.currentTimeMillis();
        for(int i=0;i+5<batch.length;i+=6)try{settings.offer(new NoticeQueue.Notice(batch[i],batch[i+1],batch[i+2],batch[i+3],batch[i+4],"info",Long.parseLong(batch[i+5])),true,now);}catch(RuntimeException malformed){}
    }
    static String probe(){return load()?nProbe():failure;}
    static String[] synthetic(){if(!load())throw new IllegalStateException(failure);nSynthetic();return nDrain();}
    static String verifyExistingAccess()throws InterruptedException{
        if(!load())return failure;if(!nProbe().equals("allowed"))return "denied";
        nStart(false);
        try{long deadline=System.currentTimeMillis()+5000;String status;
            do{Thread.sleep(50);status=nStatus();}while((status.equals("starting")||status.equals("off"))&&System.currentTimeMillis()<deadline);
            if(status.equals("allowed")){Thread.sleep(900);status=nStatus();}
            return status;
        }finally{nStop();}
    }
    private static native void nStart(boolean request);
    private static native void nStop();
    private static native String nStatus();
    private static native String nProbe();
    private static native String[] nDrain();
    private static native void nSynthetic();
}
