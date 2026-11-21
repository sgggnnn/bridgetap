package com.bridger.momentum;
/** Opening fade changes only pixels; controls keep their final, stable hit boxes. */
final class FadedUiCanvas implements UiCanvas {
 private final UiCanvas parent;private final float opacity;
 FadedUiCanvas(UiCanvas parent,float opacity){this.parent=parent;this.opacity=Math.max(0,Math.min(1,opacity));}
 private int fade(int color){return ((int)((color>>>24)*opacity)<<24)|(color&0xFFFFFF);}
 public float pixelDensity(){return parent.pixelDensity();}
 public int width(String value){return parent.width(value);}
 public void rect(int x,int y,int w,int h,int color){parent.rect(x,y,w,h,fade(color));}
 public void text(String value,int x,int y,int color){parent.text(value,x,y,fade(color));}
 public void strongText(String value,int x,int y,int color,float scale){parent.strongText(value,x,y,fade(color),scale);}
 public void textScaled(String value,int x,int y,int color,float scale){parent.textScaled(value,x,y,fade(color),scale);}
 public void roundedRect(double x,double y,double w,double h,double r,int color){parent.roundedRect(x,y,w,h,r,fade(color));}
 public void glassRect(double x,double y,double w,double h,double r,int fill,int border,float reflection){parent.glassRect(x,y,w,h,r,fade(fill),fade(border),reflection);}
 public void polygon(double[] x,double[] y,int color){parent.polygon(x,y,fade(color));}
 public void pixelImage(java.awt.image.BufferedImage image,double x,double y,double w,double h,int tint){parent.pixelImage(image,x,y,w,h,fade(tint));}
 public void image(java.awt.image.BufferedImage image,double x,double y,double w,double h,int tint){parent.image(image,x,y,w,h,fade(tint));}
 public void texturedPolygon(double[] x,double[] y,String texture,int tint){parent.texturedPolygon(x,y,texture,fade(tint));}
}
