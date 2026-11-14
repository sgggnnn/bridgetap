package com.bridger.momentum;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.unmapped.*;

/** Optional native CTM adapters, evaluated only when a cached island mesh is built. */
public final class IslandConnectedTextures {
 private boolean attempted,failed;private Object module,grass,context,lastRules,lastMode,config;private Method transform,overlayQuad;private Constructor<?> contextConstructor;private Field rules,mode;
 private Method optifine;private Constructor<?> renderEnv;
 public boolean enabled(){return !failed&&(module!=null||optifine!=null);}
 public boolean prepare(Object atlas){
  try{
   if(!attempted){attempted=true;
    try{
     module=atlas.getClass().getMethod("cera$getConnectedTextures").invoke(atlas);grass=atlas.getClass().getMethod("cera$getBetterGrass").invoke(atlas);
     for(Method m:module.getClass().getMethods())if(m.getName().equals("transform")&&m.getParameterTypes().length==6)transform=m;
     if(transform==null)throw new NoSuchMethodException("Cera CTM transform");
     ClassLoader loader=module.getClass().getClassLoader();Class<?> type=transform.getParameterTypes()[5];
     for(Constructor<?> c:type.getConstructors())if(c.getParameterTypes().length==1)contextConstructor=c;
     if(contextConstructor==null||transform==null)throw new NoSuchMethodException("Cera CTM contract");
     rules=module.getClass().getDeclaredField("transformation");rules.setAccessible(true);
     try{Class<?> cera=Class.forName("dev.rdh.cera.Cera",true,loader);config=cera.getField("CONFIG").get(null);mode=config.getClass().getField("connectedTextures");}catch(ClassNotFoundException ignored){}
    }catch(NoSuchMethodException missing){module=null;initOptifine(atlas.getClass().getClassLoader());}
   }
   if(failed||module==null)return false;
   Object current=rules.get(module),setting=mode==null?null:mode.get(config);
   if(current==lastRules&&setting==lastMode&&context!=null)return false;
   context=contextConstructor.newInstance(grass);lastRules=current;lastMode=setting;return true;
  }catch(ReflectiveOperationException|LinkageError|RuntimeException ex){fail(ex);return false;}
 }
 private void initOptifine(ClassLoader loader)throws ReflectiveOperationException{
  try{Class<?> ctm=Class.forName("net.optifine.ConnectedTextures",false,loader),env=Class.forName("net.optifine.render.RenderEnv",false,loader);
   for(Method m:ctm.getMethods())if(m.getName().equals("getConnectedTexture")&&m.getParameterTypes().length==5&&m.getReturnType().isArray())optifine=m;
   for(Constructor<?> c:env.getConstructors())if(c.getParameterTypes().length==2)renderEnv=c;
   if(renderEnv==null)optifine=null;
  }catch(ClassNotFoundException ignored){}
 }
 @SuppressWarnings("unchecked")
 public List<C_85259637> transform(C_46108969 view,C_15815210 state,C_32594356 pos,List<C_85259637> input){
  if(input.isEmpty()||!enabled())return input;
  try{
   if(module!=null){List<Object> overlays=new ArrayList<>();List<C_85259637> result=(List<C_85259637>)transform.invoke(module,view,state,pos,input,overlays,context);
    if(result==null)return input;if(overlays.isEmpty())return result;List<C_85259637> all=new ArrayList<>(result);for(Object overlay:overlays){if(overlayQuad==null)overlayQuad=overlay.getClass().getMethod("quad");all.add((C_85259637)overlayQuad.invoke(overlay));}return all;
   }
   Object env=renderEnv.newInstance(state,pos);List<C_85259637> result=new ArrayList<>();for(C_85259637 quad:input){Object array=optifine.invoke(null,view,state,pos,quad,env);if(array==null){result.add(quad);continue;}for(int i=0;i<Array.getLength(array);i++)result.add((C_85259637)Array.get(array,i));}return result;
  }catch(ReflectiveOperationException|LinkageError|RuntimeException ex){fail(ex);return input;}
 }
 private void fail(Throwable ex){failed=true;System.err.println("[BridgeTap] Optional island connected textures unavailable: "+ex.getClass().getSimpleName());}
}
