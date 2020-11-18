package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.craft.Craft.BlockMovement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public class BlockMoveEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private final BlockMovement movement;
    public BlockMoveEvent(BlockMovement movement){
        this.movement = movement;
    }
    public BlockMovement getBlockMovement(){
        return movement;
    }
    public Location getFromLocation(){
        return movement.from;
    }
    public Location getToLocation(){
        return movement.to;
    }
    public int getRotation(){
        return movement.rotation;
    }
    @Override
    public HandlerList getHandlers(){
        return handlers;
    }
    public static HandlerList getHandlerList(){
        return handlers;
    }
    public Block getToBlock(){
        return getToLocation().getBlock();
    }
    public Block getFromBlock(){
        return getFromLocation().getBlock();
    }
}