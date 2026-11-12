package com.bridger.momentum;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Optional OneConfig v1 bridge. No OneConfig types are linked on standalone installs. */
public final class OneClientIntegration {
    private static boolean tried,registered;
    private static Object manager,configAdapter;
    private static Method sync;
    private static Method editing;
    private static final float[][] sizes={{270,74},{420,118},{76,110}};
    private static final String[] TITLES={"Bridge coordinates","Replay information","Replay keystrokes"};
    public static void tick(){
        if(tried){if(registered)try{sync.invoke(configAdapter);}catch(ReflectiveOperationException ex){System.err.println("[BridgeTap] OneConfig settings sync: "+ex);}return;}tried=true;
        try{
            Class<?> wrapper=Class.forName("org.polyfrost.oneconfig.api.hud.v1.OneConfigHudWrapper");
            Class<?> impl=Class.forName(wrapper.getName()+"$DefaultImpls");
            Class<?> hudManager=Class.forName("org.polyfrost.oneconfig.api.hud.v1.HudManager");
            manager=hudManager.getField("INSTANCE").get(null);editing=hudManager.getMethod("isEditing");
            // The optional config adapter is packaged by build-oneclient.ps1 only.
            Class<?> adapter=Class.forName("com.bridger.momentum.oneclient.OneClientConfig");configAdapter=adapter.getConstructor().newInstance();sync=adapter.getMethod("syncFromBridge");
            for(int type=0;type<3;type++){
                final int index=type;
                Object proxy=Proxy.newProxyInstance(wrapper.getClassLoader(),new Class<?>[]{wrapper},(self,method,args)->invoke(index,self,method,args));
                impl.getMethod("register",wrapper).invoke(null,proxy);
            }
            for(Object hud:(java.util.List<?>)hudManager.getMethod("instances").invoke(null))for(int t=0;t<3;t++)if(("bridgetap-"+t).equals(hud.getClass().getField("id").get(hud)))adapter.getMethod("bindHudStyle",int.class,Object.class).invoke(configAdapter,t,hud);
            registered=true;System.out.println("[BridgeTap] OneConfig menus and 3 HUDs registered");
        }catch(ClassNotFoundException absent){/* Standalone installation. */}
        catch(ReflectiveOperationException|LinkageError ex){System.err.println("[BridgeTap] OneConfig integration unavailable: "+ex);}
    }
    public static boolean editing(){if(!registered)return false;try{return (Boolean)editing.invoke(manager);}catch(ReflectiveOperationException ex){return false;}}
    public static void recordSize(int type,float width,float height){sizes[type][0]=width;sizes[type][1]=height;}
    private static float scale(int t){return (float)(t==0?BridgeConfig.hudScale:t==1?BridgeConfig.replayHudScale:BridgeConfig.inputHudScale);}
    private static float x(int t){int value=t==0?BridgeConfig.hudX:t==1?BridgeConfig.replayHudX:BridgeConfig.inputHudX;if(value>=0)return value;C_43718703 w=new C_43718703(Minecraft.m_20213497());return Math.max(0,(w.m_39119333()-sizes[t][0]*scale(t))/2);}
    private static float y(int t){int value=t==0?BridgeConfig.hudY:t==1?BridgeConfig.replayHudY:BridgeConfig.inputHudY;if(value>=0)return value;C_43718703 w=new C_43718703(Minecraft.m_20213497());return Math.max(0,w.m_16103615()-88-sizes[t][1]*scale(t));}
    private static Object invoke(int t,Object self,Method method,Object[] args){
        String name=method.getName();float value=args!=null&&args.length>0&&args[0] instanceof Number?((Number)args[0]).floatValue():0;
        switch(name){
            case "getId":return "bridgetap-"+t;
            case "getName":return TITLES[t];
            case "getModId":return "bridgetap";
            case "getX":return x(t);case "getY":return y(t);case "getScale":return scale(t);
            case "setX":if(t==0)BridgeConfig.hudX=Math.round(value);else if(t==1)BridgeConfig.replayHudX=Math.round(value);else BridgeConfig.inputHudX=Math.round(value);return null;
            case "setY":if(t==0)BridgeConfig.hudY=Math.round(value);else if(t==1)BridgeConfig.replayHudY=Math.round(value);else BridgeConfig.inputHudY=Math.round(value);return null;
            case "setScale":value=Math.max(.5f,Math.min(2,value));if(t==0)BridgeConfig.hudScale=value;else if(t==1)BridgeConfig.replayHudScale=value;else BridgeConfig.inputHudScale=value;return null;
            case "getScaledWidth":return sizes[t][0]*scale(t);case "getScaledHeight":return sizes[t][1]*scale(t);
            case "getHidden":return !(t==0?BridgeConfig.enabled:t==1?BridgeConfig.showReplayInfo:BridgeConfig.showReplayInputs);
            case "setHidden":boolean shown=!(Boolean)args[0];if(t==0)BridgeConfig.enabled=shown;else if(t==1)BridgeConfig.showReplayInfo=shown;else BridgeConfig.showReplayInputs=shown;return null;
            case "getSupportsScale":case "getPlacementReady":return true;
            case "getOwnsPlacement":return false;
            case "getResizeAxes":try{return Class.forName("org.polyfrost.oneconfig.api.hud.v1.HudResize").getField("Both").get(null);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
            case "xForWidth":return x(t);case "yForHeight":return y(t);
            case "linkedProperties":try{return configAdapter.getClass().getMethod("hudProperties",int.class).invoke(configAdapter,t);}catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
            case "save":case "onDragEnd":BridgeConfig.save();return null;
            case "hashCode":return System.identityHashCode(self);case "equals":return self==args[0];case "toString":return TITLES[t];
            case "onDragStart":case "setScaledWidth":case "setScaledHeight":case "setId":case "setName":return null;
            default:throw new UnsupportedOperationException("Unsupported OneConfig HUD method: "+name);
        }
    }
    /** Editor previews use the same positions and scales as live rendering. */
    public static void renderEditor(){
        Minecraft mc=Minecraft.m_20213497();if(mc.f_21497584==null)return;
        UiCanvas canvas=new MinecraftUiCanvas(mc.f_21497584);
        java.util.List<JumpEntry> entries=Arrays.asList(new JumpEntry("1.0 Flat",.504,.504,Double.NaN,3,JumpEntry.EntryType.FLAT),new JumpEntry("Turn",.216,.216,.475,15,JumpEntry.EntryType.TURN));
        java.util.List<String> coords=new ArrayList<>();coords.add(OverlayStyle.color(7)+"Success 3.85s / 3 jumps / Axis Z");coords.addAll(EntryFormatter.hudLines(entries));
        String[][] lines={coords.toArray(new String[0]),{"\u00a7bBridge replay \u00a7f0.90 / 3.85s | Tick 18/77","Ghost XYZ: 1.4366 66.1661 8.9902","Inputs: Back Jump RMB","Velocity X -0.0003 Y 0.0831 Z 0.2874"}};
        for(int t=0;t<3;t++){
            if(t==0&&!BridgeConfig.enabled||t==1&&!BridgeConfig.showReplayInfo||t==2&&!BridgeConfig.showReplayInputs)continue;
            UiCanvas target=new ScaledUiCanvas(new MinecraftUiCanvas(mc.f_21497584,t),Math.round(x(t)),Math.round(y(t)),scale(t));
            if(t==2){ReplayInputOverlay.paint(target,0,0,new ReplayData.TickStats(1|4|16|256,0,0,0,1,0,0,19,0,0,.08,.28));recordSize(t,76,92);continue;}
            int width=0;for(String line:lines[t])width=Math.max(width,canvas.width(line)+8);recordSize(t,width,lines[t].length*11+8);
            if(OverlayStyle.backgroundShown(t))target.rect(0,0,width,lines[t].length*11+8,OverlayStyle.background(t));
            for(int i=0;i<lines[t].length;i++)target.text(OverlayStyle.styled(t,lines[t][i]),4,4+i*11,0xFFFFFFFF);
        }
    }
}
