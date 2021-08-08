package com.thizthizzydizzy.aeronautics.craft.multiblock;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.Craft.BlockMovement;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
public abstract class Multiblock{
    public Craft craft;
    public String name;
    public Block origin;
    public boolean dead;
    public Multiblock(String name){
        this(name, null, null);
    }
    public Multiblock(String name, Craft craft, Block origin){
        this.name = name;
        this.craft = craft;
        this.origin = origin;
    }
    public abstract Multiblock detect(Craft craft, Block origin);
    public abstract void init();
    public abstract void tick();
    /**
     * Rescan the multiblock to make sure it still exists
     * @return true if the multiblock still exists
     */
    public abstract boolean rescan();
    public void destroy(){
        dead = true;
    }
    public abstract void onDestroy();
    public boolean rotate(HashSet<Block> blocks, int rotation){
        Location origin = this.origin.getLocation();
        while(rotation>=4)rotation-=4;
        while(rotation<0)rotation+=4;
        ArrayList<BlockMovement> movements = new ArrayList<>();
        for(Block block : blocks){
            movements.add(new BlockMovement(block.getLocation(), craft.rotate(block.getLocation(), origin, rotation), rotation));
        }
        Iterable<Entity> entities = craft.move(blocks, movements, craft.type.mediums);
        if(entities==null)return false;
        for(Entity e : entities){
            Location l = e.getLocation();
            l.setYaw(l.getYaw()+90*rotation);
            e.teleport(l, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        return true;
    }
    public abstract void onRotated(int rotation);
}