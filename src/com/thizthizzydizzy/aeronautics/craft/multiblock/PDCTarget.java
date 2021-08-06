package com.thizthizzydizzy.aeronautics.craft.multiblock;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
public abstract class PDCTarget{
    public abstract World getWorld();
    public abstract BoundingBox getBoundingBox();
    public abstract boolean isTarget(Block block);
    public abstract boolean isValid();
    public Location getRandomLocation(){
        BoundingBox bbox = getBoundingBox();
        return new Location(getWorld(), randbetween(bbox.getMinX(),bbox.getMaxX()),randbetween(bbox.getMinY(),bbox.getMaxY()),randbetween(bbox.getMinZ(),bbox.getMaxZ()));
    }
    Random rand = new Random();
    private double randbetween(double min, double max){
        return rand.nextDouble()*(max-min)+min;
    }
}