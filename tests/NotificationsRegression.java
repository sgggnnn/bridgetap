package com.bridgetapclient.notifications;

public final class NotificationsRegression {
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static NoticeQueue.Notice notice(String id,String app,long now){return new NoticeQueue.Notice(id,app,app,"Private title","Secret message","info",now);}
    public static void main(String[] args)throws Exception{
        long now=System.currentTimeMillis();NoticeQueue q=new NoticeQueue();
        check(q.allApps&&!q.hideContents,"All Windows apps and complete messages enabled by default");
        check(q.offer(notice("default","Any Windows app",now),true,now),"Arbitrary Windows app accepted by default");
        check(q.poll(now).message.equals("Secret message"),"Full message displayed by default");
        q=new NoticeQueue();q.allApps=false;q.hideContents=true;
        check(!q.offer(notice("other","Notepad",now),true,now),"Optional app filter");
        check(q.offer(notice("one","Discord",now),true,now),"Discord accepted");
        check(!q.offer(notice("one","Discord",now),true,now),"Duplicate rejected");
        NoticeQueue.Notice hidden=q.poll(now);check(hidden.title.equals("New notification")&&hidden.message.equals("Message contents hidden"),"Optional privacy mode");
        check(!q.offer(notice("old","Discord",now-31000),true,now),"Old notifications rejected");
        q.hideContents=false;q.offer(notice("two","Chrome",now),true,now);q.hideContents=true;
        check(q.poll(now+1600).message.equals("Message contents hidden"),"Privacy applies to already pending messages");
        q.allApps=true;q.offer(notice("three","Notepad",now),true,now);q.allApps=false;check(q.poll(now+3200)==null,"Changed filter applies to pending events");
        for(int i=0;i<100;i++)q.offer(notice("burst"+i,"Discord",now),true,now);
        check(q.size()==32,"Bounded queue");q.enabled=false;check(q.poll(now+5000)==null&&q.size()==0,"Disable clears pending events");
        q=new NoticeQueue();q.offer(notice("windows","Discord",now),true,now);q.offer(new NoticeQueue.Notice("bridge","bridgetap","BridgeTap","Saved","Replay saved","success",now),false,now);
        q.clearWindows();check(q.size()==1&&q.poll(now).title.equals("Saved"),"Stopping Windows leaves BridgeTap notifications intact");
        String[] batch=WindowsNotifications.synthetic();
        check(batch.length==6&&batch[0].equals("jni-test")&&batch[2].equals("Native test"),"Embedded DLL loaded and JNI transport valid");
        check(batch[3].equals("Unicode é 你好")&&batch[4].equals("Synthetic notification"),"Unicode Windows text preserved");
        String access=WindowsNotifications.probe();check(!access.isEmpty(),"Windows API activation status available");
        if(access.equals("allowed")){String listener=WindowsNotifications.verifyExistingAccess();check(listener.equals("allowed"),"Native Windows listener and dispatcher: "+listener);}
        WindowsNotifications.stop();
        System.out.println("PASS: all-app default, full messages, optional privacy/filter, deduplication, expiry, bounded queue, native DLL extraction, JNI Unicode transport and Windows API probe: "+access);
    }
}
