package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_23014920;
import net.minecraft.unmapped.C_29058632;
import net.minecraft.unmapped.C_23708450;
import net.minecraft.unmapped.C_37925375;
import net.minecraft.unmapped.C_97866173;

import java.util.List;
import java.util.Locale;
import org.lwjgl.opengl.GL11;
import java.util.Arrays;

public class BridgeHUD {
    private static C_23708450 settingsKey;
    private static boolean keyBindingRegistered = false;

    public static void onClientTick() {
        Minecraft mc = Minecraft.m_20213497();
        if (mc == null || mc.f_58088711 == null) return;

        if (!keyBindingRegistered) {
            settingsKey = new C_23708450("key.bridgetap.openSettings", 48, "key.categories.bridgetap");
            mc.f_58088711.f_81621289 = Arrays.copyOf(mc.f_58088711.f_81621289, mc.f_58088711.f_81621289.length + 1);
            mc.f_58088711.f_81621289[mc.f_58088711.f_81621289.length - 1] = settingsKey;
            mc.f_58088711.f_41636781 = Arrays.copyOf(mc.f_58088711.f_41636781, mc.f_58088711.f_41636781.length + 1);
            mc.f_58088711.f_41636781[mc.f_58088711.f_41636781.length - 1] = settingsKey;
            C_23708450.m_72397208();
            keyBindingRegistered = true;
        }

        // Consume the press even with a screen open; never trigger the UI while typing in chat.
        boolean pressed = settingsKey.m_37810647();
        if (pressed && mc.f_70816363 == null) openSettings();
    }

    public static void openSettings() {
        Minecraft mc = Minecraft.m_20213497();
        if (mc != null) mc.m_52715402(new BridgeHistoryScreen());
    }

    public static void render(C_97866173 gui) {
        Minecraft mc = Minecraft.m_20213497();
        if (mc == null || mc.f_28396371 == null || mc.f_26407825 == null) return;
        if (!BridgeConfig.shouldShowHud(mc.f_58088711 != null && mc.f_58088711.f_63869242)) return;

        C_23014920 fr = mc.f_21497584;
        if (fr == null) return;

        List<JumpEntry> entries = BridgeTracker.getEntries();
        if (entries.isEmpty() && !BridgeTracker.isBridging()) {
            return;
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
        } else if (displayMode.equals("COORDS") || displayMode.equals("COORDS_ONLY") || displayMode.equals("TICKS")) {
            header = "";
        } else if (status == BridgeTracker.RunStatus.SUCCESS) {
            header = String.format(Locale.US, "\u00a7aSuccess %.2fs %d jumps, axis: %s", time, jumpCount, axis);
        } else if (status == BridgeTracker.RunStatus.FAILED) {
            header = String.format(Locale.US, "\u00a7cFailed %.2fs %d jumps, axis: %s", time, jumpCount, axis);
        } else {
            header = String.format(Locale.US, "\u00a7eBridging %.2fs %d jumps, axis: %s", time, jumpCount, axis);
        }

        List<String> displayLines = new java.util.ArrayList<>();
        if (displayMode.equals("ALL")) {
            for (JumpEntry entry : entries) displayLines.add(withGainType(entry.getFormattedLine(), entry));
        } else if (displayMode.equals("COORDS")) {
            for (JumpEntry entry : entries) displayLines.add(withGainType(formatCoordinateLine(entry), entry));
        } else if (displayMode.equals("COORDS_ONLY")) {
            for (JumpEntry entry : entries) displayLines.add(formatCoordinateOnlyLine(entry));
        } else if (displayMode.equals("TICKS")) {
            for (JumpEntry entry : entries) {
                if (entry.getType() != JumpEntry.EntryType.PLATES && entry.getType() != JumpEntry.EntryType.PLATES_MINUS_1) {
                    String line = String.format(Locale.US, "\u00a7b\u00bb \u00a7b%s \u00a77(%dt)", entry.getLabel(), entry.getTicksElapsed());
                    displayLines.add(withGainType(line, entry));
                }
            }
        }
        if (header.isEmpty() && displayLines.isEmpty()) return;

        // Calculate box dimensions
        int maxWidth = header.isEmpty() ? 0 : fr.m_09235706(header);
        for (String line : displayLines) {
            int w = fr.m_09235706(line);
            if (w > maxWidth) maxWidth = w;
        }

        int padding = 4;
        int lineHeight = fr.f_30706543 + 2; // 11px per line
        int lineCount = displayLines.size() + (header.isEmpty() ? 0 : 1);
        int totalHeight = lineHeight * lineCount + padding;
        int totalWidth = maxWidth + padding * 2;

        int x = BridgeConfig.hudX;
        int y = BridgeConfig.hudY;
        float scale = (float) BridgeConfig.hudScale;

        // Anchor at the configured position, then scale the complete HUD layout.
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);

        // Render background box and border
        C_37925375.m_55305366(); // GlStateManager.enableBlend()

        if (BridgeConfig.backgroundEnabled) {
            C_29058632.m_57734177(0, 0, totalWidth, totalHeight, 0xA00A111F);
            int borderColor = 0x40FFFFFF;
            C_29058632.m_57734177(0, 0, totalWidth, 1, borderColor);
            C_29058632.m_57734177(0, totalHeight - 1, totalWidth, totalHeight, borderColor);
            C_29058632.m_57734177(0, 0, 1, totalHeight, borderColor);
            C_29058632.m_57734177(totalWidth - 1, 0, totalWidth, totalHeight, borderColor);
        }

        // Draw header
        int textX = padding;
        int textY = padding;
        if (!header.isEmpty()) {
            fr.m_11445413(header, textX, textY, 0xFFFFFF, BridgeConfig.textShadow);
            textY += lineHeight;
        }

        for (String line : displayLines) {
            fr.m_11445413(line, textX, textY, 0xFFFFFF, BridgeConfig.textShadow);
            textY += lineHeight;
        }

        C_37925375.m_23590488(); // GlStateManager.disableBlend()
    GL11.glPopMatrix();
    }

    private static String formatCoordinateLine(JumpEntry entry) {
        int decimal = (int) Math.round(entry.getDecimal() * 1000.0) % 1000;
        if (decimal < 0) decimal += 1000;
        return String.format(Locale.US, "\u00a7b\u00bb \u00a7b%s: \u00a7e.%03d", entry.getLabel(), decimal);
    }

    private static String formatCoordinateOnlyLine(JumpEntry entry) {
        int decimal = (int) Math.round(entry.getDecimal() * 1000.0) % 1000;
        if (decimal < 0) decimal += 1000;
        return String.format(Locale.US, "\u00a7e.%03d", decimal);
    }

    private static String withGainType(String line, JumpEntry entry) {
        if (!BridgeConfig.showGainType || entry.getGainType() == null) return line;
        String color = entry.getGainType().equals("Gain") ? "\u00a7a" : "\u00a7e";
        return line + " " + color + "[" + entry.getGainType().toUpperCase(Locale.US) + "]";
    }
}
