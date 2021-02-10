package com.thizthizzydizzy.movecraft.craft.detector;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public abstract class CraftDetector{
    private final String name;
    public CraftDetector(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public abstract Craft detect(CraftType type, Player player, Block origin);
}