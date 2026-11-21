package com.bridger.momentum;
import org.lwjgl.opengl.*;

/** Anti-aliased signed-distance glass, in logical UI coordinates. */
final class GlassRenderer {
 static boolean rounded(double x,double y,double w,double h,double radius,int fill,int border,float reflection){
  if(w<=0||h<=0)return true;GlPrograms.Program shader=GlPrograms.get("rounded",false);if(shader==null)return false;
  int previous=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT);
  try{GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GlUiState.blend();GL20.glUseProgram(shader.id);shader.vec("size",(float)w,(float)h);shader.f("radius",(float)Math.max(0,Math.min(radius,Math.min(w,h)/2)));shader.f("stroke",(border>>>24)==0?0:1);shader.f("reflection",reflection);shader.color("fill",fill);shader.color("border",border);GlPrograms.quad(x,y,w,h);return true;
  }finally{GL20.glUseProgram(previous);GL11.glPopAttrib();}
 }
 static void polygon(double[] px,double[] py,int color){
  GlPrograms.Program shader=GlPrograms.get("solid",false);int old=GLContext.getCapabilities().OpenGL20?GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM):0;GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_TEXTURE_BIT);
  try{GlUiState.blend();GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);if(shader!=null){GL20.glUseProgram(shader.id);shader.color("color",color);}else{if(GLContext.getCapabilities().OpenGL20)GL20.glUseProgram(0);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,(color>>>24)/255f);}GL11.glBegin(GL11.GL_POLYGON);for(int i=0;i<px.length;i++)GL11.glVertex2d(px[i],py[i]);GL11.glEnd();}
  finally{if(GLContext.getCapabilities().OpenGL20)GL20.glUseProgram(old);GL11.glPopAttrib();}
 }
 private GlassRenderer(){}
}
