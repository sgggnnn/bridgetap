package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Recent summaries are transient; saved summaries each have their own file. */
public class RunHistory {
 public static final class Run {
  public final String id, status, axis,server;
  public final double time;
  public final int jumps;
  public final long created;
  public final List<String> details;
  public final ReplayData replay;
  private volatile ServerMode.Measurement modeMeasurement;
  public ServerMode.Mode mode(){return ServerMode.detect(server,modeMeasurement);}
  public void observeMeasurement(ServerMode.Measurement measured){if(measured!=null&&measured.available&&ServerMode.detect(server,measured)!=ServerMode.Mode.UNKNOWN)modeMeasurement=measured;}
  public boolean modeReady(){return modeMeasurement!=null;}
  void observeMode(ReplayData data){ServerMode.Measurement measured=ServerMode.measure(data);if(modeMeasurement==null||mode()==ServerMode.Mode.UNKNOWN||ServerMode.detect(server,measured)!=ServerMode.Mode.UNKNOWN)modeMeasurement=measured;}
  private String name="";
  private boolean protectedSave;
  public boolean isProtected(){return protectedSave;}
  private boolean imported;
  public boolean isImported(){return imported;}
  public String name(){return name;}
  public String displayName(){return name.isEmpty()?"Bridge replay":name;}
  public String serverLabel(){return server.isEmpty()?"Unknown (older recording)":server;}
  public String replayId() {
   String value=new java.math.BigInteger(id.replace("-",""),16).toString(36);
   while(value.length()<25)value="0"+value;
   return value.substring(0,5)+"-"+value.substring(5,10)+"-"+value.substring(10);
  }
  private Run(String id,String status,double time,int jumps,String axis,long created,List<String> details) {
   this(id,status,time,jumps,axis,created,details,null);
  }
  private Run(String id,String status,double time,int jumps,String axis,long created,List<String> details,ReplayData replay) {
   this(id,status,time,jumps,axis,created,details,replay,"");
  }
  private Run(String id,String status,double time,int jumps,String axis,long created,List<String> details,ReplayData replay,String server) {
   this.id=id; this.status=status; this.time=time; this.jumps=jumps; this.axis=axis; this.created=created;
   this.details=Collections.unmodifiableList(new ArrayList<>(details));
   this.replay=replay;
   this.server=validServer(server);if(!ServerMode.mcplay(this.server))modeMeasurement=ServerMode.UNAVAILABLE;else if(replay!=null)observeMode(replay);
  }
 }
 private static final java.util.concurrent.ExecutorService MODE_WORKER=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"BridgeTap replay mode index");t.setDaemon(true);return t;});
 private final java.util.concurrent.ConcurrentMap<String,java.util.concurrent.CompletableFuture<ServerMode.Measurement>> modeIndex=new java.util.concurrent.ConcurrentHashMap<>();
 public void indexModes(Collection<Run> runs){int budget=8;for(Run run:runs)if(!run.modeReady()){if(budget--<=0||modeIndex.size()>=16)break;modeIndex.computeIfAbsent(run.id,id->java.util.concurrent.CompletableFuture.supplyAsync(()->{try{return ServerMode.measure(replay(run));}catch(IOException|RuntimeException error){return ServerMode.UNAVAILABLE;}},MODE_WORKER)).thenAccept(measurement->{run.modeMeasurement=measurement;modeIndex.remove(run.id);});}}
 private final java.util.concurrent.atomic.AtomicLong metadataRevision=new java.util.concurrent.atomic.AtomicLong();
 private final java.util.concurrent.atomic.AtomicBoolean loadingSaved=new java.util.concurrent.atomic.AtomicBoolean();
 private volatile List<Run> cachedSaved=Collections.emptyList();private volatile long cachedRevision=-1,catalogVersion;private volatile String catalogError="";
 public long catalogVersion(){return catalogVersion;}public boolean loadingSaved(){return loadingSaved.get();}public String catalogError(){return catalogError;}
 private void metadataChanged(){metadataRevision.incrementAndGet();catalogVersion++;}
 public void reloadSaved(){metadataChanged();refreshSavedAsync();}
 public void refreshSavedAsync(){if(!loadingSaved.compareAndSet(false,true))return;MODE_WORKER.execute(()->{try{saved();catalogError="";}catch(IOException ex){catalogError=ex.getMessage();}finally{loadingSaved.set(false);}});}
 public List<Run> savedCached(){if(cachedRevision!=metadataRevision.get())refreshSavedAsync();return new ArrayList<>(cachedSaved);}
 /** Blocking snapshot for the storage worker, never called by painting code. */
 public List<Run> savedSnapshot()throws IOException{return cachedRevision==metadataRevision.get()?new ArrayList<>(cachedSaved):saved();}
 public List<Run> favoritesCached()throws IOException{List<Run> result=new ArrayList<>();for(Run run:savedCached())if(isFavorite(run))result.add(run);return result;}

 private final Path folder;
 private final List<Run> recent=new ArrayList<>();
 private volatile Set<String> favorites;
 private final Map<String,Path> archivedLocations=new java.util.concurrent.ConcurrentHashMap<>();
 public RunHistory(Path folder) {this.folder=folder;}
 public void add(String status,double time,int jumps,String axis,List<String> details) {
  add(status,time,jumps,axis,details,null);
 }
 public void add(String status,double time,int jumps,String axis,List<String> details,ReplayData replay) {
  add(status,time,jumps,axis,details,replay,"");
 }
 public void add(String status,double time,int jumps,String axis,List<String> details,ReplayData replay,String server) {
  synchronized(recent){recent.add(0,new Run(UUID.randomUUID().toString(),status,time,jumps,axis,System.currentTimeMillis(),details,replay,server));
  if(recent.size()>10) recent.remove(10);}
 }
 public List<Run> recent(){synchronized(recent){return Collections.unmodifiableList(new ArrayList<>(recent));}}
 public List<Run> saved() throws IOException {
  long revision=metadataRevision.get();List<Run> runs=new ArrayList<>();
  if(!Files.isDirectory(folder)){cachedSaved=Collections.emptyList();cachedRevision=revision;catalogVersion++;return runs;}
  try(DirectoryStream<Path> files=Files.newDirectoryStream(folder,"*.properties")) {
   for(Path file:files) {
    try{if(Files.size(file)>1024*1024) continue;}catch(NoSuchFileException removed){continue;}
    try(InputStream in=Files.newInputStream(file)) {
     Properties p=new Properties();p.load(in);runs.add(parse(p,null));
    } catch(NoSuchFileException removed){continue;}catch(IllegalArgumentException | NullPointerException ex) {System.err.println("[BridgeTap] Invalid saved run: "+file);}
   }
  }
  runs.sort((a,b)->Long.compare(b.created,a.created));if(metadataRevision.get()==revision){cachedSaved=Collections.unmodifiableList(new ArrayList<>(runs));cachedRevision=revision;catalogVersion++;}return runs;
 }
 public void save(Run run) throws IOException {
  save(run,true);
 }
 public void saveAutomatic(Run run)throws IOException{save(run,false);}
 private synchronized void save(Run run,boolean protect)throws IOException{
  boolean previous=run.protectedSave;try{writeSaved(run,protect);metadataChanged();}catch(IOException|RuntimeException ex){run.protectedSave=previous;throw ex;}
 }
 private void writeSaved(Run run,boolean protect)throws IOException{
  UUID.fromString(run.id);
  Files.createDirectories(folder);Path dest=folder.resolve(run.id+".properties");
  Path archived=archivedLocations.get(run.id);if(archived!=null&&!Files.exists(dest)){Path oldMeta=archived.resolve(run.id+".properties"),oldReplay=archived.resolve(run.id+".btr"),activeReplay=folder.resolve(run.id+".btr");if(Files.exists(oldMeta)){boolean moved=false;try{if(Files.isRegularFile(oldReplay)){Files.move(oldReplay,activeReplay);moved=true;}Files.move(oldMeta,dest);archivedLocations.remove(run.id);}catch(IOException ex){if(moved)Files.move(activeReplay,oldReplay);throw ex;}}}

  if(Files.exists(dest)){if(protect&&!run.protectedSave){Properties p=new Properties();try(InputStream in=Files.newInputStream(dest)){p.load(in);}p.setProperty("protected","true");Path temp=Files.createTempFile(folder,"protect-",".tmp");try{try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"BridgeTap protected replay");}Files.move(temp,dest,StandardCopyOption.REPLACE_EXISTING);run.protectedSave=true;}finally{Files.deleteIfExists(temp);}}return;}
  run.protectedSave=protect;
  Properties p=metadata(run);
  Path replayFile=folder.resolve(run.id+".btr");
  if(run.replay!=null){
   Files.deleteIfExists(replayFile);
   run.replay.write(replayFile);
  }
  try(OutputStream out=Files.newOutputStream(dest,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){p.store(out,"BridgeTap run summary and replay");}
 }
 public static String validName(String name){
  String value=name.trim();if(value.length()>64)throw new IllegalArgumentException("Name: maximum 64 characters");
  for(int i=0;i<value.length();i++)if(value.charAt(i)<32||value.charAt(i)==127||value.charAt(i)=='\u00a7')throw new IllegalArgumentException("Name contains control codes");
  return value;
 }
 private static String validServer(String server){if(server==null||server.length()>255)throw new IllegalArgumentException("Invalid server address");for(int i=0;i<server.length();i++)if(server.charAt(i)<32||server.charAt(i)==127||server.charAt(i)=='\u00a7')throw new IllegalArgumentException("Invalid server address");return server.trim();}
 private static Properties metadata(Run run){
  Properties p=new Properties();p.setProperty("id",run.id);p.setProperty("status",run.status);p.setProperty("time",String.valueOf(run.time));p.setProperty("jumps",String.valueOf(run.jumps));p.setProperty("axis",run.axis);p.setProperty("created",String.valueOf(run.created));p.setProperty("count",String.valueOf(run.details.size()));p.setProperty("name",run.name);
  if(run.modeReady()){p.setProperty("mode.version","3");p.setProperty("mode",run.mode().name());p.setProperty("mode.measured",String.valueOf(run.modeMeasurement.available));if(run.modeMeasurement.available){p.setProperty("mode.gap",String.valueOf(run.modeMeasurement.gap));p.setProperty("mode.rise",String.valueOf(run.modeMeasurement.rise));}}
  p.setProperty("server",run.server);p.setProperty("protected",String.valueOf(run.protectedSave));p.setProperty("imported",String.valueOf(run.imported));for(int i=0;i<run.details.size();i++)p.setProperty("line."+i,run.details.get(i));return p;
 }
 private static Run parse(Properties p,ReplayData replay){
  String id=UUID.fromString(p.getProperty("id")).toString(),status=p.getProperty("status"),axis=p.getProperty("axis");
  double time=Double.parseDouble(p.getProperty("time"));int jumps=Integer.parseInt(p.getProperty("jumps")),count=Integer.parseInt(p.getProperty("count","0"));long created=Long.parseLong(p.getProperty("created"));
  if((!"SUCCESS".equals(status)&&!"FAILED".equals(status))||(!"X".equals(axis)&&!"Z".equals(axis))||!Double.isFinite(time)||time<0||jumps<0||jumps>1000000||created<0||count<0||count>2000)throw new IllegalArgumentException("Invalid replay metadata");
  List<String> details=new ArrayList<>();for(int i=0;i<count;i++)details.add(p.getProperty("line."+i,"").replace("No Gay","No Gain"));
  Run run=new Run(id,status,time,jumps,axis,created,details,replay,p.getProperty("server",""));run.name=validName(p.getProperty("name",""));run.protectedSave=!p.getProperty("protected","true").equals("false");run.imported=Boolean.parseBoolean(p.getProperty("imported","false"));if((run.modeMeasurement==null||run.mode()==ServerMode.Mode.UNKNOWN)&&("3".equals(p.getProperty("mode.version"))||"2".equals(p.getProperty("mode.version"))&&Boolean.parseBoolean(p.getProperty("mode.measured","false")))){try{run.modeMeasurement=Boolean.parseBoolean(p.getProperty("mode.measured","false"))?new ServerMode.Measurement(true,Double.parseDouble(p.getProperty("mode.gap")),Double.parseDouble(p.getProperty("mode.rise"))):ServerMode.UNAVAILABLE;}catch(IllegalArgumentException invalid){run.modeMeasurement=null;}}return run;
 }
 public synchronized void rename(Run run,String name)throws IOException{
  String value=validName(name);save(run);Properties p=metadata(run);p.setProperty("name",value);
  Path temp=Files.createTempFile(folder,"rename-",".tmp");
  try{
   try(OutputStream out=Files.newOutputStream(temp)){p.store(out,"BridgeTap named replay");}
   try{Files.move(temp,folder.resolve(run.id+".properties"),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(AtomicMoveNotSupportedException ex){Files.move(temp,folder.resolve(run.id+".properties"),StandardCopyOption.REPLACE_EXISTING);}
   run.name=value;synchronized(recent){for(Run recentRun:recent)if(recentRun.id.equals(run.id))recentRun.name=value;}metadataChanged();
  }finally{Files.deleteIfExists(temp);}
 }
 public Path exportsFolder(){return folder.getParent().resolve("exports");}
 public Path importsFolder(){return folder.getParent().resolve("imports");}
 public Path exportReplay(Run run)throws IOException{
  ReplayData data=replay(run);if(data==null)throw new IOException("This summary has no replay");
  Files.createDirectories(exportsFolder());String base=run.displayName().replaceAll("[^a-zA-Z0-9_-]","_");if(base.length()>32)base=base.substring(0,32);
  Path file=exportsFolder().resolve(base+"-"+run.id+".btreplay");int suffix=2;
  while(Files.exists(file))file=exportsFolder().resolve(base+"-"+run.id+"-"+(suffix++)+".btreplay");
  ReplayArchive.write(file,metadata(run),data);return file;
 }
 public Run importReplay(Path file)throws IOException{
  ReplayArchive.Content content=ReplayArchive.read(file);Run imported;
  try{imported=parse(content.metadata,content.replay);}catch(IllegalArgumentException|NullPointerException ex){throw new IOException("Invalid replay metadata",ex);}
  final String id=imported.id;
  if(Files.exists(folder.resolve(id+".properties"))||recent().stream().anyMatch(r->r.id.equals(id))){
   Run copy=new Run(UUID.randomUUID().toString(),imported.status,imported.time,imported.jumps,imported.axis,imported.created,imported.details,imported.replay,imported.server);copy.name=imported.name;copy.observeMeasurement(imported.modeMeasurement);imported=copy;
  }
  imported.imported=true;save(imported);synchronized(recent){recent.add(0,imported);if(recent.size()>10)recent.remove(10);}return imported;
 }
 public ReplayData replay(Run run)throws IOException {
  if(run.replay!=null){run.observeMode(run.replay);return run.replay;}
  UUID.fromString(run.id);
  Path file=folder.resolve(run.id+".btr");
  ReplayData data=Files.exists(file)?ReplayData.read(file):null;run.observeMode(data);return data;
 }
 public int deleteUnprotectedFailuresBefore(long cutoff)throws IOException{
  int deleted=0;Set<String> removed=new HashSet<>(),favorites=favoriteIds();
  for(Run run:saved())if(run.status.equals("FAILED")&&run.created<cutoff&&!run.isProtected()&&!run.isImported()&&!favorites.contains(run.id)){synchronized(this){
   Path metadata=folder.resolve(run.id+".properties"),recording=folder.resolve(run.id+".btr");if(!Files.isRegularFile(metadata))continue;Properties latest=new Properties();try(InputStream in=Files.newInputStream(metadata)){latest.load(in);}if(!"false".equals(latest.getProperty("protected","true"))||Boolean.parseBoolean(latest.getProperty("imported","false"))||isFavorite(run))continue;Files.deleteIfExists(recording);Files.deleteIfExists(metadata);removed.add(run.id);deleted++;
  }}synchronized(recent){recent.removeIf(run->removed.contains(run.id));}if(deleted>0)metadataChanged();return deleted;
 }
 public Path archiveFolder(){return folder.toAbsolutePath().normalize().getParent().resolve("automatic-replay-archive");}
 public int archiveAutomatic(int keep)throws IOException{
  if(keep<0)throw new IllegalArgumentException("Negative retention limit");int automatic=0,moved=0;Set<String> removed=new HashSet<>();Path root=folder.toAbsolutePath().normalize(),archive=archiveFolder();if(!archive.startsWith(root.getParent())||archive.equals(root))throw new IOException("Invalid replay archive path");Path batch=null;
  for(Run run:savedSnapshot()){
   if(run.isProtected()||run.isImported()||isFavorite(run))continue;if(automatic++<keep)continue;
   synchronized(this){Path metadata=root.resolve(run.id+".properties").normalize(),recording=root.resolve(run.id+".btr").normalize();if(!metadata.getParent().equals(root)||Files.isSymbolicLink(metadata)||Files.isSymbolicLink(recording)||!Files.isRegularFile(metadata))continue;Properties current=new Properties();try(InputStream in=Files.newInputStream(metadata)){current.load(in);}if(!"false".equals(current.getProperty("protected","true"))||Boolean.parseBoolean(current.getProperty("imported","false"))||isFavorite(run))continue;
    if(batch==null){Files.createDirectories(archive);batch=Files.createTempDirectory(archive,"batch-");}Path targetMeta=batch.resolve(metadata.getFileName()),targetReplay=batch.resolve(recording.getFileName());boolean recordingMoved=false;try{if(Files.isRegularFile(recording)){Files.move(recording,targetReplay);recordingMoved=true;}Files.move(metadata,targetMeta);}catch(IOException ex){if(recordingMoved)Files.move(targetReplay,recording);throw ex;}archivedLocations.put(run.id,batch);removed.add(run.id);moved++;
   }
  }if(moved>0){synchronized(recent){recent.removeIf(run->removed.contains(run.id));}metadataChanged();}return moved;
 }
 public boolean hasReplay(Run run){
  try{UUID.fromString(run.id);return run.replay!=null||Files.isRegularFile(folder.resolve(run.id+".btr"));}
  catch(IllegalArgumentException ex){return false;}
 }
 private synchronized Set<String> favoriteIds() throws IOException {
  if(favorites==null){
   Set<String> loaded=new HashSet<>();Path file=folder.resolve("favorites.prefs");
   if(Files.exists(file))for(String id:Files.readAllLines(file,java.nio.charset.StandardCharsets.UTF_8)){
    try{UUID.fromString(id);loaded.add(id);}catch(IllegalArgumentException ignored){}
   }
   favorites=loaded;
  }
  return favorites;
 }
 public boolean isFavorite(Run run)throws IOException{return favoriteIds().contains(run.id);}
 public List<Run> favorites()throws IOException {
  List<Run> result=new ArrayList<>();for(Run run:saved())if(isFavorite(run))result.add(run);return result;
 }
 public synchronized boolean toggleFavorite(Run run)throws IOException {
  Set<String> updated=new HashSet<>(favoriteIds());boolean added=updated.add(run.id);if(!added)updated.remove(run.id);
  if(added)save(run);Files.createDirectories(folder);
  Path temp=Files.createTempFile(folder,"favorites-",".tmp");
  try{
   Files.write(temp,updated,java.nio.charset.StandardCharsets.UTF_8);
   try{Files.move(temp,folder.resolve("favorites.prefs"),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(AtomicMoveNotSupportedException ex){Files.move(temp,folder.resolve("favorites.prefs"),StandardCopyOption.REPLACE_EXISTING);}
   favorites=updated;metadataChanged();return added;
  }finally{Files.deleteIfExists(temp);}
 }
}
