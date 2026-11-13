package com.bridger.momentum;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Immutable render mask. Server blocks and collision data are never modified. */
public final class IslandReplacement {
    public static final class Mask {
        final Set<Long> blocks;final int minX,minY,minZ,maxX,maxY,maxZ,surface;int attachX,attachZ,presetX,presetY,presetZ;
        final List<IslandMaskArea> areas;final Set<Long> visible=java.util.concurrent.ConcurrentHashMap.newKeySet();
        Mask(Collection<ReplayData.Block> cells,int surface){this(cells,surface,Collections.emptyList(),Collections.emptySet());}
        Mask(Collection<ReplayData.Block> cells,int surface,List<IslandMaskArea> areas,Collection<Long> visible){Set<Long> keys=new HashSet<>();int ax=30000001,ay=256,az=30000001,bx=-30000001,by=-1,bz=-30000001;for(ReplayData.Block b:cells){keys.add(b.key());ax=Math.min(ax,b.x);ay=Math.min(ay,b.y);az=Math.min(az,b.z);bx=Math.max(bx,b.x);by=Math.max(by,b.y);bz=Math.max(bz,b.z);}for(IslandMaskArea b:areas){ax=Math.min(ax,b.minX);ay=Math.min(ay,b.minY);az=Math.min(az,b.minZ);bx=Math.max(bx,b.maxX);by=Math.max(by,b.maxY);bz=Math.max(bz,b.maxZ);}blocks=Collections.unmodifiableSet(keys);minX=ax;minY=ay;minZ=az;maxX=bx;maxY=by;maxZ=bz;this.surface=surface;presetX=minX;presetY=minY;presetZ=minZ;this.areas=Collections.unmodifiableList(new ArrayList<>(areas));this.visible.addAll(visible);}
        public boolean empty(){return blocks.isEmpty()&&areas.isEmpty();}
        boolean hidden(int x,int y,int z){if(x<minX||x>maxX||y<minY||y>maxY||z<minZ||z>maxZ)return false;long key=key(x,y,z);if(visible.contains(key))return false;if(blocks.contains(key))return true;for(IslandMaskArea area:areas)if(area.contains(x,y,z))return true;return false;}
    }
    public static final Mask EMPTY=new Mask(Collections.emptyList(),0);
    private static volatile Mask current=EMPTY;
    private static volatile boolean rebuildRequested;
    private static volatile boolean focus;
    static{try(java.io.InputStream in=java.nio.file.Files.newInputStream(java.nio.file.Paths.get("config","bridgetap","island-options.properties"))){Properties p=new Properties();p.load(in);focus=Boolean.parseBoolean(p.getProperty("focus","false"));}catch(java.io.IOException|IllegalArgumentException ignored){}}
    public static boolean focus(){return focus;}
    public static boolean masking(){return !current.empty()||(focus&&IslandOverlay.enabled());}
    public static void requestTerrainRebuild(){rebuildRequested=true;}
    public static void toggleFocus(){focus=!focus;requestTerrainRebuild();try{java.nio.file.Path file=java.nio.file.Paths.get("config","bridgetap","island-options.properties");java.nio.file.Files.createDirectories(file.getParent());Properties p=new Properties();p.setProperty("focus",String.valueOf(focus));try(java.io.OutputStream out=java.nio.file.Files.newOutputStream(file)){p.store(out,"BridgeTap focus mode");}}catch(java.io.IOException ex){System.err.println("[BridgeTap] Could not save focus mode");}}
    public static boolean hidden(int x,int y,int z){Mask mask=current;if(focus&&IslandOverlay.enabled())return !mask.visible.contains(key(x,y,z))&&!PlacementAppearance.isPlaced(x,y,z);return !mask.empty()&&mask.hidden(x,y,z);}
    public static void allowPlacement(int x,int y,int z){Mask mask=current;if(mask.hidden(x,y,z)){mask.visible.add(key(x,y,z));ReplaySession.invalidateScenery(x,y,z,x,y,z);}}
    public static Mask current(){return current;}
    public static void set(Mask next){Mask old=current;if(old==next)return;current=next;rebuildRequested=true;invalidate(old);invalidate(next);}
    public static void refreshTerrain(){if(!rebuildRequested)return;Minecraft mc=Minecraft.m_20213497();if(mc==null||mc.f_26407825==null||mc.f_48619021==null)return;rebuildRequested=false;ReplaySession.rebuildSceneryTerrain();}
    private static void invalidate(Mask mask){if(!mask.empty())ReplaySession.invalidateScenery(mask.minX,mask.minY,mask.minZ,mask.maxX,mask.maxY,mask.maxZ);}
    private static long key(int x,int y,int z){return ((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|(y&4095L);}
    /** Refresh only after positioning settles; volumes also mask subsequently loaded cells. */
    public static Mask extend(Mask base,List<IslandBox> expected){
        List<IslandMaskArea> areas=new ArrayList<>(base.areas);Set<Long> visible=new HashSet<>(base.visible);visible.addAll(current.visible);Map<Long,ReplayData.Block> cells=new HashMap<>();
        for(long key:base.blocks)cells.put(key,new ReplayData.Block((int)(key>>38),(int)(key&4095),(int)(key<<26>>38),1,0,0,false));
        ReplayData data=sceneData();if(data!=null)for(ReplayData.Block b:data.blocks)if(b.placed)visible.add(b.key());
        for(IslandBox box:expected){
            if(box.y>255||box.y+box.height<=0)continue;
            int cx=box.x+box.width/2,cz=box.z+box.depth/2;Mask found=captureAt(cx,Math.min(255,box.y+box.height-1),cz);
            for(long key:found.blocks)cells.putIfAbsent(key,new ReplayData.Block((int)(key>>38),(int)(key&4095),(int)(key<<26>>38),1,0,0,false));
            IslandMaskArea area=new IslandMaskArea(found.empty()?box.x:Math.min(box.x,found.minX),found.empty()?box.y:Math.min(box.y,found.minY),found.empty()?box.z:Math.min(box.z,found.minZ),found.empty()?box.x+box.width-1:Math.max(box.x+box.width-1,found.maxX),found.empty()?box.y+box.height-1:Math.max(box.y+box.height-1,found.maxY),found.empty()?box.z+box.depth-1:Math.max(box.z+box.depth-1,found.maxZ));
            boolean covered=false;for(IslandMaskArea old:areas)if(old.covers(area)){covered=true;break;}if(!covered){areas.removeIf(area::covers);areas.add(area);}
        }
        Mask result=new Mask(cells.values(),base.surface,areas,visible);result.attachX=base.attachX;result.attachZ=base.attachZ;result.presetX=base.presetX;result.presetY=base.presetY;result.presetZ=base.presetZ;return result;
    }
    public static Mask capture(){
        Minecraft mc=Minecraft.m_20213497();C_57778754 viewer=ReplaySession.viewer();if(mc==null||viewer==null)return EMPTY;
        return captureAt((int)Math.floor(viewer.f_78826675),(int)Math.floor(viewer.f_31030949)-1,(int)Math.floor(viewer.f_97691941));
    }
    public static Mask captureStartAt(int px,int py,int pz){return captureAt(px,py,pz,true);}
    private static Mask captureAt(int px,int py,int pz){return captureAt(px,py,pz,false);}
    private static Mask captureAt(int px,int py,int pz,boolean strict){
        Minecraft mc=Minecraft.m_20213497();if(mc==null)return EMPTY;
        ReplayData data=ReplaySession.recording();Set<Long> excluded=new HashSet<>();Map<Long,ReplayData.Block> recorded=new HashMap<>();
        ReplayData recent=data!=null?data:sceneData();if(recent!=null)for(ReplayData.Block b:recent.blocks)if(b.placed)excluded.add(b.key());
        if(data!=null)for(ReplayData.Block b:data.blocks)if(!b.placed&&b.id>0&&!excluded.contains(b.key()))recorded.put(b.key(),b);
        ReplayData.Block seed=null;double score=Double.MAX_VALUE;
        int radius=strict?4:12,depth=strict?6:24;
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)for(int dy=-depth;dy<=3;dy++){
            ReplayData.Block b=cell(mc,recorded,data!=null,excluded,px+dx,py+dy,pz+dz);if(b==null)continue;double distance=dx*dx+dz*dz+dy*dy*1.5;if(distance<score){score=distance;seed=b;}
        }
        if(seed==null)return EMPTY;List<ReplayData.Block> result=new ArrayList<>();Deque<ReplayData.Block> pending=new ArrayDeque<>();Set<Long> seen=new HashSet<>();pending.add(seed);seen.add(seed.key());
        int[][] directions={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        while(!pending.isEmpty()&&result.size()<IslandTemplate.MAX_BLOCKS){ReplayData.Block b=pending.removeFirst();result.add(b);for(int[] d:directions){int x=b.x+d[0],y=b.y+d[1],z=b.z+d[2];if(Math.abs(x-seed.x)>32||Math.abs(z-seed.z)>32||Math.abs(y-seed.y)>40)continue;long key=key(x,y,z);if(!seen.add(key))continue;ReplayData.Block next=cell(mc,recorded,data!=null,excluded,x,y,z);if(next!=null)pending.addLast(next);}}
        Mask mask=new Mask(result,seed.y+1);mask.attachX=seed.x;mask.attachZ=seed.z;return mask;
    }
    public static Mask presetScene(Mask start){
        ReplayData data=sceneData();Mask scene=start;
        if(data!=null){ReplayData.Frame first=data.frames.get(0);if(first.x>=start.minX-2&&first.x<=start.maxX+3&&first.z>=start.minZ-2&&first.z<=start.maxZ+3)scene=captureScene();}
        List<ReplayData.Block> cells=new ArrayList<>();for(long key:scene.blocks)cells.add(new ReplayData.Block((int)(key>>38),(int)(key&4095),(int)(key<<26>>38),1,0,0,false));
        Mask result=new Mask(cells,start.surface,scene.areas,scene.visible);result.attachX=start.attachX;result.attachZ=start.attachZ;result.presetX=start.presetX;result.presetY=start.presetY;result.presetZ=start.presetZ;return result;
    }
    public static Mask captureScene(){
        Mask nearby=capture();ReplayData data=sceneData();if(data==null)return nearby.empty()?current:nearby;
        try{java.util.function.Predicate<ReplayData.Block> supported=b->{C_81592558 block=C_81592558.m_64320102(b.id);return block!=null&&block.m_43136364()==3;};
            List<ReplayData.Block> cells;
            try{cells=ReplayIslandSelection.both(data,supported);}catch(IllegalArgumentException missing){cells=new ArrayList<>();for(boolean finish:new boolean[]{false,true})try{cells.addAll(ReplayIslandSelection.select(data,finish,supported));}catch(IllegalArgumentException ignored){}}
            if(!ReplaySession.active()){Minecraft mc=Minecraft.m_20213497();cells.removeIf(b->{C_15815210 state=mc.f_26407825.m_77852500(new C_32594356(b.x,b.y,b.z));return C_81592558.m_63461719(state.m_41958956())!=b.id;});}
            if(cells.isEmpty())return nearby.empty()?current:nearby;
            // Endpoint anchors work even when the replay camera is far away.
            // In a live world expand the captured terrain to the full connected
            // original islands, rather than hiding only the recorded fragments.
            ReplayData.Frame first=data.frames.get(0);ReplayData.Block far=cells.get(0);double distance=-1;
            for(ReplayData.Block b:cells){double d=Math.pow(b.x-first.x,2)+Math.pow(b.z-first.z,2);if(d>distance){distance=d;far=b;}}
            Mask start=captureAt((int)Math.floor(first.x),(int)Math.floor(first.y)-1,(int)Math.floor(first.z));
            Mask end=captureAt(far.x,far.y,far.z);
            // Keep the complete nearby island mask, even when the recording only
            // contains part of it. Always hide the other recorded island too,
            // including when placing an older single-island template.
            Set<Long> covered=new HashSet<>();for(ReplayData.Block b:cells)covered.add(b.key());
            for(Mask part:new Mask[]{nearby,start,end})for(long key:part.blocks)if(covered.add(key))cells.add(new ReplayData.Block((int)(key>>38),(int)(key&4095),(int)(key<<26>>38),1,0,0,false));
            if(nearby.empty())nearby=start;
            Mask scene=new Mask(cells,nearby.empty()?(int)Math.round(data.frames.get(0).y):nearby.surface);scene.attachX=nearby.empty()?(int)Math.floor(data.frames.get(0).x):nearby.attachX;scene.attachZ=nearby.empty()?(int)Math.floor(data.frames.get(0).z):nearby.attachZ;Mask reference=start.empty()?nearby:start;if(!reference.empty()){scene.presetX=reference.presetX;scene.presetY=reference.presetY;scene.presetZ=reference.presetZ;}return scene;
        }catch(IllegalArgumentException ex){return nearby.empty()?current:nearby;}
    }
    private static ReplayData sceneData(){ReplayData replay=ReplaySession.recording();if(replay!=null)return replay;String live=PresetServers.canonical(ServerPresets.address());return !live.isEmpty()&&live.equals(PresetServers.canonical(ReplayRecorder.serverAddress()))?ReplayRecorder.snapshot():null;}
    private static ReplayData.Block cell(Minecraft mc,Map<Long,ReplayData.Block> recorded,boolean replay,Set<Long> excluded,int x,int y,int z){
        if(y<0||y>255||excluded.contains(key(x,y,z)))return null;
        if(replay)return recorded.get(key(x,y,z));
        C_32594356 pos=new C_32594356(x,y,z);if(mc.f_26407825==null||!mc.f_26407825.m_09893682(pos))return null;
        C_15815210 state=mc.f_26407825.m_77852500(pos);C_81592558 block=state.m_41958956();if(block.m_43136364()!=3)return null;
        return new ReplayData.Block(x,y,z,C_81592558.m_63461719(block),block.m_14593609(state),0,false);
    }
}
