package com.bridger.momentum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Replay gallery and compatible browser for older summary-only saves. */
public class BridgeHistoryScreen extends C_85740840 {
 private boolean saved,searching,successOnly,favoritesOnly;
 private int selected,page,detailOffset;
 private String message="",query="";
 private List<RunHistory.Run> all=Collections.emptyList(),rows=Collections.emptyList();
 private int split(){return f_05230747*72/100;}
 private int columns(){return split()>=500?3:split()>=300?2:1;}
 private int cardWidth(){return (split()-12)/columns();}
 private int pageSize(){return columns()*Math.max(1,(f_07021018-98)/47);}
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
   String searchable=r.id+" "+r.replayId()+" "+r.status+" "+r.axis+" "+String.join(" ",r.details);
   if((!successOnly||"SUCCESS".equals(r.status))&&searchable.toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US)))rows.add(r);
  }
  selected=page=detailOffset=0;
 }
 private void switchView(boolean value){saved=value;favoritesOnly=false;refresh();}
 public BridgeHistoryScreen(){refresh();}
 private boolean favorite(RunHistory.Run run){try{return BridgeTracker.history().isFavorite(run);}catch(IOException ex){return false;}}
 private void play(int index){
  if(index<0||index>=rows.size())return;
  selected=index;
  try{
   ReplayData data=BridgeTracker.history().replay(rows.get(index));
   if(data==null){message="Old save: statistics only, no replay";return;}
   if(BridgeTracker.isBridging()){message="Finish the current attempt or /bt reset first";return;}
   ReplaySession.start(rows.get(index),data);
  }catch(IOException|IllegalArgumentException ex){message="Replay unreadable";System.err.println("[BridgeTap] "+ex);}
 }
 @Override public void m_93956703(int mx,int my,float partial){
  super.m_93956703(mx,my,partial);
  int w=f_05230747,h=f_07021018,split=split(),cols=columns(),cw=cardWidth(),n=pageSize();
  box(0,0,w,h,0xE0101723);text("BridgeTap Replays",8,7,0xFF55DDEE);
  button("Recent",8,21,54,!saved&&!favoritesOnly);button("Saved",66,21,48,saved&&!favoritesOnly);
  button("+ Save",118,21,52,false);button("Settings",174,21,60,false);
  button("Play >",238,21,50,false);
  button("Favourites",w-132,2,60,favoritesOnly);
  button(successOnly?"Success":"All runs",w-68,2,60,successOnly);
  box(8,42,Math.max(1,w-16),17,searching?0xFF344D63:0xFF202C3B);
  clipped(query.isEmpty()?"Search replay ID, axis or jump...":query+(searching?"_":""),12,47,w-24,0xFFC9D6E3);
  clipped(message,8,63,w-16,0xFFFFCE73);
  for(int index=page*n;index<Math.min(rows.size(),(page+1)*n);index++){
   int slot=index-page*n,x=8+(slot%cols)*cw,y=78+(slot/cols)*47;
   RunHistory.Run r=rows.get(index);boolean ok="SUCCESS".equals(r.status),replay=BridgeTracker.history().hasReplay(r);
   box(x,y,cw-5,42,index==selected?0xFF30465E:0xEA202B3B);box(x,y,2,42,ok?0xFF55DDA0:0xFFEA6875);
   clipped((favorite(r)?"\u00a7e* ":"#")+r.id.substring(0,Math.min(8,r.id.length())),x+6,y+4,cw-30,0xFF91A2B6);
   clipped(replay?"Bridge replay":"Saved summary",x+6,y+15,cw-13,0xFFE7EDF4);
   clipped(String.format(Locale.US,"%s  %.2fs",ok?"Success":"Fail",r.time),x+6,y+30,cw-13,ok?0xFF91DEB0:0xFFFF999E);
   if(replay)button(">",x+cw-24,y+3,16,false);
  }
  int dx=split+5,dw=w-dx-7;box(dx-3,78,dw+6,h-84,0xD81D2735);
  if(rows.isEmpty())clipped("No matching runs",12,84,split-20,0xFF9DADBF);
  if(selected<rows.size()){
   RunHistory.Run r=rows.get(selected);
   clipped("REPLAY DETAILS",dx,84,dw,0xFF9DAEC0);
   clipped(String.format(Locale.US,"%.2fs / %d jumps",r.time,r.jumps),dx,99,dw,0xFFE4EDF5);
   clipped("Axis: "+r.axis,dx,111,dw,0xFFC3D2DF);
   clipped(new SimpleDateFormat("dd.MM.yy HH:mm",Locale.US).format(new Date(r.created)),dx,123,dw,0xFF8E9DAF);
   clipped(BridgeTracker.history().hasReplay(r)?"Play > to watch":"Statistics only",dx,139,dw,0xFF75D6DF);
   int y=157;for(int i=detailOffset;i<r.details.size()&&y<h-20;i++,y+=12)clipped(r.details.get(i),dx,y,dw,0xFFE8EDF2);
  }
  button("< Prev",8,h-18,50,false);button("Next >",62,h-18,50,false);
  text((page+1)+" / "+Math.max(1,(rows.size()+n-1)/n),120,h-13,0xFFAEB9C4);
 }
 @Override protected void m_23755539(int mx,int my,int button){
  if(button==1){
   if(mx>=8&&mx<split()-4&&my>=78&&my<f_07021018-20){
    int col=(mx-8)/cardWidth(),row=(my-78)/47,index=page*pageSize()+row*columns()+col;
    if(col<columns()&&index<rows.size()&&index<(page+1)*pageSize()){
     selected=index;
     try{
      message=BridgeTracker.history().toggleFavorite(rows.get(index))?"Added to favourites and saved":"Removed from favourites";
      if(favoritesOnly){all=BridgeTracker.history().favorites();filter();}
     }
     catch(IOException ex){message="Could not update favourite";}
    }
   }
   return;
  }
  if(button!=0)return;
  searching=hit(mx,my,8,42,f_05230747-16,17);if(searching)return;
  if(hit(mx,my,8,21,54,16)){switchView(false);return;}
  if(hit(mx,my,66,21,48,16)){switchView(true);return;}
  if(hit(mx,my,118,21,52,16)){
   if(saved||rows.isEmpty()){message=saved?"Already saved":"No run selected";return;}
   try{RunHistory.Run run=rows.get(selected);BridgeTracker.history().save(run);message=BridgeTracker.history().hasReplay(run)?"Saved with replay":"Summary saved";}catch(IOException e){message="Save failed";System.err.println("[BridgeTap] "+e);}return;
  }
  if(hit(mx,my,174,21,60,16)){Minecraft.m_20213497().m_52715402(new BridgeSettingsScreen());return;}
  if(hit(mx,my,238,21,50,16)){play(selected);return;}
  if(hit(mx,my,f_05230747-132,2,60,16)){favoritesOnly=!favoritesOnly;refresh();return;}
  if(hit(mx,my,f_05230747-68,2,60,16)){successOnly=!successOnly;filter();return;}
  if(hit(mx,my,8,f_07021018-18,50,16)){page=Math.max(0,page-1);return;}
  if(hit(mx,my,62,f_07021018-18,50,16)){if((page+1)*pageSize()<rows.size())page++;return;}
  if(mx>=8&&mx<split()-4&&my>=78&&my<f_07021018-20){
   int col=(mx-8)/cardWidth(),row=(my-78)/47,index=page*pageSize()+row*columns()+col;
   if(col<columns()&&index<rows.size()&&index<(page+1)*pageSize()){
    selected=index;detailOffset=0;
    if((mx-8)%cardWidth()>=cardWidth()-24&&(my-78)%47<20)play(index);
   }
  }
 }
 @Override protected void m_29116903(char c,int key){
  if(key==1){if(searching){searching=false;return;}Minecraft.m_20213497().m_52715402(null);return;}
  if(searching){
   if(key==14&&!query.isEmpty())query=query.substring(0,query.length()-1);
   else if(c>=32&&c!=127&&query.length()<64)query+=c;
   filter();return;
  }
  if(key==200)detailOffset=Math.max(0,detailOffset-1);
  if(key==208&&selected<rows.size())detailOffset=Math.min(Math.max(0,rows.get(selected).details.size()-1),detailOffset+1);
 }
}
