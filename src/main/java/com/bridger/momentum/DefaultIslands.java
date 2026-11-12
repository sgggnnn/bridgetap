package com.bridger.momentum;

import java.io.*;
import java.nio.file.*;

/** Bundled static scenery is copied locally for the existing Path-based library. */
public final class DefaultIslands {
    private static final String[] FILES={"Default.btisland","Fall Island.btisland","Halloween.btisland","Speedcubing.btisland","Summer.btisland","Winter.btisland"};
    private DefaultIslands(){}
    public static synchronized void install(Path folder)throws IOException{
        Files.createDirectories(folder);
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
}
