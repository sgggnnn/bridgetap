package com.bridgetapclient.notifications;

import org.polyfrost.oneconfig.api.config.v1.*;
import org.polyfrost.oneconfig.api.config.v1.annotations.*;

/** Uses OneConfig's own notification renderer, styling and notification center. */
public final class NotificationConfig extends Config {
    @MasterToggle @Switch(title="Enable notifications",category="General") public boolean enabled=true;
    @org.polyfrost.oneconfig.api.config.v1.annotations.Number(title="Toast duration (seconds)",min=1,max=15,category="General") public int duration=5;
    @Switch(title="Receive Windows notifications",category="Windows") public boolean windows=true;
    @Switch(title="Hide message contents",category="Windows") public boolean hideContents=false;
    @Switch(title="Allow every Windows application",category="Windows") public boolean allApps=true;
    @Text(title="Allowed apps (comma separated)",category="Windows") public String applications="Discord,Chrome,Google Chrome";
    @Switch(title="BridgeTap confirmations and errors",category="BridgeTap") public boolean bridgeTap=true;
    public NotificationConfig(){super("bridgetapclient-notifications.json","assets/bridgetapclient-notifications/icon.png","BridgeTapClient Notifications",Category.UTILITY);initialize(false);}
    @Override protected void onInitialized(Tree tree){
        super.onInitialized(tree);tree.addMetadata("mod_card_authors","sggn");
        for(String field:new String[]{"enabled","duration","windows","hideContents","allApps","applications","bridgeTap"})addCallback(field,()->apply());
        apply();
    }
    private void apply(){NoticeQueue s=Notifications.SETTINGS;s.enabled=enabled;s.seconds=duration;s.windows=windows;s.hideContents=hideContents;s.allApps=allApps;s.applications=applications;s.bridgeTap=bridgeTap;if(!enabled)s.clear();}
    @Button(title="Preview a notification",text="Preview",category="General") public void preview(){Notifications.preview();}
    @Button(title="Windows notification permission",text="Allow access",category="Windows") public void allowAccess(){WindowsNotifications.requestAccess();}
    @Button(title="Windows access status",text="Show status",category="Windows") public void accessStatus(){Notifications.system("info","Windows notifications",WindowsNotifications.probe());}
}
