package dev.rdh.cera.modules.ctm;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.renderer.block.model.BakedQuad;
/** Emulates a neighbouring-block CTM rule replacing the incoming material. */
public final class ConnectedTextures {
 public Object transformation=new Object();public static int calls,lastNeighbor;
 public List<BakedQuad> transform(IBlockAccess access,IBlockState state,BlockPos pos,List<BakedQuad> input,List<?> overlays,CtmRenderContext context){if(access!=null){calls++;lastNeighbor=net.minecraft.block.Block.func_149682_b(access.func_180495_p(pos.func_177982_a(1,0,0)).func_177230_c());return input;}return Collections.emptyList();}
}
