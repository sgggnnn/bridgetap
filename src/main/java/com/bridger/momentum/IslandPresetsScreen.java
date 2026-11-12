package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.C_85740840;

public final class IslandPresetsScreen extends BridgeScreen {
    private final C_85740840 parent;private List<IslandPresets.Preset> rows=Collections.emptyList();private int page;private String name="",message="Apply places the saved layout in one click near your current island.";private IslandPresets.Preset deleting;
    public IslandPresetsScreen(C_85740840 parent){this.parent=parent;reload();}
    void reload(){try{rows=IslandPresets.local().list();page=Math.min(page,Math.max(0,(rows.size()-1)/6));}catch(Exception ex){message=ex.getMessage();}}
    @Override protected void paint(){
        header("Island presets","One click restores your saved island layout");button("Back",752,15,88,this::back);button("Import",510,15,108,()->open(new IslandPresetImportScreen(this)));button("Files",630,15,110,()->{try{message=BridgeFileActions.open(IslandPresetArchive.folder("exports"));}catch(Exception ex){message=ex.getMessage();}});panel(20,74,820,284);
        String server=ServerPresets.address();String linked="";try{linked=ServerPresets.bindings().preset(server);}catch(Exception ignored){}clipped("Auto preset for: "+(server.isEmpty()?"Join a server first":server),38,82,585,MUTED);button("Set start island",636,78,184,24,!server.isEmpty()&&!ReplaySession.active(),false,()->{try{IslandStartAnchors.mark();message="Start island marked. Presets will use this island.";}catch(Exception ex){message=ex.getMessage();}});
        if(rows.isEmpty())label("No presets yet. Position your islands, then save the layout below.",38,117);
        for(int i=page*6;i<Math.min(rows.size(),page*6+6);i++){IslandPresets.Preset preset=rows.get(i);int y=110+(i-page*6)*34;clipped(preset.name+(preset.layout.stableAnchor?"":" (legacy - resave)"),38,y+8,248,TEXT);button("Apply",302,y,94,26,true,true,()->{try{IslandOverlay.applyPreset(preset.template(),preset.layout);message="Applied: "+preset.name;open(null);}catch(Exception ex){message=ex.getMessage();}});button(preset.id.equals(linked)?"Unlink server":"Auto here",408,y,154,26,!server.isEmpty(),false,()->{try{ServerPresets.bindHere(server,preset);message="Server preset and current block appearance saved. Applies on next join.";}catch(Exception ex){message=ex.getMessage();}});button("Export",574,y,112,26,true,false,()->{try{java.nio.file.Path file=IslandPresetArchive.export(preset);message="Exported: "+file.getFileName();BridgeFileActions.open(file.getParent());}catch(Exception ex){message=ex.getMessage();}});button("Delete",698,y,122,26,true,false,()->deleting=preset);}
        button("Previous",38,322,110,24,page>0,false,()->page--);button("Next",160,322,110,24,(page+1)*6<rows.size(),false,()->page++);
        panel(20,374,820,76);input("preset-name",name,"Name for the current island layout",38,396,514,64,value->name=value);button("Save current layout",568,394,252,30,IslandOverlay.enabled(),true,()->{try{IslandPresets.local().save(name,IslandOverlay.activeTemplate(),IslandOverlay.layout());message="Preset saved.";name="";reload();}catch(Exception ex){message=ex.getMessage();}});footer(message);
        if(deleting!=null){rect(0,0,WIDTH,HEIGHT,0xBB080E18);region(0,0,WIDTH,HEIGHT,()->{});panel(200,164,460,174);text("DELETE PRESET?",220,186,TEXT);clipped(deleting.name,220,220,416,ACCENT);button("Cancel",220,282,196,30,true,false,()->deleting=null);button("Delete",440,282,196,30,true,true,()->{try{IslandPresets.local().delete(deleting);deleting=null;reload();message="Preset deleted.";}catch(Exception ex){message=ex.getMessage();}});}
    }
    @Override protected boolean escapeModal(){if(deleting!=null){deleting=null;return true;}return false;}
    @Override protected void back(){open(parent);}
}
