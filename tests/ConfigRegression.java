import com.bridger.momentum.BridgeConfig;
public class ConfigRegression {
 public static void main(String[] args){
  BridgeConfig.enabled=false;BridgeConfig.hideOnF3=false;BridgeConfig.save();
  BridgeConfig.enabled=true;BridgeConfig.hideOnF3=true;BridgeConfig.load();
  if(BridgeConfig.enabled||BridgeConfig.hideOnF3)throw new AssertionError("HUD and F3 toggles must persist");
  BridgeConfig.enabled=true;BridgeConfig.hideOnF3=true;
  if(BridgeConfig.shouldShowHud(true))throw new AssertionError("HUD must hide when F3 is open");
  if(!BridgeConfig.shouldShowHud(false))throw new AssertionError("HUD must show when F3 is closed");
  BridgeConfig.hideOnF3=false;
  if(!BridgeConfig.shouldShowHud(true))throw new AssertionError("F3 hide toggle must be optional");
  BridgeConfig.enabled=false;
  if(BridgeConfig.shouldShowHud(false))throw new AssertionError("HUD toggle must hide HUD");
  System.out.println("PASS: HUD and F3 settings survive reload");
 }
}
