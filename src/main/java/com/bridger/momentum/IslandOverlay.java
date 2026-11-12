package com.bridger.momentum;

import java.util.*;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

/** Render-only decoration: never writes blocks or supplies collisions to a world. */
public final class IslandOverlay {
    private static C_32594356 first,second;
    private static C_34399650 selectionWorld,placedWorld;
    private static IslandTemplate active;
    private static C_15815210[] states;
    private static Map<Integer,Integer> stateIndex=Collections.emptyMap();
    private static C_41102159 builtModel;
    private static int x,y,z,startX,startY,startZ,finishX,finishY,finishZ,rotation;private static boolean dirty,preview;
    private static IslandParts parts;
    private static final C_93396801.C_25517371[] meshes=new C_93396801.C_25517371[2];
    private static long lastDraw,maskRefreshAt;
    private static void moved(){lastDraw=0;maskRefreshAt=System.nanoTime()+200000000L;rememberEdit();}
    private static Backup backup;
    private static int editBaseX,editBaseY,editBaseZ;private static IslandEditHistory edits;private static boolean restoringEdit;
    private static Path libraryFile;
    private static IslandReplacement.Mask target=IslandReplacement.EMPTY;
    private static IslandReplacement.Mask originalMask=IslandReplacement.EMPTY;
    private static final class Backup {IslandTemplate template;int x,y,z,fx,fy,fz,sx,sy,sz,turn;C_34399650 world;Path file;IslandReplacement.Mask mask,baseMask;Backup(){template=active;this.x=IslandOverlay.x;this.y=IslandOverlay.y;this.z=IslandOverlay.z;fx=finishX;fy=finishY;fz=finishZ;sx=startX;sy=startY;sz=startZ;turn=rotation;world=placedWorld;file=libraryFile;mask=IslandReplacement.current();baseMask=originalMask;}}
    private static String failure="";
    public static String failure(){return failure;}
    public static boolean enabled(){return active!=null;}
    public static IslandTemplate activeTemplate(){return active;}
    public static IslandBox copyBounds(){if(active==null)return null;int[] b=sceneBounds();return new IslandBox(x+b[0],y+b[1],z+b[2],b[3]-b[0],b[4]-b[1],b[5]-b[2]);}
    public static C_15815210 visibleState(int wx,int wy,int wz,C_15815210 fallback){if(active!=null){int[] local=IslandRotation.point(wx-x,wz-z,active.anchorX,active.anchorZ,-rotation);for(int group=1;group>=0;group--){Integer index=stateIndex.get(key(local[0]-(group==1?finishX:startX),wy-y-(group==1?finishY:startY),local[1]-(group==1?finishZ:startZ)));if(index!=null&&(parts.finish[index]?1:0)==group)return states[index];}}return IslandReplacement.hidden(wx,wy,wz)?C_81592558.m_64320102(0).m_52891069():fallback;}
    public static int rotation(){return rotation*90;}
    public static void rotate(){rotation=(rotation+1)%4;moved();}
    public static boolean hasFinish(){return parts!=null&&parts.paired;}
    public static int gap(){return hasFinish()?parts.gap(finishX-startX,finishZ-startZ):0;}
    public static int finishHeight(){return finishY;}
    public static int startHeight(){return startY;}
    public static int sceneY(){return y;}
    public static IslandLayout layout(){
        if(active==null)throw new IllegalStateException("Place an island before saving a preset");
        IslandReplacement.Mask anchor=originalMask;
        // Use fixed native-island bounds, never the block currently below the camera.
        int bx=anchor.empty()?x:anchor.presetX-active.anchorX;
        int by=anchor.empty()?y:anchor.presetY-active.anchorY;
        int bz=anchor.empty()?z:anchor.presetZ-active.anchorZ;
        return new IslandLayout(x-bx,y-by,z-bz,startX,startY,startZ,finishX,finishY,finishZ,rotation,IslandReplacement.focus());
    }
    public static void applyPreset(IslandTemplate template,IslandLayout layout){
        IslandReplacement.Mask presetTarget=IslandStartAnchors.resolve(template,layout);
        Backup previous=new Backup();boolean oldFocus=IslandReplacement.focus();
        try{
            beginPreview(template,presetTarget);
            if(layout.stableAnchor&&!originalMask.empty()){
                x=originalMask.presetX-template.anchorX;
                y=originalMask.presetY-template.anchorY;
                z=originalMask.presetZ-template.anchorZ;
            }
            x+=layout.x;y+=layout.y;z+=layout.z;
            startX=layout.startX;startY=layout.startY;startZ=layout.startZ;
            finishX=layout.finishX;finishY=layout.finishY;finishZ=layout.finishZ;rotation=layout.rotation;
            if(IslandReplacement.focus()!=layout.focus)IslandReplacement.toggleFocus();moved();confirm();
        }catch(RuntimeException ex){backup=previous;cancel();if(IslandReplacement.focus()!=oldFocus)IslandReplacement.toggleFocus();throw ex;}
    }
    public static void resetStart(){startX=startY=startZ=0;moved();}
    public static void moveStart(int dx,int dy,int dz){if(active==null)return;startX+=dx;startY+=dy;startZ+=dz;moved();}
    public static void nudgeStart(int right,int up,int forward){C_57778754 camera=ReplaySession.viewer();if(camera==null)return;double yaw=Math.toRadians(camera.f_80130373);int[] local=IslandRotation.point((int)Math.round(-Math.cos(yaw)*right-Math.sin(yaw)*forward),(int)Math.round(-Math.sin(yaw)*right+Math.cos(yaw)*forward),0,0,-rotation);moveStart(local[0],up,local[1]);}
    public static void gap(int change){if(!hasFinish())return;int delta=Math.max(-gap(),change)*parts.direction;if(parts.axis==0)finishX+=delta;else finishZ+=delta;moved();}
    public static void resetFinish(){finishX=finishY=finishZ=0;moved();}
    public static void moveFinish(int dx,int dy,int dz){if(!hasFinish())return;finishX+=dx;finishY+=dy;finishZ+=dz;moved();}
    public static void nudgeFinish(int right,int up,int forward){C_57778754 camera=ReplaySession.viewer();if(camera==null)return;double yaw=Math.toRadians(camera.f_80130373);int[] local=IslandRotation.point((int)Math.round(-Math.cos(yaw)*right-Math.sin(yaw)*forward),(int)Math.round(-Math.sin(yaw)*right+Math.cos(yaw)*forward),0,0,-rotation);moveFinish(local[0],up,local[1]);}
    private static int[] sceneBounds(){int[] result=parts.bounds[0].clone();for(int a=0;a<3;a++){int offset=a==0?startX:a==1?startY:startZ;result[a]+=offset;result[a+3]+=offset;}if(hasFinish())for(int a=0;a<3;a++){int offset=a==0?finishX:a==1?finishY:finishZ;result[a]=Math.min(result[a],parts.bounds[1][a]+offset);result[a+3]=Math.max(result[a+3],parts.bounds[1][a+3]+offset);}return IslandRotation.bounds(result,active.anchorX,active.anchorZ,rotation);}
    public static void libraryFile(Path path){libraryFile=path.toAbsolutePath().normalize();}
    public static boolean isFrom(Path path){return libraryFile!=null&&libraryFile.equals(path.toAbsolutePath().normalize());}
    public static void renameLabel(Path path,String name){if(isFrom(path)&&active!=null)active=new IslandTemplate(name,active.width,active.height,active.depth,active.originX,active.originY,active.originZ,active.blocks,active.anchorX,active.anchorY,active.anchorZ,active.bothIslands);}
    public static boolean previewing(){return preview;}
    public static boolean drawn(){return active!=null&&System.nanoTime()-lastDraw<1000000000L;}
    public static void beginPreview(IslandTemplate template){beginPreview(template,IslandReplacement.captureScene());}
    private static void beginPreview(IslandTemplate template,IslandReplacement.Mask detected){ServerPresets.manual();edits=null;Backup previous=new Backup();target=detected;originalMask=target;show(template,false);backup=previous;preview=true;if(!target.empty())replaceHere();editBaseX=x;editBaseY=y;editBaseZ=z;edits=new IslandEditHistory(editLayout());}
    private static IslandLayout editLayout(){if(!originalMask.empty())return layout();return new IslandLayout(x-editBaseX,y-editBaseY,z-editBaseZ,startX,startY,startZ,finishX,finishY,finishZ,rotation,IslandReplacement.focus());}
    public static void rememberEdit(){if(preview&&edits!=null&&!restoringEdit)edits.record(editLayout());}
    public static void beginEditDrag(){if(edits!=null)edits.begin();}
    public static void endEditDrag(){if(edits!=null)edits.end(editLayout());}
    public static boolean canUndo(){return edits!=null&&edits.canUndo();}
    public static boolean canRedo(){return edits!=null&&edits.canRedo();}
    public static void undo(){if(canUndo())restoreEdit(edits.undo());}
    public static void redo(){if(canRedo())restoreEdit(edits.redo());}
    private static void restoreEdit(IslandLayout saved){restoringEdit=true;try{int bx=originalMask.empty()?editBaseX:originalMask.presetX-active.anchorX,by=originalMask.empty()?editBaseY:originalMask.presetY-active.anchorY,bz=originalMask.empty()?editBaseZ:originalMask.presetZ-active.anchorZ;x=bx+saved.x;y=by+saved.y;z=bz+saved.z;startX=saved.startX;startY=saved.startY;startZ=saved.startZ;finishX=saved.finishX;finishY=saved.finishY;finishZ=saved.finishZ;rotation=saved.rotation;if(IslandReplacement.focus()!=saved.focus)IslandReplacement.toggleFocus();moved();}finally{restoringEdit=false;}}
    public static boolean canReplace(){return !target.empty();}
    public static void detectReplacement(){IslandReplacement.Mask detected=IslandReplacement.captureScene();if(!detected.empty()){originalMask=target=detected;IslandReplacement.set(target);moved();}}
    public static boolean replacing(){return !IslandReplacement.current().empty();}
    public static void toggleReplacement(){IslandReplacement.set(replacing()?IslandReplacement.EMPTY:target);moved();}
    public static void replaceHere(){if(active==null||target.empty())return;int[] anchor=IslandRotation.point(active.anchorX+startX,active.anchorZ+startZ,active.anchorX,active.anchorZ,rotation);x=target.attachX-anchor[0];y=target.surface-active.anchorY-startY;z=target.attachZ-anchor[1];IslandReplacement.set(target);moved();}
    public static void confirm(){refreshMask();preview=false;backup=null;}
    public static void refreshMask(){maskRefreshAt=0;if(active==null||!replacing())return;List<IslandBox> boxes=new ArrayList<>();for(int group=0;group<(hasFinish()?2:1);group++){int[] b=parts.bounds[group].clone();if(group==1){b[0]+=finishX;b[3]+=finishX;b[1]+=finishY;b[4]+=finishY;b[2]+=finishZ;b[5]+=finishZ;}else{b[0]+=startX;b[3]+=startX;b[1]+=startY;b[4]+=startY;b[2]+=startZ;b[5]+=startZ;}b=IslandRotation.bounds(b,active.anchorX,active.anchorZ,rotation);boxes.add(new IslandBox(x+b[0],y+b[1],z+b[2],b[3]-b[0],b[4]-b[1],b[5]-b[2]));}target=IslandReplacement.extend(originalMask,boxes);IslandReplacement.set(target);}
    public static void cancel(){Backup previous=backup;backup=null;preview=false;disable();if(previous!=null&&previous.template!=null&&previous.world==ReplaySession.sourceWorld()){show(previous.template,true);x=previous.x;y=previous.y;z=previous.z;finishX=previous.fx;finishY=previous.fy;finishZ=previous.fz;startX=previous.sx;startY=previous.sy;startZ=previous.sz;rotation=previous.turn;libraryFile=previous.file;target=previous.mask;originalMask=previous.baseMask;IslandReplacement.set(previous.mask);}}
    public static void nudge(int right,int up,int forward){C_57778754 camera=ReplaySession.viewer();if(camera==null)return;double yaw=Math.toRadians(camera.f_80130373);move((int)Math.round(-Math.cos(yaw)*right-Math.sin(yaw)*forward),up,(int)Math.round(-Math.sin(yaw)*right+Math.cos(yaw)*forward));}
    public static String description(){return active==null?"No scenery active":active.name+" @ "+x+", "+y+", "+z;}
    public static String corners(){return "Corner 1: "+pos(first)+"  /  Corner 2: "+pos(second);}
    private static String pos(C_32594356 p){return p==null?"not set":p.m_32364273()+","+p.m_62825721()+","+p.m_23514936();}
    public static void tick(){
        Minecraft mc=Minecraft.m_20213497();
        if(mc.f_26407825!=selectionWorld){selectionWorld=mc.f_26407825;first=second=null;}
        if(active!=null&&mc.f_26407825!=placedWorld)disable();
        if(active!=null&&maskRefreshAt!=0&&System.nanoTime()>=maskRefreshAt)refreshMask();
    }
    public static void corner(boolean one){
        Minecraft mc=Minecraft.m_20213497();if(ReplaySession.active())throw new IllegalArgumentException("Select scenery in normal gameplay");
        tick();if(mc.f_28396371==null||mc.f_81460813==null||mc.f_81460813.m_76126067()==null)throw new IllegalArgumentException("Look at a block first");
        C_32594356 target=mc.f_81460813.m_76126067();if(one)first=target;else second=target;
    }
    public static IslandTemplate copy(String name){
        Minecraft mc=Minecraft.m_20213497();tick();
        if(ReplaySession.active()||first==null||second==null||mc.f_26407825==null)throw new IllegalArgumentException("Set both corners in the current world first");
        int ox=Math.min(first.m_32364273(),second.m_32364273()),oy=Math.min(first.m_62825721(),second.m_62825721()),oz=Math.min(first.m_23514936(),second.m_23514936());
        int w=Math.abs(first.m_32364273()-second.m_32364273())+1,h=Math.abs(first.m_62825721()-second.m_62825721())+1,d=Math.abs(first.m_23514936()-second.m_23514936())+1;
        if(w>IslandTemplate.MAX_SIDE||h>256||d>IslandTemplate.MAX_SIDE||(long)w*h*d>IslandTemplate.MAX_VOLUME)throw new IllegalArgumentException("Selection too large: maximum 512 horizontally / 1048576 cells");
        if(!mc.f_26407825.m_50500004(new C_32594356(ox,oy,oz),new C_32594356(ox+w-1,oy+h-1,oz+d-1)))throw new IllegalArgumentException("All selected chunks must be loaded");
        List<IslandTemplate.Block> blocks=new ArrayList<>();
        for(int bx=0;bx<w;bx++)for(int by=0;by<h;by++)for(int bz=0;bz<d;bz++){
            C_15815210 state=mc.f_26407825.m_77852500(new C_32594356(ox+bx,oy+by,oz+bz));C_81592558 block=state.m_41958956();
            if(block.m_43136364()!=3)continue;
            blocks.add(new IslandTemplate.Block(bx,by,bz,C_81592558.f_83036452.m_55287479(block).toString(),block.m_14593609(state)));
            if(blocks.size()>IslandTemplate.MAX_BLOCKS)throw new IllegalArgumentException("Selection exceeds 131072 static blocks");
        }
        return new IslandTemplate(name,w,h,d,ox,oy,oz,blocks);
    }
    public static IslandTemplate copyBox(String name,IslandBox box){
        if(!box.valid())throw new IllegalArgumentException("Invalid copy bounds");C_34399650 world=ReplaySession.viewWorld();if(world==null)throw new IllegalArgumentException("Join a world first");
        List<IslandTemplate.Block> result=new ArrayList<>();for(int x=0;x<box.width;x++)for(int y=0;y<box.height;y++)for(int z=0;z<box.depth;z++){
            C_32594356 pos=new C_32594356(box.x+x,box.y+y,box.z+z);boolean loaded=world.m_09893682(pos);if(!loaded&&!ReplaySession.active()&&active==null)throw new IllegalArgumentException("Selection extends beyond loaded terrain");C_15815210 state=visibleState(box.x+x,box.y+y,box.z+z,loaded?world.m_77852500(pos):C_81592558.m_64320102(0).m_52891069());C_81592558 block=state.m_41958956();if(block.m_43136364()!=3)continue;
            result.add(new IslandTemplate.Block(x,y,z,C_81592558.f_83036452.m_55287479(block).toString(),block.m_14593609(state)));if(result.size()>IslandTemplate.MAX_BLOCKS)throw new IllegalArgumentException("Too many blocks. Make the selection smaller.");
        }return new IslandTemplate(name,box.width,box.height,box.depth,box.x,box.y,box.z,result);
    }
    public static IslandTemplate fromReplay(String name,ReplayData replay,boolean finish){
        List<ReplayData.Block> captured=ReplayIslandSelection.select(replay,finish,b->{C_81592558 block=C_81592558.m_64320102(b.id);return block!=null&&block.m_43136364()==3;});
        return fromRecordedBlocks(name,replay,captured,finish,false);
    }
    public static IslandTemplate bothFromReplay(String name,ReplayData replay){return fromRecordedBlocks(name,replay,ReplayIslandSelection.both(replay,b->{C_81592558 block=C_81592558.m_64320102(b.id);return block!=null&&block.m_43136364()==3;}),false,true);}
    private static IslandTemplate fromRecordedBlocks(String name,ReplayData replay,List<ReplayData.Block> captured,boolean finish,boolean both){
        int ox=Integer.MAX_VALUE,oy=256,oz=Integer.MAX_VALUE,mx=Integer.MIN_VALUE,my=0,mz=Integer.MIN_VALUE;
        for(ReplayData.Block b:captured){ox=Math.min(ox,b.x);oy=Math.min(oy,b.y);oz=Math.min(oz,b.z);mx=Math.max(mx,b.x);my=Math.max(my,b.y);mz=Math.max(mz,b.z);}
        List<IslandTemplate.Block> blocks=new ArrayList<>();Set<Long> startKeys=new HashSet<>();if(both)for(ReplayData.Block b:ReplayIslandSelection.select(replay,false,cell->{C_81592558 nativeBlock=C_81592558.m_64320102(cell.id);return nativeBlock!=null&&nativeBlock.m_43136364()==3;}))startKeys.add(b.key());
        for(ReplayData.Block b:captured){C_81592558 block=C_81592558.m_64320102(b.id);blocks.add(new IslandTemplate.Block(b.x-ox,b.y-oy,b.z-oz,C_81592558.f_83036452.m_55287479(block).toString(),b.metadata,both?(startKeys.contains(b.key())?0:1):-1));}
        ReplayData.Frame point=replay.frames.get(finish?replay.frames.size()-1:0);
        return new IslandTemplate(name,mx-ox+1,my-oy+1,mz-oz+1,ox,oy,oz,blocks,Math.max(0,Math.min(mx-ox,(int)Math.floor(point.x)-ox)),Math.max(0,Math.min(my-oy+13,(int)Math.round(point.y)-oy)),Math.max(0,Math.min(mz-oz,(int)Math.floor(point.z)-oz)),both);
    }
    public static void show(IslandTemplate template,boolean original){
        Minecraft mc=Minecraft.m_20213497();C_57778754 viewer=ReplaySession.viewer();if(viewer==null)throw new IllegalArgumentException("Join a world before placing scenery");
        C_15815210[] resolved=new C_15815210[template.blocks.size()];
        for(int i=0;i<resolved.length;i++){
            IslandTemplate.Block b=template.blocks.get(i);C_81592558 block=C_81592558.m_58693857(b.key);
            if(block==null||block.m_43136364()!=3||!b.key.equals(C_81592558.f_83036452.m_55287479(block).toString()))throw new IllegalArgumentException("Missing or unsupported block: "+b.key);
            resolved[i]=block.m_28027445(b.meta);
        }
        IslandBlockView view=new IslandBlockView(template,resolved);for(int i=0;i<resolved.length;i++){IslandTemplate.Block b=template.blocks.get(i);resolved[i]=resolved[i].m_41958956().m_43334227(resolved[i],view,new C_32594356(b.x,b.y,b.z));}
        active=template;states=resolved;parts=new IslandParts(template);rotation=0;startX=startY=startZ=0;resetFinish();placedWorld=ReplaySession.sourceWorld();failure="";dirty=true;lastDraw=0;libraryFile=null;
        if(IslandReplacement.focus())IslandReplacement.requestTerrainRebuild();
        Map<Integer,Integer> indexes=new HashMap<>();for(int i=0;i<template.blocks.size();i++){IslandTemplate.Block b=template.blocks.get(i);indexes.put(key(b.x,b.y,b.z),i);}stateIndex=indexes;
        double yaw=Math.toRadians(viewer.f_80130373),distance=Math.max(8,Math.max(template.width,template.depth)*.75);
        x=original?template.originX:(int)Math.floor(viewer.f_78826675-Math.sin(yaw)*distance-template.width/2.0);
        y=original?template.originY:(int)Math.floor(viewer.f_31030949)-template.height-1;
        z=original?template.originZ:(int)Math.floor(viewer.f_97691941+Math.cos(yaw)*distance-template.depth/2.0);
    }
    public static void move(int dx,int dy,int dz){if(active!=null){x+=dx;y+=dy;z+=dz;moved();}}
    public static void disable(){if(IslandReplacement.focus())IslandReplacement.requestTerrainRebuild();active=null;states=null;parts=null;stateIndex=Collections.emptyMap();Arrays.fill(meshes,null);dirty=true;preview=false;lastDraw=0;libraryFile=null;IslandReplacement.set(IslandReplacement.EMPTY);}
    public static void render(float partial){
        Minecraft mc=Minecraft.m_20213497();
        if(active!=null&&meshes[0]!=null&&mc.m_01427411().m_35215294().m_50076884(states[0])!=builtModel)dirty=true;
        if(dirty)Arrays.fill(meshes,null);dirty=false;
        if(active==null)return;C_42232651 camera=mc.m_67314413();if(camera==null)return;
        double cx=camera.f_09368719+(camera.f_78826675-camera.f_09368719)*partial,cy=camera.f_93723721+(camera.f_31030949-camera.f_93723721)*partial,cz=camera.f_84066837+(camera.f_97691941-camera.f_84066837)*partial;
        int[] bounds=sceneBounds();if(cx<x+bounds[0]-160||cx>x+bounds[3]+160||cy<y+bounds[1]-160||cy>y+bounds[4]+160||cz<z+bounds[2]-160||cz>z+bounds[5]+160)return;
        // Use the native vertex uploader; display lists and glBegin are unreliable
        // when another renderer leaves client arrays or multiple texture units active.
        int arrayBuffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);GL11.glPushMatrix();
        try{
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,0);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glDisable(GL11.GL_TEXTURE_2D);GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D,mc.m_62864302().m_94041999());
            GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(true);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glColor4f(1,1,1,1);
            GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.01f);
            GL11.glTranslated(x-cx,y-cy,z-cz);
            GL11.glTranslated(active.anchorX+.5,0,active.anchorZ+.5);GL11.glRotatef(-rotation*90,0,1,0);GL11.glTranslated(-active.anchorX-.5,0,-active.anchorZ-.5);
            if(!failure.isEmpty()){if(preview)outline(active.width,active.height,active.depth);return;}
            C_96603444 tess=C_96603444.m_23865043();C_93396801 buffer=tess.m_74061400();
            for(int group=0;group<(hasFinish()?2:1);group++){
            if(meshes[group]==null){
                Set<Integer> opaque=new HashSet<>();for(int i=0;i<states.length;i++)if((parts.finish[i]?1:0)==group&&states[i].m_41958956().m_57757663()){IslandTemplate.Block b=active.blocks.get(i);opaque.add(key(b.x,b.y,b.z));}
                builtModel=mc.m_01427411().m_35215294().m_50076884(states[0]);
                buffer.m_90389466(GL11.GL_QUADS,C_90362304.f_51724203);
                try{for(int i=0;i<states.length;i++){
                    if((parts.finish[i]?1:0)!=group)continue;
                    IslandTemplate.Block b=active.blocks.get(i);C_41102159 model=mc.m_01427411().m_35215294().m_50076884(states[i]);
                    int tint=states[i].m_41958956().m_51470331(states[i]);
                    for(C_28786548 face:C_28786548.values())if(!opaque.contains(key(b.x+face.m_48689500(),b.y+face.m_29389506(),b.z+face.m_22521050())))quads(buffer,model.m_14859469(face),b,tint);
                    quads(buffer,model.m_89748930(),b,tint);
                }if(buffer.m_34710145()==0)throw new IllegalStateException("No visible block faces were found");meshes[group]=buffer.m_68005141();}finally{buffer.m_21091689();}
            }
            GL11.glPushMatrix();try{GL11.glTranslated(group==1?finishX:startX,group==1?finishY:startY,group==1?finishZ:startZ);buffer.m_90389466(GL11.GL_QUADS,C_90362304.f_51724203);buffer.m_76351101(meshes[group]);tess.m_70070273();if(preview){int[] box=parts.bounds[group];GL11.glTranslated(box[0],box[1],box[2]);GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_CURRENT_BIT|GL11.GL_LINE_BIT);try{outline(box[3]-box[0],box[4]-box[1],box[5]-box[2]);}finally{GL11.glPopAttrib();}}}finally{GL11.glPopMatrix();}
            }lastDraw=System.nanoTime();
        }catch(RuntimeException ex){failure="Cannot display island: "+(ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage());System.err.println("[BridgeTap] "+failure);Arrays.fill(meshes,null);}
        finally{GL11.glPopMatrix();GL11.glPopClientAttrib();GL11.glPopAttrib();GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,arrayBuffer);}
    }
    private static int key(int x,int y,int z){return x<0||y<0||z<0||x>=IslandTemplate.MAX_SIDE||y>=256||z>=IslandTemplate.MAX_SIDE?-1:(x*512+y)*512+z;}
    private static void quads(C_93396801 buffer,List<C_85259637> quads,IslandTemplate.Block b,int tint){
        for(C_85259637 quad:quads){
            int[] vertices=quad.m_02641246();int stride=vertices.length/4;if(stride<6||vertices.length%4!=0)continue;
            C_28786548 face=quad.m_22904726();float shade=face==null?1:face.m_29389506()>0?1:face.m_29389506()<0?.5f:face.m_48689500()!=0?.6f:.8f;int color=quad.m_19123181()?tint:0xFFFFFF;
            for(int v=0;v<4;v++){int p=v*stride;buffer.m_07150154(b.x+Float.intBitsToFloat(vertices[p]),b.y+Float.intBitsToFloat(vertices[p+1]),b.z+Float.intBitsToFloat(vertices[p+2])).m_01677209(((color>>16)&255)/255f*shade,((color>>8)&255)/255f*shade,(color&255)/255f*shade,1).m_37170459(Float.intBitsToFloat(vertices[p+4]),Float.intBitsToFloat(vertices[p+5])).m_67536444(240,240).m_27858740();}
        }
    }
    public static void drawSelectionBox(int w,int h,int d){outline(w,h,d);}
    private static void outline(int w,int h,int d){
        GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(false);GL11.glColor4f(.35f,.95f,.95f,.8f);GL11.glLineWidth(2);
        GL11.glBegin(GL11.GL_LINES);try{for(int i=0;i<8;i++)for(int axis:new int[]{1,2,4})if((i&axis)==0){int j=i|axis;GL11.glVertex3d((i&1)==0?0:w,(i&2)==0?0:h,(i&4)==0?0:d);GL11.glVertex3d((j&1)==0?0:w,(j&2)==0?0:h,(j&4)==0?0:d);}}finally{GL11.glEnd();}
    }
}
