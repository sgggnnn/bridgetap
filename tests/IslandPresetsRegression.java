import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;

public final class IslandPresetsRegression {
    private static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        Path folder=Files.createTempDirectory("bridgetap-presets-");IslandPresets library=new IslandPresets(folder);IslandTemplate template=new IslandTemplate("Original",22,1,1,0,64,0,Arrays.asList(new IslandTemplate.Block(0,0,0,"minecraft:stone",0,0),new IslandTemplate.Block(21,0,0,"minecraft:sandstone",2,1)),0,1,0,true);
        IslandLayout layout=new IslandLayout(1,2,3,4,5,6,30,4,-2,1,true);library.save("My islands",template,layout);IslandPresets.Preset saved=new IslandPresets(folder).list().get(0);check(saved.name.equals("My islands")&&saved.layout.finishX==30&&saved.layout.startY==5&&saved.layout.rotation==1&&saved.layout.focus,"Preset layout did not survive restart");check(saved.template().blocks.get(1).part==1&&saved.template().blocks.get(1).meta==2,"Preset lost its independent template copy or island groups");
        try{library.save("my islands",template,layout);throw new AssertionError("Duplicate preset name accepted");}catch(java.io.IOException expected){}
        check(saved.layout.stableAnchor,"New preset did not retain its fixed geometry anchor");
        Path metadata=Files.list(folder).filter(p->p.toString().endsWith(".properties")).findFirst().get();
        Properties legacy=new Properties();try(java.io.InputStream in=Files.newInputStream(metadata)){legacy.load(in);}legacy.setProperty("version","1");try(java.io.OutputStream out=Files.newOutputStream(metadata)){legacy.store(out,"legacy fixture");}
        check(!new IslandPresets(folder).list().get(0).layout.stableAnchor,"Legacy preset changed coordinate systems without migration");
        Path exported=IslandLibrary.save(template,"exports"),duplicate=IslandLibrary.save(template,"exports");
        check(exported.getFileName().toString().equals("Original.btisland")&&duplicate.getFileName().toString().equals("Original (2).btisland"),"Export filenames do not identify islands or overwrite an earlier export");
        check(IslandTemplate.read(exported).name.equals("Original"),"Named export changed template contents");
        Path oldExport=IslandLibrary.folder("exports").resolve(UUID.randomUUID()+".btisland");template.write(oldExport);IslandLibrary.nameLegacyExports();check(!Files.exists(oldExport)&&Files.exists(IslandLibrary.folder("exports").resolve("Original (3).btisland")),"Legacy UUID export was not renamed without overwriting existing files");
        IslandTemplate unsafe=new IslandTemplate("CON",22,1,1,0,64,0,template.blocks,0,1,0,true);
        check(IslandLibrary.save(unsafe,"exports").getFileName().toString().equals("_CON.btisland"),"Reserved Windows export filename was not escaped");
        library.delete(saved);check(library.list().isEmpty()&&Files.list(folder).count()==0,"Preset deletion left its asset behind");
        try{new IslandLayout(0,Integer.MIN_VALUE,0,0,0,0,0,0,0,0,false);throw new AssertionError("Invalid preset height accepted");}catch(IllegalArgumentException expected){}
        System.out.println("PASS: named island presets preserve independent template data, relative positions, orientation and focus mode");
    }
}
