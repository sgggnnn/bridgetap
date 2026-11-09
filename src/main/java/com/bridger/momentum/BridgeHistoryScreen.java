package com.bridger.momentum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_29058632;
import net.minecraft.unmapped.C_85740840;

/** Compact run-summary browser. It does not record or play back movements. */
public class BridgeHistoryScreen extends C_85740840 {
 private boolean saved;
 private int selected, page, detailOffset;
 private String message="";
 private List<RunHistory.Run> rows=Collections.emptyList();
 private static final int TOP=39, ROW=13, FOOT=20;
 private int side(){return Math.max(72,Math.min(150,f_05230747/6));}
 private int divider(){return Math.max(side()+116, Math.min(f_05230747-120, f_05230747*56/100));}
 private int listWidth(){return divider()-side()-3;}
 private int pageSize(){return Math.max(1,(f_07021018-TOP-FOOT)/ROW);}
 private boolean hit(int mx,int my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
 private void box(int x,int y,int w,int h,int color){if(w>0&&h>0)C_29058632.m_57734177(x,y,x+w,y+h,color);}
 private void text(String s,int x,int y,int c){f_11884601.m_11445413(s,x,y,c,BridgeConfig.textShadow);}
 private void clipped(String s,int x,int y,int w,int color){if(w>0)text(f_11884601.m_62332220(s,w),x,y,color);}
 private void button(String s,int x,int y,int w,boolean active){
  box(x,y,w,14,active?0xFF315A7B:0xD92A2E36);
  box(x,y,w,1,active?0xFF83B6D6:0xFF454C56);clipped(s,x+4,y+3,w-8,0xFFE1E6EB);
 }
 private void refresh(){
  try{rows=saved?BridgeTracker.history().saved():BridgeTracker.history().recent();message="";}
  catch(IOException e){rows=Collections.emptyList();message="Could not read saved runs";System.err.println("[BridgeTap] "+e);}
  if(selected>=rows.size())selected=Math.max(0,rows.size()-1);
 }
 private void switchView(boolean value){saved=value;selected=page=detailOffset=0;refresh();}
 public BridgeHistoryScreen(){refresh();}
 @Override public void m_93956703(int mx,int my,float partial){
  super.m_93956703(mx,my,partial);
  final int w=f_05230747,h=f_07021018,s=side(),split=divider(),n=pageSize();
  box(0,0,w,h,0xC80B0D12);
  box(0,0,w,16,0xEF111318);
  button("Recent",3,1,57,!saved);button("Saved",62,1,54,saved);
  clipped(saved?"Saved bridges":"Recent bridges (last 10)",122,4,w-125,0xFFC5CFD9);
  box(0,16,w,18,0xE71B1E24);
  button("+ Save",4,18,58,false);button("Settings",65,18,66,false);
  if(!message.isEmpty())clipped(message,139,21,Math.max(0,w-145),0xFFFFD07B);
  box(s+1,35,w-s-1,h-35,0xAA101319);
  box(0,35,s,h-35,0xD91B1C21);box(s,35,1,h-35,0xFF454A53);
  text("BRIDGES",6,42,0xFF89919A);
  box(3,saved?69:55,s-7,13,0xFF315C80);
  clipped("> Recent",7,57,s-12,0xFFE2EAF2);clipped("> Saved",7,71,s-12,0xFFE2EAF2);
  text("VIEW",6,98,0xFF89919A);clipped("* Settings",7,113,s-12,0xFFCCD3DA);
  box(s+1,35,split-s-2,14,0xE91F2229);
  int resultX=s+6,timeX=s+Math.max(58,listWidth()*48/100),jumpX=s+Math.max(98,listWidth()*77/100);
  text("Result",resultX,38,0xFF9EA7B0);
  if(timeX<split-35)text("Time",timeX,38,0xFF9EA7B0);
  if(jumpX<split-28)text("Jumps",jumpX,38,0xFF9EA7B0);
  box(split,35,1,h-35,0xFF454A53);
  for(int i=page*n;i<Math.min(rows.size(),(page+1)*n);i++){
   int y=50+(i-page*n)*ROW;RunHistory.Run r=rows.get(i);boolean ok="SUCCESS".equals(r.status);
   if(i==selected)box(s+2,y-2,split-s-3,ROW,0xB63D7097);
   else if((i&1)==1)box(s+2,y-2,split-s-3,ROW,0x211F2630);
   clipped(ok?"Success":"Fail",resultX,y,Math.max(0,timeX-resultX-3),ok?0xFF81E5A3:0xFFFF777B);
   if(timeX<split-35)clipped(String.format(Locale.US,"%.2fs",r.time),timeX,y,Math.max(0,jumpX-timeX-3),0xFFE4E7EB);
   if(jumpX<split-28)clipped(String.valueOf(r.jumps),jumpX+4,y,split-jumpX-6,0xFFD9E0E7);
  }
  if(rows.isEmpty())clipped(message.isEmpty()?"No saved runs yet":"Unable to load runs",s+6,53,listWidth()-12,0xFF9EA7B0);
  int dx=split+7,dw=w-dx-5;
  if(!rows.isEmpty()&&selected<rows.size()){
   RunHistory.Run r=rows.get(selected);boolean ok="SUCCESS".equals(r.status);
   clipped(String.format(Locale.US,"%.2fs  %s",r.time,ok?"Success":"Fail"),dx,40,dw,ok?0xFF82E5A2:0xFFFF777B);
   clipped(r.jumps+" jumps  \u00b7  axis "+r.axis,dx,52,dw,0xFFBEC8D2);
   clipped(new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(new Date(r.created)),dx,64,dw,0xFF8E99A5);
   box(dx,79,dw,1,0xFF3D4651);text("RUN DETAILS",dx,85,0xFF9AAAB8);
   int y=100;for(int i=detailOffset;i<r.details.size()&&y<h-FOOT;i++,y+=12)clipped(r.details.get(i),dx,y,dw,0xFFE8EDF2);
   if(r.details.size()>Math.max(0,(h-FOOT-100)/12))clipped("\u2191 / \u2193 Details",dx,h-14,dw,0xFF899AA9);
  }else clipped("Select a run",dx,41,dw,0xFF899AA9);
  box(s+1,h-FOOT,split-s-2,FOOT,0xE91B1E24);
  button("<",s+5,h-18,22,false);button(">",s+30,h-18,22,false);
  clipped((page+1)+" / "+Math.max(1,(rows.size()+n-1)/n),s+57,h-14,split-s-62,0xFFAEB9C4);
 }
 @Override protected void m_23755539(int mx,int my,int button){
  if(button!=0)return;
  int s=side(),split=divider();
  if(hit(mx,my,3,1,57,14)){switchView(false);return;}
  if(hit(mx,my,62,1,54,14)){switchView(true);return;}
  if(hit(mx,my,4,18,58,14)){
   if(saved||rows.isEmpty()||selected>=rows.size()){message=saved?"Already saved":"No run selected";return;}
   try{BridgeTracker.history().save(rows.get(selected));message="Saved";}catch(IOException e){message="Save failed";System.err.println("[BridgeTap] "+e);}return;
  }
  if(hit(mx,my,65,18,66,14)||hit(mx,my,3,110,s-7,14)){Minecraft.m_20213497().m_52715402(new BridgeSettingsScreen());return;}
  if(hit(mx,my,3,55,s-7,14)){switchView(false);return;}
  if(hit(mx,my,3,69,s-7,14)){switchView(true);return;}
  if(hit(mx,my,s+5,f_07021018-18,22,14)){page=Math.max(0,page-1);return;}
  if(hit(mx,my,s+30,f_07021018-18,22,14)){if((page+1)*pageSize()<rows.size())page++;return;}
  if(hit(mx,my,s+2,48,split-s-3,Math.max(0,f_07021018-FOOT-48))){
   int index=page*pageSize()+(my-48)/ROW;if(index<rows.size()){selected=index;detailOffset=0;}
  }
 }
 @Override protected void m_29116903(char c,int key){
  if(key==1){Minecraft.m_20213497().m_52715402(null);return;}
  if(key==200){detailOffset=Math.max(0,detailOffset-1);return;}
  if(key==208&&selected<rows.size()){detailOffset=Math.min(Math.max(0,rows.get(selected).details.size()-1),detailOffset+1);return;}
  super.m_29116903(c,key);
 }
}
