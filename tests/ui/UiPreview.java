import com.bridger.momentum.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Renders the real screen paint paths with Minecraft's bitmap font, without GL. */
public final class UiPreview implements UiCanvas {
    private final BufferedImage image=new BufferedImage(860,500,BufferedImage.TYPE_INT_ARGB);
    private final BufferedImage font;
    private final Graphics2D graphics=image.createGraphics();
    private final int[] palette={0,0x0000AA,0x00AA00,0x00AAAA,0xAA0000,0xAA00AA,0xFFAA00,0xAAAAAA,0x555555,0x5555FF,0x55FF55,0x55FFFF,0xFF5555,0xFF55FF,0xFFFF55,0xFFFFFF};
    UiPreview(BufferedImage font){this.font=font;graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);}
    public void rect(int x,int y,int w,int h,int color){graphics.setColor(new Color(color,true));graphics.fillRect(x,y,w,h);}
    private int charWidth(char c){if(c==' ')return 4;int code=c<256?c:'?',x=(code%16)*8,y=(code/16)*8;for(int col=7;col>=0;col--)for(int row=0;row<8;row++)if((font.getRGB(x+col,y+row)>>>24)>0)return col+2;return 2;}
    public int width(String s){int result=0;for(int i=0;i<s.length();i++){if(s.charAt(i)=='\u00a7'&&i+1<s.length()){i++;continue;}result+=charWidth(s.charAt(i));}return result;}
    public void text(String s,int x,int y,int color){textScaled(s,x,y,color,1);}
    public void textScaled(String s,int x,int y,int color,float scale){
        int cursor=0,tint=color;
        for(int i=0;i<s.length();i++){
            char c=s.charAt(i);if(c=='\u00a7'&&i+1<s.length()){int code=Character.digit(s.charAt(++i),16);if(code>=0)tint=0xFF000000|palette[code];continue;}
            int code=c<256?c:'?';BufferedImage glyph=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            for(int row=0;row<8;row++)for(int col=0;col<8;col++){int a=font.getRGB((code%16)*8+col,(code/16)*8+row)>>>24;glyph.setRGB(col,row,(a<<24)|(tint&0xFFFFFF));}
            AffineTransform t=new AffineTransform();t.translate(x+cursor*scale,y);t.scale(scale,scale);graphics.drawImage(glyph,t,null);cursor+=charWidth(c);
        }
    }
    private void save(String name)throws Exception{
        BufferedImage larger=new BufferedImage(1720,1000,BufferedImage.TYPE_INT_ARGB);Graphics2D g=larger.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(image,0,0,1720,1000,null);g.dispose();ImageIO.write(larger,"png",Paths.get(name+".png").toFile());
    }
    private static UiPreview render(BridgeScreen screen,BufferedImage font,String name)throws Exception{UiPreview canvas=new UiPreview(font);screen.drawPreview(canvas,-10,-10);canvas.save(name);return canvas;}
    public static void main(String[] args)throws Exception{
        testDetectionIntegration();
        BufferedImage font;try(ZipFile z=new ZipFile(args[0])){font=ImageIO.read(z.getInputStream(z.getEntry("assets/minecraft/textures/font/ascii.png")));}
        List<ReplayData.Block> blocks=Arrays.asList(new ReplayData.Block(0,69,0,1,0,0),new ReplayData.Block(0,74,31,1,0,0),new ReplayData.Block(0,69,1,24,0,0,true),new ReplayData.Block(0,74,30,24,0,1,true));
        ReplayData data=new ReplayData(Arrays.asList(new ReplayData.Frame(.5,70,.5),new ReplayData.Frame(.5,75,31.5)),blocks,false);
        for(int i=9;i>=0;i--){
            BridgeTracker.history().add(i%4==0?"FAILED":"SUCCESS",3.85+i*.12,7+i%3,i%2==0?"X":"Z",Arrays.asList("1.0 Flat: .504 +.n/a (3t)","5b Late: .460 -.044 (9t)","Tap Speed #2: .541 +.081 (9t)","Turn: .216 +.475 (15t)","Plates-1t: .260","Plates: .102"),data);
            RunHistory.Run run=BridgeTracker.history().recent().get(0);BridgeTracker.history().rename(run,i==0?"Opening / 1.0 Flat to 5b Late":i==1?"Evening practice / clean run":"Bridge practice "+(i+1));
            if(i%3==0)BridgeTracker.history().toggleFavorite(run);
        }
        BridgeHistoryScreen library=new BridgeHistoryScreen();render(library,font,"library");
        library.clickPreview(700,110,0);render(library,font,"filters");
        library.clickPreview(200,225,0);render(library,font,"axis-options");library.clickPreview(200,294,0);
        render(library,font,"filter-selection");library.clickPreview(600,365,0);render(library,font,"filtered");
        if(!"Z".equals(BridgeConfig.replayAxis))throw new AssertionError("Filter click target did not map to Z");
        library.clickPreview(450,110,0);render(library,font,"sort-options");library.clickPreview(440,158,0);render(library,font,"sorted");
        if(!"FASTEST".equals(BridgeConfig.replaySort))throw new AssertionError("Sort dropdown did not select time ascending");
        library.clickPreview(610,110,0);render(library,font,"descending");if(!"SLOWEST".equals(BridgeConfig.replaySort))throw new AssertionError("Sort direction did not change");
        BridgeSettingsScreen settings=new BridgeSettingsScreen();render(settings,font,"settings");
        BridgeColorsScreen colors=new BridgeColorsScreen();render(colors,font,"colors");colors.clickPreview(65,145,0);render(colors,font,"color-selected");colors.clickPreview(530,150,0);
        if(!"4".equals(BridgeConfig.colors[1]))throw new AssertionError("Swatch click did not update selected coordinate color");BridgeConfig.colors[1]="e";
        BridgeLayoutScreen layout=new BridgeLayoutScreen(settings);render(layout,font,"layout");int before=BridgeConfig.hudX;
        layout.clickPreview(45,117,0);layout.dragPreview(140,195);layout.releasePreview();render(layout,font,"layout-dragged");
        if(BridgeConfig.hudX<=before||BridgeConfig.hudY<=10)throw new AssertionError("Studio drag did not update both coordinates");
        java.lang.reflect.Field boundField=BridgeLayoutScreen.class.getDeclaredField("bounds");boundField.setAccessible(true);int[] b=((int[][])boundField.get(layout))[0];
        java.lang.reflect.Field sceneScale=BridgeLayoutScreen.class.getDeclaredField("sceneScale"),sceneX=BridgeLayoutScreen.class.getDeclaredField("sceneX"),sceneY=BridgeLayoutScreen.class.getDeclaredField("sceneY");
        sceneScale.setAccessible(true);sceneX.setAccessible(true);sceneY.setAccessible(true);float scale=sceneScale.getFloat(layout);
        int handleX=sceneX.getInt(layout)+Math.round(b[0]*scale)+Math.round(b[2]*scale),handleY=sceneY.getInt(layout)+Math.round(b[1]*scale)+Math.round(b[3]*scale);
        double oldScale=BridgeConfig.hudScale;layout.clickPreview(handleX,handleY,0);layout.dragPreview(handleX+30,handleY+18);layout.releasePreview();
        if(BridgeConfig.hudScale<=oldScale)throw new AssertionError("Lower-right handle did not resize the selected overlay");render(layout,font,"layout-resized");
        UiPreview inputs=new UiPreview(font);inputs.rect(0,0,860,500,0xFF101722);ReplayInputOverlay.paint(new ScaledUiCanvas(inputs,50,30,3),0,0,new ReplayData.TickStats(1|4|16|256,0,0,0,0,0,0,0,0,0,0,0));inputs.save("inputs");
        System.out.println("PASS: real screen previews, filter/sort hit targets, color selection and HUD dragging without GL");
    }
    private static void testDetectionIntegration()throws Exception{
        java.lang.reflect.Method jump=BridgeTracker.class.getDeclaredMethod("recordJump",double.class,double.class,double.class,int.class,double.class);jump.setAccessible(true);
        for(int ticks:new int[]{3,4}){
            BridgeTracker.reset();BridgeConfig.preferredAxis="AUTO";
            java.lang.reflect.Field startZ=BridgeTracker.class.getDeclaredField("startZ");startZ.setAccessible(true);startZ.setDouble(null,.5);
            PlacementEvidence.record(1,0,0,1,.5);jump.invoke(null,.5,70.0,.5,ticks,1.20);
            if(!"X".equals(BridgeTracker.getCurrentAxis()))throw new AssertionError("AUTO axis detection ignored a short opening on X");
            if(!BridgeTracker.getEntries().get(0).getLabel().equals(ticks==3?"1.0 Flat":"1.05 Flat"))throw new AssertionError("Tracker did not use the opening flat rule");
            PlacementEvidence.record(ticks+2,1,0,2,.5);PlacementEvidence.record(ticks+5,2,0,3,.5);
            jump.invoke(null,3.5,71.0,.5,ticks+9,1.20);
            if(!BridgeTracker.getEntries().get(1).getLabel().equals(ticks==3?"5b Late":"6b Late"))throw new AssertionError("Tracker did not apply late geometry to the first follow-up jump");
            PlacementEvidence.record(ticks+13,4,0,5,.5);PlacementEvidence.record(ticks+16,5,0,6,.5);
            jump.invoke(null,6.5,72.0,.5,ticks+18,1.20);
            if(BridgeTracker.getEntries().get(2).getLabel().contains("Late"))throw new AssertionError("A later jump was mislabeled as an opening late");
        }
        BridgeTracker.reset();BridgeConfig.preferredAxis="AUTO";
        System.out.println("PASS: tracker integration for 1.0/1.05 flats, 5b/6b lates and opening-only labels");
    }
}
