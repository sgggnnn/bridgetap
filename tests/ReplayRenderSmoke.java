import com.bridger.momentum.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.*;
import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;

/** Optional offscreen OpenGL smoke test; needs the Minecraft and LWJGL runtime. */
public class ReplayRenderSmoke {
 public static void main(String[] args)throws Exception{
  Pbuffer buffer=new Pbuffer(800,600,new PixelFormat().withDepthBits(24),null,null);
  try{
   buffer.makeCurrent();
   List<ReplayData.Frame> frames=Arrays.asList(new ReplayData.Frame(0,70,0),new ReplayData.Frame(7,73,7));
   List<ReplayData.Block> blocks=new ArrayList<>();
   for(int i=0;i<8;i++)blocks.add(new ReplayData.Block(i,69+i/3,i,24,i==7?1:0));
   BridgeReplayScreen screen=new BridgeReplayScreen(null,new ReplayData(frames,blocks,false));
   screen.f_05230747=800;screen.f_07021018=600;
   Method render=BridgeReplayScreen.class.getDeclaredMethod("renderWorld");render.setAccessible(true);
   Field cursor=BridgeReplayScreen.class.getDeclaredField("cursor");cursor.setAccessible(true);
   for(double tick:new double[]{0,.5,1}){
    GL11.glViewport(0,0,800,600);GL11.glClearColor(0,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
    cursor.setDouble(screen,tick);render.invoke(screen);
    if(GL11.glGetError()!=GL11.GL_NO_ERROR)throw new AssertionError("Replay renderer OpenGL error");
    ByteBuffer pixels=BufferUtils.createByteBuffer(800*600*4);GL11.glReadPixels(0,0,800,600,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
    int colored=0;for(int i=0;i<pixels.limit();i+=4)if(pixels.get(i)!=0||pixels.get(i+1)!=0||pixels.get(i+2)!=0)colored++;
    if(colored<100)throw new AssertionError("Replay viewport is blank");
   }
   System.out.println("PASS: offscreen 3D rendering at start, interpolated frame and end; no OpenGL errors");
  }finally{buffer.destroy();}
 }
}
