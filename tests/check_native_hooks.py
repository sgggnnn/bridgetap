#!/usr/bin/env python3
"""Check every Mixin injection/accessor against the exact Minecraft client bytecode.
Usage: python check_native_hooks.py <minecraft-intermediary.jar> <mod.jar>
No game, graphics context or third-party Python packages are required.
"""
import io,json,struct,sys,zipfile
class Reader:
 def __init__(self,data):self.f=io.BytesIO(data)
 def u1(self):return struct.unpack('>B',self.f.read(1))[0]
 def u2(self):return struct.unpack('>H',self.f.read(2))[0]
 def u4(self):return struct.unpack('>I',self.f.read(4))[0]
 def read(self,n):return self.f.read(n)
class Class:
 def __init__(self,data):
  r=Reader(data);assert r.u4()==0xCAFEBABE;r.u2();r.u2();self.cp=[None]*r.u2();i=1
  while i<len(self.cp):
   tag=r.u1()
   if tag==1:self.cp[i]=r.read(r.u2()).decode('utf8',errors='replace')
   elif tag in (3,4):self.cp[i]=(tag,r.read(4))
   elif tag in (5,6):self.cp[i]=(tag,r.read(8));i+=1
   elif tag in (7,8,16,19,20):self.cp[i]=(tag,r.u2())
   elif tag in (9,10,11,12,17,18):self.cp[i]=(tag,r.u2(),r.u2())
   elif tag==15:self.cp[i]=(tag,r.u1(),r.u2())
   else:raise AssertionError('Unknown classfile constant '+str(tag))
   i+=1
  r.u2();self.name=self.cp[self.cp[r.u2()][1]];r.u2()
  for _ in range(r.u2()):r.u2()
  self.fields=self.members(r);self.methods=self.members(r);self.attrs=self.attrs_read(r)
 def attrs_read(self,r):return {self.cp[r.u2()]:r.read(r.u4()) for _ in range(r.u2())}
 def members(self,r):
  out=[]
  for _ in range(r.u2()):
   flags=r.u2();name=self.cp[r.u2()];desc=self.cp[r.u2()];attrs=self.attrs_read(r);out.append((name,desc,attrs))
  return out
 def value(self,r):
  tag=chr(r.u1())
  if tag=='[':return [self.value(r) for _ in range(r.u2())]
  if tag=='@':return self.annotation(r)
  if tag=='e':return (self.cp[r.u2()],self.cp[r.u2()])
  return self.cp[r.u2()]
 def annotation(self,r):
  kind=self.cp[r.u2()];values={}
  for _ in range(r.u2()):name=self.cp[r.u2()];values[name]=self.value(r)
  return kind,values
 def annotations(self,attrs):
  result=[]
  for key in ('RuntimeVisibleAnnotations','RuntimeInvisibleAnnotations'):
   if key in attrs:
    r=Reader(attrs[key]);result.extend(self.annotation(r) for _ in range(r.u2()))
  return result
 def calls(self,owner,name,desc):
  found=[]
  for i,c in enumerate(self.cp):
   if isinstance(c,tuple) and c[0] in (10,11):
    o=self.cp[self.cp[c[1]][1]];pair=self.cp[c[2]]
    if (o,self.cp[pair[1]],self.cp[pair[2]])==(owner,name,desc):found.append(i)
  return found
 def has_call(self,attrs,indices):
  r=Reader(attrs['Code']);r.u2();r.u2();code=r.read(r.u4())
  return any(any(bytes([op])+struct.pack('>H',i) in code for op in (182,183,184,185)) for i in indices)
client=zipfile.ZipFile(sys.argv[1]);mod=zipfile.ZipFile(sys.argv[2]);config=json.loads(mod.read('bridgetap.mixins.json'));checked=0
for mixin in config['client']:
 cls=Class(mod.read(config['package'].replace('.','/')+'/'+mixin+'.class'))
 targets=[]
 for kind,values in cls.annotations(cls.attrs):
  if kind.endswith('/Mixin;'):targets=values.get('value',[])
 assert targets,'Mixin has no target: '+mixin
 for target in targets:
  native=Class(client.read(target[1:-1]+'.class'))
  for name,desc,attrs in cls.methods:
   for annotation,values in cls.annotations(attrs):
    if annotation.endswith('/Accessor;'):
     field=next((field for field in native.fields if field[0]==values['value']),None)
     assert field,(mixin,name,'missing accessor field',values['value'])
     assert desc in ('()'+field[1],'('+field[1]+')V'),(mixin,name,'accessor type mismatch',desc,field[1])
     checked+=1
    if annotation.endswith(('/Inject;','/Redirect;','/ModifyVariable;')):
     methods=values['method'];methods=[methods] if isinstance(methods,str) else methods
     for method in methods:
      candidates=[m for m in native.methods if m[0]==method.split('(')[0]]
      assert candidates,(mixin,name,'missing target',method)
      if annotation.endswith('/ModifyVariable;'):
       value_type=desc.split(')')[1]
       assert any(desc=='('+value_type+m[1][1:m[1].index(')')]+')'+value_type for m in candidates),(mixin,name,'invalid target argument capture',desc)
      checked+=1
      ats=values['at'];ats=[ats] if isinstance(ats,tuple) else ats
      for _,at in ats:
       if at.get('value')=='INVOKE':
        signature=at['target'];owner,tail=signature[1:].split(';',1);mname,params=tail.split('(',1)
        indices=native.calls(owner,mname,'('+params)
        assert any(native.has_call(a,indices) for _,_,a in candidates),(mixin,name,'missing invocation',signature)
