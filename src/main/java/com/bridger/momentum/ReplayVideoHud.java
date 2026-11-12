package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.*;

/** Composes the export HUD on the final back buffer, even when F1 is active. */
public final class ReplayVideoHud {
    private interface Draw{void paint(MinecraftUiCanvas canvas,int width,int height);}
    public static void frame(ReplayVideoOptions options){draw((canvas,w,h)->{ReplayVideoOverlay.paint(canvas,w,h,ReplaySession.videoFrame(),ReplaySession.recordingName(),ReplaySession.videoSeconds(),ReplaySession.lastTick()/20.0,options);if(options.hotbar)drawBlock(w,h);if(ReplayVideoExport.ending())ReplayVideoOverlay.ending(canvas,w,h,ReplayVideoExport.resultTime());});}
    public static C_88708284 blockStack(ReplayData data,double tick){ReplayData.Block recorded=ReplayVideoItems.block(data,tick);if(recorded==null)return null;C_81592558 block=C_81592558.m_64320102(recorded.id);int metadata=recorded.metadata;if(PlacementAppearance.enabled()){C_81592558 custom=C_81592558.m_58693857(PlacementAppearance.block());if(custom!=null){block=custom;metadata=PlacementAppearance.metadata();}}if(block==null)return null;C_88708284 stack=new C_88708284(block,64,metadata);return stack.m_23235460()==null?null:stack;}
    private static void drawBlock(int width,int height){C_88708284 stack=blockStack(ReplaySession.recording(),ReplaySession.videoSeconds()*20);if(stack==null)return;float scale=ReplayVideoOverlay.hotbarScale(width);int w=(int)Math.ceil(182*scale);boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);GL11.glPushMatrix();try{C_37925375.m_94091499(true);GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);GL11.glTranslatef((width-w)/2,height-(int)Math.ceil(24*scale),0);GL11.glScalef(scale,scale,1);C_14999373.m_28358909();Minecraft mc=Minecraft.m_20213497();mc.m_84598725().m_28591838(stack,3,3);mc.m_84598725().m_69117106(mc.f_21497584,stack,3,3,"64");}finally{C_14999373.m_94129982();C_37925375.m_94091499(depthMask);C_37925375.m_21251773();GL11.glPopMatrix();}}
    public static void status(String label,int percent){draw((canvas,w,h)->ReplayVideoOverlay.progress(canvas,w,h,label,percent));}
    private static void draw(Draw draw){Minecraft mc=Minecraft.m_20213497();C_43718703 window=new C_43718703(mc);int width=window.m_39119333(),height=window.m_16103615();boolean framebuffer=GLContext.getCapabilities().OpenGL30;int previousFramebuffer=framebuffer?GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING):0,buffer=GL11.glGetInteger(GL11.GL_DRAW_BUFFER),matrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE);boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),lighting=GL11.glIsEnabled(GL11.GL_LIGHTING);
        GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glOrtho(0,width,height,0,-1000,1000);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();
        try{if(framebuffer)GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,0);GL11.glDrawBuffer(GL11.GL_BACK);GL11.glViewport(0,0,mc.f_31800787,mc.f_74192110);C_37925375.m_21251773();C_37925375.m_04437956();draw.paint(new MinecraftUiCanvas(mc.f_21497584),width,height);}
        finally{if(depth)C_37925375.m_62426402();else C_37925375.m_21251773();if(lighting)C_37925375.m_58735801();else C_37925375.m_04437956();GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glMatrixMode(matrix);GL11.glPopAttrib();if(framebuffer)GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,previousFramebuffer);GL11.glDrawBuffer(buffer);}
    }
}
