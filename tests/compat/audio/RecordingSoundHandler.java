package bridgetap.fixture;
import net.minecraft.client.audio.*;
/** Allocated without its constructor: never opens an audio device. */
public final class RecordingSoundHandler extends SoundHandler {
 public ISound last;public int count;
 public RecordingSoundHandler(){super(null,null);}
 @Override public void func_147682_a(ISound sound){last=sound;count++;}
}