# The restored gallery is a plain screen, with no server/container callbacks.
gallery=Class(mod.read('com/bridger/momentum/BridgeHistoryScreen.class'))
for unsafe in ('m_41805697','m_47002234','m_23755539','m_47537840','m_52921597'):
 for owner in ('net/minecraft/unmapped/C_80147496','net/minecraft/unmapped/C_87711245'):
  assert not any(isinstance(c,tuple) and c[0] in (10,11) and gallery.cp[gallery.cp[c[1]][1]]==owner and gallery.cp[gallery.cp[c[2]][1]]==unsafe for c in gallery.cp),'Gallery invokes live container behaviour'
# The renderer context must be restored in an actual JVM finally block.
mc_mixin=Class(mod.read('com/bridger/momentum/mixin/MinecraftMixin.class'))
attrs=next(a for n,_,a in mc_mixin.methods if n=='bridgetap$renderReplay');r=Reader(attrs['Code']);r.u2();r.u2();r.read(r.u4());handlers=[(r.u2(),r.u2(),r.u2(),r.u2()) for _ in range(r.u2())]
assert any(h[3]==0 for h in handlers),'Replay rendering lacks a catch-all finally handler'
# Regression: render-only switches must not reset shaders or apply abilities to
# Minecraft.player. Both native setters caused side effects in 1.2.0.
session=Class(mod.read('com/bridger/momentum/ReplaySession.class'))
assert not session.calls('net/minecraft/client/Minecraft','m_07664876','(Lnet/minecraft/unmapped/C_42232651;)V'),'Replay still calls shader-resetting camera setter'
assert not session.calls('net/minecraft/unmapped/C_34267874','m_37421800','(Lnet/minecraft/unmapped/C_06007608$C_36868821;)V'),'Replay still changes live player abilities'
tail=next(a for n,_,a in session.methods if n=='tickTail')
assert session.has_call(tail,session.calls('com/bridger/momentum/ReplayFreeCamera','step','(DFDDDZ)V')),'Free flight is not wired to client ticks'
assert not session.calls('org/lwjgl/input/Keyboard','isKeyDown','(I)Z'),'Camera still bypasses Minecraft input bindings'
for kind in ('ReplayCamera','ReplayGhost'):
 cls=Class(mod.read('com/bridger/momentum/'+kind+'.class'))
 assert any(n=='m_13836848' and desc=='()Z' for n,desc,_ in cls.methods),'Replay entity still inherits the live spectator mode: '+kind
assert session.calls('net/minecraft/unmapped/C_61390454','m_60634394','()V'),'Replay chunk GPU buffers are not released'
after_tick=next(a for n,_,a in mc_mixin.methods if n=='bridgetap$afterTick')
assert mc_mixin.has_call(after_tick,mc_mixin.calls('com/bridger/momentum/ReplayRecorder','sample','()V')),'Recording is not sampled after the tick inputs/physics'
assert 'com/bridger/momentum/BridgeHotbarReplay.class' not in mod.namelist(),'Live slot-2 replay shortcut is still packaged'
assert 'LastBridgeHotbarMixin' not in config['client'],'Live inventory renderer is still intercepted'
hud=Class(mod.read('com/bridger/momentum/BridgeHUD.class'))
assert not hud.calls('com/bridger/momentum/BridgeTelemetryHUD','live','()V'),'Live telemetry is still displayed'
assert not hud.calls('com/bridger/momentum/BridgeTelemetryHUD','liveCoordinates','()V'),'Player XYZ still displayed outside replay'
assert session.calls('com/bridger/momentum/ReplayTimeline','cycleSpeed','()V'),'Playback speed control is not wired'
constructor=next(a for n,_,a in session.methods if n=='<init>')
assert session.has_call(constructor,session.calls('com/bridger/momentum/ReplayControls','slots','()[I')),'Replay inventory does not use the replay-tool definition'
targeting=next(a for n,_,a in session.methods if n=='aimsAtGhost')
assert session.has_call(targeting,session.calls('net/minecraft/unmapped/C_34399650','m_82559274','(Lnet/minecraft/unmapped/C_94584382;Lnet/minecraft/unmapped/C_94584382;)Lnet/minecraft/unmapped/C_24654288;')),'POV picking does not check replay-world occlusion'
camera=Class(mod.read('com/bridger/momentum/ReplayCamera.class'))
mouse=next(a for n,_,a in camera.methods if n=='m_74052865')
assert camera.has_call(mouse,camera.calls('com/bridger/momentum/ReplaySession','povLocked','()Z')),'POV does not suppress manual camera rotation'
position=next(a for n,_,a in session.methods if n=='positionCamera')
assert session.has_call(position,session.calls('com/bridger/momentum/ReplayTimeline','pose','()Lcom/bridger/momentum/ReplayData$Frame;')),'POV is not updated from recorded poses on every render'
skin=next(a for n,_,a in camera.methods if n=='m_68930603')
assert camera.has_call(skin,camera.calls('com/bridger/momentum/ReplayGhost','m_68930603','()Lnet/minecraft/unmapped/C_22168528;')),'Imported POV does not use the recorded actor skin'
print('PASS:',checked,'native hooks/accessors, render finally, isolated gallery, tick-driven flight and shader/ability/resource regressions')
