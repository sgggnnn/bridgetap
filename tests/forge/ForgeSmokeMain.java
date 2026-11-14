package bridgetap.smoke;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.launchwrapper.Launch;

/** Resolves native targets and replay classes through the real Forge/Mixin chain. */
public final class ForgeSmokeMain {
    public static void main(String[] args) throws Exception {
        String[] targets={"net.minecraft.client.multiplayer.WorldClient", "net.minecraft.client.audio.SoundManager", "net.minecraft.client.Minecraft", "net.minecraft.entity.EntityLivingBase",
            "net.minecraft.client.gui.GuiIngame", "net.minecraft.client.entity.EntityPlayerSP",
            "net.minecraft.client.multiplayer.PlayerControllerMP", "net.minecraft.util.MovementInputFromOptions",
            "net.minecraft.entity.player.InventoryPlayer", "net.minecraft.entity.player.EntityPlayer",
            "net.minecraft.client.renderer.ItemRenderer", "net.minecraft.client.renderer.entity.RenderItem", "net.minecraft.client.renderer.RenderGlobal",
            "net.minecraft.client.renderer.entity.RenderPlayer", "net.minecraft.client.renderer.EntityRenderer", "net.minecraft.client.renderer.BlockRendererDispatcher", "net.minecraft.client.renderer.RegionRenderCache", "net.minecraft.world.ChunkCache", "net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher", "net.minecraft.item.ItemBlock", "net.minecraft.client.renderer.ItemModelMesher", "net.minecraft.client.renderer.texture.TextureMap"};
        int hooks=0;
        for(String target:targets) {
            Class<?> type=Class.forName(target,false,Launch.classLoader);
            int count=0;
            for(Method method:type.getDeclaredMethods()) if(method.getName().contains("bridgetap$")) count++;
            if(count==0) throw new AssertionError("Mixin was not applied: "+target);
            hooks+=count;
            System.out.println("PASS Forge transformed "+target+" ("+count+" BridgeTap methods)");
        }
        for(String name:Arrays.asList("ReplaySession","ReplayCamera","ReplayGhost","BridgeHistoryScreen","BridgeSettingsScreen","BridgeColorsScreen","BridgeLayoutScreen","BridgeScreen","ReplayInputOverlay","ReplayRecorder","IslandOverlay","IslandScreen","ClipCapture","ClipScreen","ReplayFolderScreen","ReplayVideoExport","ReplayVideoExportScreen","ReplayFailureScreen","StatisticsScreen","StatsTracker","ServerPresets")) {
            Class<?> type=Class.forName("com.bridger.momentum."+name,false,Launch.classLoader);
            type.getDeclaredMethods();type.getDeclaredConstructors();
        }
        Class<?> mod=Class.forName("com.bridger.momentum.forge.BridgeTapForge",false,Launch.classLoader);
        if(mod.getAnnotations().length==0)throw new AssertionError("Missing Forge mod metadata");
        Class<?> gui=Class.forName("net.minecraftforge.client.GuiIngameForge",false,Launch.classLoader);
        gui.getDeclaredMethod("func_175180_a",float.class);
        Class<?> post=Class.forName("net.minecraftforge.client.event.RenderGameOverlayEvent$Post",false,Launch.classLoader);
        Method overlay=mod.getMethod("onOverlay",post);
        if(overlay.getDeclaredAnnotations().length==0)throw new AssertionError("Forge overlay has no event subscription");
        testKeybindRestore();
        testIslandConnectedTextures();
        testSyntheticChunkUpdates();
        testReplaySceneryPaste();
        testClipFallback();
        System.out.println("PASS: Forge LaunchWrapper, bundled Mixin/ASM, "+hooks+" transformed methods and replay class linkage");
    }
    private static void testRunConfirmation(Class<?> tracker,Object world)throws Exception{
        Class<?> pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader);
        Object position=pos.getConstructor(int.class,int.class,int.class).newInstance(10,69,21);
        long packed=(Long)pos.getMethod("func_177986_g").invoke(position);
        Field field=tracker.getDeclaredField("startConfirmation");field.setAccessible(true);Object gate=field.get(null);
        Method begin=gate.getClass().getMethod("begin",Object.class,long.class,int.class,int.class);
        Field bridging=tracker.getDeclaredField("isBridging"),origin=tracker.getDeclaredField("startTick");bridging.setAccessible(true);origin.setAccessible(true);
        tracker.getMethod("reset").invoke(null);bridging.setBoolean(null,true);origin.setInt(null,42);begin.invoke(gate,world,packed,24,42);
        Method update=tracker.getMethod("onAuthoritativePlacement",Object.class,pos,int.class);
        update.invoke(null,world,position,24);
        if((Boolean)tracker.getMethod("awaitingPlacement").invoke(null)||origin.getInt(null)!=42||!bridging.getBoolean(null))throw new AssertionError("Confirmation shifted or lost the original run tick");
        begin.invoke(gate,world,packed,24,42);update.invoke(null,world,position,0);
        if(bridging.getBoolean(null)||(Boolean)tracker.getMethod("awaitingPlacement").invoke(null)||!((java.util.List<?>)tracker.getMethod("getEntries").invoke(null)).isEmpty())throw new AssertionError("Rejected ghost placement retained a run");
        System.out.println("PASS: confirmed starts retain original tick; ghost rejection cancels provisional tracking");
    }
    public static final class IslandCtmAtlas {
        private final Object module;public IslandCtmAtlas(Object module){this.module=module;}
        public Object cera$getConnectedTextures(){return module;}public Object cera$getBetterGrass(){return null;}
    }
    private static void testIslandConnectedTextures()throws Exception{
        Class.forName("net.minecraft.init.Bootstrap",true,Launch.classLoader).getMethod("func_151354_b").invoke(null);
        Class<?> block=Class.forName("net.minecraft.block.Block",true,Launch.classLoader),state=Class.forName("net.minecraft.block.state.IBlockState",true,Launch.classLoader),pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader),access=Class.forName("net.minecraft.world.IBlockAccess",true,Launch.classLoader);
        Object glass=block.getMethod("func_149729_e",int.class).invoke(null,20),glassState=block.getMethod("func_176223_P").invoke(glass);
        Class<?> cell=Class.forName("com.bridger.momentum.IslandTemplate$Block",true,Launch.classLoader),template=Class.forName("com.bridger.momentum.IslandTemplate",true,Launch.classLoader),view=Class.forName("com.bridger.momentum.IslandBlockView",true,Launch.classLoader);
        Object a=cell.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(0,0,0,"minecraft:glass",0),b=cell.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(1,0,0,"minecraft:glass",0);
        Object copied=template.getConstructor(String.class,int.class,int.class,int.class,int.class,int.class,int.class,java.util.List.class).newInstance("Connected glass",2,1,1,0,64,0,Arrays.asList(a,b));Object states=java.lang.reflect.Array.newInstance(state,2);java.lang.reflect.Array.set(states,0,glassState);java.lang.reflect.Array.set(states,1,glassState);
        Object copiedView=view.getConstructor(template,states.getClass()).newInstance(copied,states);
        Class<?> ctm=Class.forName("dev.rdh.cera.modules.ctm.ConnectedTextures",true,Launch.classLoader),adapter=Class.forName("com.bridger.momentum.IslandConnectedTextures",true,Launch.classLoader);Object module=ctm.newInstance(),helper=adapter.newInstance(),atlas=new IslandCtmAtlas(module);
        if(!(Boolean)adapter.getMethod("prepare",Object.class).invoke(helper,atlas))throw new AssertionError("Island CTM context not initialized");
        Object position=pos.getConstructor(int.class,int.class,int.class).newInstance(0,0,0);java.util.List<Object> quads=Arrays.asList((Object)null);
        Object result=adapter.getMethod("transform",access,state,pos,java.util.List.class).invoke(helper,copiedView,glassState,position,quads);
        if(result!=quads||ctm.getField("calls").getInt(null)<1||ctm.getField("lastNeighbor").getInt(null)!=20)throw new AssertionError("Island CTM ignored copied neighbouring glass");
        if((Boolean)adapter.getMethod("prepare",Object.class).invoke(helper,atlas))throw new AssertionError("CTM context rebuilt every frame");ctm.getField("transformation").set(module,new Object());if(!(Boolean)adapter.getMethod("prepare",Object.class).invoke(helper,atlas))throw new AssertionError("CTM reload missed");
        System.out.println("PASS: connected island textures use copied neighbours and cached contexts, refreshed after CTM rules reload");
    }
    private static void testIslandAutoFit(Class<?> scenery,Class<?> template,Class<?> recording,Class<?> frame,Class<?> cell,Object replay,Object previousData,Object style)throws Exception{
        java.util.List<Object> terrain=new ArrayList<>();for(int x=10;x<=14;x++)for(int z=20;z<=24;z++)terrain.add(cell.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(x,64,z,1,0,0,false));for(int x=43;x<=47;x++)for(int z=20;z<=24;z++)terrain.add(cell.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(x,67,z,1,0,0,false));terrain.add(cell.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(45,68,22,148,0,0,false));
        Object first=frame.getConstructor(double.class,double.class,double.class).newInstance(12.5,65.0,22.5),last=frame.getConstructor(double.class,double.class,double.class).newInstance(45.5,68.0,22.5),data=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(first,last),terrain,false);set(replay,"data",data);
        try{
            if(!(Boolean)scenery.getMethod("beginAutoPreview",template).invoke(null,style))throw new AssertionError("Automatic placement failed: "+scenery.getMethod("autoStatus").invoke(null));
            if((Integer)scenery.getMethod("gap").invoke(null)!=28||(Integer)scenery.getMethod("finishHeight").invoke(null)!=3||(Integer)scenery.getMethod("rotation").invoke(null)!=0)throw new AssertionError("Automatic fit did not match native distance, rise and axis");
            Class<?> masks=Class.forName("com.bridger.momentum.IslandReplacement",true,Launch.classLoader);if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,10,64,20)||!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,47,67,24))throw new AssertionError("Automatic fit left original islands visible");
            System.out.println("PASS: native automatic island placement uses recorded start, fits 28b/+3b and masks both original endpoints");
        }finally{set(replay,"data",previousData);scenery.getMethod("cancel").invoke(null);}
    }
    private static void testLiveIslandRoles(Object unsafe,Method allocate,Class<?> minecraft,Object mc,Object viewer,Class<?> session,Field current,Class<?> scenery,Class<?> template,Object style)throws Exception{
        Object oldSession=current.get(null),oldWorld=minecraft.getField("field_71441_e").get(mc),oldServer=minecraft.getMethod("func_147104_D").invoke(mc);current.set(null,null);Class<?> serverData=Class.forName("net.minecraft.client.multiplayer.ServerData",true,Launch.classLoader);set(mc,"field_71422_O",serverData.getConstructor(String.class,String.class,boolean.class).newInstance("Placement fixture","mcplayhd.net",false));
        Class<?> fixture=Class.forName("bridgetap.fixture.IslandFixtureWorld",true,Launch.classLoader),pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader),block=Class.forName("net.minecraft.block.Block",true,Launch.classLoader);Object world=allocate.invoke(unsafe,fixture);java.util.Map<Long,Object> cells=new java.util.HashMap<>();fixture.getField("cells").set(world,cells);
        Object stone=block.getMethod("func_176223_P").invoke(block.getMethod("func_149729_e",int.class).invoke(null,1)),iron=block.getMethod("func_176223_P").invoke(block.getMethod("func_149729_e",int.class).invoke(null,148));
        for(int x=10;x<=14;x++)for(int z=20;z<=24;z++){Object p=pos.getConstructor(int.class,int.class,int.class).newInstance(x,64,z);cells.put((Long)pos.getMethod("func_177986_g").invoke(p),stone);}
        for(int x=43;x<=47;x++)for(int z=20;z<=24;z++){Object p=pos.getConstructor(int.class,int.class,int.class).newInstance(x,67,z);cells.put((Long)pos.getMethod("func_177986_g").invoke(p),stone);}Object plate=pos.getConstructor(int.class,int.class,int.class).newInstance(45,68,22);cells.put((Long)pos.getMethod("func_177986_g").invoke(plate),iron);
        for(int xx=14;xx<=16;xx++)for(int zz=21;zz<=23;zz++){Object p=pos.getConstructor(int.class,int.class,int.class).newInstance(xx,63,zz);cells.put((Long)pos.getMethod("func_177986_g").invoke(p),stone);}
        set(mc,"field_71441_e",world);set(viewer,"field_70165_t",12.5);set(viewer,"field_70163_u",65.5);set(viewer,"field_70161_v",22.5);Class<?> sourceRecorder=Class.forName("com.bridger.momentum.ReplayRecorder",true,Launch.classLoader);sourceRecorder.getMethod("reset").invoke(null);Object lowClick=pos.getConstructor(int.class,int.class,int.class).newInstance(15,63,22);for(Method method:sourceRecorder.getMethods())if(method.getName().equals("sourceAnchor"))method.invoke(null,world,lowClick);

        try{
            Object slabPos=pos.getConstructor(int.class,int.class,int.class).newInstance(12,65,22);long slabKey=(Long)pos.getMethod("func_177986_g").invoke(slabPos);Object slab=block.getMethod("func_176223_P").invoke(block.getMethod("func_149729_e",int.class).invoke(null,44));cells.put(slabKey,slab);set(viewer,"field_70122_E",true);
            Class<?> fitter=Class.forName("com.bridger.momentum.IslandAutoFit",true,Launch.classLoader);Method detect=fitter.getDeclaredMethod("detect",template);detect.setAccessible(true);Object halfFit=detect.invoke(null,style);Field startField=halfFit.getClass().getDeclaredField("start");startField.setAccessible(true);if(startField.get(halfFit).getClass().getField("y").getInt(startField.get(halfFit))!=65)throw new AssertionError("Half-block decoration raised the native deck by one block");cells.remove(slabKey);Method sameBridge=fitter.getDeclaredMethod("sameBridgeNearViewer");sameBridge.setAccessible(true);if(!(Boolean)sameBridge.invoke(null))throw new AssertionError("Leaving the slab invalidated the corrected height");Field fitCache=fitter.getDeclaredField("cached");fitCache.setAccessible(true);fitCache.set(null,null);
            System.out.println("PASS: native slab at feet 65.5 retains the broad deck at 65");
            if(!(Boolean)scenery.getMethod("beginAutoPreview",template).invoke(null,style))throw new AssertionError("Live initial fit: "+scenery.getMethod("autoStatus").invoke(null));scenery.getMethod("confirm").invoke(null);if((Integer)scenery.getMethod("bridgeHeight").invoke(null)!=3)throw new AssertionError("Low clicked ledge overrode the main floor: "+scenery.getMethod("autoStatus").invoke(null));
            // Exercise the real automatic preset path, not the same-footprint no-op.
            Object savedLayout=scenery.getMethod("layout").invoke(null);Class<?> layoutType=savedLayout.getClass();Method depthMethod=scenery.getDeclaredMethod("nativeDepth");depthMethod.setAccessible(true);int savedDepth=(Integer)depthMethod.invoke(null);int initialY=(Integer)scenery.getMethod("sceneY").invoke(null);Class<?> anchors=Class.forName("com.bridger.momentum.IslandStartAnchors",true,Launch.classLoader);
            cells.put(slabKey,slab);
            for(double feet:new double[]{65.5,65.0,65.9,65.5,65.0}){set(viewer,"field_70163_u",feet);anchors.getMethod("mark").invoke(null);scenery.getMethod("applySurfacePreset",template,layoutType,int.class).invoke(null,style,savedLayout,savedDepth);if((Integer)scenery.getMethod("sceneY").invoke(null)!=initialY)throw new AssertionError("Initial placement and automatic reapply disagree at feet="+feet);savedLayout=scenery.getMethod("layout").invoke(null);savedDepth=(Integer)depthMethod.invoke(null);}
            cells.remove(slabKey);set(viewer,"field_70163_u",65.5);
            System.out.println("PASS: initial placement and five real preset reapplications retain identical height across slab/full-floor respawns");
            int[] expected=new int[7];String[] fields={"x","y","z","finishX","finishY","finishZ","rotation"};for(int k=0;k<fields.length;k++){Field f=scenery.getDeclaredField(fields[k]);f.setAccessible(true);expected[k]=f.getInt(null);}
            set(viewer,"field_70165_t",45.5);set(viewer,"field_70163_u",68.0);set(viewer,"field_70161_v",21.5);
            if(!(Boolean)scenery.getMethod("beginAutoPreview",template).invoke(null,style))throw new AssertionError("Finish observer off the plate: "+scenery.getMethod("autoStatus").invoke(null));for(int k=0;k<fields.length;k++){Field f=scenery.getDeclaredField(fields[k]);f.setAccessible(true);if(f.getInt(null)!=expected[k])throw new AssertionError("Start/finish swapped from finish-side viewing");}scenery.getMethod("confirm").invoke(null);
            for(int x=10;x<=11;x++)for(int z=20;z<=24;z++){Object p=pos.getConstructor(int.class,int.class,int.class).newInstance(x,65,z);cells.put((Long)pos.getMethod("func_177986_g").invoke(p),stone);}Object raised=pos.getConstructor(int.class,int.class,int.class).newInstance(13,65,22);cells.put((Long)pos.getMethod("func_177986_g").invoke(raised),stone);
            set(viewer,"field_70165_t",12.5);set(viewer,"field_70163_u",66.2);set(viewer,"field_70161_v",22.5);
            if(!(Boolean)scenery.getMethod("beginAutoPreview",template).invoke(null,style))throw new AssertionError("Decorated live deck: "+scenery.getMethod("autoStatus").invoke(null));for(int k=0;k<fields.length;k++){Field f=scenery.getDeclaredField(fields[k]);f.setAccessible(true);if(f.getInt(null)!=expected[k])throw new AssertionError("Decoration shifted the automatic placement by a block");}scenery.getMethod("confirm").invoke(null);
            Class<?> randomizer=Class.forName("com.bridger.momentum.AttemptRandomizer",true,Launch.classLoader),runTracker=Class.forName("com.bridger.momentum.BridgeTracker",true,Launch.classLoader);
            randomizer.getMethod("toggleBlocks",String.class).invoke(null,"");randomizer.getMethod("toggleIslands",String.class).invoke(null,"defaults");randomizer.getMethod("toggleReapply").invoke(null);
            Field scheduleField=randomizer.getDeclaredField("schedule");scheduleField.setAccessible(true);Object schedule=scheduleField.get(null);Class<?> scheduleType=schedule.getClass();
            Field bridging=runTracker.getDeclaredField("isBridging");bridging.setAccessible(true);Field confirmationField=runTracker.getDeclaredField("startConfirmation");confirmationField.setAccessible(true);Object confirmation=confirmationField.get(null);Object firstPos=pos.getConstructor(int.class,int.class,int.class).newInstance(15,64,22);long packed=(Long)pos.getMethod("func_177986_g").invoke(firstPos);
            bridging.setBoolean(null,true);confirmation.getClass().getMethod("begin",Object.class,long.class,int.class,int.class).invoke(confirmation,world,packed,1,0);runTracker.getMethod("onAuthoritativePlacement",Object.class,pos,int.class).invoke(null,world,firstPos,0);
            if((Boolean)scheduleType.getMethod("pending").invoke(schedule))throw new AssertionError("Ghost block queued appearance changes");
            bridging.setBoolean(null,true);confirmation.getClass().getMethod("begin",Object.class,long.class,int.class,int.class).invoke(confirmation,world,packed,1,0);runTracker.getMethod("onAuthoritativePlacement",Object.class,pos,int.class).invoke(null,world,firstPos,1);
            randomizer.getMethod("tick",minecraft).invoke(null,mc);if((Boolean)scheduleType.getMethod("pending").invoke(schedule)||scenery.getMethod("activeTemplate").invoke(null)!=style)throw new AssertionError("First placement counted as a completed attempt");
            randomizer.getMethod("completed",String.class).invoke(null,"finished-1");randomizer.getMethod("completed",String.class).invoke(null,"finished-1");
            if((Integer)scheduleType.getMethod("count").invoke(schedule)!=0)throw new AssertionError("Duplicate completion counted twice");
            randomizer.getMethod("tick",minecraft).invoke(null,mc);if(scenery.getMethod("activeTemplate").invoke(null)!=style)throw new AssertionError("Island changed during an active run");
            bridging.setBoolean(null,false);set(viewer,"field_70122_E",false);randomizer.getMethod("tick",minecraft).invoke(null,mc);if(scenery.getMethod("activeTemplate").invoke(null)!=style)throw new AssertionError("Island changed before landing");
            set(viewer,"field_70122_E",true);set(viewer,"field_70165_t",12.5);set(viewer,"field_70163_u",65.0);set(viewer,"field_70161_v",22.5);randomizer.getMethod("tick",minecraft).invoke(null,mc);
            Object chosenTemplate=scenery.getMethod("activeTemplate").invoke(null);Class<?> appearance=Class.forName("com.bridger.momentum.PlacementAppearance",true,Launch.classLoader);String chosenBlock=(String)appearance.getMethod("block").invoke(null);
            if(chosenTemplate==style)throw new AssertionError("Random island did not change after completion/respawn");randomizer.getMethod("tick",minecraft).invoke(null,mc);
            if(chosenTemplate!=scenery.getMethod("activeTemplate").invoke(null)||!chosenBlock.equals(appearance.getMethod("block").invoke(null)))throw new AssertionError("Random appearances changed twice for one completion");
            randomizer.getMethod("toggleBlocks",String.class).invoke(null,"");randomizer.getMethod("toggleIslands",String.class).invoke(null,"defaults");randomizer.getMethod("toggleReapply").invoke(null);randomizer.getMethod("toggleReapply").invoke(null);
            if(!(Boolean)scenery.getMethod("reapplyAutomatically").invoke(null))throw new AssertionError("Manual reference auto fit failed");
            String[] lockedFields={"x","y","z","startX","startY","startZ","finishX","finishY","finishZ","rotation"};int[] manual=new int[lockedFields.length];for(int k=0;k<manual.length;k++){Field f=scenery.getDeclaredField(lockedFields[k]);f.setAccessible(true);manual[k]=f.getInt(null);}
            scenery.getMethod("move",int.class,int.class,int.class).invoke(null,3,1,-2);
            randomizer.getMethod("completed",String.class).invoke(null,"present-1");randomizer.getMethod("tick",minecraft).invoke(null,mc);
            if((Integer)scenery.getMethod("sceneY").invoke(null)!=manual[1]+1)throw new AssertionError("Existing island was reapplied after completion");
            Object respawnWorld=allocate.invoke(unsafe,fixture);fixture.getField("cells").set(respawnWorld,new java.util.HashMap<>(cells));
            randomizer.getMethod("completed",String.class).invoke(null,"missing-2");set(mc,"field_71441_e",respawnWorld);scenery.getMethod("tick").invoke(null);
            if((Boolean)scenery.getMethod("enabled").invoke(null))throw new AssertionError("World change left an island displayed");
            randomizer.getMethod("tick",minecraft).invoke(null,mc);
            if(!(Boolean)scenery.getMethod("enabled").invoke(null))throw new AssertionError("Missing island was not reapplied after one completed attempt");
            for(int k=0;k<manual.length;k++){Field f=scenery.getDeclaredField(lockedFields[k]);f.setAccessible(true);if(f.getInt(null)!=manual[k])throw new AssertionError("Missing-island apply differs from manual action: "+lockedFields[k]);}
            scenery.getMethod("move",int.class,int.class,int.class).invoke(null,0,1,0);randomizer.getMethod("tick",minecraft).invoke(null,mc);if((Integer)scenery.getMethod("sceneY").invoke(null)!=manual[1]+1)throw new AssertionError("Idle tick reapplied an existing island");
            set(mc,"field_71441_e",world);
            randomizer.getMethod("toggleReapply").invoke(null);randomizer.getMethod("completed",String.class).invoke(null,"disabled-1");randomizer.getMethod("tick",minecraft).invoke(null,mc);if((Boolean)scheduleType.getMethod("pending").invoke(schedule))throw new AssertionError("Disabled features still scheduled work");
            System.out.println("PASS: placements never count; completed attempts randomize once after landing; each completion applies only missing islands using the manual transform; idle/disabled actions stay unchanged");
            Class<?> aimCapture=Class.forName("com.bridger.momentum.FailAimCapture",true,Launch.classLoader),placementHooks=Class.forName("com.bridger.momentum.PlacementHooks",true,Launch.classLoader),facingType=Class.forName("net.minecraft.util.EnumFacing",true,Launch.classLoader);aimCapture.getMethod("reset").invoke(null);Field appearanceEnabled=appearance.getDeclaredField("enabled");appearanceEnabled.setAccessible(true);boolean previousAppearance=appearanceEnabled.getBoolean(null);appearanceEnabled.setBoolean(null,false);bridging.setBoolean(null,true);Object supportPos=pos.getConstructor(int.class,int.class,int.class).newInstance(14,64,22),newPos=pos.getConstructor(int.class,int.class,int.class).newInstance(15,64,22);Object east=facingType.getField("EAST").get(null);for(Method hook:placementHooks.getMethods())if(hook.getName().equals("capture"))hook.invoke(null,world,supportPos,east,null);cells.put((Long)pos.getMethod("func_177986_g").invoke(newPos),stone);for(Method hook:placementHooks.getMethods())if(hook.getName().equals("finish"))hook.invoke(null,world,viewer);if(!((String)aimCapture.getMethod("summary").invoke(null)).contains("No placement input"))throw new AssertionError("Real placement pipeline did not seed live fail evidence");appearanceEnabled.setBoolean(null,previousAppearance);bridging.setBoolean(null,false);System.out.println("PASS: actual placement capture/finish pipeline seeds fail evidence instead of reporting missing data");
            Class<?> runtime=Class.forName("com.bridger.momentum.BridgeRuntime",true,Launch.classLoader),config=Class.forName("com.bridger.momentum.BridgeConfig",true,Launch.classLoader),tracker=Class.forName("com.bridger.momentum.BridgeTracker",true,Launch.classLoader);boolean hud=config.getField("enabled").getBoolean(null);runtime.getMethod("setEnabled",boolean.class).invoke(null,false);tracker.getMethod("onBlockPlaced").invoke(null);if(config.getField("modEnabled").getBoolean(null)||(Boolean)scenery.getMethod("enabled").invoke(null)||(Boolean)tracker.getMethod("isBridging").invoke(null))throw new AssertionError("Master switch left tracking or islands enabled");runtime.getMethod("setEnabled",boolean.class).invoke(null,true);if(config.getField("enabled").getBoolean(null)!=hud)throw new AssertionError("Master switch reset per-feature preference");
            System.out.println("PASS: live finish off-plate retains start/end roles; walls and raised seeds do not move placement; master switch stops tracking/islands and preserves settings");
        }finally{scenery.getMethod("disable").invoke(null);set(mc,"field_71441_e",oldWorld);set(mc,"field_71422_O",oldServer);current.set(null,oldSession);}
    }
    private static void testReplayChat(Object unsafe,Method allocate,Class<?> minecraft,Object mc)throws Exception{
        Class<?> guiType=Class.forName("net.minecraft.client.gui.GuiIngame",true,Launch.classLoader),chatType=Class.forName("net.minecraft.client.gui.GuiNewChat",true,Launch.classLoader);
        Object live=allocate.invoke(unsafe,guiType),replay=allocate.invoke(unsafe,guiType),chat=chatType.getConstructor(minecraft).newInstance(mc);
        set(live,"field_73840_e",chat);set(live,"field_73837_f",1234);set(replay,"field_73840_e",chatType.getConstructor(minecraft).newInstance(mc));
        Class<?> helper=Class.forName("com.bridger.momentum.ReplayLiveChat",true,Launch.classLoader);Method sync=helper.getMethod("sync",guiType,guiType);
        java.util.List history=(java.util.List)chatType.getMethod("func_146238_c").invoke(chat);history.add("/msg friend hello");
        sync.invoke(null,replay,live);
        if(guiType.getMethod("func_146158_b").invoke(replay)!=chat||(Integer)guiType.getMethod("func_73834_c").invoke(replay)!=1234)throw new AssertionError("Replay did not share live chat and clock");
        Field scroll=chatType.getDeclaredField("field_146250_j");scroll.setAccessible(true);scroll.setInt(chat,4);
        history.add("A new live command");set(live,"field_73837_f",1300);sync.invoke(null,replay,live);
        if(scroll.getInt(chat)!=4||history.size()!=2||(Integer)guiType.getMethod("func_73834_c").invoke(replay)!=1300)throw new AssertionError("Replay pause/seek reset real chat history, scroll or message age");
        if(guiType.getMethod("func_146158_b").invoke(live)!=chat)throw new AssertionError("Live GUI changed");
        System.out.println("PASS: actual live chat widget, live clock, new history entries and scrolling survive replay rendering");
    }
    private static void testClipFallback()throws Exception{
        Class<?> capture=Class.forName("com.bridger.momentum.ClipCapture",true,Launch.classLoader);capture.getMethod("toggle").invoke(null);
        Method fail=capture.getDeclaredMethod("captureFailed",Throwable.class,boolean.class,boolean.class);fail.setAccessible(true);
        fail.invoke(null,new NoSuchMethodError("Simulated incompatible async capture API"),true,true);
        if(!(Boolean)capture.getMethod("enabled").invoke(null))throw new AssertionError("Async linkage failure switched the buffer OFF instead of recovering");
        fail.invoke(null,new NoSuchMethodError("Simulated incompatible framebuffer capture API"),false,true);
        if(!(Boolean)capture.getMethod("enabled").invoke(null))throw new AssertionError("Framebuffer linkage failure switched the buffer OFF instead of recovering");
        fail.invoke(null,new IllegalStateException("Simulated unavailable legacy capture"),false,false);
        if((Boolean)capture.getMethod("enabled").invoke(null))throw new AssertionError("Final capture failure must be reported instead of pretending to record");
        System.out.println("PASS: reproduced NoSuchMethodError recovery, buffer stays ON through GPU fallbacks and reports final failures");
    }
    private static void testJumpDetails(Class<?> tracker,Class<?> minecraft,Object mc,Class<?> playerType,Object player,Method snapshot,Field tick)throws Exception{
        Class<?> config=Class.forName("com.bridger.momentum.BridgeConfig",true,Launch.classLoader);
        config.getField("tickOffset").setInt(null,1);config.getField("preferredAxis").set(null,"Z");
        set(player,"field_70161_v",23.289);tick.setInt(null,100);snapshot.invoke(null,player,100);tracker.getMethod("onTickEnd",minecraft).invoke(null,mc);
        tick.setInt(null,101);set(player,"field_70177_z",541f);
        Field active=tracker.getDeclaredField("isBridging");active.setAccessible(true);active.setBoolean(null,true);
        tracker.getMethod("onJump",Class.forName("net.minecraft.entity.Entity",false,Launch.classLoader)).invoke(null,player);
        tick.setInt(null,102);set(player,"field_70161_v",23.584);set(player,"field_70177_z",90f);snapshot.invoke(null,player,102);
        Method pending=tracker.getDeclaredMethod("processPendingSamples");pending.setAccessible(true);pending.invoke(null);
        java.util.List<?> entries=(java.util.List<?>)tracker.getMethod("getEntries").invoke(null);Object first=entries.get(0);Class<?> entry=first.getClass();
        if(Math.abs((Double)entry.getMethod("getJumpYaw").invoke(first)+179)>1e-6||Math.abs((Double)entry.getMethod("getPreJumpZ").invoke(first)-23.289)>1e-6)throw new AssertionError("Delayed coordinates changed jump yaw or prior-tick Z");
        if(Math.abs((Double)entry.getMethod("getSampledX").invoke(first)-10.0)>1e-6||Math.abs((Double)entry.getMethod("getSampledZ").invoke(first)-23.584)>1e-6)throw new AssertionError("Both axes did not use the same offset-adjusted sample");
        for(int t:new int[]{111,114}){set(player,"field_70161_v",t==111?-24.123:-25.456);snapshot.invoke(null,player,t);}
        tick.setInt(null,115);Method fail=tracker.getDeclaredMethod("onFailSpeed",double.class,String.class,double.class);fail.setAccessible(true);fail.invoke(null,0.0,"Test",-1.0);
        entries=(java.util.List<?>)tracker.getMethod("getEntries").invoke(null);
        if(entries.size()!=3)throw new AssertionError("Expected both fail timing samples");
        for(int n=1;n<=2;n++){
            Object e=entries.get(n);int expected=n==1?9:12;double coordinate=n==1?-24.123:-25.456;
            if(!"Fail Tap/4b".equals(entry.getMethod("getLabel").invoke(e))||(Integer)entry.getMethod("getTicksElapsed").invoke(e)!=expected||Math.abs((Double)entry.getMethod("getCoordinate").invoke(e)-coordinate)>1e-6)throw new AssertionError("Wrong fail coordinate/timing at "+expected);
        }
        active.setBoolean(null,true);tick.setInt(null,111);fail.invoke(null,0.0,"Test",-1.0);
        entries=(java.util.List<?>)tracker.getMethod("getEntries").invoke(null);
        if(entries.size()!=4||(Integer)entry.getMethod("getTicksElapsed").invoke(entries.get(3))!=9)throw new AssertionError("Unavailable 12-tick fail was fabricated");
        fail.invoke(null,0.0,"Duplicate",-1.0);if(((java.util.List<?>)tracker.getMethod("getEntries").invoke(null)).size()!=4)throw new AssertionError("Repeated fail duplicated samples");
        tracker.getMethod("reset").invoke(null);config.getField("tickOffset").setInt(null,0);config.getField("preferredAxis").set(null,"AUTO");
        System.out.println("PASS: jump-event yaw survives delayed sampling; fail coordinates use exact offset-adjusted 9t and 12t snapshots");
    }
    private static void testKeybindRestore()throws Exception{
        Class<?> unsafeType=Class.forName("sun.misc.Unsafe");Field singleton=unsafeType.getDeclaredField("theUnsafe");singleton.setAccessible(true);Object unsafe=singleton.get(null);Method allocate=unsafeType.getMethod("allocateInstance",Class.class);
        Class<?> settings=Class.forName("net.minecraft.client.settings.GameSettings",true,Launch.classLoader),binding=Class.forName("net.minecraft.client.settings.KeyBinding",true,Launch.classLoader),helper=Class.forName("com.bridger.momentum.BridgeKeyBindings",true,Launch.classLoader);
        java.nio.file.Path file=java.nio.file.Files.createTempFile("bridgetap-key-options-",".txt");java.nio.file.Files.write(file,Arrays.asList("key_key.bridgetap.openSettings:23","key_key.bridgetap.editReplayInputs:0","key_key.bridgetap.saveClip:-99","key_key.forward:99"),java.nio.charset.StandardCharsets.UTF_8);
        for(int launch=0;launch<2;launch++){Object options=allocate.invoke(unsafe,settings),vanilla=binding.getConstructor(String.class,int.class,String.class).newInstance("key.forward",17,"key.categories.movement");Object array=java.lang.reflect.Array.newInstance(binding,1);java.lang.reflect.Array.set(array,0,vanilla);set(options,"field_74324_K",array);
            Object[] restored=(Object[])helper.getMethod("register",settings,java.nio.file.Path.class).invoke(null,options,file);int[] codes={23,0,-99,0};for(int j=0;j<4;j++)if((Integer)binding.getMethod("func_151463_i").invoke(restored[j])!=codes[j])throw new AssertionError("Saved binding reset at restart");
            helper.getMethod("register",settings,java.nio.file.Path.class).invoke(null,options,file);Field all=settings.getField("field_74324_K");if(java.lang.reflect.Array.getLength(all.get(options))!=5||(Integer)binding.getMethod("func_151463_i").invoke(vanilla)!=17)throw new AssertionError("Registration duplicated keys or reloaded another mod's options");
        }System.out.println("PASS: Forge restores changed, mouse and unbound shortcuts across startup without duplicating bindings or resetting other options");
    }
    private static void testIncrementalAppearance(Object unsafe,Method allocate,Class<?> appearance)throws Exception{
        Class<?> replacement=Class.forName("com.bridger.momentum.IslandReplacement",true,Launch.classLoader);Field rebuild=replacement.getDeclaredField("rebuildRequested");rebuild.setAccessible(true);rebuild.setBoolean(null,false);appearance.getMethod("applyAttempt",String.class).invoke(null,"minecraft:stone");if(rebuild.getBoolean(null))throw new AssertionError("Random block requested a full terrain rebuild");
        Class<?> sounds=Class.forName("com.bridger.momentum.ModernSounds",true,Launch.classLoader),handler=Class.forName("net.minecraft.client.audio.SoundHandler",true,Launch.classLoader),registry=Class.forName("net.minecraft.client.audio.SoundRegistry",true,Launch.classLoader),location=Class.forName("net.minecraft.util.ResourceLocation",true,Launch.classLoader);Object audio=allocate.invoke(unsafe,handler);set(audio,"field_147697_e",registry.getConstructor().newInstance());
        Field definitions=sounds.getDeclaredField("definitions");definitions.setAccessible(true);Object object=definitions.get(null);Class<?> parser=Class.forName("com.google.gson.JsonParser",true,Launch.classLoader),element=Class.forName("com.google.gson.JsonElement",true,Launch.classLoader);Object definition=parser.getMethod("parse",String.class).invoke(parser.getConstructor().newInstance(),"{\"sounds\":[],\"category\":\"block\",\"replace\":true}");object.getClass().getMethod("add",String.class,element).invoke(object,"modern.fixture.place",definition);
        sounds.getMethod("registerDynamic",Object.class).invoke(null,audio);Object key=location.getConstructor(String.class).newInstance("bridgetap:modern.fixture.place");if(handler.getMethod("func_147680_a",location).invoke(audio,key)==null)throw new AssertionError("Dynamic sound registration failed");sounds.getMethod("registerDynamic",Object.class).invoke(null,audio);
        object.getClass().getMethod("remove",String.class).invoke(object,"modern.fixture.place");System.out.println("PASS: random block invalidates placed regions without full terrain rebuild; dynamic sound registration works without an audio engine restart");
    }
    private static void testPlacementAudio(Object unsafe,Method allocate,Object mc,Class<?> appearance)throws Exception{
        Class<?> handler=Class.forName("bridgetap.fixture.RecordingSoundHandler",true,Launch.classLoader),pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader),sounds=Class.forName("com.bridger.momentum.ModernSounds",true,Launch.classLoader);
        Object recorder=allocate.invoke(unsafe,handler);set(mc,"field_147127_av",recorder);Field block=appearance.getDeclaredField("block"),ready=sounds.getDeclaredField("ready");block.setAccessible(true);ready.setAccessible(true);String previous=(String)block.get(null);block.set(null,"modern:amethyst_block");java.util.Set loaded=(java.util.Set)ready.get(null);loaded.add("block.amethyst_block.place");
        try{for(int i=0;i<4;i++)appearance.getMethod("playPlacement",pos).invoke(null,pos.getConstructor(int.class,int.class,int.class).newInstance(i,70,0));Object last=handler.getField("last").get(recorder);String event=Class.forName("net.minecraft.client.audio.ISound",true,Launch.classLoader).getMethod("func_147650_b").invoke(last).toString();if(handler.getField("count").getInt(recorder)!=4||!event.equals("bridgetap:modern.block.amethyst_block.place"))throw new AssertionError("Repeated placements lost their amethyst sound: "+event);Object cell=pos.getConstructor(int.class,int.class,int.class).newInstance(0,70,0);appearance.getMethod("track",pos,int.class).invoke(null,cell,24);Class<?> blocks=Class.forName("net.minecraft.block.Block",true,Launch.classLoader),location=Class.forName("net.minecraft.util.ResourceLocation",true,Launch.classLoader),record=Class.forName("net.minecraft.client.audio.PositionedSoundRecord",true,Launch.classLoader),isound=Class.forName("net.minecraft.client.audio.ISound",true,Launch.classLoader);Object actual=blocks.getMethod("func_149729_e",int.class).invoke(null,24),type=blocks.getField("field_149762_H").get(actual);String base=(String)type.getClass().getMethod("func_150496_b").invoke(type);Object echo=record.getConstructor(location,float.class,float.class,float.class,float.class,float.class).newInstance(location.getConstructor(String.class).newInstance(base),1f,.8f,.5f,70.5f,.5f),remote=record.getConstructor(location,float.class,float.class,float.class,float.class,float.class).newInstance(location.getConstructor(String.class).newInstance(base),1f,.8f,9.5f,70.5f,.5f);Method suppress=appearance.getMethod("suppressOriginal",isound);if(!(Boolean)suppress.invoke(null,echo)||(Boolean)suppress.invoke(null,remote)||(Boolean)suppress.invoke(null,last))throw new AssertionError("Original placement echo was not isolated from selected/remote sounds");}
        finally{block.set(null,previous);loaded.remove("block.amethyst_block.place");set(mc,"field_147127_av",null);}
        System.out.println("PASS: every confirmed placement sends the selected amethyst event without limiting placement cadence; no device opened");
    }
    private static void testHudPreparation(Object unsafe,Method allocate)throws Exception{
        Class<?> hud=Class.forName("com.bridger.momentum.BridgeHUD",true,Launch.classLoader),font=Class.forName("bridgetap.fixture.CountingFontRenderer",true,Launch.classLoader),nativeFont=Class.forName("net.minecraft.client.gui.FontRenderer",true,Launch.classLoader),tracker=Class.forName("com.bridger.momentum.BridgeTracker",true,Launch.classLoader),config=Class.forName("com.bridger.momentum.BridgeConfig",true,Launch.classLoader);Object fr=allocate.invoke(unsafe,font);Field bridging=tracker.getDeclaredField("isBridging"),mode=config.getField("displayMode"),epoch=hud.getDeclaredField("hudEpoch");bridging.setAccessible(true);epoch.setAccessible(true);boolean old=bridging.getBoolean(null);Object previous=mode.get(null);bridging.setBoolean(null,true);mode.set(null,"ALL");Method cached=hud.getDeclaredMethod("cachedHud",nativeFont);cached.setAccessible(true);
        try{Object first=cached.invoke(null,fr);int count=font.getField("calls").getInt(fr);if(first==null||count<1)throw new AssertionError("HUD preparation did not measure content");for(int i=0;i<1000;i++)if(cached.invoke(null,fr)!=first)throw new AssertionError("HUD reformatted between ticks");if(font.getField("calls").getInt(fr)!=count)throw new AssertionError("Repeated HUD frames rescanned text widths");epoch.setLong(null,epoch.getLong(null)+1);if(cached.invoke(null,fr)==first||font.getField("calls").getInt(fr)<=count)throw new AssertionError("HUD cache missed next tick invalidation");}finally{bridging.setBoolean(null,old);mode.set(null,previous);epoch.setLong(null,epoch.getLong(null)+1);}System.out.println("PASS: 1,000 repeated HUD frames reuse formatting and widths; next client tick refreshes live coordinates");
    }
    private static void testEarlyAppearance(Class<?> appearance,Object block)throws Exception{
        Class<?> pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader),state=Class.forName("net.minecraft.block.state.IBlockState",true,Launch.classLoader),blocks=Class.forName("net.minecraft.block.Block",true,Launch.classLoader);Object hit=pos.getConstructor(int.class,int.class,int.class).newInstance(140,70,140),target=pos.getConstructor(int.class,int.class,int.class).newInstance(140,71,140),stone=blocks.getMethod("func_176223_P").invoke(block),air=blocks.getMethod("func_176223_P").invoke(blocks.getMethod("func_149729_e",int.class).invoke(null,0));Method reserve=appearance.getMethod("reserve",pos,int.class,pos,int.class,int.class),matches=appearance.getMethod("matches",pos,state);reserve.invoke(null,hit,24,target,0,24);
        if((Boolean)matches.invoke(null,hit,stone)||!(Boolean)matches.invoke(null,target,stone)||(Boolean)matches.invoke(null,target,air))throw new AssertionError("Early tracking paints existing/air blocks or misses initial mesh");appearance.getMethod("endPlacement").invoke(null);if((Boolean)matches.invoke(null,target,stone))throw new AssertionError("Failed placement left a visible reservation");reserve.invoke(null,hit,24,target,0,24);appearance.getMethod("track",pos,int.class).invoke(null,target,24);appearance.getMethod("endPlacement").invoke(null);if(!(Boolean)matches.invoke(null,target,stone))throw new AssertionError("Confirmed placement was lost after reservation commit");System.out.println("PASS: first mesh sees the future placement without a second terrain rebuild; failed reservations and existing neighbours remain unchanged");
    }
    private static void testConnectedAppearance(Class<?> appearance,Object block)throws Exception{
        Class<?> ctm=Class.forName("dev.rdh.cera.modules.ctm.ConnectedTextures",true,Launch.classLoader),ctx=Class.forName("dev.rdh.cera.modules.ctm.CtmRenderContext",true,Launch.classLoader),pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader);Object position=pos.getConstructor(int.class,int.class,int.class).newInstance(42,70,42),state=block.getClass().getMethod("func_176223_P").invoke(block);Object transform=ctm.getConstructor().newInstance();Method method=null;for(Method m:ctm.getMethods())if(m.getName().equals("transform"))method=m;java.util.List quads=new ArrayList();quads.add(null);
        if(method.invoke(transform,null,state,position,quads,new ArrayList(),ctx.getConstructor().newInstance())==quads)throw new AssertionError("Unrelated CTM rules were disabled");appearance.getMethod("track",pos,int.class).invoke(null,position,24);if(method.invoke(transform,null,state,position,quads,new ArrayList(),ctx.getConstructor().newInstance())!=quads)throw new AssertionError("Neighbour CTM overwrote the placed appearance");System.out.println("PASS: reproduced connected-texture replacement; own placed quads survive while other CTM blocks stay unchanged");
    }
    private static void testAppearanceItems(Object unsafe,Method allocate,Class<?> minecraft,Object mc,Class<?> playerType,Object player)throws Exception{
        Class<?> blocks=Class.forName("net.minecraft.block.Block",true,Launch.classLoader),stackType=Class.forName("net.minecraft.item.ItemStack",true,Launch.classLoader),itemType=Class.forName("net.minecraft.item.Item",true,Launch.classLoader),inventory=Class.forName("net.minecraft.entity.player.InventoryPlayer",true,Launch.classLoader),appearance=Class.forName("com.bridger.momentum.PlacementAppearance",true,Launch.classLoader),mesherType=Class.forName("net.minecraft.client.renderer.ItemModelMesher",true,Launch.classLoader),managerType=Class.forName("net.minecraft.client.resources.model.ModelManager",true,Launch.classLoader),modelType=Class.forName("net.minecraft.client.resources.model.IBakedModel",true,Launch.classLoader);
        Object inv=allocate.invoke(unsafe,inventory),main=java.lang.reflect.Array.newInstance(stackType,36),block=blocks.getMethod("func_149729_e",int.class).invoke(null,24),wood=blocks.getMethod("func_149729_e",int.class).invoke(null,5),stack=stackType.getConstructor(blocks,int.class,int.class).newInstance(block,64,0);java.lang.reflect.Array.set(main,0,stack);set(inv,"field_70462_a",main);set(inv,"field_70461_c",0);set(player,"field_71071_by",inv);
        Object original=Proxy.newProxyInstance(Launch.classLoader,new Class[]{modelType},(p,m,a)->null),replacement=Proxy.newProxyInstance(Launch.classLoader,new Class[]{modelType},(p,m,a)->null),manager=allocate.invoke(unsafe,managerType);set(manager,"field_174955_d",original);Object mesher=mesherType.getConstructor(managerType).newInstance(manager);Field cache=mesherType.getDeclaredField("field_178091_b");cache.setAccessible(true);java.util.Map models=(java.util.Map)cache.get(mesher);Method id=mesherType.getDeclaredMethod("func_178081_c",itemType,int.class);id.setAccessible(true);Object originalItem=stackType.getMethod("func_77973_b").invoke(stack),chosenItem=itemType.getMethod("func_150898_a",blocks).invoke(null,wood);models.put(id.invoke(mesher,originalItem,0),original);models.put(id.invoke(mesher,chosenItem,0),replacement);
        appearance.getMethod("apply",String.class,int.class,boolean.class).invoke(null,"minecraft:planks",0,true);Object shown=mesherType.getMethod("func_178089_a",stackType).invoke(mesher,stack);if(shown!=replacement||stackType.getField("field_77994_a").getInt(stack)!=64||stackType.getMethod("func_77973_b").invoke(stack)!=originalItem)throw new AssertionError("Held/hotbar model substitution changed the real stack or failed");
        testHudPreparation(unsafe,allocate);testEarlyAppearance(appearance,block);testConnectedAppearance(appearance,block);String sound=(String)appearance.getMethod("placeSound",String.class).invoke(null,"fixture-original");if(!sound.contains("wood"))throw new AssertionError("Selected block placement sound missing");Class<?> itemBlock=Class.forName("net.minecraft.item.ItemBlock",true,Launch.classLoader);Object item=itemBlock.getConstructor(blocks).newInstance(block);Method soundHook=null;for(Method m:itemBlock.getDeclaredMethods())if(m.getName().contains("bridgetap$placementSound"))soundHook=m;if(soundHook==null)throw new AssertionError("Placement sound hook missing");if(soundHook.getParameterTypes().length!=7)throw new AssertionError("Placement redirect does not preserve the original remote sound call");appearance.getMethod("beginPlacement").invoke(null);if(!(Boolean)appearance.getMethod("placing").invoke(null))throw new AssertionError("Local placement context missing");appearance.getMethod("endPlacement").invoke(null);testIncrementalAppearance(unsafe,allocate,appearance);testPlacementAudio(unsafe,allocate,mc,appearance);appearance.getMethod("apply",String.class,int.class,boolean.class).invoke(null,"minecraft:planks",0,false);set(player,"field_71071_by",null);
        Object display=appearance.getMethod("visualStack",stackType).invoke(null,stack);if(stackType.getMethod("func_77973_b").invoke(display)!=itemType.getMethod("func_150898_a",blocks).invoke(null,wood)||stackType.getField("field_77994_a").getInt(display)!=64||stackType.getField("field_77994_a").getInt(stack)!=64)throw new AssertionError("Presentation stack changed actual inventory or lost its selected material");
        System.out.println("PASS: native hotbar/held-item model substitution preserves real item/count, and only local placement sounds change");
    }
    private static void testReplaySceneryPaste()throws Exception{
        Class<?> unsafeType=Class.forName("sun.misc.Unsafe");Field singleton=unsafeType.getDeclaredField("theUnsafe");singleton.setAccessible(true);Object unsafe=singleton.get(null);Method allocate=unsafeType.getMethod("allocateInstance",Class.class);
        Class.forName("net.minecraft.init.Bootstrap",true,Launch.classLoader).getMethod("func_151354_b").invoke(null);
        Class<?> minecraft=Class.forName("net.minecraft.client.Minecraft",true,Launch.classLoader),worldType=Class.forName("net.minecraft.client.multiplayer.WorldClient",false,Launch.classLoader),playerType=Class.forName("net.minecraft.client.entity.EntityPlayerSP",false,Launch.classLoader);
        Object mc=allocate.invoke(unsafe,minecraft),world=allocate.invoke(unsafe,worldType),viewer=allocate.invoke(unsafe,playerType);
        Field instance=minecraft.getDeclaredField("field_71432_P");instance.setAccessible(true);Object previous=instance.get(null);instance.set(null,mc);
        set(world,"field_73021_x",new ArrayList<>());set(mc,"field_71441_e",world);set(mc,"field_71439_g",viewer);set(viewer,"field_70165_t",10.0);set(viewer,"field_70163_u",70.0);set(viewer,"field_70161_v",20.0);set(viewer,"field_70177_z",0f);
        Class<?> tracker=Class.forName("com.bridger.momentum.BridgeTracker",true,Launch.classLoader);
        testRunConfirmation(tracker,world);
        testReplayChat(unsafe,allocate,minecraft,mc);
        Field tick=tracker.getDeclaredField("currentTick"),velocity=tracker.getDeclaredField("jumpVelocity");tick.setAccessible(true);velocity.setAccessible(true);
        tick.setInt(null,41);set(viewer,"field_70159_w",.125);set(viewer,"field_70181_x",-.08);set(viewer,"field_70179_y",-.25);
        Method snapshot=tracker.getDeclaredMethod("savePositionSnapshot",playerType,int.class);snapshot.setAccessible(true);snapshot.invoke(null,viewer,41);tracker.getMethod("onTickEnd",minecraft).invoke(null,mc);Object ring=velocity.get(null);Object previousTick=ring.getClass().getMethod("beforeJump",int.class).invoke(ring,42);
        if(previousTick==null||previousTick.getClass().getField("x").getDouble(previousTick)!=10.0||previousTick.getClass().getField("z").getDouble(previousTick)!=20.0)throw new AssertionError("End-of-tick sampler did not capture native player position");
        tick.setInt(null,42);set(viewer,"field_70159_w",.9);set(viewer,"field_70181_x",.42);tracker.getMethod("onTickEnd",minecraft).invoke(null,mc);
        Object beforeJump=ring.getClass().getMethod("beforeJump",int.class).invoke(ring,42);if(beforeJump.getClass().getField("x").getDouble(beforeJump)!=10.0)throw new AssertionError("Pre-jump position used the current tick");
        tracker.getMethod("reset").invoke(null);System.out.println("PASS: native tick-start positions are retained at tick end and pre-jump velocity uses the previous tick");
        testJumpDetails(tracker,minecraft,mc,playerType,viewer,snapshot,tick);
        set(viewer,"field_70161_v",20.0);set(viewer,"field_70177_z",0f);
        testAppearanceItems(unsafe,allocate,minecraft,mc,playerType,viewer);
        Class<?> session=Class.forName("com.bridger.momentum.ReplaySession",true,Launch.classLoader);Object replay=allocate.invoke(unsafe,session);set(replay,"source",world);set(replay,"camera",viewer);
        Field current=session.getDeclaredField("current");current.setAccessible(true);current.set(null,replay);Object ghost=allocate.invoke(unsafe,Class.forName("com.bridger.momentum.ReplayGhost",true,Launch.classLoader));set(replay,"ghost",ghost);set(replay,"ghostAnimationPartial",.375f);set(replay,"rendering",true);for(float nativePartial:new float[]{0f,.2f,.8f,1f}){if((Float)session.getMethod("animationPartial",Object.class,float.class).invoke(null,ghost,nativePartial)!=.375f||(Float)session.getMethod("animationPartial",Object.class,float.class).invoke(null,viewer,nativePartial)!=nativePartial)throw new AssertionError("Paused ghost interpolation changes with live tick or freezes other players");}set(replay,"rendering",false);System.out.println("PASS: paused ghost animation interpolation stays frozen while ordinary player animation is unchanged");Field invalid=session.getDeclaredField("rendererInvalid");invalid.setAccessible(true);session.getMethod("rendererInvalidated").invoke(null);if(!invalid.getBoolean(replay)||current.get(null)!=replay||minecraft.getField("field_71441_e").get(mc)!=world)throw new AssertionError("Renderer reload lost replay or modified live context");invalid.setBoolean(replay,false);set(replay,"rendering",true);session.getMethod("rendererInvalidated").invoke(null);if(invalid.getBoolean(replay))throw new AssertionError("Internal replay reload recursively invalidated itself");set(replay,"rendering",false);System.out.println("PASS: texture/shader invalidation defers replay rebinding without discarding the scene or entering a rebuild loop");
        Class<?> scenery=Class.forName("com.bridger.momentum.IslandOverlay",true,Launch.classLoader);
        try{
            Class<?> template=Class.forName("com.bridger.momentum.IslandTemplate",true,Launch.classLoader),block=Class.forName("com.bridger.momentum.IslandTemplate$Block",true,Launch.classLoader);
            Object b=block.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(0,0,0,"minecraft:sandstone",0);
            Class<?> recording=Class.forName("com.bridger.momentum.ReplayData",true,Launch.classLoader),frame=Class.forName("com.bridger.momentum.ReplayData$Frame",true,Launch.classLoader),recordedBlock=Class.forName("com.bridger.momentum.ReplayData$Block",true,Launch.classLoader);
            Object f=frame.getConstructor(double.class,double.class,double.class).newInstance(10.5,65.0,20.5),terrain=recordedBlock.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(10,64,20,24,0,0,false);
            Object data=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(f),Arrays.asList(terrain),false);
            Object t=scenery.getMethod("fromReplay",String.class,recording,boolean.class).invoke(null,"Replay island",data,false);
            scenery.getMethod("show",template,boolean.class).invoke(null,t,false);scenery.getMethod("tick").invoke(null);
            if(!(Boolean)scenery.getMethod("enabled").invoke(null))throw new AssertionError("Pasting scenery during replay was rejected or immediately disabled");
            Field z=scenery.getDeclaredField("z");z.setAccessible(true);if(z.getInt(null)<=20)throw new AssertionError("Replay scenery was not pasted in front of the camera");
            set(world,"field_73021_x",new ArrayList<>());
            Class<?> masks=Class.forName("com.bridger.momentum.IslandReplacement",true,Launch.classLoader),maskType=Class.forName("com.bridger.momentum.IslandReplacement$Mask",true,Launch.classLoader);
            java.lang.reflect.Constructor<?> constructor=maskType.getDeclaredConstructor(java.util.Collection.class,int.class);constructor.setAccessible(true);Object mask=constructor.newInstance(Arrays.asList(terrain),65);
            masks.getMethod("set",maskType).invoke(null,mask);
            set(world,"field_72995_K",true);
            Class<?> positionType=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader);Object maskedPos=positionType.getConstructor(int.class,int.class,int.class).newInstance(10,64,20);
            Class<?> region=Class.forName("net.minecraft.client.renderer.RegionRenderCache",true,Launch.classLoader);Object cache=allocate.invoke(unsafe,region);
            Object air=region.getMethod("func_180495_p",positionType).invoke(cache,maskedPos);Class<?> stateType=Class.forName("net.minecraft.block.state.IBlockState",false,Launch.classLoader),nativeBlock=Class.forName("net.minecraft.block.Block",false,Launch.classLoader);
            if((Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(air))!=0)throw new AssertionError("Region render cache did not mask the original block");
            try{
                Class<?> argentum=Class.forName("dev.rdh.argentum.impl.world.cloned.ChunkRenderContext",true,Launch.classLoader);Object context=argentum.getConstructor().newInstance();
                for(String method:new String[]{"getOriginBlockState","getBlockState"}){Object value=argentum.getMethod(method,int.class,int.class,int.class).invoke(context,method.equals("getOriginBlockState")?10:10,method.equals("getOriginBlockState")?0:64,method.equals("getOriginBlockState")?4:20);if((Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(value))!=0)throw new AssertionError("Argentum cache bypasses the mask: "+method);}
                System.out.println("PASS: optional Argentum local-section and absolute snapshot interception");
            }catch(ClassNotFoundException absent){}
            if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,10,64,20))throw new AssertionError("Original island was not visually masked");
            Class<?> dispatcher=Class.forName("net.minecraft.client.renderer.BlockRendererDispatcher",true,Launch.classLoader),pos=Class.forName("net.minecraft.util.BlockPos",true,Launch.classLoader),state=Class.forName("net.minecraft.block.state.IBlockState",false,Launch.classLoader),access=Class.forName("net.minecraft.world.IBlockAccess",false,Launch.classLoader),builder=Class.forName("net.minecraft.client.renderer.WorldRenderer",true,Launch.classLoader);
            Object skipped=dispatcher.getMethod("func_175018_a",state,pos,access,builder).invoke(allocate.invoke(unsafe,dispatcher),null,pos.getConstructor(int.class,int.class,int.class).newInstance(10,64,20),null,null);
            if(!Boolean.FALSE.equals(skipped))throw new AssertionError("Native terrain renderer still draws the replaced island");
            masks.getMethod("set",maskType).invoke(null,masks.getField("EMPTY").get(null));
            if((Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,10,64,20))throw new AssertionError("Removing the decoration failed to restore original blocks");
            Object finish=recordedBlock.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(40,64,20,24,0,0,false),endFrame=frame.getConstructor(double.class,double.class,double.class).newInstance(40.5,65.0,20.5);
            Object both=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(f,endFrame),Arrays.asList(terrain,finish),false);set(replay,"data",both);
            set(viewer,"field_70165_t",500.0);set(viewer,"field_70163_u",120.0);
            Object scene=masks.getMethod("captureScene").invoke(null);masks.getMethod("set",maskType).invoke(null,scene);
            Class<?> boxType=Class.forName("com.bridger.momentum.IslandBox",true,Launch.classLoader);Object endArea=boxType.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class).newInstance(38,62,18,6,8,6);
            Object expanded=masks.getMethod("extend",maskType,java.util.List.class).invoke(null,scene,Arrays.asList(endArea));masks.getMethod("set",maskType).invoke(null,expanded);
            if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,42,66,22))throw new AssertionError("Disconnected or late-loaded finish blocks escaped the area mask");
            if((Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,30,64,20))throw new AssertionError("Island region mask extended into the bridge corridor");
            masks.getMethod("allowPlacement",int.class,int.class,int.class).invoke(null,42,66,22);if((Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,42,66,22))throw new AssertionError("Area mask hid a newly placed bridge block");
            System.out.println("PASS: disconnected and late-loaded finish cells masked, corridor and new placements preserved");
            for(int islandX:new int[]{10,40})if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,islandX,64,20))throw new AssertionError("Remote replay camera lost an endpoint island mask");
            // A single-island template must still hide both originals, and a
            // missing finish in an older capture must not lose the start mask.
            scenery.getMethod("beginPreview",template).invoke(null,t);
            for(int islandX:new int[]{10,40})if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,islandX,64,20))throw new AssertionError("Single-island placement left an original endpoint visible");
            Object pairTemplate=scenery.getMethod("bothFromReplay",String.class,recording).invoke(null,"Both islands",both);scenery.getMethod("show",template,boolean.class).invoke(null,pairTemplate,true);
            testIslandAutoFit(scenery,template,recording,frame,recordedBlock,replay,both,pairTemplate);
            scenery.getMethod("moveStart",int.class,int.class,int.class).invoke(null,5,2,0);Object movedStart=scenery.getMethod("visibleState",int.class,int.class,int.class,stateType).invoke(null,15,66,20,air);if((Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(movedStart))!=24)throw new AssertionError("Start island did not move independently");scenery.getMethod("resetStart").invoke(null);
            scenery.getMethod("moveFinish",int.class,int.class,int.class).invoke(null,300,4,0);
            Object moved=scenery.getMethod("visibleState",int.class,int.class,int.class,stateType).invoke(null,340,68,20,air);
            if((Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(moved))!=24)throw new AssertionError("Finish island did not move beyond the original template bounds");
            Object untouched=scenery.getMethod("visibleState",int.class,int.class,int.class,stateType).invoke(null,10,64,20,air);
            if((Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(untouched))!=24)throw new AssertionError("Moving finish also moved the start island");
            scenery.getMethod("rotate").invoke(null);Object rotated=scenery.getMethod("visibleState",int.class,int.class,int.class,stateType).invoke(null,10,68,350,air);
            if((Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(rotated))!=24)throw new AssertionError("Rotation lost the independently moved finish island");
            scenery.getMethod("refreshMask").invoke(null);if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,10,68,350))throw new AssertionError("Replacement mask did not follow the moved and rotated finish island");
            scenery.getMethod("moveStart",int.class,int.class,int.class).invoke(null,5,2,0);Class<?> layoutType=Class.forName("com.bridger.momentum.IslandLayout",true,Launch.classLoader);Object layout=scenery.getMethod("layout").invoke(null);scenery.getMethod("applyPreset",template,layoutType).invoke(null,pairTemplate,layout);if((Boolean)scenery.getMethod("previewing").invoke(null)||(Integer)scenery.getMethod("startHeight").invoke(null)!=2||(Integer)scenery.getMethod("finishHeight").invoke(null)!=4||(Integer)scenery.getMethod("rotation").invoke(null)!=90)throw new AssertionError("One-click preset did not restore and confirm the independent layout");
            scenery.getMethod("beginPreview",template).invoke(null,t);scenery.getMethod("cancel").invoke(null);
            if((Integer)scenery.getMethod("finishHeight").invoke(null)!=4)throw new AssertionError("Cancel lost the previous finish placement");
            if((Integer)scenery.getMethod("rotation").invoke(null)!=90)throw new AssertionError("Cancel lost the previous island orientation");
            scenery.getMethod("resetStart").invoke(null);scenery.getMethod("resetFinish").invoke(null);if((Integer)scenery.getMethod("gap").invoke(null)!=29)throw new AssertionError("Reset finish did not restore the original gap");
            System.out.println("PASS: independent finish position beyond template bounds, unchanged start and cancel/reset restoration");
            scenery.getMethod("confirm").invoke(null);set(replay,"data",data);scene=masks.getMethod("captureScene").invoke(null);masks.getMethod("set",maskType).invoke(null,scene);
            if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,10,64,20))throw new AssertionError("Missing finish terrain discarded the valid start island mask");
            masks.getMethod("set",maskType).invoke(null,masks.getField("EMPTY").get(null));set(viewer,"field_70165_t",10.0);set(viewer,"field_70163_u",70.0);
            System.out.println("PASS: both original endpoints hidden for single-island placement, remote cameras and partial recordings");
            System.out.println("PASS: native terrain mask hides original blocks and restores them without world edits");
            Class<?> format=Class.forName("net.minecraft.client.renderer.vertex.VertexFormat",true,Launch.classLoader),formats=Class.forName("net.minecraft.client.renderer.vertex.DefaultVertexFormats",true,Launch.classLoader),quad=Class.forName("net.minecraft.client.renderer.block.model.BakedQuad",true,Launch.classLoader),facing=Class.forName("net.minecraft.util.EnumFacing",true,Launch.classLoader);
            Object nativeBuffer=builder.getConstructor(int.class).newInstance(1024),blockFormat=formats.getField("field_176600_a").get(null);builder.getMethod("func_181668_a",int.class,format).invoke(nativeBuffer,7,blockFormat);
            int[] vertices=new int[28];for(int i=0;i<4;i++){vertices[i*7]=Float.floatToIntBits(i==1||i==2?1:0);vertices[i*7+1]=Float.floatToIntBits(i>=2?1:0);vertices[i*7+3]=-1;}
            Object q=quad.getConstructor(int[].class,int.class,facing).newInstance(vertices,-1,facing.getEnumConstants()[1]);Object offset=block.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(3,2,4,"minecraft:sandstone",0);
            Method emit=scenery.getDeclaredMethod("quads",builder,java.util.List.class,block,int.class);emit.setAccessible(true);emit.invoke(null,nativeBuffer,Arrays.asList(q),offset,0xFFFFFF);
            if((Integer)builder.getMethod("func_178989_h").invoke(nativeBuffer)!=4)throw new AssertionError("Native island mesh has no vertices");
            java.nio.ByteBuffer raw=(java.nio.ByteBuffer)builder.getMethod("func_178966_f").invoke(nativeBuffer);if(raw.getFloat(0)!=3||raw.getFloat(4)!=2||raw.getFloat(8)!=4||raw.getInt(24)!=0x00F000F0)throw new AssertionError("Island position or full-bright vertex format is invalid");
            Object cached=builder.getMethod("func_181672_a").invoke(nativeBuffer);builder.getMethod("func_178977_d").invoke(nativeBuffer);builder.getMethod("func_181668_a",int.class,format).invoke(nativeBuffer,7,blockFormat);builder.getMethod("func_178993_a",cached.getClass()).invoke(nativeBuffer,cached);
            if((Integer)builder.getMethod("func_178989_h").invoke(nativeBuffer)!=4)throw new AssertionError("Native cached island mesh was not restored");builder.getMethod("func_178977_d").invoke(nativeBuffer);
            System.out.println("PASS: native island mesh format, positions, full-bright UVs and cached vertex restoration");
            Object pane=block.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(0,0,0,"minecraft:glass_pane",0),eastStone=block.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(1,0,0,"minecraft:stone",0),southPane=block.getConstructor(int.class,int.class,int.class,String.class,int.class).newInstance(0,0,1,"minecraft:glass_pane",0);
            Object panes=template.getConstructor(String.class,int.class,int.class,int.class,int.class,int.class,int.class,java.util.List.class).newInstance("Connected panes",2,1,2,0,64,0,Arrays.asList(pane,eastStone,southPane));scenery.getMethod("show",template,boolean.class).invoke(null,panes,true);
            Field resolved=scenery.getDeclaredField("states");resolved.setAccessible(true);String paneState=((Object[])resolved.get(null))[0].toString();if(!paneState.contains("east=true")||!paneState.contains("south=true")||!paneState.contains("west=false")||!paneState.contains("north=false"))throw new AssertionError("Pane actual connections were not resolved against the copied island: "+paneState);
            System.out.println("PASS: native glass pane connections use copied east/south neighbours instead of a default cross");
            Class<?> appearance=Class.forName("com.bridger.momentum.PlacementAppearance",true,Launch.classLoader);java.util.Map<Long,Integer> playerBlocks=new java.util.HashMap<>();playerBlocks.put((Long)recordedBlock.getMethod("key").invoke(terrain),24);set(replay,"placedIds",playerBlocks);masks.getMethod("set",maskType).invoke(null,masks.getField("EMPTY").get(null));appearance.getMethod("apply",String.class,int.class,boolean.class).invoke(null,"minecraft:diamond_block",0,true);
            Object sandstone=nativeBlock.getMethod("func_149729_e",int.class).invoke(null,24);Object original=nativeBlock.getMethod("func_176223_P").invoke(sandstone);Object replacement=appearance.getMethod("replacement",positionType,stateType).invoke(null,maskedPos,original);if(replacement==null||(Integer)nativeBlock.getMethod("func_149682_b",nativeBlock).invoke(null,stateType.getMethod("func_177230_c").invoke(replacement))!=57)throw new AssertionError("Placed-block visual selection was not applied");appearance.getMethod("apply",String.class,int.class,boolean.class).invoke(null,"minecraft:diamond_block",0,false);
            masks.getMethod("toggleFocus").invoke(null);
            Class<?> focusAudio=Class.forName("com.bridger.momentum.FocusAudio",true,Launch.classLoader),soundType=Class.forName("net.minecraft.client.audio.ISound",true,Launch.classLoader),attenuation=Class.forName("net.minecraft.client.audio.ISound$AttenuationType",true,Launch.classLoader);
            for(int soundCase=0;soundCase<3;soundCase++){final int which=soundCase;Object sound=Proxy.newProxyInstance(Launch.classLoader,new Class[]{soundType},(proxy,method,arguments)->{switch(method.getName()){case "func_147656_j":return attenuation.getField(which==2?"NONE":"LINEAR").get(null);case "func_147649_g":return which==0?1000f:10f;case "func_147654_h":return 70f;case "func_147651_i":return which==0?1000f:20f;default:throw new AssertionError("Unexpected sound accessor: "+method.getName());}});boolean muted=(Boolean)focusAudio.getMethod("suppress",soundType).invoke(null,sound);if(muted!=(which==0))throw new AssertionError("Native focus audio misclassified sound "+which);}
            System.out.println("PASS: native Focus sound filtering preserves own footsteps and non-positional interface audio");
            if(!(Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,1000,64,1000))throw new AssertionError("Focus mode did not hide neighbouring island terrain");if((Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,10,64,20))throw new AssertionError("Focus mode hid the player's bridge");scenery.getMethod("disable").invoke(null);if((Boolean)masks.getMethod("hidden",int.class,int.class,int.class).invoke(null,1000,64,1000))throw new AssertionError("Focus remained active after removing scenery");masks.getMethod("toggleFocus").invoke(null);scenery.getMethod("show",template,boolean.class).invoke(null,panes,true);
            System.out.println("PASS: independent start movement, client-only placed block appearance and reversible focus mode");
            // Save against fixed island geometry, then change both the camera's
            // supporting block and the next run's starting block by one voxel.
            Object neighbour=recordedBlock.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(11,64,20,24,0,0,false);
            Object wide=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(f,endFrame),Arrays.asList(terrain,neighbour,finish),false);
            set(replay,"data",wide);set(viewer,"field_70165_t",10.5);set(viewer,"field_70163_u",65.0);set(viewer,"field_70161_v",20.5);
            scenery.getMethod("beginPreview",template).invoke(null,pairTemplate);scenery.getMethod("moveStart",int.class,int.class,int.class).invoke(null,1,2,0);scenery.getMethod("moveFinish",int.class,int.class,int.class).invoke(null,30,4,0);scenery.getMethod("rotate").invoke(null);scenery.getMethod("confirm").invoke(null);
            Object fixedLayout=scenery.getMethod("layout").invoke(null),before=scenery.getMethod("copyBounds").invoke(null);
            Object shiftedFrame=frame.getConstructor(double.class,double.class,double.class).newInstance(11.5,65.0,20.5);
            wide=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(shiftedFrame,endFrame),Arrays.asList(terrain,neighbour,finish),false);set(replay,"data",wide);
            for(double cameraX:new double[]{11.5,40.5,500,10.5}){
                set(viewer,"field_70165_t",cameraX);scenery.getMethod("applyPreset",template,layoutType).invoke(null,pairTemplate,fixedLayout);
                Object after=scenery.getMethod("copyBounds").invoke(null);
                for(String fieldName:new String[]{"x","y","z","width","height","depth"})if(!boxType.getField(fieldName).get(before).equals(boxType.getField(fieldName).get(after)))throw new AssertionError("Preset drift after camera/run movement: "+fieldName);
            }
            System.out.println("PASS: exact preset voxel positions survive repeated application, one-block run/camera changes and finish-island viewing");
            Object finishOnly=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(f,endFrame),Arrays.asList(finish),false);set(replay,"data",finishOnly);set(viewer,"field_70165_t",40.5);try{scenery.getMethod("applyPreset",template,layoutType).invoke(null,pairTemplate,fixedLayout);throw new AssertionError("Preset silently used the finish island when the start was missing");}catch(java.lang.reflect.InvocationTargetException expected){if(!(expected.getCause() instanceof IllegalArgumentException))throw expected;}set(replay,"data",wide);
            System.out.println("PASS: missing start island blocks preset placement instead of anchoring on the nearby finish");

            scenery.getMethod("beginPreview",template).invoke(null,pairTemplate);Object initialBounds=scenery.getMethod("copyBounds").invoke(null);scenery.getMethod("moveStart",int.class,int.class,int.class).invoke(null,0,3,0);scenery.getMethod("rotate").invoke(null);scenery.getMethod("undo").invoke(null);if((Integer)scenery.getMethod("rotation").invoke(null)!=0)throw new AssertionError("Undo did not restore rotation");scenery.getMethod("undo").invoke(null);if((Integer)scenery.getMethod("startHeight").invoke(null)!=0)throw new AssertionError("Undo did not restore start position");scenery.getMethod("redo").invoke(null);if((Integer)scenery.getMethod("startHeight").invoke(null)!=3)throw new AssertionError("Redo did not restore edited start height");scenery.getMethod("cancel").invoke(null);
            System.out.println("PASS: native island editor undo/redo restores independent position and rotation");
            Object placedBlock=recordedBlock.getConstructor(int.class,int.class,int.class,int.class,int.class,int.class,boolean.class).newInstance(11,65,20,24,2,0,true);Object videoData=recording.getConstructor(java.util.List.class,java.util.List.class,boolean.class).newInstance(Arrays.asList(f),Arrays.asList(placedBlock),false);Class<?> videoHud=Class.forName("com.bridger.momentum.ReplayVideoHud",true,Launch.classLoader);Object videoStack=videoHud.getMethod("blockStack",recording,double.class).invoke(null,videoData,0.0);if(videoStack==null||videoStack.getClass().getField("field_77994_a").getInt(videoStack)!=64||(Integer)videoStack.getClass().getMethod("func_77960_j").invoke(videoStack)!=2)throw new AssertionError("Video hotbar lost the recorded construction block stack or variant");System.out.println("PASS: native video hotbar creates a 64-block construction stack with recorded metadata");

            try{Class<?> blur=Class.forName("eu.midnightdust.blur.BlurRenderer",true,Launch.classLoader),guiScreen=Class.forName("net.minecraft.client.gui.GuiScreen",true,Launch.classLoader),placementScreen=Class.forName("com.bridger.momentum.IslandPlacementScreen",true,Launch.classLoader);Object editor=allocate.invoke(unsafe,placementScreen);set(mc,"field_71462_r",editor);int calls=blur.getField("calls").getInt(null);blur.getMethod("render").invoke(null);Object background=blur.getMethod("renderBackground",guiScreen).invoke(null,editor);if(blur.getField("calls").getInt(null)!=calls||!Boolean.FALSE.equals(background))throw new AssertionError("Island placement still receives GUI blur or gradient");set(mc,"field_71462_r",null);blur.getMethod("render").invoke(null);if(blur.getField("calls").getInt(null)!=calls+1)throw new AssertionError("Blur remained disabled outside the island editor");System.out.println("PASS: optional LegacyGuiBlur is suppressed only in world-editing screens");}catch(ClassNotFoundException absent){}


            set(mc,"field_71441_e",allocate.invoke(unsafe,worldType));scenery.getMethod("tick").invoke(null);if((Boolean)scenery.getMethod("enabled").invoke(null))throw new AssertionError("Scenery survived a world change");
            System.out.println("PASS: native sandstone scenery paste during replay, camera placement and source-world lifecycle");
            testLiveIslandRoles(unsafe,allocate,minecraft,mc,viewer,session,current,scenery,template,pairTemplate);
        }finally{scenery.getMethod("disable").invoke(null);current.set(null,null);instance.set(null,previous);}
    }
    private static void set(Object target,String name,Object value)throws Exception{
        Class<?> type=target.getClass();while(type!=null){try{Field field=type.getDeclaredField(name);field.setAccessible(true);field.set(target,value);return;}catch(NoSuchFieldException ex){type=type.getSuperclass();}}throw new NoSuchFieldException(name);
    }

    private static void testSyntheticChunkUpdates() throws Exception {
        // Exercise Forge's actual populated-chunk notification gate without a GL
        // context. Only fields read by these two methods are initialized.
        Class<?> unsafeType=Class.forName("sun.misc.Unsafe");
        Field singleton=unsafeType.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        Object unsafe=singleton.get(null);Method allocate=unsafeType.getMethod("allocateInstance",Class.class);
        Class<?> worldType=Class.forName("net.minecraft.world.World",false,Launch.classLoader);
        Object world=allocate.invoke(unsafe,Class.forName("net.minecraft.client.multiplayer.WorldClient",false,Launch.classLoader));
        Field remote=worldType.getDeclaredField("field_72995_K");remote.setAccessible(true);remote.setBoolean(world,true);
        Class<?> access=Class.forName("net.minecraft.world.IWorldAccess",false,Launch.classLoader);
        final int[] notifications={0,0};final int[] bounds=new int[6];
        Object listener=Proxy.newProxyInstance(Launch.classLoader,new Class<?>[]{access},(p,m,a)->{
            if(m.getName().equals("func_174960_a"))notifications[0]++;
            if(m.getName().equals("func_147585_a")){
                notifications[1]++;for(int i=0;i<6;i++)bounds[i]=(Integer)a[i];
            }
            return null;
        });
        ArrayList<Object> listeners=new ArrayList<>();listeners.add(listener);
        Field accesses=worldType.getDeclaredField("field_73021_x");accesses.setAccessible(true);accesses.set(world,listeners);
        Class<?> chunkType=Class.forName("net.minecraft.world.chunk.Chunk",false,Launch.classLoader);
        Object chunk=allocate.invoke(unsafe,chunkType);
        if((Boolean)chunkType.getMethod("func_150802_k").invoke(chunk))throw new AssertionError("Synthetic chunk unexpectedly populated");
        Class<?> posType=Class.forName("net.minecraft.util.BlockPos",false,Launch.classLoader);
        Object pos=posType.getConstructor(int.class,int.class,int.class).newInstance(15,64,-1);
        Class<?> state=Class.forName("net.minecraft.block.state.IBlockState",false,Launch.classLoader);
        worldType.getMethod("markAndNotifyBlock",posType,chunkType,state,state,int.class).invoke(world,pos,chunk,null,null,2);
        if(notifications[0]!=0||notifications[1]!=0)throw new AssertionError("Expected Forge to skip flags=2 notification on unpopulated chunk");
        worldType.getMethod("markAndNotifyBlock",posType,chunkType,state,state,int.class).invoke(world,pos,null,null,null,2);
        if(notifications[0]!=1)throw new AssertionError("Control notification should pass when the population gate is bypassed");
        Method notify=worldType.getMethod("func_147458_c",int.class,int.class,int.class,int.class,int.class,int.class);
        notify.invoke(world,15,64,-1,15,64,-1);
        if(notifications[1]!=1||!Arrays.equals(bounds,new int[]{15,64,-1,15,64,-1}))throw new AssertionError("Explicit replay edit did not invalidate its region");
        // The same notification is required when removing a placed block on rewind.
        notify.invoke(world,15,64,-1,15,64,-1);
        if(notifications[1]!=2)throw new AssertionError("Rewind must invalidate the same region again");
        System.out.println("PASS: reproduced Forge's skipped synthetic-chunk notification; explicit edit/rewind updates reach renderer");
    }
}
