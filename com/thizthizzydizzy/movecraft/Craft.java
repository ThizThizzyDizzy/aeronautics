package com.thizthizzydizzy.movecraft;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Campfire;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
public class Craft{
    private final Movecraft movecraft;
    public final CraftType type;
    public final Set<Block> blocks;
    public Player pilot;
    public ArrayList<Player> copilots = new ArrayList<>();
    public Set<Player> aaDirectors = new HashSet<>();
    public Set<Player> cannonDirectors = new HashSet<>();
    public Direction cruise = Direction.NONE;
    private int timer = 0;
    private int involuntaryTimer = 0;
    private int maneuverTimer = 0;
    private final World world;
    private BoundingBox bbox;
    private boolean sinking = false;
    public boolean moving;
    public boolean disabledAir = false;
    public boolean disabledDive = false;
    public int fuel = 0;
    public int repilotTimer = 0;
    private static final int NONE = 0;
    private static final int CONSTRUCTION = 1;
    private static final int COMBAT = 2;
    private int mode = NONE;
    private int modeTimer = 0;
    private int damageReport = 0; 
    private int damageReportTimer = 0;
    private static final Set<Material> blocksThatPop = new HashSet<>();
//    private static final HashMap<Entity, Double> tnt = new HashMap<>();
    private boolean canFly;
    private boolean canDive;
    static{
        blocksThatPop.add(Material.REDSTONE_WIRE);
        blocksThatPop.add(Material.REPEATER);
        blocksThatPop.add(Material.COMPARATOR);
        blocksThatPop.add(Material.LEVER);
        blocksThatPop.add(Material.WHITE_CARPET);
        blocksThatPop.add(Material.LIGHT_BLUE_CARPET);
        blocksThatPop.add(Material.LIGHT_GRAY_CARPET);
        blocksThatPop.add(Material.BLUE_CARPET);
        blocksThatPop.add(Material.GREEN_CARPET);
        blocksThatPop.add(Material.LIME_CARPET);
        blocksThatPop.add(Material.YELLOW_CARPET);
        blocksThatPop.add(Material.PINK_CARPET);
        blocksThatPop.add(Material.RED_CARPET);
        blocksThatPop.add(Material.MAGENTA_CARPET);
        blocksThatPop.add(Material.PURPLE_CARPET);
        blocksThatPop.add(Material.BLACK_CARPET);
        blocksThatPop.add(Material.GRAY_CARPET);
        blocksThatPop.add(Material.ORANGE_CARPET);
        blocksThatPop.add(Material.CYAN_CARPET);
        blocksThatPop.add(Material.BROWN_CARPET);
        blocksThatPop.add(Material.OAK_SIGN);
        blocksThatPop.add(Material.BIRCH_SIGN);
        blocksThatPop.add(Material.ACACIA_SIGN);
        blocksThatPop.add(Material.JUNGLE_SIGN);
        blocksThatPop.add(Material.SPRUCE_SIGN);
        blocksThatPop.add(Material.DARK_OAK_SIGN);
        blocksThatPop.add(Material.OAK_WALL_SIGN);
        blocksThatPop.add(Material.BIRCH_WALL_SIGN);
        blocksThatPop.add(Material.ACACIA_WALL_SIGN);
        blocksThatPop.add(Material.JUNGLE_WALL_SIGN);
        blocksThatPop.add(Material.SPRUCE_WALL_SIGN);
        blocksThatPop.add(Material.DARK_OAK_WALL_SIGN);
        blocksThatPop.add(Material.TORCH);
        blocksThatPop.add(Material.WALL_TORCH);
        blocksThatPop.add(Material.REDSTONE_TORCH);
        blocksThatPop.add(Material.REDSTONE_WALL_TORCH);
        blocksThatPop.add(Material.OAK_SAPLING);
        blocksThatPop.add(Material.BIRCH_SAPLING);
        blocksThatPop.add(Material.SPRUCE_SAPLING);
        blocksThatPop.add(Material.ACACIA_SAPLING);
        blocksThatPop.add(Material.JUNGLE_SAPLING);
        blocksThatPop.add(Material.DARK_OAK_SAPLING);
        blocksThatPop.add(Material.RAIL);
        blocksThatPop.add(Material.POWERED_RAIL);
        blocksThatPop.add(Material.ACTIVATOR_RAIL);
        blocksThatPop.add(Material.DETECTOR_RAIL);
        blocksThatPop.add(Material.GRASS);
        blocksThatPop.add(Material.FERN);
        blocksThatPop.add(Material.DEAD_BUSH);
        blocksThatPop.add(Material.SEAGRASS);
        blocksThatPop.add(Material.SEA_PICKLE);
        blocksThatPop.add(Material.DANDELION);
        blocksThatPop.add(Material.POPPY);
        blocksThatPop.add(Material.BLUE_ORCHID);
        blocksThatPop.add(Material.ALLIUM);
        blocksThatPop.add(Material.AZURE_BLUET);
        blocksThatPop.add(Material.RED_TULIP);
        blocksThatPop.add(Material.ORANGE_TULIP);
        blocksThatPop.add(Material.WHITE_TULIP);
        blocksThatPop.add(Material.PINK_TULIP);
        blocksThatPop.add(Material.OXEYE_DAISY);
        blocksThatPop.add(Material.CORNFLOWER);
        blocksThatPop.add(Material.LILY_OF_THE_VALLEY);
        blocksThatPop.add(Material.WITHER_ROSE);
        blocksThatPop.add(Material.BROWN_MUSHROOM);
        blocksThatPop.add(Material.RED_MUSHROOM);
        blocksThatPop.add(Material.LADDER);
        blocksThatPop.add(Material.STONE_PRESSURE_PLATE);
        blocksThatPop.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        blocksThatPop.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        blocksThatPop.add(Material.OAK_PRESSURE_PLATE);
        blocksThatPop.add(Material.SPRUCE_PRESSURE_PLATE);
        blocksThatPop.add(Material.BIRCH_PRESSURE_PLATE);
        blocksThatPop.add(Material.JUNGLE_PRESSURE_PLATE);
        blocksThatPop.add(Material.ACACIA_PRESSURE_PLATE);
        blocksThatPop.add(Material.DARK_OAK_PRESSURE_PLATE);
        blocksThatPop.add(Material.OAK_BUTTON);
        blocksThatPop.add(Material.SPRUCE_BUTTON);
        blocksThatPop.add(Material.BIRCH_BUTTON);
        blocksThatPop.add(Material.JUNGLE_BUTTON);
        blocksThatPop.add(Material.ACACIA_BUTTON);
        blocksThatPop.add(Material.DARK_OAK_BUTTON);
        blocksThatPop.add(Material.STONE_BUTTON);
        blocksThatPop.add(Material.VINE);
        blocksThatPop.add(Material.LILY_PAD);
        blocksThatPop.add(Material.TRIPWIRE_HOOK);
        blocksThatPop.add(Material.SUNFLOWER);
        blocksThatPop.add(Material.LILAC);
        blocksThatPop.add(Material.ROSE_BUSH);
        blocksThatPop.add(Material.PEONY);
        blocksThatPop.add(Material.TALL_GRASS);
        blocksThatPop.add(Material.LARGE_FERN);
        blocksThatPop.add(Material.OAK_DOOR);
        blocksThatPop.add(Material.SPRUCE_DOOR);
        blocksThatPop.add(Material.BIRCH_DOOR);
        blocksThatPop.add(Material.ACACIA_DOOR);
        blocksThatPop.add(Material.JUNGLE_DOOR);
        blocksThatPop.add(Material.DARK_OAK_DOOR);
        blocksThatPop.add(Material.SNOW);
        blocksThatPop.add(Material.BELL);
        blocksThatPop.add(Material.LANTERN);
        blocksThatPop.add(Material.SWEET_BERRY_BUSH);
        blocksThatPop.add(Material.SCAFFOLDING);
        blocksThatPop.add(Material.TUBE_CORAL);
        blocksThatPop.add(Material.BRAIN_CORAL);
        blocksThatPop.add(Material.BUBBLE_CORAL);
        blocksThatPop.add(Material.FIRE_CORAL);
        blocksThatPop.add(Material.HORN_CORAL);
        blocksThatPop.add(Material.TUBE_CORAL_FAN);
        blocksThatPop.add(Material.BRAIN_CORAL_FAN);
        blocksThatPop.add(Material.BUBBLE_CORAL_FAN);
        blocksThatPop.add(Material.FIRE_CORAL_FAN);
        blocksThatPop.add(Material.HORN_CORAL_FAN);
        blocksThatPop.add(Material.DEAD_TUBE_CORAL);
        blocksThatPop.add(Material.DEAD_BRAIN_CORAL);
        blocksThatPop.add(Material.DEAD_BUBBLE_CORAL);
        blocksThatPop.add(Material.DEAD_FIRE_CORAL);
        blocksThatPop.add(Material.DEAD_HORN_CORAL);
        blocksThatPop.add(Material.DEAD_TUBE_CORAL_FAN);
        blocksThatPop.add(Material.DEAD_BRAIN_CORAL_FAN);
        blocksThatPop.add(Material.DEAD_BUBBLE_CORAL_FAN);
        blocksThatPop.add(Material.DEAD_FIRE_CORAL_FAN);
        blocksThatPop.add(Material.DEAD_HORN_CORAL_FAN);
        blocksThatPop.add(Material.WHITE_BANNER);
        blocksThatPop.add(Material.LIGHT_BLUE_BANNER);
        blocksThatPop.add(Material.LIGHT_GRAY_BANNER);
        blocksThatPop.add(Material.BLUE_BANNER);
        blocksThatPop.add(Material.GREEN_BANNER);
        blocksThatPop.add(Material.LIME_BANNER);
        blocksThatPop.add(Material.YELLOW_BANNER);
        blocksThatPop.add(Material.PINK_BANNER);
        blocksThatPop.add(Material.RED_BANNER);
        blocksThatPop.add(Material.MAGENTA_BANNER);
        blocksThatPop.add(Material.PURPLE_BANNER);
        blocksThatPop.add(Material.BLACK_BANNER);
        blocksThatPop.add(Material.GRAY_BANNER);
        blocksThatPop.add(Material.ORANGE_BANNER);
        blocksThatPop.add(Material.CYAN_BANNER);
        blocksThatPop.add(Material.BROWN_BANNER);
        blocksThatPop.add(Material.WHITE_BED);
        blocksThatPop.add(Material.LIGHT_BLUE_BED);
        blocksThatPop.add(Material.LIGHT_GRAY_BED);
        blocksThatPop.add(Material.BLUE_BED);
        blocksThatPop.add(Material.GREEN_BED);
        blocksThatPop.add(Material.LIME_BED);
        blocksThatPop.add(Material.YELLOW_BED);
        blocksThatPop.add(Material.PINK_BED);
        blocksThatPop.add(Material.RED_BED);
        blocksThatPop.add(Material.MAGENTA_BED);
        blocksThatPop.add(Material.PURPLE_BED);
        blocksThatPop.add(Material.BLACK_BED);
        blocksThatPop.add(Material.GRAY_BED);
        blocksThatPop.add(Material.ORANGE_BED);
        blocksThatPop.add(Material.CYAN_BED);
        blocksThatPop.add(Material.BROWN_BED);
    }
    public HashMap<Player, Block> aaTargets = new HashMap<>();
    public HashMap<Player, Block> cannonTargets = new HashMap<>();
    public boolean notTickingAnymore = false;
    public Craft(Movecraft plugin, CraftType type, Set<Block> blocks, Player pilot){
        this.movecraft = plugin;
        this.type = type;
        this.blocks = blocks;
        this.pilot = pilot;
        canFly = type.flight!=null;
        canDive = type.dive!=null;
        if(type.type!=CraftType.SUBCRAFT){
            movecraft.tickingCrafts.add(this);
        }
        world = pilot.getWorld();
        updateHull(null, 0, false, null);
    }
    public void tick(){
        if(type.type==CraftType.SUBCRAFT)return;
        int moveTime = getMovementDetails().moveTime;
        if(sinking){
            timer++;
            if(timer>=moveTime/2){
                timer-=moveTime/2;
                sink();
            }
            return;
        }
        if(damageReport>0)damageReportTimer++;
        if(damageReportTimer>=movecraft.damageTimeout)damageReport = damageReportTimer = 0;
        modeTimer++;
        setMode(NONE);
        if(movecraft.combatAND){
            if(copilots.size()+1>=movecraft.combatPilots&&getCrew().size()>=movecraft.combatCrew){
                setMode(COMBAT);
            }
        }else{
            if(copilots.size()+1>=movecraft.combatPilots||getCrew().size()>=movecraft.combatCrew){
                setMode(COMBAT);
            }
        }
        if(isPilotOnBoard()){
            repilotTimer = 0;
        }else{
            repilotTimer++;
            if(repilotTimer>20*10*30){
                repilot();
            }
        }
        if(maneuverTimer<moveTime)maneuverTimer++;
        if(isUnderwater(true)){
            if(!canDive){
                involuntaryTimer++;
                if(involuntaryTimer>=moveTime/Math.max(getMovementDetails().horizDist, getMovementDetails().vertDist)){
                    if(canFly){
                        move(0, 1, 0, false);
                    }else{
                        if(!move(0, -1, 0, false))startSinking();
                    }
                    involuntaryTimer = 0;
                }
            }
        }else{
            if(!canFly){
                involuntaryTimer++;
                if(involuntaryTimer>=moveTime/Math.max(getMovementDetails().horizDist, getMovementDetails().vertDist)){
                    if(!move(0, -1, 0, false))startSinking();
                    involuntaryTimer = 0;
                }
            }
        }
        if(cruise==Direction.NONE){
            timer = 0;
        }else{
            timer++;
            if(timer>=moveTime){
                timer-=moveTime;
                move();
            }
        }
        if(type.type!=CraftType.CRAFT)return;
        String text = "";
        if(type.flight!=null){
            for(ArrayList<Material> m : type.flight.requiredRatios.keySet()){
                float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                float ratio = type.flight.requiredRatios.get(m);
                float error = Math.abs((actual-ratio)/ratio);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<ratio)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+friendlyName(m.get(0))+ChatColor.RESET+": "+percent(actual, 2)+"/"+percent(ratio, 2);
            }
            for(ArrayList<Material> m : type.flight.requiredBlocks.keySet()){
                int actual = movecraft.getBlocks(blocks, m);//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                int blocks = type.flight.requiredBlocks.get(m);
                float error = Math.abs((actual-blocks)/(float)blocks);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<blocks)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+friendlyName(m.get(0))+ChatColor.RESET+": "+actual+"/"+blocks;
            }
        }
        if(type.dive!=null){
            for(ArrayList<Material> m : type.dive.requiredRatios.keySet()){
                float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                float ratio = type.dive.requiredRatios.get(m);
                float error = Math.abs((actual-ratio)/ratio);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<ratio)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+friendlyName(m.get(0))+ChatColor.RESET+": "+percent(actual, 2)+"/"+percent(ratio, 2);
            }
            for(ArrayList<Material> m : type.dive.requiredBlocks.keySet()){
                int actual = movecraft.getBlocks(blocks, m);//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                int blocks = type.dive.requiredBlocks.get(m);
                float error = Math.abs((actual-blocks)/(float)blocks);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<blocks)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+friendlyName(m.get(0))+ChatColor.RESET+": "+actual+"/"+blocks;
            }
        }
        if(isUnderwater(true)){
            for(ArrayList<Material> m : type.dive.requiredEngineRatios.keySet()){
                float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                float ratio = type.dive.requiredEngineRatios.get(m);
                float error = Math.abs((actual-ratio)/ratio);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<ratio)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+"Engines- "+friendlyName(m.get(0))+ChatColor.RESET+": "+percent(actual, 2)+"/"+percent(ratio, 2);
            }
            for(ArrayList<Material> m : type.dive.requiredEngineBlocks.keySet()){
                int actual = movecraft.getBlocks(blocks, m);//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                int blocks = type.dive.requiredEngineBlocks.get(m);
                float error = Math.abs((actual-blocks)/(float)blocks);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<blocks)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+"Engines- "+friendlyName(m.get(0))+ChatColor.RESET+": "+actual+"/"+blocks;
            }
        }else if(type.flight!=null){
            for(ArrayList<Material> m : type.flight.requiredEngineRatios.keySet()){
                float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                float ratio = type.flight.requiredEngineRatios.get(m);
                float error = Math.abs((actual-ratio)/ratio);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<ratio)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+"Engines- "+friendlyName(m.get(0))+ChatColor.RESET+": "+percent(actual, 2)+"/"+percent(ratio, 2);
            }
            for(ArrayList<Material> m : type.flight.requiredEngineBlocks.keySet()){
                int actual = movecraft.getBlocks(blocks, m);//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                int blocks = type.flight.requiredEngineBlocks.get(m);
                float error = Math.abs((actual-blocks)/(float)blocks);
                ChatColor color = ChatColor.GREEN;
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                if(actual<blocks)color = ChatColor.DARK_RED;
                text+=", "+color.toString()+"Engines- "+friendlyName(m.get(0))+ChatColor.RESET+": "+actual+"/"+blocks;
            }
        }
        switch(mode){
            case COMBAT:
                ChatColor color = ChatColor.GREEN;
                float error = Math.abs((blocks.size()-type.minSize)/(float)type.minSize);
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                text+=", "+type.minSize+"<"+color+blocks.size()+ChatColor.RESET+"<"+type.maxSize;
                if(!text.isEmpty())text = text.substring(2);
                if(damageReport>0){
                    text+=" | "+ChatColor.RED+"Took "+ChatColor.DARK_RED+damageReport+ChatColor.RED+" damage!";
                }
                //TODO what if the ship's on fire?
                //TODO ammo readout
                actionbarCrew(text);
                break;
            case CONSTRUCTION:
                for(ArrayList<Material> m : type.bannedRatios.keySet()){
                    float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                    float ratio = type.bannedRatios.get(m);
                    error = Math.abs((actual-ratio)/ratio);
                    color = ChatColor.GREEN;
                    if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                    if(error<movecraft.redThreshold)color = ChatColor.RED;
                    if(actual>ratio)color = ChatColor.DARK_RED;
                    text+=", "+color.toString()+friendlyName(m.get(0))+ChatColor.RESET+": "+percent(actual, 2)+"/"+percent(ratio, 2);
                }
                for(ArrayList<Material> m : type.limitedBlocks.keySet()){
                    int actual = movecraft.getBlocks(blocks, m);//TODO OPTOMIZE--don't do this every tick, only when the ship changes
                    int limit = type.limitedBlocks.get(m);
                    error = Math.abs((actual-limit)/(float)limit);
                    color = ChatColor.GREEN;
                    if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                    if(error<movecraft.redThreshold)color = ChatColor.RED;
                    if(actual>limit)color = ChatColor.DARK_RED;
                    text+=", "+color.toString()+friendlyName(m.get(0))+ChatColor.RESET+": "+actual+"/"+limit;
                }
                color = ChatColor.GREEN;
                error = Math.abs((blocks.size()-type.minSize)/(float)type.minSize);
                if(error<movecraft.yellowThreshold)color = ChatColor.YELLOW;
                if(error<movecraft.redThreshold)color = ChatColor.RED;
                text+=", "+type.minSize+"<"+color+blocks.size()+ChatColor.RESET+"<"+type.maxSize;
                if(!text.isEmpty())text = text.substring(2);
                actionbarCrew(text);
                break;
            case NONE:
                int crew = getCrew().size();
                int pilots = copilots.size()+1;
                if(crew>1){
                    text = "Total Crew: "+crew+"/"+getCrewNames().size();
                    if(pilots>1){
                        text = "Pilots: "+pilots+"/"+getPilotNames().size()+" | "+text;
                    }
                    actionbarPilots(text);
                }
                break;
        }
        for(Player p : aaDirectors){
            if(aaTargets.containsKey(p)){
                actionbar(p, ChatColor.GREEN+"Targeted "+ChatColor.RESET+aaTargets.get(p).getX()+" "+aaTargets.get(p).getY()+" "+aaTargets.get(p).getZ());
            }
        }
        for(Player p : cannonDirectors){
            if(cannonTargets.containsKey(p)){
                actionbar(p, ChatColor.GREEN+"Targeted "+ChatColor.RESET+cannonTargets.get(p).getX()+" "+cannonTargets.get(p).getY()+" "+cannonTargets.get(p).getZ());
            }
        }
    }
    public void cruise(Direction direction){
        if(cruise==direction){
            cruise = Direction.NONE;
        }else{
            cruise = direction;
        }
        updateSigns();
    }
    public void release(){
        if(!copilots.isEmpty()){
            repilot();
            return;
        }
        notTickingAnymore = true;
        notifyCrew("Craft released.");
        movecraft.crafts.remove(this);
        movecraft.projectiles.remove(this);
    }
    private void rotate(int amount){
        if(checkDisabled())return;
        if(!checkFuel())return;
        rotateAbout(getOrigin().clone().subtract(0.5,0.5,0.5), amount);
    }
    public ArrayList<BlockMovement> rotateAbout(Location origin, int amount){//rotate about the block
        origin.setX(Math.round(origin.getX()));
        origin.setY(Math.round(origin.getY()));
        origin.setZ(Math.round(origin.getZ()));
        while(amount>=4)amount-=4;
        while(amount<0)amount+=4;
        ArrayList<BlockMovement> movements = new ArrayList<>();
        for(Block block : getMovableBlocks()){
            movements.add(new BlockMovement(block.getLocation(), rotate(block.getLocation(), origin, amount), amount));
        }
        Iterable<Entity> entities = move(movements, false);
        if(entities==null)return null;
        for(Entity e : entities){
            Location l = e.getLocation();
            l.setYaw(l.getYaw()+90*amount);
            e.teleport(l, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        while(amount>0){
            amount--;
            switch(cruise){
                case NORTH:
                    cruise = Direction.EAST;
                    break;
                case EAST:
                    cruise = Direction.SOUTH;
                    break;
                case SOUTH:
                    cruise = Direction.WEST;
                    break;
                case WEST:
                    cruise = Direction.NORTH;
                    break;
            }
        }
        return movements;
    }
    private void move(){
        if(checkDisabled())return;
        if(!checkFuel())return;
        ArrayList<BlockMovement> movements = new ArrayList<>();
        movecraft.debug(pilot, "Moving "+cruise.toString());
        movecraft.debug(pilot, "Compiling BlockMovements");
        MovementDetails movement = getMovementDetails();
        int x = cruise.x;
        int y = cruise.y;
        int z = cruise.z;
        if(type.type==CraftType.PROJECTILE){
            movecraft.debug(pilot, "Projectile original move "+x+" "+y+" "+z);
            movecraft.debug(pilot, "Projectile move details: fd|vert|horiz "+type.moveForward+"|"+type.moveVert+"|"+type.moveHoriz);
            y = type.moveVert;
            if(cruise.x!=0){//x
                x *= type.moveForward;
                z *= type.moveHoriz;
            }else{//z
                z *= type.moveForward;
                x *= type.moveHoriz;
            }
            movecraft.debug(pilot, "Projectile moving "+x+" "+y+" "+z);
        }
        for(Block block : getMovableBlocks()){
            movements.add(new BlockMovement(block.getLocation(), block.getRelative(x*movement.horizDist, y*movement.vertDist, z*movement.horizDist).getLocation()));
        }
        movecraft.debug(pilot, "Compiled BlockMovements");
        boolean success = move(movements, false)!=null;
        if(type.type==CraftType.PROJECTILE&&!success){
            move(movements, false);
        }
    }
    private void move(BlockFace face, int distance){
        movecraft.debug(pilot, "Moving "+face.toString());
        if(checkDisabled())return;
        if(!checkFuel())return;
        ArrayList<BlockMovement> movements = new ArrayList<>();
        movecraft.debug(pilot, "Compiling BlockMovements");
        for(Block block : getMovableBlocks()){
            Block newBlock = block;
            for(int i = 0; i<distance; i++)newBlock = newBlock.getRelative(face);
            movements.add(new BlockMovement(block.getLocation(), newBlock.getLocation()));
        }
        movecraft.debug(pilot, "Compiled BlockMovements");
        move(movements, false);
    }
    private boolean move(int x, int y, int z, boolean voluntary){
        movecraft.debug(pilot, "Moving "+x+" "+y+" "+z);
        if(voluntary){
            if(checkDisabled())return false;
            if(!checkFuel())return false;
        }
        ArrayList<BlockMovement> movements = new ArrayList<>();
        movecraft.debug(pilot, "Compiling BlockMovements");
        for(Block block : getMovableBlocks()){
            movements.add(new BlockMovement(block.getLocation(), block.getRelative(x,y,z).getLocation()));
        }
        movecraft.debug(pilot, "Compiled BlockMovements");
        return move(movements, false)!=null;
    }
    private Iterable<Entity> move(Collection<BlockMovement> movements, boolean force){
        if(blocks.isEmpty())return null;
        boolean underwaterMove = isUnderwater(false)&&((type.dive!=null&&canDive)||(type.dive!=null&&type.flight!=null&&canFly&&!canDive));
        int waterLevel = 0;
        if(underwaterMove)waterLevel = getWaterLevel();
        for(Block block : blocks){
            if(block.getType()==Material.AIR){
                criticalError(41693, "AIR BLOCKS FOUND ON SHIP!");
                return null;
            }
        }
        HashMap<Entity, Location> entityMovements = new HashMap<>();
        HashMap<OfflinePlayer, Location> spawnMovements = new HashMap<>();
        int entityRotation = 0;
        for(Entity entity : world.getNearbyEntities(getBoundingBox().expand(BlockFace.UP, 2))){
            Block b = world.getBlockAt(entity.getLocation());
            for(int i = 0; i<2; i++){
                if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN){
                    b = b.getRelative(BlockFace.DOWN);
                }
            }
            if(blocks.contains(b)){
                for(BlockMovement m : movements){
                    if(m.from.equals(b.getLocation())){
                        Location diff = m.to.clone().subtract(m.from.clone());
                        entityMovements.put(entity, rotate(entity.getLocation().clone().add(diff), m.to.clone().add(.5, 0, .5), m.rotation));
                        entityRotation = m.rotation;
                        break;
                    }
                }
            }
        }
        for(OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()){
            if(p.getBedSpawnLocation()==null)continue;
            if(getBoundingBox().expand(BlockFace.UP, 2).contains(p.getBedSpawnLocation().toVector())){
                Block b = world.getBlockAt(p.getBedSpawnLocation());
                for(int i = 0; i<2; i++){
                    if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN){
                        b = b.getRelative(BlockFace.DOWN);
                    }
                }
                if(blocks.contains(b)){
                    for(BlockMovement m : movements){ 
                       if(m.from.equals(b.getLocation())){
                            Location diff = m.to.clone().subtract(m.from.clone());
                            spawnMovements.put(p, rotate(p.getBedSpawnLocation().clone().add(diff), m.to.clone().add(.5, 0, .5), m.rotation));
                            entityRotation = m.rotation;
                            break;
                        }
                    }
                }
            }
        }
        if(!force){
            movecraft.debug(pilot, "Checking Collisions");
            for(BlockMovement movement : movements){
                Block newLocation = world.getBlockAt(movement.to);
                if(!world.getBlockAt(movement.from).getChunk().isLoaded()||!newLocation.getChunk().isLoaded()){
                    if(type.type==CraftType.PROJECTILE){
                        startSinking();
                    }else{
                        notifyPilots("Ship not loaded! ("+newLocation.getX()+","+newLocation.getY()+","+newLocation.getZ()+") Stopping...");
                        playSound(getPilots(), newLocation.getLocation(), Sound.ENTITY_WITHER_AMBIENT, .5f);
                        cruise = Direction.NONE;
                    }
                    return null;
                }
                if(!(blocks.contains(newLocation)||newLocation.getType()==Material.AIR||newLocation.getType()==Material.CAVE_AIR||newLocation.getType()==Material.FIRE||(underwaterMove&&(newLocation.getType()==Material.WATER||newLocation.getType()==Material.BUBBLE_COLUMN)))){
                    if(type.collisionDamage!=0){
                        world.createExplosion(newLocation.getLocation().add(0.5,0.5,0.5), type.collisionDamage);
                    }else{
                        notifyPilots("Craft obstructed by "+newLocation.getType().toString()+"! ("+newLocation.getX()+","+newLocation.getY()+","+newLocation.getZ()+")");
                        playSound(getPilots(), newLocation.getLocation(), Sound.BLOCK_ANVIL_LAND, .5f);
                    }
                    return null;
                }
            }
            fuel--;
        }
        movecraft.debug(pilot, "Prepared Move");
        ArrayList<BlockChange> changes = new ArrayList<>();
        ArrayList<Block> newBlocks = new ArrayList<>();
        HashSet<Block> blox = new HashSet<>(blocks);
        for(BlockMovement movement : movements){ 
            Block movesFrom = world.getBlockAt(movement.from);
            Block movesTo = world.getBlockAt(movement.to);
            if(movesFrom.getType()==Material.AIR){
                criticalError(39614, "AIR BLOCKS FOUND ON SHIP!");
                return null;
            }
            if(movesTo.getType()!=movesFrom.getType()||!isInert(movesFrom.getType())){
                changes.add(new BlockChange(movesTo, movesFrom, movement.rotation));
            }
            newBlocks.add(movesTo);
            blox.remove(movesTo);
        }
        movecraft.debug(pilot, "Mid-compiled BlockChanges");
        finishCompilingBlockChanges(underwaterMove, waterLevel, changes, blox);
        Collections.sort(changes);
        movecraft.debug(pilot, "Sorted BlockChanges");
//        int created = 0;
//        int destroyed = 0;
//        for(BlockChange change : changes){
//            if(change.type==Material.AIR&&change.block.getType()!=Material.AIR)destroyed++;
//            if(change.type!=Material.AIR&&change.block.getType()==Material.AIR)created++;
//            if(change.type==Material.WATER&&change.block.getType()!=Material.WATER)destroyed++;
//            if(change.type!=Material.WATER&&change.block.getType()==Material.WATER)created++;
//        }
//        if(created-destroyed!=0){
////            criticalError(81037, "NET CHANGE IS NOT EQUAL TO 0!"); 
////            return null;
//        }
        moving = true;
        for(Block block : blocks){
            if(blocksThatPop.contains(block.getType())){
                block.setType(Material.AIR, false);
            }
        }
        movecraft.debug(pilot, "Airified popping blocks");
        for(BlockChange change : changes){
            if(!blocksThatPop.contains(change.type))change.change();
        }
        movecraft.debug(pilot, "Changed non-popping blocks");
        for(BlockChange change : changes){
            if(blocksThatPop.contains(change.type)){
                change.change();
            }
        }
        movecraft.debug(pilot, "Changed popping blocks");
        for(BlockMovement movement : movements){
            movement.move1(this);
        }
        for(BlockMovement movement : movements){
            movement.move2(this);
        }
        movecraft.debug(pilot, "Moved Ship Blocks");
        moving = false;
        blocks.clear();
        blocks.addAll(newBlocks);
        if(underwaterMove){
            Set<Block> outsideBlocks = new HashSet<>();
            Set<Block> outerHull = new HashSet<>();
            Set<Block> innerShip = new HashSet<>();
            scanHull(outsideBlocks, outerHull, innerShip);
            for(Block b : outerHull){
                if(b.getBlockData() instanceof Waterlogged){
                    Waterlogged l = (Waterlogged) b.getBlockData();
                    l.setWaterlogged(b.getY()<=waterLevel);
                    b.setBlockData(l);
                }
            }
            innerShip.removeAll(blocks);
            for(Block b : innerShip){
                if(b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN)b.setType(Material.AIR);
                if(b.getBlockData() instanceof Waterlogged){
                    Waterlogged l = (Waterlogged) b.getBlockData();
                    l.setWaterlogged(false);
                    b.setBlockData(l);
                }
            }
            movecraft.debug(pilot, "Recalculated Waterlogged blocks");
        }
        calculateBoundingBox();
        for(Entity e : entityMovements.keySet()){
            e.teleport(entityMovements.get(e), PlayerTeleportEvent.TeleportCause.PLUGIN);
            if(e instanceof Hanging){
                Hanging f = (Hanging) e;
                int amount = entityRotation;
                while(amount>0){
                    amount--;
                    switch(f.getFacing()){
                        case NORTH:
                            f.setFacingDirection(BlockFace.EAST);
                            break;
                        case EAST:
                            f.setFacingDirection(BlockFace.SOUTH);
                            break;
                        case SOUTH:
                            f.setFacingDirection(BlockFace.WEST);
                            break;
                        case WEST:
                            f.setFacingDirection(BlockFace.UP);
                            break;
                    }
                }
            }
        }
        for(OfflinePlayer p : spawnMovements.keySet()){
            p.getPlayer().setBedSpawnLocation(spawnMovements.get(p));
        }
        movecraft.debug(pilot, "Moved Players");
        for(Player player : getPilots()){
            if(player.getLocation().distance(getOrigin())>500){
                actionbar(player, "Ship moved to ("+getOrigin().getBlockX()+", "+getOrigin().getBlockY()+", "+getOrigin().getBlockZ()+")");
            }
        }
        signs = null;
        updateSigns();
        return entityMovements.keySet();
    }
    private Location getOrigin(){
        return getBoundingBox().getCenter().toLocation(world);
    }
    BoundingBox getBoundingBox(){
        if(bbox==null)calculateBoundingBox();
        return bbox;
    }
    private void calculateBoundingBox(){
        if(blocks.isEmpty())bbox = new BoundingBox(0, 0, 0, 0, 0, 0);
        int x1,y1,z1,x2,y2,z2;
        x1 = y1 = z1 = Integer.MAX_VALUE;
        x2 = y2 = z2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            x1 = Math.min(x1, block.getX());
            y1 = Math.min(y1, block.getY());
            z1 = Math.min(z1, block.getZ());
            x2 = Math.max(x2, block.getX());
            y2 = Math.max(y2, block.getY());
            z2 = Math.max(z2, block.getZ());
        }
        bbox =  new BoundingBox(x1, y1, z1, x2+1, y2+1, z2+1);
    }
    public boolean removeBlock(Player player, Block b, boolean damage){
        if(moving)return false;
        signs = null;
        if(damage)setMode(COMBAT);
        else setMode(CONSTRUCTION);
        movecraft.debug(pilot, "Breaking block; damage: "+damage);
        if(updateHull(player, blocks.remove(b)?1:0, damage, b.getLocation())){
            return true;
        }else{
            blocks.add(b);
            return false;
        }
    }
    int multiDamage = 0;
    public void startRemoveBlocks(Player player, Block b, boolean damage){
        if(moving)return;
        signs = null;
        movecraft.debug(pilot, "Adding blocks to break; so far: "+(multiDamage+1));
        if(damage)setMode(COMBAT);
        else setMode(CONSTRUCTION);
        multiDamage++;
        blocks.remove(b);
    }
    public void finishRemoveBlocks(){
        signs = null;
        movecraft.debug(pilot, "Breaking blocks; damage: "+multiDamage);
        updateHull(null, multiDamage, true, null);
        multiDamage = 0;
    }
    public boolean updateHull(Player player, int damage, boolean damaged, Location l){
        signs = null;
        movecraft.debug(pilot, "Updating hull; damage: "+damage+" "+damaged);
        for(Iterator<Block> it = blocks.iterator(); it.hasNext();){
            Block block = it.next();
            if(type.bannedBlocks.contains(block.getType())){
                it.remove();
                damage++;
            }
        }
        if(damage>0){
            damageReport += damage;
            damageReportTimer = 0;
        }
        if(blocks.size()<type.minSize&&type.type!=CraftType.PROJECTILE){
            if(damaged)startSinking();
            else{
                notifyBlockChange(player, "Craft too small!");
                return false;
            }
        }
        if(type.flight!=null){
            boolean fly = true;
            for(ArrayList<Material> materials : type.flight.requiredRatios.keySet()){
                float requiredRatio = type.flight.requiredRatios.get(materials);
                int amount = movecraft.getBlocks(blocks, materials);
                float actualRatio = amount/(float)blocks.size();
                if(actualRatio<requiredRatio){
                    fly = false;
                    if(!damaged){
                        notifyBlockChange(player, "Not enough flight blocks: "+materials.get(0).toString()+" or similar! ("+percent(actualRatio,2)+"<"+percent(requiredRatio,2)+")");
                        if(canFly)return false;
                    }
                }
            }
            for(ArrayList<Material> materials : type.flight.requiredBlocks.keySet()){
                int required = type.flight.requiredBlocks.get(materials);
                int actual = movecraft.getBlocks(blocks, materials);
                if(actual<required){
                    fly = false;
                    if(!damaged){
                        notifyBlockChange(player, "Not enough flight blocks: "+materials.get(0).toString()+" or similar! ("+actual+"<"+required+")");
                        if(canFly)return false;
                    }
                }
            }
            canFly = fly;
        }
        if(type.dive!=null){
            boolean dive = true;
            for(ArrayList<Material> materials : type.dive.requiredRatios.keySet()){
                float requiredRatio = type.dive.requiredRatios.get(materials);
                int amount = movecraft.getBlocks(blocks, materials);
                float actualRatio = amount/(float)blocks.size();
                if(actualRatio<requiredRatio){
                    dive = false;
                    if(!damaged){
                        notifyBlockChange(player, "Not enough dive blocks: "+materials.get(0).toString()+" or similar! ("+percent(actualRatio,2)+"<"+percent(requiredRatio,2)+")");
                        if(canDive)return false;
                    }
                }
            }
            for(ArrayList<Material> materials : type.dive.requiredBlocks.keySet()){
                int required = type.dive.requiredBlocks.get(materials);
                int actual = movecraft.getBlocks(blocks, materials);
                if(actual<required){
                    dive = false;
                    if(!damaged){
                        notifyBlockChange(player, "Not enough dive blocks: "+materials.get(0).toString()+" or similar! ("+actual+"<"+required+")");
                        if(canDive)return false;
                    }
                }
            }
            canDive = dive;
        }
        if(damaged){
            for(ArrayList<Material> material : type.bannedRatios.keySet()){
                float ratio = type.bannedRatios.get(material);
                int amount = movecraft.getBlocks(blocks, material);
                float actual = amount/(float)blocks.size();
                if(actual>ratio){
                    notifyBlockChange(player, "Too many blocks: "+material.toString()+"! ("+percent(actual,2)+">"+percent(ratio,2)+")");
                    return false;
                }
            }
        }
        updateDisabled();
        if(l!=null){
            if(!damaged){
                playSound(player, l, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, .95f);
            }else{
                playSound(player, l, Sound.ENTITY_GENERIC_EXPLODE, 2);
            }
        }
        calculateBoundingBox();
        updateSigns();
        return true;
    }
    public void playSound(Player player, Location location, Sound sound, float pitch){
        if(player==null)return;
        if(location==null)location = player.getLocation();
        player.playSound(location, sound, SoundCategory.MASTER, 100, pitch);
    }
    public void playSound(Iterable<Player> players, Location location, Sound sound, float pitch){
        for(Player p : players)playSound(p, location, sound, pitch);
    }
    public boolean addBlock(Block block, boolean force){
        return addBlock(null, block, force);
    }
    public boolean addBlock(Player player, Block block, boolean force){
        if(moving){
            return false;
        }
        setMode(CONSTRUCTION);
        if(type.bannedBlocks.contains(block.getType())){
            notifyBlockChange(player, block.getType()+" is not allowed on crafts!");
            return false;
        }
        ArrayList<Block> craft = new ArrayList<>(blocks);
        craft.add(block);
        if(craft.size()>type.maxSize){
            notifyBlockChange(player, "Craft too large!");
            return false;
        }
        if(type.flight!=null){
            boolean fly = true;
            for(ArrayList<Material> materials : type.flight.requiredRatios.keySet()){
                float requiredRatio = type.flight.requiredRatios.get(materials);
                int amount = movecraft.getBlocks(craft, materials);
                float actualRatio = amount/(float)craft.size();
                if(actualRatio<requiredRatio){
                    fly = false;
                    notifyBlockChange(player, "Not enough flight blocks: "+materials.get(0).toString()+" or similar! ("+percent(actualRatio,2)+"<"+percent(requiredRatio,2)+")");
                    if(canFly)return false;
                }
            }
            for(ArrayList<Material> materials : type.flight.requiredBlocks.keySet()){
                int required = type.flight.requiredBlocks.get(materials);
                int actual = movecraft.getBlocks(craft, materials);
                if(actual<required){
                    fly = false;
                    notifyBlockChange(player, "Not enough flight blocks: "+materials.get(0).toString()+" or similar! ("+actual+"<"+required+")");
                    if(canFly)return false;
                }
            }
            canFly = fly;
        }
        if(type.dive!=null){
            boolean dive = true;
            for(ArrayList<Material> materials : type.dive.requiredRatios.keySet()){
                float requiredRatio = type.dive.requiredRatios.get(materials);
                int amount = movecraft.getBlocks(craft, materials);
                float actualRatio = amount/(float)craft.size();
                if(actualRatio<requiredRatio){
                    dive = false;
                    notifyBlockChange(player, "Not enough dive blocks: "+materials.get(0).toString()+" or similar! ("+percent(actualRatio,2)+"<"+percent(requiredRatio,2)+")");
                    if(canDive)return false;
                }
            }
            for(ArrayList<Material> materials : type.dive.requiredBlocks.keySet()){
                int required = type.dive.requiredBlocks.get(materials);
                int actual = movecraft.getBlocks(craft, materials);
                if(actual<required){
                    dive = false;
                    notifyBlockChange(player, "Not enough dive blocks: "+materials.get(0).toString()+" or similar! ("+actual+"<"+required+")");
                    if(canDive)return false;
                }
            }
            canDive = dive;
        }
        for(ArrayList<Material> material : type.bannedRatios.keySet()){
            float ratio = type.bannedRatios.get(material);
            int amount = movecraft.getBlocks(craft, material);
            float actual = amount/(float)craft.size();
            if(actual>ratio){
                notifyBlockChange(player, "Too many blocks: "+material.toString()+"! ("+percent(actual,2)+">"+percent(ratio,2)+")");
                return false;
            }
        }
        for(ArrayList<Material> material : type.limitedBlocks.keySet()){
            int limit = type.limitedBlocks.get(material);
            int actual = movecraft.getBlocks(craft, material);
            if(actual>limit){
                notifyBlockChange(player, "Too many blocks: "+material.get(0).toString()+"! ("+actual+">"+limit+")");
                return false;
            }
        }
        playSound(player, block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f);
        blocks.add(block);
        updateDisabled();
        calculateBoundingBox();
        updateSigns();
        return true;
    }
    private Location rotate(Location location, Location origin){
        location.subtract(origin);
        double x = -location.getZ();
        double z = location.getX();
        location.setX(x);
        location.setZ(z);
        location.add(origin);
        return location;
    }
    private Location rotate(Location location, Location origin, int amount){
        while(amount>=4)amount-=4;
        while(amount<0)amount+=4;
        if(amount==0)return location;
        for(int i = 0; i<amount; i++){
            location = rotate(location, origin);
        }
        return location;
    }
    public void rotate(Sign sign, int i){
        sign.setLine(0, Movecraft.helm[0]);
        sign.setLine(1, Movecraft.helm[1]);
        sign.setLine(2, Movecraft.helm[2]);
        sign.update();
        rotate(i);
    }
    private void startSinking(){
        notifyCrew(ChatColor.RED+"This craft has taken too much damage and is now SINKING!");
        movecraft.crafts.remove(this);
        movecraft.projectiles.remove(this);
        movecraft.sinking.add(this);
        sinking = true;
        pilot = null;
    }
    private void sink(){
        ArrayList<BlockMovement> movements = new ArrayList<>();
        boolean somethingChanged = false;
        do{
            somethingChanged = false;
            for(Iterator<Block> it = blocks.iterator(); it.hasNext();){
                Block block = it.next();
                Block down = block.getRelative(BlockFace.DOWN);
                if(down.getType()==Material.AIR||down.getType()==Material.CAVE_AIR||down.getType()==Material.WATER||down.getType()==Material.BUBBLE_COLUMN||blocks.contains(down));
                else{
                    it.remove();
                    somethingChanged = true;
                }
            }
        }while(somethingChanged);
        if(blocks.isEmpty()){
            movecraft.sinking.remove(this);
            notTickingAnymore = true;
            return;
        }
        for(Block block : blocks){
            movements.add(new BlockMovement(block, block.getRelative(BlockFace.DOWN)));
        }
        move(movements, true);
    }
    public static void rotateBlock(BlockData data, int rotation){
        while(rotation>=4)rotation-=4;
        while(rotation<0)rotation+=4;
        if(rotation==0)return;
        for(int i = 0; i<rotation; i++)rotateBlock(data);
    }
    public static void rotateBlock(BlockData data){
        if(data instanceof RedstoneWire){
            RedstoneWire wire = (RedstoneWire)data;
            Connection n = wire.getFace(BlockFace.NORTH);
            Connection e = wire.getFace(BlockFace.EAST);
            Connection s = wire.getFace(BlockFace.SOUTH);
            Connection w = wire.getFace(BlockFace.WEST);
            wire.setFace(BlockFace.NORTH, w);
            wire.setFace(BlockFace.EAST, n);
            wire.setFace(BlockFace.SOUTH, e);
            wire.setFace(BlockFace.WEST, s);
        }
        if(data instanceof Directional){
            Directional d = (Directional) data;
            switch(d.getFacing()){
                case NORTH:
                    d.setFacing(BlockFace.EAST);
                    break;
                case EAST:
                    d.setFacing(BlockFace.SOUTH);
                    break;
                case SOUTH:
                    d.setFacing(BlockFace.WEST);
                    break;
                case WEST:
                    d.setFacing(BlockFace.NORTH);
                    break;
            }
        }
        if(data instanceof MultipleFacing){
            MultipleFacing m = (MultipleFacing) data;
            boolean n = m.hasFace(BlockFace.NORTH);
            boolean e = m.hasFace(BlockFace.EAST);
            boolean s = m.hasFace(BlockFace.SOUTH);
            boolean w = m.hasFace(BlockFace.WEST);
            m.setFace(BlockFace.NORTH, w);
            m.setFace(BlockFace.EAST, n);
            m.setFace(BlockFace.SOUTH, e);
            m.setFace(BlockFace.WEST, s);
        }
        if(data instanceof Orientable){
            Orientable o = (Orientable) data;
            switch(o.getAxis()){
                case X:
                    o.setAxis(Axis.Z);
                    break;
                case Z:
                    o.setAxis(Axis.X);
                    break;
            }
        }
        if(data instanceof Rail){
            Rail r = (Rail) data;
            switch(r.getShape()){
                case ASCENDING_EAST:
                    r.setShape(Rail.Shape.ASCENDING_SOUTH);
                    break;
                case ASCENDING_NORTH:
                    r.setShape(Rail.Shape.ASCENDING_EAST);
                    break;
                case ASCENDING_SOUTH:
                    r.setShape(Rail.Shape.ASCENDING_WEST);
                    break;
                case ASCENDING_WEST:
                    r.setShape(Rail.Shape.ASCENDING_NORTH);
                    break;
                case EAST_WEST:
                    r.setShape(Rail.Shape.NORTH_SOUTH);
                    break;
                case NORTH_EAST:
                    r.setShape(Rail.Shape.SOUTH_EAST);
                    break;
                case NORTH_SOUTH:
                    r.setShape(Rail.Shape.EAST_WEST);
                    break;
                case NORTH_WEST:
                    r.setShape(Rail.Shape.NORTH_EAST);
                    break;
                case SOUTH_EAST:
                    r.setShape(Rail.Shape.SOUTH_WEST);
                    break;
                case SOUTH_WEST:
                    r.setShape(Rail.Shape.NORTH_WEST);
                    break;
            }
        }
        if(data instanceof Rotatable){
            Rotatable r = (Rotatable) data;
            switch(r.getRotation()){
                case NORTH:
                    r.setRotation(BlockFace.EAST);
                    break;
                case EAST:
                    r.setRotation(BlockFace.SOUTH);
                    break;
                case SOUTH:
                    r.setRotation(BlockFace.WEST);
                    break;
                case WEST:
                    r.setRotation(BlockFace.NORTH);
                    break;
                case EAST_NORTH_EAST:
                    r.setRotation(BlockFace.SOUTH_SOUTH_EAST);
                    break;
                case EAST_SOUTH_EAST:
                    r.setRotation(BlockFace.SOUTH_SOUTH_WEST);
                    break;
                case NORTH_EAST:
                    r.setRotation(BlockFace.SOUTH_EAST);
                    break;
                case NORTH_NORTH_EAST:
                    r.setRotation(BlockFace.EAST_SOUTH_EAST);
                    break;
                case NORTH_NORTH_WEST:
                    r.setRotation(BlockFace.EAST_NORTH_EAST);
                    break;
                case NORTH_WEST:
                    r.setRotation(BlockFace.NORTH_EAST);
                    break;
                case SOUTH_EAST:
                    r.setRotation(BlockFace.SOUTH_WEST);
                    break;
                case SOUTH_SOUTH_EAST:
                    r.setRotation(BlockFace.WEST_SOUTH_WEST);
                    break;
                case SOUTH_SOUTH_WEST:
                    r.setRotation(BlockFace.WEST_NORTH_WEST);
                    break;
                case SOUTH_WEST:
                    r.setRotation(BlockFace.NORTH_WEST);
                    break;
                case WEST_NORTH_WEST:
                    r.setRotation(BlockFace.NORTH_NORTH_EAST);
                    break;
                case WEST_SOUTH_WEST:
                    r.setRotation(BlockFace.NORTH_NORTH_WEST);
                    break;
            }
        }
    }
    private PacketPlayOutChat actionbar(String text, UUID uid){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.GAME_INFO, uid);
        return packet;
    }
    private void actionbarPilot(String text){
        if(pilot==null)return;
        PacketPlayOutChat packet = actionbar(text, pilot.getUniqueId());
        ((CraftPlayer)pilot).getHandle().playerConnection.sendPacket(packet);
    }
    private void actionbarPilots(String text){
        for(Player player : getPilots()){
            PacketPlayOutChat packet = actionbar(text, player.getUniqueId());
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
        }
    }
    private void actionbarCrew(String text){
        for(Player player : getCrew()){
            PacketPlayOutChat packet = actionbar(text, player.getUniqueId());
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
        }
    }
    private void actionbar(Player player, String text){
        PacketPlayOutChat packet = actionbar(text, player.getUniqueId());
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }
    private void notifyPilot(String message){
        if(pilot!=null){
            if(type.type==CraftType.PROJECTILE)movecraft.debug(pilot, message);
            else pilot.sendMessage(message);
        }
    }
    private void notifyPilots(String message){
        for(Player player : getPilots()){
            if(type.type==CraftType.PROJECTILE)movecraft.debug(player, message);
            else player.sendMessage(message);
        }
    }
    private void notifyCrew(String message){
        for(Player player : getCrew()){
            if(type.type==CraftType.PROJECTILE)movecraft.debug(player, message);
            else player.sendMessage(message);
        }
    }
    private void refuel(){
        if(fuel>0)return;
        if(type.type==CraftType.PROJECTILE){
            startSinking();
            return;
        }
        if(type.fuels.isEmpty())fuel = Integer.MAX_VALUE;
        for(Block block : blocks){
            if(block.getType()==Material.FURNACE){
                FurnaceInventory furnace = ((Furnace)block.getState()).getInventory();
                for(Material m : type.fuels.keySet()){
                    if(furnace.getFuel()!=null&&furnace.getFuel().getType()==m){
                        ItemStack s = furnace.getFuel();
                        if(s.getAmount()==1)furnace.setFuel(new ItemStack(Material.AIR));
                        else{
                            s.setAmount(s.getAmount()-1);
                            furnace.setFuel(s);
                        }
                        fuel+=type.fuels.get(m);
                        return;
                    }
                    if(furnace.getSmelting()!=null&&furnace.getSmelting().getType()==m){
                        ItemStack s = furnace.getSmelting();
                        if(s.getAmount()==1)furnace.setSmelting(new ItemStack(Material.AIR));
                        else{
                            s.setAmount(s.getAmount()-1);
                            furnace.setSmelting(s);
                        }
                        fuel+=type.fuels.get(m);
                        return;
                    }
                    if(furnace.getResult()!=null&&furnace.getResult().getType()==m){
                        ItemStack s = furnace.getResult();
                        if(s.getAmount()==1)furnace.setResult(new ItemStack(Material.AIR));
                        else{
                            s.setAmount(s.getAmount()-1);
                            furnace.setResult(s);
                        }
                        fuel+=type.fuels.get(m);
                        return;
                    }
                }
            }
        }
    }
    private static boolean isInert(Material material){
        if(material.name().contains("_WOOL"))return true;
        if(material.name().contains("_PLANKS"))return true;
        if(material.name().contains("GLASS")&&!material.name().contains("PANE"))return true;
        if(material.name().contains("TERRACOTTA")&&!material.name().contains("GLAZED"))return true;
        switch(material){
            case END_STONE:
            case END_STONE_BRICKS:
            case REDSTONE_BLOCK:
                return true;
        }
        return false;
    }
    private void criticalError(int code, String error){
        cruise = Direction.NONE;
        notifyPilots(ChatColor.DARK_RED+"Critical error whilist moving ship:");
        notifyPilots(ChatColor.DARK_RED+error);
        notifyPilots(ChatColor.DARK_RED+"Your craft has been stopped to help prevent further damage.");
        notifyPilots(ChatColor.DARK_RED+""+ChatColor.BOLD+"Error Code: "+code);
        notifyPilots(ChatColor.DARK_RED+"Please send this code, along with as many details as possible to ThizThizzyDizzy so the problem can be fixed.");
    }
    public boolean isPilotOnBoard(){
        return isOnBoard(pilot);
    }
    public boolean isOnBoard(Entity entity){
        Block b = world.getBlockAt(entity.getLocation());
        for(int i = 0; i<2; i++){
            if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN){
                b = b.getRelative(BlockFace.DOWN);
            }
        }
        if(blocks.contains(b)){
            return true;
        }
        return false;
    }
    public void maneuver(int x, int y, int z){
        if(maneuverTimer<getMovementDetails().moveTime/Math.min(getMovementDetails().horizDist, getMovementDetails().vertDist))return;
        timer = maneuverTimer = 0;
        move(x, y, z, true);
    }
    public void rotateSubcraft(Craft craft, Player player, Block sign, int amount, String name){
        ArrayList<BlockMovement> movements = craft.rotateAbout(sign.getLocation(), amount);
        if(movements==null)return;
        for(BlockMovement m : movements){
            blocks.remove(world.getBlockAt(m.from));
        }
        for(BlockMovement m : movements){
            blocks.add(world.getBlockAt(m.to));
        }
        actionbarPilots("Subcraft rotated: "+ChatColor.AQUA+name);
    }
    private void updateSigns(){
        for(Sign sign : getSigns()){
            CraftSign cs = CraftSign.getSign(sign);
            if(cs!=null){
                cs.update(this, sign);
            }
        }
    }
    public Set<String> getCrewNames(){
        HashSet<String> crew = new HashSet<>();
        for(Sign sign : getSigns()){
            if(sign.getLine(0).equalsIgnoreCase("Crew:")||sign.getLine(0).equalsIgnoreCase("Pilot:")){
                crew.add(sign.getLine(1));
                crew.add(sign.getLine(2));
                crew.add(sign.getLine(3));
            }
        }
        return crew;
    }
    public Set<String> getPilotNames(){
        HashSet<String> pilots = new HashSet<>();
        for(Sign sign : getSigns()){
            if(sign.getLine(0).equalsIgnoreCase("Pilot:")){
                pilots.add(sign.getLine(1));
                pilots.add(sign.getLine(2));
                pilots.add(sign.getLine(3));
            }
        }
        return pilots;
    }
    public ArrayList<Player> getPilots(){
        ArrayList<Player> pilots = new ArrayList<>();
        if(pilot!=null)pilots.add(pilot);
        pilots.addAll(copilots);
        return pilots;
    }
    public ArrayList<Player> getCrew(){
        HashSet<Player> crew = new HashSet<>();
        crew.addAll(getPilots());
        for(String s : getCrewNames()){
            for(Player p : pilot.getWorld().getPlayers()){
                if(p.getName().equals(s)){
                    if(isOnBoard(p))crew.add(p);
                }
            }
        }
        return new ArrayList<>(crew);
    }
    public boolean isCrew(Player player){
        if(isPilot(player))return true;
        boolean crew = true;
        for(String s : getCrewNames()){
            crew = false;
            if(player.getName().equals(s))return true;
        }
        return crew;
    }
    public boolean isPilot(Player player){
        boolean pilot = true;
        for(String s : getPilotNames()){
            pilot = false;
            if(player.getName().equals(s))return true;
        }
        return pilot;
    }
    public boolean checkCrew(Player player){
        if(player==null)return true;
        boolean crew = isCrew(player);
        if(!crew){
            player.sendMessage("You are not a registered crew member on this craft!");
        }
        return crew;
    }
    public boolean checkPilot(Player player){
        if(player==null)return true;
        boolean pilot = isPilot(player);
        if(!pilot){
            player.sendMessage("You are not a registered pilot on this craft!");
        }
        return pilot;
    }
    public boolean checkCopilot(Player player){
        if(player==null)return true;
        if(!checkPilot(player))return false;
        if(pilot==player||copilots.contains(player)){
            return true;
        }else{
            player.sendMessage("You are not a co-pilot!");
        }
        return true;
    }
    public void addCopilot(Player player){
        if(!copilots.contains(player))copilots.add(player);
    }
    /**
     * Transfer ship to co-pilots after 5 minutes
     * @return <code>true</code> if ownership was transferred
     */
    public boolean repilot(){
        if(copilots.isEmpty())return false;
        notifyPilot(ChatColor.DARK_RED+"Transferring ship to co-pilot "+copilots.get(0).getDisplayName());
        pilot = copilots.remove(0);
        notifyPilot(ChatColor.BLUE+"Transferred craft to you!");
        return true;
    }
    /**
     * Checks to see if the craft is disabled, and if so, notifies the pilots.
     * @return <code>true</code> if the craft is disabled
     */
    private boolean checkDisabled(){
        boolean disabled = disabledAir;
        if(isUnderwater(true)){
            disabled = disabledDive;
        }else if(type.flight==null)return true;
        if(disabled){
            playSound(getPilots(), null, Sound.BLOCK_ANVIL_LAND, 0.4f);
            notifyPilots("Craft is disabled!");
        }
        return disabled;
    }
    /**
     * Checks to see if the craft has fuel, and if not, notifies the crew.
     * @return <code>true</code> if the craft has fuel
     */
    private boolean checkFuel(){
        refuel();
        if(fuel<=0){
            notifyCrew("Out of fuel!");
            return false;
        }
        return true;
    }
    public void setMode(int mode){
        if(mode>this.mode)this.mode = mode;
        if(mode==this.mode)modeTimer = 0;
        if(mode<this.mode){
            switch(this.mode){
                case CONSTRUCTION:
                    if(modeTimer>movecraft.constructionTimeout){
                        this.mode = mode;
                    }
                    break;
                case COMBAT:
                    if(modeTimer>movecraft.combatTimeout){
                        this.mode = mode;
                    }
                    break;
            }
        }
    }
    private void notifyBlockChange(Player player, String message){
        if(player==null)notifyCrew(message);
        else player.sendMessage(message);
    }
    private void updateDisabled(){
        disabledAir = disabledDive = false;
        if(type.flight!=null){
            for(ArrayList<Material> m : type.flight.requiredEngineRatios.keySet()){
                float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();
                float ratio = type.flight.requiredEngineRatios.get(m);
                if(actual<ratio)disabledAir = true;
            }
            for(ArrayList<Material> m : type.flight.requiredEngineBlocks.keySet()){
                int actual = movecraft.getBlocks(blocks, m);
                int blocks = type.flight.requiredEngineBlocks.get(m);
                if(actual<blocks)disabledAir = true;
            }
        }
        if(type.dive!=null){
            for(ArrayList<Material> m : type.dive.requiredEngineRatios.keySet()){
                float actual = movecraft.getBlocks(blocks, m)/(float)blocks.size();
                float ratio = type.dive.requiredEngineRatios.get(m);
                if(actual<ratio)disabledDive = true;
            }
            for(ArrayList<Material> m : type.dive.requiredEngineBlocks.keySet()){
                int actual = movecraft.getBlocks(blocks, m);
                int blocks = type.dive.requiredEngineBlocks.get(m);
                if(actual<blocks)disabledDive = true;
            }
        }
    }
    private String friendlyName(Material m){
        String str = m.toString().replace("WHITE_", "");
        return str.charAt(0)+str.substring(1).toLowerCase();
    }
    String undock(HashSet<Block> blocks){
        HashSet<Block> ship = new HashSet<>(this.blocks);
        ship.removeAll(blocks);
        if(blocks.size()<type.minSize){
            return "Craft too small!";
        }
        if(type.flight!=null){
            for(ArrayList<Material> materials : type.flight.requiredRatios.keySet()){
                float requiredRatio = type.flight.requiredRatios.get(materials);
                int amount = movecraft.getBlocks(blocks, materials);
                float actualRatio = amount/(float)blocks.size();
                if(actualRatio<requiredRatio){
                    return "Not enough blocks: "+materials.get(0).toString()+" or similar! ("+percent(actualRatio,2)+"<"+percent(requiredRatio,2)+")";
                }
            }
            for(ArrayList<Material> materials : type.flight.requiredBlocks.keySet()){
                int required = type.flight.requiredBlocks.get(materials);
                int actual = movecraft.getBlocks(blocks, materials);
                if(actual<required){
                    return "Not enough blocks: "+materials.get(0).toString()+" or similar! ("+actual+"<"+required+")";
                }
            }
        }
        if(type.dive!=null){
            for(ArrayList<Material> materials : type.dive.requiredRatios.keySet()){
                float requiredRatio = type.dive.requiredRatios.get(materials);
                int amount = movecraft.getBlocks(blocks, materials);
                float actualRatio = amount/(float)blocks.size();
                if(actualRatio<requiredRatio){
                    return "Not enough blocks: "+materials.get(0).toString()+" or similar! ("+percent(actualRatio,2)+"<"+percent(requiredRatio,2)+")";
                }
            }
            for(ArrayList<Material> materials : type.dive.requiredBlocks.keySet()){
                int required = type.dive.requiredBlocks.get(materials);
                int actual = movecraft.getBlocks(blocks, materials);
                if(actual<required){
                    return "Not enough blocks: "+materials.get(0).toString()+" or similar! ("+actual+"<"+required+")";
                }
            }
        }
        for(ArrayList<Material> material : type.bannedRatios.keySet()){
            float ratio = type.bannedRatios.get(material);
            int amount = movecraft.getBlocks(blocks, material);
            float actual = amount/(float)blocks.size();
            if(actual>ratio){
                return "Too many blocks: "+material.toString()+"! ("+percent(actual,2)+">"+percent(ratio,2)+")";
            }
        }
        this.blocks.removeAll(blocks);
        updateDisabled();
        calculateBoundingBox();
        updateSigns();
        return null;
    }
    private Collection<Block> getMovableBlocks(){
        Set<Block> movable = new HashSet<>(blocks);
        movecraft.debug(pilot, "Getting movable blocks");
        //TODO moving other ships too
//        for(Block block : blocks){
//            for(int x = -1; x<=1; x++){
//                for(int y = -1; y<=1; y++){
//                    for(int z = -1; z<=1; z++){
//                        if(Math.abs(x)+Math.abs(y)+Math.abs(z)!=1)continue;//only 6 cardinal directions
//                        Block newblock = block.getRelative(x,y,z);
//                        Craft other = movecraft.getCraft(newblock);
//                        if(other!=null&&other!=this&&type.children.contains(other.type)){
//                            movable.addAll(other.getMovableBlocks());
//                        }
//                    }
//                }
//            }
//        }
        movecraft.debug(pilot, "Got Movable blocks");
        return movable;
    }
    public void newRound(Entity entity){
//        if(entity.getType()==EntityType.PRIMED_TNT){
//            tnt.put(entity, 0d);
//        }
//        if(entity.getType()==EntityType.SMALL_FIREBALL){
//            movecraft.debug(pilot, "Directing AA...");
//            Block b = getAATarget(entity.getLocation(), entity.getVelocity());
//            Vector targetVector = getAADirection(entity.getVelocity());
//            if(b!=null||targetVector!=null){
//                Vector aaVel = entity.getVelocity();
//                double speed = aaVel.length();
//                aaVel = aaVel.normalize();
//                if(b!=null)targetVector = b.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
//                if(targetVector.getX() - aaVel.getX() > movecraft.fireballAngleLimit){
//                    aaVel.setX(aaVel.getX() + movecraft.fireballAngleLimit);
//                }else if(targetVector.getX() - aaVel.getX() < -movecraft.fireballAngleLimit){
//                    aaVel.setX(aaVel.getX() - movecraft.fireballAngleLimit);
//                }else{
//                    aaVel.setX(targetVector.getX());
//                }
//                if(targetVector.getY() - aaVel.getY() > movecraft.fireballAngleLimit){
//                    aaVel.setY(aaVel.getY() + movecraft.fireballAngleLimit);
//                }else if(targetVector.getY() - aaVel.getY() < -movecraft.fireballAngleLimit){
//                    aaVel.setY(aaVel.getY() - movecraft.fireballAngleLimit);
//                }else{
//                    aaVel.setY(targetVector.getY());
//                }
//                if(targetVector.getZ() - aaVel.getZ() > movecraft.fireballAngleLimit){
//                    aaVel.setZ(aaVel.getZ() + movecraft.fireballAngleLimit);
//                }else if(targetVector.getZ() - aaVel.getZ() < -movecraft.fireballAngleLimit){
//                    aaVel.setZ(aaVel.getZ() - movecraft.fireballAngleLimit);
//                }else{
//                    aaVel.setZ(targetVector.getZ());
//                }
//                aaVel = aaVel.multiply(speed);
//                entity.setVelocity(aaVel);
//                ((SmallFireball)entity).setDirection(aaVel);
//            }
//        }
    }
    void addAADirector(Player player){
        if(aaDirectors.contains(player)){
            aaDirectors.remove(player);
            player.sendMessage("You are no longer directing AA on this craft");
            return;
        }
        movecraft.clearDirector(player);
        aaDirectors.add(player);
        player.sendMessage("You are now directing AA on this craft");
    }
    void addCannonDirector(Player player){
        if(cannonDirectors.contains(player)){
            cannonDirectors.remove(player);
            player.sendMessage("You are no longer directing the cannons on this craft");
            return;
        }
        movecraft.clearDirector(player);
        cannonDirectors.add(player);
        player.sendMessage("You are now directing the cannons on this craft");
    }
    private MovementDetails getMovementDetails(){
        if(isUnderwater(true))return type.dive;
        return type.flight;
    }
    private int getWaterLevel(){
        Set<Block> outsideBlocks = new HashSet<>();
        Set<Block> outerHull = new HashSet<>();
        Set<Block> innerShip = new HashSet<>();
        scanHull(outsideBlocks, outerHull, innerShip);
        int max = 0;
        for(Block b : outsideBlocks){
            if(b.getType()==Material.WATER||((b.getBlockData() instanceof Waterlogged)&&((Waterlogged)b.getBlockData()).isWaterlogged()))max = Math.max(b.getY(), max);
        }
        return max;
    }
    private boolean isUnderwater(boolean truly){
        if(type.dive==null)return false;
        Set<Block> outsideBlocks = new HashSet<>();
        Set<Block> outerHull = new HashSet<>();
        Set<Block> innerShip = new HashSet<>();
        scanHull(outsideBlocks, outerHull, innerShip);
        boolean foundOne = false;
        for(Block b : outsideBlocks){
            if(truly){
                if(!getBoundingBox().contains(b.getX()+.5,b.getY()+.5,b.getZ()+.5))continue;
            }
            foundOne = true;
            if(b.getType()==Material.WATER||((b.getBlockData() instanceof Waterlogged)&&((Waterlogged)b.getBlockData()).isWaterlogged()))return true;
        }
        if(truly&&!foundOne)return isUnderwater(false);
        return false;
    }
    private boolean isWaterConnected(Block a, Block b){
        BlockFace face = a.getFace(b);
        int A = isWaterConnected(a, face);
        int B = isWaterConnected(b, face.getOppositeFace());
        if(A+B>=3)return true;
        if(A==0||B==0)return false;
        boolean anw = false;
        boolean ane = false;
        boolean asw = false;
        boolean ase = false;
        boolean bnw = false;
        boolean bne = false;
        boolean bsw = false;
        boolean bse = false;
        if(a.getBlockData() instanceof Slab){
            Slab s = (Slab) a.getBlockData();
            anw = ane = s.getType()==Slab.Type.TOP;
            asw = ase = s.getType()==Slab.Type.BOTTOM;
        }
        if(b.getBlockData() instanceof Slab){
            Slab s = (Slab) b.getBlockData();
            bnw = bne = s.getType()==Slab.Type.TOP;
            bsw = bse = s.getType()==Slab.Type.BOTTOM;
        }
        if(a.getBlockData() instanceof Stairs){
            Stairs s = (Stairs)a.getBlockData();
            if(face==BlockFace.UP||face==BlockFace.DOWN){
                switch(s.getFacing()){
                    case NORTH:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                anw = ane = asw = true;
                                break;
                            case INNER_RIGHT:
                                anw = ane = ase = true;
                                break;
                            case OUTER_LEFT:
                                anw = true;
                                break;
                            case OUTER_RIGHT:
                                ane = true;
                                break;
                            case STRAIGHT:
                                anw = ane = true;
                                break;
                        }
                        break;
                    case SOUTH:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                asw = ase = ane = true;
                                break;
                            case INNER_RIGHT:
                                anw = asw = ase = true;
                                break;
                            case OUTER_LEFT:
                                ase = true;
                                break;
                            case OUTER_RIGHT:
                                asw = true;
                                break;
                            case STRAIGHT:
                                asw = ase = true;
                                break;
                        }
                        break;
                    case EAST:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                anw = ane = ase = true;
                                break;
                            case INNER_RIGHT:
                                asw = ase = ane = true;
                                break;
                            case OUTER_LEFT:
                                ane = true;
                                break;
                            case OUTER_RIGHT:
                                ase = true;
                                break;
                            case STRAIGHT:
                                ane = ase = true;
                                break;
                        }
                        break;
                    case WEST:
                        switch(s.getShape()){
                            case INNER_LEFT:
                                anw = asw = ase = true;
                                break;
                            case INNER_RIGHT:
                                ane = anw = asw = true;
                                break;
                            case OUTER_LEFT:
                                asw = true;
                                break;
                            case OUTER_RIGHT:
                                anw = true;
                                break;
                            case STRAIGHT:
                                anw = asw = true;
                                break;
                        }
                        break;
                }
            }else{
                if(s.getHalf()==Bisected.Half.TOP){
                    anw = ane = true;
                }
                if(s.getHalf()==Bisected.Half.TOP){
                    asw = ase = true;
                }
                int side = 0;//0 is same, then clockwise
                switch(face){
                    case NORTH:
                        side = 0;
                        break;
                    case EAST:
                        side = 1;
                        break;
                    case SOUTH:
                        side = 2;
                        break;
                    case WEST:
                        side = 3;
                        break;
                }
                switch(s.getFacing()){
                    case NORTH:
                        break;
                    case EAST:
                        side--;
                        break;
                    case WEST:
                        side--;
                        break;
                    case SOUTH:
                        side--;
                        break;
                }
                if(side<0)side+=4;
                switch(s.getShape()){
                    case INNER_LEFT:
                        if(side==1)anw=true;
                        if(side==2)ane=true;
                        break;
                    case INNER_RIGHT:
                        if(side==2)anw=true;
                        if(side==3)ane=true;
                        break;
                    case OUTER_LEFT:
                        if(side==0)anw=true;
                        if(side==3)ane=true;
                        break;
                    case OUTER_RIGHT:
                        if(side==1)anw=true;
                        if(side==0)ane=true;
                        break;
                    case STRAIGHT:
                        if(side==1)anw=true;
                        if(side==3)ane=true;    
                        break;
                }
            }
        }
        if((anw||bnw)&&(ane||bne)&&(asw||bsw)&&(ase||bse))return false;
        return true;
    }
    private int isWaterConnected(Block b, BlockFace face){
        switch(b.getType()){
            case BUBBLE_COLUMN:
            case CHEST:
            case TRAPPED_CHEST:
            case CONDUIT:
            case TUBE_CORAL:
            case TUBE_CORAL_FAN:
            case TUBE_CORAL_WALL_FAN:
            case BRAIN_CORAL:
            case BRAIN_CORAL_FAN:
            case BRAIN_CORAL_WALL_FAN:
            case BUBBLE_CORAL:
            case BUBBLE_CORAL_FAN:
            case BUBBLE_CORAL_WALL_FAN:
            case FIRE_CORAL:
            case FIRE_CORAL_FAN:
            case FIRE_CORAL_WALL_FAN:
            case HORN_CORAL:
            case HORN_CORAL_FAN:
            case HORN_CORAL_WALL_FAN:
            case DEAD_TUBE_CORAL:
            case DEAD_TUBE_CORAL_FAN:
            case DEAD_TUBE_CORAL_WALL_FAN:
            case DEAD_BRAIN_CORAL:
            case DEAD_BRAIN_CORAL_FAN:
            case DEAD_BRAIN_CORAL_WALL_FAN:
            case DEAD_BUBBLE_CORAL:
            case DEAD_BUBBLE_CORAL_FAN:
            case DEAD_BUBBLE_CORAL_WALL_FAN:
            case DEAD_FIRE_CORAL:
            case DEAD_FIRE_CORAL_FAN:
            case DEAD_FIRE_CORAL_WALL_FAN:
            case DEAD_HORN_CORAL:
            case DEAD_HORN_CORAL_FAN:
            case DEAD_HORN_CORAL_WALL_FAN:
            case ENDER_CHEST:
            case OAK_FENCE:
            case BIRCH_FENCE:
            case SPRUCE_FENCE:
            case JUNGLE_FENCE:
            case ACACIA_FENCE:
            case DARK_OAK_FENCE:
            case NETHER_BRICK_FENCE:
            case IRON_BARS:
            case KELP:
            case LADDER:
            case SEAGRASS:
            case OAK_SIGN:
            case OAK_WALL_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_WALL_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_SIGN:
            case ACACIA_SIGN:
            case ACACIA_WALL_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_SIGN:
            case TALL_SEAGRASS:
            case COBBLESTONE_WALL:
            case MOSSY_COBBLESTONE_WALL:
            case BRICK_WALL:
            case PRISMARINE_WALL:
            case RED_SANDSTONE_WALL:
            case MOSSY_STONE_BRICK_WALL:
            case GRANITE_WALL:
            case STONE_BRICK_WALL:
            case NETHER_BRICK_WALL:
            case ANDESITE_WALL:
            case RED_NETHER_BRICK_WALL:
            case SANDSTONE_WALL:
            case END_STONE_BRICK_WALL:
            case DIORITE_WALL:
                return 2;
            case CAMPFIRE://insulated on the bottom only
                if(face==BlockFace.DOWN)return 0;
                return 2;
            case GLASS_PANE://depends on blockstate..???
                MultipleFacing pane = (MultipleFacing)b.getBlockData();
                switch(face){
                    case EAST:
                    case WEST:
                        if(pane.hasFace(BlockFace.NORTH)||pane.hasFace(BlockFace.SOUTH))return 2;
                        return 0;
                    case NORTH:
                    case SOUTH:
                        if(pane.hasFace(BlockFace.EAST)||pane.hasFace(BlockFace.WEST))return 2;
                        return 0;
                }
                return 0;
            case OAK_SLAB:
            case SPRUCE_SLAB:
            case BIRCH_SLAB:
            case JUNGLE_SLAB:
            case ACACIA_SLAB:
            case DARK_OAK_SLAB:
            case STONE_SLAB:
            case SMOOTH_STONE_SLAB:
            case SANDSTONE_SLAB:
            case CUT_SANDSTONE_SLAB:
            case PETRIFIED_OAK_SLAB:
            case COBBLESTONE_SLAB:
            case BRICK_SLAB:
            case STONE_BRICK_SLAB:
            case NETHER_BRICK_SLAB:
            case QUARTZ_SLAB:
            case RED_SANDSTONE_SLAB:
            case CUT_RED_SANDSTONE_SLAB:
            case PURPUR_SLAB:
            case PRISMARINE_SLAB:
            case PRISMARINE_BRICK_SLAB:
            case DARK_PRISMARINE_SLAB:
            case POLISHED_GRANITE_SLAB:
            case SMOOTH_RED_SANDSTONE_SLAB:
            case MOSSY_STONE_BRICK_SLAB:
            case POLISHED_DIORITE_SLAB:
            case MOSSY_COBBLESTONE_SLAB:
            case END_STONE_BRICK_SLAB:
            case SMOOTH_SANDSTONE_SLAB:
            case SMOOTH_QUARTZ_SLAB:
            case GRANITE_SLAB:
            case ANDESITE_SLAB:
            case RED_NETHER_BRICK_SLAB:
            case POLISHED_ANDESITE_SLAB:
            case DIORITE_SLAB:
                Slab slab = (Slab)b.getBlockData();
                if(slab.getType()==Slab.Type.DOUBLE)return 0;
                if(face==BlockFace.UP){
                    if(slab.getType()==Slab.Type.TOP)return 0;
                    if(slab.getType()==Slab.Type.BOTTOM)return 2;
                }
                if(face==BlockFace.DOWN){
                    if(slab.getType()==Slab.Type.BOTTOM)return 0;
                    if(slab.getType()==Slab.Type.TOP)return 2;
                }
                return 1;
            case PURPUR_STAIRS:
            case OAK_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case STONE_BRICK_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_STAIRS:
            case BIRCH_STAIRS:
            case JUNGLE_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case PRISMARINE_STAIRS:
            case PRISMARINE_BRICK_STAIRS:
            case DARK_PRISMARINE_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case POLISHED_GRANITE_STAIRS:
            case SMOOTH_RED_SANDSTONE_STAIRS:
            case MOSSY_STONE_BRICK_STAIRS:
            case POLISHED_DIORITE_STAIRS:
            case MOSSY_COBBLESTONE_STAIRS:
            case END_STONE_BRICK_STAIRS:
            case STONE_STAIRS:
            case SMOOTH_SANDSTONE_STAIRS:
            case SMOOTH_QUARTZ_STAIRS:
            case GRANITE_STAIRS:
            case ANDESITE_STAIRS:
            case RED_NETHER_BRICK_STAIRS:
            case POLISHED_ANDESITE_STAIRS:
            case DIORITE_STAIRS:
                Stairs stairs = (Stairs)b.getBlockData();
                if(face==BlockFace.UP){
                    return stairs.getHalf()==Bisected.Half.TOP?0:1;
                }
                if(face==BlockFace.DOWN){
                    return stairs.getHalf()==Bisected.Half.BOTTOM?0:1;
                }
                if(stairs.getShape()==Stairs.Shape.OUTER_LEFT||stairs.getShape()==Stairs.Shape.OUTER_RIGHT)return 1;
                if(stairs.getShape()==Stairs.Shape.STRAIGHT)return stairs.getFacing()==face?0:1;
                if(stairs.getShape()==Stairs.Shape.INNER_LEFT){
                    switch(stairs.getFacing()){
                        case NORTH:
                            return face==BlockFace.NORTH||face==BlockFace.WEST?0:1;
                        case EAST:
                            return face==BlockFace.EAST||face==BlockFace.NORTH?0:1;
                        case SOUTH:
                            return face==BlockFace.SOUTH||face==BlockFace.EAST?0:1;
                        case WEST:
                            return face==BlockFace.WEST||face==BlockFace.SOUTH?0:1;
                    }
                }
                if(stairs.getShape()==Stairs.Shape.INNER_RIGHT){
                    switch(stairs.getFacing()){
                        case NORTH:
                            return face==BlockFace.NORTH||face==BlockFace.EAST?0:1;
                        case EAST:
                            return face==BlockFace.EAST||face==BlockFace.SOUTH?0:1;
                        case SOUTH:
                            return face==BlockFace.SOUTH||face==BlockFace.WEST?0:1;
                        case WEST:
                            return face==BlockFace.WEST||face==BlockFace.NORTH?0:1;
                    }
                }
                return 1;
            case OAK_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case IRON_TRAPDOOR:
                TrapDoor door = (TrapDoor)b.getBlockData();
                if(face==BlockFace.UP&&!door.isOpen()&&door.getHalf()==Bisected.Half.TOP)return 0;
                if(face==BlockFace.DOWN&&!door.isOpen()&&door.getHalf()==Bisected.Half.BOTTOM)return 0;
                if(door.isOpen()&&face==door.getFacing().getOppositeFace())return 0;
                return 2;
        }
        return 0;
    }
    public int getYSize(){
        int y1,y2;
        y1 = Integer.MAX_VALUE;
        y2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            y1 = Math.min(y1, block.getY());
            y2 = Math.max(y2, block.getY());
        }
        return y2-y1+1;
    }
    public int getXSize(){
        int x1,x2;
        x1 = Integer.MAX_VALUE;
        x2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            x1 = Math.min(x1, block.getX());
            x2 = Math.max(x2, block.getX());
        }
        return x2-x1+1;
    }
    public int getZSize(){
        int z1,z2;
        z1 = Integer.MAX_VALUE;
        z2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            z1 = Math.min(z1, block.getZ());
            z2 = Math.max(z2, block.getZ());
        }
        return z2-z1+1;
    }
    public double distance(Location l){
        return getOrigin().distance(l);
    }
    Block getAATarget(Location location, Vector v) {
        if(aaDirectors.isEmpty())return null;
        Block smallest = null;
        double a = 0;
        for(Player p : aaDirectors){
            Block b = getAATarget(p);
            if(b==null)continue;
            double angle = v.normalize().angle(b.getLocation().toVector().subtract(location.toVector().normalize()));
            if(smallest==null||angle<a){
                smallest = b;
                a = angle;
            }
        }
        return smallest;
    }
    Block getAATarget(Player p){
        if(aaTargets.containsKey(p))return aaTargets.get(p);
        return getTarget(p);
    }
    Vector getAADirection(Vector vect){
        if(aaDirectors.isEmpty())return null;
        Vector smallest = null;
        double a = 0;
        for(Player p : aaDirectors){
            Vector v = getDirection(p);
            if(v==null)continue;
            double angle = v.normalize().angle(vect.normalize());
            if(smallest==null||angle<a){
                smallest = v;
                a = angle;
            }
        }
        return smallest;
    }
    Block getCannonTarget(Location location, Vector v) {
        if(cannonDirectors.isEmpty())return null;
        Block smallest = null;
        double a = 0;
        for(Player p : cannonDirectors){
            Block b = getCannonTarget(p);
            if(b==null)continue;
            double angle = v.normalize().angle(b.getLocation().toVector().subtract(location.toVector().normalize()));
            if(smallest==null||angle<a){
                smallest = b;
                a = angle;
            }
        }
        return smallest;
    }
    Block getCannonTarget(Player p){
        if(cannonTargets.containsKey(p))return cannonTargets.get(p);
        return getTarget(p);
    }
    Vector getCannonDirection(Vector vect){
        if(cannonDirectors.isEmpty())return null;
        Vector smallest = null;
        double a = 0;
        for(Player p : cannonDirectors){
            Vector v = getDirection(p);
            if(v==null)continue;
            double angle = v.normalize().angle(vect.normalize());
            if(smallest==null||angle<a){
                smallest = v;
                a = angle;
            }
        }
        return smallest;
    }
    Vector getDirection(Player p){
        if(p.getInventory().getItemInMainHand().getType()==Material.STICK||p.getInventory().getItemInOffHand().getType()==Material.STICK){
            return p.getLocation().getDirection();
        }
        return null;
    }
    public Block getTarget(Player p){
        if(p.getInventory().getItemInMainHand().getType()==Material.STICK||p.getInventory().getItemInOffHand().getType()==Material.STICK){
            RayTraceResult result = p.rayTraceBlocks(movecraft.directorTargetRange, FluidCollisionMode.NEVER);
            if(result!=null&&result.getHitBlock()!=null&&!blocks.contains(result.getHitBlock())){
                return result.getHitBlock();
            }
            Block b = p.getTargetBlock(Movecraft.transparent, movecraft.directorTargetRange);
            if(!blocks.contains(b))return b;
        }
        return null;
    }
    public void aaTarget(Player player, Block target){
        if(target==null)aaTargets.remove(player);
        else aaTargets.put(player, target);
    }
    public void cannonTarget(Player player, Block target){
        if(target==null)cannonTargets.remove(player);
        else cannonTargets.put(player, target);
    }
    private void finishCompilingBlockChanges(boolean underwaterMove, int waterLevel, ArrayList<BlockChange> changes, Set<Block> blox){
        int A = 0;
        int B = 0;
        int C = 0;
        for(Block block : blox){
            if(underwaterMove){
                if(block.getY()>waterLevel){
                    changes.add(new BlockChange(block, Material.AIR, null, null));
                    A++;
                }else{
                    changes.add(new BlockChange(block, Material.WATER, null, null));
                    B++;
                }
            }else{
                changes.add(new BlockChange(block, Material.AIR, null, null));
                C++;
            }
        }
        movecraft.debug(pilot, "Compiled BlockChanges "+A+" "+B+" "+C);
    }
    boolean containsBlock(Block block){
        return blocks.contains(block);
    }//TODO optimize - this is super laggy on large ships
    private static class BlockChange implements Comparable<BlockChange>{
        private final Material type;
        private final BlockData data;
        private final BlockState state;
        private final Block block;
        public BlockChange(Block block, Material type, BlockData data, BlockState state, int rotation){
            this.block = block;
            this.type = type;
            this.data = data;
            this.state = state;
            rotateBlock(data, rotation);
        }
        public BlockChange(Block block, Material type, BlockData data, BlockState state){
            this(block, type, data, state, 0);
        }
        public BlockChange(Block block, Block to){
            this(block, to.getType(), to.getBlockData(), to.getState(), 0);
        }
        public BlockChange(Block block, Block to, int rotation){
            this(block, to.getType(), to.getBlockData(), to.getState(), rotation);
        }
        public void change(){
            BlockState st = block.getState();
            if(st instanceof Container){
                ((Container)st).getSnapshotInventory().clear();
                st.update(false, false);
            }
            if(block.getType()!=type){
                block.setType(type, false);
            }
            if(isInert(type))return;
            if(data!=null)block.setBlockData(data);
            BlockState newState = block.getState();
            if(state!=null){
                if(state instanceof org.bukkit.block.Beehive){
                    Beehive theNew = (Beehive)newState;
                    Beehive theOld = (Beehive)state;
//                    theNew.
                }
                if(state instanceof Container){
                    Container newContainer = (Container)newState;
                    Container oldContainer = (Container)state;
                    newContainer.getSnapshotInventory().setContents(oldContainer.getSnapshotInventory().getContents());
                }
                if(state instanceof Structure){
                    Structure newStructure = (Structure)newState;
                    Structure oldStructure = (Structure)state;
                    newStructure.setAuthor(oldStructure.getAuthor());
                    newStructure.setBoundingBoxVisible(oldStructure.isBoundingBoxVisible());
                    newStructure.setIgnoreEntities(oldStructure.isIgnoreEntities());
                    newStructure.setIntegrity(oldStructure.getIntegrity());
                    newStructure.setMetadata(oldStructure.getMetadata());
                    newStructure.setMirror(oldStructure.getMirror());
                    newStructure.setRelativePosition(oldStructure.getRelativePosition());
                    newStructure.setRotation(oldStructure.getRotation());
                    newStructure.setSeed(oldStructure.getSeed());
                    newStructure.setShowAir(oldStructure.isShowAir());
                    newStructure.setStructureName(oldStructure.getStructureName());
                    newStructure.setStructureSize(oldStructure.getStructureSize());
                    newStructure.setUsageMode(oldStructure.getUsageMode());
                }
                if(state instanceof Skull){
                    Skull newSkull = (Skull)newState;
                    Skull oldSkull = (Skull)state;
                    if(oldSkull.hasOwner())newSkull.setOwningPlayer(oldSkull.getOwningPlayer());
                }
                if(state instanceof Sign){
                    Sign newSign = (Sign)newState;
                    Sign oldSign = (Sign)state;
                    for(int i = 0; i<oldSign.getLines().length; i++){
                        newSign.setLine(i, oldSign.getLine(i));
                    }
                    newSign.setEditable(oldSign.isEditable());
                }
                if(state instanceof Lockable){
                    Lockable newLockable = (Lockable)newState;
                    Lockable oldLockable = (Lockable)state;
                    newLockable.setLock(oldLockable.getLock());
                }
                if(state instanceof Jukebox){
                    Jukebox newJukebox = (Jukebox)newState;
                    Jukebox oldJukebox = (Jukebox)state;
                    newJukebox.setPlaying(oldJukebox.getPlaying());
                    newJukebox.setRecord(oldJukebox.getRecord());
                }
                if(state instanceof Lectern){
                    Lectern newLectern = (Lectern)newState;
                    Lectern oldLectern = (Lectern)state;
                    newLectern.setPage(oldLectern.getPage());
                }
                if(state instanceof EndGateway){
                    EndGateway newEndGateway = (EndGateway)newState;
                    EndGateway oldEndGateway = (EndGateway)state;
                    newEndGateway.setAge(oldEndGateway.getAge());;
                    newEndGateway.setExactTeleport(oldEndGateway.isExactTeleport());
                    newEndGateway.setExitLocation(oldEndGateway.getExitLocation());
                }
                if(state instanceof Furnace){
                    Furnace newFurnace = (Furnace)newState;
                    Furnace oldFurnace = (Furnace)state;
                    newFurnace.setBurnTime(oldFurnace.getBurnTime());
                    newFurnace.setCookTime(oldFurnace.getCookTime());
                    newFurnace.setCookTimeTotal(oldFurnace.getCookTimeTotal());
                }
                if(state instanceof CreatureSpawner){
                    CreatureSpawner newCreatureSpawner = (CreatureSpawner)newState;
                    CreatureSpawner oldCreatureSpawner = (CreatureSpawner)state;
                    newCreatureSpawner.setDelay(oldCreatureSpawner.getDelay());
                    newCreatureSpawner.setMaxNearbyEntities(oldCreatureSpawner.getMaxNearbyEntities());
                    newCreatureSpawner.setMaxSpawnDelay(oldCreatureSpawner.getMaxSpawnDelay());
                    newCreatureSpawner.setMinSpawnDelay(oldCreatureSpawner.getMinSpawnDelay());
                    newCreatureSpawner.setRequiredPlayerRange(oldCreatureSpawner.getRequiredPlayerRange());
                    newCreatureSpawner.setSpawnCount(oldCreatureSpawner.getSpawnCount());
                    newCreatureSpawner.setSpawnRange(oldCreatureSpawner.getSpawnRange());
                    newCreatureSpawner.setSpawnedType(oldCreatureSpawner.getSpawnedType());
                }
                if(state instanceof Banner){
                    Banner newBanner = (Banner)newState;
                    Banner oldBanner = (Banner)state;
                    newBanner.setBaseColor(oldBanner.getBaseColor());
                    newBanner.setPatterns(oldBanner.getPatterns());
                }
                if(state instanceof Beacon){
                    Beacon newBeacon = (Beacon)newState;
                    Beacon oldBeacon = (Beacon)state;
                    newBeacon.setPrimaryEffect(oldBeacon.getPrimaryEffect().getType());
                    newBeacon.setSecondaryEffect(oldBeacon.getSecondaryEffect().getType());
                }
                if(state instanceof BrewingStand){
                    BrewingStand newBrewingStand = (BrewingStand)newState;
                    BrewingStand oldBrewingStand = (BrewingStand)state;
                    newBrewingStand.setBrewingTime(oldBrewingStand.getBrewingTime());
                    newBrewingStand.setFuelLevel(oldBrewingStand.getFuelLevel());
                }
                if(state instanceof Campfire){
                    Campfire newCampfire = (Campfire)newState;
                    Campfire oldCampfire = (Campfire)state;
                    for(int i = 0; i<oldCampfire.getSize(); i++){
                        newCampfire.setItem(i, oldCampfire.getItem(i));
                        newCampfire.setCookTime(i, oldCampfire.getCookTime(i));
                        newCampfire.setCookTimeTotal(i, oldCampfire.getCookTimeTotal(i));
                    }
                }
                if(state instanceof CommandBlock){
                    CommandBlock newCommandBlock = (CommandBlock)newState;
                    CommandBlock oldCommandBlock = (CommandBlock)state;
                    newCommandBlock.setCommand(oldCommandBlock.getCommand());
                    newCommandBlock.setName(oldCommandBlock.getName());
                }
                try{
                    newState.update(false, false);
                }catch(NullPointerException ex){
                    Movecraft.instance.getServer().broadcastMessage("Failed to copy block state: "+block.getType().name());
                }
            }
        }
        @Override
        public int compareTo(BlockChange o){
            boolean a = block.getType().isSolid();
            boolean b = o.block.getType().isSolid();
            if(a&&b)return 0;
            if(a)return -1;
            if(b)return 1;
            return 0;
        }
    }
    private static class BlockMovement{
        private final Location from;
        private final Location to;
        private final int rotation;
        public BlockMovement(Location from, Location to){
            this(from, to, 0);
        }
        public BlockMovement(Block from, Block to){
            this(from.getLocation(), to.getLocation());
        }
        public BlockMovement(Block from, Location to){
            this(from.getLocation(), to);
        }
        public BlockMovement(Location from, Location to, int rotation){
            this.from = from.clone();
            this.to = to.clone();
            this.rotation = rotation;
        }
        public BlockMovement(Block from, Block to, int rotation){
            this(from.getLocation(), to.getLocation(), rotation);
        }
        public BlockMovement(Block from, Location to, int rotation){
            this(from.getLocation(), to, rotation);
        }
        public void move1(Craft parent){
            Block block = parent.world.getBlockAt(from);
            if(parent.blocks.contains(block))return;
            Craft craft = parent.movecraft.getCraft(from);
            if(parent.type.children.contains(craft.type)){
                craft.blocks.remove(block);
            }
        }
        public void move2(Craft parent){
            Block block = parent.world.getBlockAt(from);
            Block t = parent.world.getBlockAt(to);
            if(parent.blocks.contains(block))return;
            Craft craft = parent.movecraft.getCraft(from);
            if(parent.type.children.contains(craft.type)){
                craft.blocks.add(t);
            }
        }
    }
    public static String percent(double d, int decimals){
        return Math.round(d*100*Math.pow(10, decimals))/Math.pow(10, decimals)+"%";
    }
    /**
     * Scans the ship hull for airtightness. Feed in empty lists to get filled
     * @param outsideBlocks All blocks that are outside of the ship within one block of its bounding box. This includes ship hull blocks that are exterior-waterloggable
     * @param outerHull All blocks that are part of the outer hull of the ship
     * @param innerShip All that are part of the ship's interior (including air blocks) and/or are not on the outside of the ship
     */
    private void scanHull(Set<Block> outsideBlocks, Set<Block> outerHull, Set<Block> innerShip){
        signs = null;
        Set<Block> allBlocks = new HashSet<>();
        Set<Block> nextLayer = new HashSet<>();
        int x1,y1,z1,x2,y2,z2;
        x1 = y1 = z1 = Integer.MAX_VALUE;
        x2 = y2 = z2 = Integer.MIN_VALUE;
        for(Block block : blocks){
            x1 = Math.min(x1, block.getX());
            y1 = Math.min(y1, block.getY());
            z1 = Math.min(z1, block.getZ());
            x2 = Math.max(x2, block.getX());
            y2 = Math.max(y2, block.getY());
            z2 = Math.max(z2, block.getZ());
        }
        int bbox;
        for(int x = x1-1; x<=x2+1; x++){
            for(int y = y1-1; y<=y2+1; y++){
                for(int z = z1-1; z<=z2+1; z++){
                    allBlocks.add(world.getBlockAt(x,y,z));
                    if(x==x1-1||y==y1-1||z==z1-1||x==x2+1||y==y2+1||z==z2+1){
                        nextLayer.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        while(!nextLayer.isEmpty()){
            Set<Block> thisLayer = new HashSet<>(nextLayer);
            outsideBlocks.addAll(nextLayer);
            nextLayer.clear();
            for(Block b : thisLayer){
                for(int x = -1; x<=1; x++){
                    for(int y = -1; y<=1; y++){
                        for(int z = -1; z<=1; z++){
                            if(Math.abs(x)+Math.abs(y)+Math.abs(z)>1)continue;
                            Block newBlock = b.getRelative(x,y,z);
                            if(thisLayer.contains(newBlock)||outsideBlocks.contains(newBlock)||(newBlock.getX()<x1||newBlock.getY()<y1||newBlock.getZ()<z1||newBlock.getX()>x2||newBlock.getY()>y2||newBlock.getZ()>z2)||nextLayer.contains(newBlock)){
                                continue;
                            }
                            if(blocks.contains(b)||blocks.contains(newBlock)){
                                if(isWaterConnected(b,newBlock)){
                                    nextLayer.add(newBlock);
                                    if(blocks.contains(b))outerHull.add(newBlock);
                                }
                                if(!blocks.contains(b))outerHull.add(newBlock);
                            }else{
                                nextLayer.add(newBlock);
                            }
                        }
                    }
                }
            }
        }
        innerShip.addAll(allBlocks);
        innerShip.removeAll(outsideBlocks);
        innerShip.removeAll(outerHull);
    }
    private Set<Sign> signs = null;
    public Set<Sign> getSigns(){
        if(signs==null){
            Set<Sign> signs = new HashSet<>();
            for(Block b : blocks){
                if(Movecraft.Tags.isSign(b.getType()))signs.add((Sign)b.getState());
            }
            this.signs = signs;
        }
        return signs;
    }
}