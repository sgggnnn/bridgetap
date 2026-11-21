package com.bridger.momentum;

import java.awt.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/** Bundled Inter, fixed logical metrics and DPI-specific, padded glyph atlases. */
public final class MenuTypography {
    private static Font load(){try(InputStream in=MenuTypography.class.getResourceAsStream("/assets/bridgetap/fonts/Inter-Regular.ttf")){if(in!=null)return Font.createFont(Font.TRUETYPE_FONT,in);}catch(IOException|FontFormatException ignored){}return new Font(Font.SANS_SERIF,Font.PLAIN,12);}
    public static final Font SMALL=load().deriveFont(12f),FONT=SMALL.deriveFont(24f);
    private static final FontRenderContext FRC=new FontRenderContext(null,true,true);
    private static final String EXTRA="\u2026\u2714\u2716\u25B2\u25BC\u2191\u2193";
    private static final float[] ADVANCE=new float[256];private static final Map<Integer,Atlas> ATLASES=new HashMap<>();
    static{for(int i=0;i<256;i++)ADVANCE[i]=(float)SMALL.getStringBounds(String.valueOf(character(i)),FRC).getWidth();}
    private static char character(int glyph){return glyph<EXTRA.length()?EXTRA.charAt(glyph):(char)glyph;}
    public static char glyph(char c){int extra=EXTRA.indexOf(c);return c>=32&&c<256?c:extra>=0?(char)extra:'?';}
    public static float advance(char c){return ADVANCE[glyph(c)];}
    public static int width(String text){float width=0;for(HudText.Span span:HudText.spans(text,0xFFFFFFFF))for(int i=0;i<span.text.length();i++)width+=advance(span.text.charAt(i));return Math.round(width);}
    public static final class Glyph {
        public final int x,y,width,height;public final float offsetX,offsetY,logicalWidth,logicalHeight;public final boolean empty;
        Glyph(int x,int y,Rectangle ink,int pad,int density,float ascent){this.x=x;this.y=y;width=ink.width+pad*2;height=ink.height+pad*2;offsetX=(ink.x-pad)/(float)density;offsetY=ascent+(ink.y-pad)/(float)density;logicalWidth=width/(float)density;logicalHeight=height/(float)density;empty=ink.width==0||ink.height==0;}
    }
    public static final class Atlas {
        public final BufferedImage image;public final Glyph[] glyphs=new Glyph[256];public final int density,padding=6,cell;public final boolean bold;
        Atlas(int density){this(density,false);}
        Atlas(int density,boolean bold){
            this.density=density;this.bold=bold;Font font=SMALL.deriveFont(bold?Font.BOLD:Font.PLAIN,12f*density);Rectangle[] bounds=new Rectangle[256];GlyphVector[] vectors=new GlyphVector[256];int maximum=1;
            for(int i=0;i<256;i++){vectors[i]=font.createGlyphVector(FRC,new char[]{character(i)});bounds[i]=vectors[i].getPixelBounds(FRC,0,0);maximum=Math.max(maximum,Math.max(bounds[i].width,bounds[i].height)+padding*2);}
            int side=1;while(side<maximum)side*=2;cell=side;image=new BufferedImage(cell*16,cell*16,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();g.setColor(Color.WHITE);g.setFont(font);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            float ascent=SMALL.getLineMetrics("Ag",FRC).getAscent();
            for(int i=0;i<256;i++){int x=(i%16)*cell,y=(i/16)*cell;glyphs[i]=new Glyph(x,y,bounds[i],padding,density,ascent);if(i>=32||i<EXTRA.length())g.drawGlyphVector(vectors[i],x+padding-bounds[i].x,y+padding-bounds[i].y);}
            g.dispose();
        }
    }
    public static synchronized Atlas atlas(int density){int key=Math.max(1,Math.min(4,density));return ATLASES.computeIfAbsent(key,Atlas::new);}
    public static synchronized Atlas boldAtlas(int density){int key=Math.max(1,Math.min(4,density));return ATLASES.computeIfAbsent(key+4,k->new Atlas(key,true));}
    public static BufferedImage atlas(){return atlas(2).image;}
    private MenuTypography(){}
}
