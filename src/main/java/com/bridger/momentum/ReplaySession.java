package com.bridger.momentum;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
import com.bridger.momentum.mixin.ItemInHandAccessor;
import com.bridger.momentum.mixin.ReplayContextAccessor;
import com.bridger.momentum.mixin.ReplayControllerAccessor;
import com.bridger.momentum.mixin.ReplayRendererAccessor;

/** The live client stays untouched between render calls; the replay has its own world and renderer. */
public final class ReplaySession {
 private static ReplaySession current;
 private static ReplaySession ticking;
 private static C_24384787 reusableRenderer;
 private final ReplayData data;
 private final RunHistory.Run run;
 private final ReplayTimeline timeline;
 private final ReplayFreeCamera movement=new ReplayFreeCamera();
 private final C_34399650 source,world;
 private final C_57778754 player,camera;
 private final ReplayGhost ghost;
 private final C_24384787 renderer;
 private final C_16190149 particles;
 private final C_34267874 controls;
 private final Map<Long,ReplayData.Block> applied=new HashMap<>();
 private volatile Map<Long,Integer> placedIds=Collections.emptyMap();
 private final Set<Long> chunks=new HashSet<>();
 private final Set<Long> coveredChunks=new HashSet<>();
 private int sceneTick=-1,sourceSlot;
 private boolean rendering;
 private volatile boolean rendererInvalid;
 private boolean followPov;
 private boolean previousSneak;
 private ReplayControls.Center centerState;
 private long lastFrame=System.nanoTime(),lastAction;
 private long lastActionSequence;
 private C_24384787 previousRenderer;
 private C_16190149 previousParticles;
 private C_34267874 previousControls;
 private C_42232651 previousCamera;
 private C_42232651 previousPointedEntity;
 private C_24654288 previousHit;
 private int previousPerspective;
 private boolean previousHitboxes;
 private C_88708284 previousHand;
 private float previousHandHeight,previousLastHandHeight;
 private boolean guiTick;
 private boolean movementCaptured,jumping,sneaking;
 private double forward,right;
 private final C_97866173 gui;
 private C_97866173 previousGui;

