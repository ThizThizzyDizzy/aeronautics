package com.thizthizzydizzy.aeronautics;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.CraftType;
import com.thizthizzydizzy.aeronautics.craft.collision_handler.CollisionHandler;
import com.thizthizzydizzy.aeronautics.craft.collision_handler.StandardCollisionHandler;
import com.thizthizzydizzy.aeronautics.craft.collision_handler.StopCollisionHandler;
import com.thizthizzydizzy.aeronautics.craft.detector.CraftDetector;
import com.thizthizzydizzy.aeronautics.craft.detector.StandardDetector;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.engine.LegacyStandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.eds.DuctedEDS;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.eds.OmnipresentEDS;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.engine.LiftCell;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.engine.Turbine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.FurnaceGenerator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.SingleBlockGenerator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.TestGenerator;
import com.thizthizzydizzy.aeronautics.craft.sink_handler.FallSinkHandler;
import com.thizthizzydizzy.aeronautics.craft.sink_handler.SinkHandler;
import com.thizthizzydizzy.aeronautics.craft.special.BlockResistance;
import com.thizthizzydizzy.aeronautics.craft.special.DamageReport;
import com.thizthizzydizzy.aeronautics.craft.special.FireChargeDirector;
import com.thizthizzydizzy.aeronautics.craft.special.FireChargeLifespan;
import com.thizthizzydizzy.aeronautics.craft.special.MobSpawnProtection;
import com.thizthizzydizzy.aeronautics.craft.special.PointDefenseCannon;
import com.thizthizzydizzy.aeronautics.craft.special.Special;
import com.thizthizzydizzy.aeronautics.craft.special.SpillProtection;
import com.thizthizzydizzy.aeronautics.craft.special.TNTDirector;
import com.thizthizzydizzy.aeronautics.craft.special.TNTImpactDetonation;
import com.thizthizzydizzy.aeronautics.craft.special.TNTTracer;
import com.thizthizzydizzy.aeronautics.craft.special.LegacyTurbine;
import com.thizthizzydizzy.aeronautics.craft.special.MultiblockTooltip;
import com.thizthizzydizzy.aeronautics.craft.special.VerticalTurbine;
import com.thizthizzydizzy.aeronautics.file.FileFormat;
import com.thizthizzydizzy.aeronautics.listener.BlockListener;
import com.thizthizzydizzy.aeronautics.listener.EntityListener;
import com.thizthizzydizzy.aeronautics.listener.PlayerListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
public class Aeronautics extends JavaPlugin implements Listener{
    public final ArrayList<CraftType> craftTypes = new ArrayList<>();
    public boolean debug = false;
    public final ArrayList<CraftDetector> detectors = new ArrayList<>();
    public final ArrayList<CollisionHandler> collisionHandlers = new ArrayList<>();
    public final ArrayList<SinkHandler> sinkHandlers = new ArrayList<>();
    private ArrayList<Craft> crafts = new ArrayList<>();
    public static final HashSet<Material> blocksThatPop = new HashSet<>();
    private BukkitTask tickLoop;
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
    @EventHandler
    public void init(AeronauticsInitializationEvent event){
        event.registerCraftDetector(new StandardDetector(this));
        event.registerSinkHandler(new FallSinkHandler(this));
        event.registerCollisionHandler(new StandardCollisionHandler(this));
        event.registerCollisionHandler(new StopCollisionHandler(this));
        event.registerEngine(new LegacyStandardEngine());
        event.registerEngine(new StandardEngine());
        event.registerSpecial(new FireChargeLifespan());
        event.registerSpecial(new FireChargeDirector());
        event.registerSpecial(new TNTDirector());
        event.registerSpecial(new BlockResistance());
        event.registerSpecial(new TNTTracer());
        event.registerSpecial(new SpillProtection());
        event.registerSpecial(new TNTImpactDetonation());
        event.registerSpecial(new MobSpawnProtection());
        event.registerSpecial(new PointDefenseCannon());
        event.registerSpecial(new DamageReport());
        event.registerSpecial(new LegacyTurbine());
        event.registerSpecial(new VerticalTurbine());
        event.registerSpecial(new MultiblockTooltip());
    }
    @EventHandler
    public void initStandardEngine(StandardEngineInitializationEvent event){
        event.registerEnergyDistributionSystem(new OmnipresentEDS());
        event.registerEnergyDistributionSystem(new DuctedEDS());
        event.registerGenerator(new FurnaceGenerator());
        event.registerGenerator(new SingleBlockGenerator());
        event.registerGenerator(new TestGenerator());
        event.registerEngine(new Turbine());
        event.registerEngine(new LiftCell());
    }
    public void registerCraftDetector(CraftDetector detector){
        detectors.add(detector);
    }
    public void registerSinkHandler(SinkHandler handler){
        sinkHandlers.add(handler);
    }
    public void registerCollisionHandler(CollisionHandler handler){
        collisionHandlers.add(handler);
    }
    public void registerEngine(Engine engine){
        Engine.engines.add(engine);
        engine.onRegister();
    }
    public void registerSpecial(Special special){
        Special.specials.add(special);
        special.onRegister();
    }
    @Override
    public void onEnable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this); //register self initialization event
        Engine.init();
        Special.init();
        pm.callEvent(new AeronauticsInitializationEvent(this));
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new EntityListener(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Load craft types">
        File craftsFolder = new File(getDataFolder(), "crafts");
        if(!craftsFolder.exists()){
            craftsFolder.mkdirs();
            //TODO default crafts
        }
        FILE:for(File f : craftsFolder.listFiles()){
            String name = f.getName();
            String extension;
            if(name.contains(".")){
                String[] split = name.split("\\.");
                extension = split[split.length-1];
            }else extension = "";
            for(FileFormat format : FileFormat.getFileFormats()){
                if(format.getFileExtension().equalsIgnoreCase(extension)){
                    CraftType type;
                    try{
                        type = format.load(this, f);
                    }catch(Exception ex){
                        logger.log(Level.WARNING, "Failed to load CraftType from file "+name+"!", ex);
                        continue FILE;
                    }
                    if(type==null){
                        logger.log(Level.WARNING, "Failed to load CraftType from file {0}!", name);
                    }else{
                        craftTypes.add(type);
                        logger.log(Level.INFO, "Loaded CraftType {0}", type.getName());
                    }
                    continue FILE;
                }
            }
            logger.log(Level.WARNING, "Unrecognized file extension .{0} on file {1}! (Skipping...)", new Object[]{extension, name});
        }
//</editor-fold>
        CraftSign.init(this);
        tickLoop = new BukkitRunnable(){
            @Override
            public void run(){
                //<editor-fold defaultstate="collapsed" desc="Ticking crafts">
                for(Iterator<Craft> it = crafts.iterator(); it.hasNext();){
                    Craft c = it.next();
                    c.tick();
                    if(c.dead)it.remove();
                } //</editor-fold>
            }
        }.runTaskTimer(this, 1, 1);
        getCommand("aeronautics").setExecutor(new CommandAeronautics(this));
        logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    @Override
    public void onDisable(){
        tickLoop.cancel();
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public void debug(Player player, String message){
        if(debug)player.sendMessage(message);
    }
    public void debug(Iterable<Player> players, String message){
        for(Player p : players)debug(p, message);
    }
    public Craft detect(CraftType type, Player player, Block origin){
        return type.detector.detect(type, player, origin);
    }
    public void addCraft(Craft newCraft){
        synchronized(crafts){
            crafts.add(newCraft);
            newCraft.init();
        }
    }
    public void releaseCraft(Craft craft){
        crafts.remove(craft);
    }
    public static Direction getSignRotation(BlockData data){
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
    /**
     * checks if a material is inert, meaning that it has no blockstates or NBT
     * @param material the material to check
     * @return if the material is inert
     */
    public static boolean isInert(Material material){
        //TODO something better
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
    public Craft getCraft(Block block){
        for(Craft craft : crafts){
            if(craft.contains(block))return craft;
        }
        return null;
    }
    public Craft getCraft(Location location){
        for(Craft craft : crafts){
            if(craft.getBoundingBox().contains(location.getX(),location.getY(),location.getZ())){
                return craft;
            }
        }
        return null;
    }
    public Craft getCraftOnBoard(Entity entity){
        Location location = entity.getLocation();
        for(Craft craft : crafts){
            for(int i = 0; i<craft.type.onBoardThreshold; i++){
                if(craft.getBoundingBox().contains(location.getX(),location.getY()-i,location.getZ())){
                    if(craft.isOnBoard(entity))return craft;
                }
            }
        }
        return null;
    }
    public Craft getCraft(Entity entity){
        for(Craft craft : crafts){
            if(craft.isOnBoard(entity))return craft;
        }
        return null;
    }
    public Craft getCraftWithCrew(Player player){
        for(Craft craft : crafts){
            if(craft.isCrew(player))return craft;
        }
        return null;
    }
    public ArrayList<Craft> getCrafts(){
        return crafts;
    }
}