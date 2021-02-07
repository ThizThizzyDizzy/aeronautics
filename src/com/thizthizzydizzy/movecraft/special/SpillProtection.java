package com.thizthizzydizzy.movecraft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.CraftSpecial;
import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
public class SpillProtection extends Special implements Listener{
    public SpillProtection(){
        super("movecraft:spill_protection");
    }
    @Override
    protected void load(JSON.JSONObject json){}
    @Override
    public Special newInstance(){
        return new SpillProtection();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public void init(CraftSpecial special){}
    @Override
    public void tick(CraftSpecial special){}
    @Override
    public void event(CraftSpecial special, Event event){
        if(event instanceof BlockExplodeEvent){
            BlockExplodeEvent bee = (BlockExplodeEvent)event;
            if(hasNearbyFluid(bee.getBlock()))bee.setCancelled(true);
        }
        if(event instanceof EntityExplodeEvent){
            EntityExplodeEvent eee = (EntityExplodeEvent)event;
            for (Iterator<Block> it = eee.blockList().iterator(); it.hasNext();) {
                Block b = it.next();
                if(hasNearbyFluid(b))it.remove();
            }
        }
    }
    private boolean hasNearbyFluid(Block b){
        return hasFluid(b.getRelative(-1,0,0))
                ||hasFluid(b.getRelative(1,0,0))
                ||hasFluid(b.getRelative(0,1,0))//don't need to check below; fluids don't flow up
                ||hasFluid(b.getRelative(0,0,-1))
                ||hasFluid(b.getRelative(0,0,1));
    }
    private boolean hasFluid(Block b){
        if(b.isLiquid())return true;
        if(b.getBlockData() instanceof Waterlogged){
            return ((Waterlogged)b.getBlockData()).isWaterlogged();
        }
        return false;
    }
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special){}
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
}