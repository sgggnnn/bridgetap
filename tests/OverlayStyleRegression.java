import com.bridger.momentum.*;
import java.util.*;
public final class OverlayStyleRegression {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception {
  for(int i=0;i<3;i++)OverlayStyle.reset(i);
  BridgeConfig.displayMode="ALL";List<JumpEntry> entries=Collections.singletonList(new JumpEntry("1.0 Flat",.504,.504,.001,3,JumpEntry.EntryType.FLAT));
  String chat=EntryFormatter.lines(entries).get(0);OverlayStyle.part(0,0xFF123456);OverlayStyle.part(1,0xFF987654);
  String hud=EntryFormatter.hudLines(entries).get(0);check(hud.contains("§#123456")&&hud.contains("§#987654"),"RGB parts are not independent");
  check(chat.equals(EntryFormatter.lines(entries).get(0))&&!chat.contains("§#"),"RGB style leaked into chat or saved summaries");
  check(HudText.unformatted(chat).equals(HudText.unformatted(hud)),"RGB styling changed visible content");
  List<HudText.Span> spans=HudText.spans("A§#123456B§aC§rD",0xFFFFFFFF);
  check(spans.size()==4&&spans.get(1).color==0xFF123456&&spans.get(2).color==0xFF55FF55&&spans.get(3).color==0xFFFFFFFF,"RGB/legacy/reset parser mismatch");
  check(HudText.unformatted("§#12ZZ99Bad").equals("§#12ZZ99Bad"),"Malformed tokens were swallowed");
  OverlayStyle.text(1,0xFF284769);check(OverlayStyle.styled(1,"§aHello").equals("§#284769Hello"),"Uniform replay color does not override old formatting");
  OverlayStyle.part(8,0x60102030);OverlayStyle.pressedBackground=0x70405060;OverlayStyle.shadowColor(1,0x90123456);OverlayStyle.save();OverlayStyle.pressedBackground=0;OverlayStyle.load();
  check(OverlayStyle.background(0)==0x60102030&&OverlayStyle.pressedBackground==0x70405060&&OverlayStyle.shadowColor[1]==0x90123456,"ARGB or shadow persistence failed");
  BridgeConfig.showBridgeVelocity=true;OverlayStyle.text(0,0xFFFFFFFF);OverlayStyle.part(10,0xFF13579B);
  JumpVelocity history=new JumpVelocity();history.record(1,0,70,-23.123);entries.get(0).setPreJumpZ(history.beforeJump(2));
  check(EntryFormatter.hudLines(entries).get(0).contains("§#13579B[Velo Z (-1t): .123]"),"Velo RGB is not independent");
  check(!OverlayStyle.customText[0]&&!EntryFormatter.lines(entries).get(0).contains("§#"),"Velo picker did not restore component coloring or leaked RGB to chat");
  OverlayStyle.save();OverlayStyle.resetPart(10);OverlayStyle.part(10,0xFF13579B);OverlayStyle.load();check(OverlayStyle.part(10)==0xFF13579B,"Velo RGB not persisted");
  BridgeConfig.showBridgeVelocity=false;
  OverlayStyle.resetPart(0);check(OverlayStyle.color(0).equals(BridgeConfig.color(0)),"Legacy palette fallback was not restored");
  for(int i=0;i<3;i++)OverlayStyle.reset(i);
  System.out.println("PASS: independent RGB HUD colors, ARGB/shadow persistence, legacy chat isolation and safe RGB parsing");
 }
}
