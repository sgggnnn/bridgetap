package com.bridger.momentum;
import java.awt.image.BufferedImage;import java.util.*;import java.util.function.Consumer;

/** Bounded selection grid. Normalized cells map to the same source region during export. */
public final class OverlaySelectionScreen extends BridgeGlassScreen {
 private final SkyOverlayScreen parent;private final OverlayRecolor.Preview source;private final int color;private final Consumer<OverlayRecolor.Selection> done;
 private BitSet cells;private final Deque<BitSet> undo=new ArrayDeque<>();private BufferedImage result,highlight;private int mode=3,tolerance=24,brush=1,startX,startY,lastX,lastY;private boolean dragging,erase,showHighlight=true;private double ix,iy,iw,ih;
 public OverlaySelectionScreen(SkyOverlayScreen parent,OverlayRecolor.Preview source,int color,OverlayRecolor.Selection previous,Consumer<OverlayRecolor.Selection> done){this.parent=parent;this.source=source;this.color=color;this.done=done;cells=previous==null?new BitSet():previous.cells();refresh();}
 private int w(){return source.original.getWidth();}private int h(){return source.original.getHeight();}
 private OverlayRecolor.Selection selection(){return new OverlayRecolor.Selection(w(),h(),cells);}
 private void refresh(){result=OverlayRecolor.apply(source.original,OverlayRecolor.Settings.color(color).selected(source.path,selection()));highlight=new BufferedImage(w(),h(),BufferedImage.TYPE_INT_ARGB);for(int y=0;y<h();y++)for(int x=0;x<w();x++)if(cells.get(y*w()+x))highlight.setRGB(x,y,UiTheme.alpha(0xFFFFFF,.24));}
 private void snapshot(){if(undo.size()==16)undo.removeFirst();undo.addLast((BitSet)cells.clone());}
 private int gx(int x){return Math.max(0,Math.min(w()-1,(int)((x-ix)*w()/iw)));}private int gy(int y){return Math.max(0,Math.min(h()-1,(int)((y-iy)*h()/ih)));}
 private void dot(int x,int y){int size=mode==3?1:brush;for(int a=Math.max(0,y-size+1);a<=Math.min(h()-1,y+size-1);a++)for(int b=Math.max(0,x-size+1);b<=Math.min(w()-1,x+size-1);b++)cells.set(a*w()+b,!erase);}
 private void stroke(int x,int y){int steps=Math.max(Math.abs(x-lastX),Math.abs(y-lastY));for(int i=0;i<=steps;i++)dot(lastX+(x-lastX)*i/Math.max(1,steps),lastY+(y-lastY)*i/Math.max(1,steps));lastX=x;lastY=y;refresh();}
 private void begin(){if(mouseX<ix||mouseX>=ix+iw||mouseY<iy||mouseY>=iy+ih)return;snapshot();startX=lastX=gx(mouseX);startY=lastY=gy(mouseY);
  if(mode==2){int picked=source.original.getRGB(startX,startY);for(int y=0;y<h();y++)for(int x=0;x<w();x++){int c=source.original.getRGB(x,y);if((c>>>24)!=0&&Math.abs(((c>>16)&255)-((picked>>16)&255))<=tolerance&&Math.abs(((c>>8)&255)-((picked>>8)&255))<=tolerance&&Math.abs((c&255)-(picked&255))<=tolerance)cells.set(y*w()+x,!erase);}refresh();}
  else{dragging=true;if(mode==0||mode==3)stroke(startX,startY);}
 }
 @Override public void dragPreview(int x,int y){super.dragPreview(x,y);if(dragging){if(mode==0||mode==3)stroke(gx(x),gy(y));else{lastX=gx(x);lastY=gy(y);}}}
 @Override public void releasePreview(){super.releasePreview();if(dragging&&mode==1){for(int y=Math.min(startY,lastY);y<=Math.max(startY,lastY);y++)for(int x=Math.min(startX,lastX);x<=Math.max(startX,lastX);x++)cells.set(y*w()+x,!erase);refresh();}dragging=false;}
 @Override protected void paint(){
  windowHeader("Select recolor pixels");button("Cancel",812,25,128,32,true,false,this::back);button("Use selection",952,25,136,32,true,true,()->{releasePreview();done.accept(selection());open(parent);});
  clipped(source.path,32,105,1056,MUTED);label("Paint on the image. Only selected areas of this texture will change.",32,133);
  double scale=Math.min(700.0/w(),390.0/h());iw=w()*scale;ih=h()*scale;ix=32+(700-iw)/2;iy=174+(390-ih)/2;
  ui.roundedRect(32,174,700,390,12,UiTheme.alpha(TEXT,.035));ui.pixelImage(result,ix,iy,iw,ih,0xFFFFFFFF);
  if(showHighlight)ui.pixelImage(highlight,ix,iy,iw,ih,0xFFFFFFFF);
  if(iw/w()>=8&&ih/h()>=8){for(int x=0;x<=w();x++)rect((int)Math.round(ix+x*iw/w()),(int)iy,1,(int)ih,UiTheme.alpha(TEXT,.10));for(int y=0;y<=h();y++)rect((int)ix,(int)Math.round(iy+y*ih/h()),(int)iw,1,UiTheme.alpha(TEXT,.10));}
  if(dragging&&mode==1){double x=ix+Math.min(startX,lastX)*iw/w(),y=iy+Math.min(startY,lastY)*ih/h();ui.glassRect(x,y,(Math.abs(startX-lastX)+1)*iw/w(),(Math.abs(startY-lastY)+1)*ih/h(),0,UiTheme.alpha(ACCENT,.14),ACCENT,0);}
  region(32,174,700,390,this::begin);
  text("SELECTION TOOL",772,174,MUTED);String[] modes={"Pen (one pixel)","Brush","Rectangle","Similar color"};int[] tools={3,0,1,2};for(int i=0;i<4;i++){final int chosen=tools[i];choice(modes[i],772,206+i*34,316,mode==chosen,()->mode=chosen);}
  toggle("Erase from selection",erase,772,350,316,()->erase=!erase);
  if(mode==2)slider("tolerance","Color tolerance",tolerance,0,100,"",772,390,316,true,v->tolerance=v);
  else if(mode==0)slider("brush","Brush size",brush,1,8," cells",772,390,316,true,v->brush=v);
  if(mode==3)label("Click a cell, or drag to draw a selection.",772,404);
  button("Undo",772,450,152,32,!undo.isEmpty(),false,()->{cells=undo.removeLast();refresh();});
  button("Invert",936,450,152,32,true,false,()->{snapshot();cells.flip(0,w()*h());refresh();});
  button("Select all",772,496,152,32,true,false,()->{snapshot();cells.set(0,w()*h());refresh();});
  button("Clear",936,496,152,32,true,false,()->{snapshot();cells.clear();refresh();});
  label(cells.cardinality()+" / "+(w()*h())+" cells selected",772,552);
  toggle("Show selection highlight",showHighlight,772,578,316,()->showHighlight=!showHighlight);
  windowStatus("Selection grid: "+w()+" x "+h()+" / Large textures use sampled areas, not individual source pixels / Esc: cancel");
 }
 @Override protected void back(){open(parent);}
}
