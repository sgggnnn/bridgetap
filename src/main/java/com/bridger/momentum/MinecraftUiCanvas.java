package com.bridger.momentum;

import net.minecraft.unmapped.C_23014920;
import net.minecraft.unmapped.C_29058632;
import org.lwjgl.opengl.GL11;

/** Native drawing adapter shared by menus and the recorded-input HUD. */
public final class MinecraftUiCanvas implements UiCanvas {
    private final C_23014920 font;
    public MinecraftUiCanvas(C_23014920 font){this.font=font;}
    public void rect(int x,int y,int width,int height,int color){if(width>0&&height>0)C_29058632.m_57734177(x,y,x+width,y+height,color);}
    public void text(String value,int x,int y,int color){font.m_11445413(value,x,y,color,false);}
    public int width(String value){return font.m_09235706(value);}
    public void textScaled(String value,int x,int y,int color,float scale){
        GL11.glPushMatrix();try{GL11.glTranslatef(x,y,0);GL11.glScalef(scale,scale,1);font.m_11445413(value,0,0,color,false);}finally{GL11.glPopMatrix();}
    }
}
