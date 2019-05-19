package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Direction;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.CraftType;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
public class SignClick implements Listener{
    private final Movecraft movecraft;
    //TODO rename class, this also handles right-clicking with a stick
    public SignClick(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onSignClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getItem()!=null&&event.getItem().getType()==Material.STICK&&(event.getAction()==Action.RIGHT_CLICK_AIR||event.getAction()==Action.RIGHT_CLICK_BLOCK)){
            Craft craft = movecraft.getCraft(player);
            if(craft!=null&&craft.isPilotOnBoard()){
                //fly with stick
                event.setCancelled(true);
                BlockFace face = player.getFacing();
                int x=0,y=0,z=0;
                float pitch = player.getLocation().getPitch();
                float yaw = player.getLocation().getYaw();
                if(pitch>30)y = -1;
                if(pitch<-30)y = 1;
                if(yaw<-120||yaw>120)z = -1;
                if(yaw>-60&&yaw<60)z = 1;
                if(yaw>30&&yaw<150)x = -1;
                if(yaw<-30&&yaw>-150)x = 1;
                craft.maneuver(x,y,z);
                return;
            }
        }
        if(event.getAction()!=Action.RIGHT_CLICK_BLOCK&&event.getAction()!=Action.LEFT_CLICK_BLOCK)return;
        Block block = event.getClickedBlock();
        if(Movecraft.Tags.isSign(block.getType())){
            //HOLY COW IT'S A SIGN
            Sign sign = (Sign) block.getState();
            if(sign.getLine(0).equalsIgnoreCase("Subcraft Rotate")){
                for(CraftType type : movecraft.subcraftTypes){
                    if(sign.getLine(1).equalsIgnoreCase(type.name)){
                        switch(event.getAction()){
                            case RIGHT_CLICK_BLOCK:
                                movecraft.rotateSubcraft(type, player, block, 1);
                                event.setCancelled(true);
                                break;
                            case LEFT_CLICK_BLOCK:
                                movecraft.rotateSubcraft(type, player, block, -1);
                                event.setCancelled(true);
                                break;
                        }
                    }
                }
            }else if(movecraft.isHelm(sign.getLines())){
                Craft craft = movecraft.getCraft(block);
                if(craft!=null){
                    if(craft.pilot.getUniqueId().equals(player.getUniqueId())){
                        switch(event.getAction()){
                            case RIGHT_CLICK_BLOCK:
                                craft.rotate(sign, 1);
                                event.setCancelled(true);
                                break;
                            case LEFT_CLICK_BLOCK:
                                craft.rotate(sign, -1);
                                event.setCancelled(true);
                                break;
                        }
                    }
                }
            }
            if(event.getAction()==Action.RIGHT_CLICK_BLOCK){
                for(CraftType type : movecraft.craftTypes){
                    if(sign.getLine(0).equalsIgnoreCase(type.name)){
                        //it's a detect craft sign!
                        movecraft.detect(type, player, block);
                    }
                }
                if(sign.getLine(0).equalsIgnoreCase("Cruise: OFF")||sign.getLine(0).equalsIgnoreCase("Cruise: ON")){
                    //it's a cruise sign!
                    Craft craft = movecraft.getCraft(block);
                    if(craft!=null){
                        if(craft.pilot.getUniqueId().equals(player.getUniqueId())){
                            craft.cruise(sign, getSignRotation(block.getBlockData()));
                        }else{
                            player.sendMessage("That's not your ship!");
                        }
                    }
                }else if(sign.getLine(0).equalsIgnoreCase("Ascend: OFF")||sign.getLine(0).equalsIgnoreCase("Ascend: ON")){
                    //it's an ascend sign!
                    Craft craft = movecraft.getCraft(block);
                    if(craft!=null){
                        if(craft.pilot.getUniqueId().equals(player.getUniqueId())){
                            craft.ascend(sign);
                        }else{
                            player.sendMessage("That's not your ship!");
                        }
                    }
                }else if(sign.getLine(0).equalsIgnoreCase("Descend: OFF")||sign.getLine(0).equalsIgnoreCase("Descend: ON")){
                    //it's a descend sign!
                    Craft craft = movecraft.getCraft(block);
                    if(craft!=null){
                        if(craft.pilot.getUniqueId().equals(player.getUniqueId())){
                            craft.descend(sign);
                        }else{
                            player.sendMessage("That's not your ship!");
                        }
                    }
                }else if(sign.getLine(0).equalsIgnoreCase("Release")){
                    //it's a release sign!
                    Craft craft = movecraft.getCraft(player);
                    if(craft!=null){
                        craft.release();
                    }
                }else if(sign.getLine(0).equalsIgnoreCase("Contacts:")){
                    //it's a contacts sign!
                    //TODO read contacts
                }else if(sign.getLine(0).equalsIgnoreCase("Crew:")){
                    //it's a crew sign!
                    //TODO do nothing
                }else if(sign.getLine(0).equalsIgnoreCase("Pilot:")){
                    //it's a pilot sign!
                    //TODO do nothing
                }else if(sign.getLine(0).equalsIgnoreCase("Status:")){
                    //it's a status sign!
                    //TODO read status
                }else if(sign.getLine(0).equalsIgnoreCase("Speed:")){
                    //it's a speed sign!
                    //TODO read speed
                }
            }
        }
    }
    private Direction getSignRotation(BlockData data){
        if(data instanceof WallSign){
            WallSign sign = (WallSign)data;
            switch(sign.getFacing().getOppositeFace()){
                case NORTH:
                    return Direction.NORTH;
                case SOUTH:
                    return Direction.SOUTH;
                case EAST:
                    return Direction.EAST;
                case WEST:
                    return Direction.WEST;
                default:
                    return Direction.NONE;
            }
        }
        if(data instanceof org.bukkit.block.data.type.Sign){
            org.bukkit.block.data.type.Sign sign = (org.bukkit.block.data.type.Sign)data;
            switch(sign.getRotation().getOppositeFace()){
                case NORTH:
                case NORTH_NORTH_EAST:
                case NORTH_NORTH_WEST:
                    return Direction.NORTH;
                case EAST:
                case EAST_NORTH_EAST:
                case EAST_SOUTH_EAST:
                    return Direction.EAST;
                case WEST:
                case WEST_NORTH_WEST:
                case WEST_SOUTH_WEST:
                    return Direction.WEST;
                case SOUTH:
                case SOUTH_SOUTH_EAST:
                case SOUTH_SOUTH_WEST:
                    return Direction.SOUTH;
                default:
                    return Direction.NONE;
            }
        }
        return Direction.NONE;
    }
}