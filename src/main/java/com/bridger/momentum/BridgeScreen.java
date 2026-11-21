package com.bridger.momentum;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Shared visual language and scaled hit testing for BridgeTap's native screens. */
public abstract class BridgeScreen extends C_85740840 {
    public static final int WIDTH=860, HEIGHT=500;
    protected static int BG=UiTheme.BACKGROUND,PANEL=UiTheme.PANEL,RAISED=UiTheme.RAISED,BORDER=UiTheme.BORDER;
    protected static int TEXT=UiTheme.TEXT,MUTED=UiTheme.SECONDARY,GREEN=UiTheme.SUCCESS,RED=UiTheme.FAILURE;
    protected static int ACCENT=UiTheme.accent();
    protected UiCanvas ui;
    protected int mouseX,mouseY;
    protected String dropdown="",focus="";
    private final List<Action> actions=new ArrayList<>();
    private final List<Runnable> overlays=new ArrayList<>();
    private Field activeField;
    private Slider activeSlider;
    private static final class Slider {final String id;final int x,width,min,max;final java.util.function.IntConsumer change;Slider(String id,int x,int width,int min,int max,java.util.function.IntConsumer change){this.id=id;this.x=x;this.width=width;this.min=min;this.max=max;this.change=change;}}
    private void slide(int x){if(activeSlider!=null){double value=Math.max(0,Math.min(1,(x-activeSlider.x)/(double)activeSlider.width));activeSlider.change.accept((int)Math.round(activeSlider.min+value*(activeSlider.max-activeSlider.min)));}}

    private String keyboardFocus="",pressedControl="",tooltipText="";private long pressedUntil;private boolean forbidden;
    private String controlId(int x,int y,int w,int h){return x+":"+y+":"+w+":"+h;}

