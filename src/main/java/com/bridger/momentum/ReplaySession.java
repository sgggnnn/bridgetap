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
 private final Set<Long> chunks=new HashSet<>();
 private int sceneTick=-1,sourceSlot;
 private boolean rendering;
 private long lastFrame=System.nanoTime(),lastAction;
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
  this.data=data;this.run=run;timeline=new ReplayTimeline(data);source=mc.f_26407825;player=mc.f_28396371;
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
  camera.f_29439708.f_35249023[0]=tool(280,"\u00a7eBackward -0.05 seconds");
  camera.f_29439708.f_35249023[1]=tool(341,"\u00a7aPause / Play");
  camera.f_29439708.f_35249023[2]=tool(369,"\u00a7eForward +0.05 seconds");
  camera.f_29439708.f_35249023[3]=tool(347,"\u00a7bPlayback speed");
  camera.f_29439708.f_35249023[4]=tool(262,"\u00a7eRestart");
  camera.f_29439708.f_35249023[5]=tool(345,"\u00a7bReturn camera to ghost");
  camera.f_29439708.f_35249023[6]=tool(381,"\u00a7bInspect ghost (pause + exact POV)");
  camera.f_29439708.f_35249023[8]=tool(166,"\u00a7cExit replay");
  camera.f_29439708.f_13682807=1;
  // A renderer owns worker threads and large native buffers. Reuse the worker
  // pool, and release world-specific buffers when leaving each replay.
  if(reusableRenderer==null)reusableRenderer=new C_24384787(mc);
  renderer=reusableRenderer;renderer.m_96356728(world);
  mc.m_64046137().m_77304627(source);
  particles=new C_16190149(world,mc.m_65924382());gui=new C_97866173(mc);
 }
 private static C_88708284 tool(int id,String name){return new C_88708284(C_98888256.m_06629599(id)).m_12689347(name);}
 public static boolean active(){return current!=null;}
 public static boolean rendering(){return current!=null&&current.rendering;}
 public static C_57778754 realPlayer(){return current==null?Minecraft.m_20213497().f_28396371:current.player;}
 public static boolean start(RunHistory.Run run,ReplayData data){
  Minecraft mc=Minecraft.m_20213497();if(mc.f_26407825==null||mc.f_28396371==null)return false;
  stop();current=new ReplaySession(mc,run,data);mc.m_52715402(null);status("\u00a7aPlay");return true;
 }
 public static void stop(){
  ReplaySession session=current;if(session==null)return;
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
 public static void exit(){stop();Minecraft.m_20213497().m_52715402(new BridgeHistoryScreen());}
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
   if(mc.f_70816363==null&&mc.f_92501186){
    s.selectHotbar(keys);
    double forward=s.movementCaptured?s.forward:axis(keys.f_15607202,keys.f_46759128);
    double right=s.movementCaptured?s.right:axis(keys.f_25268871,keys.f_20678369);
    double up=s.movementCaptured?(s.jumping?1:0)-(s.sneaking?1:0):axis(keys.f_94470987,keys.f_89071996);
    s.movement.step(.05,s.camera.f_80130373,forward,right,up,down(keys.f_50204069));
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
  if(mc.f_70816363==null&&mc.f_92501186)s.timeline.advance(dt);
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
  ItemInHandAccessor hand=(ItemInHandAccessor)mc.m_52934152();
  s.previousHand=hand.bridgetap$getItem();s.previousHandHeight=hand.bridgetap$getHeight();s.previousLastHandHeight=hand.bridgetap$getLastHeight();
  hand.bridgetap$setItem(s.camera.f_29439708.m_27760697());hand.bridgetap$setHeight(1);hand.bridgetap$setLastHeight(1);
  if(s.guiTick){s.gui.m_74507790();s.guiTick=false;}
 }
 public static void endRender(){
  ReplaySession s=current;if(s==null||!s.rendering)return;Minecraft mc=Minecraft.m_20213497();
  mc.f_26407825=s.source;mc.f_28396371=s.player;mc.f_48619021=s.previousRenderer;mc.f_49464910=s.previousParticles;mc.f_17639899=s.previousControls;mc.f_28235293=s.previousGui;
  mc.f_81460813=s.previousHit;mc.f_30642501=s.previousPointedEntity;
  ((ReplayContextAccessor)mc).bridgetap$setRenderCamera(s.previousCamera);mc.f_58088711.f_33865429=s.previousPerspective;mc.m_64046137().m_72442377(s.previousHitboxes);s.rendering=false;
  ItemInHandAccessor hand=(ItemInHandAccessor)mc.m_52934152();hand.bridgetap$setItem(s.previousHand);hand.bridgetap$setHeight(s.previousHandHeight);hand.bridgetap$setLastHeight(s.previousLastHandHeight);
 }
 private static boolean down(C_23708450 key){return key.m_70603120();}
 private static double axis(C_23708450 positive,C_23708450 negative){return (down(positive)?1:0)-(down(negative)?1:0);}
 private void selectHotbar(C_76792751 keys){
  for(int i=0;i<Math.min(9,keys.f_81621289.length);i++)if(down(keys.f_81621289[i]))camera.f_29439708.f_13682807=i;
 }
 private void positionCamera(float partial){
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
  s.camera.f_29439708.f_13682807=Math.floorMod(s.camera.f_29439708.f_13682807-(delta>0?1:-1),9);return true;
 }
 public static void action(){
  ReplaySession s=current;if(s==null)return;long now=System.nanoTime();if(now-s.lastAction<120000000)return;s.lastAction=now;
  s.selectHotbar(Minecraft.m_20213497().f_58088711);
  int slot=s.camera.f_29439708.f_13682807;
  if(slot==0||slot==2){s.timeline.seek(s.timeline.tick()+(slot==0?-1:1));status(String.format(Locale.US,"\u00a7e%s %+.2f seconds",slot==0?"Backward":"Forward",slot==0?-.05:.05));}
  if(slot==1){s.timeline.toggle();status(s.timeline.playing()?"\u00a7aPlay":"\u00a7ePause");}
  if(slot==3){s.timeline.cycleSpeed();status("\u00a7bSpeed: "+s.timeline.speed()+"x");}
  if(slot==4){s.timeline.restart();status("\u00a7aRestart");}
  if(slot==5){s.resetCamera();status("\u00a7bCamera reset");}
  if(slot==6){s.inspectGhost();status("\u00a7bGhost POV - paused at recorded tick (free flight)");}
  if(slot==8)exit();
 }
 private void resetCamera(){
  ReplayData.Frame f=timeline.pose();double a=Math.toRadians(f.yaw+100),x=f.x+Math.cos(a)*7,z=f.z+Math.sin(a)*7;
  float yaw=(float)Math.toDegrees(Math.atan2(-(f.x-x),f.z-z));camera.m_12877971(x,f.y+4,z,yaw,22);
  camera.f_74900774=yaw;camera.f_45219594=22;
  movement.reset(x,f.y+4,z);positionCamera(1);
 }
 private void inspectGhost(){
  timeline.pause();timeline.seek(Math.floor(timeline.tick()));
  ReplayData.Frame f=timeline.pose();
  camera.m_12877971(f.x,f.y,f.z,f.yaw,f.pitch);camera.f_74900774=f.yaw;camera.f_45219594=f.pitch;
  movement.reset(f.x,f.y,f.z);positionCamera(1);
 }
 public static boolean hideGhostModel(C_23527099 actor){
  ReplaySession s=current;
  return s!=null&&s.rendering&&actor==s.ghost&&ReplayInspection.eyeInside(s.timeline.pose(),s.camera.f_78826675,s.camera.f_31030949+s.camera.m_75246573(),s.camera.f_97691941);
 }
 private static void status(String message){if(current!=null)current.gui.m_62518566(message,false);}
 private void syncScene(){
  int tick=(int)timeline.tick();if(sceneTick==tick)return;
  Map<Long,ReplayData.Block> next=timeline.blocks();
  for(Map.Entry<Long,ReplayData.Block> e:applied.entrySet())if(!next.containsKey(e.getKey()))setBlock(e.getValue(),true);
  for(Map.Entry<Long,ReplayData.Block> e:next.entrySet())if(!e.getValue().sameState(applied.get(e.getKey())))setBlock(e.getValue(),false);
  applied.clear();applied.putAll(next);sceneTick=tick;
 }
 private void setBlock(ReplayData.Block b,boolean clear){
  ensureChunk(b.x>>4,b.z>>4);
  C_81592558 block=C_81592558.m_64320102(clear?0:b.id);
  if(block!=null)world.m_38188749(new C_32594356(b.x,b.y,b.z),block.m_28027445(clear?0:b.metadata),2);
 }
 private void ensureChunk(int x,int z){long chunk=((long)x<<32)^(z&0xFFFFFFFFL);if(chunks.add(chunk))world.m_66086785(x,z,true);}
 public static void renderHud(){
  ReplaySession s=current;if(s==null||!s.rendering)return;Minecraft mc=Minecraft.m_20213497();
  C_43718703 scaled=new C_43718703(mc);
  String label=String.format(Locale.US,"\u00a7bReplay \u00a7f%.2f / %.2fs \u00a77| %.2fx | %s",s.timeline.seconds(),s.timeline.duration(),s.timeline.speed(),s.timeline.playing()?"Play":"Paused");
  ReplayData.Frame actor=s.timeline.pose();
  String coords=String.format(Locale.US,"\u00a7eGhost XYZ: \u00a7f%.4f  %.4f  %.4f",actor.x,actor.y,actor.z);
  String hint="\u00a77Jump/Sneak: vertical | Sprint: fast | Slot 7: inspect";
  int y=scaled.m_16103615()-118;
  mc.f_21497584.m_11445413(label,(scaled.m_39119333()-mc.f_21497584.m_09235706(label))/2,y,0xFFFFFF,true);
  mc.f_21497584.m_11445413(coords,(scaled.m_39119333()-mc.f_21497584.m_09235706(coords))/2,y+12,0xFFFFFF,true);
  mc.f_21497584.m_11445413(hint,(scaled.m_39119333()-mc.f_21497584.m_09235706(hint))/2,y+24,0xFFFFFF,true);
 }
}
