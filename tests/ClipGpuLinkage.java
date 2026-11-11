import com.bridger.momentum.ClipGpuSync;
import java.net.*;
import java.io.File;

/** Resolve real binding signatures without initializing GL or opening a window. */
public final class ClipGpuLinkage {
    public static void main(String[] args)throws Exception{
        URL[] jars=new URL[args.length];for(int i=0;i<args.length;i++)jars[i]=new File(args[i]).toURI().toURL();
        try(URLClassLoader loader=new URLClassLoader(jars,null)){
            Class<?> api=Class.forName("org.lwjgl.opengl.GL32",false,loader);new ClipGpuSync(api);
            System.out.println("PASS: linked real GPU fence API with "+api.getMethod("glFenceSync",int.class,int.class).getReturnType().getName()+" handles without a GL context");
        }
    }
}
