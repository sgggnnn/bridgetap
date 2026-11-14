package com.bridger.momentum;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;

/** Resolve original platform roles and floor planes once, independently of spectator position. */
final class IslandAutoFit {
 static final class Result {final IslandAutoPlacement.Plan plan;final IslandReplacement.Mask mask;final String summary;final IslandAutoPlacement.Point start,end;Result(IslandAutoPlacement.Plan plan,IslandReplacement.Mask mask,String summary,IslandAutoPlacement.Point start,IslandAutoPlacement.Point end){this.plan=plan;this.mask=mask;this.summary=summary;this.start=start;this.end=end;}}
 private static Result cached;private static Object cachedWorld;
 static Result reference(){return cachedWorld==ReplaySession.sourceWorld()?cached:null;}
 static void restoreWorldReference(IslandReplacement.Mask mask){if(cached!=null&&cached.mask==mask)cachedWorld=ReplaySession.sourceWorld();}
 static boolean sameBridgeNearViewer(){
  Minecraft mc=Minecraft.m_20213497();C_57778754 viewer=ReplaySession.viewer();Result old=cached;
  if(ReplaySession.active()||old==null||cachedWorld!=ReplaySession.sourceWorld()||viewer==null||mc==null||mc.f_26407825==null)return false;
  int x=(int)Math.floor(viewer.f_78826675),z=(int)Math.floor(viewer.f_97691941);
  boolean nearby=false;
  for(IslandAutoPlacement.Point p:new IslandAutoPlacement.Point[]{old.start,old.end}){
   long key=((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|((p.y-1)&4095L);
   if(Math.abs(viewer.f_31030949-p.y)<=6&&(old.mask.blocks.contains(key)||Math.abs(viewer.f_78826675-p.x)<=3&&Math.abs(viewer.f_97691941-p.z)<=3))nearby=true;
   int nativeSurface=p==old.start?old.mask.surface:p.y;C_32594356 floor=new C_32594356(p.x,nativeSurface-1,p.z),above=new C_32594356(p.x,nativeSurface,p.z);
   if(!mc.f_26407825.m_09893682(floor))return false;
   int floorId=C_81592558.m_63461719(mc.f_26407825.m_77852500(floor).m_41958956()),aboveId=C_81592558.m_63461719(mc.f_26407825.m_77852500(above).m_41958956());
   if(!BridgeGeometry.surface(floorId)||BridgeGeometry.surface(aboveId)&&!PlacementAppearance.isPlaced(p.x,p.y,p.z))return false;
  }
  return nearby;
 }
 static Result forAttempt(IslandTemplate template){
  if(sameBridgeNearViewer()){Result old=cached;return new Result(IslandAutoPlacement.fit(template,old.start,old.end),old.mask,old.summary,old.start,old.end);}
  return detect(template);
 }

 private static List<ReplayData.Block> cells(IslandReplacement.Mask mask){List<ReplayData.Block> result=new ArrayList<>();for(long k:mask.blocks)result.add(new ReplayData.Block((int)(k>>38),(int)(k&4095),(int)(k<<26>>38),1,0,0,false));return result;}
 private static boolean finish(IslandReplacement.Mask mask,IslandAutoDetector.Grid grid){for(ReplayData.Block b:cells(mask)){int id=grid.block(b.x,b.y,b.z);if(id==147||id==148)return true;}return false;}
 private static boolean overlaps(IslandReplacement.Mask a,IslandReplacement.Mask b){for(long k:a.blocks)if(b.blocks.contains(k))return true;return false;}
 private static IslandReplacement.Mask normalize(IslandReplacement.Mask mask,IslandPlatformDeck deck){IslandReplacement.Mask result=new IslandReplacement.Mask(cells(mask),deck.surface,mask.areas,mask.visible);result.attachX=deck.centerX;result.attachZ=deck.centerZ;result.presetX=mask.presetX;result.presetY=mask.presetY;result.presetZ=mask.presetZ;return result;}
 /** Presets and initial fitting must use the same broad native deck, not the
  * arbitrary flood-fill seed (which can be a lower ledge or a wall). */
 static IslandReplacement.Mask startReference(IslandReplacement.Mask mask,double feet,int px,int pz){
  if(mask.empty())return mask;
  try{return normalize(mask,nativeDeck(mask,feet,px,pz));}
  catch(IllegalArgumentException sparse){return mask;} // Legacy partial captures have no broad deck to normalize.
 }
 static IslandPlatformDeck nativeDeck(IslandReplacement.Mask mask,double feet,int px,int pz){
  Minecraft mc=Minecraft.m_20213497();ReplayData replay=ReplaySession.active()?ReplaySession.recording():null;
  Map<Long,Integer> terrain=new HashMap<>();
  if(replay!=null){for(ReplayData.Block b:replay.blocks)if(!b.placed)terrain.put(b.key(),b.id);}
  else for(ReplayData.Block b:cells(mask)){C_32594356 p=new C_32594356(b.x,b.y,b.z);terrain.put(b.key(),C_81592558.m_63461719(mc.f_26407825.m_77852500(p).m_41958956()));}
  IslandAutoDetector.Grid grid=(x,y,z)->terrain.getOrDefault(((long)x&0x3ffffff)<<38|((long)z&0x3ffffff)<<12|(y&4095L),0);
  return IslandPlatformDeck.find(grid,cells(mask),feet,px,pz);
 }
 static Result detect(IslandTemplate template){
  Minecraft mc=Minecraft.m_20213497();C_57778754 viewer=ReplaySession.viewer();if(mc==null||viewer==null)throw new IllegalArgumentException("Join a bridge map first");
  ReplayData data=IslandReplacement.sceneData();boolean replay=ReplaySession.active();
  Set<Long> excluded=new HashSet<>();Map<Long,Integer> recorded=new HashMap<>();if(data!=null){for(ReplayData.Block b:data.blocks)if(b.placed)excluded.add(b.key());if(replay)for(ReplayData.Block b:data.blocks)if(!b.placed&&!excluded.contains(b.key()))recorded.put(b.key(),b.id);}
  Map<Long,Integer> cache=new HashMap<>();IslandAutoDetector.Grid grid=(x,y,z)->{if(y<0||y>255)return 0;long key=((long)x&0x3FFFFFF)<<38|((long)z&0x3FFFFFF)<<12|(long)y;if(excluded.contains(key))return 0;if(replay)return recorded.getOrDefault(key,0);Integer old=cache.get(key);if(old!=null)return old;C_32594356 pos=new C_32594356(x,y,z);int id=mc.f_26407825.m_09893682(pos)?C_81592558.m_63461719(mc.f_26407825.m_77852500(pos).m_41958956()):0;cache.put(key,id);return id;};
  int px=(int)Math.floor(viewer.f_78826675),pz=(int)Math.floor(viewer.f_97691941);double feet=viewer.f_31030949;
  if(replay){ReplayData.Frame first=data.frames.get(0);px=(int)Math.floor(first.x);pz=(int)Math.floor(first.z);feet=first.y;}
  IslandReplacement.Mask nearby=IslandReplacement.captureStartAt(px,(int)Math.floor(feet)-1,pz),start=nearby;
  boolean atFinish=!replay&&!nearby.empty()&&finish(nearby,grid);
  if(atFinish){
   start=IslandReplacement.EMPTY;
   if(data!=null){ReplayData.Frame f=data.frames.get(0);IslandReplacement.Mask candidate=IslandReplacement.captureStartAt((int)Math.floor(f.x),(int)Math.floor(f.y)-1,(int)Math.floor(f.z));if(!candidate.empty()&&!finish(candidate,grid)&&!overlaps(candidate,nearby)){start=candidate;feet=f.y;px=(int)Math.floor(f.x);pz=(int)Math.floor(f.z);}}
   if(start.empty()){PresetServers.Anchor known=IslandStartAnchors.automatic();if(known!=null){IslandReplacement.Mask candidate=IslandReplacement.captureStartAt(known.x,known.y,known.z);if(!candidate.empty()&&!finish(candidate,grid)&&!overlaps(candidate,nearby)){start=candidate;feet=known.y+1;px=known.x;pz=known.z;}}}
  }
  if(start.empty()||finish(start,grid))throw new IllegalArgumentException("Start island unknown. Stand on its floor and retry");
  IslandPlatformDeck deck=IslandPlatformDeck.find(grid,cells(start),feet,px,pz);int dx=0,dz=0;Integer recordedRow=null;
  if(data!=null){ReplayData.Frame first=data.frames.get(0);if(first.x>=start.minX-2&&first.x<=start.maxX+3&&first.z>=start.minZ-2&&first.z<=start.maxZ+3){ReplayData.Block a=null,b=null;for(ReplayData.Block block:data.blocks)if(block.placed&&block.id>0){if(a==null||block.tick<a.tick)a=block;if(b==null||block.tick>=b.tick)b=block;}if(a!=null&&b!=null){int ax=b.x-a.x,az=b.z-a.z;if(Math.abs(ax)+Math.abs(az)>1){dx=Math.abs(ax)>=Math.abs(az)?Integer.signum(ax):0;dz=dx==0?Integer.signum(az):0;if(dx!=0)recordedRow=a.z;if(dz!=0)recordedRow=a.x;}}}}
  IslandAutoDetector.Route route=IslandAutoDetector.find(grid,deck.minX,deck.maxX,deck.minZ,deck.maxZ,deck.surface,dx,dz,deck.centerX,deck.centerZ);
  if(route==null)throw new IllegalArgumentException("Finish not found. Load both islands and retry");
  IslandReplacement.Mask end=IslandReplacement.captureStartAt(route.finish.x,route.finish.y-1,route.finish.z);
  if(end.empty()||overlaps(start,end))throw new IllegalArgumentException("Distinct start and finish islands are required");
  if(!finish(end,grid)&&dx==0&&dz==0)throw new IllegalArgumentException("Finish role is uncertain. Record a bridge or use Preview / Adjust");
  if(atFinish&&!overlaps(nearby,end))throw new IllegalArgumentException("This is another lane. Stand on its start and retry");
  IslandPlatformDeck endDeck=IslandPlatformDeck.find(grid,cells(end),route.finish.y,route.finish.x,route.finish.z);
  int vx=Integer.signum(route.finish.x-route.start.x),vz=Integer.signum(route.finish.z-route.start.z);if(vx!=0)vz=0;int row=IslandBridgeRow.resolve(deck,endDeck,cells(end),grid,vx,vz,recordedRow);
  deck=vx!=0?deck.recenter(deck.centerX,row):deck.recenter(row,deck.centerZ);endDeck=vx!=0?endDeck.recenter(endDeck.centerX,row):endDeck.recenter(row,endDeck.centerZ);
  IslandAutoPlacement.Point first=deck.edge(vx,vz,row),last=endDeck.edge(-vx,-vz,row);
  if((last.x-first.x)*vx+(last.z-first.z)*vz<2)throw new IllegalArgumentException("Platform order is ambiguous. Stand on the start and retry");
  start=normalize(start,deck);end=normalize(end,endDeck);List<ReplayData.Block> all=cells(start);all.addAll(cells(end));
  IslandReplacement.Mask mask=new IslandReplacement.Mask(all,start.surface);mask.attachX=start.attachX;mask.attachZ=start.attachZ;mask.presetX=start.presetX;mask.presetY=start.presetY;mask.presetZ=start.presetZ;
  if(!replay)IslandStartAnchors.remember(start);
  int gap=Math.abs(last.x-first.x)+Math.abs(last.z-first.z)-1;
  IslandAutoPlacement.Plan fitted=IslandAutoPlacement.fit(template,first,last);System.out.println("[BridgeTap] Island fit: "+template.name+" feet="+viewer.f_31030949+" reference="+feet+" start="+first.x+","+first.y+","+first.z+" end="+last.x+","+last.y+","+last.z+" copyFloor="+IslandAutoPlacement.deck(template,new IslandParts(template),0)[4]+" offsetY="+fitted.y);
  Result result=new Result(fitted,mask,"Aligned: "+gap+"b gap / "+String.format(Locale.US,"%+d",last.y-first.y)+"b height",first,last);if(!replay){cached=result;cachedWorld=ReplaySession.sourceWorld();}return result;
 }
}
