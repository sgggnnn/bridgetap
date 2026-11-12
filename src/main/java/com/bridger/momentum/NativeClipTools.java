package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Use explicitly installed, checksum-verified tools. Never download or extract executables. */
public final class NativeClipTools {
    public static boolean windows(){return System.getProperty("os.name","").toLowerCase(Locale.US).contains("win")&&System.getProperty("os.arch","").contains("64");}
    public static synchronized Path tool(String name)throws IOException{
        if(!windows()||(!name.equals("ffmpeg.exe")&&!name.equals("bridgetap-loopback.exe")))throw new IOException("Windows x64 audio tools unavailable");
        Properties hashes=new Properties();try(InputStream in=NativeClipTools.class.getResourceAsStream("/bridgetap-native/hashes.properties")){if(in==null)throw new IOException("Missing clip runtime");hashes.load(in);}
        String expected=hashes.getProperty(name);if(expected==null)throw new IOException("Unknown clip tool");
        Path installed=Paths.get("config","bridgetap","tools",name).toAbsolutePath();
        if(Files.exists(installed)){
            if(!Files.isRegularFile(installed)||!hash(installed).equals(expected))throw new IOException("Clip tool checksum mismatch: "+name);
            return installed;
        }
        // Existing users may already have this exact verified runtime from 1.6.
        Path legacy=Paths.get("config","bridgetap","runtime","1.6.0-dev.2",name).toAbsolutePath();
        if(Files.isRegularFile(legacy)&&hash(legacy).equals(expected))return legacy;
        throw new IOException("Install the optional clip tools pack into your Minecraft folder (config/bridgetap/tools/). Missing: "+name);
    }
    private static String hash(Path file)throws IOException{
        try{MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] data=new byte[65536];try(InputStream in=Files.newInputStream(file)){int n;while((n=in.read(data))>=0)digest.update(data,0,n);}StringBuilder result=new StringBuilder();for(byte b:digest.digest())result.append(String.format(Locale.ROOT,"%02x",b&255));return result.toString();}catch(NoSuchAlgorithmException ex){throw new IOException(ex);}
    }
}
