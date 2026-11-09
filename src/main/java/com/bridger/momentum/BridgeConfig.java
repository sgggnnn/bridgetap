package com.bridger.momentum;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class BridgeConfig {
    private static final File CONFIG_FILE = new File("config/bridgetap.properties");

    public static boolean enabled = true;
    public static boolean hideOnF3 = true;
    public static int hudX = 10;
    public static int hudY = 10;
    public static double hudScale = 1.0;
    public static String preferredAxis = "AUTO"; // AUTO, Z, or X
    public static boolean backgroundEnabled = true;
    public static boolean textShadow = true;
    public static boolean showGainType = false;
    public static String displayMode = "ALL"; // ALL, COORDS, COORDS_ONLY, TICKS, or TIME
    public static int tickOffset = -1;
    public static int plateTickOffset = -1;
    public static int turnTicks = 15;

    public static boolean shouldShowHud(boolean debugOpen) { return enabled && !(hideOnF3 && debugOpen); }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Properties props = new Properties();
            props.load(reader);
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            hideOnF3 = Boolean.parseBoolean(props.getProperty("hideOnF3", "true"));
            hudX = Integer.parseInt(props.getProperty("hudX", "10"));
            hudY = Integer.parseInt(props.getProperty("hudY", "10"));
            hudScale = Double.parseDouble(props.getProperty("hudScale", "1.0"));
            if (hudScale < 0.5 || hudScale > 2.0) hudScale = 1.0;
            preferredAxis = props.getProperty("preferredAxis", "AUTO");
            backgroundEnabled = Boolean.parseBoolean(props.getProperty("backgroundEnabled", "true"));
            textShadow = Boolean.parseBoolean(props.getProperty("textShadow", "true"));
            showGainType = Boolean.parseBoolean(props.getProperty("showGainType", "false"));
            displayMode = props.getProperty("displayMode", "ALL").toUpperCase(java.util.Locale.US);
            if (!displayMode.equals("ALL") && !displayMode.equals("COORDS") && !displayMode.equals("COORDS_ONLY") && !displayMode.equals("TICKS") && !displayMode.equals("TIME")) {
                displayMode = "ALL";
            }
            tickOffset = parseOffset(props.getProperty("tickOffset", "-1"), -1);
            plateTickOffset = parseOffset(props.getProperty("plateTickOffset", String.valueOf(tickOffset)), tickOffset);
            turnTicks = Integer.parseInt(props.getProperty("turnTicks", "15"));
            if (turnTicks != 12 && turnTicks != 15) turnTicks = 15;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                Properties props = new Properties();
                props.setProperty("enabled", String.valueOf(enabled));
                props.setProperty("hideOnF3", String.valueOf(hideOnF3));
                props.setProperty("hudX", String.valueOf(hudX));
                props.setProperty("hudY", String.valueOf(hudY));
                props.setProperty("hudScale", String.valueOf(hudScale));
                props.setProperty("preferredAxis", preferredAxis);
                props.setProperty("backgroundEnabled", String.valueOf(backgroundEnabled));
                props.setProperty("textShadow", String.valueOf(textShadow));
                props.setProperty("showGainType", String.valueOf(showGainType));
                props.setProperty("displayMode", displayMode);
                props.setProperty("tickOffset", String.valueOf(tickOffset));
                props.setProperty("plateTickOffset", String.valueOf(plateTickOffset));
                props.setProperty("turnTicks", String.valueOf(turnTicks));
                props.store(writer, "BridgeTap Configuration");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int parseOffset(String value, int fallback) {
        try {
            int offset = Integer.parseInt(value);
            return offset >= -40 && offset <= 40 ? offset : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
