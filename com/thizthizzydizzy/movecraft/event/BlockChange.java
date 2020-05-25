package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.CraftSign;
import com.thizthizzydizzy.movecraft.Movecraft;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
public class BlockChange implements Listener{
    private final Movecraft movecraft;
    public BlockChange(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    public void breakBlock(Block block){
        Craft craft = movecraft.getCraft(block);
        if(craft!=null)craft.removeBlock(null, block, true);
    }
    public void placeBlock(Block block, Block against){
        Craft craft = movecraft.getCraft(against);
        if(craft!=null)craft.addBlock(block, true);
    }
    public boolean breakBlock(Player player, Block block){
        Craft craft = movecraft.getCraft(block);
        if(craft!=null){
            CraftSign sign = CraftSign.getSign(block);
            if(sign!=null){
                if(sign.name.equals("Pilot:")){
                    if(!craft.checkPilot(player))return false;
                }
            }
            if(!craft.checkCrew(player))return false;
            return craft.removeBlock(player, block, false);
        }
        return true;
    }
    public boolean placeBlock(Player player, Block block, Block against){
        return movecraft.placeBlock(player, block, against);
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(!breakBlock(event.getPlayer(), event.getBlock()))event.setCancelled(true);
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event){
        Craft craft = movecraft.getCraft(event.getBlock());
        if(craft!=null){
            if(event.getLine(0).trim().equalsIgnoreCase("Pilot:")){
                if(!craft.checkPilot(event.getPlayer())){
                    event.getPlayer().sendMessage("Only pilots can add pilot signs!");
                    event.setCancelled(true);
                }
            }
            if(event.getLine(0).trim().equalsIgnoreCase("Crew:")){
                if(!craft.checkCrew(event.getPlayer())){
                    event.getPlayer().sendMessage("Only crew members can add crew signs!");
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onBlockBoom(BlockExplodeEvent event){
        if(hasWater(event.getBlock().getRelative(-1,0,0))
         ||hasWater(event.getBlock().getRelative(1,0,0))
         ||hasWater(event.getBlock().getRelative(0,-1,0))
         ||hasWater(event.getBlock().getRelative(0,1,0))
         ||hasWater(event.getBlock().getRelative(0,0,1))
         ||hasWater(event.getBlock().getRelative(0,0,-1))){
            event.setCancelled(true);
            return;
        }
        if(movecraft.resistances.containsKey(event.getBlock().getType())){
            if(new Random(event.getBlock().getX() + event.getBlock().getY() + event.getBlock().getZ() + (System.currentTimeMillis() >> 12)).nextFloat()<=movecraft.resistances.get(event.getBlock().getType())){
                event.setCancelled(true);
                return;
            }
        }
        breakBlock(event.getBlock());
    }
    @EventHandler
    public void onEntityBoom(EntityExplodeEvent event){
        for (Iterator<Block> it = event.blockList().iterator(); it.hasNext();) {
            Block b = it.next();
            if(hasWater(b.getRelative(-1,0,0))
                    ||hasWater(b.getRelative(1,0,0))
                    ||hasWater(b.getRelative(0,-1,0))
                    ||hasWater(b.getRelative(0,1,0))
                    ||hasWater(b.getRelative(0,0,1))
                    ||hasWater(b.getRelative(0,0,-1))){
                it.remove();
                continue;
            }
            if(movecraft.resistances.containsKey(b.getType())){
                if(new Random(b.getX() + b.getY() + b.getZ() + (System.currentTimeMillis() >> 12)).nextFloat()<=movecraft.resistances.get(b.getType())){
                    it.remove();
                    continue;
                }
            }
            breakBlock(b);
        }
    }
    private boolean hasWater(Block b){
        if(b.isLiquid())return true;
        if(b.getBlockData() instanceof Waterlogged){
            return ((Waterlogged)b.getBlockData()).isWaterlogged();
        }
        return false;
    }
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event){
        breakBlock(event.getBlock());
    }
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event){
        placeBlock(event.getBlock(), event.getSource());
    }
    @EventHandler
    public void onBlockPop(BlockPhysicsEvent event){
        if(event.getBlock().getType()==Material.AIR)breakBlock(null, event.getBlock());
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!placeBlock(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst()))event.setCancelled(true);
    }
}