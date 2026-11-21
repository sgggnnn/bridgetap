package com.bridger.momentum;
import org.lwjgl.opengl.*;
/** Raw GL scope: restores FBOs/program/texture units plus compatibility render state. */
final class GlUiState implements AutoCloseable {
 final int program,active,draw,read;private final boolean core,ext;
 GlUiState(){
  core=GLContext.getCapabilities().OpenGL30;ext=GLContext.getCapabilities().GL_EXT_framebuffer_object;
  program=GLContext.getCapabilities().OpenGL20?GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM):0;active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
  draw=core?GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING):ext?GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT):0;
  read=core?GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING):draw;
  GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
 }
 void clean(){for(int plane=0;plane<6;plane++)GL11.glDisable(GL11.GL_CLIP_PLANE0+plane);GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_STENCIL_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_FOG);GL11.glDepthMask(false);GL11.glColorMask(true,true,true,true);}
 public void close(){if(core){GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);}else if(ext)EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,draw);GL11.glPopAttrib();if(GLContext.getCapabilities().OpenGL20)GL20.glUseProgram(program);GL13.glActiveTexture(active);}
 static void blend(){GL11.glEnable(GL11.GL_BLEND);GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE_MINUS_SRC_ALPHA);}
}
