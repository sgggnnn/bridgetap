package com.bridger.momentum;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Searchable gallery with durable names, sorting and portable replay files. */
public class BridgeHistoryScreen extends C_85740840 {
 private static final int TOP=99,ROW=47;
 private boolean saved,searching,successOnly,favoritesOnly;
 private int selected,page,detailOffset;
 private String message="",query="";
 private ReplaySort sort=ReplaySort.parse(BridgeConfig.replaySort);
 private List<RunHistory.Run> all=Collections.emptyList(),rows=Collections.emptyList();
 private final Map<String,BridgeGeometry.Result> geometry=new HashMap<>();
 private int split(){return f_05230747*72/100;}
 private int columns(){return split()>=500?3:split()>=300?2:1;}
 private int cardWidth(){return Math.max(1,(split()-12)/columns());}
 private int pageSize(){return columns()*Math.max(1,(f_07021018-TOP-20)/ROW);}
 private boolean hit(int mx,int my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
 private void box(int x,int y,int w,int h,int c){if(w>0&&h>0)C_29058632.m_57734177(x,y,x+w,y+h,c);}
 private void text(String s,int x,int y,int c){f_11884601.m_11445413(s,x,y,c,BridgeConfig.textShadow);}
 private void clipped(String s,int x,int y,int w,int c){if(w>0)text(f_11884601.m_62332220(s,w),x,y,c);}
 private void button(String s,int x,int y,int w,boolean active){box(x,y,w,16,active?0xFF315A7B:0xE02A3443);clipped(s,x+4,y+4,w-8,0xFFE1E6EB);}
 private void refresh(){
  try{all=favoritesOnly?BridgeTracker.history().favorites():saved?BridgeTracker.history().saved():BridgeTracker.history().recent();message="";}
  catch(IOException e){all=Collections.emptyList();message="Could not read saved runs";System.err.println("[BridgeTap] "+e);}
  filter();
 }
 private void filter(){
  rows=new ArrayList<>();
  for(RunHistory.Run r:all){
   String searchable=r.name()+" "+r.id+" "+r.replayId()+" "+r.status+" "+r.axis+" "+String.join(" ",r.details);
   if((!successOnly||"SUCCESS".equals(r.status))&&searchable.toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US)))rows.add(r);
  }
  rows.sort(sort.comparator());selected=page=detailOffset=0;
 }
 private void switchView(boolean value){saved=value;favoritesOnly=false;refresh();}
 public BridgeHistoryScreen(){refresh();}
 public void refreshFor(String id,String status){
  refresh();for(int i=0;i<rows.size();i++)if(rows.get(i).id.equals(id)){selected=i;page=i/pageSize();break;}message=status;
 }
 public void imported(RunHistory.Run run){saved=true;favoritesOnly=successOnly=false;query="";refreshFor(run.id,"Imported: "+run.displayName());}
 private boolean favorite(RunHistory.Run run){try{return BridgeTracker.history().isFavorite(run);}catch(IOException ex){return false;}}
 private BridgeGeometry.Result dimensions(RunHistory.Run run){
  if(!geometry.containsKey(run.id)){
   BridgeGeometry.Result result;
   try{ReplayData data=BridgeTracker.history().replay(run);result=data==null?BridgeGeometry.Result.unavailable("Statistics only"):data.geometry();}
   catch(IOException|IllegalArgumentException ex){result=BridgeGeometry.Result.unavailable("Replay unavailable");}
   geometry.put(run.id,result);
  }
  return geometry.get(run.id);
 }
 private void play(int index){
  if(index<0||index>=rows.size())return;selected=index;
  try{
   ReplayData data=BridgeTracker.history().replay(rows.get(index));
   if(data==null){message="Old save: statistics only, no replay";return;}
   if(BridgeTracker.isBridging()){message="Finish the current attempt or /bt reset first";return;}
   ReplaySession.start(rows.get(index),data);
  }catch(IOException|IllegalArgumentException ex){message="Replay unreadable";System.err.println("[BridgeTap] "+ex);}
 }
 private int cardAt(int mx,int my){
  if(mx<8||mx>=split()-4||my<TOP||my>=f_07021018-20)return -1;
  int col=(mx-8)/cardWidth(),row=(my-TOP)/ROW,index=page*pageSize()+row*columns()+col;
  return col<columns()&&index<rows.size()&&index<(page+1)*pageSize()?index:-1;
 }
 @Override public void m_93956703(int mx,int my,float partial){
  super.m_93956703(mx,my,partial);
  int w=f_05230747,h=f_07021018,split=split(),cols=columns(),cw=cardWidth(),n=pageSize();
  box(0,0,w,h,0xE0101723);text("BridgeTap Replays",8,7,0xFF55DDEE);
  button("Recent",8,21,54,!saved&&!favoritesOnly);button("Saved",66,21,48,saved&&!favoritesOnly);
  button("+ Save",118,21,52,false);button("Settings",174,21,60,false);
  button("Favourites",w-132,2,60,favoritesOnly);button(successOnly?"Success":"All runs",w-68,2,60,successOnly);
  button("Rename",8,42,56,false);button("Sort: "+sort.label,68,42,92,false);
  button("Export",164,42,52,false);button("Import",220,42,52,false);button("Files",276,42,38,false);
  box(8,63,Math.max(1,w-16),17,searching?0xFF344D63:0xFF202C3B);
  clipped(query.isEmpty()?"Search name, replay ID, axis or jump...":query+(searching?"_":""),12,68,w-24,0xFFC9D6E3);
  clipped(message,8,84,w-16,0xFFFFCE73);
  for(int index=page*n;index<Math.min(rows.size(),(page+1)*n);index++){
   int slot=index-page*n,x=8+(slot%cols)*cw,y=TOP+(slot/cols)*ROW;
   RunHistory.Run r=rows.get(index);boolean ok="SUCCESS".equals(r.status),replay=BridgeTracker.history().hasReplay(r);
   box(x,y,cw-5,42,index==selected?0xFF30465E:0xEA202B3B);box(x,y,2,42,ok?0xFF55DDA0:0xFFEA6875);
   clipped((favorite(r)?"\u00a7e* ":"#")+r.id.substring(0,8),x+6,y+4,cw-30,0xFF91A2B6);
   clipped(r.name().isEmpty()&&!replay?"Saved summary":r.displayName(),x+6,y+15,cw-13,0xFFE7EDF4);
   clipped(String.format(Locale.US,"%s  %.3fs",ok?"Success":"Fail",r.time),x+6,y+30,cw-13,ok?0xFF91DEB0:0xFFFF999E);
   if(replay)button(">",x+cw-24,y+3,16,false);
  }
  int dx=split+5,dw=w-dx-7;box(dx-3,TOP,dw+6,h-TOP-6,0xD81D2735);
  if(rows.isEmpty())clipped("No matching runs",12,TOP+6,split-20,0xFF9DADBF);
  if(selected<rows.size()){
   RunHistory.Run r=rows.get(selected);int y=TOP+6;
   clipped("REPLAY DETAILS",dx,y,dw,0xFF9DAEC0);clipped(r.displayName(),dx,y+13,dw,0xFFFFFFFF);
   clipped(String.format(Locale.US,"%.3fs / %d jumps",r.time,r.jumps),dx,y+26,dw,0xFFE4EDF5);
   clipped("Axis: "+r.axis,dx,y+38,dw,0xFFC3D2DF);
   clipped(new SimpleDateFormat("dd.MM.yy HH:mm:ss",Locale.US).format(new Date(r.created)),dx,y+50,dw,0xFF8E9DAF);
   BridgeGeometry.Result bridge=dimensions(r);
   clipped(bridge.available?String.format(Locale.US,"Gap (edge): %.2f blocks",bridge.gap):"Gap (edge): N/A",dx,y+65,dw,0xFF80DED5);
   clipped(bridge.available?String.format(Locale.US,"Height delta: %+.2f blocks",bridge.rise):"Height delta: N/A",dx,y+77,dw,0xFF80DED5);
   clipped(bridge.available?"Card > to watch":bridge.reason,dx,y+91,dw,0xFF9DAEC0);
   int lineY=y+108;for(int i=detailOffset;i<r.details.size()&&lineY<h-20;i++,lineY+=12)clipped(r.details.get(i),dx,lineY,dw,0xFFE8EDF2);
  }
  button("< Prev",8,h-18,50,false);button("Next >",62,h-18,50,false);
  text((page+1)+" / "+Math.max(1,(rows.size()+n-1)/n),120,h-13,0xFFAEB9C4);
 }
 @Override protected void m_23755539(int mx,int my,int button){
  int index=cardAt(mx,my);
  if(button==1){
   if(index>=0){selected=index;try{String status=BridgeTracker.history().toggleFavorite(rows.get(index))?"Added to favourites and saved":"Removed from favourites";if(favoritesOnly)refreshFor(rows.get(index).id,status);else message=status;}catch(IOException ex){message="Could not update favourite";}}
   return;
  }
  if(button!=0)return;
  searching=hit(mx,my,8,63,f_05230747-16,17);if(searching)return;
  if(hit(mx,my,8,21,54,16)){switchView(false);return;}
  if(hit(mx,my,66,21,48,16)){switchView(true);return;}
  if(hit(mx,my,118,21,52,16)){
   if(rows.isEmpty()){message="No run selected";return;}
   try{BridgeTracker.history().save(rows.get(selected));message="Saved";}catch(IOException ex){message="Save failed";}return;
  }
  if(hit(mx,my,174,21,60,16)){Minecraft.m_20213497().m_52715402(new BridgeSettingsScreen());return;}
  if(hit(mx,my,8,42,56,16)){if(rows.isEmpty())message="Select a replay first";else Minecraft.m_20213497().m_52715402(new BridgeRenameScreen(this,rows.get(selected)));return;}
  if(hit(mx,my,68,42,92,16)){sort=sort.next();BridgeConfig.replaySort=sort.name();BridgeConfig.save();filter();return;}
  if(hit(mx,my,164,42,52,16)){
   if(rows.isEmpty()){message="Select a replay first";return;}
   try{Path file=BridgeTracker.history().exportReplay(rows.get(selected));message="Exported: "+file.getFileName()+" (Files opens exports)";}
   catch(IOException ex){message="Export failed: "+ex.getMessage();}return;
  }
  if(hit(mx,my,220,42,52,16)){Minecraft.m_20213497().m_52715402(new BridgeImportScreen(this));return;}
  if(hit(mx,my,276,42,38,16)){try{message=BridgeFileActions.open(BridgeTracker.history().exportsFolder());}catch(IOException|RuntimeException ex){message="Could not open exports folder";}return;}
  if(hit(mx,my,f_05230747-132,2,60,16)){favoritesOnly=!favoritesOnly;refresh();return;}
  if(hit(mx,my,f_05230747-68,2,60,16)){successOnly=!successOnly;filter();return;}
  if(hit(mx,my,8,f_07021018-18,50,16)){page=Math.max(0,page-1);return;}
  if(hit(mx,my,62,f_07021018-18,50,16)){if((page+1)*pageSize()<rows.size())page++;return;}
  if(index>=0){selected=index;detailOffset=0;if((mx-8)%cardWidth()>=cardWidth()-24&&(my-TOP)%ROW<20)play(index);}
 }
 @Override protected void m_29116903(char c,int key){
  if(key==1){if(searching){searching=false;return;}Minecraft.m_20213497().m_52715402(null);return;}
  if(searching){if(key==14&&!query.isEmpty())query=query.substring(0,query.length()-1);else if(c>=32&&c!=127&&query.length()<64)query+=c;filter();return;}
  if(key==200)detailOffset=Math.max(0,detailOffset-1);
  if(key==208&&selected<rows.size())detailOffset=Math.min(Math.max(0,rows.get(selected).details.size()-1),detailOffset+1);
 }
}
