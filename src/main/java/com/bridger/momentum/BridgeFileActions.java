package com.bridger.momentum;
import java.nio.file.*;
import java.awt.Desktop;
import java.io.IOException;
import net.minecraft.unmapped.C_85740840;

public final class BridgeFileActions {
 public static String open(Path folder)throws IOException{
  Files.createDirectories(folder);
  if(Desktop.isDesktopSupported()&&Desktop.getDesktop().isSupported(Desktop.Action.OPEN)){Desktop.getDesktop().open(folder.toFile());return "Folder opened";}
  C_85740840.m_80874759(folder.toAbsolutePath().toString());return "Folder path copied";
 }
}
