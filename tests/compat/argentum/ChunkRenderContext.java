package dev.rdh.argentum.impl.world.cloned;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

/** Descriptor-compatible cache fixture. Actual Argentum descriptors are checked separately. */
public final class ChunkRenderContext {
    private final Origin origin=new Origin();
    public Origin origin(){return origin;}
    public IBlockState getOriginBlockState(int x,int y,int z){return Blocks.field_150348_b.func_176223_P();}
    public IBlockState getBlockState(int x,int y,int z){return Blocks.field_150348_b.func_176223_P();}
    public static final class Origin {public int minX(){return 0;}public int minY(){return 64;}public int minZ(){return 16;}}
}
