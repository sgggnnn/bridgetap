package com.bridgetapclient.notifications;

import java.util.*;

/** Bounded, transient queue. Message bodies are never written to disk. */
public final class NoticeQueue {
    public static final class Notice {
        public final String id,appId,app,title,message,type;
        public final long created;
        public Notice(String id,String appId,String app,String title,String message,String type,long created){
            this.id=clean(id,160);this.appId=clean(appId,240);this.app=clean(app,64);
            this.title=clean(title,120);this.message=clean(message,400);this.type=clean(type,16);this.created=created;
        }
    }
    public volatile boolean enabled=true,windows=true,bridgeTap=true,hideContents=false,allApps=true;
    public volatile String applications="Discord,Chrome,Google Chrome";
    public volatile int seconds=5;
    private final ArrayDeque<Notice> pending=new ArrayDeque<Notice>();
    private final LinkedHashMap<String,Boolean> seen=new LinkedHashMap<String,Boolean>();
    private long lastShown;
    public synchronized boolean offer(Notice notice,boolean fromWindows,long now){
        if(!enabled||fromWindows&&!windows||!fromWindows&&!bridgeTap&&notice.appId.equals("bridgetap"))return false;
        if(notice.created<now-30000||notice.created>now+10000)return false;
        if(fromWindows&&!allowedApp(notice))return false;
        String key=notice.appId+":"+notice.id;
        if(seen.containsKey(key))return false;
        seen.put(key,true);while(seen.size()>256)seen.remove(seen.keySet().iterator().next());
        while(pending.size()>=32)pending.removeFirst();
        pending.addLast(fromWindows?new Notice(notice.id,notice.appId,notice.app,hideContents?"New notification":notice.title,hideContents?"Message contents hidden":notice.message,"windows",notice.created):notice);
        return true;
    }
    public synchronized Notice poll(long now){
        if(!enabled){pending.clear();return null;}
        while(!pending.isEmpty()&&pending.peekFirst().created<now-20000)pending.removeFirst();
        if(pending.isEmpty()||now-lastShown<1500)return null;
        lastShown=now;Notice notice=pending.removeFirst();
        if(notice.type.equals("windows")){
            if(!windows||!allowedApp(notice))return null;
            if(hideContents)return new Notice(notice.id,notice.appId,notice.app,"New notification","Message contents hidden",notice.type,notice.created);
        }else if(!bridgeTap&&notice.appId.equals("bridgetap"))return null;
        return notice;
    }
    public synchronized void clear(){pending.clear();}
    public synchronized void clearWindows(){for(Iterator<Notice> iterator=pending.iterator();iterator.hasNext();)if(iterator.next().type.equals("windows"))iterator.remove();}
    public synchronized int size(){return pending.size();}
    private boolean allowedApp(Notice notice){
        if(allApps)return true;String hay=(notice.appId+" "+notice.app).toLowerCase(Locale.ROOT);
        for(String app:applications.split(","))if(!app.trim().isEmpty()&&hay.contains(app.trim().toLowerCase(Locale.ROOT)))return true;
        return false;
    }
    static String clean(String value,int limit){
        if(value==null)return "";StringBuilder text=new StringBuilder();
        for(int i=0;i<value.length()&&text.length()<limit;i++){
            char c=value.charAt(i);if(c=='\u00a7'){if(i+1<value.length())i++;continue;}
            text.append(Character.isISOControl(c)?' ':c);
        }
        return text.toString().trim();
    }
}
