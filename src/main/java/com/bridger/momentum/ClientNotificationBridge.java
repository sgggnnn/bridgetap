package com.bridger.momentum;

import java.lang.reflect.Method;

/** Optional integration: BridgeTap keeps working when the notification mod is absent. */
public final class ClientNotificationBridge {
    private static Method post;
    private static boolean checked;
    private static UpdateManager.State lastUpdate;
    private ClientNotificationBridge(){}
    public static synchronized boolean post(String type,String title,String message){
        try{
            if(!checked){checked=true;try{post=Class.forName("com.bridgetapclient.notifications.Notifications").getMethod("post",String.class,String.class,String.class);}catch(ClassNotFoundException missing){return false;}}
            return post!=null&&Boolean.TRUE.equals(post.invoke(null,type,title,message));
        }catch(ReflectiveOperationException|LinkageError unavailable){return false;}
    }
    public static void tick(){
        UpdateManager.State state=UpdateManager.state;if(state==lastUpdate)return;lastUpdate=state;
        if(state==UpdateManager.State.AVAILABLE)post("info","BridgeTap update available",UpdateManager.release==null?"A new version is available.":"BridgeTap "+UpdateManager.release.version+" is available.");
        else if(state==UpdateManager.State.READY)post("success","Update ready","The update will be installed when Minecraft closes.");
        else if(state==UpdateManager.State.FAILED)post("error","BridgeTap update",UpdateManager.error);
    }
}
