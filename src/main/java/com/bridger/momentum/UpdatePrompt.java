package com.bridger.momentum;

/** Keep a startup notice pending across menus and quick server connections. */
public final class UpdatePrompt {
 private boolean waiting;private long idleSince;
 public boolean ready(boolean pending,boolean menu,boolean idle,long now){
  if(!pending){waiting=false;return false;}
  if(menu){waiting=false;return true;}
  if(!idle){waiting=false;return false;}
  if(!waiting){waiting=true;idleSince=now;return false;}
  return now-idleSince>=2000000000L;
 }
 public static boolean customTitle(String name){String type=name.toLowerCase(java.util.Locale.ROOT);return (type.startsWith("org.polyfrost.")||type.startsWith("dev.polyfrost."))&&(type.contains("mainmenu")||type.contains("titlescreen"));}
}
