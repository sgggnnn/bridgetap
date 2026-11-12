package com.bridger.momentum;

import java.nio.file.*;
import java.util.*;

public final class IslandPresetImportScreen extends BridgeScreen {
    private final IslandPresetsScreen parent;private List<Path> files=new ArrayList<>();private int page;private String message="Put shared .btpreset files in the imports folder, then click Refresh.";
    public IslandPresetImportScreen(IslandPresetsScreen parent){this.parent=parent;refresh();}
    private void refresh(){try{files=new ArrayList<>();try(DirectoryStream<Path> stream=Files.newDirectoryStream(IslandPresetArchive.folder("imports"),"*.btpreset")){for(Path file:stream)if(Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)){files.add(file);if(files.size()>=500)break;}}files.sort(Comparator.comparing(p->p.getFileName().toString(),String.CASE_INSENSITIVE_ORDER));page=Math.min(page,Math.max(0,(files.size()-1)/8));}catch(Exception error){message=error.getMessage();}}
    @Override protected void paint(){header("Import island presets","Shared scenery and complete layouts");button("Back",752,15,88,this::back);panel(20,74,820,372);button("Open imports folder",38,92,250,()->{try{message=BridgeFileActions.open(IslandPresetArchive.folder("imports"));}catch(Exception error){message=error.getMessage();}});button("Refresh",302,92,120,this::refresh);
        if(files.isEmpty())label("No .btpreset files found.",38,153);
        for(int i=page*8;i<Math.min(files.size(),page*8+8);i++){Path file=files.get(i);int y=145+(i-page*8)*31;clipped(file.getFileName().toString(),38,y+8,585,TEXT);button("Import",650,y,170,24,true,true,()->{try{IslandPresets.Preset preset=IslandPresetArchive.importFile(file,IslandPresets.local());message="Imported: "+preset.name+". Back opens your presets.";parent.reload();}catch(Exception error){message=error.getMessage();}});}
        button("Previous",38,414,102,24,page>0,false,()->page--);button("Next",152,414,102,24,(page+1)*8<files.size(),false,()->page++);footer(message);
    }
    @Override protected void back(){parent.reload();open(parent);}
}
