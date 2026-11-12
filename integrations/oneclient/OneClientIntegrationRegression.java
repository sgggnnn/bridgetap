import com.bridger.momentum.*;
import java.lang.reflect.*;
public final class OneClientIntegrationRegression {
 public static void main(String[] args)throws Exception{
  Class<?> api=Class.forName("org.polyfrost.oneconfig.api.hud.v1.OneConfigHudWrapper");
  Method bridge=OneClientIntegration.class.getDeclaredMethod("invoke",int.class,Object.class,Method.class,Object[].class);bridge.setAccessible(true);
  Object self=new Object();
  for(int i=0;i<3;i++){
   bridge.invoke(null,i,self,api.getMethod("setX",float.class),new Object[]{143f});
   bridge.invoke(null,i,self,api.getMethod("setY",float.class),new Object[]{81f});
   bridge.invoke(null,i,self,api.getMethod("setScale",float.class),new Object[]{1.5f});
   if(!bridge.invoke(null,i,self,api.getMethod("getX"),null).equals(143f)||!bridge.invoke(null,i,self,api.getMethod("getY"),null).equals(81f))throw new AssertionError("HUD position mismatch");
   if(!bridge.invoke(null,i,self,api.getMethod("getScale"),null).equals(1.5f))throw new AssertionError("HUD scale mismatch");
   bridge.invoke(null,i,self,api.getMethod("setHidden",boolean.class),new Object[]{true});
   if(!bridge.invoke(null,i,self,api.getMethod("getHidden"),null).equals(true))throw new AssertionError("HUD visibility mismatch");
   if(!bridge.invoke(null,i,self,api.getMethod("getModId"),null).equals("bridgetap"))throw new AssertionError("Wrong provider");
   if(bridge.invoke(null,i,self,api.getMethod("getResizeAxes"),null)==null)throw new AssertionError("Missing resize policy");
   OneClientIntegration.recordSize(i,100,50);
   if(!bridge.invoke(null,i,self,api.getMethod("getScaledWidth"),null).equals(150f))throw new AssertionError("HUD bounds mismatch");
  }
  System.out.println("PASS: installed OneConfig wrapper API, independent 3 HUD positions/scales/visibility and bounds");
 }
}
