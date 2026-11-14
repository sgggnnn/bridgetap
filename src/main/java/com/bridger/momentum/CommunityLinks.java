package com.bridger.momentum;
import java.awt.Desktop;
import java.net.URI;
import net.minecraft.unmapped.C_85740840;
public final class CommunityLinks {
 public static final String DISCORD="https://discord.gg/7gYxewvgjD";
 private CommunityLinks(){}
 public static String discord(){
  try{if(Desktop.isDesktopSupported()&&Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){Desktop.getDesktop().browse(URI.create(DISCORD));return "Discord invite opened in your browser.";}}
  catch(Exception ignored){}
  C_85740840.m_80874759(DISCORD);return "Discord invite copied: "+DISCORD;
 }
}
