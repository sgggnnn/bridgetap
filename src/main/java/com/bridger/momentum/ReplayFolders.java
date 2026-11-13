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
    private volatile long revision;public long revision(){return revision;}
    private int batchDepth;private boolean batchDirty;
    private boolean cleanupFailures;private long lastCleanup;
    private final Map<String,String> serverFolders=new HashMap<>(),parents=new HashMap<>(),modeFolders=new HashMap<>();
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
        for(String key:p.stringPropertyNames())if(key.startsWith("parent.")){String child=key.substring(7),parent=p.getProperty(key);if(names.containsKey(child)&&names.containsKey(parent)&&!child.equals(parent))parents.put(child,parent);}
        for(String child:parents.keySet()){Set<String> seen=new HashSet<>();String at=child;while(parents.containsKey(at)){if(!seen.add(at))throw new IOException("Cyclic replay folders");at=parents.get(at);}}
        for(String key:p.stringPropertyNames())if(key.startsWith("modeFolder.")&&parents.containsKey(p.getProperty(key)))modeFolders.put(key.substring(11),p.getProperty(key));
    }
    public synchronized String parentOf(String id){return parents.getOrDefault(id,ALL);}
    public synchronized int depth(String id){int depth=0;while(parents.containsKey(id)){depth++;id=parents.get(id);}return depth;}
    public synchronized String pathLabel(String id){String result=label(id);while(parents.containsKey(id)){id=parents.get(id);result=label(id)+" / "+result;}return result;}
    private void appendTree(String parent,List<Map.Entry<String,String>> sorted,Map<String,String> result){for(Map.Entry<String,String> e:sorted)if(parentOf(e.getKey()).equals(parent)){result.put(e.getKey(),e.getValue());appendTree(e.getKey(),sorted,result);}}
    public synchronized Map<String,String> folders(){List<Map.Entry<String,String>> list=new ArrayList<>(names.entrySet());list.sort(Comparator.comparing(Map.Entry<String,String>::getValue,String.CASE_INSENSITIVE_ORDER).thenComparing(Map.Entry::getKey));Map<String,String> result=new LinkedHashMap<>();appendTree(ALL,list,result);return Collections.unmodifiableMap(result);}
    public synchronized String label(String id){return id.equals(ALL)?"All folders":id.equals(UNFILED)?"Unfiled":names.getOrDefault(id,"Missing folder");}
    public synchronized String folderOf(String replay){return members.getOrDefault(replay,UNFILED);}
    public synchronized boolean matches(String replay,String filter){if(filter.equals(ALL))return true;String at=folderOf(replay);while(true){if(at.equals(filter))return true;if(!parents.containsKey(at))return false;at=parents.get(at);}}
    public synchronized boolean autoServers(){return autoServers;}
    public synchronized boolean cleanupFailures(){return cleanupFailures;}
    public synchronized void toggleCleanupFailures()throws IOException{cleanupFailures=!cleanupFailures;lastCleanup=0;store(new LinkedHashMap<>(names),new HashMap<>(members));}
    public synchronized int cleanupDue(RunHistory history,long now)throws IOException{if(!cleanupFailures||now-lastCleanup<3600000L)return 0;int deleted=history.deleteUnprotectedFailuresBefore(now-2L*24*60*60*1000);if(deleted>0){Set<String> existing=new HashSet<>();for(RunHistory.Run run:history.saved())existing.add(run.id);for(RunHistory.Run run:history.recent())existing.add(run.id);Map<String,String> retained=new HashMap<>(members);retained.keySet().retainAll(existing);store(new LinkedHashMap<>(names),retained);}lastCleanup=now;return deleted;}
    public synchronized void toggleAutoServers()throws IOException{autoServers=!autoServers;store(new LinkedHashMap<>(names),new HashMap<>(members));}
    public synchronized void fileByServer(RunHistory history,RunHistory.Run run)throws IOException{
        if(!autoServers||run.server.isEmpty()||run.server.equals("Unknown server"))return;
        boolean mcplay=ServerMode.mcplay(run.server);String server=mcplay?"mcplayhd.net":PresetServers.canonical(run.server);
        String current=folderOf(run.id),root=serverFolders.get(server);
        if(root!=null&&!names.containsKey(root))root=null;
        String label=server.equals("singleplayer")?"Singleplayer":server;if(label.length()>64)label=label.substring(0,48)+"-"+Integer.toHexString(server.hashCode());
        if(root==null)for(Map.Entry<String,String> e:names.entrySet())if(parentOf(e.getKey()).isEmpty()&&e.getValue().equalsIgnoreCase(label)){root=e.getKey();break;}
        // Preserve explicit moves to personal folders; migrate old server-root entries.
        if(!current.equals(UNFILED)&&!current.equals(root)&&!modeFolders.containsValue(current))return;
        if(root==null)root=create(label);serverFolders.put(server,root);
        String destination=root;boolean createdModes=false;
        if(mcplay){
            for(ServerMode.Mode mode:ServerMode.Mode.values()){
                String key=root+"."+mode.name(),child=modeFolders.get(key);
                if(child==null||!names.containsKey(child)){child=createChild(root,mode.label);modeFolders.put(key,child);createdModes=true;}
            }
            if(run.modeReady())destination=modeFolders.get(root+"."+run.mode().name());
        }
        if(!current.equals(destination)){move(run.id,destination);}else if(createdModes)store(new LinkedHashMap<>(names),new HashMap<>(members));
    }
    private static String validName(String value){if(value==null||value.trim().isEmpty()||value.length()>64||value.matches(".*[\\p{Cntrl}].*"))throw new IllegalArgumentException("Use 1-64 printable characters");return value.trim();}
    private void checkName(String value,String except,String parent){for(Map.Entry<String,String> e:names.entrySet())if(!e.getKey().equals(except)&&parentOf(e.getKey()).equals(parent)&&e.getValue().equalsIgnoreCase(value))throw new IllegalArgumentException("A folder with that name already exists");}
    public synchronized String create(String value)throws IOException{
        return createChild(ALL,value);
    }
    private String createChild(String parent,String value)throws IOException{
        value=validName(value);checkName(value,"",parent);if(names.size()>=200)throw new IllegalArgumentException("Maximum 200 folders");String id=UUID.randomUUID().toString();Map<String,String> next=new LinkedHashMap<>(names);next.put(id,value);if(!parent.isEmpty())parents.put(id,parent);try{store(next,new HashMap<>(members));}catch(IOException ex){parents.remove(id);throw ex;}return id;
    }
    public synchronized void rename(String id,String value)throws IOException{require(id);value=validName(value);checkName(value,id,parentOf(id));Map<String,String> next=new LinkedHashMap<>(names);next.put(id,value);store(next,new HashMap<>(members));}
    public synchronized void delete(String id)throws IOException{require(id);Map<String,String> next=new LinkedHashMap<>(names),assigned=new HashMap<>(members);Set<String> removed=new HashSet<>();for(String candidate:names.keySet()){String at=candidate;while(true){if(at.equals(id)){removed.add(candidate);break;}if(!parents.containsKey(at))break;at=parents.get(at);}}next.keySet().removeAll(removed);assigned.values().removeIf(removed::contains);store(next,assigned);parents.keySet().removeAll(removed);modeFolders.values().removeIf(removed::contains);}
    public synchronized void move(String replay,String id)throws IOException{
        if(replay==null||replay.isEmpty()||replay.length()>128)throw new IllegalArgumentException("Invalid replay ID");
        if(!id.equals(UNFILED))require(id);if(batchDepth>0){if(id.equals(UNFILED))members.remove(replay);else members.put(replay,id);batchDirty=true;return;}Map<String,String> next=new HashMap<>(members);if(id.equals(UNFILED))next.remove(replay);else next.put(replay,id);
        if(next.size()>100000)throw new IllegalArgumentException("Too many folder entries");store(new LinkedHashMap<>(names),next);
    }
    private void require(String id){if(!names.containsKey(id))throw new IllegalArgumentException("Select a folder first");}
    public synchronized void beginBatch(){batchDepth++;}
    public synchronized void endBatch()throws IOException{if(batchDepth<=0)throw new IllegalStateException("No folder batch");if(--batchDepth==0&&batchDirty){batchDirty=false;store(new LinkedHashMap<>(names),new HashMap<>(members));}}
    public synchronized void retainRuns(Set<String> ids)throws IOException{if(members.keySet().removeIf(id->!ids.contains(id)))store(new LinkedHashMap<>(names),new HashMap<>(members));}
    private void store(Map<String,String> next,Map<String,String> assigned)throws IOException{
        if(batchDepth>0){names=next;members=assigned;batchDirty=true;return;}
        Properties p=new Properties();p.setProperty("version","1");for(Map.Entry<String,String> e:next.entrySet())p.setProperty("folder."+e.getKey(),e.getValue());for(Map.Entry<String,String> e:assigned.entrySet())p.setProperty("run."+e.getKey(),e.getValue());
        p.setProperty("autoServers",String.valueOf(autoServers));
        p.setProperty("cleanupFailures",String.valueOf(cleanupFailures));
        for(Map.Entry<String,String> e:serverFolders.entrySet())if(next.containsKey(e.getValue()))p.setProperty("server."+e.getKey(),e.getValue());
        for(Map.Entry<String,String> e:parents.entrySet())if(next.containsKey(e.getKey())&&next.containsKey(e.getValue()))p.setProperty("parent."+e.getKey(),e.getValue());
        for(Map.Entry<String,String> e:modeFolders.entrySet())if(next.containsKey(e.getValue()))p.setProperty("modeFolder."+e.getKey(),e.getValue());
        Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"folders-",".part");
        try{try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"BridgeTap replay folders");}for(int attempt=0;;attempt++){try{try{Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}break;}catch(AccessDeniedException locked){if(attempt==4)throw locked;try{Thread.sleep(5L<<attempt);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();throw new InterruptedIOException("Folder save interrupted");}}}names=next;members=assigned;revision++;}finally{Files.deleteIfExists(temp);}
    }
}
