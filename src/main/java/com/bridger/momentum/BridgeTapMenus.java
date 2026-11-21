package com.bridger.momentum;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_85740840;

/** Shared entry points for native OneConfig buttons and the desktop menu bridge. */
public final class BridgeTapMenus {
    private BridgeTapMenus(){}
    private static long nextPoll;
    private static final Path ROOT=Paths.get("config/bridgetap");
    private static void show(C_85740840 screen){Minecraft.m_20213497().m_52715402(screen);}
    public static void open(String id){
        Minecraft mc=Minecraft.m_20213497();
        C_85740840 parent=mc.f_70816363;
        if(mc.f_28396371==null){show(new WorldRequiredScreen(parent));return;}
        switch(id){
            case "replays":show(ReplayFolderScreen.home());break;
            case "tools":show(new ReplayToolsScreen(ReplayFolderScreen.home(),new BridgeHistoryScreen(),null));break;
            case "imports":show(new BridgeImportScreen(new BridgeHistoryScreen(),parent));break;
            case "replay-details":case "share":case "bookmarks":case "video-export":case "fail-analysis":show(ReplayFolderScreen.forTool(id));break;
            case "statistics":show(new StatisticsScreen(parent));break;
            case "replay-storage":show(new ReplayStorageScreen(parent));break;
            case "islands":show(new IslandScreen(parent));break;
            case "island-presets":show(new IslandPresetsScreen(parent));break;
            case "island-import":show(new IslandPresetImportScreen(new IslandPresetsScreen(parent)));break;
            case "island-copy":show(new IslandCopyScreen(new IslandScreen(parent)));break;
            case "island-materials":if(IslandOverlay.activeTemplate()==null)show(new IslandScreen(parent));else show(new IslandMaterialsScreen(new IslandScreen(parent),IslandOverlay.activeTemplate()));break;
            case "island-placement":if(IslandOverlay.activeTemplate()==null)show(new IslandScreen(parent));else show(new IslandPlacementScreen(new IslandScreen(parent)));break;
            case "blocks":show(new PlacementAppearanceScreen(parent));break;
            case "sky":show(new SkyOverlayScreen(parent));break;
            case "sandstone":show(new SkyOverlayScreen(parent,true));break;
            case "theme":show(new MenuThemeScreen(parent));break;
            case "clips":show(new ClipScreen(parent));break;
            case "keybinds":show(new KeybindProfilesScreen(parent));break;
            case "failure-display":show(new FailureDisplayScreen(parent));break;
            case "colors":show(new BridgeColorsScreen(parent));break;
            case "classic-layout":show(new BridgeLayoutScreen(parent));break;
            case "hud-editor":try{Class<?> manager=Class.forName("org.polyfrost.oneconfig.api.hud.v1.HudManager");manager.getMethod("openEditor").invoke(manager.getField("INSTANCE").get(null));}catch(ReflectiveOperationException ex){show(new BridgeLayoutScreen(parent));}break;
            case "settings":show(new BridgeSettingsScreen(parent));break;
            case "guide":show(new BridgeTutorialScreen(parent));break;
            case "updates":UpdateManager.open(parent);break;
            case "discord":CommunityLinks.discord();break;
            default:throw new IllegalArgumentException("Unknown BridgeTap menu: "+id);
        }
    }
    private static final class WorldRequiredScreen extends BridgeScreen {
        private final C_85740840 parent;
        WorldRequiredScreen(C_85740840 parent){this.parent=parent;}
        @Override protected void paint(){
            header("BridgeTap","Join a world to open these tools");
            button("Back",752,15,88,this::back);
            label("Launch your Minecraft profile and join a server or a local world.",40,144);
            label("Then reopen BridgeTap from Right Shift or from the launcher.",40,184);
        }
        @Override protected void back(){open(parent);}
    }
    public static String openReplayTool(String id,C_85740840 parent,RunHistory.Run run){
        try{switch(id){
            case "replay-details":show(new BridgeReplayDetailsScreen(parent,run));break;
            case "share":show(new ReplayShareScreen(parent,run));break;
            case "bookmarks":show(new ReplayBookmarksScreen(parent,run));break;
            case "video-export":show(new ReplayVideoExportScreen(parent,run));break;
            case "fail-analysis":show(new ReplayFailureScreen(parent,run));break;
            default:throw new IllegalArgumentException("Unknown replay tool: "+id);
        }return "";}catch(Exception ex){return "Cannot open replay tool: "+ex.getMessage();}
    }
    public static void pollLauncher(){
        long now=System.currentTimeMillis();if(now<nextPoll)return;nextPoll=now+1000;
        Minecraft mc=Minecraft.m_20213497();if(mc.f_28396371==null)return;
        try{
            Files.createDirectories(ROOT);Files.write(ROOT.resolve("client-menu.ready"),Long.toString(now).getBytes(StandardCharsets.UTF_8));
            Path request=ROOT.resolve("client-menu.request");if(!Files.isRegularFile(request)||Files.size(request)>2048)return;
            Properties props=new Properties();try(InputStream in=Files.newInputStream(request)){props.load(in);}Files.deleteIfExists(request);
            long created=Long.parseLong(props.getProperty("created","0"));if(Math.abs(now-created)>60000)return;
            String result="opened";try{open(props.getProperty("menu",""));}catch(RuntimeException ex){result="error: "+ex.getMessage();}
            Files.write(ROOT.resolve("client-menu.result"),(props.getProperty("request","")+"\n"+result).getBytes(StandardCharsets.UTF_8));
        }catch(Exception ex){System.err.println("[BridgeTap] Launcher menu bridge: "+ex.getMessage());}
    }
}