    private final UiMotion motion=new UiMotion();private long openedAt;
    private float controlScale=1;private int controlX,controlY;
    private static final class Action {
        int x,y,w,h;Runnable action;boolean enabled=true,focusable;String id;
        Action(int x,int y,int w,int h,Runnable action){this.x=x;this.y=y;this.w=w;this.h=h;this.action=action;this.id=x+":"+y+":"+w+":"+h;}
        boolean hit(int a,int b){return a>=x&&a<x+w&&b>=y&&b<y+h;}
    }
    private static final class Field {
        String value;int max;Consumer<String> change;
        Field(String value,int max,Consumer<String> change){this.value=value;this.max=max;this.change=change;}
    }
    protected abstract void paint();
    public final void drawPreview(UiCanvas canvas,int mx,int my) {
        UiTheme.refresh();BG=UiTheme.BACKGROUND;PANEL=UiTheme.PANEL;RAISED=UiTheme.RAISED;BORDER=UiTheme.BORDER;TEXT=UiTheme.TEXT;MUTED=UiTheme.SECONDARY;GREEN=UiTheme.SUCCESS;RED=UiTheme.FAILURE;ACCENT=UiTheme.accent();ThemeManager.local().tick();motion.frame(System.nanoTime());ui=canvas;mouseX=mx;mouseY=my;actions.clear();overlays.clear();activeField=null;tooltipText="";forbidden=false;
        paintBackground();paint();
        for(int i=0;i<overlays.size();i++)overlays.get(i).run();
        // Modal/dropdown backdrops also trap keyboard focus, not only mouse clicks.
        int blocker=0;for(int i=0;i<actions.size();i++){Action a=actions.get(i);if(a.x==0&&a.y==0&&a.w==canvasWidth()&&a.h==canvasHeight()&&!a.focusable)blocker=i;}if(blocker>0)actions.subList(0,blocker).clear();
        if(forbidden){int x=mouseX+10,y=mouseY+12;rect(x,y,10,1,RED);rect(x,y+9,10,1,RED);rect(x,y,1,10,RED);rect(x+9,y,1,10,RED);for(int i=0;i<8;i++)rect(x+i+1,y+i+1,1,1,RED);}
        paintTooltip();
    }
    protected void paintBackground(){ui.roundedRect(0,0,canvasWidth(),canvasHeight(),ThemeManager.local().current().cornerRadius+3,backgroundColor());}
    protected boolean flatControls(){return false;}
    protected int canvasWidth(){return WIDTH;}
    protected int canvasHeight(){return HEIGHT;}
    protected float scale(){return Math.min(1f,Math.min((f_05230747-8f)/canvasWidth(),(f_07021018-8f)/canvasHeight()));}
    protected int originX(float s){return (int)((f_05230747/s-canvasWidth())/2);}
    protected int originY(float s){return (int)((f_07021018/s-canvasHeight())/2);}
    @Override public final void m_93956703(int mx,int my,float partial) {
        if(openedAt==0)openedAt=System.nanoTime();float opening=(float)Math.min(1,(System.nanoTime()-openedAt)/180000000.0);opening=1-(1-opening)*(1-opening)*(1-opening);
        UiTheme.refresh();float s=scale();int x=originX(s),y=originY(s);
        try(GlUiState state=new GlUiState()){
            state.clean();

            // Actual pixel density, not Minecraft's nominal GUI scale, drives the glyph atlas.
            java.nio.IntBuffer viewport=org.lwjgl.BufferUtils.createIntBuffer(16);GL11.glGetInteger(GL11.GL_VIEWPORT,viewport);
            if(backdropColor()!=0&&backgroundColor()!=0){
                if(flatControls()){
                    float px=viewport.get(2)/(float)Math.max(1,f_05230747),py=viewport.get(3)/(float)Math.max(1,f_07021018);
                    BackdropBlur.render(ThemeManager.local().current(),opening,viewport.get(0)+x*s*px,viewport.get(1)+(f_07021018-(y+canvasHeight())*s)*py,canvasWidth()*s*px,canvasHeight()*s*py,16*s*Math.min(px,py));
                }else BackdropBlur.render(ThemeManager.local().current(),opening);
            }
            MinecraftUiCanvas nativeUi=new MinecraftUiCanvas(f_11884601,true,s*viewport.get(2)/(float)Math.max(1,f_05230747));
            if(backdropColor()!=0)nativeUi.rect(0,0,f_05230747,f_07021018,UiTheme.alpha(0x050914,ThemeManager.local().current().appearance==ThemeManager.Appearance.LIGHT_GLASS?.03:.08));
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glScalef(s,s,1);GL11.glTranslatef(x,y,0);
            try{drawPreview(new FadedUiCanvas(nativeUi,opening),(int)(mx/s)-x,(int)(my/s)-y);}finally{GL11.glPopMatrix();}
        }
    }
    public final boolean clickPreview(int x,int y,int button) {
        mouseX=x;mouseY=y;
        if(button==1){rightClick(x,y);return true;}
        if(button!=0)return false;
        for(int i=actions.size()-1;i>=0;i--){Action a=actions.get(i);if(a.hit(x,y)){if(a.enabled){keyboardFocus=a.focusable?a.id:"";pressedControl=a.id;pressedUntil=System.nanoTime()+140000000L;a.action.run();}return true;}}
        focus="";dropdown="";return false;
    }
    @Override protected final void m_23755539(int x,int y,int button){float s=scale();clickPreview((int)(x/s)-originX(s),(int)(y/s)-originY(s),button);}
    protected void rightClick(int x,int y){}
    protected int backgroundColor(){return BG;}
    protected int backdropColor(){return 0x55080D15;}
    public void dragPreview(int x,int y){mouseX=x;mouseY=y;slide(x);}
    public void releasePreview(){activeSlider=null;BridgeConfig.save();}
    @Override protected void m_52921597(int x,int y,int button,long elapsed){float s=scale();if(button==0)dragPreview((int)(x/s)-originX(s),(int)(y/s)-originY(s));}
    @Override protected void m_47537840(int x,int y,int button){if(button==0){float s=scale();mouseX=(int)(x/s)-originX(s);mouseY=(int)(y/s)-originY(s);releasePreview();}}
    protected void scroll(int x,int y,int direction){}
    @Override public void m_29921564(){super.m_29921564();int d=Mouse.getEventDWheel();if(d!=0&&dropdown.isEmpty())scroll(mouseX,mouseY,d>0?-1:1);}
    @Override protected final void m_29116903(char c,int key){
        if(key==1){if(!dropdown.isEmpty()){dropdown="";return;}if(escapeModal())return;back();return;}
        boolean control=org.lwjgl.input.Keyboard.isCreated()&&(org.lwjgl.input.Keyboard.isKeyDown(29)||org.lwjgl.input.Keyboard.isKeyDown(157));
        if(shortcut(key,control))return;
        if(keyboardNavigation()&&key==15){focus="";cycleFocus(org.lwjgl.input.Keyboard.isCreated()&&(org.lwjgl.input.Keyboard.isKeyDown(42)||org.lwjgl.input.Keyboard.isKeyDown(54)));return;}
        if(keyboardNavigation()&&(key==28||key==156||key==57)&&focus.isEmpty()&&!keyboardFocus.isEmpty()){
            for(Action a:actions)if(a.id.equals(keyboardFocus)&&a.enabled&&a.focusable){a.action.run();return;}
        }
        if(activeField!=null&&!focus.isEmpty()){
            if(key==28||key==156){focus="";return;}
            if(key==14){String v=activeField.value;if(!v.isEmpty()){int n=v.length();activeField.value=v.substring(0,n-Character.charCount(v.codePointBefore(n)));activeField.change.accept(activeField.value);}return;}
            String append=m_34591428(key)?m_38845775():String.valueOf(c);
            StringBuilder value=new StringBuilder(activeField.value);
            for(int i=0;i<append.length()&&value.length()<activeField.max;i++)if(append.charAt(i)>=32&&append.charAt(i)!=127&&append.charAt(i)!='\u00a7')value.append(append.charAt(i));
            activeField.value=value.toString();activeField.change.accept(activeField.value);return;
        }
        key(key);
    }
    protected boolean shortcut(int key,boolean control){return false;}
    protected boolean keyboardNavigation(){return false;}
    protected final boolean hasButtonFocus(){return !keyboardFocus.isEmpty();}
    protected final void clearButtonFocus(){keyboardFocus="";}
    protected final void suppressTooltips(){tooltipText="";forbidden=false;}
    private void cycleFocus(boolean backwards){List<Action> enabled=new ArrayList<>();for(Action a:actions)if(a.enabled&&a.focusable)enabled.add(a);if(enabled.isEmpty())return;int index=-1;for(int i=0;i<enabled.size();i++)if(enabled.get(i).id.equals(keyboardFocus))index=i;keyboardFocus=enabled.get(index<0?(backwards?enabled.size()-1:0):Math.floorMod(index+(backwards?-1:1),enabled.size())).id;}
    protected void key(int key){}
    protected boolean escapeModal(){return false;}
    protected void back(){Minecraft.m_20213497().m_52715402(null);}
    protected final void open(C_85740840 screen){BridgeConfig.save();Minecraft mc=Minecraft.m_20213497();if(mc!=null)mc.m_52715402(screen);}
    protected final boolean over(int x,int y,int w,int h){return mouseX>=x&&mouseX<x+w&&mouseY>=y&&mouseY<y+h;}
    protected final void region(int x,int y,int w,int h,Runnable callback){actions.add(new Action(controlX+Math.round(x*controlScale),controlY+Math.round(y*controlScale),Math.round(w*controlScale),Math.round(h*controlScale),callback));}
    protected final void controlsAt(int x,int y,float scale,Runnable draw){
        UiCanvas previous=ui;int mx=mouseX,my=mouseY,ox=controlX,oy=controlY;float oldScale=controlScale;
        ui=new ScaledUiCanvas(previous,x,y,scale);mouseX=Math.round((mx-x)/scale);mouseY=Math.round((my-y)/scale);controlX=x;controlY=y;controlScale=scale;
        try{draw.run();}finally{ui=previous;mouseX=mx;mouseY=my;controlX=ox;controlY=oy;controlScale=oldScale;}
    }
    protected final void rect(int x,int y,int w,int h,int color){ui.rect(x,y,w,h,color);}
    protected final void text(String value,int x,int y,int color){ui.text(value,x,y,color);}
    protected final void label(String value,int x,int y){text(value,x,y,MUTED);}
    protected final void clipped(String value,int x,int y,int width,int color){
        if(ui.width(value)<=width){text(value,x,y,color);return;}
        tooltip(value,x,y,width,16);
        while(!value.isEmpty()&&ui.width(value+"\u2026")>width)value=value.substring(0,value.offsetByCodePoints(value.length(),-1));
        if(width>=ui.width("\u2026"))text(value+"\u2026",x,y,color);
    }
    protected final void line(int x,int y,int width){rect(x,y,width,1,BORDER);}
    protected final float animate(String key,float target,float speed){return motion.value(key,target,speed);}
    protected final void rounded(int x,int y,int width,int height,int color){ui.roundedRect(x,y,width,height,Math.min(ThemeManager.local().current().cornerRadius,Math.min(width,height)/2.0),color);}
    protected final void panel(int x,int y,int width,int height){
        double r=ThemeManager.local().current().cornerRadius;
        ui.roundedRect(x,y+3,width,height,r,UiTheme.alpha(0x000000,ThemeManager.local().current().appearance==ThemeManager.Appearance.LIGHT_GLASS?.06:.12));
        ui.glassRect(x,y,width,height,r,PANEL,BORDER,ThemeManager.local().current().surface==ThemeManager.Surface.FLAT?0:.025f);
    }
    protected final void header(String title,String subtitle){
        text("BRIDGETAP",22,11,ACCENT);ui.textScaled(title,22,29,TEXT,1.25f);
        int subtitleX=Math.max(200,38+Math.round(ui.width(title)*1.25f));clipped(subtitle,subtitleX,33,Math.max(0,430-subtitleX),MUTED);
    }
    protected final void footer(String value){ui.glassRect(20,canvasHeight()-26,canvasWidth()-40,24,7,UiTheme.alpha(PANEL,.82),BORDER,0);clipped(value,30,canvasHeight()-20,canvasWidth()-60,TEXT);}
    protected final void tooltip(String value,int x,int y,int width,int height){if(!value.isEmpty()&&over(x,y,width,height))tooltipText=value;}
    private void paintTooltip(){
        if(tooltipText.isEmpty())return;int max=Math.min(360,canvasWidth()-32);List<String> lines=new ArrayList<>();String row="";
        for(String word:tooltipText.replace('\n',' ').split(" ")){
            if(!row.isEmpty()&&ui.width(row+" "+word)>max-20){lines.add(row);row="";}
            for(int offset=0;offset<word.length();offset++){String next=word.substring(offset,offset+1);if(ui.width(row+next)>max-20){lines.add(row);row="";}row+=next;}
            row+=" ";
        }if(!row.trim().isEmpty())lines.add(row.trim());
        int width=0;for(String line:lines)width=Math.max(width,Math.min(max-20,ui.width(line)));width+=20;int height=lines.size()*16+16;
        int x=Math.max(8,Math.min(mouseX+12,canvasWidth()-width-8)),y=Math.max(8,Math.min(mouseY+18,canvasHeight()-height-8));
        rounded(x,y,width,height,UiTheme.PANEL);for(int i=0;i<lines.size();i++)clippedTooltip(lines.get(i),x+10,y+8+i*16,width-20);
    }
    private void clippedTooltip(String value,int x,int y,int width){while(!value.isEmpty()&&ui.width(value)>width)value=value.substring(0,value.offsetByCodePoints(value.length(),-1));text(value,x,y,TEXT);}
    protected final void button(String title,int x,int y,int width,int height,boolean enabled,boolean primary,Runnable action){button(title,x,y,width,height,enabled,primary,false,"",action);}
    protected final void dangerButton(String title,int x,int y,int width,int height,boolean enabled,String hint,Runnable action){button(title,x,y,width,height,enabled,false,true,hint,action);}
    protected final void button(String title,int x,int y,int width,int height,boolean enabled,boolean primary,boolean danger,String hint,Runnable action){
        boolean hover=over(x,y,width,height);String id=controlId(controlX+Math.round(x*controlScale),controlY+Math.round(y*controlScale),Math.round(width*controlScale),Math.round(height*controlScale));
        float hoverAmount=animate("hover:"+id,hover?1:0,16);
        int baseColor=danger?UiTheme.DANGER:primary?ACCENT:flatControls()?UiTheme.alpha(TEXT,.035):RAISED;
        int color=!enabled?UiTheme.DISABLED:UiTheme.alpha(UiTheme.blend(baseColor,primary?0xFFFFFFFF:TEXT,hoverAmount*(primary?.12:.07)),(baseColor>>>24)/255.0);
        if(enabled&&id.equals(pressedControl)&&System.nanoTime()<pressedUntil)color=danger?UiTheme.alpha(RED,.3):primary?UiTheme.blend(ACCENT,0xFF000000,.2):UiTheme.alpha(TEXT,.12);
        ui.glassRect(x,y,width,height,flatControls()?10:Math.min(ThemeManager.local().current().cornerRadius,height/2.0),color,flatControls()?0:primary?UiTheme.alpha(0xFFFFFF,.14):BORDER,flatControls()?0:primary?0:.012f);
        if(id.equals(keyboardFocus))ui.glassRect(x-2,y-2,width+4,height+4,Math.min(ThemeManager.local().current().cornerRadius+2,(height+4)/2.0),0,UiTheme.alpha(ACCENT,.8),0);
        int tw=ui.width(title);clipped(title,x+Math.max(8,(width-tw)/2),y+(height-12)/2,width-16,!enabled?UiTheme.DISABLED_TEXT:primary?UiTheme.onAccent(color):danger?RED:TEXT);
        region(x,y,width,height,()->{focus="";if(enabled)action.run();});Action target=actions.get(actions.size()-1);target.enabled=enabled;target.focusable=true;
        if(hover&&!enabled){forbidden=true;tooltipText=hint.isEmpty()?"Unavailable right now":hint;}else if(hover&&!hint.isEmpty())tooltipText=hint;
    }
    protected final void tabular(String value,int center,int y,int color){int width=0;for(int i=0;i<value.length();i++)width+=Character.isDigit(value.charAt(i))?7:ui.width(value.substring(i,i+1));int x=center-width/2;for(int i=0;i<value.length();i++){String glyph=value.substring(i,i+1);int cell=Character.isDigit(value.charAt(i))?7:ui.width(glyph);text(glyph,x+(cell-ui.width(glyph))/2,y,color);x+=cell;}}
    protected final void statusBadge(boolean success,int x,int y,boolean compact){int width=compact?68:82;rounded(x,y,width,20,UiTheme.alpha(success?GREEN:RED,.16));if(success)check(x+6,y+5,GREEN);else{for(int i=0;i<7;i++){rect(x+6+i,y+6+i,1,1,RED);rect(x+12-i,y+6+i,1,1,RED);}}text(success?"Success":"Failed",x+19,y+3,success?GREEN:RED);}
    protected final void button(String title,int x,int y,int width,Runnable action){button(title,x,y,width,24,true,false,action);}
    protected final void tab(String title,int x,int y,int width,boolean selected,Runnable action){
        rounded(x,y,width,29,selected?ACCENT:RAISED);clipped(title,x+10,y+9,width-20,selected?UiTheme.onAccent(ACCENT):TEXT);
        region(x,y,width,30,()->{focus="";dropdown="";action.run();});
    }
    protected final void toggle(String title,boolean value,int x,int y,int width,Runnable action){
        label(title,x,y+8);int tx=x+width-56;rounded(tx,y+3,56,20,value?UiTheme.alpha(ACCENT,.24):UiTheme.RAISED);
        text(value?"ON":"OFF",tx+(value?8:25),y+7,value?GREEN:MUTED);rounded(tx+(value?39:5),y+7,12,12,value?ACCENT:0xFF939BA2);
        region(x,y,width,26,()->{action.run();BridgeConfig.save();});
    }
    protected final void slider(String id,String title,int value,int min,int max,String suffix,int x,int y,int width,boolean enabled,java.util.function.IntConsumer change){
        label(title,x,y);String number=value+suffix;text(number,x+width-ui.width(number),y,enabled?TEXT:MUTED);
        int top=y+23,track=width-12,tx=x+6;double progress=(value-min)/(double)Math.max(1,max-min);
        ui.roundedRect(tx,top,track,4,2,UiTheme.alpha(TEXT,.12));ui.roundedRect(tx,top,Math.max(4,track*progress),4,2,UiTheme.alpha(ACCENT,enabled?1:.4));
        double knob=tx+track*progress;ui.roundedRect(knob-6,top-4,12,12,6,enabled?TEXT:MUTED);
        Slider model=new Slider(id,tx,track,min,max,change);if(activeSlider!=null&&activeSlider.id.equals(id))activeSlider=model;
        region(x,y+14,width,24,()->{focus="";if(enabled){activeSlider=model;slide(mouseX);}});
    }
    protected final void stepper(String title,String value,int x,int y,int width,Runnable minus,Runnable plus){
        label(title,x,y+8);int rx=x+width-122;button("-",rx,y,25,24,true,false,()->{minus.run();BridgeConfig.save();});
        rect(rx+28,y,64,24,UiTheme.RAISED);clipped(value,rx+33,y+8,54,TEXT);
        button("+",rx+95,y,27,24,true,false,()->{plus.run();BridgeConfig.save();});
    }
    protected final void input(String id,String value,String hint,int x,int y,int width,int max,Consumer<String> change){
        ui.glassRect(x,y,width,26,flatControls()?13:Math.min(8,ThemeManager.local().current().cornerRadius),flatControls()?UiTheme.alpha(TEXT,.04):UiTheme.RAISED,focus.equals(id)?UiTheme.alpha(ACCENT,.75):flatControls()?0:BORDER,0);
        clipped(value.isEmpty()?hint:value+(focus.equals(id)?"|":""),x+9,y+9,width-18,value.isEmpty()?MUTED:TEXT);
        region(x,y,width,26,()->{focus=id;dropdown="";});
        if(focus.equals(id))activeField=new Field(value,max,change);
    }
    protected final void dropdown(String id,String title,String[] options,int selected,int x,int y,int width,Consumer<Integer> change){
        button("",x,y,width,26,true,false,()->{dropdown=dropdown.equals(id)?"":id;});
        int titleWidth=Math.min(ui.width(title),width-38);clipped(title,x+Math.max(8,(width-22-titleWidth)/2),y+9,width-34,TEXT);
        int cx=x+width-16,cy=y+11;for(int i=0;i<4;i++){int yy=dropdown.equals(id)?cy+3-i:cy+i;rect(cx-3+i,yy,1,1,MUTED);rect(cx+3-i,yy,1,1,MUTED);}
        if(!dropdown.equals(id))return;
        overlays.add(()->{
            int top=Math.min(y+30,canvasHeight()-12-options.length*22);
            region(0,0,canvasWidth(),canvasHeight(),()->{dropdown="";focus="";});
            ui.glassRect(x-1,top-1,width+2,options.length*22+2,ThemeManager.local().current().cornerRadius,UiTheme.alpha(PANEL,.96),BORDER,.02f);
            for(int i=0;i<options.length;i++){
                final int index=i;int ry=top+i*22;
                if(selected==i||over(x,ry,width,22))ui.roundedRect(x+3,ry+2,width-6,18,5,selected==i?UiTheme.SELECTED:UiTheme.HOVER);
                clipped(options[i],x+9,ry+7,width-18,selected==i?ACCENT:TEXT);
                region(x,ry,width,22,()->{dropdown="";change.accept(index);});
            }
        });
    }
    protected final void star(int x,int y){
        String[] pixels={"0001000","0001000","1111111","0111110","0011100","0110110","0100010"};
        for(int row=0;row<7;row++)for(int col=0;col<7;col++)if(pixels[row].charAt(col)=='1')rect(x+col,y+row,1,1,0xFFE8C66B);
    }
    protected final void check(int x,int y,int color){rect(x,y+3,2,2,color);rect(x+2,y+5,2,2,color);rect(x+4,y+3,2,2,color);rect(x+6,y+1,2,2,color);}
}
