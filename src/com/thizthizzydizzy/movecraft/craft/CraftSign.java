package com.thizthizzydizzy.movecraft.craft;
import com.thizthizzydizzy.movecraft.Movecraft;
import com.thizthizzydizzy.movecraft.engine.Engine;
import com.thizthizzydizzy.movecraft.special.Special;
import java.util.ArrayList;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
public abstract class CraftSign{
    private static final ArrayList<CraftSign> signs = new ArrayList<>();
    public static void init(Movecraft movecraft){
        addSign(new CraftSign("movecraft:pilot_craft"){
            @Override
            public boolean matches(Craft craft, Sign sign){
                String name = sign.getLine(0);
                for(CraftType type : movecraft.craftTypes){
                    if(type.getName().equals(name))return true;
                    if(type.getDisplayName().equalsIgnoreCase(name))return true;
                }
                return false;
            }
            @Override
            public void click(Craft craft, Sign sign, PlayerInteractEvent event){
                String name = sign.getLine(0);
                CraftType piloting = null;
                for(CraftType type : movecraft.craftTypes){
                    if(type.getName().equals(name)){
                        piloting = type;
                        break;
                    }
                    if(type.getDisplayName().equalsIgnoreCase(name.trim()))piloting = type;
                }
                if(piloting==null){
                    throw new IllegalArgumentException("Cannot pilot craft! No craft exists matching "+name+"!");//should never hit
                }
                if(craft==null){
                    //pilot new craft
                    movecraft.debug(event.getPlayer(), "Attempting to pilot "+name);
                    Craft newCraft = movecraft.detect(piloting, event.getPlayer(), sign.getBlock());
                    if(newCraft.canPilot(event.getPlayer())){
                        newCraft.addPilot(event.getPlayer());
                        movecraft.addCraft(newCraft);
                    }
                }else{
                    if(craft.type==piloting){
                        if(craft.canPilot(event.getPlayer())){
                            if(craft.isPilot(event.getPlayer())){
                                movecraft.debug(event.getPlayer(), "You are already piloting this craft!");
                            }else{
                                craft.addPilot(event.getPlayer());
                                movecraft.debug(event.getPlayer(), "You are now co-piloting this craft");
                            }
                        }
                    }else{
                        throw new UnsupportedOperationException("Not yet implemented");//TODO undock
                    }
                }
            }
            @Override
            public void update(Craft craft, Sign sign){}
            @Override
            public boolean canLeftClick(Craft craft, Sign sign){
                return false;
            }
            @Override
            public boolean canRightClick(Craft craft, Sign sign){
                return true;
            }
        });
        Engine.createSigns();
        Special.createSigns();
    }
    public static CraftSign getSign(Craft craft, Sign sign){
        for(CraftSign craftSign : signs){
            if(craftSign.matches(craft, sign)){
                return craftSign;
            }
        }
        return null;
    }
    private final String name;
    protected CraftSign(String name){
        this.name = name;//TODO validate
    }
    public static void addSign(CraftSign sign){
        signs.add(sign);
    }
    public String getName(){
        return name;
    }
    public boolean canRespond(Craft craft, Sign sign, Action action){
        if(action==Action.LEFT_CLICK_BLOCK)return canLeftClick(craft, sign);
        if(action==Action.RIGHT_CLICK_BLOCK)return canRightClick(craft, sign);
        return false;
    }
    public abstract boolean matches(Craft craft, Sign sign);
    public abstract void click(Craft craft, Sign sign, PlayerInteractEvent event);
    public abstract void update(Craft craft, Sign sign);
    public abstract boolean canLeftClick(Craft craft, Sign sign);
    public abstract boolean canRightClick(Craft craft, Sign sign);
}