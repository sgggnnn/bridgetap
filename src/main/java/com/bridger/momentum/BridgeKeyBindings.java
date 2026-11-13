package com.bridger.momentum;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import net.minecraft.unmapped.*;

/** Late registration restores only this mod's bindings; never reloads other game options. */
public final class BridgeKeyBindings {
 public static final String[] IDS={"key.bridgetap.openSettings","key.bridgetap.editReplayInputs","key.bridgetap.saveClip","key.bridgetap.nextProfile"};
 private static final int[] DEFAULTS={48,62,67,0};
 private BridgeKeyBindings(){}
 public static C_23708450[] register(C_76792751 settings,Path options){
  Map<String,Integer> saved=read(options);List<C_23708450> all=new ArrayList<>(Arrays.asList(settings.f_41636781));C_23708450[] result=new C_23708450[IDS.length];
  for(int i=0;i<IDS.length;i++){
   for(C_23708450 key:all)if(IDS[i].equals(key.m_82538581())){result[i]=key;break;}
   if(result[i]==null){result[i]=new C_23708450(IDS[i],DEFAULTS[i],"key.categories.bridgetap");Integer code=saved.get(IDS[i]);if(code!=null)result[i].m_05281935(code);all.add(result[i]);}
  }
  settings.f_41636781=all.toArray(new C_23708450[0]);C_23708450.m_72397208();return result;
 }
 static Map<String,Integer> read(Path path){Map<String,Integer> values=new HashMap<>();try{if(!Files.isRegularFile(path)||Files.size(path)>4*1024*1024)return values;try(BufferedReader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){String line;while((line=reader.readLine())!=null){int colon=line.indexOf(':');if(colon<0)continue;String id=line.substring(0,colon);for(String known:IDS)if(id.equals("key_"+known))try{values.put(known,Integer.parseInt(line.substring(colon+1).trim()));}catch(NumberFormatException ignored){}}}}catch(IOException ex){System.err.println("[BridgeTap] Keybind restore: "+ex.getMessage());}return values;}
 public static Map<String,Integer> snapshot(C_76792751 settings){Map<String,Integer> result=new LinkedHashMap<>();for(C_23708450 key:settings.f_41636781)if(!IDS[3].equals(key.m_82538581()))result.put(key.m_82538581(),key.m_31350030());return result;}
 public static void apply(C_76792751 settings,Map<String,Integer> values){for(C_23708450 key:settings.f_41636781){Integer code=values.get(key.m_82538581());if(code!=null&&!IDS[3].equals(key.m_82538581()))key.m_05281935(code);}C_23708450.m_72397208();C_23708450.m_95396469();settings.m_18664673();}
}
