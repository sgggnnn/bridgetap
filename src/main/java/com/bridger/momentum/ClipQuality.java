package com.bridger.momentum;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/** Lossless capture storage; video compression happens only once, at export. */
public final class ClipQuality {
    public static final int DEFAULT_HEIGHT=720;
    public static int[] size(int width,int height,int maximumHeight){
        if(width<2||height<2||(maximumHeight!=360&&maximumHeight!=720&&maximumHeight!=1080))throw new IllegalArgumentException("Invalid capture dimensions");
        double factor=Math.min(1,Math.min(maximumHeight/(double)height,(maximumHeight*16.0/9)/width));
        return new int[]{Math.max(2,(int)(width*factor)/2*2),Math.max(2,(int)(height*factor)/2*2)};
    }
    public static byte[] lossless(BufferedImage image)throws IOException{
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        ImageWriter writer=ImageIO.getImageWritersByFormatName("png").next();try(MemoryCacheImageOutputStream output=new MemoryCacheImageOutputStream(bytes)){
            writer.setOutput(output);ImageWriteParam params=writer.getDefaultWriteParam();if(params.canWriteCompressed()){params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);params.setCompressionQuality(.75f);}writer.write(null,new IIOImage(image,null,null),params);output.flush();
        }finally{writer.dispose();}return bytes.toByteArray();
    }
}
