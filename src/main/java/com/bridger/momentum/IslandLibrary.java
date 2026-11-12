package com.bridger.momentum;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Internal UUID paths; exported filenames identify the island being shared. */
public final class IslandLibrary {
    public static void rename(Path path,String name)throws IOException{
        Path root=folder("saved").toRealPath(),target=path.toAbsolutePath().normalize();
        if(!target.getParent().equals(root)||!target.getFileName().toString().endsWith(".btisland")||!Files.isRegularFile(target,LinkOption.NOFOLLOW_LINKS))throw new IOException("Select a saved island first");
        IslandTemplate old=IslandTemplate.read(target),renamed=new IslandTemplate(name,old.width,old.height,old.depth,old.originX,old.originY,old.originZ,old.blocks,old.anchorX,old.anchorY,old.anchorZ,old.bothIslands);
        Path temp=root.resolve(UUID.randomUUID()+".part");try{renamed.write(temp);try{Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}
    }
    public static void delete(Path path,String kind)throws IOException{
        if(!kind.equals("saved")&&!kind.equals("imports"))throw new IOException("Invalid island library");
        Path root=folder(kind).toRealPath(),target=path.toAbsolutePath().normalize();
        if(!target.getParent().equals(root)||!target.getFileName().toString().endsWith(".btisland")||!Files.isRegularFile(target,LinkOption.NOFOLLOW_LINKS))throw new IOException("Select an island in this library");
        Files.delete(target);
    }
    public static Path folder(String kind)throws IOException{Path p=Paths.get("config","bridgetap","islands",kind);Files.createDirectories(p);return p;}
    public static List<Path> list(String kind)throws IOException{
        Path root=folder(kind);if(kind.equals("defaults"))DefaultIslands.install(root);
        List<Path> result=new ArrayList<>();try(DirectoryStream<Path> stream=Files.newDirectoryStream(root,"*.btisland")){
            for(Path file:stream)if(Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)){result.add(file);if(result.size()>=1000)break;}
        }
        result.sort(Comparator.comparing(p->p.getFileName().toString()));return result;
    }
    public static Path save(IslandTemplate template,String kind)throws IOException{
        if(kind.equals("exports"))return export(template);
        Path temp=folder(kind).resolve(UUID.randomUUID()+".part"),target=folder(kind).resolve(UUID.randomUUID()+".btisland");
        try{template.write(temp);Files.move(temp,target);return target;}finally{Files.deleteIfExists(temp);}
    }
    private static Path export(IslandTemplate template)throws IOException{
        Path root=folder("exports"),temp=root.resolve(UUID.randomUUID()+".part");
        try{template.write(temp);return moveNamed(temp,root,template.name);}finally{Files.deleteIfExists(temp);}
    }
    static String safeFileName(String title){
        String name=title.replaceAll("[<>:\"/\\\\|?*\\p{Cntrl}]","_").replaceAll("[ .]+$","").trim();
        if(name.isEmpty())name="Island";
        if(name.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?"))name="_"+name;
        return name;
    }
    private static Path moveNamed(Path source,Path root,String title)throws IOException{
        String name=safeFileName(title);
        for(int n=1;n<=10000;n++){
            Path target=root.resolve(name+(n==1?"":" ("+n+")")+".btisland");
            try{Files.move(source,target);return target;}catch(FileAlreadyExistsException duplicate){}
        }throw new IOException("Too many exports with this name");
    }
    public static void nameLegacyExports()throws IOException{
        Path root=folder("exports");int count=0;
        try(DirectoryStream<Path> files=Files.newDirectoryStream(root,"*.btisland")){
            for(Path file:files){if(++count>1000)break;
                if(!file.getFileName().toString().matches("[a-fA-F0-9]{8}(-[a-fA-F0-9]{4}){3}-[a-fA-F0-9]{12}\\.btisland")||!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))continue;
                try{moveNamed(file,root,IslandTemplate.readName(file));}catch(IOException invalid){/* Leave unreadable exports untouched. */}
            }
        }
    }
    public static Path importFile(Path path)throws IOException{return save(IslandTemplate.read(path),"saved");}
}
