package com.bridger.momentum;

/** Read-only material preview; leaving restores the previous client scenery. */
public final class IslandMaterialWorldPreviewScreen extends BridgeScreen {
    private final IslandMaterialsScreen parent;private boolean finished;
    public IslandMaterialWorldPreviewScreen(IslandMaterialsScreen parent){this.parent=parent;}
    @Override protected int backgroundColor(){return 0;}
    @Override protected int backdropColor(){return 0;}
    @Override protected void paint(){
        panel(20,16,820,60);text("MATERIAL PREVIEW",36,30,ACCENT);
        clipped(IslandOverlay.failure().isEmpty()?"Actual block models and textures. Return to keep editing.":IslandOverlay.failure(),36,52,500,MUTED);
        button("Rotate",552,31,90,28,true,false,IslandOverlay::rotate);
        button("Back to materials",654,31,170,28,true,true,this::back);
    }
    @Override protected void back(){if(!finished){IslandOverlay.cancel();finished=true;}open(parent);}
    @Override public void m_47002234(){if(!finished){IslandOverlay.cancel();finished=true;}}
}
