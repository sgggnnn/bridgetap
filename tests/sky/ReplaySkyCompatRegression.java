import com.bridger.momentum.ReplaySkyCompat;
import net.ornithemc.osl.resource.loader.api.resource.manager.ResourceManager;
public class ReplaySkyCompatRegression {
 public static class Sky {public Object layers=new Object();int reloads;public void resourcesReloaded(ResourceManager resources){if(resources!=ResourceManager.client())throw new AssertionError();layers=new Object();reloads++;}}
 public static class Renderer {final Sky sky=new Sky();public Sky cera$getCustomSky(){return sky;}}
 public static void main(String[] args){
  Renderer live=new Renderer(),replay=new Renderer();ReplaySkyCompat compat=new ReplaySkyCompat();
  compat.sync(live,replay);if(replay.sky.reloads!=1||live.sky.reloads!=0)throw new AssertionError("Secondary renderer not initialized independently");
  for(int i=0;i<1000;i++)compat.sync(live,replay);if(replay.sky.reloads!=1)throw new AssertionError("Sky assets reloaded every frame");
  live.sky.layers=new Object();compat.sync(live,replay);if(replay.sky.reloads!=2)throw new AssertionError("Pack reload missed");
  new ReplaySkyCompat().sync(new Object(),new Object());
  System.out.println("PASS secondary Cera sky initialization, resource reload and 1000-frame cache; absent mod is optional");
 }
}
