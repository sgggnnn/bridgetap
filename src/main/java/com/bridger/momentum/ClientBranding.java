package com.bridger.momentum;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.Display;

/** Window branding for the integrated client; standalone BridgeTap keeps its host identity. */
public final class ClientBranding {
    private static boolean checked, integrated, iconApplied;
    private static long nextCheck;
    private ClientBranding() {}

    public static void tick() {
        long now=System.currentTimeMillis();
        if(now<nextCheck)return;
        nextCheck=now+1000;
        if(!checked){
            checked=true;
            integrated=Boolean.getBoolean("bridgetap.client") || ClientBranding.class.getResource("/bridgetapclient-edition")!=null;
        }
        if(!integrated || !Display.isCreated())return;
        String title=Display.getTitle();
        if(!iconApplied || title.contains("OneClient") || title.contains("ONECLIENT") || title.contains("One Client"))
            Display.setTitle("BridgeTapClient | Minecraft 1.8.9");
        if(iconApplied)return;
        iconApplied=true;
        try(InputStream stream=ClientBranding.class.getResourceAsStream("/assets/bridgetap/icon.png")){
            BufferedImage original=ImageIO.read(stream);
            ByteBuffer[] icons=new ByteBuffer[4];
            int[] sizes={16,32,48,256};
            for(int i=0;i<sizes.length;i++){
                int size=sizes[i];
                BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics=image.createGraphics();
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.drawImage(original,0,0,size,size,null);graphics.dispose();
                ByteBuffer pixels=ByteBuffer.allocateDirect(size*size*4);
                for(int y=0;y<size;y++)for(int x=0;x<size;x++){
                    int pixel=image.getRGB(x,y);
                    pixels.put((byte)(pixel>>16)).put((byte)(pixel>>8)).put((byte)pixel).put((byte)(pixel>>24));
                }
                pixels.flip();icons[i]=pixels;
            }
            Display.setIcon(icons);
        }catch(Exception error){System.err.println("[BridgeTapClient] Unable to apply window icon: "+error.getMessage());}
    }
}
