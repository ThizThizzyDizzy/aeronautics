package com.thizthizzydizzy.movecraft;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
public class CraftType{
    public final String name;
    public final Set<Material> bannedBlocks = new HashSet<>();
    public final Set<Material> engines = new HashSet<>();
    public final Set<Environment> environments = new HashSet<>();
    public final HashMap<ArrayList<Material>, Float> bannedRatios = new HashMap<>();
    public final HashMap<ArrayList<Material>, Integer> limitedBlocks = new HashMap<>();
    public final HashMap<ArrayList<Material>, Float> requiredRatios = new HashMap<>();
    public int minSize = 10;
    public int maxSize = 10000;
    public int moveTime = 20;
    public int minTime = 2;
    public double enginePercent;
    public final boolean subcraft;
    public int moveDistance = 1;
    public CraftType(String name){
        this(name, false);
    }
    public CraftType(String name, boolean subcraft){
        this.name = name;
        bannedBlocks.add(Material.AIR);
        bannedBlocks.add(Material.VOID_AIR);
        bannedBlocks.add(Material.CAVE_AIR);
        bannedBlocks.add(Material.BEDROCK);
        bannedBlocks.add(Material.STRUCTURE_BLOCK);
        bannedBlocks.add(Material.STRUCTURE_VOID);
        bannedBlocks.add(Material.BARRIER);
        this.subcraft = subcraft;
    }
    public void banBlocks(String categoryName){
        banBlocks(Movecraft.getBlocks(categoryName));
    }
    public void banBlock(String name){
        banBlock(Material.matchMaterial(name));
    }
    public void banBlock(Material block){
        if(bannedBlocks.contains(block))return;
        bannedBlocks.add(block);
    }
    public void banBlocks(Iterable<Material> block){
        for(Material b : block)banBlock(b);
    }
    public void limitBlock(String name, int limit){
        limitBlock(Material.matchMaterial(name), limit);
    }
    public void limitBlock(Material block, int limit){
        ArrayList<Material> m = new ArrayList<>();
        m.add(block);
        limitBlocks(m, limit);
    }
    public void limitBlocks(ArrayList<Material> blocks, int limit){
        limitedBlocks.put(blocks, limit);
    }
    public void addRequiredRatio(ArrayList<Material> blocks, float ratio){
        requiredRatios.put(blocks, ratio);
    }
    public void addBannedRatio(String block, float ratio){
        addBannedRatio(Material.matchMaterial(block), ratio);
    }
    public void addBannedRatio(Material block, float ratio){
        ArrayList<Material> m = new ArrayList<>();
        m.add(block);
        addBannedRatio(m, ratio);
    }
    public void addBannedRatio(ArrayList<Material> blocks, float ratio){
        bannedRatios.put(blocks, ratio);
    }
    public void addEngine(String name){
        addEngine(Material.matchMaterial(name));
    }
    public void addEngine(Material block){
        engines.add(block);
    }
}