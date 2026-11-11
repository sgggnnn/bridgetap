package com.bridger.momentum;

/** Transparent placement editor: preview in the world before confirming. */
public final class IslandPlacementScreen extends BridgeScreen {
    private final IslandScreen parent;private boolean dragging,finished;private int dragX,dragY,movePart;private String gapText="";
    private boolean savePreset;private String presetName="",presetMessage="";
    public IslandPlacementScreen(IslandScreen parent){this.parent=parent;}
    @Override protected int backgroundColor(){return 0;}
    @Override protected int backdropColor(){return 0;}
    @Override protected void paint(){
        region(0,0,610,HEIGHT,()->{dragging=true;dragX=mouseX;dragY=mouseY;});
        panel(620,12,228,476);text("PLACE ISLAND",636,29,ACCENT);clipped(IslandOverlay.activeTemplate()==null?"No island":IslandOverlay.activeTemplate().name,636,52,196,TEXT);
        dropdown("island-part",new String[]{"Move: Whole scene","Move: Finish island","Move: Start island"}[movePart],new String[]{"Whole scene","Finish island","Start island"},movePart,636,78,196,value->movePart=value);
        toggle("Hide old blocks",IslandOverlay.replacing(),636,110,196,IslandOverlay::toggleReplacement);
        button(IslandOverlay.canReplace()?"Replace current island":"Detect original islands",636,143,196,24,true,false,()->{if(IslandOverlay.canReplace())IslandOverlay.replaceHere();else IslandOverlay.detectReplacement();});
        if(IslandOverlay.hasFinish()){
            if(!focus.equals("finish-gap"))gapText=String.valueOf(IslandOverlay.gap());
            label("Gap",636,189);button("-",710,181,25,24,true,false,()->{IslandOverlay.gap(-1);focus="";});
            input("finish-gap",gapText,"Blocks",738,181,64,8,value->{gapText=value;try{int distance=Integer.parseInt(value);if(distance>=0&&distance<=30000000)IslandOverlay.gap(distance-IslandOverlay.gap());}catch(NumberFormatException ignored){}});
            button("+",805,181,27,24,true,false,()->{IslandOverlay.gap(1);focus="";});
            int elevation=movePart==0?IslandOverlay.sceneY():movePart==2?IslandOverlay.startHeight():IslandOverlay.finishHeight();
            stepper(movePart==0?"Scene Y":"Height",(movePart!=0&&elevation>=0?"+":"")+elevation+"b",636,216,196,()->raise(-1),()->raise(1));
        }else{label("Use Copy both islands to",636,187);label("adjust the finish separately.",636,209);}
        button("Left",636,253,92,()->nudge(-1,0,0));button("Right",740,253,92,()->nudge(1,0,0));
        button("Nearer",636,286,92,()->nudge(0,0,-1));button("Farther",740,286,92,()->nudge(0,0,1));
        button("Reset part",636,319,92,24,IslandOverlay.enabled(),false,()->{if(movePart==2)IslandOverlay.resetStart();else if(movePart==1)IslandOverlay.resetFinish();else{IslandOverlay.resetStart();IslandOverlay.resetFinish();}});
        button("Rotate: "+IslandOverlay.rotation(),740,319,92,24,IslandOverlay.enabled(),false,IslandOverlay::rotate);
        toggle("Focus mode",IslandReplacement.focus(),636,348,196,IslandReplacement::toggleFocus);
        clipped(!IslandOverlay.failure().isEmpty()?IslandOverlay.failure():!IslandOverlay.canReplace()?"Original islands not detected":IslandOverlay.drawn()?"Preview ready":"Waiting for world preview...",636,377,196,IslandOverlay.failure().isEmpty()?MUTED:RED);
        button("Place island",636,389,196,32,IslandOverlay.enabled()&&IslandOverlay.drawn()&&IslandOverlay.failure().isEmpty(),true,()->{finished=true;IslandOverlay.confirm();open(null);});
        button("Cancel",636,435,92,28,true,false,this::back);button("Save preset",740,435,92,28,IslandOverlay.enabled(),false,()->{savePreset=true;presetName=IslandOverlay.activeTemplate().name;presetMessage="";});
        rect(12,HEIGHT-34,568,22,0xCC101722);text("Drag: move / Scroll: distance / Enter: place / Esc: cancel",22,HEIGHT-27,TEXT);
        if(savePreset){rect(0,0,WIDTH,HEIGHT,0xBB080E18);region(0,0,WIDTH,HEIGHT,()->{});panel(200,164,460,184);text("SAVE ISLAND PRESET",220,185,TEXT);input("preset-name",presetName,"Preset name",220,216,416,64,value->presetName=value);clipped(presetMessage,220,258,416,MUTED);button("Cancel",220,296,196,30,true,false,()->savePreset=false);button("Save preset",440,296,196,30,true,true,()->{try{IslandPresets.local().save(presetName,IslandOverlay.activeTemplate(),IslandOverlay.layout());savePreset=false;}catch(Exception ex){presetMessage=ex.getMessage();}});}
    }
    private void nudge(int right,int up,int forward){if(movePart==1)IslandOverlay.nudgeFinish(right,up,forward);else if(movePart==2)IslandOverlay.nudgeStart(right,up,forward);else IslandOverlay.nudge(right,up,forward);}
    private void raise(int amount){if(movePart==2)IslandOverlay.moveStart(0,amount,0);else if(movePart==1)IslandOverlay.moveFinish(0,amount,0);else IslandOverlay.move(0,amount,0);}
    @Override public void dragPreview(int x,int y){super.dragPreview(x,y);if(!dragging)return;int dx=(x-dragX)/10,dy=(y-dragY)/10;if(dx!=0||dy!=0){nudge(dx,-dy,0);dragX+=dx*10;dragY+=dy*10;}}
    @Override public void releasePreview(){dragging=false;}
    @Override protected void scroll(int x,int y,int direction){nudge(0,0,direction);}
    @Override protected void key(int key){if(savePreset)return;if(key==203)nudge(-1,0,0);if(key==205)nudge(1,0,0);if(key==200)nudge(0,1,0);if(key==208)nudge(0,-1,0);if((key==28||key==156)&&IslandOverlay.enabled()&&IslandOverlay.drawn()&&IslandOverlay.failure().isEmpty()){finished=true;IslandOverlay.confirm();open(null);}}
    @Override protected void back(){IslandOverlay.cancel();finished=true;open(parent);}
    @Override protected boolean escapeModal(){if(savePreset){savePreset=false;return true;}return false;}
    @Override public void m_47002234(){if(!finished)IslandOverlay.cancel();}
}
