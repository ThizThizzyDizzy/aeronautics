package com.thizthizzydizzy.aeronautics.craft.collision_handler;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.Medium;
import java.util.List;
import java.util.Collection;
import org.bukkit.block.Block;
public class StopCollisionHandler extends CollisionHandler{
    private final Aeronautics aeronautics;
    public StopCollisionHandler(Aeronautics aeronautics){
        super("aeronautics:stop");
        this.aeronautics = aeronautics;
    }
    @Override
    public CollisionHandler newInstance(){
        return new StopCollisionHandler(aeronautics);
    }
    @Override
    public void load(JSON.JSONObject json){}
    @Override
    public CollisionResult collide(Craft craft, Collection<Block> blocks, Collection<Craft.BlockMovement> movements, Craft.BlockMovement movement, List<Medium> mediums){
        craft.notifyPilots("Movement obstructed!");// by "+movement.to.getBlock().getType().toString()+"! ("+movement.to.getX()+","+movement.to.getY()+","+movement.to.getZ()+")", movement.to, Sound.BLOCK_ANVIL_LAND, .5f);
        return CollisionResult.CANCEL;
    }
}