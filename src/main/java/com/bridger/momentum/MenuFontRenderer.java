package com.bridger.momentum;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Tight per-glyph UVs plus transparent padding eliminate atlas-row bleed. */
final class MenuFontRenderer {
    private static final LinkedHashMap<Integer,Integer> TEXTURES=new LinkedHashMap<>(4,.75f,true);
    static void draw(String value,int x,int y,int color){draw(value,x,y,color,1);}
    private static int texture(MenuTypography.Atlas atlas){
        Integer existing=TEXTURES.get(atlas.density+(atlas.bold?4:0));if(existing!=null&&GL11.glIsTexture(existing))return existing;
        BufferedImage image=atlas.image;ByteBuffer pixels=BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4);
        for(int py=0;py<image.getHeight();py++)for(int px=0;px<image.getWidth();px++){int c=image.getRGB(px,py);pixels.put((byte)255).put((byte)255).put((byte)255).put((byte)(c>>>24));}pixels.flip();
        int texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,image.getWidth(),image.getHeight(),0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
        TEXTURES.put(atlas.density+(atlas.bold?4:0),texture);if(TEXTURES.size()>8){Iterator<Map.Entry<Integer,Integer>> it=TEXTURES.entrySet().iterator();Map.Entry<Integer,Integer> old=it.next();GL11.glDeleteTextures(old.getValue());it.remove();}return texture;
    }
    static void draw(String value,int x,int y,int color,float density){draw(value,x,y,color,density,false);}
    static void draw(String value,int x,int y,int color,float density,boolean bold){
        int scale=Math.max(1,Math.min(4,(int)Math.ceil(density))),max=GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);MenuTypography.Atlas atlas=(bold?MenuTypography.boldAtlas(scale):MenuTypography.atlas(scale));while(atlas.image.getWidth()>max&&scale>1){--scale;atlas=bold?MenuTypography.boldAtlas(scale):MenuTypography.atlas(scale);}
        GlPrograms.Program shader=GlPrograms.get("font",false);int previous=GLContext.getCapabilities().OpenGL20?GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM):0,active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_TEXTURE_BIT|GL11.GL_CURRENT_BIT|GL11.GL_PIXEL_MODE_BIT);
        try{
            GL13.glActiveTexture(GL13.GL_TEXTURE0);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_LIGHTING);GlUiState.blend();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture(atlas));if(shader!=null){GL20.glUseProgram(shader.id);shader.i("image",0);}else if(GLContext.getCapabilities().OpenGL20)GL20.glUseProgram(0);
            float cursor=x,side=atlas.image.getWidth();
            for(HudText.Span span:HudText.spans(value,color)){
                int c=((color>>>24)<<24)|(span.color&0xFFFFFF);if(shader!=null)shader.color("color",c);else GL11.glColor4f(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f,(c>>>24)/255f);
                GL11.glBegin(GL11.GL_QUADS);
                for(int i=0;i<span.text.length();i++){
                    char ch=span.text.charAt(i);MenuTypography.Glyph glyph=atlas.glyphs[MenuTypography.glyph(ch)];
                    if(!glyph.empty){float u=glyph.x/side,v=glyph.y/side,ur=(glyph.x+glyph.width)/side,vb=(glyph.y+glyph.height)/side,left=cursor+glyph.offsetX,top=y+glyph.offsetY,right=left+glyph.logicalWidth,bottom=top+glyph.logicalHeight;
                        GL11.glTexCoord2f(u,v);GL11.glVertex2f(left,top);GL11.glTexCoord2f(ur,v);GL11.glVertex2f(right,top);GL11.glTexCoord2f(ur,vb);GL11.glVertex2f(right,bottom);GL11.glTexCoord2f(u,vb);GL11.glVertex2f(left,bottom);}
                    cursor+=MenuTypography.advance(ch);
                }GL11.glEnd();
            }
        }finally{if(GLContext.getCapabilities().OpenGL20)GL20.glUseProgram(previous);GL11.glPopAttrib();GL13.glActiveTexture(active);}
    }
}
