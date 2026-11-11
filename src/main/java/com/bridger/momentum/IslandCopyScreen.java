package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import java.nio.file.Path;

/** Copy editor mirrors placement: visible bounds, drag, resize, save or cancel. */
public final class IslandCopyScreen extends BridgeScreen {
    private final IslandScreen parent;private String name="",message="Drag to move. Right-drag to look around.";private boolean dragging,looking,picking;private int dragX,dragY,lookX,lookY;private C_32594356 first;
    public IslandCopyScreen(IslandScreen parent){this.parent=parent;ReplaySession.freeCopyCamera();IslandCopySelection.begin();}
    @Override protected int backgroundColor(){return 0;}
    @Override protected int backdropColor(){return 0;}
    @Override protected void paint(){
        region(0,0,608,HEIGHT,()->{if(picking)pick();else{dragging=true;dragX=mouseX;dragY=mouseY;}});
        panel(620,12,228,476);text("COPY ISLAND",636,29,ACCENT);label("The box is what will be copied.",636,53);
        input("name",name,"Island name (optional)",636,75,196,64,value->name=value);
        button("Auto select nearby island",636,114,196,26,true,false,()->{IslandCopySelection.auto();picking=false;first=null;});
        button(picking?(first==null?"Click first corner":"Click opposite corner"):"Pick two corners",636,152,196,26,true,picking,()->{picking=true;first=null;});
        IslandBox box=IslandCopySelection.box();
        if(box!=null){stepper("Width",String.valueOf(box.width),636,195,196,()->box.resize(0,-1),()->box.resize(0,1));stepper("Height",String.valueOf(box.height),636,230,196,()->box.resize(1,-1),()->box.resize(1,1));stepper("Depth",String.valueOf(box.depth),636,265,196,()->box.resize(2,-1),()->box.resize(2,1));clipped(box.x+", "+box.y+", "+box.z+" / "+box.volume()+" cells",636,309,196,MUTED);}
        clipped(message,636,337,196,MUTED);
        button("Save island",636,373,196,32,box!=null&&box.valid(),true,this::save);button("Cancel",636,421,196,28,true,false,this::back);
        rect(12,HEIGHT-34,588,22,0xCC101722);text("Drag: move / Right-drag: look / Scroll: distance / Enter: save",22,HEIGHT-27,TEXT);
    }
    private void pick(){float s=scale();C_32594356 hit=IslandCopySelection.pick((mouseX+originX(s))*s,(mouseY+originY(s))*s,f_05230747>0?f_05230747:WIDTH,f_07021018>0?f_07021018:HEIGHT);if(hit==null){message="No block here. Right-drag to aim.";return;}if(first==null){first=hit;IslandCopySelection.corners(hit,hit);message="Now click the opposite corner.";}else{IslandCopySelection.corners(first,hit);picking=false;first=null;message="Selection ready. Drag or resize it.";}}
    private void save(){try{IslandBox box=IslandCopySelection.box();if(box==null||!box.valid())throw new IllegalArgumentException("Selection too large: resize the box");IslandTemplate island=IslandOverlay.copyBox(name.trim().isEmpty()?"My island":name,box);Path file=IslandLibrary.save(island,"saved");parent.copiedFile(file,"Island saved. Select Place when you want to paste it.");IslandCopySelection.end();open(parent);}catch(Exception ex){message=ex.getMessage()==null?"Could not copy island":ex.getMessage();}}
    @Override public void dragPreview(int x,int y){super.dragPreview(x,y);if(!dragging)return;int dx=(x-dragX)/10,dy=(y-dragY)/10;if(dx!=0||dy!=0){IslandCopySelection.move(dx,-dy,0);dragX+=dx*10;dragY+=dy*10;}}
    @Override public void releasePreview(){dragging=false;}
    @Override protected void rightClick(int x,int y){if(x<610){looking=true;lookX=x;lookY=y;}}
    @Override protected void m_52921597(int x,int y,int button,long elapsed){if(button!=1){super.m_52921597(x,y,button,elapsed);return;}if(!looking)return;float s=scale();int nx=(int)(x/s)-originX(s),ny=(int)(y/s)-originY(s);C_57778754 viewer=ReplaySession.viewer();if(viewer!=null){viewer.f_80130373+=(nx-lookX)*.25f;viewer.f_03549461=Math.max(-89,Math.min(89,viewer.f_03549461+(ny-lookY)*.25f));}lookX=nx;lookY=ny;}
    @Override protected void m_47537840(int x,int y,int button){if(button==1)looking=false;else super.m_47537840(x,y,button);}
    @Override protected void scroll(int x,int y,int direction){IslandCopySelection.move(0,0,direction);}
    @Override protected void key(int key){if(key==28||key==156)save();if(key==203)IslandCopySelection.move(-1,0,0);if(key==205)IslandCopySelection.move(1,0,0);if(key==200)IslandCopySelection.move(0,1,0);if(key==208)IslandCopySelection.move(0,-1,0);}
    @Override protected void back(){IslandCopySelection.end();open(parent);}
    @Override public void m_47002234(){IslandCopySelection.end();}
}
