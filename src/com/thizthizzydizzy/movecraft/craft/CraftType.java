package com.thizthizzydizzy.movecraft.craft;
import com.thizthizzydizzy.movecraft.craft.engine.Engine;
import com.thizthizzydizzy.movecraft.craft.special.Special;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Material;
public class CraftType{
    private final String name;
    private String displayName;
    private final int minSize;
    private final int maxSize;
    public ArrayList<Engine> engines = new ArrayList<>();
    public ArrayList<Special> specials = new ArrayList<>();
    public HashSet<Material> allowedBlocks = new HashSet<>();
    public HashSet<Material> bannedBlocks = new HashSet<>();
    public CraftType(String name, int minSize, int maxSize){
        this.name = name;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }
    public String getName(){
        return name;
    }
    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }
    public String getDisplayName(){
        return displayName==null?name:displayName;
    }
}