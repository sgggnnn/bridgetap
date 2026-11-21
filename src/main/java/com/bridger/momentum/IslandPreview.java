package com.bridger.momentum;
/** Compact top view of the selected template, also usable by headless UI previews. */
public final class IslandPreview {
    private final java.util.List<IslandTemplate.Block> voxels;private final IslandTemplate template;
    private final int[][] color;private final int columns,rows;
    public IslandPreview(IslandTemplate island){template=island;voxels=new java.util.ArrayList<>(island.blocks);voxels.sort(java.util.Comparator.comparingInt((IslandTemplate.Block b)->b.x+b.z).thenComparingInt(b->b.y));double factor=Math.min(1,32.0/Math.max(island.width,island.depth));columns=Math.max(1,(int)Math.round(island.width*factor));rows=Math.max(1,(int)Math.round(island.depth*factor));color=new int[rows][columns];int[][] height=new int[rows][columns];for(int[] row:height)java.util.Arrays.fill(row,-1);
        for(IslandTemplate.Block b:island.blocks){int x=b.x*columns/island.width,z=b.z*rows/island.depth;if(b.y>=height[z][x]){height[z][x]=b.y;color[z][x]=material(b.key,b.meta);}}
    }
    public void paint(UiCanvas ui,int x,int y,int width,int height){
        paint(ui,x,y,width,height,"Top view");
    }
    public void paint(UiCanvas ui,int x,int y,int width,int height,String title){
        ui.rect(x,y,width,height,0xFF101A27);int size=Math.max(1,Math.min((width-16)/columns,(height-32)/rows)),ox=x+(width-size*columns)/2,oy=y+24+(height-32-size*rows)/2;
        for(int z=0;z<rows;z++)for(int a=0;a<columns;){int c=color[z][a],end=a+1;while(end<columns&&color[z][end]==c)end++;if(c!=0)ui.rect(ox+a*size,oy+z*size,(end-a)*size,size,0xFF000000|c);a=end;}
        ui.text(title,x+8,y+7,0xFF94A4BA);
    }
    /** Isometric voxel preview of the full height, not just the top surface. */
    public void paintIsometric(UiCanvas ui,int x,int y,int width,int height,String title){
        ui.rect(x,y,width,height,0xFF101A27);new TutorialScene().paintIsland(ui,template,x+8,y+22,width-16,height-28);ui.text(title,x+8,y+7,UiTheme.SECONDARY);
    }
    static int material(String key,int meta){
        if(key.contains("wool")||key.contains("carpet")||key.contains("stained_")){int[] dyes={0xE6E5DC,0xD97B27,0xB150BC,0x70AAD6,0xE8CA3D,0x7FB243,0xDD91AB,0x4A4E50,0x969C9D,0x368A94,0x7B4AA8,0x374A9E,0x755439,0x4E7136,0xAB3C34,0x292D32};return dyes[meta&15];}
        if(key.contains("cherry"))return 0xE8B5B2;if(key.contains("pale_oak"))return 0xCBC5AE;if(key.contains("bamboo"))return 0xC6AE55;if(key.contains("amethyst"))return 0x9B78BC;if(key.contains("copper"))return key.contains("oxidized")?0x549B81:0xB86F4F;
if(key.contains("gold")||key.contains("gilded"))return 0xC79832;if(key.contains("blackstone"))return 0x39323E;if(key.contains("basalt"))return 0x50515B;if(key.contains("obsidian"))return 0x4E2868;if(key.contains("nether_brick"))return 0x3E202B;if(key.contains("crimson"))return 0x9D253B;if(key.contains("sand"))return 0xD5C18D;if(key.contains("grass")||key.contains("leaves"))return 0x65A64D;if(key.contains("prismarine"))return 0x47998C;if(key.contains("dirt"))return 0x885B3C;if(key.contains("wood")||key.contains("planks")||key.contains("log"))return 0xA47D4D;if(key.contains("quartz")||key.contains("snow"))return 0xDFE5E6;if(key.contains("wool"))return 0xC7B8C9;if(key.contains("glass"))return 0x7DAFB8;return 0x8E969E;}
}
