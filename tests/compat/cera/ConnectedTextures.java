package dev.rdh.cera.modules.ctm;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.renderer.block.model.BakedQuad;
/** Emulates a neighbouring-block CTM rule replacing the incoming material. */
public final class ConnectedTextures {
 public List<BakedQuad> transform(IBlockAccess access,IBlockState state,BlockPos pos,List<BakedQuad> input,List<?> overlays,CtmRenderContext context){return Collections.emptyList();}
}
