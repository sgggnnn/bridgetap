package com.bridger.momentum;

/** Canvas transform used by the HUD studio; font metrics remain in local pixels. */
public final class ScaledUiCanvas implements UiCanvas {
    private final UiCanvas parent;private final int x,y;private final float scale;
    public ScaledUiCanvas(UiCanvas parent,int x,int y,float scale){this.parent=parent;this.x=x;this.y=y;this.scale=scale;}
    public void polygon(double[] px,double[] py,int color){double[] a=new double[px.length],b=new double[py.length];for(int i=0;i<a.length;i++){a[i]=x+px[i]*scale;b[i]=y+py[i]*scale;}parent.polygon(a,b,color);}
    public void pixelImage(java.awt.image.BufferedImage image,double px,double py,double width,double height,int tint){parent.pixelImage(image,x+px*scale,y+py*scale,width*scale,height*scale,tint);}
    public void image(java.awt.image.BufferedImage image,double px,double py,double width,double height,int tint){parent.image(image,x+px*scale,y+py*scale,width*scale,height*scale,tint);}
    public void texturedPolygon(double[] px,double[] py,String texture,int tint){double[] a=new double[px.length],b=new double[py.length];for(int i=0;i<a.length;i++){a[i]=x+px[i]*scale;b[i]=y+py[i]*scale;}parent.texturedPolygon(a,b,texture,tint);}
    public float pixelDensity(){return parent.pixelDensity()*scale;}
    public void roundedRect(double a,double b,double w,double h,double radius,int color){parent.roundedRect(x+a*scale,y+b*scale,w*scale,h*scale,radius*scale,color);}
    public void glassRect(double a,double b,double w,double h,double radius,int fill,int border,float reflection){parent.glassRect(x+a*scale,y+b*scale,w*scale,h*scale,radius*scale,fill,border,reflection);}
    public void rect(int a,int b,int w,int h,int color){parent.rect(x+Math.round(a*scale),y+Math.round(b*scale),Math.max(1,Math.round(w*scale)),Math.max(1,Math.round(h*scale)),color);}
    public void text(String value,int a,int b,int color){parent.textScaled(value,x+Math.round(a*scale),y+Math.round(b*scale),color,scale);}
    public int width(String value){return parent.width(value);}
    public void strongText(String value,int a,int b,int color,float other){parent.strongText(value,x+Math.round(a*scale),y+Math.round(b*scale),color,scale*other);}
    public void textScaled(String value,int a,int b,int color,float other){parent.textScaled(value,x+Math.round(a*scale),y+Math.round(b*scale),color,scale*other);}
}
