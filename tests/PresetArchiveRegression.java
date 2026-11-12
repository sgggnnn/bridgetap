import com.bridger.momentum.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public final class PresetArchiveRegression {
    private static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        Path root=Files.createTempDirectory("preset-sharing-");IslandPresets source=new IslandPresets(root.resolve("source")),target=new IslandPresets(root.resolve("friend"));
        IslandTemplate island=new IslandTemplate("Two islands",22,1,1,0,64,0,Arrays.asList(new IslandTemplate.Block(0,0,0,"minecraft:stone",0,0),new IslandTemplate.Block(21,0,0,"minecraft:sandstone",2,1)),0,1,0,true);
        IslandPresets.Preset preset=source.save("Winter / practice",island,new IslandLayout(1,2,3,4,5,6,7,8,9,1,true));Path archive=IslandPresetArchive.export(preset);check(archive.getFileName().toString().equals("Winter _ practice.btpreset"),"Preset export is not named safely");source.delete(preset);
        IslandPresets.Preset imported=IslandPresetArchive.importFile(archive,target);check(imported.name.equals("Winter / practice")&&imported.layout.finishX==7&&imported.layout.startY==5&&imported.layout.rotation==1&&imported.layout.focus,"Shared layout did not survive detached import");check(imported.template().blocks.get(1).part==1&&imported.template().blocks.get(1).meta==2,"Shared scenery lost part membership or metadata");check(IslandPresetArchive.importFile(archive,target).name.equals("Winter / practice (2)"),"Import overwrote a same-name preset");
        try(ZipFile zip=new ZipFile(archive.toFile())){check(zip.size()==2&&zip.getEntry("preset.properties")!=null&&zip.getEntry("island.btisland")!=null,"Preset archive contains unexpected private/server data");}
        Path attack=root.resolve("unsafe.btpreset");try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(attack))){zip.putNextEntry(new ZipEntry("../escape.txt"));zip.write(1);zip.closeEntry();}try{IslandPresetArchive.importFile(attack,target);throw new AssertionError("Path traversal entry accepted");}catch(java.io.IOException expected){}check(!Files.exists(root.resolve("escape.txt")),"Import escaped its temporary directory");
        Path oversized=root.resolve("oversized.btpreset");try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(oversized))){zip.putNextEntry(new ZipEntry("preset.properties"));zip.write(new byte[65537]);zip.closeEntry();}try{IslandPresetArchive.importFile(oversized,target);throw new AssertionError("Unbounded metadata accepted");}catch(java.io.IOException expected){}check(target.list().size()==2,"Rejected archive partially imported a preset");
        System.out.println("PASS: detached preset sharing, layout/parts persistence, duplicate names and bounded safe imports");
    }
}
