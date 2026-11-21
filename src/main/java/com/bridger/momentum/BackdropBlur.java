package com.bridger.momentum;
import java.nio.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Dynamic world-backdrop blur. Own color-only FBOs, never replaces game/shader-pack targets. */
public final class BackdropBlur {
 private static final class Target {
  int fbo,texture,width,height;
  void ensure(int w,int h){if(width==w&&height==h&&fbo!=0)return;delete();width=w;height=h;texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,w,h,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,(ByteBuffer)null);
   if(core()){fbo=GL30.glGenFramebuffers();GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,fbo);GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,GL30.GL_COLOR_ATTACHMENT0,GL11.GL_TEXTURE_2D,texture,0);}else{fbo=EXTFramebufferObject.glGenFramebuffersEXT();EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,fbo);EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,GL11.GL_TEXTURE_2D,texture,0);}GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
   int status=core()?GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER):EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);if(status!=GL30.GL_FRAMEBUFFER_COMPLETE)throw new IllegalStateException("Incomplete blur target");allocations++;
  }
  void delete(){if(texture!=0)GL11.glDeleteTextures(texture);if(fbo!=0){if(core())GL30.glDeleteFramebuffers(fbo);else EXTFramebufferObject.glDeleteFramebuffersEXT(fbo);}texture=fbo=width=height=0;}
 }
 private static final Target capture=new Target(),ping=new Target(),pong=new Target();private static boolean failed;private static String status="Blur ready";private static int allocations;
 private static boolean core(){return GLContext.getCapabilities().OpenGL30;}
 private static void bind(int id){if(core())GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,id);else EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,id);}
 public static String status(){return status;}
 static int allocations(){return allocations;}
 static void release(){capture.delete();ping.delete();pong.delete();}
 public static boolean render(ThemeManager.Settings settings,float opening){return render(settings,opening,0,0,0,0,0);}
 public static boolean render(ThemeManager.Settings settings,float opening,float left,float bottom,float panelWidth,float panelHeight,float radius){
  if(!settings.blurEnabled()||opening<=0)return false;
  if(failed||!GLContext.getCapabilities().OpenGL20||(!core()&&!GLContext.getCapabilities().GL_EXT_framebuffer_object)){status="Blur unavailable; tinted fallback";return false;}
  GlPrograms.Program blur=GlPrograms.get("kawase",true),composite=GlPrograms.get("composite",true);if(blur==null||composite==null){failed=true;status="Blur unavailable; shader fallback";return false;}
  IntBuffer viewport=BufferUtils.createIntBuffer(16);GL11.glGetInteger(GL11.GL_VIEWPORT,viewport);int vx=viewport.get(0),vy=viewport.get(1),width=viewport.get(2),height=viewport.get(3);
  if(width<1||height<1||width>8192||height>8192||(long)width*height>16777216){status="Blur unavailable at this framebuffer size";return false;}
  double factor=Math.min(.5,Math.min(1280.0/width,720.0/height));int w=Math.max(1,(int)Math.ceil(width*factor)),h=Math.max(1,(int)Math.ceil(height*factor));
  try(GlUiState scope=new GlUiState()){
   scope.clean();GL13.glActiveTexture(GL13.GL_TEXTURE0);capture.ensure(width,height);ping.ensure(w,h);pong.ensure(w,h);
   bind(scope.draw);int sourceRead=GL11.glGetInteger(GL11.GL_READ_BUFFER),drawBuffer=GL11.glGetInteger(GL11.GL_DRAW_BUFFER);if(drawBuffer==GL11.GL_NONE){status="No readable color target; tinted fallback";return false;}GL11.glReadBuffer(drawBuffer);
   try{
    if(GL11.glGetInteger(GL13.GL_SAMPLES)>1){if(!core()){status="Multisample blur unavailable; tinted fallback";return false;}GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,scope.draw);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,capture.fbo);GL30.glBlitFramebuffer(vx,vy,vx+width,vy+height,0,0,width,height,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER,scope.draw);}
    else{GL11.glBindTexture(GL11.GL_TEXTURE_2D,capture.texture);GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,vx,vy,width,height);}
   }finally{bind(scope.draw);GL11.glReadBuffer(sourceRead);}
   GL11.glDisable(GL11.GL_BLEND);GL20.glUseProgram(blur.id);blur.i("image",0);
   int source=capture.texture,sourceW=width,sourceH=height;float strength=Math.max(.25f,settings.effectiveBlur()*(float)factor/4.6f);
   for(int pass=0;pass<4;pass++){Target target=pass%2==0?ping:pong;bind(target.fbo);GL11.glViewport(0,0,w,h);GL11.glBindTexture(GL11.GL_TEXTURE_2D,source);blur.vec("texel",pass%2==0?1f/sourceW:0,pass%2==0?0:1f/sourceH);blur.f("offset",strength);GlPrograms.fullscreen();source=target.texture;sourceW=w;sourceH=h;}
   bind(scope.draw);GL11.glViewport(vx,vy,width,height);GL20.glUseProgram(composite.id);GL11.glBindTexture(GL11.GL_TEXTURE_2D,source);composite.i("image",0);composite.vec("panelOrigin",left,bottom);composite.vec("panelSize",panelWidth,panelHeight);composite.f("radius",radius);composite.f("opacity",Math.max(0,Math.min(1,opening)));GlUiState.blend();GlPrograms.fullscreen();status="Gaussian blur active";return true;
  }catch(RuntimeException unavailable){failed=true;status="Blur unavailable; tinted fallback";return false;}
 }
 private BackdropBlur(){}
}
