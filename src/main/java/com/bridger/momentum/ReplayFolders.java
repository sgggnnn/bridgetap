package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Local organization references replay IDs; removing a folder never deletes a run. */
public final class ReplayFolders {
    public static final String ALL="",UNFILED="unfiled",SAVED="saved-replays";
    private static ReplayFolders local;
    private final Path file;
    private Map<String,String> names=new LinkedHashMap<>(),members=new HashMap<>();
    private boolean autoServers=true;
    private boolean cleanupFailures;private long lastCleanup;
    private final Map<String,String> serverFolders=new HashMap<>();
    public static synchronized ReplayFolders local()throws IOException{if(local==null)local=new ReplayFolders(Paths.get("config","bridgetap","replay-folders.properties"));return local;}
    public ReplayFolders(Path file)throws IOException{this.file=file;
        if(!Files.exists(file))return;if(Files.size(file)>4*1024*1024)throw new IOException("Replay folders file is too large");
        Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}catch(IllegalArgumentException ex){throw new IOException("Invalid replay folders file",ex);}
        if(!"1".equals(p.getProperty("version")))throw new IOException("Unsupported replay folders format");
        autoServers=Boolean.parseBoolean(p.getProperty("autoServers","true"));
        cleanupFailures=Boolean.parseBoolean(p.getProperty("cleanupFailures","false"));
        for(String key:p.stringPropertyNames())if(key.startsWith("folder.")){
            String id=key.substring(7),name=p.getProperty(key);if(!id.matches("[0-9a-f-]{36}"))throw new IOException("Invalid folder ID");
            try{validName(name);}catch(IllegalArgumentException ex){throw new IOException("Invalid folder name",ex);}names.put(id,name);
        }
        if(names.size()>200)throw new IOException("Too many replay folders");
        for(String key:p.stringPropertyNames())if(key.startsWith("run.")){
            String id=key.substring(4),folder=p.getProperty(key);if(id.length()>128||id.isEmpty())throw new IOException("Invalid replay ID");
            if(names.containsKey(folder))members.put(id,folder);
        }
        if(members.size()>100000)throw new IOException("Too many folder entries");
        for(String key:p.stringPropertyNames())if(key.startsWith("server.")&&names.containsKey(p.getProperty(key)))serverFolders.put(key.substring(7),p.getProperty(key));
    }
    public synchronized Map<String,String> folders(){List<Map.Entry<String,String>> list=new ArrayList<>(names.entrySet());list.sort(Comparator.comparing(Map.Entry<String,String>::getValue,String.CASE_INSENSITIVE_ORDER).thenComparing(Map.Entry::getKey));Map<String,String> result=new LinkedHashMap<>();for(Map.Entry<String,String> e:list)result.put(e.getKey(),e.getValue());return Collections.unmodifiableMap(result);}
    public synchronized String label(String id){return id.equals(ALL)?"All folders":id.equals(UNFILED)?"Unfiled":names.getOrDefault(id,"Missing folder");}
    public synchronized String folderOf(String replay){return members.getOrDefault(replay,UNFILED);}
    public synchronized boolean matches(String replay,String filter){return filter.equals(ALL)||folderOf(replay).equals(filter);}
    public synchronized boolean autoServers(){return autoServers;}
    public synchronized boolean cleanupFailures(){return cleanupFailures;}
    public synchronized void toggleCleanupFailures()throws IOException{cleanupFailures=!cleanupFailures;lastCleanup=0;store(new LinkedHashMap<>(names),new HashMap<>(members));}
    public synchronized int cleanupDue(RunHistory history,long now)throws IOException{if(!cleanupFailures||now-lastCleanup<3600000L)return 0;int deleted=history.deleteUnprotectedFailuresBefore(now-2L*24*60*60*1000);if(deleted>0){Set<String> existing=new HashSet<>();for(RunHistory.Run run:history.saved())existing.add(run.id);for(RunHistory.Run run:history.recent())existing.add(run.id);Map<String,String> retained=new HashMap<>(members);retained.keySet().retainAll(existing);store(new LinkedHashMap<>(names),retained);}lastCleanup=now;return deleted;}
    public synchronized void toggleAutoServers()throws IOException{autoServers=!autoServers;store(new LinkedHashMap<>(names),new HashMap<>(members));}
    public synchronized void fileByServer(RunHistory history,RunHistory.Run run)throws IOException{
        if(!autoServers||run.server.isEmpty()||run.server.equals("Unknown server")||!folderOf(run.id).equals(UNFILED))return;
        String server=run.server.toLowerCase(Locale.ROOT);if(server.endsWith(":25565"))server=server.substring(0,server.length()-6);if(server.endsWith("."))server=server.substring(0,server.length()-1);
        String label=server.equals("singleplayer")?"Singleplayer":server;if(label.length()>64)label=label.substring(0,48)+"-"+Integer.toHexString(server.hashCode());
        String destination=serverFolders.get(server);if(destination!=null&&!names.containsKey(destination))destination=null;
        if(destination==null)for(Map.Entry<String,String> e:names.entrySet())if(e.getValue().equalsIgnoreCase(label)){destination=e.getKey();break;}
        if(destination==null)destination=create(label);history.saveAutomatic(run);serverFolders.put(server,destination);move(run.id,destination);
    }
    private static String validName(String value){if(value==null||value.trim().isEmpty()||value.length()>64||value.matches(".*[\\p{Cntrl}].*"))throw new IllegalArgumentException("Use 1-64 printable characters");return value.trim();}
    private void checkName(String value,String except){for(Map.Entry<String,String> e:names.entrySet())if(!e.getKey().equals(except)&&e.getValue().equalsIgnoreCase(value))throw new IllegalArgumentException("A folder with that name already exists");}
    public synchronized String create(String value)throws IOException{
        value=validName(value);checkName(value,"");if(names.size()>=200)throw new IllegalArgumentException("Maximum 200 folders");String id=UUID.randomUUID().toString();Map<String,String> next=new LinkedHashMap<>(names);next.put(id,value);store(next,new HashMap<>(members));return id;
    }
    public synchronized void rename(String id,String value)throws IOException{require(id);value=validName(value);checkName(value,id);Map<String,String> next=new LinkedHashMap<>(names);next.put(id,value);store(next,new HashMap<>(members));}
    public synchronized void delete(String id)throws IOException{require(id);Map<String,String> next=new LinkedHashMap<>(names),assigned=new HashMap<>(members);next.remove(id);assigned.values().removeIf(id::equals);store(next,assigned);}
    public synchronized void move(String replay,String id)throws IOException{
        if(replay==null||replay.isEmpty()||replay.length()>128)throw new IllegalArgumentException("Invalid replay ID");
        if(!id.equals(UNFILED))require(id);Map<String,String> next=new HashMap<>(members);if(id.equals(UNFILED))next.remove(replay);else next.put(replay,id);
        if(next.size()>100000)throw new IllegalArgumentException("Too many folder entries");store(new LinkedHashMap<>(names),next);
    }
    private void require(String id){if(!names.containsKey(id))throw new IllegalArgumentException("Select a folder first");}
    private void store(Map<String,String> next,Map<String,String> assigned)throws IOException{
        Properties p=new Properties();p.setProperty("version","1");for(Map.Entry<String,String> e:next.entrySet())p.setProperty("folder."+e.getKey(),e.getValue());for(Map.Entry<String,String> e:assigned.entrySet())p.setProperty("run."+e.getKey(),e.getValue());
        p.setProperty("autoServers",String.valueOf(autoServers));
        p.setProperty("cleanupFailures",String.valueOf(cleanupFailures));
        for(Map.Entry<String,String> e:serverFolders.entrySet())if(next.containsKey(e.getValue()))p.setProperty("server."+e.getKey(),e.getValue());
        Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"folders-",".part");
        try{try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"BridgeTap replay folders");}try{Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}names=next;members=assigned;}finally{Files.deleteIfExists(temp);}
    }
}
