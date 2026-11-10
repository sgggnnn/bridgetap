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
        String[] targets={"net.minecraft.client.Minecraft", "net.minecraft.entity.EntityLivingBase",
            "net.minecraft.client.gui.GuiIngame", "net.minecraft.client.entity.EntityPlayerSP",
            "net.minecraft.client.multiplayer.PlayerControllerMP", "net.minecraft.util.MovementInputFromOptions",
            "net.minecraft.entity.player.InventoryPlayer", "net.minecraft.entity.player.EntityPlayer",
            "net.minecraft.client.renderer.ItemRenderer", "net.minecraft.client.renderer.RenderGlobal",
            "net.minecraft.client.renderer.entity.RenderPlayer"};
        int hooks=0;
        for(String target:targets) {
            Class<?> type=Class.forName(target,false,Launch.classLoader);
            int count=0;
            for(Method method:type.getDeclaredMethods()) if(method.getName().contains("bridgetap$")) count++;
            if(count==0) throw new AssertionError("Mixin was not applied: "+target);
            hooks+=count;
            System.out.println("PASS Forge transformed "+target+" ("+count+" BridgeTap methods)");
        }
        for(String name:Arrays.asList("ReplaySession","ReplayCamera","ReplayGhost","BridgeHistoryScreen","BridgeSettingsScreen","BridgeColorsScreen","BridgeLayoutScreen","BridgeScreen","ReplayInputOverlay","ReplayRecorder")) {
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
        System.out.println("PASS: Forge LaunchWrapper, bundled Mixin/ASM, "+hooks+" transformed methods and replay class linkage");
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
