package eu.midnightdust.blur;

/** Signature fixture for LegacyGuiBlur 6.3.5; no third-party implementation. */
public final class BlurRenderer {
    public static int calls;
    public static void render(){calls++;}
    public static boolean renderBackground(net.minecraft.client.gui.GuiScreen screen){calls++;return true;}
}
