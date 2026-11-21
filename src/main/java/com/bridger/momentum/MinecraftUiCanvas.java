package com.bridger.momentum;
import net.minecraft.unmapped.*;

import org.lwjgl.opengl.GL11;

/** Native drawing adapter shared by menus and the recorded-input HUD. */
public final class MinecraftUiCanvas implements UiCanvas {
    private final C_23014920 font;private final int style;private final boolean menu;private final float density;
    public MinecraftUiCanvas(C_23014920 font){this(font,-1);}
    public MinecraftUiCanvas(C_23014920 font,int style){this.font=font;this.style=style;this.menu=false;this.density=1;}
    public MinecraftUiCanvas(C_23014920 font,boolean menu){this(font,menu,1);}
    public MinecraftUiCanvas(C_23014920 font,boolean menu,float density){this.font=font;this.style=-1;this.menu=menu;this.density=Math.max(.25f,density);}
    private static final java.util.Map<String,C_22168528> SCENE_TEXTURES=new java.util.HashMap<>();
    public void pixelImage(java.awt.image.BufferedImage image,double x,double y,double w,double h,int tint){MenuImageRenderer.draw(image,x,y,w,h,tint,true);}
    public void image(java.awt.image.BufferedImage image,double x,double y,double w,double h,int tint){MenuImageRenderer.draw(image,x,y,w,h,tint);}
    public void texturedPolygon(double[] px,double[] py,String texture,int tint){
        int active=GL11.glGetInteger(org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE);C_37925375.m_79158255(org.lwjgl.opengl.GL13.GL_TEXTURE0);int previous=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),program=org.lwjgl.opengl.GLContext.getCapabilities().OpenGL20?GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM):0;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT);
        try{
            net.minecraft.client.Minecraft.m_20213497().m_65924382().m_69671809(SCENE_TEXTURES.computeIfAbsent(texture,C_22168528::new));
            GlPrograms.Program image=GlPrograms.get("image",false);GlUiState.blend();GL11.glDisable(GL11.GL_ALPHA_TEST);
            if(image!=null){org.lwjgl.opengl.GL20.glUseProgram(image.id);image.i("image",0);image.color("color",tint);}else{GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glColor4f(((tint>>16)&255)/255f,((tint>>8)&255)/255f,(tint&255)/255f,(tint>>>24)/255f);}
            GL11.glBegin(GL11.GL_QUADS);for(int i=0;i<4;i++){GL11.glTexCoord2f(i==1||i==2?1:0,i>=2?1:0);GL11.glVertex2d(px[i],py[i]);}GL11.glEnd();
        }finally{C_37925375.m_20466433(previous);if(org.lwjgl.opengl.GLContext.getCapabilities().OpenGL20)org.lwjgl.opengl.GL20.glUseProgram(program);GL11.glPopAttrib();C_37925375.m_79158255(active);}
    }
    public float pixelDensity(){return density;}
    public void roundedRect(double x,double y,double w,double h,double radius,int color){if(!GlassRenderer.rounded(x,y,w,h,radius,color,0,0))UiCanvas.super.roundedRect(x,y,w,h,radius,color);}
    public void glassRect(double x,double y,double w,double h,double radius,int fill,int border,float reflection){if(!GlassRenderer.rounded(x,y,w,h,radius,fill,border,reflection))UiCanvas.super.glassRect(x,y,w,h,radius,fill,border,reflection);}
    public void polygon(double[] px,double[] py,int color){GlassRenderer.polygon(px,py,color);}
    public void rect(int x,int y,int width,int height,int color){if(!menu){if(width>0&&height>0)C_29058632.m_57734177(x,y,x+width,y+height,color);return;}if(width>0&&height>0)GlassRenderer.polygon(new double[]{x,x+width,x+width,x},new double[]{y,y,y+height,y+height},color);}
    public void text(String value,int x,int y,int color){if(menu)MenuFontRenderer.draw(value,x,y,color,density);else if(style<0)HudText.draw(font,value,x,y,color,false);else HudText.drawStyled(style,font,value,x,y,color);}
    public int width(String value){return menu?MenuTypography.width(value):HudText.width(font,value);}
    public void strongText(String value,int x,int y,int color,float scale){
        if(!menu){textScaled(value,x,y,color,scale);return;}
        GL11.glPushMatrix();try{GL11.glTranslatef(x,y,0);GL11.glScalef(scale,scale,1);MenuFontRenderer.draw(value,0,0,color,density*scale,true);}finally{GL11.glPopMatrix();}
    }
    public void textScaled(String value,int x,int y,int color,float scale){
        GL11.glPushMatrix();try{GL11.glTranslatef(x,y,0);GL11.glScalef(scale,scale,1);if(menu)MenuFontRenderer.draw(value,0,0,color,density*scale);else if(style<0)HudText.draw(font,value,0,0,color,false);else HudText.drawStyled(style,font,value,0,0,color);}finally{GL11.glPopMatrix();}
    }
}
