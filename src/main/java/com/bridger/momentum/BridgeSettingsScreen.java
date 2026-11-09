package com.bridger.momentum;

import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_29058632;
import net.minecraft.unmapped.C_37925375;
import net.minecraft.unmapped.C_85740840;

import java.util.Locale;
import org.lwjgl.opengl.GL11;

/** Modal native Minecraft screen for editing BridgeTap HUD settings. */
public class BridgeSettingsScreen extends C_85740840 {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 333;

    @Override
    public void m_93956703(int mouseX, int mouseY, float partialTicks) {
        super.m_93956703(mouseX, mouseY, partialTicks);
        C_37925375.m_55305366();
        drawRect(0, 0, f_05230747, f_07021018, 0xC008101B);

        float scale = panelScale();
        int x = ((int)(f_05230747 / scale) - PANEL_WIDTH) / 2;
        int y = ((int)(f_07021018 / scale) - PANEL_HEIGHT) / 2;
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0f);
        drawRect(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xF0162232);
        drawRect(x, y, PANEL_WIDTH, 2, 0xFF53D8E8);
        drawText("BridgeTap Settings", x + 12, y + 12, 0xFF53D8E8);
        drawButton(x + 10, y + 27, 108, 18, "HUD: " + onOff(BridgeConfig.enabled));
        drawButton(x + 124, y + 27, 108, 18, "Hide F3: " + onOff(BridgeConfig.hideOnF3));
        drawButton(x + 238, y + 27, 112, 18, "History");

