package com.bridger.momentum;

import java.io.File;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_80955943;
import net.minecraft.unmapped.C_85740840;

/**
 * Screen to create standalone custom sky overlay resource packs from any active or installed pack,
 * with structured hierarchical folder navigation.
 */
public final class SkyOverlayScreen extends BridgeScreen {
    private final C_85740840 parent;
    private final boolean sandstone;
    private static final java.util.concurrent.ExecutorService PACK_INDEX=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"BridgeTap overlay sources");t.setDaemon(true);return t;});
    private java.util.concurrent.CompletableFuture<Map<File,Integer>> compatibleWork;
    private final Map<File,Integer> compatible=new HashMap<>();
    private boolean sourcesReady;
    private void finishSources(){
        if(compatibleWork==null||!compatibleWork.isDone())return;
        try{compatible.clear();compatible.putAll(compatibleWork.join());sourcesReady=true;
            if(selectedPack==null||!compatible.containsKey(selectedPack.file)){
                selectedPack=null;preview=null;previewGeneration++;scan=null;
                for(SkyOverlayCreator.PackEntry p:allPacks)if(compatible.containsKey(p.file)){selectPack(p);currentPath=p.folderCategory;break;}
                if(selectedPack==null)previewStatus="No compatible "+(sandstone?"sandstone":"sky")+" packs found. Add one to resourcepacks and refresh.";
            }
            updateFolderView();
        }catch(RuntimeException ex){previewStatus="Could not index packs. Refresh to try again.";}finally{compatibleWork=null;}
    }
    private boolean supported(SkyOverlayCreator.PackEntry pack){return sourcesReady&&compatible.containsKey(pack.file);}

    private static final java.util.concurrent.ExecutorService PREVIEWS=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"BridgeTap overlay previews");t.setDaemon(true);return t;});
    private boolean recolorEnabled;private int recolorColor=0x8B5CF6;private String colorHex="8B5CF6";private int previewLayer,previewGeneration,loadingGeneration;private long previewChanged;
    private OverlayRecolor.Preview preview;private String previewStatus="Select a source pack to preview its textures.";
    private java.util.concurrent.CompletableFuture<OverlayRecolor.Preview> previewWork;
    @Override protected int canvasHeight(){return 720;}
    private OverlayRecolor.Selection selection;private String selectionPath;
    private OverlayRecolor.Settings recolor(){OverlayRecolor.Settings settings=recolorEnabled?OverlayRecolor.Settings.color(recolorColor):OverlayRecolor.Settings.original();return selectionPath==null?settings:settings.selected(selectionPath,selection);}

    private void previewChanged(){previewGeneration++;previewChanged=System.nanoTime();previewStatus="Updating preview...";}
    private void updatePreview(){
        if(previewWork!=null&&previewWork.isDone()){
            try{OverlayRecolor.Preview ready=previewWork.join();if(loadingGeneration==previewGeneration){preview=ready;previewStatus="";}}
            catch(RuntimeException failure){if(loadingGeneration==previewGeneration){preview=null;Throwable cause=failure;while(cause.getCause()!=null)cause=cause.getCause();previewStatus=cause instanceof java.io.IOException?cause.getMessage():"Could not load this texture preview. Try another layer.";}}
            previewWork=null;
        }
        if(selectedPack!=null&&previewWork==null&&loadingGeneration!=previewGeneration&&System.nanoTime()-previewChanged>120000000L){
            loadingGeneration=previewGeneration;final java.io.File source=selectedPack.file;final boolean red=includeCelestials;final int layer=previewLayer;final OverlayRecolor.Settings settings=recolor();
            final OverlayRecolor.Preview cached=preview!=null&&Math.floorMod(layer,preview.total)==preview.index?preview:null;
            previewWork=java.util.concurrent.CompletableFuture.supplyAsync(()->{try{return cached==null?OverlayRecolor.preview(source,sandstone,red,layer,settings):OverlayRecolor.recolor(cached,settings);}catch(java.io.IOException ex){throw new java.util.concurrent.CompletionException(ex);}},PREVIEWS);
        }
    }
    private void previewImage(java.awt.image.BufferedImage image,int x,int y,int w,int h){
        rounded(x,y,w,h,UiTheme.alpha(0x000000,.22));
        for(int row=0;row<h;row+=10)for(int col=0;col<w;col+=10)rect(x+col,y+row,Math.min(10,w-col),Math.min(10,h-row),((row/10+col/10)&1)==0?0xFF343B45:0xFF464D57);
        double scale=Math.min(w/(double)image.getWidth(),h/(double)image.getHeight());double iw=image.getWidth()*scale,ih=image.getHeight()*scale;ui.image(image,x+(w-iw)/2,y+(h-ih)/2,iw,ih,0xFFFFFFFF);
    }
    private final List<SkyOverlayCreator.PackEntry> allPacks = new ArrayList<>();
    private final List<SkyOverlayCreator.PackEntry> filteredPacks = new ArrayList<>();
    private SkyOverlayCreator.FolderNode rootFolder;
    private String currentPath = "";
    private int folderPage = 0;
    private SkyOverlayCreator.PackEntry selectedPack;
    private String search = "";
    private String overlayName = "";
    private boolean includeCelestials = true;
    private volatile boolean creating = false;
    private volatile int selectedSkyCount = -1;
    private int page = 0;
    private static final int PAGE_SIZE = 7;
    private String message = "Select a texture pack with custom sky to create an overlay.";
    private int messageColor = MUTED;
    private File lastCreatedFile;
    private java.util.concurrent.CompletableFuture<SkyOverlayCreator.CreateResult> creation;
    private java.util.concurrent.CompletableFuture<Integer> scan;

    private static final class FolderItem {
        final boolean isUp;
        final boolean isFolder;
        final SkyOverlayCreator.FolderNode folder;
        final SkyOverlayCreator.PackEntry pack;

        FolderItem(boolean isUp, SkyOverlayCreator.FolderNode folder, SkyOverlayCreator.PackEntry pack) {
            this.isUp = isUp;
            this.isFolder = folder != null;
            this.folder = folder;
            this.pack = pack;
        }
    }

    private final List<FolderItem> currentItems = new ArrayList<>();

    public SkyOverlayScreen(C_85740840 parent) {
        this(parent, false);
    }

    public SkyOverlayScreen(C_85740840 parent, boolean sandstone) {
        this.parent = parent;
        this.sandstone = sandstone;
        message = sandstone ? "Select a pack to extract its sandstone textures." : message;
        reloadPacks();
    }

    private String outputFolder() { return sandstone ? SandstoneOverlayCreator.OUTPUT_FOLDER : SkyOverlayCreator.OUTPUT_FOLDER; }

    private void reloadPacks() {
        allPacks.clear();
        Minecraft mc = Minecraft.m_20213497();
        if (mc != null) {
            allPacks.addAll(SkyOverlayCreator.listAvailablePacks(mc));
            if (mc.m_21067396() != null && mc.m_21067396().m_08141491() != null) {
                Set<String> activePaths = SkyOverlayCreator.getActivePackPaths(mc);
                rootFolder = SkyOverlayCreator.buildFolderTree(mc.m_21067396().m_08141491(), activePaths);
            }
        }
        sourcesReady=false;final List<SkyOverlayCreator.PackEntry> candidates=new ArrayList<>(allPacks);
        compatibleWork=java.util.concurrent.CompletableFuture.supplyAsync(()->{
            Map<File,Integer> found=new HashMap<>();
            for(SkyOverlayCreator.PackEntry p:candidates)try{int count=OverlayRecolor.textures(p.file,sandstone,true).size();if(count>0)found.put(p.file,count);}catch(java.io.IOException|RuntimeException ignored){}
            return found;
        },PACK_INDEX);
        updateFolderView();
    }

    private void selectPack(SkyOverlayCreator.PackEntry pack) {
        if(selectedPack==null||pack==null||!selectedPack.file.equals(pack.file)){selection=null;selectionPath=null;}
        this.selectedPack = pack;
        preview=null;previewLayer=0;previewChanged();
        selectedSkyCount = -1;
        if (pack != null) {
            this.overlayName = sandstone ? SandstoneOverlayCreator.defaultName(pack.file) : SkyOverlayCreator.defaultOverlayName(pack.file);
            final boolean red = includeCelestials;
            scan = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try { return sandstone ? SandstoneOverlayCreator.scan(pack.file, red) : pack.skyCount(); }
                catch (Exception ex) { throw new java.util.concurrent.CompletionException(ex); }
            }, ReplaySharing.WORKER);

        }
    }

    private void finishScan() {
        if(scan==null||!scan.isDone())return;
        try {selectedSkyCount=scan.join();message=selectedSkyCount>0?selectedSkyCount+" files detected. Ready to create overlay.":"No supported textures found in this pack.";messageColor=selectedSkyCount>0?GREEN:MUTED;}
        catch(RuntimeException ex){selectedSkyCount=0;message="Could not scan this pack. Check that its textures are valid and not oversized.";messageColor=RED;}
        finally{scan=null;}
    }
    private void updateFolderView() {
        currentItems.clear();
        filteredPacks.clear();
        String q = search.trim().toLowerCase(Locale.ROOT);

        if (!q.isEmpty() || rootFolder == null) {
            // Flat / Search view
            for (SkyOverlayCreator.PackEntry p : allPacks) {
                String searchTarget = p.displayName + " " + p.folderCategory + " " + p.relativePath;
                if (supported(p) && (q.isEmpty() || searchTarget.toLowerCase(Locale.ROOT).contains(q))) {
                    filteredPacks.add(p);
                }
            }
        } else {
            // Structured folder view
            SkyOverlayCreator.FolderNode current = SkyOverlayCreator.findNode(rootFolder, currentPath);
            for (SkyOverlayCreator.PackEntry pack : current.packs) {
                if(supported(pack))currentItems.add(new FolderItem(false, null, pack));
            }
        }
        page = 0;
    }

    private void openFolder(String path) {
        this.currentPath = path;
        this.page = 0;
        folderPage = 0;
        updateFolderView();
    }

    private void upFolder() {
        if (currentPath.contains("/")) {
            currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
        } else {
            currentPath = "";
        }
        this.page = 0;
        folderPage = 0;
        updateFolderView();
    }

    private void createOverlay() {
        if (selectedPack == null || creating) return;
        Minecraft mc = Minecraft.m_20213497();
        if (mc == null || mc.m_21067396() == null) {
            message = "Minecraft resource pack repository is unavailable.";
            messageColor = RED;
            return;
        }
        File packDir = mc.m_21067396().m_08141491();
        if (packDir == null) {
            message = "Resource packs directory not found.";
            messageColor = RED;
            return;
        }

        creating = true;
        message = sandstone ? "Extracting sandstone textures..." : "Extracting custom sky layers...";
        messageColor = ACCENT;

        final File source = selectedPack.file;
        final String title = overlayName;
        final boolean celestial = includeCelestials;
        final OverlayRecolor.Settings colors = recolor();
        creation = java.util.concurrent.CompletableFuture.supplyAsync(
                () -> sandstone ? SandstoneOverlayCreator.createOverlay(source, packDir, title, celestial, colors) : SkyOverlayCreator.createOverlay(source, packDir, title, celestial, colors), ReplaySharing.WORKER);
    }

    private void finishCreation() {
        if (creation == null || !creation.isDone()) return;
        try {
            SkyOverlayCreator.CreateResult res = creation.join();
            if (res.success) {
                lastCreatedFile = res.outputFile;
                reloadPacks();
                currentPath = outputFolder();
                updateFolderView();
                message = "Created in resourcepacks/" + outputFolder() + "/";
                messageColor = GREEN;
            } else { message = res.message; messageColor = RED; }
        } catch (RuntimeException ex) {
            message = "Could not create this overlay. Your existing packs were kept.";
            messageColor = RED;
        } finally { creation = null; creating = false; }
    }

    private void activateCreated() {
        if (lastCreatedFile == null) return;
        Minecraft mc = Minecraft.m_20213497();
        boolean ok = SkyOverlayCreator.activateOverlay(mc, lastCreatedFile);
        if (ok) {
            message = "Overlay activated on top! Minecraft resources reloaded.";
            messageColor = GREEN;
            reloadPacks();
        } else {
            message = "Could not automatically activate overlay. Use Resource Packs menu.";
            messageColor = RED;
        }
    }

    private void openFolderLocation() {
        Minecraft mc = Minecraft.m_20213497();
        if (mc != null && mc.m_21067396() != null && mc.m_21067396().m_08141491() != null) {
            try {
                File dir = mc.m_21067396().m_08141491();
                if (!currentPath.isEmpty()) {
                    File sub = new File(dir, currentPath);
                    if (sub.isDirectory()) dir = sub;
                }
                BridgeFileActions.open(dir.toPath());
                message = "Opened folder: " + dir.getName();
                messageColor = TEXT;
            } catch (Exception ex) {
                message = "Failed to open folder: " + ex.getMessage();
                messageColor = RED;
            }
        }
    }

    @Override
    protected void paint() {
        finishSources();
        finishCreation();
        finishScan();
        updatePreview();
        header(sandstone ? "Sandstone Overlay" : "Custom Sky Overlay", "Extract textures into a separate pack / Your source stays unchanged");
        button(sandstone ? "Sky overlay" : "Sandstone overlay", 420, 15, 168, () -> open(new SkyOverlayScreen(parent, !sandstone)));
        button("Refresh packs", 600, 15, 140, this::reloadPacks);
        button("Back", 752, 15, 88, this::back);

        panel(20, 74, 164, 390);
        panel(194, 74, 320, 390);
        panel(526, 74, 314, 328);
        text("FOLDERS", 30, 90, TEXT);
        button("All resource packs", 30, 108, 144, 24, true, currentPath.isEmpty(), () -> openFolder(""));
        button("Parent folder", 30, 140, 144, 24, !currentPath.isEmpty(), false, this::upFolder);
        SkyOverlayCreator.FolderNode current = SkyOverlayCreator.findNode(rootFolder, currentPath);
        int folders = current == null ? 0 : current.subfolders.size();
        int folderPages = Math.max(1, (folders + 7) / 8);
        folderPage = Math.max(0, Math.min(folderPage, folderPages - 1));
        for (int i = folderPage * 8; i < Math.min(folders, folderPage * 8 + 8); i++) {
            SkyOverlayCreator.FolderNode folder = current.subfolders.get(i);
            int y = 180 + (i % 8) * 28;
            rect(30, y + 7, 9, 8, 0xFFE0C16B);
            button(folder.name + " (" + folder.totalPacks + ")", 44, y, 130, 24,
                    true, false, () -> openFolder(folder.relativePath));
        }
        if (folders == 0) label("No subfolders", 30, 184);
        button("<", 30, 422, 32, 24, folderPage > 0, false, () -> folderPage--);
        button(">", 142, 422, 32, 24, folderPage + 1 < folderPages, false, () -> folderPage++);
        clipped((folderPage + 1) + " / " + folderPages, 74, 430, 60, MUTED);
        text("TEXTURE PACKS", 206, 90, TEXT);
        input("search", search, "Search all folders...", 206, 108, 296, 64, v -> {
            search = v;
            updateFolderView();
        });
        boolean isFolderMode = search.trim().isEmpty() && rootFolder != null;
        clipped(isFolderMode ? (currentPath.isEmpty() ? "resourcepacks /" : currentPath.replace("/", " / "))
                : "Search results / all folders", 206, 144, 296, ACCENT);
        int totalItems = isFolderMode ? currentItems.size() : filteredPacks.size();
        int totalPages = Math.max(1, (totalItems + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, totalPages - 1));
        for (int i = page * PAGE_SIZE; i < Math.min((page + 1) * PAGE_SIZE, totalItems); i++) {
            SkyOverlayCreator.PackEntry pack = isFolderMode ? currentItems.get(i).pack : filteredPacks.get(i);
            int y = 166 + (i % PAGE_SIZE) * 35;
            boolean selected = selectedPack != null && selectedPack.file.equals(pack.file);
            button("", 206, y, 296, 32, true, selected, () -> selectPack(pack));
            clipped(pack.displayName, 214, y + 5, pack.active ? 234 : 280, selected ? UiTheme.onAccent(ACCENT) : TEXT);
            clipped(pack.folderCategory.isEmpty() ? "resourcepacks" : pack.folderCategory,
                    214, y + 20, 280, selected ? UiTheme.onAccent(ACCENT) : MUTED);
            if (pack.active) text("ON", 480, y + 5, selected?UiTheme.onAccent(ACCENT):GREEN);
        }
        if (totalItems == 0) clipped(!sourcesReady?"Finding compatible packs...":isFolderMode?"No compatible packs here. Choose a folder.":"No matching compatible packs.",206,180,296,MUTED);
        button("Previous", 206, 422, 80, 24, page > 0, false, () -> page--);
        button("Next", 292, 422, 64, 24, page + 1 < totalPages, false, () -> page++);
        clipped(totalItems + " packs / " + (page + 1) + "/" + totalPages, 366, 430, 136, MUTED);

        // Right Panel: Generator Controls
        text(sandstone ? "SANDSTONE OVERLAY GENERATOR" : "SKY OVERLAY GENERATOR", 538, 92, TEXT);
        label("Selected source pack", 538, 114);
        if (selectedPack != null) {
            String category = selectedPack.folderCategory.isEmpty() ? "" : "[" + selectedPack.folderCategory + "] ";
            clipped(category + selectedPack.displayName, 538, 128, 290, ACCENT);
            int count = selectedSkyCount;
            if (count > 0) {
                text((sandstone ? "Sandstone detected: " : "Custom sky detected: ") + count + " files", 538, 146, GREEN);
            } else if (count < 0) {
                text(sandstone ? "Scanning sandstone textures..." : "Scanning custom sky...", 538, 146, MUTED);
            } else {
                text(sandstone ? "No sandstone textures found" : "No OptiFine/MCPatcher sky files found", 538, 146, MUTED);
            }
        } else {
            text("No pack selected", 538, 128, MUTED);
        }

        label("New overlay name (.zip)", 538, 172);
        input("overlayName", overlayName, "Overlay pack name...", 538, 188, 290, 64, v -> overlayName = v);

        toggle(sandstone ? "Include red sandstone" : "Include custom sun & moon", includeCelestials, 538, 226, 290, () -> { includeCelestials = !includeCelestials; {String title=overlayName;selectPack(selectedPack);overlayName=title;} });

        boolean canCreate = selectedPack != null && selectedSkyCount > 0 && !overlayName.trim().isEmpty();
        button(creating ? "Extracting files..." : sandstone ? "Create Sandstone Overlay" : "Create Sky Overlay", 538, 264, 290, 32, canCreate && !creating, true, this::createOverlay);

        clipped(message, 538, 306, 290, messageColor);

        button("Activate in Minecraft now", 538, 334, 290, 26, lastCreatedFile != null && !creating, false, this::activateCreated);
        button("Resource Packs", 538, 368, 140, 26, true, false, () -> open(new C_80955943(this)));
        button("Open folder", 688, 368, 140, 26, true, false, this::openFolderLocation);

        panel(20,480,494,192);text("LIVE TEXTURE PREVIEW",32,491,TEXT);
        button("<",420,486,34,24,preview!=null&&preview.total>1,false,()->{previewLayer--;previewChanged();});
        button(">",464,486,34,24,preview!=null&&preview.total>1,false,()->{previewLayer++;previewChanged();});
        if(preview!=null){
            label("Original",32,520);label(recolorEnabled?"Recolored":"Original (recolor OFF)",276,520);
            previewImage(preview.original,32,540,228,102);previewImage(preview.colored,276,540,226,102);
            clipped((preview.index+1)+"/"+preview.total+" / "+preview.path,32,650,468,MUTED);
        }else {
            clipped(selectedPack==null?"No source selected":selectedPack.displayName,32,530,464,ACCENT);
            String status=previewStatus;int lineY=554;
            while(!status.isEmpty()&&lineY<625){int end=status.length();while(end>1&&ui.width(status.substring(0,end))>456)end--;text(status.substring(0,end),32,lineY,MUTED);status=status.substring(end);lineY+=18;}
            if(selectedPack!=null&&previewWork==null)button("Reload preview",32,636,170,24,true,false,()->{preview=null;previewChanged();});
        }
        if(preview!=null&&!previewStatus.isEmpty())clipped(previewStatus,32,560,464,ACCENT);
        panel(526,410,314,262);text("CHOOSE A COLOR",538,428,TEXT);
        int[] colors={0xFFFFFF,0xFACC15,0xFB923C,0xEF4444,0xEC4899,0x8B5CF6,0x3B82F6,0x22D3EE,0x10B981,0x84CC16,0xA16207,0x374151};
        for(int i=0;i<colors.length;i++){
            final int color=colors[i];int x=538+(i%6)*49,y=458+(i/6)*45;
            ui.roundedRect(x,y,40,34,8,0xFF000000|color);if(recolorEnabled&&recolorColor==color)check(x+16,y+12,UiTheme.onAccent(color));
            region(x,y,40,34,()->{recolorColor=color;colorHex=String.format(java.util.Locale.ROOT,"%06X",color);recolorEnabled=true;previewChanged();});
            tooltip(String.format(java.util.Locale.ROOT,"#%06X",color),x,y,40,34);
        }
        label("Custom color (HEX)",538,554);
        input("recolor-hex",colorHex,"8B5CF6",538,576,216,6,value->{colorHex=value;if(value.matches("[0-9a-fA-F]{6}")){recolorColor=Integer.parseInt(value,16);recolorEnabled=true;previewChanged();}});
        ui.roundedRect(770,576,58,26,8,0xFF000000|recolorColor);
        button("Original colors",538,616,290,26,true,false,()->{recolorEnabled=false;previewChanged();});
        button("Select pixels...",538,648,140,24,preview!=null,false,()->{final OverlayRecolor.Preview source=preview;open(new OverlaySelectionScreen(this,source,recolorColor,source.path.equals(selectionPath)?selection:null,chosen->{selection=chosen;selectionPath=source.path;recolorEnabled=true;previewChanged();}));});
        button("All textures",688,648,140,24,selectionPath!=null,false,()->{selection=null;selectionPath=null;previewChanged();});

        footer(sandstone ? "Sandstone only / Legacy + modern texture names / Activate explicitly / Esc: back" : "Browse folders, select a pack, create and activate its custom sky / Esc: back");
    }

    @Override
    protected void scroll(int x, int y, int direction) {
        if (x < 184) {
            SkyOverlayCreator.FolderNode current = SkyOverlayCreator.findNode(rootFolder, currentPath);
            int pages = current == null ? 1 : Math.max(1, (current.subfolders.size() + 7) / 8);
            folderPage = Math.max(0, Math.min(pages - 1, folderPage + direction));
        } else if (x < 514) {
            boolean isFolderMode = search.trim().isEmpty() && rootFolder != null;
            int totalItems = isFolderMode ? currentItems.size() : filteredPacks.size();
            int totalPages = Math.max(1, (totalItems + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.max(0, Math.min(totalPages - 1, page + direction));
        }
    }

    @Override
    protected void back() {
        open(parent);
    }
}
