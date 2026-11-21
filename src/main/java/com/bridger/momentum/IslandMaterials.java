package com.bridger.momentum;

import java.util.*;

/** Palette edits preserve geometry, attachment points and the two island roles. */
public final class IslandMaterials {
    private IslandMaterials(){}
    public static List<String> palette(IslandTemplate island){
        Set<String> keys=new TreeSet<>();for(IslandTemplate.Block block:island.blocks)keys.add(block.key);
        return new ArrayList<>(keys);
    }
    public static boolean modern(String key){return key.startsWith("modern:");}
    public static IslandTemplate replace(IslandTemplate source,String name,String from,String to,int metadata){
        if(!palette(source).contains(from))throw new IllegalArgumentException("Choose an island material first");
        List<IslandTemplate.Block> blocks=new ArrayList<>();
        for(IslandTemplate.Block b:source.blocks){boolean replace=b.key.equals(from);blocks.add(new IslandTemplate.Block(b.x,b.y,b.z,replace?to:b.key,replace?metadata:b.meta,b.part));}
        return new IslandTemplate(name,source.width,source.height,source.depth,source.originX,source.originY,source.originZ,blocks,source.anchorX,source.anchorY,source.anchorZ,source.bothIslands);
    }
}
