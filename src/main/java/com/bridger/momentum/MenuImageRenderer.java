package com.bridger.momentum;
import java.awt.image.BufferedImage;import java.nio.*;import java.util.*;import org.lwjgl.BufferUtils;import org.lwjgl.opengl.*;
/** Small bounded preview texture cache. Uploads only when the worker supplies a new thumbnail. */
final class MenuImageRenderer {
 private static final LinkedHashMap<BufferedImage,Integer> textures=new LinkedHashMap<>(8,.75f,true);
 static void draw(BufferedImage image,double x,double y,double w,double h,int tint){draw(image,x,y,w,h,tint,false);}
 static void draw(BufferedImage image,double x,double y,double w,double h,int tint,boolean sharp){
  if(image==null)return;GlPrograms.Program shader=GlPrograms.get("image",false);if(shader==null)return;
  try(GlUiState state=new GlUiState()){
   state.clean();GL13.glActiveTexture(GL13.GL_TEXTURE0);Integer texture=textures.get(image);
   if(texture==null){
    if(image.getWidth()>512||image.getHeight()>256)return;
    if(textures.size()>=4){Iterator<Integer> old=textures.values().iterator();GL11.glDeleteTextures(old.next());old.remove();}
    texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
    ByteBuffer pixels=BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4);for(int py=0;py<image.getHeight();py++)for(int px=0;px<image.getWidth();px++){int c=image.getRGB(px,py);pixels.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>>24));}pixels.flip();int alignment=GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,1);try{GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,image.getWidth(),image.getHeight(),0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);}finally{GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,alignment);}textures.put(image,texture);
   }else GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
   GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,sharp?GL11.GL_NEAREST:GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,sharp?GL11.GL_NEAREST:GL11.GL_LINEAR);
   GL20.glUseProgram(shader.id);shader.i("image",0);shader.color("color",tint);GlUiState.blend();GlPrograms.quad(x,y,w,h);
  }
 }
 private MenuImageRenderer(){}
}
