package com.bridger.momentum;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Portable data-only archive; entries are bounded and never extracted by their names. */
public final class ReplayArchive {
 public static final class Content {
  public final Properties metadata;public final ReplayData replay;
  private Content(Properties metadata,ReplayData replay){this.metadata=metadata;this.replay=replay;}
 }
 public static void write(Path destination,Properties metadata,ReplayData replay)throws IOException{
  Path temp=Files.createTempDirectory("bridgetap-export-"),recording=temp.resolve("recording.btr");boolean complete=false,created=false;
  try{
   replay.write(recording);Properties manifest=new Properties();manifest.putAll(metadata);manifest.setProperty("format","BridgeTap-replay-1");
   try(OutputStream target=Files.newOutputStream(destination,StandardOpenOption.CREATE_NEW)){
    created=true;
    try(ZipOutputStream zip=new ZipOutputStream(target)){
    zip.putNextEntry(new ZipEntry("metadata.properties"));manifest.store(zip,"BridgeTap portable replay");zip.closeEntry();
    zip.putNextEntry(new ZipEntry("recording.btr"));Files.copy(recording,zip);zip.closeEntry();
    }
   }
   complete=true;
  }finally{Files.deleteIfExists(recording);Files.deleteIfExists(temp);if(created&&!complete)Files.deleteIfExists(destination);}
 }
 private static void copy(InputStream in,OutputStream out,int limit)throws IOException{
  byte[] buffer=new byte[8192];int size=0,n;while((n=in.read(buffer))!=-1){size+=n;if(size>limit)throw new IOException("Replay archive entry exceeds limit");out.write(buffer,0,n);}
 }
 public static Content read(Path archive)throws IOException{
  if(Files.size(archive)>10*1024*1024)throw new IOException("Replay archive exceeds limit");
  Path temp=Files.createTempFile("bridgetap-import-",".btr");
  try(ZipFile zip=new ZipFile(archive.toFile())){
   Set<String> entries=new HashSet<>();Enumeration<? extends ZipEntry> all=zip.entries();
   while(all.hasMoreElements()){
    ZipEntry entry=all.nextElement();String name=entry.getName();
    if(entry.isDirectory()||(!name.equals("metadata.properties")&&!name.equals("recording.btr"))||!entries.add(name))throw new IOException("Unexpected archive entry");
   }
   if(entries.size()!=2)throw new IOException("Incomplete replay archive");
   Properties metadata=new Properties();ByteArrayOutputStream bytes=new ByteArrayOutputStream();
   try(InputStream in=zip.getInputStream(zip.getEntry("metadata.properties"))){copy(in,bytes,1024*1024);}
   metadata.load(new ByteArrayInputStream(bytes.toByteArray()));
   if(!"BridgeTap-replay-1".equals(metadata.getProperty("format")))throw new IOException("Unsupported archive format");
   try(InputStream in=zip.getInputStream(zip.getEntry("recording.btr"));OutputStream out=Files.newOutputStream(temp)){copy(in,out,8*1024*1024);}
   return new Content(metadata,ReplayData.read(temp));
  }catch(IllegalArgumentException ex){throw new IOException("Invalid replay archive",ex);}
  finally{Files.deleteIfExists(temp);}
 }
}
