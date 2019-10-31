package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.CraftSign;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        Craft craft = movecraft.getCraft(against);
        if(craft!=null){
            if(!craft.checkCrew(player))return false;
            return craft.addBlock(player, block, false);
        }
        return true;
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
        if(event.getBlock().getType()==Material.AIR)breakBlock(null, event.getBlock());
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(!placeBlock(event.getPlayer(), event.getBlockPlaced(), event.getBlockAgainst()))event.setCancelled(true);
    }
}