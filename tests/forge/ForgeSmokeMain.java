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
        String[] targets={"net.minecraft.client.audio.SoundManager", "net.minecraft.client.Minecraft", "net.minecraft.entity.EntityLivingBase",
            "net.minecraft.client.gui.GuiIngame", "net.minecraft.client.entity.EntityPlayerSP",
            "net.minecraft.client.multiplayer.PlayerControllerMP", "net.minecraft.util.MovementInputFromOptions",
            "net.minecraft.entity.player.InventoryPlayer", "net.minecraft.entity.player.EntityPlayer",
            "net.minecraft.client.renderer.ItemRenderer", "net.minecraft.client.renderer.RenderGlobal",
            "net.minecraft.client.renderer.entity.RenderPlayer", "net.minecraft.client.renderer.EntityRenderer", "net.minecraft.client.renderer.BlockRendererDispatcher", "net.minecraft.client.renderer.RegionRenderCache", "net.minecraft.world.ChunkCache", "net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher"};
        int hooks=0;
        for(String target:targets) {
            Class<?> type=Class.forName(target,false,Launch.classLoader);
            int count=0;
            for(Method method:type.getDeclaredMethods()) if(method.getName().contains("bridgetap$")) count++;
            if(count==0) throw new AssertionError("Mixin was not applied: "+target);
            hooks+=count;
            System.out.println("PASS Forge transformed "+target+" ("+count+" BridgeTap methods)");
        }
        for(String name:Arrays.asList("ReplaySession","ReplayCamera","ReplayGhost","BridgeHistoryScreen","BridgeSettingsScreen","BridgeColorsScreen","BridgeLayoutScreen","BridgeScreen","ReplayInputOverlay","ReplayRecorder","IslandOverlay","IslandScreen","ClipCapture","ClipScreen","ReplayFolderScreen")) {
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
        testSyntheticChunkUpdates();
        testReplaySceneryPaste();
        testClipFallback();
        System.out.println("PASS: Forge LaunchWrapper, bundled Mixin/ASM, "+hooks+" transformed methods and replay class linkage");
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
    private static void testReplaySceneryPaste()throws Exception{
        Class<?> unsafeType=Class.forName("sun.misc.Unsafe");Field singleton=unsafeType.getDeclaredField("theUnsafe");singleton.setAccessible(true);Object unsafe=singleton.get(null);Method allocate=unsafeType.getMethod("allocateInstance",Class.class);
        Class.forName("net.minecraft.init.Bootstrap",true,Launch.classLoader).getMethod("func_151354_b").invoke(null);
        Class<?> minecraft=Class.forName("net.minecraft.client.Minecraft",true,Launch.classLoader),worldType=Class.forName("net.minecraft.client.multiplayer.WorldClient",false,Launch.classLoader),playerType=Class.forName("net.minecraft.client.entity.EntityPlayerSP",false,Launch.classLoader);
        Object mc=allocate.invoke(unsafe,minecraft),world=allocate.invoke(unsafe,worldType),viewer=allocate.invoke(unsafe,playerType);
        Field instance=minecraft.getDeclaredField("field_71432_P");instance.setAccessible(true);Object previous=instance.get(null);instance.set(null,mc);
        set(mc,"field_71441_e",world);set(mc,"field_71439_g",viewer);set(viewer,"field_70165_t",10.0);set(viewer,"field_70163_u",70.0);set(viewer,"field_70161_v",20.0);set(viewer,"field_70177_z",0f);
        Class<?> session=Class.forName("com.bridger.momentum.ReplaySession",true,Launch.classLoader);Object replay=allocate.invoke(unsafe,session);set(replay,"source",world);set(replay,"camera",viewer);
        Field current=session.getDeclaredField("current");current.setAccessible(true);current.set(null,replay);
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
            set(mc,"field_71441_e",allocate.invoke(unsafe,worldType));scenery.getMethod("tick").invoke(null);if((Boolean)scenery.getMethod("enabled").invoke(null))throw new AssertionError("Scenery survived a world change");
            System.out.println("PASS: native sandstone scenery paste during replay, camera placement and source-world lifecycle");
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
