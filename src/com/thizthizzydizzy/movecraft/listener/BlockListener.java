package com.thizthizzydizzy.movecraft.listener;
import com.thizthizzydizzy.movecraft.Movecraft;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.event.BlockMoveEvent;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
public class BlockListener implements Listener{
    private final Movecraft movecraft;
    public BlockListener(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Craft craft = movecraft.getCraft(event.getBlock());
        if(craft==null)return;
        craft.event(event);
        if(event.isCancelled())return;
        if(!craft.isCrew(event.getPlayer()))return;
        if(!craft.removeBlock(event.getPlayer(), event.getBlock(), false))event.setCancelled(true);
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event){
        Craft craft = movecraft.getCraft(event.getBlock());
        if(craft!=null){
            String line = event.getLine(0).trim();
            if(line.equalsIgnoreCase("Pilot:")||line.equalsIgnoreCase("Pilots:")){
                if(!craft.canPilot(event.getPlayer())){
                    event.getPlayer().sendMessage("Only pilots can add pilot signs!");
                    event.setCancelled(true);
                }
            }
            if(line.equalsIgnoreCase("Crew:")){
                if(!craft.isCrew(event.getPlayer())){
                    event.getPlayer().sendMessage("Only crew members can add crew signs!");
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        Craft craft = movecraft.getCraft(event.getBlock());
        if(craft==null)return;
        craft.event(event);
        if(event.isCancelled())return;
        craft.removeBlock(null, event.getBlock(), true);
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        HashMap<Block, Craft> crafts = new HashMap<>();
        List<Block> blockList = event.blockList();
        do{
            for(Block b : blockList){
                if(!event.blockList().contains(b))continue;//that was already removed, no reason to do it again
                Craft craft = movecraft.getCraft(b);
                if(craft==null)continue;
                craft.event(event);
                crafts.put(b, craft);
            }
        }while(!blockList.equals(event.blockList()));
        for(Block b : event.blockList()){
            Craft c = crafts.get(b);
            c.removeBlock(null, b, true);
        }
    }
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event){
        Craft craft = movecraft.getCraft(event.getBlock());
        if(craft==null)return;
        craft.event(event);
        if(event.isCancelled())return;
        craft.removeBlock(null, event.getBlock(), true);
    }
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event){
        Craft craft = movecraft.getCraft(event.getSource());
        if(craft==null)return;
        craft.event(event);
        if(event.isCancelled())return;
        craft.addBlock(null, event.getBlock(), true);
    }
    @EventHandler
    public void onBlockPop(BlockPhysicsEvent event){
        if(event.getBlock().getType()==Material.AIR){
            Craft craft = movecraft.getCraft(event.getBlock());
            if(craft==null)return;
            craft.event(event);
            if(event.isCancelled())return;
            craft.removeBlock(null, event.getBlock(), false);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Craft craft = movecraft.getCraft(event.getBlockAgainst());
        if(craft==null)return;
        craft.event(event);
        if(event.isCancelled())return;
        if(!craft.isCrew(event.getPlayer())){
            event.setCancelled(true);
            return;
        }
        if(!craft.addBlock(event.getPlayer(), event.getBlock(), false))event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockMove(BlockMoveEvent event){
        event.getCraft().event(event);
    }
    
    @EventHandler
    public void onButtonPress(BlockRedstoneEvent event){
        Craft craft = movecraft.getCraft(event.getBlock());
        if(craft!=null)craft.event(event);
    }
}