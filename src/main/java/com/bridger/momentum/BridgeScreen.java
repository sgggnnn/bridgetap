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
    protected static final int BG=0xFF101722,PANEL=0xFF182231,RAISED=0xFF202D3E,BORDER=0xFF304054;
    protected static final int TEXT=0xFFE8EDF5,MUTED=0xFF94A4BA,ACCENT=0xFF63D3D3,GREEN=0xFF80CDA4,RED=0xFFEA939D;
    protected UiCanvas ui;
    protected int mouseX,mouseY;
    protected String dropdown="",focus="";
    private final List<Action> actions=new ArrayList<>();
    private final List<Runnable> overlays=new ArrayList<>();
    private Field activeField;
    private float controlScale=1;private int controlX,controlY;
    private static final class Action {
        int x,y,w,h;Runnable action;
        Action(int x,int y,int w,int h,Runnable action){this.x=x;this.y=y;this.w=w;this.h=h;this.action=action;}
        boolean hit(int a,int b){return a>=x&&a<x+w&&b>=y&&b<y+h;}
    }
    private static final class Field {
        String value;int max;Consumer<String> change;
        Field(String value,int max,Consumer<String> change){this.value=value;this.max=max;this.change=change;}
    }
    protected abstract void paint();
    public final void drawPreview(UiCanvas canvas,int mx,int my) {
        ui=canvas;mouseX=mx;mouseY=my;actions.clear();overlays.clear();activeField=null;
        rect(0,0,canvasWidth(),canvasHeight(),backgroundColor());paint();
        for(int i=0;i<overlays.size();i++)overlays.get(i).run();
    }
    protected int canvasWidth(){return WIDTH;}
    protected int canvasHeight(){return HEIGHT;}
    protected float scale(){return Math.min(1f,Math.min((f_05230747-8f)/WIDTH,(f_07021018-8f)/HEIGHT));}
    protected int originX(float s){return (int)((f_05230747/s-WIDTH)/2);}
    protected int originY(float s){return (int)((f_07021018/s-HEIGHT)/2);}
    @Override public final void m_93956703(int mx,int my,float partial) {
        C_29058632.m_57734177(0,0,f_05230747,f_07021018,backdropColor());
        float s=scale();int x=originX(s),y=originY(s);
        C_37925375.m_55305366();GL11.glPushMatrix();GL11.glScalef(s,s,1);GL11.glTranslatef(x,y,0);
        try {
            drawPreview(new MinecraftUiCanvas(f_11884601),(int)(mx/s)-x,(int)(my/s)-y);
        } finally { GL11.glPopMatrix();C_37925375.m_23590488(); }
    }
    public final boolean clickPreview(int x,int y,int button) {
        mouseX=x;mouseY=y;
        if(button==1){rightClick(x,y);return true;}
        if(button!=0)return false;
        for(int i=actions.size()-1;i>=0;i--){Action a=actions.get(i);if(a.hit(x,y)){a.action.run();return true;}}
        focus="";dropdown="";return false;
    }
    @Override protected final void m_23755539(int x,int y,int button){float s=scale();clickPreview((int)(x/s)-originX(s),(int)(y/s)-originY(s),button);}
    protected void rightClick(int x,int y){}
    protected int backgroundColor(){return BG;}
    protected int backdropColor(){return 0xDD080D15;}
    public void dragPreview(int x,int y){mouseX=x;mouseY=y;}
    public void releasePreview(){BridgeConfig.save();}
    @Override protected void m_52921597(int x,int y,int button,long elapsed){float s=scale();if(button==0)dragPreview((int)(x/s)-originX(s),(int)(y/s)-originY(s));}
    @Override protected void m_47537840(int x,int y,int button){if(button==0){float s=scale();mouseX=(int)(x/s)-originX(s);mouseY=(int)(y/s)-originY(s);releasePreview();}}
    protected void scroll(int x,int y,int direction){}
    @Override public void m_29921564(){super.m_29921564();int d=Mouse.getEventDWheel();if(d!=0&&dropdown.isEmpty())scroll(mouseX,mouseY,d>0?-1:1);}
    @Override protected final void m_29116903(char c,int key){
        if(key==1){if(!dropdown.isEmpty()){dropdown="";return;}if(escapeModal())return;back();return;}
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
        while(!value.isEmpty()&&ui.width(value+"...")>width)value=value.substring(0,value.offsetByCodePoints(value.length(),-1));
        if(width>=ui.width("..."))text(value+"...",x,y,color);
    }
    protected final void line(int x,int y,int width){rect(x,y,width,1,BORDER);}
    protected final void panel(int x,int y,int width,int height){rect(x,y,width,height,PANEL);}
    protected final void header(String title,String subtitle){
        text("BRIDGETAP",22,17,ACCENT);text(title,22,34,TEXT);clipped(subtitle,160,34,WIDTH-340,MUTED);line(20,52,WIDTH-40);
    }
    protected final void footer(String value){line(20,476,WIDTH-40);clipped(value,22,485,WIDTH-44,MUTED);}
    protected final void button(String title,int x,int y,int width,int height,boolean enabled,boolean primary,Runnable action){
        int color=!enabled?0xFF1B2531:primary?(over(x,y,width,height)?0xFF78E1DF:ACCENT):over(x,y,width,height)?0xFF304259:RAISED;
        rect(x,y,width,height,color);rect(x,y,width,1,primary?0xFF9BE8E4:BORDER);
        int tw=ui.width(title);clipped(title,x+Math.max(8,(width-tw)/2),y+(height-8)/2,width-16,!enabled?0xFF59677A:primary?0xFF10232B:TEXT);
        region(x,y,width,height,()->{focus="";if(enabled)action.run();});
    }
    protected final void button(String title,int x,int y,int width,Runnable action){button(title,x,y,width,24,true,false,action);}
    protected final void tab(String title,int x,int y,int width,boolean selected,Runnable action){
        if(selected)rect(x,y,width,29,0xFF203446);clipped(title,x+10,y+9,width-20,selected?ACCENT:MUTED);
        if(selected)rect(x,y+28,width,2,ACCENT);region(x,y,width,30,()->{focus="";dropdown="";action.run();});
    }
    protected final void toggle(String title,boolean value,int x,int y,int width,Runnable action){
        label(title,x,y+8);int tx=x+width-56;rect(tx,y+3,56,20,value?0xFF24483E:0xFF3E2D38);
        text(value?"ON":"OFF",tx+8,y+9,value?GREEN:RED);rect(tx+39,y+7,10,12,value?GREEN:0xFF82717E);
        region(x,y,width,26,()->{action.run();BridgeConfig.save();});
    }
    protected final void stepper(String title,String value,int x,int y,int width,Runnable minus,Runnable plus){
        label(title,x,y+8);int rx=x+width-122;button("-",rx,y,25,24,true,false,()->{minus.run();BridgeConfig.save();});
        rect(rx+28,y,64,24,0xFF101A27);clipped(value,rx+33,y+8,54,TEXT);
        button("+",rx+95,y,27,24,true,false,()->{plus.run();BridgeConfig.save();});
    }
    protected final void input(String id,String value,String hint,int x,int y,int width,int max,Consumer<String> change){
        rect(x,y,width,26,0xFF101A27);rect(x,y+25,width,1,focus.equals(id)?ACCENT:BORDER);
        clipped(value.isEmpty()?hint:value+(focus.equals(id)?"|":""),x+9,y+9,width-18,value.isEmpty()?MUTED:TEXT);
        region(x,y,width,26,()->{focus=id;dropdown="";});
        if(focus.equals(id))activeField=new Field(value,max,change);
    }
    protected final void dropdown(String id,String title,String[] options,int selected,int x,int y,int width,Consumer<Integer> change){
        button(title+"  v",x,y,width,26,true,false,()->{dropdown=dropdown.equals(id)?"":id;});
        if(!dropdown.equals(id))return;
        overlays.add(()->{
            int top=Math.min(y+30,HEIGHT-12-options.length*22);
            region(0,0,WIDTH,HEIGHT,()->{dropdown="";focus="";});
            rect(x-1,top-1,width+2,options.length*22+2,BORDER);rect(x,top,width,options.length*22,PANEL);
            for(int i=0;i<options.length;i++){
                final int index=i;int ry=top+i*22;
                if(selected==i||over(x,ry,width,22))rect(x,ry,width,22,RAISED);
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
