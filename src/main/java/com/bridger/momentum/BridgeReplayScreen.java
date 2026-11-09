package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import java.util.*;

/** Isolated 3D replay viewport with a timeline and an orbit/follow camera. */
public final class BridgeReplayScreen extends C_85740840 {
 private final C_85740840 parent;
 private final ReplayData data;
 private double cursor,speed=1,zoom=18,yaw=45,pitch=30;
 private boolean playing=true,follow;
 private long last=System.nanoTime();
 private int dragX,dragY;
 private boolean orbiting,seeking;
 private final double cx,cy,cz;
 private static final double[] SPEEDS={0.25,0.5,1,2,4};
 public BridgeReplayScreen(C_85740840 parent,ReplayData data){
  this.parent=parent;this.data=data;
  ReplayData.Frame a=data.frames.get(0),b=data.frames.get(data.frames.size()-1);
  cx=(a.x+b.x)/2;cy=(a.y+b.y)/2;cz=(a.z+b.z)/2;
  zoom=Math.max(12,Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.z-b.z)*(a.z-b.z))*.65+8);
 }
 private void box(int x,int y,int w,int h,int c){C_29058632.m_57734177(x,y,x+w,y+h,c);}
 private void text(String s,int x,int y,int c){f_11884601.m_11445413(s,x,y,c,true);}
 private void button(String s,int x,int w){box(x,f_07021018-26,w,18,0xFF30465E);text(s,x+4,f_07021018-21,0xFFFFFF);}
 @Override public void m_93956703(int mx,int my,float partial){
  long now=System.nanoTime();double elapsed=Math.min(.25,(now-last)/1e9);last=now;
  if(playing){cursor=Math.min(data.frames.size()-1,cursor+elapsed*20*speed);if(cursor>=data.frames.size()-1)playing=false;}
  box(0,0,f_05230747,f_07021018,0xFF101927);
  renderWorld();
  text("BridgeTap Replay",8,8,0x55FFFF);
  text(String.format(Locale.US,"%.2fs / %.2fs",cursor/20,(data.frames.size()-1)/20.0),8,21,0xFFFFFF);
  text("Drag: orbit | Arrows: seek | Space: pause | +/-: zoom | Esc: back",8,35,0xAABBCD);
  if(data.truncated)text("Recording limit reached (partial replay)",8,48,0xFFCC55);
  int y=f_07021018-43,w=Math.max(1,f_05230747-16);
  box(8,y,w,8,0xFF324257);
  box(8,y,(int)(w*cursor/Math.max(1,data.frames.size()-1)),8,0xFF53D8E8);
  button(playing?"Pause":"Play",8,48);button("Restart",60,52);
  button(speed+"x",116,45);button(follow?"Follow":"Overview",165,64);
  button("-",233,20);button("+",257,20);button("Back",281,36);
 }
 private void renderWorld(){
  int dw=Display.getWidth(),dh=Display.getHeight();
  int vx=0,vy=(int)(60.0*dh/f_07021018),vw=dw,vh=Math.max(1,(int)((f_07021018-112.0)*dh/f_07021018));
  int i=(int)cursor,j=Math.min(data.frames.size()-1,i+1);double t=cursor-i;
  ReplayData.Frame a=data.frames.get(i),b=data.frames.get(j);
  double px=a.x+(b.x-a.x)*t,py=a.y+(b.y-a.y)*t,pz=a.z+(b.z-a.z)*t;
  GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
  GL11.glViewport(vx,vy,vw,vh);GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor(vx,vy,vw,vh);
  GL11.glDepthMask(true);GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LEQUAL);
  GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_ALPHA_TEST);
  GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();
  double distance=follow?Math.min(zoom,14):zoom,aspect=(double)vw/vh;
  GL11.glOrtho(-distance*aspect,distance*aspect,-distance,distance,-2048,2048);
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();
  GL11.glRotated(pitch,1,0,0);GL11.glRotated(yaw,0,1,0);
  double ox=follow?px:cx,oy=follow?py:cy,oz=follow?pz:cz;
  // Subtract the origin before emitting vertices to retain precision far from spawn.
  GL11.glBegin(GL11.GL_QUADS);
  Map<String,ReplayData.Block> visible=new LinkedHashMap<>();
  for(ReplayData.Block block:data.blocks)if(block.tick<=cursor)visible.put(block.x+","+block.y+","+block.z,block);
  for(ReplayData.Block block:visible.values())if(block.id!=0)cube(block.x-ox,block.y-oy,block.z-oz,1,1,1,color(block.id));
  cube(px-ox-.3,py-oy,pz-oz-.15,.6,1.35,.3,0x44BBDD);
  cube(px-ox-.25,py-oy+1.35,pz-oz-.25,.5,.5,.5,0xF1C69A);
  GL11.glEnd();
  GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopAttrib();
 }
 private static int color(int id){
  switch(id){
   case 24:case 128:case 179:return 0xDDCB8E;
   case 2:case 18:case 31:case 35:case 159:return 0x69AA55;
   case 3:case 5:case 17:return 0x987353;
   case 41:case 147:return 0xF4CE49;
   case 168:case 169:return 0x68AD9B;
   case 8:case 9:return 0x4477BB;
   default:return 0x9299A1;
  }
 }
 private static void tint(int c,double k){GL11.glColor3d(((c>>16)&255)/255.0*k,((c>>8)&255)/255.0*k,(c&255)/255.0*k);}
 private static void v(double x,double y,double z){GL11.glVertex3d(x,y,z);}
 private static void cube(double x,double y,double z,double w,double h,double d,int c){
  tint(c,1);v(x,y+h,z);v(x+w,y+h,z);v(x+w,y+h,z+d);v(x,y+h,z+d);
  tint(c,.5);v(x,y,z);v(x+w,y,z);v(x+w,y,z+d);v(x,y,z+d);
  tint(c,.75);v(x,y,z);v(x,y+h,z);v(x+w,y+h,z);v(x+w,y,z);
  tint(c,.75);v(x,y,z+d);v(x,y+h,z+d);v(x+w,y+h,z+d);v(x+w,y,z+d);
  tint(c,.6);v(x,y,z);v(x,y+h,z);v(x,y+h,z+d);v(x,y,z+d);
  tint(c,.6);v(x+w,y,z);v(x+w,y+h,z);v(x+w,y+h,z+d);v(x+w,y,z+d);
 }
 private void seek(int mx){cursor=Math.max(0,Math.min(data.frames.size()-1,(mx-8.0)/Math.max(1,f_05230747-16)*(data.frames.size()-1)));playing=false;}
 @Override protected void m_23755539(int mx,int my,int button){
  if(button!=0)return;
  if(my>=f_07021018-47&&my<=f_07021018-32){seeking=true;seek(mx);return;}
  if(my>=f_07021018-26&&my<=f_07021018-8){
   if(mx<58){if(cursor>=data.frames.size()-1)cursor=0;playing=!playing;}
   else if(mx<114){cursor=0;playing=true;}
   else if(mx<163){int n=0;while(n<SPEEDS.length&&SPEEDS[n]!=speed)n++;speed=SPEEDS[(n+1)%SPEEDS.length];}
   else if(mx<231)follow=!follow;
   else if(mx<255)zoom=Math.min(160,zoom*1.2);
   else if(mx<279)zoom=Math.max(2,zoom/1.2);
   else Minecraft.m_20213497().m_52715402(parent);
   return;
  }
  if(my>52&&my<f_07021018-60){orbiting=true;dragX=mx;dragY=my;}
 }
 @Override protected void m_52921597(int mx,int my,int button,long time){drag(mx,my);}
 private void drag(int mx,int my){
  if(seeking)seek(mx);
  if(orbiting){yaw+=(mx-dragX)*.6;pitch=Math.max(-85,Math.min(85,pitch+(my-dragY)*.5));dragX=mx;dragY=my;}
 }
 @Override protected void m_47537840(int mx,int my,int button){orbiting=seeking=false;}
 @Override protected void m_29116903(char c,int key){
  if(key==1){Minecraft.m_20213497().m_52715402(parent);return;}
  if(key==57){if(cursor>=data.frames.size()-1)cursor=0;playing=!playing;}
  if(key==203||key==205){cursor=Math.max(0,Math.min(data.frames.size()-1,cursor+(key==203?-20:20)));playing=false;}
  if(c=='+')zoom=Math.max(2,zoom/1.2);
  if(c=='-')zoom=Math.min(160,zoom*1.2);
 }
}
