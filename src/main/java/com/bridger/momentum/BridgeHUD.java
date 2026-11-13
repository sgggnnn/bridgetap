package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_23014920;
import net.minecraft.unmapped.C_29058632;
import net.minecraft.unmapped.C_23708450;
import net.minecraft.unmapped.C_37925375;
import net.minecraft.unmapped.C_97866173;
import net.minecraft.unmapped.C_43718703;

import java.util.List;
import java.util.Locale;
import org.lwjgl.opengl.GL11;
import java.util.Arrays;

public class BridgeHUD {
    private static long hudEpoch,preparedEpoch=-1;
    private static C_23014920 preparedFont;
    private static HudFrame preparedHud;
    private static final class HudFrame{final String header;final List<String> lines;final int width,height,lineHeight;HudFrame(String header,List<String> lines,int width,int height,int lineHeight){this.header=header;this.lines=lines;this.width=width;this.height=height;this.lineHeight=lineHeight;}}
    private static HudFrame cachedHud(C_23014920 fr){if(preparedEpoch!=hudEpoch||preparedFont!=fr){preparedHud=prepareHud(fr);preparedEpoch=hudEpoch;preparedFont=fr;}return preparedHud;}
    private static HudFrame prepareHud(C_23014920 fr){
        List<JumpEntry> entries = BridgeTracker.getEntries();
        if (entries.isEmpty() && !BridgeTracker.isBridging()) {
            return null;
        }

        // Header string based on status
        double time = BridgeTracker.getCurrentBridgeTime();
        int jumpCount = BridgeTracker.getJumpCount();
        String axis = BridgeTracker.getCurrentAxis();
        BridgeTracker.RunStatus status = BridgeTracker.getStatus();
        String displayMode = BridgeConfig.displayMode;

        String header;
        if (displayMode.equals("TIME")) {
            String color = status == BridgeTracker.RunStatus.SUCCESS ? "\u00a7a" : status == BridgeTracker.RunStatus.FAILED ? "\u00a7c" : "\u00a7e";
            header = color + String.format(Locale.US, "Time: %.2fs", time);
        } else if (displayMode.equals("COORDS") || displayMode.equals("COORDS_ONLY") || displayMode.equals("LAST_TURN") || displayMode.equals("TICKS")) {
            header = "";
        } else if (status == BridgeTracker.RunStatus.SUCCESS) {
            header = String.format(Locale.US, "\u00a7aSuccess %.2fs %d jumps, axis: %s", time, jumpCount, axis);
        } else if (status == BridgeTracker.RunStatus.FAILED) {
            header = String.format(Locale.US, "\u00a7cFailed %.2fs %d jumps, axis: %s", time, jumpCount, axis);
        } else {
            header = String.format(Locale.US, "\u00a7eBridging %.2fs %d jumps, axis: %s", time, jumpCount, axis);
        }

        if(BridgeConfig.chatOutput)header="";
        List<String> displayLines = new java.util.ArrayList<>();
        if (!BridgeConfig.chatOutput&&!displayMode.equals("TIME")) displayLines.addAll(EntryFormatter.hudLines(entries));
        if (header.isEmpty() && displayLines.isEmpty()) return null;

        // Calculate box dimensions
        int maxWidth = header.isEmpty() ? 0 : fr.m_09235706(header);
        for (String line : displayLines) {
            int w = HudText.width(fr,line);
            if (w > maxWidth) maxWidth = w;
        }

        int padding = 4;
        int lineHeight = fr.f_30706543 + 2; // 11px per line
        int lineCount = displayLines.size() + (header.isEmpty() ? 0 : 1);
        int totalHeight = lineHeight * lineCount + padding;
        int totalWidth = maxWidth + padding * 2;

        if(!header.isEmpty())header=OverlayStyle.styled(0,OverlayStyle.color(7)+HudText.unformatted(header));
        for(int i=0;i<displayLines.size();i++)displayLines.set(i,OverlayStyle.styled(0,displayLines.get(i)));
        return new HudFrame(header,displayLines,totalWidth,totalHeight,lineHeight);
    }

    private static C_23708450 settingsKey;
    private static C_23708450 layoutKey;
    private static C_23708450 clipKey,profileKey;
    private static boolean keyBindingRegistered = false;
    private static long nextCleanup;

