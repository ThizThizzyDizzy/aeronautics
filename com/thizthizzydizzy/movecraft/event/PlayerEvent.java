package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.Direction;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
public class PlayerEvent implements Listener{
    private final Movecraft movecraft;
    public PlayerEvent(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        movecraft.clearCopilot(player);
        Craft craft = movecraft.getCraft(player);
        if(craft!=null){
            if(!craft.repilot()){
                craft.cruise = Direction.NONE;
            }
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        movecraft.playerJoined(player);
    }
}