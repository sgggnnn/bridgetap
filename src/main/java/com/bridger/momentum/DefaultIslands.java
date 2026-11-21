package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;

/** Bundled static scenery is copied locally for the existing Path-based library. */
public final class DefaultIslands {
    private static final String[] FILES={"Anniversary.btisland","Default.btisland","Fall Island.btisland","Halloween.btisland","Speedcubing.btisland","Summer.btisland","Winter.btisland"};
    private DefaultIslands(){}
    public static synchronized void install(Path folder)throws IOException{
        Files.createDirectories(folder);
        moveLegacyBastion(folder);
        for(String name:FILES){
            Path target=folder.resolve(name);
            if(Files.exists(target,LinkOption.NOFOLLOW_LINKS))continue;
            Path temp=Files.createTempFile(folder,"default-island-",".part");
            try{
                try(InputStream in=DefaultIslands.class.getResourceAsStream("/assets/bridgetap/islands/defaults/"+name)){
                    if(in==null)throw new IOException("Missing bundled island: "+name);
                    Files.copy(in,temp,StandardCopyOption.REPLACE_EXISTING);
                }
                IslandTemplate.read(temp); // Validate before adding a partially installed file to the library.
                try{Files.move(temp,target);}catch(FileAlreadyExistsException concurrentInstall){/* Keep the existing local file. */}
            }finally{Files.deleteIfExists(temp);}
        }
    }
    /** Move only the exact previously bundled sample; preserve edited/personal files. */
    private static void moveLegacyBastion(Path folder)throws IOException{
        Path old=folder.resolve("Bastion.btisland");
        if(!Files.isRegularFile(old,LinkOption.NOFOLLOW_LINKS)||Files.size(old)>32768)return;
        byte[] original;
        try(InputStream in=DefaultIslands.class.getResourceAsStream("/assets/bridgetap/islands/examples/Bastion.btisland")){
            if(in==null)return;ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);original=bytes.toByteArray();
        }
        if(!java.util.Arrays.equals(original,Files.readAllBytes(old)))return;
        Path imports=folder.resolveSibling("imports");Files.createDirectories(imports);
        Path target=imports.resolve("Bastion.btisland");if(Files.exists(target,LinkOption.NOFOLLOW_LINKS))target=imports.resolve("Bastion-"+java.util.UUID.randomUUID()+".btisland");
        Files.move(old,target);
    }

}
