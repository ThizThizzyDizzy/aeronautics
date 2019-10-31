package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.CraftSign;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
public class PlayerInteract implements Listener{
    private final Movecraft movecraft;
    public PlayerInteract(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getItem()!=null&&event.getItem().getType()==Material.STICK&&(event.getAction()==Action.RIGHT_CLICK_AIR||event.getAction()==Action.RIGHT_CLICK_BLOCK)){
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
        if(event.getAction()!=Action.RIGHT_CLICK_BLOCK&&event.getAction()!=Action.LEFT_CLICK_BLOCK)return;
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