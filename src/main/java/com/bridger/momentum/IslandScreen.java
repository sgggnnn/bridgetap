package com.bridger.momentum;

import java.nio.file.*;
import java.util.*;
import net.minecraft.unmapped.C_85740840;

/** Island library: choose an island, preview in the world, then confirm. */
public final class IslandScreen extends BridgeScreen {
    private final C_85740840 parent;
    private List<Path> files=Collections.emptyList();private int tab,page,selected;
    private String name="",message="Choose an island and click Place to preview it in the world.";
    private final Map<Path,String> names=new HashMap<>();
    private final Map<Path,IslandTemplate> loaded=new HashMap<>();
    private final Set<Path> invalid=new HashSet<>();
    private ReplayData replay;private RunHistory.Run replayRun;private String replayLabel="No replay selected";
    private Path deleting,renaming;private String renameText="";private IslandPreview preview;private IslandTemplate previewTemplate;
    public IslandScreen(C_85740840 parent){this.parent=parent;replay=ReplaySession.recording();if(replay!=null)replayLabel=ReplaySession.recordingName();refresh();}
    public IslandScreen(C_85740840 parent,RunHistory.Run run){this(parent);if(replay==null&&run!=null){replayRun=run;replayLabel=run.displayName();}}
    private interface Operation{void run()throws Exception;}
    private void run(Operation op){try{op.run();}catch(Exception ex){message=ex.getMessage()==null?"Operation failed":ex.getMessage();}}
    private String kind(){return tab==3?"imports":"saved";}
    private void refresh(){run(()->{
        IslandLibrary.nameLegacyExports();
        Path old=selected<files.size()?files.get(selected):null;files=IslandLibrary.list(kind());names.clear();loaded.clear();invalid.clear();preview=null;previewTemplate=null;
        for(Path file:files)try{names.put(file,IslandTemplate.readName(file));}catch(Exception ex){names.put(file,"Unreadable island");}
        files.sort(Comparator.comparing(p->names.get(p),String.CASE_INSENSITIVE_ORDER));selected=Math.max(0,files.indexOf(old));page=selected/6;
    });}
    private IslandTemplate current()throws Exception{
        if(selected<0||selected>=files.size())throw new IllegalArgumentException("Choose an island first");Path file=files.get(selected);
        if(invalid.contains(file))throw new IllegalArgumentException("This island file cannot be read. You can delete it.");
        if(!loaded.containsKey(file))try{loaded.put(file,IslandTemplate.read(file));}catch(Exception ex){invalid.add(file);throw ex;}
        return loaded.get(file);
    }
    private void switchTab(int value){tab=value;focus="";deleting=null;refresh();}
    private void selectSaved(Path file){tab=0;refresh();selected=Math.max(0,files.indexOf(file));page=selected/6;}
    public void copiedFile(Path file,String status){selectSaved(file);message=status;}
    private void place()throws Exception{
        IslandTemplate template=current();IslandOverlay.beginPreview(template);IslandOverlay.libraryFile(files.get(selected));open(new IslandPlacementScreen(this));
    }
    private void copyBoth()throws Exception{
        if(replay==null&&replayRun!=null)replay=BridgeTracker.history().replay(replayRun);
        String title=name.trim().isEmpty()?(replayLabel.length()>45?replayLabel.substring(0,45):replayLabel)+" - Both islands":name;
        Path file=IslandLibrary.save(IslandOverlay.bothFromReplay(title,replay),"saved");selectSaved(file);message="Both islands saved together, preserving their gap and height. Click Place when ready.";
    }
    @Override protected void paint(){
        header("Island library","Copy. Preview. Place.");button("Back",752,15,88,this::back);
        button("Presets",620,15,120,()->open(new IslandPresetsScreen(this)));
        tab("My islands",20,60,108,tab==0,()->switchTab(0));tab("From replay",140,60,128,tab==1,()->switchTab(1));tab("Select blocks",280,60,128,tab==2,()->switchTab(2));tab("Import",420,60,88,tab==3,()->switchTab(3));
        button("Focus: "+(IslandReplacement.focus()?"ON":"OFF"),516,60,100,IslandReplacement::toggleFocus);
        button("Files",628,60,96,()->run(()->{message=BridgeFileActions.open(IslandLibrary.folder(kind()));}));button("Refresh",736,60,104,this::refresh);
        if(tab==1)copyReplay();else if(tab==2)copyRegion();else library();
        footer(message);if(deleting!=null)deleteModal();if(renaming!=null)renameModal();
    }
    private void library(){
        panel(20,106,392,346);panel(428,106,412,346);text(tab==3?"IMPORT ISLANDS":"MY ISLANDS",36,123,TEXT);
        if(files.isEmpty()){label(tab==3?"Put .btisland files in the imports folder.":"Your saved islands will appear here.",36,166);if(tab==0)button("Copy an island from a replay",36,206,360,30,true,true,()->switchTab(1));}
        for(int i=page*6;i<Math.min(files.size(),page*6+6);i++){
            final int index=i;final Path file=files.get(i);int y=147+(i-page*6)*42;
            button(names.get(file),36,y,264,32,true,selected==i,()->selected=index);
            button("Delete",310,y,86,32,true,false,()->deleting=file);
        }
        button("Previous",36,414,94,24,page>0,false,()->{page--;selected=page*6;});button("Next",142,414,94,24,(page+1)*6<files.size(),false,()->{page++;selected=page*6;});label((page+1)+" / "+Math.max(1,(files.size()+5)/6),264,423);
        if(files.isEmpty()){text("CREATE YOUR FIRST ISLAND",448,131,TEXT);label("Open From replay and copy both islands,",448,168);label("then position the preview in the world.",448,189);return;}
        try{
            IslandTemplate island=current();text("SELECTED ISLAND",448,124,MUTED);clipped(island.name,448,144,372,TEXT);
            if(previewTemplate!=island){preview=new IslandPreview(island);previewTemplate=island;}preview.paint(ui,448,165,372,172);
            label(island.width+" x "+island.height+" x "+island.depth+" blocks  /  "+island.blocks.size()+" copied",448,347);
            if(tab==3){button("Import island",448,374,372,32,true,true,()->run(()->{Path imported=IslandLibrary.importFile(files.get(selected));selectSaved(imported);message="Imported. Click Place to preview it.";}));}
            else{
                button("Place island",448,374,372,32,true,true,()->run(this::place));
                button("Rename",448,418,90,24,true,false,()->{renaming=files.get(selected);renameText=island.name;});
                button("Export",546,418,82,24,true,false,()->run(()->{Path file=IslandLibrary.save(current(),"exports");message="Exported: "+file.getFileName()+" / islands/exports";}));
                button("Remove placed island",636,418,184,24,IslandOverlay.enabled(),false,()->{IslandOverlay.disable();message="Placed island removed. Saved files are unchanged.";});
            }
        }catch(Exception ex){clipped(ex.getMessage()==null?"Unreadable island":ex.getMessage(),448,166,372,RED);label("Delete the file or select another island.",448,194);}
    }
    private void copyReplay(){
        panel(20,106,820,346);text("COPY AN ISLAND FROM A REPLAY",40,128,TEXT);label("Selected replay",40,162);clipped(replayLabel,40,182,770,ACCENT);
        input("name",name,"Island name (optional)",40,221,778,64,value->name=value);
        button("Copy both islands",40,276,778,38,replay!=null||replayRun!=null,true,()->run(this::copyBoth));
        label("Saves both islands as one scene. Copying does not paste or replace anything.",40,343);
        label("Only terrain stored in the recording can be copied. Bridge placements are excluded.",40,367);
        if(replay==null&&replayRun==null)label("Return to the replay library and select a playable run first.",40,405);
    }
    private void copyRegion(){
        panel(20,106,820,346);text("COPY IN THE WORLD",40,128,TEXT);label("Open the selection editor to see exactly what you are copying.",40,174);label("Auto-select an island, drag the box, resize it, or pick two corners.",40,204);
        button("Select an island in the world",40,260,778,40,true,true,()->open(new IslandCopyScreen(this)));label("Right-drag to look around. Save island confirms the copy; Escape cancels.",40,335);label("Copying saves a template. It never pastes or hides the original island.",40,365);
    }
    private void deleteModal(){
        Path target=deleting;rect(0,0,WIDTH,HEIGHT,0xBB080E18);region(0,0,WIDTH,HEIGHT,()->{});panel(200,164,460,170);text("DELETE ISLAND?",220,184,TEXT);clipped(names.getOrDefault(target,"Selected island"),220,210,420,ACCENT);label("This removes the saved island file.",220,235);
        button("Cancel",220,280,196,30,true,false,()->deleting=null);
        button("Delete island",440,280,196,30,true,true,()->run(()->{IslandLibrary.delete(target,kind());if(IslandOverlay.isFrom(target))IslandOverlay.disable();deleting=null;refresh();message="Island deleted.";}));
    }
    private void renameModal(){
        rect(0,0,WIDTH,HEIGHT,0xBB080E18);region(0,0,WIDTH,HEIGHT,()->{});panel(200,164,460,180);text("RENAME ISLAND",220,184,TEXT);
        input("rename-island",renameText,"Island name",220,218,416,64,value->renameText=value);
        button("Cancel",220,290,196,30,true,false,()->renaming=null);
        button("Save name",440,290,196,30,!renameText.trim().isEmpty(),true,()->run(()->{Path target=renaming;IslandLibrary.rename(target,renameText);IslandOverlay.renameLabel(target,renameText);renaming=null;refresh();selected=Math.max(0,files.indexOf(target));page=selected/6;message="Island renamed.";}));
    }
    @Override protected boolean escapeModal(){if(renaming!=null){renaming=null;return true;}if(deleting!=null){deleting=null;return true;}return false;}
    @Override protected void back(){open(parent);}
}
