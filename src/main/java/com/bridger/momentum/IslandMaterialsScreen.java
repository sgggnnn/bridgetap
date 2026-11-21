package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.*;

/** Create a personal island by replacing materials, including imported modern blocks. */
public final class IslandMaterialsScreen extends BridgeScreen {
    private boolean isometric=true;
    private final IslandScreen parent;private IslandTemplate island;
    private final List<String> blocks=new ArrayList<>(),filtered=new ArrayList<>();
    private List<String> palette;private String from,to="modern:polished_blackstone_bricks",search="",name,message="Replace a material, then save your new island.";
    private int sourcePage,page,variant,modernCount=-1,previewVariant=-1,changed;
    private IslandTemplate previewSource,pending;private String previewFrom="",previewTo="";private IslandPreview before,after;
    private void preparePreview(){
        if(previewSource==island&&previewFrom.equals(from)&&previewTo.equals(to)&&previewVariant==variant)return;
        int metadata=IslandMaterials.modern(to)?0:variant;pending=IslandMaterials.replace(island,island.name,from,to,metadata);before=new IslandPreview(island);after=new IslandPreview(pending);changed=0;
        for(IslandTemplate.Block b:island.blocks)if(b.key.equals(from)&&(!b.key.equals(to)||b.meta!=metadata))changed++;
        previewSource=island;previewFrom=from;previewTo=to;previewVariant=variant;
    }
    private void validateSelection()throws Exception{
        if(IslandMaterials.modern(to))ModernAppearance.prepareIsland(Collections.singleton(to));
        else{C_81592558 block=C_81592558.m_58693857(to);if(block==null||block.m_43136364()!=3)throw new IllegalArgumentException("Choose a supported block");}
    }
    private void previewWorld(){try{preparePreview();IslandOverlay.beginAutoPreview(pending);open(new IslandMaterialWorldPreviewScreen(this));}catch(Exception ex){message=ex.getMessage();}}

    public IslandMaterialsScreen(IslandScreen parent,IslandTemplate source){
        this.parent=parent;island=source;palette=IslandMaterials.palette(source);from=palette.get(0);name=source.name.length()>54?source.name.substring(0,54)+" - Custom":source.name+" - Custom";
        for(int i=0;i<4096;i++){C_81592558 b=C_81592558.m_64320102(i);if(b!=null&&C_81592558.m_63461719(b)==i&&b.m_43136364()==3)blocks.add(C_81592558.f_83036452.m_55287479(b).toString());}filter();
    }
    private void filter(){filtered.clear();for(String key:blocks)if(key.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))filtered.add(key);page=0;}
    private void replace(){try{
        if(IslandMaterials.modern(to))ModernAppearance.prepareIsland(Collections.singleton(to));
        else{C_81592558 block=C_81592558.m_58693857(to);if(block==null||block.m_43136364()!=3)throw new IllegalArgumentException("Choose a supported block");}
        island=IslandMaterials.replace(island,island.name,from,to,IslandMaterials.modern(to)?0:variant);palette=IslandMaterials.palette(island);from=to;sourcePage=palette.indexOf(from)/4;message="Material replaced. Save as new island to keep your changes.";
    }catch(Exception ex){message=ex.getMessage();}}
    @Override protected void paint(){
        if(modernCount!=ModernAppearance.blocks().size()){blocks.removeIf(IslandMaterials::modern);blocks.addAll(ModernAppearance.blocks());modernCount=ModernAppearance.blocks().size();Collections.sort(blocks);filter();}
        header("Island materials","Keep the shape. Choose your palette.");button(isometric?"View: 3D":"View: Top",600,15,140,()->isometric=!isometric);button("Back",752,15,88,this::back);
        panel(20,72,282,232);panel(314,72,300,232);panel(626,72,214,366);
        text("ISLAND MATERIAL",36,88,TEXT);
        for(int i=sourcePage*4;i<Math.min(palette.size(),sourcePage*4+4);i++){String key=palette.get(i);button(key,36,120+(i-sourcePage*4)*32,250,28,true,key.equals(from),()->from=key);}
        button("Previous",36,264,118,24,sourcePage>0,false,()->sourcePage--);button("Next",166,264,120,24,(sourcePage+1)*4<palette.size(),false,()->sourcePage++);
        preparePreview();if(isometric){before.paintIsometric(ui,20,316,282,128,"Current island");after.paintIsometric(ui,314,316,300,128,"Live material preview");}else{before.paint(ui,20,316,282,128,"Current island");after.paint(ui,314,316,300,128,"Live material preview");}
        label(changed+" blocks in this preview",36,454);button("Preview in world",330,448,268,24,true,false,this::previewWorld);
        input("material-search",search,"Search blocks...",330,88,268,64,value->{search=value;filter();});
        for(int i=page*5;i<Math.min(filtered.size(),page*5+5);i++){String key=filtered.get(i);button(key,330,128+(i-page*5)*28,268,24,true,key.equals(to),()->to=key);}
        button("Previous",330,264,128,24,page>0,false,()->page--);button("Next",470,264,128,24,(page+1)*5<filtered.size(),false,()->page++);
        text("REPLACEMENT",642,88,TEXT);clipped(to,642,116,182,ACCENT);
        stepper("Variant",String.valueOf(variant),642,148,182,()->variant=Math.max(0,variant-1),()->variant=Math.min(15,variant+1));
        button("Replace material",642,195,182,30,true,true,this::replace);
        button(ModernAppearance.importing()?"Importing...":"Download 26.2",642,239,182,28,!ModernAppearance.importing(),false,ModernAppearance::download);
        button("Import local JAR",642,275,182,28,!ModernAppearance.importing(),false,ModernAppearance::browse);
        clipped(ModernAppearance.status(),642,314,182,MUTED);
        input("island-name",name,"Island name",642,341,182,64,value->name=value);
        button("Save as new island",642,393,182,28,!name.trim().isEmpty(),true,()->{try{
            preparePreview();if(changed>0)validateSelection();IslandTemplate chosen=changed>0?pending:island;
            IslandTemplate saved=new IslandTemplate(name,chosen.width,chosen.height,chosen.depth,chosen.originX,chosen.originY,chosen.originZ,chosen.blocks,chosen.anchorX,chosen.anchorY,chosen.anchorZ,chosen.bothIslands);
            parent.copiedFile(IslandLibrary.save(saved,"saved"),"Custom island saved. Click Apply automatically to place it.");open(parent);
        }catch(Exception ex){message=ex.getMessage();}});
        footer(message);
    }
    @Override protected void back(){open(parent);}
}
