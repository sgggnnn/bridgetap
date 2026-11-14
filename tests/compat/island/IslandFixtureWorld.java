package bridgetap.fixture;
import java.util.Map;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
/** Allocated without a constructor. Original terrain is independent of the render mask. */
public final class IslandFixtureWorld extends WorldClient {
 public Map<Long,IBlockState> cells;
 public IslandFixtureWorld(){super(null,null,0,null,null);}
 @Override public IBlockState func_180495_p(BlockPos pos){IBlockState s=cells.get(pos.func_177986_g());return s==null?Block.func_149729_e(0).func_176223_P():s;}
 @Override public boolean func_175667_e(BlockPos pos){return true;}
 @Override public void func_147458_c(int a,int b,int c,int d,int e,int f){}
}
