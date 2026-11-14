package com.bridger.momentum;
import java.lang.reflect.*;

/** Cera owns sky assets per renderer, so secondary renderers need their own reload. */
public final class ReplaySkyCompat {
 private Object live,sky,lastLayers;private Method getter,reload,client;private Field layers;private boolean absent,failed;
 public void sync(Object source,Object target){
  if(absent||failed||source==null||target==null)return;
  try{
   if(getter==null){
    try{getter=source.getClass().getMethod("cera$getCustomSky");}catch(NoSuchMethodException ex){absent=true;return;}
    live=getter.invoke(source);sky=getter.invoke(target);
    layers=live.getClass().getDeclaredField("layers");layers.setAccessible(true);
    Class<?> resources=Class.forName("net.ornithemc.osl.resource.loader.api.resource.manager.ResourceManager",true,source.getClass().getClassLoader());
    client=resources.getMethod("client");reload=sky.getClass().getMethod("resourcesReloaded",resources);
   }
   Object loaded=layers.get(live);if(loaded==lastLayers)return;
   reload.invoke(sky,client.invoke(null));lastLayers=loaded;
  }catch(ReflectiveOperationException|LinkageError ex){failed=true;System.err.println("[BridgeTap] Optional replay custom sky initialization failed: "+ex.getClass().getSimpleName());}
 }
}
