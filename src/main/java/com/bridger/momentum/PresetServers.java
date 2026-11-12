package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Server addresses reference preset UUIDs; island data is never duplicated here. */
public final class PresetServers {
    private final Path file;private final Properties bindings=new Properties();
    public PresetServers(Path file)throws IOException{this.file=file;if(Files.exists(file)){if(Files.size(file)>1048576)throw new IOException("Preset server bindings are too large");try(InputStream in=Files.newInputStream(file)){bindings.load(in);}}}
    public static String canonical(String value){if(value==null)return "";String server=value.trim().toLowerCase(Locale.ROOT);if(server.equals("unknown server"))return "";if(server.endsWith(":25565"))server=server.substring(0,server.length()-6);if(server.endsWith("."))server=server.substring(0,server.length()-1);return server;}
    public String preset(String server){return bindings.getProperty(canonical(server),"");}
    public static final class Anchor{public final int x,y,z;public Anchor(int x,int y,int z){if(Math.abs((long)x)>30000000||Math.abs((long)z)>30000000||y<0||y>255)throw new IllegalArgumentException("Invalid start island");this.x=x;this.y=y;this.z=z;}}
    public Anchor anchor(String server){String key=appearanceKey(server)+".start";try{return new Anchor(Integer.parseInt(bindings.getProperty(key+".x")),Integer.parseInt(bindings.getProperty(key+".y")),Integer.parseInt(bindings.getProperty(key+".z")));}catch(IllegalArgumentException invalid){return null;}}
    public void setAnchor(String server,Anchor anchor)throws IOException{if(canonical(server).isEmpty())throw new IOException("Join a server first");String key=appearanceKey(server)+".start";Properties next=new Properties();next.putAll(bindings);next.setProperty(key+".x",String.valueOf(anchor.x));next.setProperty(key+".y",String.valueOf(anchor.y));next.setProperty(key+".z",String.valueOf(anchor.z));store(next);}
    public static final class Appearance{public final String block;public final int variant;public final boolean enabled;public Appearance(String block,int variant,boolean enabled){if(block==null||!block.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||variant<0||variant>15)throw new IllegalArgumentException("Invalid block appearance");this.block=block;this.variant=variant;this.enabled=enabled;}}
    private static String appearanceKey(String server){return "@appearance."+Base64.getUrlEncoder().withoutPadding().encodeToString(canonical(server).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public Appearance appearance(String server){String key=appearanceKey(server);try{String block=bindings.getProperty(key+".block");return block==null?null:new Appearance(block,Integer.parseInt(bindings.getProperty(key+".variant","0")),Boolean.parseBoolean(bindings.getProperty(key+".enabled","false")));}catch(IllegalArgumentException invalid){return null;}}
    public void assign(String server,String id)throws IOException{assign(server,id,null);}
    public void assign(String server,String id,Appearance appearance)throws IOException{server=canonical(server);if(server.isEmpty())throw new IOException("Join a server first");if(!id.isEmpty())UUID.fromString(id);Properties next=new Properties();next.putAll(bindings);if(id.isEmpty())next.remove(server);else next.setProperty(server,id);String key=appearanceKey(server);for(String suffix:new String[]{".block",".variant",".enabled"})next.remove(key+suffix);if(!id.isEmpty()&&appearance!=null){next.setProperty(key+".block",appearance.block);next.setProperty(key+".variant",String.valueOf(appearance.variant));next.setProperty(key+".enabled",String.valueOf(appearance.enabled));}store(next);}
    private void store(Properties next)throws IOException{Path target=file.toAbsolutePath();Files.createDirectories(target.getParent());Path temp=Files.createTempFile(target.getParent(),"preset-servers-",".part");try{try(OutputStream out=Files.newOutputStream(temp)){next.store(out,"BridgeTap automatic island presets by server");}try{Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}bindings.clear();bindings.putAll(next);}finally{Files.deleteIfExists(temp);}}
}
