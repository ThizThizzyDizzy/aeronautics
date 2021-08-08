package com.thizthizzydizzy.aeronautics.craft.collision_handler;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.Medium;
import java.util.List;
import java.util.Collection;
import org.bukkit.block.Block;
public abstract class CollisionHandler{
    private final String name;
    public CollisionHandler(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public abstract CollisionHandler newInstance();
    public abstract void load(JSON.JSONObject json);
    public abstract CollisionResult collide(Craft craft, Collection<Block> blocks, Collection<Craft.BlockMovement> movements, Craft.BlockMovement movement, List<Medium> mediums);
}