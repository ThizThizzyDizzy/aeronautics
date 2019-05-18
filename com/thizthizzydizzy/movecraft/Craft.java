package com.thizthizzydizzy.movecraft;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.v1_14_R1.ChatMessageType;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
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
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
public class Craft{
    private final Movecraft plugin;
    public final CraftType type;
    public final ArrayList<Block> blocks;
    public Player pilot;
    public Direction cruise = Direction.NONE;
    private int timer = 0;
    private final World world;
    private BukkitTask ticker;
    private BoundingBox bbox;
    private boolean sinking = false;
    public boolean moving;
    public boolean disabled = false;
    public int fuel = 0;
    private static final Set<Material> blocksThatPop = new HashSet<>();
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
    }
    public Craft(Movecraft plugin, CraftType type, ArrayList<Block> blocks, Player pilot){
        this.plugin = plugin;
        this.type = type;
        this.blocks = blocks;
        this.pilot = pilot;
        ticker = new BukkitRunnable() {
            @Override
            public void run(){
                tick();
            }
        }.runTaskTimer(plugin, 1, 1);
        world = pilot.getWorld();
        updateHull(0);
    }
    public void tick(){
        if(sinking){
            timer++;
            if(timer>=type.moveTime/2){
                timer-=type.moveTime/2;
                sink();
            }
            return;
        }
        if(cruise==Direction.NONE){
            timer = 0;
        }else{
            timer++;
            if(timer>=type.moveTime){
                timer-=type.moveTime;
                move();
            }
        }
    }
    public void cruise(Sign sign, Direction direction){
        if(cruise==Direction.NONE||cruise==Direction.UP||cruise==Direction.DOWN){
            cruise = direction;
        }else{
            cruise = Direction.NONE;
        }
        sign.setLine(0, "Cruise: "+(cruise==Direction.NONE?"OFF":"ON"));
        sign.update();
    }
    public void ascend(Sign sign){
        if(cruise==Direction.UP){
            cruise = Direction.NONE;
        }else{
            cruise = Direction.UP;
        }
        sign.setLine(0, "Ascend: "+(cruise==Direction.UP?"ON":"OFF"));
        sign.update();
    }
    public void descend(Sign sign){
        if(cruise==Direction.DOWN){
            cruise = Direction.NONE;
        }else{
            cruise = Direction.DOWN;
        }
        sign.setLine(0, "Descend: "+(cruise==Direction.DOWN?"ON":"OFF"));
        sign.update();
    }
    public void release(){
        ticker.cancel();
        notifyPilot("Craft released.");
        plugin.crafts.remove(this);
    }
    private void rotate(int amount){
        if(disabled){
            notifyPilot("Craft is disabled!");
            return;
        }
        refuel();
        if(fuel<=0){
            notifyPilot("Out of fuel!");
            return;
        }
        rotateAbout(getOrigin(), amount);
    }
    public void rotateAbout(Location origin, int amount){//rotate about the block
        ArrayList<BlockMovement> movements = new ArrayList<>();
        for(Block block : blocks){
            movements.add(new BlockMovement(block.getLocation(), rotate(block.getLocation(), origin, amount), amount));
        }
        Iterable<Entity> entities = move(movements, false);
        if(entities==null)return;
        for(Entity e : entities){
            Location l = e.getLocation();
            l.setYaw(l.getYaw()+90*amount);
            e.teleport(l, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }
    private void move(){
        if(disabled){
            notifyPilot("Craft is disabled!");
            return;
        }
        refuel();
        if(fuel<=0){
            notifyPilot("Out of fuel!");
            return;
        }
        ArrayList<BlockMovement> movements = new ArrayList<>();
        for(Block block : blocks){
            movements.add(new BlockMovement(block.getLocation(), block.getRelative(cruise.x*type.moveDistance, cruise.y*type.moveDistance, cruise.z*type.moveDistance).getLocation()));
        }
        move(movements, false);
    }
    private Iterable<Entity> move(Collection<BlockMovement> movements, boolean force){
        if(blocks.isEmpty())return null;
        for(Block block : blocks){
            if(block.getType()==Material.AIR){
                criticalError(41693, "AIR BLOCKS FOUND ON SHIP!");
                return null;
            }
        }
        HashMap<Entity, Location> entityMovements = new HashMap<>();
        for(Entity entity : world.getNearbyEntities(getBoundingBox().expand(BlockFace.UP, 2))){
            Block b = world.getBlockAt(entity.getLocation());
            if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR){
                b = b.getRelative(BlockFace.DOWN);
            }
            if(b.getType()==Material.AIR||b.getType()==Material.CAVE_AIR){
                b = b.getRelative(BlockFace.DOWN);
            }
            if(blocks.contains(b)){
                for(BlockMovement m : movements){
                    if(m.from.equals(b.getLocation())){
                        Location diff = m.to.clone().subtract(m.from);
                        entityMovements.put(entity, rotate(entity.getLocation().add(diff), m.to.add(.5, 0, .5), m.rotation));
                        break;
                    }
                }
            }
        }
        if(!force){
            for(BlockMovement movement : movements){
                Block newLocation = world.getBlockAt(movement.to);
                if(!(blocks.contains(newLocation)||newLocation.getType()==Material.AIR||newLocation.getType()==Material.CAVE_AIR)){
                    notifyPilot("Craft obstructed by "+newLocation.getType().toString()+"! ("+newLocation.getX()+","+newLocation.getY()+","+newLocation.getZ()+")");
                    return null;
                }
            }
            fuel--;
        }
        ArrayList<BlockChange> changes = new ArrayList<>();
        ArrayList<Block> newBlocks = new ArrayList<>();
        for(BlockMovement movement : movements){
            Block movesFrom = world.getBlockAt(movement.from);
            Block movesTo = world.getBlockAt(movement.to);
            if(movesFrom.getType()==Material.AIR){
                criticalError(39614, "AIR BLOCKS FOUND ON SHIP!");
                return null;
            }
            changes.add(new BlockChange(movesTo, movesFrom, movement.rotation));
            newBlocks.add(movesTo);
        }
        for(Block block : blocks){
            if(newBlocks.contains(block))continue;
            changes.add(new BlockChange(block, Material.AIR, null, null));
        }
        Collections.sort(changes);
        int created = 0;
        int destroyed = 0;
        for(BlockChange change : changes){
            if(change.type==Material.AIR&&change.block.getType()!=Material.AIR)destroyed++;
            if(change.type!=Material.AIR&&change.block.getType()==Material.AIR)created++;
        }
        if(created-destroyed!=0){
            criticalError(81037, "NET CHANGE IS NOT EQUAL TO 0!"); 
            return null;
        }
        moving = true;
        for(Block block : blocks){
            if(blocksThatPop.contains(block.getType())){
                block.setType(Material.AIR, false);
            }
        }
        for(BlockChange change : changes){
            if(!blocksThatPop.contains(change.type))change.change();
        }
        for(BlockChange change : changes){
            if(blocksThatPop.contains(change.type)){
                change.change();
            }
        }
        moving = false;
        blocks.clear();
        blocks.addAll(newBlocks);
        calculateBoundingBox();
        for(Entity e : entityMovements.keySet()){
            e.teleport(entityMovements.get(e), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        return entityMovements.keySet();
    }
    private Location getOrigin(){
        return getBoundingBox().getCenter().toLocation(world);
    }
    private BoundingBox getBoundingBox(){
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
        bbox =  new BoundingBox(x1, y1, z1, x2, y2, z2);
    }
    public void removeBlock(Block b){
        if(moving)return;
        updateHull(blocks.remove(b)?1:0);
    }
    public void updateHull(int damage){
        for(Iterator<Block> it = blocks.iterator(); it.hasNext();){
            Block block = it.next();
            if(type.bannedBlocks.contains(block.getType())){
                it.remove();
                damage++;
            }
        }
        ChatColor color = damage==1?ChatColor.YELLOW:ChatColor.RED;
        if(damage>0)actionbar(color+"Craft took "+damage+" damage!");
        if(blocks.size()<type.minSize){
            startSinking();
        }else if(blocks.size()-100<type.minSize){
            if(damage>0)actionbar(color+"Craft minimum size: "+blocks.size()+"/"+type.minSize);
        }
        for(ArrayList<Material> materials : type.requiredRatios.keySet()){
            float requiredRatio = type.requiredRatios.get(materials);
            int amount = plugin.getBlocks(blocks, materials);
            float actualRatio = amount/(float)blocks.size();
            if(actualRatio<requiredRatio){
                startSinking();
            }else if((amount-100)/(float)blocks.size()<requiredRatio){
                if(damage>0)actionbar(color+"Blocks: ("+materials.get(0).toString()+" or similar) "+Math.round(actualRatio*10000)/100f+"%>"+Math.round(requiredRatio*10000)/100f+"%");
            }
        }
        for(ArrayList<Material> material : type.bannedRatios.keySet()){
            float ratio = type.bannedRatios.get(material);
            int amount = plugin.getBlocks(blocks, material);
            float actual = amount/(float)blocks.size();
            if(actual>ratio){
                notifyPilot(ChatColor.RED+"Warning:"+ChatColor.RESET+" This craft now has too many blocks: "+material.toString()+"! ("+Math.round(actual*10000)/100f+"%>"+Math.round(ratio*10000)/100f+"%)");
            }else if((amount+10)/(float)blocks.size()>ratio){
                if(damage>0)actionbar(color+"Block limit: ("+material.get(0).toString()+" or similar) "+Math.round(actual*10000)/100f+"%>"+Math.round(ratio*10000)/100f+"%");
            }
        }
        if(type.engines.isEmpty())disabled = false;
        else{
            float actualRatio = plugin.getBlocks(blocks, type.engines)/(float)blocks.size();
            disabled = actualRatio<type.enginePercent;
        }
        calculateBoundingBox();
    }
    public void addBlock(Block block){
        if(moving){
            actionbar(ChatColor.RED+"Cannot add block during a move!");
            return;
        }
        if(type.bannedBlocks.contains(block.getType()))return;
        ArrayList<Block> craft = new ArrayList<>(blocks);
        craft.add(block);
        if(craft.size()>type.maxSize){
            notifyPilot("Too many blocks! ("+craft.size()+">"+type.maxSize+")");
            actionbar(ChatColor.RED+"Failed to add block.");
            return;
        }else if(craft.size()+10>type.maxSize){
            actionbar(ChatColor.YELLOW+"Craft size: "+craft.size()+"/"+type.maxSize);
        }
        for(ArrayList<Material> materials : type.requiredRatios.keySet()){
            float requiredRatio = type.requiredRatios.get(materials);
            int amount = plugin.getBlocks(craft, materials);
            float actualRatio = amount/(float)craft.size();
            if(actualRatio<requiredRatio){
                notifyPilot("Not enough blocks: "+materials.get(0).toString()+" or similar! ("+Math.round(actualRatio*10000)/100f+"%>"+Math.round(requiredRatio*10000)/100f+"%)");
                actionbar(ChatColor.RED+"Failed to add block.");
                return;
            }else if((amount-10)/(float)craft.size()<requiredRatio){
                actionbar(ChatColor.YELLOW+"Blocks: ("+materials.get(0).toString()+" or similar) "+Math.round(actualRatio*10000)/100f+"%>"+Math.round(requiredRatio*10000)/100f+"%");
            }
        }
        for(ArrayList<Material> material : type.bannedRatios.keySet()){
            float ratio = type.bannedRatios.get(material);
            int amount = plugin.getBlocks(craft, material);
            float actual = amount/(float)craft.size();
            if(actual>ratio){
                notifyPilot("Too many blocks: "+material.toString()+"! ("+Math.round(actual*10000)/100f+"%>"+Math.round(ratio*10000)/100f+"%)");
                actionbar(ChatColor.RED+"Failed to add block.");
                return;
            }else if((amount+10)/(float)craft.size()>ratio){
                actionbar(ChatColor.YELLOW+"Block limit: ("+material.get(0).toString()+" or similar) "+Math.round(actual*10000)/100f+"%>"+Math.round(ratio*10000)/100f+"%");
            }
        }
        for(ArrayList<Material> material : type.limitedBlocks.keySet()){
            int limit = type.limitedBlocks.get(material);
            int actual = plugin.getBlocks(craft, material);
            if(actual>limit){
                notifyPilot("Too many blocks: "+material.get(0).toString()+"! ("+actual+">"+limit+")");
                actionbar(ChatColor.RED+"Failed to add block.");
                return;
            }else if(actual+10>limit){
                actionbar(ChatColor.YELLOW+"Block limit: ("+material.get(0).toString()+" or similar) "+actual+"/"+limit);
            }
        }
        actionbar(ChatColor.GREEN+"Successfully added block!");
        blocks.add(block);
        calculateBoundingBox();
    }
    private Location rotate(Location location, Location origin){
        location = location.subtract(origin);
        double x = -location.getZ();
        double z = location.getX();
        location.setX(x);
        location.setZ(z);
        location = location.add(origin);
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
        notifyPilot(ChatColor.RED+"This craft has taken too much damage and is now SINKING!");
        plugin.crafts.remove(this);
        plugin.sinking.add(this);
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
                if(down.getType()==Material.AIR||down.getType()==Material.CAVE_AIR||down.getType()==Material.WATER||blocks.contains(down));
                else{
                    it.remove();
                    somethingChanged = true;
                }
            }
        }while(somethingChanged);
        if(blocks.isEmpty()){
            plugin.sinking.remove(this);
            ticker.cancel();
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
    private void notifyPilot(String message){
        if(pilot!=null)pilot.sendMessage(message);
    }
    public void actionbar(String text){
        if(!(pilot instanceof CraftPlayer))return;
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.GAME_INFO);
        ((CraftPlayer)pilot).getHandle().playerConnection.sendPacket(packet);
    }
    private void refuel(){
        if(fuel>0)return;
        for(Block block : blocks){//TODO probably a better way to do that
            if(block.getType()==Material.FURNACE){
                FurnaceInventory furnace = ((Furnace)block.getState()).getInventory();
                for(Material m : plugin.fuels.keySet()){
                    if(furnace.contains(m)){
                        furnace.remove(new ItemStack(m));
                        fuel+=plugin.fuels.get(m);
                        return;
                    }
                }
            }
        }
    }
    private static boolean isInert(Material material){
        if(material.name().contains("_WOOL"))return true;
        if(material.name().contains("_PLANKS"))return true;
        if(material.name().contains("TERRACOTTA")&&!material.name().contains("GLAZED"))return true;
        switch(material){
            case END_STONE:
            case END_STONE_BRICKS:
                return true;
        }
        return false;
    }
    private void criticalError(int code, String error){
        cruise = Direction.NONE;
        notifyPilot(ChatColor.DARK_RED+"Critical error whilist moving ship:");
        notifyPilot(ChatColor.DARK_RED+error);
        notifyPilot(ChatColor.DARK_RED+"Your craft has been stopped to help prevent further damage.");
        notifyPilot(ChatColor.DARK_RED+""+ChatColor.BOLD+"Error Code: "+code);
        notifyPilot(ChatColor.DARK_RED+"Please send this code, along with as many details as possible to ThizThizzyDizzy so the problem can be fixed.");
    }
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
                if(isInert(type))return;
            }
            if(data!=null)block.setBlockData(data);
            BlockState newState = block.getState();
            if(state!=null){
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
            this.from = from;
            this.to = to;
            this.rotation = rotation;
        }
        public BlockMovement(Block from, Block to, int rotation){
            this(from.getLocation(), to.getLocation(), rotation);
        }
        public BlockMovement(Block from, Location to, int rotation){
            this(from.getLocation(), to, rotation);
        }
    }
}