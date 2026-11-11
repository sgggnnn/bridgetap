import com.bridger.momentum.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public final class IslandRegression {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        List<IslandTemplate.Block> blocks=Arrays.asList(new IslandTemplate.Block(0,0,0,"minecraft:sandstone",2),new IslandTemplate.Block(2,1,3,"minecraft:stained_glass",14));
        IslandTemplate island=new IslandTemplate("Île / practice",3,2,4,-12,64,30,blocks);
        Path p=IslandLibrary.save(island,"saved");IslandTemplate read=IslandTemplate.read(p);
        check(read.name.equals(island.name)&&read.originX==-12&&read.blocks.get(1).meta==14&&read.blocks.get(1).key.equals("minecraft:stained_glass"),"Portable block names/metadata and anchor round trip");
        check(IslandTemplate.readName(p).equals(island.name),"Library header reader");
        Path export=IslandLibrary.save(read,"exports"),copy=IslandLibrary.importFile(export);
        check(!copy.equals(p)&&IslandTemplate.read(copy).blocks.size()==2&&IslandLibrary.list("saved").size()==2,"Friend import preserves data and creates a separate safe file");
        check(export.getFileName().toString().equals("Île _ practice.btisland")&&export.toAbsolutePath().getParent().equals(IslandLibrary.folder("exports").toAbsolutePath()),"Named exports must sanitize separators and stay in the export folder");
        try{new IslandTemplate("bad",3,2,4,0,64,0,Arrays.asList(blocks.get(0),blocks.get(0)));throw new AssertionError("Duplicate coordinate accepted");}catch(IllegalArgumentException expected){}
        try{new IslandTemplate("bad",512,256,512,0,0,0,blocks);throw new AssertionError("Excessive volume accepted");}catch(IllegalArgumentException expected){}
        try{new IslandTemplate("bad",3,2,4,0,64,0,Arrays.asList(new IslandTemplate.Block(4,0,0,"minecraft:stone",0)));throw new AssertionError("Out-of-bounds block accepted");}catch(IllegalArgumentException expected){}
        Path malformed=Paths.get("bad.btisland");try(DataOutputStream out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(malformed)))){out.writeInt(0x42544953);out.writeInt(1);out.writeUTF("bad");for(int n:new int[]{1,1,1,0,64,0,Integer.MAX_VALUE})out.writeInt(n);}
        try{IslandTemplate.read(malformed);throw new AssertionError("Unbounded block count accepted");}catch(IOException expected){}
        byte[] data=Files.readAllBytes(p);Files.write(malformed,Arrays.copyOf(data,data.length/2));try{IslandTemplate.read(malformed);throw new AssertionError("Truncated island accepted");}catch(IOException expected){}
        List<ReplayData.Block> terrain=new ArrayList<>();
        for(int x=-2;x<=0;x++)for(int z=-1;z<=1;z++)terrain.add(new ReplayData.Block(x,64,z,24,2,0,false));
        for(int x=8;x<=10;x++)for(int z=-1;z<=1;z++)terrain.add(new ReplayData.Block(x,65,z,168,0,0,false));
        for(int x=1;x<8;x++)terrain.add(new ReplayData.Block(x,64,0,24,0,1,true));
        terrain.add(new ReplayData.Block(1,64,0,24,0,0,false)); // a scan/placement overlap must not reconnect islands
        ReplayData recording=new ReplayData(Arrays.asList(new ReplayData.Frame(-.5,65,.5),new ReplayData.Frame(8.5,66,.5)),terrain,false);
        List<ReplayData.Block> start=ReplayIslandSelection.select(recording,false,b->true),finish=ReplayIslandSelection.select(recording,true,b->true);
        check(start.size()==9&&finish.size()==9&&start.stream().allMatch(b->b.x<=0)&&finish.stream().allMatch(b->b.x>=8),"Replay island extraction excludes the built bridge and the other platform");
        IslandLibrary.delete(copy,"saved");check(!Files.exists(copy)&&Files.exists(p),"Delete removes only the selected island");
        try{IslandLibrary.delete(export,"saved");throw new AssertionError("Deletion escaped the selected library");}catch(IOException expected){}
        IslandTemplate anchored=new IslandTemplate("Attached",3,2,4,-12,64,30,blocks,1,1,2);Path attached=IslandLibrary.save(anchored,"saved");IslandTemplate attachment=IslandTemplate.read(attached);check(attachment.anchorX==1&&attachment.anchorY==1&&attachment.anchorZ==2,"Platform attachment point survives export");
        check(ReplayIslandSelection.both(recording,b->true).size()==18,"One-button copy must preserve both islands, without bridge placements");
        List<ReplayData.Block> early=new ArrayList<>();for(ReplayData.Block b:terrain)if(!b.placed||b.x<=2)early.add(b);
        ReplayData failed=new ReplayData(Arrays.asList(new ReplayData.Frame(-.5,65,.5),new ReplayData.Frame(2.5,30,.5)),early,false);
        check(ReplayIslandSelection.both(failed,b->true).size()==18,"Finish island selection must not depend on the final frame being on the platform");
        for(int axis=0;axis<2;axis++)for(boolean reverse:new boolean[]{false,true}){
            List<IslandTemplate.Block> pair=new ArrayList<>();for(int along:new int[]{0,1,20,21})for(int side=0;side<3;side++)pair.add(new IslandTemplate.Block(axis==0?along:side,0,axis==0?side:along,"minecraft:stone",0));
            IslandTemplate paired=new IslandTemplate("Pair",axis==0?22:3,1,axis==0?3:22,0,64,0,pair,axis==0?(reverse?21:0):1,1,axis==1?(reverse?21:0):1,true);
            IslandParts parts=new IslandParts(paired);check(parts.paired&&parts.axis==axis&&parts.direction==(reverse?-1:1),"Island separation must respect X/Z and negative bridge directions");
            for(int i=0;i<pair.size();i++){int along=axis==0?pair.get(i).x:pair.get(i).z;check(parts.finish[i]==(reverse?along<2:along>=20),"Start blocks were assigned to the finish island");}
            check(parts.gap(0,0)==18&&parts.gap(axis==0?parts.direction*7:0,axis==1?parts.direction*7:0)==25,"Independent finish movement must adjust the platform gap");
        }
        check(!new IslandParts(island).paired,"Single-island copies must not enable finish controls");
        for(int turn=0;turn<4;turn++){int[] rotated=IslandRotation.point(-20,31,2,-3,turn),restored=IslandRotation.point(rotated[0],rotated[1],2,-3,-turn);check(restored[0]==-20&&restored[1]==31,"Orientation must round-trip without moving the attachment point");int[] fixed=IslandRotation.point(2,-3,2,-3,turn);check(fixed[0]==2&&fixed[1]==-3,"Rotation moved the start attachment point");int[] bounds=IslandRotation.bounds(new int[]{-20,0,-10,30,5,40},2,-3,turn);check((bounds[3]-bounds[0])*(bounds[5]-bounds[2])==2500,"Rotated copy/render bounds changed footprint area");}
        IslandLibrary.rename(p,"Renamed island");IslandTemplate renamed=IslandTemplate.read(p);check(renamed.name.equals("Renamed island")&&renamed.blocks.size()==island.blocks.size()&&renamed.originX==island.originX,"Rename must keep the same file, geometry and position");
        try{IslandLibrary.rename(p,"");throw new AssertionError("Blank island name accepted");}catch(IllegalArgumentException good){}
        check(IslandTemplate.read(p).name.equals("Renamed island"),"Failed rename damaged the original island");
        try{IslandLibrary.rename(export,"Wrong folder");throw new AssertionError("Rename escaped the saved library");}catch(IOException good){}
        IslandBox box=new IslandBox(0,64,0,10,10,10);box.move(2,500,-3);check(box.y==246&&box.x==2&&box.z==-3,"Copy box stays in world height");box.resize(0,200);check(box.width==210&&box.valid(),"Copy box supports large islands");
        List<IslandTemplate.Block> large=new ArrayList<>();for(int part=0;part<2;part++)for(int x=0;x<40;x++)for(int y=0;y<25;y++)for(int z=0;z<20;z++)large.add(new IslandTemplate.Block(part*200+x,y,z,"minecraft:stone",0,part));
        IslandTemplate big=new IslandTemplate("Large paired",240,25,20,0,50,0,large,20,25,10,true);Path bigFile=IslandLibrary.save(big,"saved");IslandTemplate loaded=IslandTemplate.read(bigFile);IslandParts bigParts=new IslandParts(loaded);check(loaded.blocks.size()==40000&&loaded.blocks.get(20000).x==200&&bigParts.paired&&bigParts.gap(5,0)==165,"Large v4 paired scenery lost coordinates, part membership or distance controls");
        List<IslandTemplate.Block> untagged=new ArrayList<>();for(IslandTemplate.Block b:large)untagged.add(new IslandTemplate.Block(b.x,b.y,b.z,b.key,b.meta));check(new IslandParts(new IslandTemplate("Manual paired copy",240,25,20,0,50,0,untagged,20,25,10,false)).paired,"Large manual/older copies with two clear islands should enable distance controls");
        List<IslandTemplate.Block> packed=Arrays.asList(new IslandTemplate.Block(0,0,0,"minecraft:stone",0,0),new IslandTemplate.Block(1,0,0,"minecraft:stone",0,1),new IslandTemplate.Block(0,0,1,"minecraft:stone",0,1),new IslandTemplate.Block(1,0,1,"minecraft:stone",0,0));IslandParts exact=new IslandParts(new IslandTemplate("No corridor",2,1,2,0,64,0,packed,0,1,0,true));check(exact.paired&&!exact.finish[0]&&exact.finish[1]&&exact.finish[2]&&!exact.finish[3],"Tagged islands must stay independently editable without an empty row or column");
        IslandMaskArea area=new IslandMaskArea(30,40,-8,45,80,8);check(area.contains(40,70,0)&&!area.contains(29,70,0)&&!area.contains(40,81,0),"Area masks must include disconnected/late island cells but exclude the surrounding bridge");check(area.covers(new IslandMaskArea(31,50,-2,40,70,2)),"Replacement coverage comparison");
        System.out.println("PASS: portable island templates, Unicode names, safe detached sharing, block validation and bounded malformed input");
    }
}
