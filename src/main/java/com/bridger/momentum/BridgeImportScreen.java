package com.bridger.momentum;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

public final class BridgeImportScreen extends C_85740840 {
 private final BridgeHistoryScreen parent;
 private List<Path> files=new ArrayList<>();private int page;private String message="Copy .btreplay files into imports, then Refresh";
 public BridgeImportScreen(BridgeHistoryScreen parent){this.parent=parent;refresh();}
 private int pageSize(){return Math.max(1,(f_07021018-91)/20);}
 private void refresh(){try{
  Path folder=BridgeTracker.history().importsFolder();Files.createDirectories(folder);files=new ArrayList<>();
  try(DirectoryStream<Path> entries=Files.newDirectoryStream(folder,"*.btreplay")){for(Path file:entries)if(Files.isRegularFile(file))files.add(file);}
  files.sort(Comparator.comparing(p->p.getFileName().toString(),String.CASE_INSENSITIVE_ORDER));page=0;
 }catch(IOException ex){message="Could not list imports folder";}}
 private void text(String s,int x,int y,int c){f_11884601.m_11445413(s,x,y,c,true);}
 private void box(int x,int y,int w,int h,int c){C_29058632.m_57734177(x,y,x+w,y+h,c);}
 private void button(String label,int x,int y,int w){box(x,y,w,17,0xFF2A4058);text(label,x+4,y+4,0xFFFFFF);}
 @Override public void m_93956703(int mx,int my,float partial){
  box(0,0,f_05230747,f_07021018,0xEE101927);text("Import a friend's replay",8,8,0x55DDEE);
  button("Open folder",8,27,82);button("Refresh",94,27,62);button("Copy path",160,27,72);button("Back",236,27,60);
  text(f_11884601.m_62332220(message,f_05230747-16),8,50,0xFFCC77);
  int n=pageSize();for(int i=page*n;i<Math.min(files.size(),(page+1)*n);i++){
   int y=66+(i-page*n)*20;box(8,y,f_05230747-16,18,0xFF243449);
   text(f_11884601.m_62332220("Import > "+files.get(i).getFileName(),f_05230747-24),12,y+5,0xFFFFFF);
  }
  if(files.isEmpty())text("No .btreplay files in imports",8,71,0xAABBCC);
  button("< Prev",8,f_07021018-20,58);button("Next >",70,f_07021018-20,58);
  text((page+1)+" / "+Math.max(1,(files.size()+n-1)/n),138,f_07021018-15,0xAABBCC);
 }
 @Override protected void m_23755539(int mx,int my,int button){
  if(button!=0)return;
  if(my>=27&&my<44){
   if(mx>=8&&mx<90){try{message=BridgeFileActions.open(BridgeTracker.history().importsFolder());}catch(IOException|RuntimeException ex){message="Could not open folder - use Copy path";}}
   else if(mx>=94&&mx<156)refresh();
   else if(mx>=160&&mx<232){m_80874759(BridgeTracker.history().importsFolder().toAbsolutePath().toString());message="Imports path copied";}
   else if(mx>=236&&mx<296)Minecraft.m_20213497().m_52715402(parent);
   return;
  }
  if(my>=f_07021018-20){if(mx>=8&&mx<66)page=Math.max(0,page-1);else if(mx>=70&&mx<128&&(page+1)*pageSize()<files.size())page++;return;}
  if(mx>=8&&mx<f_05230747-8&&my>=66){int index=page*pageSize()+(my-66)/20;
   if(index<files.size()&&index<(page+1)*pageSize())try{RunHistory.Run run=BridgeTracker.history().importReplay(files.get(index));parent.imported(run);Minecraft.m_20213497().m_52715402(parent);}catch(IOException ex){message="Import failed: "+ex.getMessage();}
  }
 }
 @Override protected void m_29116903(char c,int key){if(key==1)Minecraft.m_20213497().m_52715402(parent);}
}
