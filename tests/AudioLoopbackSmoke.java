import com.bridger.momentum.*;
import java.nio.file.*;

/** Explicit Windows-only check; not part of automatic unit runs because it records output audio. */
public final class AudioLoopbackSmoke {
    public static void main(String[] args)throws Exception{
        Path folder=Paths.get(args[0]);Files.createDirectories(folder);long start=System.nanoTime();
        ClipAudio.capture(true);
        try{
            Thread.sleep(700);Path wave=ClipAudio.snapshot(start,System.nanoTime(),folder);byte[] bytes=Files.readAllBytes(wave);
            if(bytes.length<1000||bytes[0]!='R'||bytes[1]!='I'||bytes[8]!='W')throw new AssertionError("WASAPI did not return a valid audio snapshot");
            System.out.println("PASS: Java controller, bundled WASAPI helper and synchronized WAV snapshot ("+bytes.length+" bytes)");
            Files.delete(wave);
        }finally{ClipAudio.capture(false);}
    }
}
