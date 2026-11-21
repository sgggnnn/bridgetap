package com.bridgetapclient.notifications;

import java.lang.reflect.*;
import java.util.*;

/** Public, optional API for BridgeTap actions and other client mods. */
public final class Notifications {
    public static final NoticeQueue SETTINGS=new NoticeQueue();
    private static Object config;
    private static boolean initialized;
    private static long retry;
    private static final Map<String,Method> senders=new HashMap<String,Method>();
    private Notifications(){}
    public static void tick(){
        long now=System.currentTimeMillis();
        if(!initialized&&now>=retry){retry=now+10000;try{
            Class<?> nativeApi=Class.forName("org.polyfrost.oneconfig.api.notifications.v1.Notifications");
            for(String type:new String[]{"info","success","error","warning"})senders.put(type,nativeApi.getMethod(type,String.class,String.class,long.class));
            if(config==null)config=new NotificationConfig();
            Runtime.getRuntime().addShutdownHook(new Thread(WindowsNotifications::stop,"Windows notification shutdown"));initialized=true;
        }catch(Exception|LinkageError error){System.err.println("[BridgeTapClient Notifications] Service unavailable: "+error.getClass().getSimpleName());}}
        if(!initialized)return;
        WindowsNotifications.tick();
        NoticeQueue.Notice notice=SETTINGS.poll(now);if(notice==null)return;
        try{Method sender=senders.get(notice.type);if(sender==null)sender=senders.get("info");
            sender.invoke(null,notice.app+" • "+notice.title,notice.message,(long)Math.max(1,Math.min(15,SETTINGS.seconds))*1000);
        }catch(Exception|LinkageError error){System.err.println("[BridgeTapClient Notifications] Display unavailable: "+error.getClass().getSimpleName());}
    }
    public static boolean post(String type,String title,String message){long now=System.currentTimeMillis();return SETTINGS.offer(new NoticeQueue.Notice(UUID.randomUUID().toString(),"bridgetap","BridgeTapClient",title,message,type,now),false,now);}
    public static boolean system(String type,String title,String message){long now=System.currentTimeMillis();return SETTINGS.offer(new NoticeQueue.Notice(UUID.randomUUID().toString(),"notifications","BridgeTapClient",title,message,type,now),false,now);}
    public static void preview(){system("success","Notifications ready","Windows notifications appear directly in Minecraft. No companion app is needed.");}
}
