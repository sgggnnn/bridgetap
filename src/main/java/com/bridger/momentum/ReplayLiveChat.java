package com.bridger.momentum;
import com.bridger.momentum.mixin.ReplayGuiAccessor;
import net.minecraft.unmapped.C_97866173;
/** Share the actual chat widget, scroll/history and wall-clock age, never the replay timeline. */
public final class ReplayLiveChat {
 public static void sync(C_97866173 replay,C_97866173 live){
  if(live==null||replay==live)return;
  ReplayGuiAccessor target=(ReplayGuiAccessor)replay;
  target.bridgetap$setChat(live.m_20387035());
  target.bridgetap$setTicks(live.m_76131799());
 }
}
