import com.bridger.momentum.*;
import java.nio.file.*;
import java.io.*;
public final class NativeClipToolsRegression {
 public static void main(String[] args)throws Exception{
  if(!NativeClipTools.windows()){System.out.println("SKIP: Windows clip tools installation");return;}
  Path file=NativeClipTools.tool("ffmpeg.exe"),backup=file.resolveSibling("ffmpeg-test-backup.bin");Files.move(file,backup);
  try{
   try{NativeClipTools.tool("ffmpeg.exe");throw new AssertionError("Missing executable was silently recreated");}catch(IOException expected){if(!expected.getMessage().contains("Install the optional"))throw expected;}
   if(Files.exists(file))throw new AssertionError("Runtime extracted executable");
   Files.write(file,new byte[]{1,2,3});
   try{NativeClipTools.tool("ffmpeg.exe");throw new AssertionError("Modified executable accepted");}catch(IOException expected){if(!expected.getMessage().contains("checksum mismatch"))throw expected;}
  }finally{Files.deleteIfExists(file);Files.move(backup,file);}
  if(!NativeClipTools.tool("ffmpeg.exe").equals(file))throw new AssertionError("Installed tool path changed");
  System.out.println("PASS: explicit clip tools installation, no executable extraction and rejected modified tool");
 }
}
