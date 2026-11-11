package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.GL11;

public final class IslandCopySelection {
    private static IslandBox box;private static boolean active;
    public static IslandBox box(){return box;}
    public static void end(){active=false;box=null;}
    public static void begin(){active=true;auto();}
    public static void auto(){
        IslandBox placed=IslandOverlay.copyBounds();if(placed!=null){box=placed;return;}
        Minecraft mc=Minecraft.m_20213497();IslandReplacement.Mask found=mc!=null&&mc.f_26407825!=null?IslandReplacement.capture():IslandReplacement.EMPTY;
        if(!found.empty())box=new IslandBox(found.minX,found.minY,found.minZ,found.maxX-found.minX+1,found.maxY-found.minY+1,found.maxZ-found.minZ+1);
        else{C_57778754 viewer=mc==null?null:ReplaySession.viewer();int x=viewer==null?0:(int)Math.floor(viewer.f_78826675),y=viewer==null?64:(int)Math.floor(viewer.f_31030949),z=viewer==null?0:(int)Math.floor(viewer.f_97691941);box=new IslandBox(x-8,Math.max(0,y-18),z-8,17,Math.min(24,256-Math.max(0,y-18)),17);}
    }
    public static void corners(C_32594356 a,C_32594356 b){box=new IslandBox(Math.min(a.m_32364273(),b.m_32364273()),Math.min(a.m_62825721(),b.m_62825721()),Math.min(a.m_23514936(),b.m_23514936()),Math.abs(a.m_32364273()-b.m_32364273())+1,Math.abs(a.m_62825721()-b.m_62825721())+1,Math.abs(a.m_23514936()-b.m_23514936())+1);}
    public static void move(int right,int up,int forward){if(box==null)return;C_57778754 viewer=ReplaySession.viewer();if(viewer==null)return;double yaw=Math.toRadians(viewer.f_80130373);box.move((int)Math.round(-Math.cos(yaw)*right-Math.sin(yaw)*forward),up,(int)Math.round(-Math.sin(yaw)*right+Math.cos(yaw)*forward));}
    public static C_32594356 pick(double x,double y,int width,int height){
        C_57778754 viewer=ReplaySession.viewer();C_34399650 world=ReplaySession.viewWorld();if(viewer==null||world==null)return null;
        double yaw=Math.toRadians(viewer.f_80130373),pitch=Math.toRadians(viewer.f_03549461),tangent=Math.tan(Math.toRadians(70)/2),nx=(x/width*2-1)*tangent*width/height,ny=(1-y/height*2)*tangent;
        double dx=-Math.sin(yaw)*Math.cos(pitch)-Math.cos(yaw)*nx+Math.sin(yaw)*Math.sin(pitch)*ny,dy=-Math.sin(pitch)+Math.cos(pitch)*ny,dz=Math.cos(yaw)*Math.cos(pitch)-Math.sin(yaw)*nx-Math.cos(yaw)*Math.sin(pitch)*ny;
        double length=Math.sqrt(dx*dx+dy*dy+dz*dz),ey=viewer.f_31030949+viewer.m_75246573();C_24654288 hit=world.m_82559274(new C_94584382(viewer.f_78826675,ey,viewer.f_97691941),new C_94584382(viewer.f_78826675+dx/length*160,ey+dy/length*160,viewer.f_97691941+dz/length*160));return hit==null?null:hit.m_76126067();
    }
    public static void render(float partial){
        if(!active||box==null)return;Minecraft mc=Minecraft.m_20213497();C_42232651 viewer=mc.m_67314413();if(viewer==null)return;
        double x=viewer.f_09368719+(viewer.f_78826675-viewer.f_09368719)*partial,y=viewer.f_93723721+(viewer.f_31030949-viewer.f_93723721)*partial,z=viewer.f_84066837+(viewer.f_97691941-viewer.f_84066837)*partial;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{GL11.glTranslated(box.x-x,box.y-y,box.z-z);IslandOverlay.drawSelectionBox(box.width,box.height,box.depth);}finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
}
