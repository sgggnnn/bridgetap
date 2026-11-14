package com.bridger.momentum;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** ZIP/GZIP portable replay + SHA-256, encoded as one bounded, offline text code. */
public final class ReplayShareCode {
 public static final String PREFIX="BTR1:";
 public static final int MAX_CHARS=1900;
 private ReplayShareCode(){}
 private static String hash(byte[] bytes)throws IOException{
  try{byte[] digest=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder s=new StringBuilder();for(byte b:digest)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
  catch(NoSuchAlgorithmException ex){throw new IOException("SHA-256 unavailable");}
 }
 public static String encode(byte[] archive)throws IOException{
  long length=PREFIX.length()+65+(archive.length*4L+2)/3;
  if(length>MAX_CHARS)throw new IOException("Full replay needs "+length+" characters (limit "+MAX_CHARS+"). Export a file or use optional Discord sharing.");
  // Check that callers cannot package arbitrary data as a share code.
  RunHistory.previewImport(ReplayArchive.read(archive));
  return PREFIX+hash(archive)+":"+Base64.getUrlEncoder().withoutPadding().encodeToString(archive);
 }
 public static ReplayArchive.Content decode(String text)throws IOException{
  if(text==null||text.length()>MAX_CHARS+32)throw new IOException("Share code is too long (limit 1900 characters)");
  String code=text.trim();if(code.startsWith("```")){int newline=code.indexOf('\n');if(newline<0||!code.endsWith("```"))throw new IOException("Incomplete share code");code=code.substring(newline+1,code.length()-3).trim();}
  if(code.length()>MAX_CHARS||!code.startsWith(PREFIX))throw new IOException("Copy a complete BTR1: replay code first");
  int split=code.indexOf(':',PREFIX.length());
  if(split!=PREFIX.length()+64||!code.substring(PREFIX.length(),split).matches("[0-9a-f]{64}"))throw new IOException("Invalid share code checksum");
  byte[] bytes;String payload=code.substring(split+1);
  try{bytes=Base64.getUrlDecoder().decode(payload);}catch(IllegalArgumentException ex){throw new IOException("Share code is damaged or incomplete");}
  if(!Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).equals(payload)||!MessageDigest.isEqual(hash(bytes).getBytes(StandardCharsets.US_ASCII),code.substring(PREFIX.length(),split).getBytes(StandardCharsets.US_ASCII)))throw new IOException("Share code checksum failed. Copy the complete code again.");
  ReplayArchive.Content content=ReplayArchive.read(bytes);RunHistory.previewImport(content);return content;
 }
}
