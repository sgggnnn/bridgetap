package com.bridger.momentum.oneclient;

import com.bridger.momentum.*;
import java.util.*;
import java.util.function.*;
import org.polyfrost.oneconfig.api.config.v1.*;
import org.polyfrost.oneconfig.api.config.v1.Properties;
import org.polyfrost.oneconfig.api.hud.v1.Hud;

/** Shared OneClient Style controls plus independent component color pickers in HUD Settings. */
public final class NativeHudStyles {
    private static final Hud[] huds=new Hud[3];private static final int[] text=new int[3],bg=new int[3],shadowColor=new int[3];
    private static final boolean[] shown=new boolean[3],shadow=new boolean[3],full=new boolean[3];
    private static final List<List<Property<?>>> properties=new ArrayList<>();
    public static void bind(int type,Object value){Hud hud=(Hud)value;huds[type]=hud;hud.setTextColor(OverlayStyle.text[type]);hud.setBgColor(OverlayStyle.background(type));hud.setShowBackground(OverlayStyle.backgroundShown(type));hud.setShowShadow(OverlayStyle.shadow(type));hud.setShadowColor(OverlayStyle.shadowColor[type]);hud.setFullShadow(OverlayStyle.fullShadow[type]);snapshot(type);}
    public static void edit(int type){if(huds[type]!=null)org.polyfrost.oneconfig.api.hud.v1.HudManager.pendingSelection=huds[type];org.polyfrost.oneconfig.api.hud.v1.HudManager.INSTANCE.openEditor();}
    private static void snapshot(int t){Hud h=huds[t];text[t]=h.getTextColor();bg[t]=h.getBgColor();shown[t]=h.getShowBackground();shadow[t]=h.getShowShadow();shadowColor[t]=h.getShadowColor();full[t]=h.getFullShadow();}
    public static void sync(){for(int t=0;t<3;t++){Hud h=huds[t];if(h==null)continue;
        if(h.getTextColor()!=text[t])OverlayStyle.text(t,h.getTextColor());else if(OverlayStyle.text[t]!=text[t])h.setTextColor(OverlayStyle.text[t]);
        if(h.getBgColor()!=bg[t])OverlayStyle.background(t,h.getBgColor());else if(OverlayStyle.background(t)!=bg[t])h.setBgColor(OverlayStyle.background(t));
        if(h.getShowBackground()!=shown[t])OverlayStyle.backgroundShown(t,h.getShowBackground());else if(OverlayStyle.backgroundShown(t)!=shown[t])h.setShowBackground(OverlayStyle.backgroundShown(t));
        if(h.getShowShadow()!=shadow[t])OverlayStyle.shadow(t,h.getShowShadow());else if(OverlayStyle.shadow(t)!=shadow[t])h.setShowShadow(OverlayStyle.shadow(t));
        if(h.getShadowColor()!=shadowColor[t])OverlayStyle.shadowColor(t,h.getShadowColor());else if(OverlayStyle.shadowColor[t]!=shadowColor[t])h.setShadowColor(OverlayStyle.shadowColor[t]);
        if(h.getFullShadow()!=full[t]){OverlayStyle.fullShadow[t]=h.getFullShadow();OverlayStyle.save();}else if(OverlayStyle.fullShadow[t]!=full[t])h.setFullShadow(OverlayStyle.fullShadow[t]);
        snapshot(t);
    }}
    private static Property<Integer> color(String id,String title,IntSupplier get,IntConsumer set,boolean alpha){Property<Integer> p=Properties.functional(()->get.getAsInt(),v->set.accept(v),id,title,null,Integer.class);p.addMetadata("visualizer",Visualizer.ColorVisualizer.class);p.addMetadata("category","Colors");if(!alpha)p.addMetadata("noAlpha",true);return p;}
    public static synchronized List<Property<?>> properties(int type){
        while(properties.size()<3)properties.add(null);if(properties.get(type)!=null)return properties.get(type);
        List<Property<?>> result=new ArrayList<>();
        if(type==0)for(int i=0;i<BridgeConfig.colors.length;i++){final int part=i;result.add(color("componentColor"+i,BridgeConfig.COLOR_NAMES[i],()->OverlayStyle.part(part),v->OverlayStyle.part(part,v),i==8||i==9));}
        else if(type==1){result.add(color("textColorRgb","Text color",()->OverlayStyle.text[1],v->OverlayStyle.text(1,v),false));result.add(color("backgroundColorRgb","Background color",()->OverlayStyle.background(1),v->OverlayStyle.background(1,v),true));}
        else{
            result.add(color("idleTextRgb","Released text",()->OverlayStyle.text[2],v->OverlayStyle.text(2,v),false));
            result.add(color("pressedTextRgb","Pressed text",()->OverlayStyle.pressedText,v->{OverlayStyle.pressedText=v|0xFF000000;OverlayStyle.save();},false));
            result.add(color("idleBackgroundRgb","Released background",()->OverlayStyle.background(2),v->OverlayStyle.background(2,v),true));
            result.add(color("pressedBackgroundRgb","Pressed background",()->OverlayStyle.pressedBackground,v->{OverlayStyle.pressedBackground=v;OverlayStyle.save();},true));
            result.add(color("pressedAccentRgb","Pressed accent",()->OverlayStyle.accent,v->{OverlayStyle.accent=v;OverlayStyle.save();},true));
        }
        Property<Void> reset=Properties.dummy("resetOverlayStyle","Reset this overlay's style",null);reset.addMetadata("visualizer",Visualizer.ButtonVisualizer.class);reset.addMetadata("text","Reset");reset.addMetadata("runnable",(Runnable)()->OverlayStyle.reset(type));result.add(reset);
        properties.set(type,Collections.unmodifiableList(result));return properties.get(type);
    }
}
