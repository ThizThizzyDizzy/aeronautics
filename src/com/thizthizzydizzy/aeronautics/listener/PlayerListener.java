package com.thizthizzydizzy.aeronautics.listener;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
public class PlayerListener implements Listener{
    private final Aeronautics aeronautics;
    public PlayerListener(Aeronautics aeronautics){
        this.aeronautics = aeronautics;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction()==Action.LEFT_CLICK_BLOCK||event.getAction()==Action.RIGHT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            Craft craft = aeronautics.getCraft(block);
            if(state instanceof Sign){
                Sign sign = (Sign)state;
                CraftSign craftSign = CraftSign.getSign(craft, sign);
                if(craftSign!=null){
                    if(craftSign.canRespond(craft, sign, event.getAction())){
                        event.setCancelled(true);
                        craftSign.click(craft, sign, event);
                    }
                }
            }
            if(craft!=null){
                craft.event(event);
            }
        }else{
            Craft craft = aeronautics.getCraftOnBoard(player);
            if(craft!=null&&craft.isOnBoard(player)){
                craft.event(event);
            }
        }
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Craft craft = aeronautics.getCraftWithCrew(event.getPlayer());
        if(craft!=null){
            craft.event(event);
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Craft craft = aeronautics.getCraftWithCrew(event.getPlayer());
        if(craft!=null){
            craft.event(event);
        }
    }
}