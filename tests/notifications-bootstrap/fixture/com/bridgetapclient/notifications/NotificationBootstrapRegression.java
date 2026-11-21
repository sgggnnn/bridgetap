package com.bridgetapclient.notifications;
/** Runs the real product bootstrap with an API containing a missing optional overload type. */
public final class NotificationBootstrapRegression {
    public static void main(String[] args)throws Exception{
        try{Class.forName("org.jetbrains.skia.Image");throw new AssertionError("Legacy dependency must be absent");}catch(ClassNotFoundException expected){}
        Notifications.SETTINGS.windows=false;
        Notifications.tick();
        Notifications.preview();
        Notifications.tick();
        if(org.polyfrost.oneconfig.api.notifications.v1.Notifications.sent!=1)throw new AssertionError("Notification registration or dispatch failed without optional legacy Skia");
        System.out.println("PASS: real notification bootstrap and dispatch work with legacy Skia absent");
    }
}
