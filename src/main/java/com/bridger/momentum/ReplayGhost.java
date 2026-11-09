package com.bridger.momentum;

import net.minecraft.unmapped.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;

/** Native player model; recorded profile properties keep the recorded skin. */
public final class ReplayGhost extends C_54810664 {
 private final ReplayData.Identity identity;
 private final C_97147141 info;
 public ReplayGhost(C_34399650 world,ReplayData.Identity identity){
  super(world,profile(identity));this.identity=identity;this.info=new C_97147141(profile(identity));
  f_80637786=true;
 }
 private static GameProfile profile(ReplayData.Identity who){
  GameProfile profile=new GameProfile(UUID.fromString(who.uuid),who.name);
  if(!who.textureValue.isEmpty())profile.getProperties().put("textures",who.textureSignature.isEmpty()?new Property("textures",who.textureValue):new Property("textures",who.textureValue,who.textureSignature));
  return profile;
 }
 @Override public C_97147141 m_96591750(){return info;}
 @Override public boolean m_13836848(){return false;}
 @Override public String m_63406370(){return identity.model;}
 @Override public boolean m_05104748(C_88812351 part){return (identity.modelParts&part.m_23895741())!=0;}
 @Override public C_95231345 m_33641726(){return new C_69136080(identity.displayName);}
 public void apply(ReplayData.Frame f){
  m_70517412(f.x,f.y,f.z);
  f_09368719=f_59999765=f.x;f_93723721=f_90348373=f.y;f_84066837=f_88346412=f.z;
  f_74900774=f_80130373=f.yaw;f_45219594=f_03549461=f.pitch;
  f_28781883=f_50412996=f.headYaw;f_14416847=f_21972499=f.bodyYaw;
  f_67244135=f_19026405=f.swing;f_47083104=f_18020014=f.walkSpeed;f_00715684=f.walkProgress;
  f_99890177=f_22466577=f.x;f_62909408=f_60272612=f.y;f_49011524=f_22361063=f.z;
  f_47825615=f.onGround;m_22136189(f.sneaking);
  C_88708284 held=f_29439708.f_35249023[0];
  if(f.itemId==0)f_29439708.f_35249023[0]=null;
  else if(held==null||C_98888256.m_18004261(held.m_23235460())!=f.itemId||held.m_30227787()!=f.itemDamage){
   C_98888256 item=C_98888256.m_06629599(f.itemId);
   f_29439708.f_35249023[0]=item==null?null:new C_88708284(item,1,f.itemDamage);
  }
 }
}
