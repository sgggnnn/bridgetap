package com.bridger.momentum;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.GL11;

public final class BridgeColorsScreen extends C_85740840 {
 private float scale(){return Math.min(1f,Math.min((f_05230747-8f)/350,(f_07021018-8f)/310));}
 private void text(String s,int x,int y,int c){f_11884601.m_11445413(s,x,y,c,true);}
 @Override public void m_93956703(int mx,int my,float partial){
  C_29058632.m_57734177(0,0,f_05230747,f_07021018,0xE0101825);
  float s=scale();int x=((int)(f_05230747/s)-350)/2,y=((int)(f_07021018/s)-310)/2;
  GL11.glPushMatrix();GL11.glScalef(s,s,1);
  text("BridgeTap - Overlay colors",x+8,y+8,0x55FFFF);
  for(int i=0;i<BridgeConfig.colors.length;i++){
   int ry=y+30+i*24;
   text(BridgeConfig.COLOR_NAMES[i],x+8,ry+5,0xFFFFFF);
   for(int c=0;c<16;c++){
    String code=Integer.toHexString(c);int cx=x+140+c*13;
    if(BridgeConfig.colors[i].equals(code))C_29058632.m_57734177(cx-1,ry-1,cx+12,ry+18,0xFFFFFFFF);
    C_29058632.m_57734177(cx,ry,cx+11,ry+17,0xFF202A38);
    text("\u00a7"+code+"#",cx+2,ry+4,0xFFFFFF);
   }
  }
  text("Click a color - Esc: back",x+8,y+282,0xAABBCC);
  GL11.glPopMatrix();
 }
 @Override protected void m_23755539(int mx,int my,int button){
  if(button!=0)return;
  float s=scale();int x=((int)(f_05230747/s)-350)/2,y=((int)(f_07021018/s)-310)/2;
  int px=(int)(mx/s)-x-140,py=(int)(my/s)-y-30;
  if(px>=0&&px<208&&py>=0&&py<240&&py%24<18){BridgeConfig.colors[py/24]=Integer.toHexString(px/13);BridgeConfig.save();}
 }
 @Override protected void m_29116903(char c,int key){if(key==1)Minecraft.m_20213497().m_52715402(new BridgeSettingsScreen());}
}
