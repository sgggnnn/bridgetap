# Replay chunk readiness regression

`ReplayChunkCoverageRegression.java` runs against the real `ChunkTracker` from
Argentum's nested `META-INF/jars/celeritas-common-2.5.0-pre.1.jar`, plus FastUtil.
These optional third-party libraries are not bundled in BridgeTap.

Compile this test and `src/main/java/com/bridger/momentum/ReplayChunkCoverage.java`
with both libraries on the classpath, then run `ReplayChunkCoverageRegression`
with the compiled classes and the same libraries. Use Java 21 for the current
Argentum library. The BridgeTap helper itself is compiled with `--release 8`.

The test reproduces an unready isolated replay chunk, verifies that loading the
neighbor ring makes it ready for terrain/entity visibility, and checks adjacent
chunks, negative coordinates and duplicate-load suppression. This validates the
chunk-readiness cause without requiring OpenGL; shader output still requires an
in-game check.
