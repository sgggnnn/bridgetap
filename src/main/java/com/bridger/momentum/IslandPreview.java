package com.bridger.momentum;

/** Compact top view of the selected template, also usable by headless UI previews. */
public final class IslandPreview {
    private final int[][] color;private final int columns,rows;
    public IslandPreview(IslandTemplate island){double factor=Math.min(1,32.0/Math.max(island.width,island.depth));columns=Math.max(1,(int)Math.round(island.width*factor));rows=Math.max(1,(int)Math.round(island.depth*factor));color=new int[rows][columns];int[][] height=new int[rows][columns];for(int[] row:height)java.util.Arrays.fill(row,-1);
        for(IslandTemplate.Block b:island.blocks){int x=b.x*columns/island.width,z=b.z*rows/island.depth;if(b.y>=height[z][x]){height[z][x]=b.y;color[z][x]=material(b.key);}}
    }
    public void paint(UiCanvas ui,int x,int y,int width,int height){
        ui.rect(x,y,width,height,0xFF101A27);int size=Math.max(1,Math.min((width-16)/columns,(height-32)/rows)),ox=x+(width-size*columns)/2,oy=y+24+(height-32-size*rows)/2;
        for(int z=0;z<rows;z++)for(int a=0;a<columns;){int c=color[z][a],end=a+1;while(end<columns&&color[z][end]==c)end++;if(c!=0)ui.rect(ox+a*size,oy+z*size,(end-a)*size,size,0xFF000000|c);a=end;}
        ui.text("Top view",x+8,y+7,0xFF94A4BA);
    }
    private static int material(String key){if(key.contains("sand"))return 0xD5C18D;if(key.contains("grass")||key.contains("leaves"))return 0x65A64D;if(key.contains("prismarine"))return 0x47998C;if(key.contains("dirt"))return 0x885B3C;if(key.contains("wood")||key.contains("planks")||key.contains("log"))return 0xA47D4D;if(key.contains("quartz")||key.contains("snow"))return 0xDFE5E6;if(key.contains("wool"))return 0xC7B8C9;if(key.contains("glass"))return 0x7DAFB8;return 0x8E969E;}
}