 private ReplaySession(Minecraft mc,RunHistory.Run run,ReplayData data){
  this.data=data;this.run=run;run.observeMode(data);timeline=new ReplayTimeline(data,run.time);source=mc.f_26407825;player=mc.f_28396371;
  world=new C_34399650(null,new C_06007608(0,C_06007608.C_36868821.f_11202516,false,false,C_61837504.f_74160535),data.dimension,C_57526564.f_55214254,mc.f_40149437);
  world.m_44107872(data.dayTime);world.m_04000603(0);world.m_29822868(0);
  camera=new ReplayCamera(mc,world,mc.m_55181026());
  camera.f_95719371=true;camera.f_77844715.f_26706858=true;camera.f_77844715.f_77637162=true;camera.f_77844715.f_86452611=true;camera.f_77844715.f_63336874=true;
  camera.f_92990329=new C_54206210();
  controls=new C_34267874(mc,mc.m_55181026());
  ((ReplayControllerAccessor)controls).bridgetap$setReplayMode(C_06007608.C_36868821.f_11202516);
  ghost=new ReplayGhost(world,data.identity);ghost.apply(data.frames.get(0));
  syncScene();world.m_34714727(-198954,ghost);
  resetCamera();
  Arrays.fill(camera.f_29439708.f_35249023,null);
  for(int slot:ReplayControls.slots())camera.f_29439708.f_35249023[slot]=tool(ReplayControls.item(slot,timeline),ReplayControls.label(slot,timeline));
  centerState=ReplayControls.center(timeline);
  camera.f_29439708.f_13682807=1;
  // A renderer owns worker threads and large native buffers. Reuse the worker
  // pool, and release world-specific buffers when leaving each replay.
  if(reusableRenderer==null)reusableRenderer=new C_24384787(mc);
  renderer=reusableRenderer;renderer.m_96356728(world);
  mc.m_64046137().m_77304627(source);
  particles=new C_16190149(world,mc.m_65924382());gui=new C_97866173(mc);
 }
 private static C_88708284 tool(int id,String name){return new C_88708284(C_98888256.m_06629599(id)).m_12689347(name);}
 public static Object token(){return current;}
 public static String recordingId(){return current==null?"":current.run.id;}
 public static int lastTick(){return current==null?0:current.timeline.lastTick();}
 private int videoCameraMode=-1;
 public static final class VideoState {final C_88708284[] hotbar;final double tick;final boolean playing,pov,hideGui,bobbing;final double x,y,z;final float yaw,pitch;VideoState(ReplaySession s){hotbar=java.util.Arrays.copyOf(s.camera.f_29439708.f_35249023,9);hideGui=Minecraft.m_20213497().f_58088711.f_06955029;bobbing=Minecraft.m_20213497().f_58088711.f_16743685;tick=s.timeline.tick();playing=s.timeline.playing();pov=s.followPov;x=s.camera.f_78826675;y=s.camera.f_31030949;z=s.camera.f_97691941;yaw=s.camera.f_80130373;pitch=s.camera.f_03549461;}}
 public static VideoState beginVideo(boolean pov){return beginVideo(pov?0:2,false);}
 public static VideoState beginVideo(int camera,boolean cleanHud){ReplaySession s=current;VideoState saved=new VideoState(s);java.util.Arrays.fill(s.camera.f_29439708.f_35249023,0,9,null);s.timeline.pause();s.timeline.seek(0);s.videoCameraMode=camera;Minecraft mc=Minecraft.m_20213497();if(cleanHud)mc.f_58088711.f_06955029=true;mc.f_58088711.f_16743685=false;if(camera==0)s.setPov(true);else if(camera==1){s.followPov=false;s.clearPose();s.positionCamera(1);}else s.videoOverview();return saved;}
 public static ReplayData.Frame videoFrame(){return current==null?null:current.timeline.pose();}
 public static double videoSeconds(){return current==null?0:current.timeline.seconds();}
 public static void videoTick(double tick){if(current!=null){current.timeline.pause();current.timeline.seek(tick);}}
 public static void inspectTick(double tick){videoTick(tick);}
 public static void endVideo(VideoState saved){ReplaySession s=current;if(s==null)return;System.arraycopy(saved.hotbar,0,s.camera.f_29439708.f_35249023,0,9);s.videoCameraMode=-1;Minecraft mc=Minecraft.m_20213497();mc.f_58088711.f_06955029=saved.hideGui;mc.f_58088711.f_16743685=saved.bobbing;s.timeline.pause();s.timeline.seek(saved.tick);if(saved.playing)s.timeline.toggle();s.movement.reset(saved.x,saved.y,saved.z);s.camera.m_12877971(saved.x,saved.y,saved.z,saved.yaw,saved.pitch);s.setPov(saved.pov);s.lastFrame=System.nanoTime();}
 public static boolean active(){return current!=null;}
 public static Integer placedId(long key){return current==null||current.placedIds==null?null:current.placedIds.get(key);}
 public static ReplayData recording(){return current==null?null:current.data;}
 public static String recordingName(){return current==null?"":current.run.displayName();}
 public static C_57778754 viewer(){return current==null?Minecraft.m_20213497().f_28396371:current.camera;}
 public static C_34399650 sourceWorld(){return current==null?Minecraft.m_20213497().f_26407825:current.source;}
 public static C_34399650 viewWorld(){return current==null?Minecraft.m_20213497().f_26407825:current.world;}
 public static void freeCopyCamera(){if(current!=null&&current.followPov)current.setPov(false);}
 public static void rendererInvalidated(){if(current!=null&&!current.rendering)current.rendererInvalid=true;}
 public static void rebuildSceneryTerrain(){Minecraft mc=Minecraft.m_20213497();if(mc!=null&&mc.f_48619021!=null)mc.f_48619021.m_12518137();rendererInvalidated();}
 public static void invalidateScenery(int x,int y,int z,int maxX,int maxY,int maxZ){Minecraft mc=Minecraft.m_20213497();if(mc==null)return;C_34399650 live=sourceWorld();if(live!=null)live.m_76770021(x,y,z,maxX,maxY,maxZ);if(current!=null&&current.world!=null)current.world.m_76770021(x,y,z,maxX,maxY,maxZ);}
 public static ReplayData.TickStats recordedInputs(){return current==null?null:current.timeline.sample().stats;}
 public static boolean rendering(){return current!=null&&current.rendering;}
 public static boolean povLocked(){return current!=null&&current.followPov;}
 public static ReplayGhost povActor(){return povLocked()?current.ghost:null;}
 public static C_57778754 realPlayer(){return current==null?Minecraft.m_20213497().f_28396371:current.player;}
 public static boolean start(RunHistory.Run run,ReplayData data){
  Minecraft mc=Minecraft.m_20213497();if(mc.f_26407825==null||mc.f_28396371==null)return false;
  stop();current=new ReplaySession(mc,run,data);current.lastActionSequence=BridgeTelemetry.actionSequence();mc.m_52715402(null);status("\u00a7aPlay");return true;
 }
 public static void stop(){
  ReplayVideoExport.cancel();ReplaySession session=current;if(session==null)return;
  endRender();current=null;
  ReplayRendererAccessor resources=(ReplayRendererAccessor)session.renderer;
  try{
   resources.bridgetap$getDispatcher().m_21945757();
   C_61390454 storage=resources.bridgetap$getChunkStorage();
   if(storage!=null)storage.m_60634394();
   resources.bridgetap$setChunkStorage(null);
   resources.bridgetap$getCompiledChunks().clear();resources.bridgetap$getDirtyChunks().clear();
   synchronized(resources.bridgetap$getBlockEntities()){resources.bridgetap$getBlockEntities().clear();}
  }finally{
   session.renderer.m_96356728(null);
   Minecraft mc=Minecraft.m_20213497();mc.m_64046137().m_77304627(mc.f_26407825);
  }
 }
 public static void exit(){stop();Minecraft.m_20213497().m_52715402(ReplayFolderScreen.home());}
 public static void tickHead(){
  ReplaySession s=current;ticking=s;if(s==null)return;Minecraft mc=Minecraft.m_20213497();
  if(mc.f_26407825!=s.source||mc.f_28396371!=s.player){stop();return;}
  s.sourceSlot=s.player.f_29439708.f_13682807;
  s.movementCaptured=false;s.forward=s.right=0;s.jumping=s.sneaking=false;
 }
 public static void tickTail(){
  ReplaySession s=ticking;ticking=null;if(s==null)return;
  s.player.f_29439708.f_13682807=s.sourceSlot;
  if(s==current){
   Minecraft mc=Minecraft.m_20213497();C_76792751 keys=mc.f_58088711;
   if(mc.f_70816363==null&&mc.f_92501186&&!ReplayVideoExport.active()){
    s.selectHotbar(keys);
    boolean sneak=down(keys.f_89071996);
    if(s.followPov&&sneak&&!s.previousSneak){s.resetCamera();status("\u00a7bFree camera");}
    s.previousSneak=sneak;
    double forward=s.movementCaptured?s.forward:axis(keys.f_15607202,keys.f_46759128);
    double right=s.movementCaptured?s.right:axis(keys.f_25268871,keys.f_20678369);
    double up=s.movementCaptured?(s.jumping?1:0)-(s.sneaking?1:0):axis(keys.f_94470987,keys.f_89071996);
    if(!s.followPov)s.movement.step(.05,s.camera.f_80130373,forward,right,up,down(keys.f_50204069));
   }else s.movement.step(.05,s.camera.f_80130373,0,0,0,false);
   s.guiTick=true;
  }
 }
 public static boolean captureMovement(C_54206210 input){
  ReplaySession s=current;if(s==null||input!=s.player.f_92990329)return false;
  s.forward=Math.signum(input.f_81168741);s.right=-Math.signum(input.f_54482307);
  s.jumping=input.f_92317429;s.sneaking=input.f_28813338;s.movementCaptured=true;return true;
 }
 public static void beginRender(float partial){
  ReplaySession s=current;if(s==null||s.rendering)return;Minecraft mc=Minecraft.m_20213497();
  if(mc.f_26407825!=s.source||mc.f_28396371!=s.player){stop();return;}
  long now=System.nanoTime();double dt=Math.min(.1,(now-s.lastFrame)/1e9);s.lastFrame=now;
  if(mc.f_70816363==null&&mc.f_92501186&&!ReplayVideoExport.active())s.timeline.advance(dt);
  if(ReplayVideoExport.cleanHud())mc.f_58088711.f_06955029=true;
  if(!ReplayVideoExport.active())s.updateCenter();
  s.positionCamera(partial);
  s.syncScene();ReplayData.Frame pose=s.timeline.pose();s.ghost.apply(pose);
  s.ensureChunk((int)Math.floor(pose.x)>>4,(int)Math.floor(pose.z)>>4);
  // Update native chunk membership without running AI, movement or animation ticks.
  s.world.m_15238577(s.ghost,false);
  s.previousRenderer=mc.f_48619021;s.previousParticles=mc.f_49464910;s.previousControls=mc.f_17639899;
  s.previousCamera=mc.m_67314413();s.previousPerspective=mc.f_58088711.f_33865429;s.previousHitboxes=mc.m_64046137().m_06540705();
  s.previousGui=mc.f_28235293;
  s.previousHit=mc.f_81460813;s.previousPointedEntity=mc.f_30642501;
  s.rendering=true;
  mc.f_26407825=s.world;mc.f_28396371=s.camera;mc.f_48619021=s.renderer;mc.f_49464910=s.particles;mc.f_17639899=s.controls;mc.f_28235293=s.gui;
  ((ReplayContextAccessor)mc).bridgetap$setRenderCamera(s.camera);mc.f_58088711.f_33865429=0;mc.m_64046137().m_72442377(true);
  mc.m_64046137().m_77304627(s.world);
  if(s.rendererInvalid){s.rendererInvalid=false;s.renderer.m_96356728(null);s.renderer.m_96356728(s.world);}
  ItemInHandAccessor hand=(ItemInHandAccessor)mc.m_52934152();
  s.previousHand=hand.bridgetap$getItem();s.previousHandHeight=hand.bridgetap$getHeight();s.previousLastHandHeight=hand.bridgetap$getLastHeight();
  hand.bridgetap$setItem(s.followPov?s.ghost.f_29439708.m_27760697():s.camera.f_29439708.m_27760697());hand.bridgetap$setHeight(1);hand.bridgetap$setLastHeight(1);
  if(s.guiTick){s.gui.m_74507790();s.guiTick=false;}
 }
 public static void endRender(){
  ReplaySession s=current;if(s==null||!s.rendering)return;Minecraft mc=Minecraft.m_20213497();
  mc.f_26407825=s.source;mc.f_28396371=s.player;mc.f_48619021=s.previousRenderer;mc.f_49464910=s.previousParticles;mc.f_17639899=s.previousControls;mc.f_28235293=s.previousGui;
  mc.f_81460813=s.previousHit;mc.f_30642501=s.previousPointedEntity;mc.m_64046137().m_77304627(s.source);
  ((ReplayContextAccessor)mc).bridgetap$setRenderCamera(s.previousCamera);mc.f_58088711.f_33865429=s.previousPerspective;mc.m_64046137().m_72442377(s.previousHitboxes);s.rendering=false;
  ItemInHandAccessor hand=(ItemInHandAccessor)mc.m_52934152();hand.bridgetap$setItem(s.previousHand);hand.bridgetap$setHeight(s.previousHandHeight);hand.bridgetap$setLastHeight(s.previousLastHandHeight);
 }
 private static boolean down(C_23708450 key){return key.m_70603120();}
 private static double axis(C_23708450 positive,C_23708450 negative){return (down(positive)?1:0)-(down(negative)?1:0);}
 private void selectHotbar(C_76792751 keys){
  for(int i:ReplayControls.slots())if(i<keys.f_81621289.length&&down(keys.f_81621289[i]))camera.f_29439708.f_13682807=i;
 }
 private void positionCamera(float partial){
  if(videoCameraMode==1){double[] view=ReplayVideoCamera.follow(timeline.pose(),data.frames.get(0),data.frames.get(data.frames.size()-1));movement.reset(view[0],view[1],view[2]);camera.m_12877971(view[0],view[1],view[2],(float)view[3],(float)view[4]);}
  if(followPov){
   ReplayData.Frame f=timeline.pose();
   camera.m_12877971(f.x,f.y,f.z,f.yaw,f.pitch);camera.f_74900774=f.yaw;camera.f_45219594=f.pitch;
   camera.f_09368719=camera.f_59999765=f.x;camera.f_93723721=camera.f_90348373=f.y;camera.f_84066837=camera.f_88346412=f.z;
   camera.f_92990329.f_28813338=f.sneaking;camera.m_22136189(f.sneaking);camera.f_47825615=f.onGround;
   camera.f_67244135=camera.f_19026405=f.swing;
   if(f.stats!=null){camera.f_24826402=f.stats.vx;camera.f_35148123=f.stats.vy;camera.f_21201587=f.stats.vz;}
   camera.f_67908378=camera.f_11999391=f.yaw;camera.f_96860657=camera.f_67640190=f.pitch;camera.f_95719371=true;
   return;
  }
  double x=movement.x(partial),y=movement.y(partial),z=movement.z(partial);
  camera.m_70517412(x,y,z);
  camera.f_95719371=true;
  camera.f_09368719=camera.f_59999765=x;camera.f_93723721=camera.f_90348373=y;camera.f_84066837=camera.f_88346412=z;
  camera.f_67908378=camera.f_11999391=camera.f_80130373;
  camera.f_96860657=camera.f_67640190=camera.f_03549461;
 }
 public static boolean scroll(C_87542771 inventory,int delta){
  ReplaySession s=current;if(s==null||inventory!=s.player.f_29439708)return false;
  if(delta==0)return true;
  s.camera.f_29439708.f_13682807=ReplayControls.scroll(s.camera.f_29439708.f_13682807,delta);return true;
 }
 public static void action(boolean attack){
  if(ReplayVideoExport.active())return;
  ReplaySession s=current;if(s==null)return;
  s.selectHotbar(Minecraft.m_20213497().f_58088711);
  int slot=s.camera.f_29439708.f_13682807;
  long sequence=BridgeTelemetry.actionSequence();boolean fresh=sequence!=s.lastActionSequence;
  long now=System.nanoTime();if(!ReplayControls.acceptAction(slot,fresh,now,s.lastAction))return;s.lastAction=now;s.lastActionSequence=sequence;
  if(slot==ReplayControls.EXIT){exit();return;}
  if(attack&&fresh&&!s.followPov&&s.aimsAtGhost()){s.setPov(true);status("\u00a7bPOV - Sneak to return to free camera");return;}
  if(slot==ReplayControls.BACK||slot==ReplayControls.FORWARD){int step=s.timeline.skip(slot==ReplayControls.BACK?-1:1);status(String.format(Locale.US,"\u00a7e%s %+.2f seconds",slot==ReplayControls.BACK?"Backward":"Forward",(slot==ReplayControls.BACK?-step:step)/20.0));}
  if(slot==ReplayControls.CENTER){
   if(s.timeline.finished()){s.timeline.restart();status("\u00a7aReplay from beginning");}
   else{s.timeline.toggle();status(s.timeline.playing()?"\u00a7aPlay":"\u00a7ePause");}
  }
  if(slot==ReplayControls.SPEED){
   s.timeline.cycleSpeed();
   String label=ReplayControls.label(ReplayControls.SPEED,s.timeline);
   s.camera.f_29439708.f_35249023[ReplayControls.SPEED]=tool(ReplayControls.item(ReplayControls.SPEED,s.timeline),label);
   status(label);
  }
  s.updateCenter();
 }
 private void updateCenter(){
  ReplayControls.Center state=ReplayControls.center(timeline);
  if(state!=centerState){camera.f_29439708.f_35249023[ReplayControls.CENTER]=tool(ReplayControls.item(ReplayControls.CENTER,timeline),ReplayControls.label(ReplayControls.CENTER,timeline));centerState=state;}
 }
 private boolean aimsAtGhost(){
  double x=camera.f_78826675,y=camera.f_31030949+camera.m_75246573(),z=camera.f_97691941;
  double distance=ReplayTargeting.hit(timeline.pose(),x,y,z,camera.f_80130373,camera.f_03549461,64);
  if(!Double.isFinite(distance))return false;if(distance<.02)return true;
  double[] dir=ReplayTargeting.direction(camera.f_80130373,camera.f_03549461);
  C_94584382 origin=new C_94584382(x,y,z);
  C_24654288 obstacle=world.m_82559274(origin,new C_94584382(x+dir[0]*(distance-.02),y+dir[1]*(distance-.02),z+dir[2]*(distance-.02)));
  return obstacle==null||(obstacle.f_94083482!=null&&origin.m_74339345(obstacle.f_94083482)>=distance-.03);
 }
 private void videoOverview(){followPov=false;clearPose();double ax=Double.POSITIVE_INFINITY,az=ax,bx=Double.NEGATIVE_INFINITY,bz=bx,top=0;for(ReplayData.Frame f:data.frames){ax=Math.min(ax,f.x);az=Math.min(az,f.z);bx=Math.max(bx,f.x);bz=Math.max(bz,f.z);top=Math.max(top,f.y);}double cx=(ax+bx)/2,cz=(az+bz)/2,d=Math.max(8,Math.max(bx-ax,bz-az)*.9),px=cx+d,py=top+d*.8,pz=cz+d;float yaw=(float)Math.toDegrees(Math.atan2(-(cx-px),cz-pz)),pitch=(float)Math.toDegrees(Math.atan2(py-top,Math.hypot(cx-px,cz-pz)));camera.m_12877971(px,py,pz,yaw,pitch);movement.reset(px,py,pz);positionCamera(1);}
 private void resetCamera(){
  followPov=false;clearPose();
  ReplayData.Frame f=timeline.pose();double a=Math.toRadians(f.yaw+100),x=f.x+Math.cos(a)*7,z=f.z+Math.sin(a)*7;
  float yaw=(float)Math.toDegrees(Math.atan2(-(f.x-x),f.z-z));camera.m_12877971(x,f.y+4,z,yaw,22);
  camera.f_74900774=yaw;camera.f_45219594=22;
  movement.reset(x,f.y+4,z);positionCamera(1);
 }
 private void clearPose(){camera.f_92990329.f_28813338=false;camera.m_22136189(false);camera.f_67244135=camera.f_19026405=0;camera.f_24826402=camera.f_35148123=camera.f_21201587=0;}
 private void setPov(boolean follow){
  if(!follow){movement.reset(camera.f_78826675,camera.f_31030949,camera.f_97691941);clearPose();}
  followPov=follow;positionCamera(1);
  previousSneak=down(Minecraft.m_20213497().f_58088711.f_89071996);
 }
 public static boolean hideGhostModel(C_23527099 actor){
  ReplaySession s=current;
  return s!=null&&s.rendering&&actor==s.ghost&&ReplayInspection.eyeInside(s.timeline.pose(),s.camera.f_78826675,s.camera.f_31030949+s.camera.m_75246573(),s.camera.f_97691941);
 }
 private static void status(String message){if(current!=null)current.gui.m_62518566(message,false);}
 private void syncScene(){
  int tick=(int)timeline.tick();if(sceneTick==tick)return;
  Map<Long,ReplayData.Block> next=timeline.blocks();
  Map<Long,Integer> placed=new HashMap<>();for(ReplayData.Block b:next.values())if(b.placed&&b.id!=0)placed.put(b.key(),b.id);placedIds=Collections.unmodifiableMap(placed);
  for(Map.Entry<Long,ReplayData.Block> e:applied.entrySet())if(!next.containsKey(e.getKey()))setBlock(e.getValue(),true);
  for(Map.Entry<Long,ReplayData.Block> e:next.entrySet())if(!e.getValue().sameState(applied.get(e.getKey())))setBlock(e.getValue(),false);
  applied.clear();applied.putAll(next);sceneTick=tick;
 }
 private void setBlock(ReplayData.Block b,boolean clear){
  ensureChunk(b.x>>4,b.z>>4);
  C_81592558 block=C_81592558.m_64320102(clear?0:b.id);
  if(block!=null&&world.m_38188749(new C_32594356(b.x,b.y,b.z),block.m_28027445(clear?0:b.metadata),2)){
   // Synthetic client chunks never receive the server's populated/light flags.
   // setBlockState's normal flags=2 notification skips those chunks. Notify the
   // replay world's renderer explicitly, including adjacent faces at chunk edges.
   world.m_76770021(b.x,b.y,b.z,b.x,b.y,b.z);
  }
 }
 private void ensureChunk(int x,int z){ReplayChunkCoverage.ensure(chunks,coveredChunks,x,z,(cx,cz)->world.m_66086785(cx,cz,true));}
 public static void renderHud(){
  if(!ReplayVideoExport.overlays())return;
  ReplaySession s=current;if(s==null||!s.rendering)return;
  if(BridgeConfig.showReplayInputs){
   Minecraft mc=Minecraft.m_20213497();C_43718703 window=new C_43718703(mc);
   int height=ReplayInputOverlay.height(s.timeline.sample().stats);OneClientIntegration.recordSize(2,76,height);
   float scale=(float)Math.min(BridgeConfig.inputHudScale,Math.min((window.m_39119333()-8f)/76,(window.m_16103615()-8f)/height));
   int x=OverlayPlacement.clamp(BridgeConfig.inputHudX,(int)Math.ceil(76*scale),window.m_39119333());
   int y=OverlayPlacement.clamp(BridgeConfig.inputHudY,(int)Math.ceil(height*scale),window.m_16103615());
   ReplayInputOverlay.paint(new ScaledUiCanvas(new MinecraftUiCanvas(mc.f_21497584,2),x,y,scale),0,0,s.timeline.sample().stats);
  }
  if(!BridgeConfig.showReplayInfo)return;
  List<String> lines=new ArrayList<>();
  lines.add(String.format(Locale.US,"\u00a7b%s \u00a7f%.2f / %.2fs \u00a77| Tick %d/%d | %.2fx | %s",s.run.displayName(),s.timeline.seconds(),s.timeline.duration(),(int)s.timeline.tick(),s.timeline.lastTick(),s.timeline.speed(),s.timeline.playing()?"Play":"Paused"));
  ReplayData.Frame actor=s.timeline.sample();
  lines.addAll(BridgeTelemetryHUD.lines(actor.x,actor.y,actor.z,actor.stats,"Ghost"));
  lines.add("\u00a77Server mode: \u00a7f"+s.run.mode().label);
  BridgeGeometry.Result dimensions=s.data.geometry();
  if(dimensions.available)lines.add(String.format(Locale.US,"\u00a77Platform gap: %.2fb | Height: %+.2fb",dimensions.gap,dimensions.rise));
  lines.add("\u00a77Sticks: "+(s.timeline.playing()?"1 second":"1 tick")+" | "+(s.followPov?"POV: Sneak to leave":"Left-click player: POV"));
  BridgeTelemetryHUD.draw(lines);
 }
}
