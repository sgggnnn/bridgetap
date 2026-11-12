package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** A detached scenery/layout archive; no server bindings or executable content. */
public final class IslandPresetArchive {
    private static final int MAX=33*1024*1024;
    public static Path folder(String kind)throws IOException{Path path=Paths.get("config","bridgetap","island-presets",kind);Files.createDirectories(path);return path;}
    private static Properties metadata(String name,IslandLayout layout){Properties p=new Properties();p.setProperty("version",layout.stableAnchor?"2":"1");p.setProperty("name",name);String[] keys={"x","y","z","sx","sy","sz","fx","fy","fz","rotation"};int[] values={layout.x,layout.y,layout.z,layout.startX,layout.startY,layout.startZ,layout.finishX,layout.finishY,layout.finishZ,layout.rotation};for(int i=0;i<keys.length;i++)p.setProperty(keys[i],String.valueOf(values[i]));p.setProperty("focus",String.valueOf(layout.focus));return p;}
    public static Path export(IslandPresets.Preset preset)throws IOException{
        Path root=folder("exports"),scratch=Files.createTempDirectory(root,".preset-");Path island=scratch.resolve("island.btisland"),output=null;
        try{preset.template().write(island);String stem=IslandLibrary.safeFileName(preset.name);for(int n=1;n<10000;n++){Path candidate=root.resolve(stem+(n==1?"":" ("+n+")")+".btpreset");try{Files.createFile(candidate);output=candidate;break;}catch(FileAlreadyExistsException duplicate){}}if(output==null)throw new IOException("Too many preset exports with this name");
            try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(output))){zip.putNextEntry(new ZipEntry("preset.properties"));ByteArrayOutputStream props=new ByteArrayOutputStream();metadata(preset.name,preset.layout).store(props,"BridgeTap portable island preset");zip.write(props.toByteArray());zip.closeEntry();zip.putNextEntry(new ZipEntry("island.btisland"));Files.copy(island,zip);zip.closeEntry();}return output;
        }catch(IOException error){if(output!=null)Files.deleteIfExists(output);throw error;}finally{Files.deleteIfExists(island);Files.deleteIfExists(scratch);}
    }
    public static IslandPresets.Preset importFile(Path archive,IslandPresets library)throws IOException{
        if(!Files.isRegularFile(archive,LinkOption.NOFOLLOW_LINKS)||Files.size(archive)>MAX)throw new IOException("Invalid or oversized preset archive");
        Path scratch=Files.createTempDirectory("bridgetap-preset-import-");Path island=scratch.resolve("island.btisland");Properties props=new Properties();Set<String> entries=new HashSet<>();
        try{
            try(ZipInputStream zip=new ZipInputStream(Files.newInputStream(archive))){ZipEntry entry;while((entry=zip.getNextEntry())!=null){String name=entry.getName();if(entry.isDirectory()||(!name.equals("preset.properties")&&!name.equals("island.btisland"))||!entries.add(name))throw new IOException("Unexpected preset archive entry");int limit=name.equals("preset.properties")?65536:32*1024*1024;ByteArrayOutputStream header=name.equals("preset.properties")?new ByteArrayOutputStream():null;try(OutputStream out=header!=null?header:Files.newOutputStream(island,StandardOpenOption.CREATE_NEW)){byte[] bytes=new byte[8192];int n,total=0;while((n=zip.read(bytes))!=-1){total+=n;if(total>limit)throw new IOException("Preset archive entry is too large");out.write(bytes,0,n);}}if(header!=null)props.load(new ByteArrayInputStream(header.toByteArray()));}}
            if(entries.size()!=2)throw new IOException("Preset archive is incomplete");String version=props.getProperty("version","");if(!version.equals("1")&&!version.equals("2"))throw new IOException("Unsupported preset format");
            IslandLayout layout=new IslandLayout(number(props,"x"),number(props,"y"),number(props,"z"),number(props,"sx"),number(props,"sy"),number(props,"sz"),number(props,"fx"),number(props,"fy"),number(props,"fz"),number(props,"rotation"),Boolean.parseBoolean(props.getProperty("focus","false")),version.equals("2"));
            String name=RunHistory.validName(props.getProperty("name",""));if(name.isEmpty())throw new IOException("Preset has no name");Set<String> used=new HashSet<>();for(IslandPresets.Preset preset:library.list())used.add(preset.name.toLowerCase(Locale.ROOT));String candidate=name;for(int n=2;used.contains(candidate.toLowerCase(Locale.ROOT));n++){String suffix=" ("+n+")";candidate=name.substring(0,Math.min(name.length(),64-suffix.length()))+suffix;}
            return library.save(candidate,IslandTemplate.read(island),layout);
        }catch(IllegalArgumentException invalid){throw new IOException("Invalid preset metadata",invalid);}finally{Files.deleteIfExists(island);Files.deleteIfExists(scratch);}
    }
    private static int number(Properties p,String key){return Integer.parseInt(p.getProperty(key,"0"));}
}
