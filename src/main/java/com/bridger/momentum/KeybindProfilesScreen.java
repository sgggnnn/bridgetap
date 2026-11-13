package com.bridger.momentum;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.*;
public final class KeybindProfilesScreen extends BridgeScreen {
 private final C_85740840 parent;private KeybindProfiles profiles;private String selected="",name="My profile",message="Assign Next keybind profile in Minecraft Controls to cycle profiles.";private int page;
 public KeybindProfilesScreen(C_85740840 parent){this.parent=parent;try{profiles=new KeybindProfiles(Paths.get("config/bridgetap/keybind-profiles.properties"));selected=profiles.active();if(profiles.find(selected)!=null)name=profiles.find(selected).name;}catch(Exception ex){message=ex.getMessage();}}
 private void action(Runnable task){try{task.run();}catch(Exception ex){message=ex.getMessage();}}
 public static void cycle(){try{KeybindProfiles profiles=new KeybindProfiles(Paths.get("config/bridgetap/keybind-profiles.properties"));KeybindProfiles.Profile next=profiles.next();if(next==null)return;BridgeKeyBindings.apply(Minecraft.m_20213497().f_58088711,next.keys);profiles.select(next.id);Minecraft.m_20213497().f_28396371.m_83523863(new C_69136080("\u00a7b[BridgeTap] Keybind profile: "+next.name));}catch(Exception ex){System.err.println("[BridgeTap] Profile switch: "+ex.getMessage());}}
 @Override protected void paint(){header("Keybind profiles","Named Minecraft and mod shortcuts / One-key profile switching");panel(20,74,380,376);panel(416,74,424,376);if(profiles==null){footer(message);return;}java.util.List<KeybindProfiles.Profile> rows=profiles.list();for(int i=page*10;i<Math.min(rows.size(),page*10+10);i++){KeybindProfiles.Profile p=rows.get(i);button((p.id.equals(profiles.active())?"* ":"")+p.name,36,95+(i-page*10)*29,348,26,true,p.id.equals(selected),()->{selected=p.id;name=p.name;});}button("Previous",36,409,110,24,page>0,false,()->page--);button("Next",160,409,110,24,(page+1)*10<rows.size(),false,()->page++);
  input("name",name,"Profile name",434,100,388,64,value->name=value);button("Save current as new",434,147,388,30,true,true,()->{try{selected=profiles.save("",name,BridgeKeyBindings.snapshot(Minecraft.m_20213497().f_58088711)).id;message="Profile saved.";}catch(Exception ex){message=ex.getMessage();}});
  button("Apply selected",434,193,388,30,profiles.find(selected)!=null,true,()->{try{BridgeKeyBindings.apply(Minecraft.m_20213497().f_58088711,profiles.find(selected).keys);profiles.select(selected);message="Profile applied and Minecraft options saved.";}catch(Exception ex){message=ex.getMessage();}});
  button("Rename selected",434,239,388,30,profiles.find(selected)!=null,false,()->{try{profiles.rename(selected,name);message="Profile renamed. Saved keybindings unchanged.";}catch(Exception ex){message=ex.getMessage();}});
  button("Replace saved keys with current keys",434,281,388,30,profiles.find(selected)!=null,false,()->{try{KeybindProfiles.Profile old=profiles.find(selected);profiles.save(selected,old.name,BridgeKeyBindings.snapshot(Minecraft.m_20213497().f_58088711));message="Saved keybindings updated.";}catch(Exception ex){message=ex.getMessage();}});
  button("Delete selected",434,323,388,30,profiles.find(selected)!=null,false,()->{try{profiles.delete(selected);selected="";}catch(Exception ex){message=ex.getMessage();}});label("Cycle shortcut stays unchanged across profiles.",434,377);label("Set it in Options > Controls > BridgeTap.",434,398);button("Back",696,442,144,this::back);footer(message);
 }
 @Override protected void back(){open(parent);}
}
