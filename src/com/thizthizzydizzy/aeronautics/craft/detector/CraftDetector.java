package com.thizthizzydizzy.aeronautics.craft.detector;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftType;
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
    public abstract CraftDetector newInstance();
    public abstract void load(JSON.JSONObject json);
    public abstract Craft detect(CraftType type, Player player, Block origin);
}