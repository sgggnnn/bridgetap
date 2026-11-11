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
    public static int replayHudX=-1,replayHudY=-1,inputHudX=10,inputHudY=10;
    public static double replayHudScale=1.0,inputHudScale=1.0;
    public static String preferredAxis = "AUTO"; // AUTO, Z, or X
    public static boolean backgroundEnabled = true;
    public static boolean textShadow = true;
    public static boolean showGainType = false;
    public static boolean chatOutput = false;
    public static boolean showReplayInfo = true;
    public static boolean showReplayInputs = true;
    public static boolean useNoGayLabel = false;
    public static String replaySort="NEWEST";
    public static String replayQuery="", replayStatus="ALL", replayAxis="ALL", replayTechnique="ALL", replayContent="ALL";
    public static String replaySource="ALL";
    public static String replayMinTime="", replayMaxTime="";
    public static final String[] COLOR_NAMES = {"Labels", "Coordinates", "Positive delta", "Negative delta", "Ticks", "Gain", "No Gain", "Header", "Background", "Border"};
    public static final String[] colors = {"b", "e", "a", "c", "7", "a", "e", "b", "0", "7"};
    public static String color(int part) { return "\u00a7" + colors[part]; }
    public static int rgb(int part) {
        int[] palette={0x000000,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
        return palette[Integer.parseInt(colors[part],16)];
    }
    public static String displayMode = "ALL"; // ALL, COORDS, COORDS_ONLY, TICKS, or TIME
    public static int tickOffset = 0;
    public static int plateTickOffset = 0;
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
            replayHudX=Integer.parseInt(props.getProperty("replayHudX","-1"));replayHudY=Integer.parseInt(props.getProperty("replayHudY","-1"));
            inputHudX=Integer.parseInt(props.getProperty("inputHudX","10"));inputHudY=Integer.parseInt(props.getProperty("inputHudY","10"));
            replayHudScale=validScale(props.getProperty("replayHudScale","1.0"));inputHudScale=validScale(props.getProperty("inputHudScale","1.0"));
            preferredAxis = props.getProperty("preferredAxis", "AUTO");
            backgroundEnabled = Boolean.parseBoolean(props.getProperty("backgroundEnabled", "true"));
            textShadow = Boolean.parseBoolean(props.getProperty("textShadow", "true"));
            showGainType = Boolean.parseBoolean(props.getProperty("showGainType", "false"));
            chatOutput = Boolean.parseBoolean(props.getProperty("chatOutput", "false"));
            showReplayInfo = Boolean.parseBoolean(props.getProperty("showReplayInfo", "true"));
            showReplayInputs = Boolean.parseBoolean(props.getProperty("showReplayInputs", "true"));
            useNoGayLabel = Boolean.parseBoolean(props.getProperty("useNoGayLabel", "false"));
            replaySort=ReplaySort.parse(props.getProperty("replaySort","NEWEST")).name();
            replayQuery=props.getProperty("replayQuery", "");
            replayStatus=props.getProperty("replayStatus", "ALL");
            replaySource=props.getProperty("replaySource","ALL");
            replayAxis=props.getProperty("replayAxis", "ALL");
            replayTechnique=props.getProperty("replayTechnique", "ALL");
            replayContent=props.getProperty("replayContent", "ALL");
            replayMinTime=props.getProperty("replayMinTime", "");
            replayMaxTime=props.getProperty("replayMaxTime", "");
            for(int i=0;i<colors.length;i++){
                String value=props.getProperty("color."+i,colors[i]).toLowerCase(java.util.Locale.US);
                if(value.matches("[0-9a-f]"))colors[i]=value;
            }
            displayMode = props.getProperty("displayMode", "ALL").toUpperCase(java.util.Locale.US);
            if (!displayMode.equals("ALL") && !displayMode.equals("COORDS") && !displayMode.equals("COORDS_ONLY") && !displayMode.equals("LAST_TURN") && !displayMode.equals("TICKS") && !displayMode.equals("TIME")) {
                displayMode = "ALL";
            }
            tickOffset = parseOffset(props.getProperty("tickOffset", "0"), 0);
            plateTickOffset = parseOffset(props.getProperty("plateTickOffset", "0"), 0);
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
                props.setProperty("replayHudX",String.valueOf(replayHudX));props.setProperty("replayHudY",String.valueOf(replayHudY));
                props.setProperty("inputHudX",String.valueOf(inputHudX));props.setProperty("inputHudY",String.valueOf(inputHudY));
                props.setProperty("replayHudScale",String.valueOf(replayHudScale));props.setProperty("inputHudScale",String.valueOf(inputHudScale));
                props.setProperty("preferredAxis", preferredAxis);
                props.setProperty("backgroundEnabled", String.valueOf(backgroundEnabled));
                props.setProperty("textShadow", String.valueOf(textShadow));
                props.setProperty("showGainType", String.valueOf(showGainType));
                props.setProperty("chatOutput", String.valueOf(chatOutput));
                props.setProperty("showReplayInfo", String.valueOf(showReplayInfo));
                props.setProperty("showReplayInputs", String.valueOf(showReplayInputs));
                props.setProperty("useNoGayLabel", String.valueOf(useNoGayLabel));
                props.setProperty("replaySort",replaySort);
                props.setProperty("replayQuery",replayQuery);
                props.setProperty("replayStatus",replayStatus);
                props.setProperty("replaySource",replaySource);
                props.setProperty("replayAxis",replayAxis);
                props.setProperty("replayTechnique",replayTechnique);
                props.setProperty("replayContent",replayContent);
                props.setProperty("replayMinTime",replayMinTime);
                props.setProperty("replayMaxTime",replayMaxTime);
                for(int i=0;i<colors.length;i++)props.setProperty("color."+i,colors[i]);
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

    private static double validScale(String text){double value=Double.parseDouble(text);return Double.isFinite(value)&&value>=.5&&value<=2?value:1;}
    private static int parseOffset(String value, int fallback) {
        try {
            int offset = Integer.parseInt(value);
            return offset >= -40 && offset <= 40 ? offset : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
