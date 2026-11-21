package com.bridgetapclient.notifications;

import java.lang.reflect.*;
import java.lang.invoke.*;
import java.util.*;

/** Public, optional API for BridgeTap actions and other client mods. */
public final class Notifications {
    public static final NoticeQueue SETTINGS=new NoticeQueue();
    private static Object config;
    private static boolean initialized;
    private static long retry;
    private static final Map<String,MethodHandle> senders=new HashMap<String,MethodHandle>();
    private Notifications(){}
    public static void tick(){
        long now=System.currentTimeMillis();
        if(!initialized&&now>=retry){retry=now+10000;try{
            Class<?> nativeApi=Class.forName("org.polyfrost.oneconfig.api.notifications.v1.Notifications");
            Class<?> noticeType=Class.forName("org.polyfrost.oneconfig.api.notifications.v1.Notification",false,nativeApi.getClassLoader());
            MethodType signature=MethodType.methodType(noticeType,String.class,String.class,long.class);
            for(String type:new String[]{"info","success","error","warning"})senders.put(type,MethodHandles.publicLookup().findStatic(nativeApi,type,signature));
            if(config==null)config=new NotificationConfig();
            Runtime.getRuntime().addShutdownHook(new Thread(WindowsNotifications::stop,"Windows notification shutdown"));initialized=true;
        }catch(Exception|LinkageError error){System.err.println("[BridgeTapClient Notifications] Service unavailable: "+error.getClass().getSimpleName()+(error instanceof NoClassDefFoundError?" "+error.getMessage():""));}}
        if(!initialized)return;
        WindowsNotifications.tick();
        NoticeQueue.Notice notice=SETTINGS.poll(now);if(notice==null)return;
        try{MethodHandle sender=senders.get(notice.type);if(sender==null)sender=senders.get("info");
            sender.invoke(notice.app+" • "+notice.title,notice.message,(long)Math.max(1,Math.min(15,SETTINGS.seconds))*1000);
        }catch(Throwable error){if(error instanceof VirtualMachineError)throw (VirtualMachineError)error;if(error instanceof ThreadDeath)throw (ThreadDeath)error;System.err.println("[BridgeTapClient Notifications] Display unavailable: "+error.getClass().getSimpleName());}
    }
    public static boolean post(String type,String title,String message){long now=System.currentTimeMillis();return SETTINGS.offer(new NoticeQueue.Notice(UUID.randomUUID().toString(),"bridgetap","BridgeTapClient",title,message,type,now),false,now);}
    public static boolean system(String type,String title,String message){long now=System.currentTimeMillis();return SETTINGS.offer(new NoticeQueue.Notice(UUID.randomUUID().toString(),"notifications","BridgeTapClient",title,message,type,now),false,now);}
    public static void preview(){system("success","Notifications ready","Windows notifications appear directly in Minecraft. No companion app is needed.");}
}
