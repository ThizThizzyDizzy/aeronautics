package com.thizthizzydizzy.movecraft;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
public class CraftType{
    public final String name;
    public final Set<Material> bannedBlocks = new HashSet<>();
    public final HashMap<ArrayList<Material>, Float> bannedRatios = new HashMap<>();
    public final HashMap<ArrayList<Material>, Integer> limitedBlocks = new HashMap<>();
    public int minSize = 10;
    public int maxSize = 10000;
//    public int minTime = 2;
    public final int type;
    public static final int CRAFT = 0;
    public static final int SUBCRAFT = 1;
    public static final int PROJECTILE = 2;
    Set<CraftType> children = new HashSet<>();
    Set<String> tempChildren = new HashSet<>();
    public MovementDetails flight = null;
    public MovementDetails dive = null;
    public HashMap<Material, Integer> fuels = new HashMap<>();
    public int moveForward;
    public int moveHoriz;
    public int moveVert;
    public int fuel;
    public int collisionDamage = 0;
    public CraftType(String name){
        this(name, CRAFT);
    }
    public CraftType(String name, int type){
        this.name = name;
        bannedBlocks.add(Material.AIR);
        bannedBlocks.add(Material.VOID_AIR);
        bannedBlocks.add(Material.CAVE_AIR);
        bannedBlocks.add(Material.BEDROCK);
        bannedBlocks.add(Material.STRUCTURE_BLOCK);
        bannedBlocks.add(Material.STRUCTURE_VOID);
        bannedBlocks.add(Material.BARRIER);
        if(type==CRAFT){
            CraftSign.addSign(new CraftSign(name){
                @Override
                public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                    if(sign.getLine(0).equalsIgnoreCase(name)){
                        movecraft.debug(event.getPlayer(), "Attempting to pilot "+name);
                        Craft craft = movecraft.getCraft(sign.getBlock());
                        if(craft!=null){
                            if(craft.type==CraftType.this){
                                if(craft.pilot!=event.getPlayer()){
                                    if(craft.isPilot(event.getPlayer())){
                                        movecraft.clearCopilot(event.getPlayer());
                                        craft.addCopilot(event.getPlayer());
                                        event.getPlayer().sendMessage("You are now a co-pilot!");
                                        return;
                                    }
                                    event.getPlayer().sendMessage("This ship is already piloted!");
                                    return;
                                }
                            }else{
                                if(!craft.type.children.contains(CraftType.this)){
                                    event.getPlayer().sendMessage("This ship is already piloted!");
                                    return;
                                }
                            }
                        }
                        //TODO let skiffs take off (and land)
                        movecraft.clearCopilot(event.getPlayer());
                        movecraft.detect(CraftType.this, event.getPlayer(), sign.getBlock());
                    }
                }
                @Override
                public void update(Craft craft, Sign sign){
                    sign.setLine(0, name);
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
                @Override
                public String getTag(Sign sign){
                    return null;
                }
            });
        }
        if(type==PROJECTILE){
            CraftSign.addSign(new CraftSign("[Launch"+name+"]"){
                @Override
                public void click(Movecraft movecraft, Sign sign, PlayerInteractEvent event){
                    if(sign.getLine(0).equalsIgnoreCase(name)){
                        movecraft.launchProjectile(CraftType.this, event.getPlayer(), sign.getBlock());
                    }
                }
                @Override
                public void update(Craft craft, Sign sign){
                    sign.setLine(0, "[Launch"+name+"]");
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
                @Override
                public String getTag(Sign sign){
                    return sign.getLine(1);
                }
            });
        }
        this.type = type;
    }
    public void banBlocks(String categoryName){
        banBlocks(Movecraft.getBlocks(categoryName));
    }
    public void banBlock(String name){
        banBlock(Material.matchMaterial(name));
    }
    public void banBlock(Material block){
        bannedBlocks.add(block);
    }
    public void banBlocks(Iterable<Material> block){
        for(Material b : block)banBlock(b);
    }
    public void limitBlock(String name, int limit){
        limitBlock(Material.matchMaterial(name), limit);
    }
    public void limitBlock(Material block, int limit){
        ArrayList<Material> m = new ArrayList<>();
        m.add(block);
        limitBlocks(m, limit);
    }
    public void limitBlocks(ArrayList<Material> blocks, int limit){
        limitedBlocks.put(blocks, limit);
    }
    public void addBannedRatio(String block, float ratio){
        addBannedRatio(Material.matchMaterial(block), ratio);
    }
    public void addBannedRatio(Material block, float ratio){
        ArrayList<Material> m = new ArrayList<>();
        m.add(block);
        addBannedRatio(m, ratio);
    }
    public void addBannedRatio(ArrayList<Material> blocks, float ratio){
        bannedRatios.put(blocks, ratio);
    }
}