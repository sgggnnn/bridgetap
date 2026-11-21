package org.polyfrost.oneconfig.api.notifications.v1;
public final class Notifications {
    public static int sent;
    public static Notification info(String title,String message,long duration){sent++;return new Notification();}
    public static Notification success(String title,String message,long duration){sent++;return new Notification();}
    public static Notification error(String title,String message,long duration){sent++;return new Notification();}
    public static Notification warning(String title,String message,long duration){sent++;return new Notification();}
    public static void legacy(org.jetbrains.skia.Image optionalImage){}
}
