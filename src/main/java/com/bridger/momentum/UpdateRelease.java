package com.bridger.momentum;

import java.util.*;
import java.util.regex.*;

/** Strict release selection shared by the network checker and installer tests. */
public final class UpdateRelease {
 public static final long MAX_JAR=128L*1024*1024;
 public final String version,name,sha256,url;public final long size;public final boolean forge;
 public UpdateRelease(String version,String name,String sha256,long size,boolean forge){
  numbers(version);String expected="BridgeTap-"+version+(forge?"-Forge-1.8.9.jar":"-OneClient.jar");
  if(!name.equals(expected)&&!( !forge&&name.equals("BridgeTap-"+version+".jar")))throw new IllegalArgumentException("Unexpected release filename");
  if(!sha256.matches("[a-f0-9]{64}")||size<1||size>MAX_JAR)throw new IllegalArgumentException("Missing release checksum or invalid size");
  this.version=version;this.name=name;this.sha256=sha256;this.size=size;this.forge=forge;
  url="https://github.com/sgggnnn/bridgetap/releases/download/v"+version+"/"+name;
 }
 private static long[] numbers(String v){Matcher m=Pattern.compile("(0|[1-9][0-9]{0,8})\\.(0|[1-9][0-9]{0,8})\\.(0|[1-9][0-9]{0,8})(?:-(dev|alpha|beta|rc)\\.([0-9]{1,8}))?").matcher(v);if(!m.matches())throw new IllegalArgumentException("Unsupported version: "+v);return new long[]{Long.parseLong(m.group(1)),Long.parseLong(m.group(2)),Long.parseLong(m.group(3)),m.group(4)==null?4:Arrays.asList("dev","alpha","beta","rc").indexOf(m.group(4)),m.group(5)==null?0:Long.parseLong(m.group(5))};}
 public static boolean newer(String latest,String installed){long[] a=numbers(latest),b=numbers(installed);for(int i=0;i<a.length;i++)if(a[i]!=b[i])return a[i]>b[i];return false;}
}
