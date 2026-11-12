package com.bridger.momentum;

import net.minecraft.unmapped.C_23014920;
import net.minecraft.unmapped.C_29058632;
import org.lwjgl.opengl.GL11;

/** Native drawing adapter shared by menus and the recorded-input HUD. */
public final class MinecraftUiCanvas implements UiCanvas {
    private final C_23014920 font;private final int style;
    public MinecraftUiCanvas(C_23014920 font){this(font,-1);}
    public MinecraftUiCanvas(C_23014920 font,int style){this.font=font;this.style=style;}
    public void rect(int x,int y,int width,int height,int color){if(width>0&&height>0)C_29058632.m_57734177(x,y,x+width,y+height,color);}
    public void text(String value,int x,int y,int color){if(style<0)HudText.draw(font,value,x,y,color,false);else HudText.drawStyled(style,font,value,x,y,color);}
    public int width(String value){return HudText.width(font,value);}
    public void textScaled(String value,int x,int y,int color,float scale){
        GL11.glPushMatrix();try{GL11.glTranslatef(x,y,0);GL11.glScalef(scale,scale,1);if(style<0)HudText.draw(font,value,0,0,color,false);else HudText.drawStyled(style,font,value,0,0,color);}finally{GL11.glPopMatrix();}
    }
}
