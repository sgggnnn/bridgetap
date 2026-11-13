import com.bridger.momentum.ReplayChunkCoverage;
import java.util.*;
import org.embeddedt.embeddium.impl.render.chunk.map.ChunkTracker;

/** Runs against the actual Celeritas library embedded in OneClient's Argentum. */
public final class ReplayChunkCoverageRegression {
 public static void main(String[] args){
  ChunkTracker sparse=new ChunkTracker();sparse.onChunkStatusAdded(-15,0,3);
  if(!sparse.getReadyChunks().isEmpty())throw new AssertionError("Sparse fixture should reproduce invisible terrain");
  ChunkTracker tracker=new ChunkTracker();Set<Long> loaded=new HashSet<>(),covered=new HashSet<>();
  ReplayChunkCoverage.ensure(loaded,covered,-15,0,(x,z)->tracker.onChunkStatusAdded(x,z,3));
  if(tracker.getReadyChunks().size()!=1)throw new AssertionError("Replay chunk is not ready for terrain/entity visibility");
  int count=loaded.size();ReplayChunkCoverage.ensure(loaded,covered,-15,0,(x,z)->{throw new AssertionError("Duplicate load");});
  if(count!=9||loaded.size()!=count)throw new AssertionError("Incorrect halo");
  ReplayChunkCoverage.ensure(loaded,covered,-14,0,(x,z)->tracker.onChunkStatusAdded(x,z,3));
  if(loaded.size()!=12||tracker.getReadyChunks().size()!=2)throw new AssertionError("Adjacent replay chunks missing/duplicated");
  tracker.setRequiredNeighborRadius(1);
  if(tracker.getReadyChunks().size()!=2)throw new AssertionError("Reload discarded ready replay chunks");
  System.out.println("PASS: reproduced sparse-world invisibility with real Argentum/Celeritas tracker; replay halo restores terrain/entity readiness and deduplicates loads");
 }
}
