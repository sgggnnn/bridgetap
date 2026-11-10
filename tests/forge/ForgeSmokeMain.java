package bridgetap.smoke;

import java.lang.reflect.Method;
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
        for(String name:Arrays.asList("ReplaySession","ReplayCamera","ReplayGhost","BridgeHistoryScreen","BridgeSettingsScreen","ReplayRecorder")) {
            Class<?> type=Class.forName("com.bridger.momentum."+name,false,Launch.classLoader);
            type.getDeclaredMethods();type.getDeclaredConstructors();
        }
        Class<?> mod=Class.forName("com.bridger.momentum.forge.BridgeTapForge",false,Launch.classLoader);
        if(mod.getAnnotations().length==0)throw new AssertionError("Missing Forge mod metadata");
        System.out.println("PASS: Forge LaunchWrapper, bundled Mixin/ASM, "+hooks+" transformed methods and replay class linkage");
    }
}
