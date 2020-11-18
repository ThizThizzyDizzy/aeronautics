package com.thizthizzydizzy.movecraft.listener;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.Direction;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
public class PlayerListener implements Listener{
    private final Movecraft movecraft;
    public PlayerListener(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getItem()!=null&&event.getItem().getType()==Material.STICK){
            for(Craft craft : movecraft.crafts){
                if(craft.aaDirectors.contains(player)){
                    craft.aaTarget(player, (event.getAction()==Action.RIGHT_CLICK_AIR||(event.getAction()==Action.RIGHT_CLICK_BLOCK&&!player.isSneaking()))?craft.getTarget(player):null);
                    return;
                }
                if(craft.cannonDirectors.contains(player)){
                    craft.cannonTarget(player, (event.getAction()==Action.RIGHT_CLICK_AIR||(event.getAction()==Action.RIGHT_CLICK_BLOCK&&!player.isSneaking()))?craft.getTarget(player):null);
                    return;
                }
            }
            if((event.getAction()==Action.RIGHT_CLICK_AIR||event.getAction()==Action.RIGHT_CLICK_BLOCK)){
                Craft craft = movecraft.getCraft(player);
                if(craft!=null&&craft.isPilotOnBoard()){
                    //fly with stick
                    event.setCancelled(true);
                    int x=0,y=0,z=0;
                    float pitch = player.getLocation().getPitch();
                    float yaw = player.getLocation().getYaw();
                    if(pitch>30)y = -1;
                    if(pitch<-30)y = 1;
                    while(yaw<-180)yaw+=360;
                    while(yaw>180)yaw-=360;
                    if(pitch<60&&pitch>-60){
                        if(yaw<-120||yaw>120)z = -1;
                        if(yaw>-60&&yaw<60)z = 1;
                        if(yaw>30&&yaw<150)x = -1;
                        if(yaw<-30&&yaw>-150)x = 1;
                    }
                    craft.maneuver(x,y,z);
                    return;
                }
            }
        }
        if(event.getAction()==Action.RIGHT_CLICK_BLOCK||event.getAction()==Action.LEFT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            if(Movecraft.Tags.isSign(block.getType())){
                //HOLY COW IT'S A SIGN
                Sign s = (Sign) block.getState();
                CraftSign sign = CraftSign.getSign(s);
                if(sign!=null){
                    if(sign.canRespond(event.getAction()))sign.click(movecraft, s, event);
                }
            }
        }
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