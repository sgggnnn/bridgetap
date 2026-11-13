package bridgetap.fixture;
import net.minecraft.client.gui.FontRenderer;
/** Allocated without its constructor; counts width work without OpenGL. */
public final class CountingFontRenderer extends FontRenderer {
 public int calls;
 public CountingFontRenderer(){super(null,null,null,false);}
 @Override public int func_78256_a(String value){calls++;return value.length()*6;}
}
