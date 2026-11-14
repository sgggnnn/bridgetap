import com.bridger.momentum.BridgeConfig;
public class ConfigRegression {
 public static void main(String[] args){
  if(BridgeConfig.showLiveInputs)throw new AssertionError("Live keystrokes must default OFF");
  BridgeConfig.showLiveInputs=true;BridgeConfig.save();BridgeConfig.showLiveInputs=false;BridgeConfig.load();if(!BridgeConfig.showLiveInputs)throw new AssertionError("Live keystrokes option did not persist");BridgeConfig.showLiveInputs=false;
  if(BridgeConfig.tickOffset!=0||BridgeConfig.plateTickOffset!=0)throw new AssertionError("Both offsets default to zero");
  if(BridgeConfig.detailedReplayInfo)throw new AssertionError("Replay information should default to compact");
  BridgeConfig.detailedReplayInfo=true;BridgeConfig.save();BridgeConfig.detailedReplayInfo=false;BridgeConfig.load();
  if(!BridgeConfig.detailedReplayInfo)throw new AssertionError("Detailed replay information must persist");
  BridgeConfig.detailedReplayInfo=false;
  BridgeConfig.modEnabled=false;BridgeConfig.save();BridgeConfig.modEnabled=true;BridgeConfig.load();if(BridgeConfig.modEnabled)throw new AssertionError("Master mod switch must persist");BridgeConfig.modEnabled=true;
  BridgeConfig.enabled=false;BridgeConfig.hideOnF3=false;BridgeConfig.showReplayInfo=false;BridgeConfig.useNoGayLabel=true;BridgeConfig.displayMode="LAST_TURN";BridgeConfig.save();
  BridgeConfig.enabled=true;BridgeConfig.hideOnF3=true;BridgeConfig.showReplayInfo=true;BridgeConfig.useNoGayLabel=false;BridgeConfig.displayMode="ALL";BridgeConfig.load();
  if(BridgeConfig.showReplayInfo||!BridgeConfig.useNoGayLabel||!"LAST_TURN".equals(BridgeConfig.displayMode))throw new AssertionError("Replay info, label and compact display must persist");
  if(BridgeConfig.enabled||BridgeConfig.hideOnF3)throw new AssertionError("HUD and F3 toggles must persist");
  BridgeConfig.enabled=true;BridgeConfig.hideOnF3=true;
  if(BridgeConfig.shouldShowHud(true))throw new AssertionError("HUD must hide when F3 is open");
  if(!BridgeConfig.shouldShowHud(false))throw new AssertionError("HUD must show when F3 is closed");
  BridgeConfig.hideOnF3=false;
  if(!BridgeConfig.shouldShowHud(true))throw new AssertionError("F3 hide toggle must be optional");
  BridgeConfig.enabled=false;
  if(BridgeConfig.shouldShowHud(false))throw new AssertionError("HUD toggle must hide HUD");
  try{
   java.util.Properties props=new java.util.Properties();java.io.File file=new java.io.File("config/bridgetap.properties");
   try(java.io.FileReader reader=new java.io.FileReader(file)){props.load(reader);}
   props.setProperty("tickOffset","8");props.remove("plateTickOffset");
   try(java.io.FileWriter writer=new java.io.FileWriter(file)){props.store(writer,"");}BridgeConfig.load();
   if(BridgeConfig.tickOffset!=8||BridgeConfig.plateTickOffset!=0)throw new AssertionError("Missing plate offset defaults to zero independently of jump offset");
   props.remove("tickOffset");try(java.io.FileWriter writer=new java.io.FileWriter(file)){props.store(writer,"");}BridgeConfig.load();
   if(BridgeConfig.tickOffset!=0||BridgeConfig.plateTickOffset!=0)throw new AssertionError("Existing config files without offsets use zero defaults");
  }catch(java.io.IOException ex){throw new AssertionError(ex);}
  System.out.println("PASS: HUD and F3 settings survive reload");
 }
}
