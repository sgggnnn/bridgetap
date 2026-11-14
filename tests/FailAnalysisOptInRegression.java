import com.bridger.momentum.*;import java.nio.file.*;import java.util.*;import java.io.*;
public class FailAnalysisOptInRegression {
 public static void main(String[] args)throws Exception{
  if(BridgeConfig.autoFailAnalysis)throw new AssertionError("Analysis default must be OFF");Files.createDirectories(Paths.get("config"));Properties legacy=new Properties();legacy.setProperty("autoFailAnalysis","true");try(OutputStream out=Files.newOutputStream(Paths.get("config/bridgetap.properties"))){legacy.store(out,"");}BridgeConfig.load();if(BridgeConfig.autoFailAnalysis)throw new AssertionError("Legacy automatic default was not switched off");BridgeConfig.autoFailAnalysis=true;BridgeConfig.save();BridgeConfig.autoFailAnalysis=false;BridgeConfig.load();if(!BridgeConfig.autoFailAnalysis)throw new AssertionError("Explicit opt-in was not saved");BridgeConfig.autoFailAnalysis=false;BridgeConfig.save();BridgeConfig.autoFailAnalysis=true;BridgeConfig.load();if(BridgeConfig.autoFailAnalysis)throw new AssertionError("Explicit OFF was not saved");System.out.println("PASS OFF default, legacy automatic-default migration, explicit opt-in and opt-out persistence");
 }
}
