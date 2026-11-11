package com.bridger.momentum;

import java.util.*;
import net.minecraft.unmapped.*;

public final class PlacementAppearanceScreen extends BridgeScreen {
    private final C_85740840 parent;private String search="",selected=PlacementAppearance.block(),message="Choose any registered block. Changes are visual only.";private int variant=PlacementAppearance.metadata(),page;private boolean enabled=PlacementAppearance.enabled();private final List<String> blocks=new ArrayList<>(),filtered=new ArrayList<>();
    public PlacementAppearanceScreen(C_85740840 parent){this.parent=parent;for(int i=0;i<4096;i++){C_81592558 b=C_81592558.m_64320102(i);if(b!=null&&C_81592558.m_63461719(b)==i)blocks.add(C_81592558.f_83036452.m_55287479(b).toString());}Collections.sort(blocks);filter();}
    private void filter(){filtered.clear();for(String key:blocks)if(key.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))filtered.add(key);page=0;}
    @Override protected void paint(){
        header("Block appearance","Choose how your placed blocks look");button("Back",752,15,88,this::back);panel(20,74,470,376);panel(506,74,334,376);
        input("search",search,"Search any block...",36,92,438,64,value->{search=value;filter();});
        for(int i=page*10;i<Math.min(filtered.size(),page*10+10);i++){String key=filtered.get(i);button(key,36,134+(i-page*10)*26,438,24,true,key.equals(selected),()->selected=key);}
        button("Previous",36,413,110,24,page>0,false,()->page--);button("Next",158,413,110,24,(page+1)*10<filtered.size(),false,()->page++);label(filtered.size()+" blocks",302,421);
        text("SELECTED BLOCK",526,96,MUTED);clipped(selected,526,122,294,TEXT);toggle("Enabled",enabled,526,160,294,()->enabled=!enabled);
        stepper("Variant",String.valueOf(variant),526,204,294,()->variant=Math.max(0,variant-1),()->variant=Math.min(15,variant+1));
        label("Works in gameplay and replays.",526,257);label("Server blocks and collisions stay",526,280);label("the same. Air hides placed blocks.",526,299);
        button("Apply appearance",526,351,294,32,true,true,()->{try{PlacementAppearance.apply(selected,variant,enabled);message="Block appearance saved.";}catch(Exception ex){message=ex.getMessage();}});
        button("Restore original blocks",526,402,294,28,true,false,()->{try{enabled=false;PlacementAppearance.apply(selected,variant,false);message="Original block appearance restored.";}catch(Exception ex){message=ex.getMessage();}});footer(message);
    }
    @Override protected void scroll(int x,int y,int direction){page=Math.max(0,Math.min(Math.max(0,(filtered.size()-1)/10),page+direction));}
    @Override protected void back(){open(parent);}
}
