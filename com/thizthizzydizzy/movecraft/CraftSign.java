package com.thizthizzydizzy.movecraft;
import java.util.ArrayList;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
public abstract class CraftSign{
    public static ArrayList<CraftSign> signs = new ArrayList<>();
    public final String name;
    static{
        addSign(new CraftSign("Subcraft Rotate"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                for(CraftType type : movecraft.subcraftTypes){
                    if(sign.getLine(1).equalsIgnoreCase(type.name)){
                        switch(event.getAction()){
                            case RIGHT_CLICK_BLOCK:
                                movecraft.rotateSubcraft(type, event.getPlayer(), sign.getBlock(), 1, sign.getLine(2));
                                event.setCancelled(true);
                                break;
                            case LEFT_CLICK_BLOCK:
                                movecraft.rotateSubcraft(type, event.getPlayer(), sign.getBlock(), -1, sign.getLine(2));
                                event.setCancelled(true);
                                break;
                        }
                    }
                }
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "Subcraft Rotate");
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return true;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("[helm]"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                Craft craft = movecraft.getCraft(sign.getBlock());
                if(craft!=null){
                    if(!craft.checkCopilot(event.getPlayer()))return;
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
            @Override
            public void update(Craft craft, Sign sign){
                for(int i = 0; i<Movecraft.helm.length; i++){
                    sign.setLine(i, Movecraft.helm[i]);
                }
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return true;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("Cruise:"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                Craft craft = movecraft.getCraft(sign.getBlock());
                if(craft!=null){
                    if(!craft.checkCopilot(event.getPlayer()))return;
                    craft.cruise(Movecraft.getSignRotation(sign.getBlockData()));
                }
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "Cruise: "+(craft.cruise==Movecraft.getSignRotation(sign.getBlockData())?"ON":"OFF"));
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return false;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("Ascend:"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                Craft craft = movecraft.getCraft(sign.getBlock());
                if(craft!=null){
                    if(!craft.checkCopilot(event.getPlayer()))return;
                    craft.cruise(Direction.UP);
                }
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "Ascend: "+(craft.cruise==Direction.UP?"ON":"OFF"));
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return false;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("Descend:"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                Craft craft = movecraft.getCraft(sign.getBlock());
                if(craft!=null){
                    if(!craft.checkCopilot(event.getPlayer()))return;
                    craft.cruise(Direction.DOWN);
                }
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "Descend: "+(craft.cruise==Direction.DOWN?"ON":"OFF"));
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return false;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("Release"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                Craft craft = movecraft.getCraft(event.getPlayer());
                if(craft!=null){
                    craft.release();
                }
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "Release");
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return false;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("AA Director"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                movecraft.clearDirector(event.getPlayer());
                Craft craft = movecraft.getCraft(sign.getBlock());
                if(craft!=null)craft.addAADirector(event.getPlayer());
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "AA Director");
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return false;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
        addSign(new CraftSign("Cannon Director"){
            @Override
            public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                movecraft.clearDirector(event.getPlayer());
                Craft craft = movecraft.getCraft(sign.getBlock());
                if(craft!=null)craft.addCannonDirector(event.getPlayer());
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, "Cannon Director");
                sign.update();
            }
            @Override
            public boolean canLeftClick(){
                return false;
            }
            @Override
            public boolean canRightClick(){
                return true;
            }
        });
    }
    public CraftSign(String name){
        this.name = name;
    }
    public static void addSign(CraftSign sign){
        signs.add(sign);
    }
    public static CraftSign getSign(Block block){
        if(Movecraft.Tags.isSign(block.getType()))return getSign((Sign)block.getState());
        return null;
    }
    public static CraftSign getSign(Sign sign){
        if(Movecraft.isHelm(sign.getLines())){
            return getSign("[helm]");
        }
        return getSign(sign.getLine(0).trim());
    }
    private static CraftSign getSign(String name){
        for(CraftSign s : signs){
            if(s.name.equalsIgnoreCase(name))return s;
        }
        if(name.contains(":")){
            for(CraftSign s : signs){
                if(name.toLowerCase().startsWith(s.name.toLowerCase()))return s;
            }
        }
        return null;
    }
    public abstract void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event);
    public abstract void update(Craft craft, Sign sign);
    public boolean canRespond(Action action){
        if(action==Action.LEFT_CLICK_BLOCK)return canLeftClick();
        if(action==Action.RIGHT_CLICK_BLOCK)return canRightClick();
        return false;
    }
    public abstract boolean canLeftClick();
    public abstract boolean canRightClick();
}