    public static void onClientTick() {
        hudEpoch++;
        Minecraft mc = Minecraft.m_20213497();
        if (mc == null || mc.f_58088711 == null) return;
        UpdateManager.tick(mc);
        OneClientIntegration.tick();
        IslandReplacement.refreshTerrain();
        PlacementAppearance.tick();
        ServerPresets.tick();
        ReplayVideoExport.tick();
        long now=System.currentTimeMillis();if(now>=nextCleanup){nextCleanup=now+60000;ReplayStorage.cleanup(BridgeTracker.history(),now);}

        registerKeys(mc);
        if(!keyBindingRegistered)return;

        // Consume the press even with a screen open; never trigger the UI while typing in chat.
        boolean pressed = settingsKey.m_37810647();
        boolean layoutPressed = layoutKey.m_37810647();
        boolean clipPressed = clipKey.m_37810647();
        boolean profilePressed=profileKey.m_37810647();
        ClipCapture.tick();
        if (mc.f_70816363 == null) {
            if(profilePressed&&mc.f_28396371!=null)KeybindProfilesScreen.cycle();
            if(clipPressed)ClipCapture.save();
            if (layoutPressed && ReplaySession.active()) mc.m_52715402(new BridgeLayoutScreen(null));
            else if (pressed) openSettings();
        }
    }

    public static synchronized void registerKeys(Minecraft mc){
        if(mc==null||mc.f_58088711==null||mc.f_25803373==null)return;
        if (!keyBindingRegistered) {
            C_23708450[] keys=BridgeKeyBindings.register(mc.f_58088711,mc.f_25803373.toPath().resolve("options.txt"));
            settingsKey=keys[0];layoutKey=keys[1];clipKey=keys[2];profileKey=keys[3];keyBindingRegistered=true;
        }
    }

    public static void openSettings() {
        Minecraft mc = Minecraft.m_20213497();
        if (mc != null && mc.f_28396371 != null) mc.m_52715402(ReplayFolderScreen.home());
    }

    public static void render(C_97866173 gui) {
        Minecraft mc = Minecraft.m_20213497();
        if(OneClientIntegration.editing()){OneClientIntegration.renderEditor();return;}
        if (mc != null && mc.f_70816363 instanceof BridgeLayoutScreen) return;
        if (ReplaySession.active()) { ReplaySession.renderHud(); return; }
        if (mc == null || mc.f_28396371 == null || mc.f_26407825 == null) return;
        if (BridgeConfig.chatOutput) return;
        if (!BridgeConfig.shouldShowHud(mc.f_58088711 != null && mc.f_58088711.f_63869242)) return;

        C_23014920 fr = mc.f_21497584;
        if (fr == null) return;

        HudFrame frame=cachedHud(fr);if(frame==null)return;
        String header=frame.header;List<String> displayLines=frame.lines;int totalWidth=frame.width,totalHeight=frame.height,lineHeight=frame.lineHeight,padding=4;

        C_43718703 window = new C_43718703(mc);
        float scale = (float)Math.min(BridgeConfig.hudScale,Math.min((window.m_39119333()-8.0)/totalWidth,(window.m_16103615()-8.0)/totalHeight));
        int x = OverlayPlacement.clamp(BridgeConfig.hudX,(int)Math.ceil(totalWidth*scale),window.m_39119333());
        int y = OverlayPlacement.clamp(BridgeConfig.hudY,(int)Math.ceil(totalHeight*scale),window.m_16103615());

        OneClientIntegration.recordSize(0,totalWidth,totalHeight);
        // Anchor at the configured position, then scale the complete HUD layout.
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);

        // Render background box and border
        C_37925375.m_55305366(); // GlStateManager.enableBlend()

        if (BridgeConfig.backgroundEnabled) {
            C_29058632.m_57734177(0, 0, totalWidth, totalHeight, OverlayStyle.background(0));
            int borderColor = OverlayStyle.part(9);
            C_29058632.m_57734177(0, 0, totalWidth, 1, borderColor);
            C_29058632.m_57734177(0, totalHeight - 1, totalWidth, totalHeight, borderColor);
            C_29058632.m_57734177(0, 0, 1, totalHeight, borderColor);
            C_29058632.m_57734177(totalWidth - 1, 0, totalWidth, totalHeight, borderColor);
        }

        // Draw header
        int textX = padding;
        int textY = padding;
        if (!header.isEmpty()) {
            HudText.drawStyled(0,fr,header, textX, textY, 0xFFFFFF);
            textY += lineHeight;
        }

        for (String line : displayLines) {
            HudText.drawStyled(0,fr,line, textX, textY, 0xFFFFFF);
            textY += lineHeight;
        }

        C_37925375.m_23590488(); // GlStateManager.disableBlend()
    GL11.glPopMatrix();
    }

}
