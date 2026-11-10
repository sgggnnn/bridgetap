package bridgetap.smoke;

/** Real Forge startup and coremod discovery, with a graphics-free final target. */
public final class ForgeSmokeTweaker extends net.minecraftforge.fml.common.launcher.FMLTweaker {
    @Override public String getLaunchTarget() {
        super.getLaunchTarget();
        return "bridgetap.smoke.ForgeSmokeMain";
    }
}
