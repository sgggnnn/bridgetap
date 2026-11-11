package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Extract pinned private tools from the mod; never executes a tool from PATH. */
public final class NativeClipTools {
    public static boolean windows(){return System.getProperty("os.name","").toLowerCase(Locale.US).contains("win")&&System.getProperty("os.arch","").contains("64");}
    public static synchronized Path tool(String name)throws IOException{
        if(!windows()||(!name.equals("ffmpeg.exe")&&!name.equals("bridgetap-loopback.exe")))throw new IOException("Windows x64 audio tools unavailable");
        Properties hashes=new Properties();try(InputStream in=NativeClipTools.class.getResourceAsStream("/bridgetap-native/hashes.properties")){if(in==null)throw new IOException("Missing clip runtime");hashes.load(in);}
        String expected=hashes.getProperty(name);if(expected==null)throw new IOException("Unknown clip tool");
        Path folder=Paths.get("config","bridgetap","runtime","1.6.0-dev.2").toAbsolutePath();Files.createDirectories(folder);Path file=folder.resolve(name);
        if(Files.isRegularFile(file)&&hash(file).equals(expected))return file;
        Path temp=Files.createTempFile(folder,"clip-",".part");
        try(InputStream in=NativeClipTools.class.getResourceAsStream("/bridgetap-native/"+name)){
            if(in==null)throw new IOException("Missing clip tool");Files.copy(in,temp,StandardCopyOption.REPLACE_EXISTING);if(!hash(temp).equals(expected))throw new IOException("Clip tool checksum mismatch");Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);
        }finally{Files.deleteIfExists(temp);}return file;
    }
    private static String hash(Path file)throws IOException{
        try{MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] data=new byte[65536];try(InputStream in=Files.newInputStream(file)){int n;while((n=in.read(data))>=0)digest.update(data,0,n);}StringBuilder result=new StringBuilder();for(byte b:digest.digest())result.append(String.format(Locale.ROOT,"%02x",b&255));return result.toString();}catch(NoSuchAlgorithmException ex){throw new IOException(ex);}
    }
}
