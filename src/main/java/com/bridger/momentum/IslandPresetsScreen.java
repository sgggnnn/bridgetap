package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.C_85740840;

public final class IslandPresetsScreen extends BridgeScreen {
    private final C_85740840 parent;private List<IslandPresets.Preset> rows=Collections.emptyList();private int page;private String name="",message="Apply places the saved layout in one click near your current island.";private IslandPresets.Preset deleting;
    public IslandPresetsScreen(C_85740840 parent){this.parent=parent;reload();}
    private void reload(){try{rows=IslandPresets.local().list();page=Math.min(page,Math.max(0,(rows.size()-1)/7));}catch(Exception ex){message=ex.getMessage();}}
    @Override protected void paint(){
        header("Island presets","One click restores your saved island layout");button("Back",752,15,88,this::back);panel(20,74,820,284);
        if(rows.isEmpty())label("No presets yet. Position your islands, then save the layout below.",38,117);
        for(int i=page*7;i<Math.min(rows.size(),page*7+7);i++){IslandPresets.Preset preset=rows.get(i);int y=94+(i-page*7)*34;clipped(preset.name+(preset.layout.stableAnchor?"":" (legacy - resave)"),38,y+8,490,TEXT);button("Apply",550,y,128,26,true,true,()->{try{IslandOverlay.applyPreset(preset.template(),preset.layout);message="Applied: "+preset.name;open(null);}catch(Exception ex){message=ex.getMessage();}});button("Delete",692,y,128,26,true,false,()->deleting=preset);}
        button("Previous",38,322,110,24,page>0,false,()->page--);button("Next",160,322,110,24,(page+1)*7<rows.size(),false,()->page++);
        panel(20,374,820,76);input("preset-name",name,"Name for the current island layout",38,396,514,64,value->name=value);button("Save current layout",568,394,252,30,IslandOverlay.enabled(),true,()->{try{IslandPresets.local().save(name,IslandOverlay.activeTemplate(),IslandOverlay.layout());message="Preset saved.";name="";reload();}catch(Exception ex){message=ex.getMessage();}});footer(message);
        if(deleting!=null){rect(0,0,WIDTH,HEIGHT,0xBB080E18);region(0,0,WIDTH,HEIGHT,()->{});panel(200,164,460,174);text("DELETE PRESET?",220,186,TEXT);clipped(deleting.name,220,220,416,ACCENT);button("Cancel",220,282,196,30,true,false,()->deleting=null);button("Delete",440,282,196,30,true,true,()->{try{IslandPresets.local().delete(deleting);deleting=null;reload();message="Preset deleted.";}catch(Exception ex){message=ex.getMessage();}});}
    }
    @Override protected boolean escapeModal(){if(deleting!=null){deleting=null;return true;}return false;}
    @Override protected void back(){open(parent);}
}
