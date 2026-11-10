package com.bridger.momentum;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.input.Keyboard;

public final class BridgeRenameScreen extends C_85740840 {
 private final BridgeHistoryScreen parent;
 private final RunHistory.Run run;
 private String value,message="";
 public BridgeRenameScreen(BridgeHistoryScreen parent,RunHistory.Run run){this.parent=parent;this.run=run;value=run.name();}
 private void text(String s,int x,int y,int c){f_11884601.m_11445413(s,x,y,c,true);}
 private void box(int x,int y,int w,int h,int c){C_29058632.m_57734177(x,y,x+w,y+h,c);}
 @Override public void m_41805697(){Keyboard.enableRepeatEvents(true);}
 @Override public void m_47002234(){Keyboard.enableRepeatEvents(false);}
 @Override public void m_93956703(int mx,int my,float partial){
  box(0,0,f_05230747,f_07021018,0xEE101927);int w=Math.min(420,f_05230747-24),x=(f_05230747-w)/2,y=f_07021018/2-48;
  text("Name this replay",x,y,0x55DDEE);text("64 characters - naming also saves the recording",x,y+17,0xAABBCC);
  box(x,y+34,w,22,0xFF293A50);String shown=value;while(!shown.isEmpty()&&f_11884601.m_09235706(shown+"_")>w-12)shown=shown.substring(1);
  text(shown+"_",x+5,y+41,0xFFFFFF);
  box(x,y+67,86,20,0xFF315A7B);text("Save (Enter)",x+5,y+73,0xFFFFFF);
  box(x+94,y+67,86,20,0xFF293A50);text("Cancel (Esc)",x+99,y+73,0xFFFFFF);
  text(f_11884601.m_62332220(message,w),x,y+99,0xFFCC77);
 }
 private void save(){try{BridgeTracker.history().rename(run,value);parent.refreshFor(run.id,"Name saved");Minecraft.m_20213497().m_52715402(parent);}catch(IOException|IllegalArgumentException ex){message=ex.getMessage();}}
 private void append(String chars){for(int i=0;i<chars.length()&&value.length()<64;i++){char c=chars.charAt(i);if(c>=32&&c!=127&&c!='\u00a7')value+=c;}}
 @Override protected void m_29116903(char c,int key){
  if(key==1){Minecraft.m_20213497().m_52715402(parent);return;}
  if(key==28||key==156){save();return;}
  if(key==14){if(!value.isEmpty())value=value.substring(0,value.length()-1);return;}
  if(m_34591428(key)){append(m_38845775());return;}append(String.valueOf(c));
 }
 @Override protected void m_23755539(int mx,int my,int button){
  if(button!=0)return;int w=Math.min(420,f_05230747-24),x=(f_05230747-w)/2,y=f_07021018/2+19;
  if(my>=y&&my<y+20){if(mx>=x&&mx<x+86)save();else if(mx>=x+94&&mx<x+180)Minecraft.m_20213497().m_52715402(parent);}
 }
}
