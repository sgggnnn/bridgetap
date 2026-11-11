package com.bridger.momentum;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Converts packed OpenGL RGB rows into an upright, aspect-preserving video image. */
public final class ClipPixels {
    public static BufferedImage image(byte[] rgb,int width,int height,int outputWidth,int outputHeight){
        if(width<1||height<1||outputWidth<1||outputHeight<1||rgb.length!=(long)width*height*3)throw new IllegalArgumentException("Invalid captured image");
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);int[] row=new int[width];
        for(int y=0;y<height;y++){int p=(height-1-y)*width*3;for(int x=0;x<width;x++){row[x]=((rgb[p]&255)<<16)|((rgb[p+1]&255)<<8)|(rgb[p+2]&255);p+=3;}image.setRGB(0,y,width,1,row,0,width);}
        if(width==outputWidth&&height==outputHeight)return image;
        BufferedImage small=new BufferedImage(outputWidth,outputHeight,BufferedImage.TYPE_INT_RGB);Graphics2D g=small.createGraphics();
        try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(image,0,0,outputWidth,outputHeight,null);}finally{g.dispose();}
        return small;
    }
}