        drawAdjustRow(x, y + 48, "HUD X", String.valueOf(BridgeConfig.hudX));
        drawAdjustRow(x, y + 72, "HUD Y", String.valueOf(BridgeConfig.hudY));
        drawAdjustRow(x, y + 96, "HUD size", String.format(Locale.US, "%.1fx", BridgeConfig.hudScale));
        drawButton(x + 10, y + 120, PANEL_WIDTH - 20, 20, "Background: " + onOff(BridgeConfig.backgroundEnabled));
        drawButton(x + 10, y + 144, PANEL_WIDTH - 20, 20, "Display: " + BridgeConfig.displayMode);
        drawButton(x + 10, y + 168, PANEL_WIDTH - 20, 20, "Text shadow: " + onOff(BridgeConfig.textShadow));
        drawAdjustRow(x, y + 192, "Jump offset", signed(BridgeConfig.tickOffset));
        drawAdjustRow(x, y + 216, "Plate offset", signed(BridgeConfig.plateTickOffset));
        drawButton(x + 10, y + 240, PANEL_WIDTH - 20, 20, "Turn detection: " + BridgeConfig.turnTicks + " ticks");
        drawButton(x + 10, y + 264, PANEL_WIDTH - 20, 20, "Gain type: " + onOff(BridgeConfig.showGainType));
        drawButton(x + 130, y + 303, 100, 20, "Done (Esc)");
        GL11.glPopMatrix();
        C_37925375.m_23590488();
    }

    @Override
    protected void m_23755539(int mouseX, int mouseY, int button) {
        if (button != 0) return;
        float scale = panelScale();
        mouseX = (int)(mouseX / scale); mouseY = (int)(mouseY / scale);
        int x = ((int)(f_05230747 / scale) - PANEL_WIDTH) / 2;
        int y = ((int)(f_07021018 / scale) - PANEL_HEIGHT) / 2;

        if (inside(mouseX, mouseY, x + 10, y + 27, 108, 18)) {
            BridgeConfig.enabled = !BridgeConfig.enabled;
        } else if (inside(mouseX, mouseY, x + 124, y + 27, 108, 18)) {
            BridgeConfig.hideOnF3 = !BridgeConfig.hideOnF3;
        } else if (inside(mouseX, mouseY, x + 238, y + 27, 112, 18)) {
            Minecraft.m_20213497().m_52715402(new BridgeHistoryScreen());
            return;
        } else if (inside(mouseX, mouseY, x + 250, y + 48, 34, 20)) {
            BridgeConfig.hudX -= 5;
        } else if (inside(mouseX, mouseY, x + 305, y + 48, 34, 20)) {
            BridgeConfig.hudX += 5;
        } else if (inside(mouseX, mouseY, x + 250, y + 72, 34, 20)) {
            BridgeConfig.hudY -= 5;
        } else if (inside(mouseX, mouseY, x + 305, y + 72, 34, 20)) {
            BridgeConfig.hudY += 5;
        } else if (inside(mouseX, mouseY, x + 250, y + 96, 34, 20)) {
            BridgeConfig.hudScale = Math.max(0.5, Math.round((BridgeConfig.hudScale - 0.1) * 10.0) / 10.0);
        } else if (inside(mouseX, mouseY, x + 305, y + 96, 34, 20)) {
            BridgeConfig.hudScale = Math.min(2.0, Math.round((BridgeConfig.hudScale + 0.1) * 10.0) / 10.0);
        } else if (inside(mouseX, mouseY, x + 10, y + 120, PANEL_WIDTH - 20, 20)) {
            BridgeConfig.backgroundEnabled = !BridgeConfig.backgroundEnabled;
        } else if (inside(mouseX, mouseY, x + 10, y + 144, PANEL_WIDTH - 20, 20)) {
            cycleDisplayMode();
        } else if (inside(mouseX, mouseY, x + 10, y + 168, PANEL_WIDTH - 20, 20)) {
            BridgeConfig.textShadow = !BridgeConfig.textShadow;
        } else if (inside(mouseX, mouseY, x + 250, y + 192, 34, 20)) {
            BridgeConfig.tickOffset = Math.max(-40, BridgeConfig.tickOffset - 1);
        } else if (inside(mouseX, mouseY, x + 305, y + 192, 34, 20)) {
            BridgeConfig.tickOffset = Math.min(40, BridgeConfig.tickOffset + 1);
        } else if (inside(mouseX, mouseY, x + 250, y + 216, 34, 20)) {
            BridgeConfig.plateTickOffset = Math.max(-40, BridgeConfig.plateTickOffset - 1);
        } else if (inside(mouseX, mouseY, x + 305, y + 216, 34, 20)) {
            BridgeConfig.plateTickOffset = Math.min(40, BridgeConfig.plateTickOffset + 1);
        } else if (inside(mouseX, mouseY, x + 10, y + 240, PANEL_WIDTH - 20, 20)) {
            BridgeConfig.turnTicks = BridgeConfig.turnTicks == 15 ? 12 : 15;
        } else if (inside(mouseX, mouseY, x + 10, y + 264, PANEL_WIDTH - 20, 20)) {
            BridgeConfig.showGainType = !BridgeConfig.showGainType;
        } else if (inside(mouseX, mouseY, x + 130, y + 303, 100, 20)) {
            close();
            return;
        } else {
            return;
        }
        BridgeConfig.save();
    }

    @Override
    protected void m_29116903(char typedChar, int keyCode) {
        if (keyCode == 1) {
            close();
            return;
        }
        super.m_29116903(typedChar, keyCode);
    }

    private float panelScale() {
        return Math.min(1.0f, Math.min((f_05230747 - 8.0f) / PANEL_WIDTH, (f_07021018 - 8.0f) / PANEL_HEIGHT));
    }

    private void drawAdjustRow(int x, int y, String label, String value) {
        drawText(label + ": " + value, x + 12, y + 6, 0xFFFFFFFF);
        drawButton(x + 250, y, 34, 20, "-");
        drawButton(x + 305, y, 34, 20, "+");
    }

    private void drawButton(int x, int y, int width, int height, String label) {
        drawRect(x, y, width, height, 0xFF2C3A4B);
        drawRect(x, y, width, 1, 0xFF61758A);
        int textX = x + (width - f_11884601.m_09235706(label)) / 2;
        int textY = y + (height - f_11884601.f_30706543) / 2;
        drawText(label, textX, textY, 0xFFFFFFFF);
    }

    private void drawText(String text, int x, int y, int color) {
        f_11884601.m_11445413(text, x, y, color, BridgeConfig.textShadow);
    }

    private static void drawRect(int x, int y, int width, int height, int color) {
        C_29058632.m_57734177(x, y, x + width, y + height, color);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private static void cycleDisplayMode() {
        if (BridgeConfig.displayMode.equals("ALL")) BridgeConfig.displayMode = "COORDS";
        else if (BridgeConfig.displayMode.equals("COORDS")) BridgeConfig.displayMode = "COORDS_ONLY";
        else if (BridgeConfig.displayMode.equals("COORDS_ONLY")) BridgeConfig.displayMode = "TICKS";
        else if (BridgeConfig.displayMode.equals("TICKS")) BridgeConfig.displayMode = "TIME";
        else BridgeConfig.displayMode = "ALL";
    }

    private void close() {
        Minecraft mc = Minecraft.m_20213497();
        BridgeConfig.save();
        if (mc != null) mc.m_52715402(null);
    }
}
