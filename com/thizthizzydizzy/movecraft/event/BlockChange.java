package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
public class BlockChange implements Listener{
    private final Movecraft movecraft;
    public BlockChange(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    public void breakBlock(Block block){
        Craft craft = movecraft.getCraft(block);
        if(craft!=null)craft.removeBlock(block);
    }
    public void placeBlock(Block block, Block against){
        Craft craft = movecraft.getCraft(against);
        if(craft!=null)craft.addBlock(block);
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        breakBlock(event.getBlock());
    }
    @EventHandler
    public void onBlockBoom(BlockExplodeEvent event){
        breakBlock(event.getBlock());
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
        if(event.getBlock().getType()==Material.AIR)breakBlock(event.getBlock());
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        placeBlock(event.getBlockPlaced(), event.getBlockAgainst());
    }
}