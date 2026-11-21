package com.bridger.momentum;

/** Small drawing boundary: native Minecraft in game, an SVG recorder in previews. */
public interface UiCanvas {
    /** Convex polygon fallback for software previews. Native menus batch each face. */
    default void polygon(double[] px,double[] py,int color){
        double low=Double.POSITIVE_INFINITY,high=Double.NEGATIVE_INFINITY;
        for(double y:py){low=Math.min(low,y);high=Math.max(high,y);}
        for(int y=(int)Math.ceil(low);y<high;y++){
            double left=Double.POSITIVE_INFINITY,right=Double.NEGATIVE_INFINITY;
            for(int i=0,j=px.length-1;i<px.length;j=i++)if((py[i]<=y&&py[j]>y)||(py[j]<=y&&py[i]>y)){
                double x=px[i]+(y-py[i])*(px[j]-px[i])/(py[j]-py[i]);left=Math.min(left,x);right=Math.max(right,x);
            }
            if(left<=right)rect((int)Math.ceil(left),y,Math.max(1,(int)Math.ceil(right)-(int)Math.ceil(left)),1,color);
        }
    }
    default void texturedPolygon(double[] px,double[] py,String texture,int tint){polygon(px,py,tint);}
    default void pixelImage(java.awt.image.BufferedImage image,double x,double y,double width,double height,int tint){image(image,x,y,width,height,tint);}
    default void image(java.awt.image.BufferedImage image,double x,double y,double width,double height,int tint){
        if(image==null)return;
        // Bounded software preview fallback; native rendering uploads the thumbnail once.
        int cols=Math.min(128,image.getWidth()),rows=Math.min(64,image.getHeight());
        for(int row=0;row<rows;row++)for(int col=0;col<cols;col++){
            int c=image.getRGB(col*image.getWidth()/cols,row*image.getHeight()/rows);int color=(((c>>>24)*(tint>>>24)/255)<<24)|((((c>>16)&255)*((tint>>16)&255)/255)<<16)|((((c>>8)&255)*((tint>>8)&255)/255)<<8)|((c&255)*(tint&255)/255);
            int left=(int)Math.round(x+width*col/cols),top=(int)Math.round(y+height*row/rows);rect(left,top,(int)Math.ceil(width/cols),(int)Math.ceil(height/rows),color);
        }
    }
    default float pixelDensity(){return 1;}
    default void roundedRect(double x,double y,double width,double height,double radius,int color){
        int left=(int)Math.floor(x),top=(int)Math.floor(y),w=(int)Math.ceil(width),h=(int)Math.ceil(height);double r=Math.max(0,Math.min(radius,Math.min(width,height)/2));
        if(r<1){rect(left,top,w,h,color);return;}
        int edge=(int)Math.ceil(r);rect(left+edge,top,w-edge*2,h,color);rect(left,top+edge,edge,h-edge*2,color);rect(left+w-edge,top+edge,edge,h-edge*2,color);
        for(int row=0;row<edge;row++)for(int col=0;col<edge;col++){
            int covered=0;for(int sy=0;sy<4;sy++)for(int sx=0;sx<4;sx++){double dx=r-col-(sx+.5)/4,dy=r-row-(sy+.5)/4;if(dx*dx+dy*dy<=r*r)covered++;}
            if(covered==0)continue;int c=(((color>>>24)*covered/16)<<24)|(color&0xFFFFFF);rect(left+col,top+row,1,1,c);rect(left+w-col-1,top+row,1,1,c);rect(left+col,top+h-row-1,1,1,c);rect(left+w-col-1,top+h-row-1,1,1,c);
        }
    }
    default void glassRect(double x,double y,double width,double height,double radius,int fill,int border,float reflection){roundedRect(x,y,width,height,radius,border);roundedRect(x+1,y+1,width-2,height-2,Math.max(0,radius-1),fill);}
    void rect(int x,int y,int width,int height,int color);
    void text(String value,int x,int y,int color);
    int width(String value);
    default void strongText(String value,int x,int y,int color,float scale){textScaled(value,x,y,color,scale);}
    void textScaled(String value,int x,int y,int color,float scale);
}
