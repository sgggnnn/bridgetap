package net.ornithemc.osl.resource.loader.api.resource.manager;
/** Minimal contract fixture for the optional reflection bridge; never packaged in the mod. */
public final class ResourceManager {
 private static final ResourceManager INSTANCE=new ResourceManager();
 public static ResourceManager client(){return INSTANCE;}
}
