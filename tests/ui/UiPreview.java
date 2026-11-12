import com.bridger.momentum.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Renders the real screen paint paths with Minecraft's bitmap font, without GL. */
public final class UiPreview implements UiCanvas {
    private final BufferedImage image=new BufferedImage(860,500,BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage font;
    private final Graphics2D graphics=image.createGraphics();
    private final int[] palette={0,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    UiPreview(BufferedImage font){this.font=font;graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);}
    public void rect(int x,int y,int w,int h,int color){graphics.setColor(new Color(color,true));graphics.fillRect(x,y,w,h);}
    private int charWidth(char c){if(c==' ')return 4;int code=c<256?c:'?',x=(code%16)*8,y=(code/16)*8;for(int col=7;col>=0;col--)for(int row=0;row<8;row++)if((font.getRGB(x+col,y+row)>>>24)>0)return col+2;return 2;}
    public int width(String s){int result=0;for(int i=0;i<s.length();i++){if(s.charAt(i)=='\u00a7'&&i+1<s.length()){i++;continue;}result+=charWidth(s.charAt(i));}return result;}
    public void text(String s,int x,int y,int color){textScaled(s,x,y,color,1);}
    public void textScaled(String s,int x,int y,int color,float scale){
        int cursor=0,tint=color;
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);if(c=='\u00a7'&&i+1<s.length()){int code=Character.digit(s.charAt(++i),16);if(code>=0)tint=0xFF000000|palette[code];continue;}
            int code=c<256?c:'?';BufferedImage glyph=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            for(int row=0;row<8;row++)for(int col=0;col<8;col++){int a=font.getRGB((code%16)*8+col,(code/16)*8+row)>>>24;glyph.setRGB(col,row,(a<<24)|(tint&0xFFFFFF));}
            AffineTransform t=new AffineTransform();t.translate(x+cursor*scale,y);t.scale(scale,scale);graphics.drawImage(glyph,t,null);cursor+=charWidth(c);
        }
    }
    private void save(String name)throws Exception{
        BufferedImage larger=new BufferedImage(1720,1000,BufferedImage.TYPE_INT_ARGB);Graphics2D g=larger.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(image,0,0,1720,1000,null);g.dispose();ImageIO.write(larger,"png",Paths.get(name+".png").toFile());
    }
    private static UiPreview render(BridgeScreen screen,BufferedImage font,String name)throws Exception{UiPreview canvas=new UiPreview(font);screen.drawPreview(canvas,-10,-10);canvas.save(name);return canvas;}
    public static void main(String[] args)throws Exception{
        testDetectionIntegration();
        if(ReplayFolders.local().autoServers())ReplayFolders.local().toggleAutoServers();
        BufferedImage font;try(ZipFile z=new ZipFile(args[0])){font=ImageIO.read(z.getInputStream(z.getEntry("assets/minecraft/textures/font/ascii.png")));}
        List<ReplayData.Block> blocks=Arrays.asList(new ReplayData.Block(0,69,0,1,0,0),new ReplayData.Block(0,74,31,1,0,0),new ReplayData.Block(0,69,1,24,0,0,true),new ReplayData.Block(0,74,30,24,0,1,true));
        ReplayData data=new ReplayData(Arrays.asList(new ReplayData.Frame(.5,70,.5),new ReplayData.Frame(.5,75,31.5)),blocks,false);
        for(int i=9;i>=0;i--){
            BridgeTracker.history().add(i%4==0?"FAILED":"SUCCESS",3.85+i*.12,7+i%3,i%2==0?"X":"Z",Arrays.asList("1.0 Flat: .504 +.n/a (3t)","5b Late: .460 -.044 (9t)","Tap Speed #2: .541 +.081 (9t)","Turn: .216 +.475 (15t)","Plates-1t: .260","Plates: .102"),data,"mc.example.net");
            RunHistory.Run run=BridgeTracker.history().recent().get(0);BridgeTracker.history().rename(run,i==0?"Opening / 1.0 Flat to 5b Late":i==1?"Evening practice / clean run":"Bridge practice "+(i+1));
            if(i%3==0)BridgeTracker.history().toggleFavorite(run);
        }
        BridgeHistoryScreen library=new BridgeHistoryScreen();render(library,font,"library");
        library.clickPreview(700,110,0);render(library,font,"filters");
        library.clickPreview(200,225,0);render(library,font,"axis-options");library.clickPreview(200,294,0);
        render(library,font,"filter-selection");library.clickPreview(600,365,0);render(library,font,"filtered");
        if(!"Z".equals(BridgeConfig.replayAxis))throw new AssertionError("Filter click target did not map to Z");
        library.clickPreview(450,110,0);render(library,font,"sort-options");library.clickPreview(440,158,0);render(library,font,"sorted");
        if(!"FASTEST".equals(BridgeConfig.replaySort))throw new AssertionError("Sort dropdown did not select time ascending");
        library.clickPreview(610,110,0);render(library,font,"descending");if(!"SLOWEST".equals(BridgeConfig.replaySort))throw new AssertionError("Sort direction did not change");
        BridgeSettingsScreen settings=new BridgeSettingsScreen();render(settings,font,"settings");settings.clickPreview(250,246,0);render(settings,font,"pre-jump-velocity-enabled");if(!BridgeConfig.showBridgeVelocity)throw new AssertionError("Pre-jump velocity toggle did not enable");settings.clickPreview(250,246,0);
        settings.clickPreview(250,411,0);render(settings,font,"both-axes-enabled");if(!BridgeConfig.showBothAxes)throw new AssertionError("Both axes toggle did not enable");settings.clickPreview(250,411,0);
        settings.clickPreview(250,455,0);render(settings,font,"jump-angle-enabled");if(!BridgeConfig.showJumpAngle)throw new AssertionError("Jump angle toggle did not enable");settings.clickPreview(250,455,0);
        UpdateManager.installed="1.7.3";UpdateManager.release=new UpdateRelease("1.8.0","BridgeTap-1.8.0-OneClient.jar",new String(new char[64]).replace('\0','a'),100,false);
        UpdateManager.state=UpdateManager.State.AVAILABLE;render(new UpdateScreen(null),font,"update-available");
        UpdateManager.state=UpdateManager.State.DOWNLOADING;UpdateManager.downloaded=50;render(new UpdateScreen(null),font,"update-downloading");
        UpdateManager.state=UpdateManager.State.READY;render(new UpdateScreen(null),font,"update-restart");
        UpdateManager.state=UpdateManager.State.FAILED;UpdateManager.error="Downloaded update checksum mismatch";render(new UpdateScreen(null),font,"update-checksum-error");UpdateManager.state=UpdateManager.State.IDLE;
        BridgeColorsScreen colors=new BridgeColorsScreen();render(colors,font,"colors");colors.clickPreview(65,145,0);render(colors,font,"color-selected");colors.clickPreview(530,150,0);
        if(!"4".equals(BridgeConfig.colors[1]))throw new AssertionError("Swatch click did not update selected coordinate color");BridgeConfig.colors[1]="e";
        colors.clickPreview(65,395,0);colors.clickPreview(530,150,0);render(colors,font,"velo-color-selected");
        if(!"4".equals(BridgeConfig.colors[10])||!"e".equals(BridgeConfig.colors[1]))throw new AssertionError("Velo swatch is unreachable or overwrites coordinates");BridgeConfig.colors[10]="7";
        BridgeLayoutScreen layout=new BridgeLayoutScreen(settings);render(layout,font,"layout");int before=BridgeConfig.hudX;
        layout.clickPreview(15,15,0);layout.dragPreview(140,195);layout.releasePreview();render(layout,font,"layout-dragged");
        if(BridgeConfig.hudX<=before||BridgeConfig.hudY<=10)throw new AssertionError("Studio drag did not update both coordinates");
        java.lang.reflect.Field boundField=BridgeLayoutScreen.class.getDeclaredField("bounds");boundField.setAccessible(true);int[] b=((int[][])boundField.get(layout))[0];
        java.lang.reflect.Field sceneScale=BridgeLayoutScreen.class.getDeclaredField("sceneScale"),sceneX=BridgeLayoutScreen.class.getDeclaredField("sceneX"),sceneY=BridgeLayoutScreen.class.getDeclaredField("sceneY");
        sceneScale.setAccessible(true);sceneX.setAccessible(true);sceneY.setAccessible(true);float scale=sceneScale.getFloat(layout);
        int handleX=sceneX.getInt(layout)+Math.round(b[0]*scale)+Math.round(b[2]*scale),handleY=sceneY.getInt(layout)+Math.round(b[1]*scale)+Math.round(b[3]*scale);
        double oldScale=BridgeConfig.hudScale;layout.clickPreview(handleX,handleY,0);layout.dragPreview(handleX+30,handleY+18);layout.releasePreview();
        if(BridgeConfig.hudScale<=oldScale)throw new AssertionError("Lower-right handle did not resize the selected overlay");render(layout,font,"layout-resized");
        java.lang.reflect.Method key=BridgeLayoutScreen.class.getDeclaredMethod("key",int.class);key.setAccessible(true);key.invoke(layout,15);
        render(layout,font,"layout-fullscreen");b=((int[][])boundField.get(layout))[0];
        layout.clickPreview(b[0]+5,b[1]+5,0);layout.dragPreview(859,499);layout.releasePreview();render(layout,font,"layout-bottom-right");
        b=((int[][])boundField.get(layout))[0];
        if(b[0]+b[2]!=860||b[1]+b[3]!=500)throw new AssertionError("HUD cannot reach the bottom-right screen edges behind the hidden inspector");
        layout.clickPreview(b[0]+5,b[1]+5,0);layout.dragPreview(0,0);layout.releasePreview();render(layout,font,"layout-top-left");
        b=((int[][])boundField.get(layout))[0];
        if(b[0]!=0||b[1]!=0)throw new AssertionError("HUD cannot reach the top-left screen edges");
        // Select keystrokes through the floating inspector, then drag to a screen corner.
        key.invoke(layout,15);render(layout,font,"layout-inspector");layout.clickPreview(650,125,0);render(layout,font,"layout-inputs");
        java.lang.reflect.Field selection=BridgeLayoutScreen.class.getDeclaredField("selected");selection.setAccessible(true);
        if(selection.getInt(layout)!=2)throw new AssertionError("Floating inspector did not select recorded inputs");
        key.invoke(layout,15);render(layout,font,"layout-inputs-fullscreen");b=((int[][])boundField.get(layout))[2];
        layout.clickPreview(b[0]+5,b[1]+5,0);layout.dragPreview(859,499);layout.releasePreview();render(layout,font,"layout-inputs-bottom-right");
        b=((int[][])boundField.get(layout))[2];
        if(b[0]+b[2]!=860||b[1]+b[3]!=500)throw new AssertionError("Recorded inputs cannot reach the bottom-right screen edges");
        java.lang.reflect.Method current=BridgeHistoryScreen.class.getDeclaredMethod("current");current.setAccessible(true);RunHistory.Run selectedRun=(RunHistory.Run)current.invoke(library);
        String folderId=ReplayFolders.local().create("Training bridges");ReplayFolderScreen folders=new ReplayFolderScreen(library,selectedRun);render(folders,font,"replay-folders");
        java.lang.reflect.Field managerRows=ReplayFolderScreen.class.getDeclaredField("rows");managerRows.setAccessible(true);List<RunHistory.Run> managerRuns=(List<RunHistory.Run>)managerRows.get(folders);int managerIndex=0;for(int i=0;i<managerRuns.size();i++)if(managerRuns.get(i).id.equals(selectedRun.id))managerIndex=i;
        java.lang.reflect.Field folderPage=ReplayFolderScreen.class.getDeclaredField("page");folderPage.setAccessible(true);folderPage.setInt(folders,managerIndex/9);render(folders,font,"replay-folder-page");folders.clickPreview(250,218+(managerIndex%9)*25,0);folders.dragPreview(80,257);folders.releasePreview();render(folders,font,"replay-folder-moved");
        if(!ReplayFolders.local().folderOf(selectedRun.id).equals(folderId))throw new AssertionError("Move replay button did not update its folder");
        folders.clickPreview(80,257,0);render(folders,font,"library-folder");
        if(((List<?>)managerRows.get(folders)).size()!=1)throw new AssertionError("Selecting a folder did not show its contents in the middle table");
        IslandLibrary.save(new IslandTemplate("Practice island",1,1,1,0,64,0,Arrays.asList(new IslandTemplate.Block(0,0,0,"minecraft:sandstone",0))),"saved");
        IslandScreen islands=new IslandScreen(library,selectedRun);render(islands,font,"default-islands");
        java.lang.reflect.Field islandTab=IslandScreen.class.getDeclaredField("tab");islandTab.setAccessible(true);if(islandTab.getInt(islands)!=4||IslandLibrary.list("defaults").size()!=6)throw new AssertionError("Default islands are not shown on first open");
        java.util.Set<Path> originals=new java.util.HashSet<>(IslandLibrary.list("saved"));
        islands.clickPreview(490,428,0);render(islands,font,"default-island-saved-copy");
        if(islandTab.getInt(islands)!=0||IslandLibrary.list("saved").size()!=originals.size()+1)throw new AssertionError("Save a copy did not select a personal copy");
        Path defaultCopy=IslandLibrary.list("saved").stream().filter(p->!originals.contains(p)).findFirst().orElseThrow(()->new AssertionError("Missing copied island"));
        if(!IslandTemplate.read(defaultCopy).name.equals("Default"))throw new AssertionError("Copied wrong bundled model");IslandLibrary.delete(defaultCopy,"saved");
        islands.clickPreview(780,72,0);render(islands,font,"default-copy-removed");
        islands.clickPreview(165,72,0);render(islands,font,"island-scenery");
        islands.clickPreview(490,428,0);render(islands,font,"island-rename");java.lang.reflect.Field renameText=IslandScreen.class.getDeclaredField("renameText");renameText.setAccessible(true);renameText.set(islands,"Renamed scenery");render(islands,font,"island-rename-edited");islands.clickPreview(500,305,0);render(islands,font,"island-renamed");
        if(!IslandTemplate.read(IslandLibrary.list("saved").get(0)).name.equals("Renamed scenery"))throw new AssertionError("Rename island button did not save the new name");
        islands.clickPreview(350,160,0);render(islands,font,"island-delete-confirm");islands.clickPreview(250,295,0);render(islands,font,"island-delete-cancel");if(IslandLibrary.list("saved").size()!=1)throw new AssertionError("Cancel deleted the island");
        islands.clickPreview(350,160,0);render(islands,font,"island-delete-confirmed");islands.clickPreview(500,295,0);render(islands,font,"island-empty");if(!IslandLibrary.list("saved").isEmpty())throw new AssertionError("Delete island button did not remove the selected template");
        islands.clickPreview(270,70,0);render(islands,font,"island-copy-from-replay");if(islandTab.getInt(islands)!=1)throw new AssertionError("From replay tab no longer accessible");
        render(new IslandCopyScreen(islands),font,"island-copy-selection");
        java.lang.reflect.Field activeIsland=IslandOverlay.class.getDeclaredField("active"),islandParts=IslandOverlay.class.getDeclaredField("parts");activeIsland.setAccessible(true);islandParts.setAccessible(true);
        IslandTemplate pairedScenery=new IslandTemplate("Paired scenery",22,1,1,0,64,0,Arrays.asList(new IslandTemplate.Block(0,0,0,"minecraft:stone",0),new IslandTemplate.Block(21,0,0,"minecraft:stone",0)),0,1,0,true);activeIsland.set(null,pairedScenery);islandParts.set(null,new IslandParts(pairedScenery));IslandOverlay.resetFinish();
        IslandPlacementScreen placement=new IslandPlacementScreen(islands);render(placement,font,"island-placement-controls");placement.clickPreview(730,90,0);render(placement,font,"island-part-menu");placement.clickPreview(730,143,0);render(placement,font,"island-finish-controls");
        placement.clickPreview(817,190,0);if(IslandOverlay.gap()!=21)throw new AssertionError("Finish gap plus button did not move the arrival island");
        render(placement,font,"island-gap-adjusted");placement.clickPreview(760,190,0);render(placement,font,"island-gap-focused");java.lang.reflect.Method typed=BridgeScreen.class.getDeclaredMethod("m_29116903",char.class,int.class);typed.setAccessible(true);
        typed.invoke(placement,'\0',14);typed.invoke(placement,'\0',14);render(placement,font,"island-gap-empty");typed.invoke(placement,'2',3);typed.invoke(placement,'8',9);render(placement,font,"island-gap-28");
        if(IslandOverlay.gap()!=28)throw new AssertionError("Typing the finish distance did not apply it");placement.clickPreview(780,330,0);render(placement,font,"island-rotated");if(IslandOverlay.rotation()!=90)throw new AssertionError("Rotate island button did not change orientation");placement.clickPreview(730,90,0);render(placement,font,"island-start-menu");placement.clickPreview(730,163,0);render(placement,font,"island-start-controls");placement.clickPreview(817,227,0);if(IslandOverlay.startHeight()!=1||IslandOverlay.finishHeight()!=0)throw new AssertionError("Start height control also moved the finish island");render(placement,font,"island-start-moved");IslandOverlay.disable();
        render(ReplayFolderScreen.home(),font,"replay-library-home");
        ReplayFolderScreen successful=ReplayFolderScreen.home();render(successful,font,"replay-status-all");successful.clickPreview(410,116,0);render(successful,font,"replay-status-menu");successful.clickPreview(410,166,0);render(successful,font,"replay-status-success");
        for(RunHistory.Run run:(List<RunHistory.Run>)managerRows.get(successful))if(!run.status.equals("SUCCESS"))throw new AssertionError("Successful-only filter contains a failed bridge");if(!BridgeConfig.replayStatus.equals("SUCCESS"))throw new AssertionError("Successful filter was not persisted");BridgeConfig.replayStatus="ALL";
        BridgeTracker.history().importReplay(BridgeTracker.history().exportReplay(selectedRun));ReplayFolderScreen importedOnly=ReplayFolderScreen.home();render(importedOnly,font,"replay-source-all");importedOnly.clickPreview(550,116,0);render(importedOnly,font,"replay-source-menu");importedOnly.clickPreview(550,166,0);render(importedOnly,font,"replay-source-imported");List<RunHistory.Run> imports=(List<RunHistory.Run>)managerRows.get(importedOnly);if(imports.size()!=1||!imports.get(0).isImported())throw new AssertionError("Imported-only filter did not isolate the imported replay");if(!BridgeConfig.replaySource.equals("IMPORTED"))throw new AssertionError("Import filter was not persisted");BridgeConfig.replaySource="ALL";
        render(new BridgeReplayDetailsScreen(folders,selectedRun),font,"expanded-bridge-coordinates");
        BridgeTracker.history().add("SUCCESS",3.95,4,"Z",Collections.emptyList(),new ReplayData(Arrays.asList(new ReplayData.Frame(.5,65,.5),new ReplayData.Frame(.5,68,29.5)),Arrays.asList(new ReplayData.Block(0,64,0,1,0,0,false),new ReplayData.Block(0,67,29,1,0,0,false),new ReplayData.Block(0,64,1,24,0,0,true),new ReplayData.Block(0,67,28,24,0,1,true)),false),"mcplayhd.net");ReplayFolderScreen modeLibrary=ReplayFolderScreen.home();render(modeLibrary,font,"replay-mode-all");modeLibrary.clickPreview(290,151,0);render(modeLibrary,font,"replay-mode-menu");modeLibrary.clickPreview(290,225,0);render(modeLibrary,font,"replay-mode-short");List<RunHistory.Run> modeRows=(List<RunHistory.Run>)managerRows.get(modeLibrary);if(modeRows.size()!=1||modeRows.get(0).mode()!=ServerMode.Mode.SHORT||!BridgeConfig.replayMode.equals("SHORT"))throw new AssertionError("Short-mode filter did not isolate the matching McPlay replay");BridgeConfig.replayMode="ALL";

        ReplayVideoExportScreen videoExport=new ReplayVideoExportScreen(folders,selectedRun);render(videoExport,font,"replay-video-export");videoExport.clickPreview(500,181,0);render(videoExport,font,"replay-video-fps");videoExport.clickPreview(500,255,0);java.lang.reflect.Field videoFps=ReplayVideoExportScreen.class.getDeclaredField("fps");videoFps.setAccessible(true);if(videoFps.getInt(videoExport)!=120)throw new AssertionError("Replay video rate dropdown did not select 120 FPS");render(videoExport,font,"replay-video-120");UiPreview exportHud=new UiPreview(font);exportHud.rect(0,0,860,500,0xFF243847);ReplayVideoOverlay.paint(exportHud,860,500,new ReplayData.Frame(1.4,66.1,8.9,0,0,0,0,false,false,0,0,0,24,0,new ReplayData.TickStats(1|4|16|256,0,0,0,1,0,7,12,0,.01,.08,.28)),"Opening / 1.0 Flat to 5b Late",.9,3.85,ReplayVideoOptions.defaults());exportHud.save("replay-video-keystrokes");ReplayVideoOverlay.ending(exportHud,860,500,"3.950");exportHud.save("replay-video-result");RunHistory.Run failedRun=BridgeTracker.history().recent().stream().filter(r->r.status.equals("FAILED")).findFirst().orElse(selectedRun);render(new ReplayFailureScreen(folders,failedRun),font,"replay-failure-analysis");

        IslandPresets.local().save("My bridge layout",pairedScenery,new IslandLayout(0,0,0,0,0,0,8,2,0,1,false));render(new IslandPresetsScreen(library),font,"island-presets");
        IslandPresets.Preset sharedPreset=IslandPresets.local().list().get(0);Path sharedFile=IslandPresetArchive.export(sharedPreset);Files.copy(sharedFile,IslandPresetArchive.folder("imports").resolve(sharedFile.getFileName()));IslandPresetImportScreen presetImport=new IslandPresetImportScreen(new IslandPresetsScreen(library));render(presetImport,font,"island-preset-import");int presetCount=IslandPresets.local().list().size();presetImport.clickPreview(710,155,0);if(IslandPresets.local().list().size()!=presetCount+1)throw new AssertionError("Preset import button did not create the shared layout");render(presetImport,font,"island-preset-imported");

        Files.createDirectories(Paths.get("config"));Files.write(Paths.get("config","bridgetap-clips.properties"),new byte[]{102,112,115,61,92,117,88,88,88,88});
        ClipScreen clips=new ClipScreen(library);render(clips,font,"video-clips");
        if(ClipCapture.fps()!=15||ClipCapture.seconds()!=30||ClipCapture.height()!=720)throw new AssertionError("Malformed clip preferences did not fall back to defaults");
        clips.clickPreview(350,185,0);render(clips,font,"video-buffer-on");if(!ClipCapture.enabled())throw new AssertionError("Video buffer toggle did not enable capture");
        clips.clickPreview(350,185,0);if(ClipCapture.enabled())throw new AssertionError("Video buffer toggle did not stop capture");
        render(clips,font,"video-buffer-off");clips.clickPreview(350,230,0);render(clips,font,"video-frame-rates");clips.clickPreview(350,349,0);
        if(ClipCapture.fps()!=60)throw new AssertionError("60 FPS menu option did not change capture rate");
        render(clips,font,"video-60fps");clips.clickPreview(350,230,0);render(clips,font,"video-frame-rates-120");clips.clickPreview(350,371,0);
        if(ClipCapture.fps()!=120)throw new AssertionError("120 FPS menu option did not change capture rate");
        render(clips,font,"video-120fps");
        render(new PlacementAppearanceScreen(library),font,"block-appearance");
        UiPreview inputs=new UiPreview(font);inputs.rect(0,0,860,500,0xFF101722);ReplayInputOverlay.paint(new ScaledUiCanvas(inputs,50,30,3),0,0,new ReplayData.TickStats(1|4|16|256,0,0,0,0,0,0,0,0,0,0,0));inputs.save("inputs");
        System.out.println("PASS: real screen previews, filter/sort hit targets, color selection and HUD dragging without GL");
    }
    private static void testDetectionIntegration()throws Exception{
        java.lang.reflect.Method jump=BridgeTracker.class.getDeclaredMethod("recordJump",double.class,double.class,double.class,int.class,double.class);jump.setAccessible(true);
        for(int ticks:new int[]{3,4}){
            BridgeTracker.reset();BridgeConfig.preferredAxis="AUTO";
            java.lang.reflect.Field startZ=BridgeTracker.class.getDeclaredField("startZ");startZ.setAccessible(true);startZ.setDouble(null,.5);
            PlacementEvidence.record(1,0,0,1,.5);jump.invoke(null,.5,70.0,.5,ticks,1.20);
            if(!"X".equals(BridgeTracker.getCurrentAxis()))throw new AssertionError("AUTO axis detection ignored a short opening on X");
            if(!BridgeTracker.getEntries().get(0).getLabel().equals(ticks==3?"1.0 Flat":"1.05 Flat"))throw new AssertionError("Tracker did not use the opening flat rule");
            PlacementEvidence.record(ticks+2,1,0,2,.5);PlacementEvidence.record(ticks+5,2,0,3,.5);
            jump.invoke(null,3.5,71.0,.5,ticks+9,1.20);
            if(!BridgeTracker.getEntries().get(1).getLabel().equals(ticks==3?"5b Late":"6b Late"))throw new AssertionError("Tracker did not apply late geometry to the first follow-up jump");
            PlacementEvidence.record(ticks+13,4,0,5,.5);PlacementEvidence.record(ticks+16,5,0,6,.5);
            jump.invoke(null,6.5,72.0,.5,ticks+18,1.20);
            if(BridgeTracker.getEntries().get(2).getLabel().contains("Late"))throw new AssertionError("A later jump was mislabeled as an opening late");
        }
        BridgeTracker.reset();BridgeConfig.preferredAxis="AUTO";
        System.out.println("PASS: tracker integration for 1.0/1.05 flats, 5b/6b lates and opening-only labels");
    }
}
