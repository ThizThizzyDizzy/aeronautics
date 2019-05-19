package com.thizthizzydizzy.movecraft;
import com.thizthizzydizzy.movecraft.event.BlockChange;
import com.thizthizzydizzy.movecraft.event.SignClick;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
public class Movecraft extends JavaPlugin{
    public static final String[] helm = {"\\  |  /","-       -","/  |  \\"};
    public ArrayList<Craft> sinking = new ArrayList<>();
    public HashMap<Material, Integer> fuels = new HashMap<>();
    public static Movecraft instance;
    public void rotateSubcraft(CraftType type, Player player, Block sign, int amount){
        Craft craft = detect(type, player, sign);
        if(craft!=null){
            craft.rotateAbout(sign.getLocation(), amount);
        }
    }
    public Craft getCraft(Location location){
        for(Craft craft : crafts){
            if(craft.getBoundingBox().contains(location.toVector()))return craft;
        }
        return null;
    }
    public static class Tags{
        public static Set<Material> signs = new HashSet<>();
        public static Set<Material> wool = new HashSet<>();
        public static boolean isSign(Material mat){
            if(mat==null)return false;
            return signs.contains(mat);
        }
        public static boolean isWool(Material mat){
            if(mat==null)return false;
            return wool.contains(mat);
        }
        static{
            signs.add(Material.OAK_SIGN);
            signs.add(Material.BIRCH_SIGN);
            signs.add(Material.SPRUCE_SIGN);
            signs.add(Material.JUNGLE_SIGN);
            signs.add(Material.ACACIA_SIGN);
            signs.add(Material.DARK_OAK_SIGN);
            signs.add(Material.OAK_WALL_SIGN);
            signs.add(Material.BIRCH_WALL_SIGN);
            signs.add(Material.SPRUCE_WALL_SIGN);
            signs.add(Material.JUNGLE_WALL_SIGN);
            signs.add(Material.ACACIA_WALL_SIGN);
            signs.add(Material.DARK_OAK_WALL_SIGN);
            wool.add(Material.WHITE_WOOL);
            wool.add(Material.RED_WOOL);
            wool.add(Material.ORANGE_WOOL);
            wool.add(Material.YELLOW_WOOL);
            wool.add(Material.GREEN_WOOL);
            wool.add(Material.LIME_WOOL);
            wool.add(Material.BLUE_WOOL);
            wool.add(Material.MAGENTA_WOOL);
            wool.add(Material.PURPLE_WOOL);
            wool.add(Material.PINK_WOOL);
            wool.add(Material.BLACK_WOOL);
            wool.add(Material.BROWN_WOOL);
            wool.add(Material.LIGHT_GRAY_WOOL);
            wool.add(Material.LIGHT_BLUE_WOOL);
            wool.add(Material.GRAY_WOOL);
            wool.add(Material.CYAN_WOOL);
        }
    }
    public ArrayList<CraftType> craftTypes = new ArrayList<>();
    public ArrayList<CraftType> subcraftTypes = new ArrayList<>();
    public ArrayList<Craft> crafts = new ArrayList<>();
    public void onEnable(){
        instance = this;
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new SignClick(this), this);
        pm.registerEvents(new BlockChange(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
        MemorySection fuels = (MemorySection) getConfig().get("fuels");
        for(String fuel : fuels.getKeys(false)){
            this.fuels.put(Material.matchMaterial(fuel), (int)fuels.get(fuel));
        }
        ArrayList<Object> l = (ArrayList<Object>)getConfig().getList("craft-types");
        for(Object o : l){
            LinkedHashMap craft = (LinkedHashMap)o;
            CraftType type = new CraftType((String)craft.get("name"));
            for(Object aKey : craft.keySet()){
                if(aKey instanceof String){
                    String key = (String)aKey;
                    switch(key.toLowerCase()){
                        case "min-size":
                            type.minSize = (int)craft.get(aKey);
                            break;
                        case "max-size":
                            type.maxSize = (int)craft.get(aKey);
                            break;
                        case "engine-percent":
                            type.enginePercent = (double)craft.get(aKey);
                            break;
                        case "allowed-blocks":
                            ArrayList list = (ArrayList)craft.get(aKey);
                            ArrayList<Material> mats = new ArrayList<>();
                            for(Object obj : list){
                                mats.addAll(getBlocks((String)obj));
                            }
                            FOR:for(Material m : Material.values()){
                                if(m.isBlock()&&!m.isLegacy()&&!mats.contains(m)){
                                    type.banBlock(m);
                                }
                            }
                            break;
                        case "banned-blocks":
                            list = (ArrayList)craft.get(aKey);
                            for(Object obj : list){
                                type.banBlocks((String)obj);
                            }
                            break;
                        case "environments":
                            list = (ArrayList)craft.get(aKey);
                            for(Object obj : list){
                                type.environments.add(Environment.match((String)obj));
                            }
                            break;
                        case "engines":
                            list = (ArrayList)craft.get(aKey);
                            for(Object obj : list){
                                type.addEngine((String)obj);
                            }
                            break;
                        case "limited-blocks":
                            LinkedHashMap map = (LinkedHashMap)craft.get(aKey);
                            for(Object str : map.keySet()){
                                type.limitBlocks(getBlocks(str), (int)map.get(str));
                            }
                            break;
                        case "banned-ratios":
                            map = (LinkedHashMap)craft.get(aKey);
                            for(Object str : map.keySet()){
                                type.addBannedRatio(getBlocks(str), (float)(double)map.get(str));
                            }
                            break;
                        case "required-ratios":
                            map = (LinkedHashMap)craft.get(aKey);
                            for(Object str : map.keySet()){
                                type.addRequiredRatio(getBlocks(str), (float)(double)map.get(str));
                            }
                            break;
                        case "move-time":
                            type.moveTime = (int) craft.get(aKey);
                            break;
                        case "move-distance":
                            type.moveDistance = (int) craft.get(aKey);
                            break;
                    }
                }
            }
            craftTypes.add(type);
        }
        l = (ArrayList<Object>)getConfig().getList("subcrafts");
        for(Object o : l){
            LinkedHashMap craft = (LinkedHashMap)o;
            CraftType type = new CraftType((String)craft.get("name"), true);
            for(Object aKey : craft.keySet()){
                if(aKey instanceof String){
                    String key = (String)aKey;
                    switch(key.toLowerCase()){
                        case "min-size":
                            type.minSize = (int)craft.get(aKey);
                            break;
                        case "max-size":
                            type.maxSize = (int)craft.get(aKey);
                            break;
                        case "banned-blocks":
                            ArrayList list = (ArrayList)craft.get(aKey);
                            for(Object obj : list){
                                type.banBlock((String)obj);
                            }
                            break;
                    }
                }
            }
            subcraftTypes.add(type);
        }
        logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public void onDisable(){
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public static ArrayList<Material> getBlocks(Object object){
        ArrayList<Material> theBlocks = new ArrayList<>();
        if(object instanceof ArrayList){
            for(Object o : (ArrayList)object){
                if(o instanceof Material){
                    theBlocks.add((Material)o);
                }
                if(o instanceof String){
                    theBlocks.add(Material.matchMaterial((String)o));
                }
            }
        }
        if(object instanceof String){
            String str = (String)object;
            switch(str.toLowerCase().replace("_", " ").trim()){
                case "wool":
                    theBlocks.add(Material.WHITE_WOOL);
                    theBlocks.add(Material.RED_WOOL);
                    theBlocks.add(Material.ORANGE_WOOL);
                    theBlocks.add(Material.YELLOW_WOOL);
                    theBlocks.add(Material.GREEN_WOOL);
                    theBlocks.add(Material.LIME_WOOL);
                    theBlocks.add(Material.BLUE_WOOL);
                    theBlocks.add(Material.MAGENTA_WOOL);
                    theBlocks.add(Material.PURPLE_WOOL);
                    theBlocks.add(Material.PINK_WOOL);
                    theBlocks.add(Material.BLACK_WOOL);
                    theBlocks.add(Material.BROWN_WOOL);
                    theBlocks.add(Material.LIGHT_GRAY_WOOL);
                    theBlocks.add(Material.LIGHT_BLUE_WOOL);
                    theBlocks.add(Material.GRAY_WOOL);
                    theBlocks.add(Material.CYAN_WOOL);
                    break;
                case "carpet":
                    theBlocks.add(Material.WHITE_CARPET);
                    theBlocks.add(Material.RED_CARPET);
                    theBlocks.add(Material.ORANGE_CARPET);
                    theBlocks.add(Material.YELLOW_CARPET);
                    theBlocks.add(Material.GREEN_CARPET);
                    theBlocks.add(Material.LIME_CARPET);
                    theBlocks.add(Material.BLUE_CARPET);
                    theBlocks.add(Material.MAGENTA_CARPET);
                    theBlocks.add(Material.PURPLE_CARPET);
                    theBlocks.add(Material.PINK_CARPET);
                    theBlocks.add(Material.BLACK_CARPET);
                    theBlocks.add(Material.BROWN_CARPET);
                    theBlocks.add(Material.LIGHT_GRAY_CARPET);
                    theBlocks.add(Material.LIGHT_BLUE_CARPET);
                    theBlocks.add(Material.GRAY_CARPET);
                    theBlocks.add(Material.CYAN_CARPET);
                    break;
                case "terracotta":
                    theBlocks.add(Material.TERRACOTTA);
                    theBlocks.add(Material.RED_TERRACOTTA);
                    theBlocks.add(Material.ORANGE_TERRACOTTA);
                    theBlocks.add(Material.YELLOW_TERRACOTTA);
                    theBlocks.add(Material.GREEN_TERRACOTTA);
                    theBlocks.add(Material.LIME_TERRACOTTA);
                    theBlocks.add(Material.BLUE_TERRACOTTA);
                    theBlocks.add(Material.MAGENTA_TERRACOTTA);
                    theBlocks.add(Material.PURPLE_TERRACOTTA);
                    theBlocks.add(Material.PINK_TERRACOTTA);
                    theBlocks.add(Material.WHITE_TERRACOTTA);
                    theBlocks.add(Material.BLACK_TERRACOTTA);
                    theBlocks.add(Material.BROWN_TERRACOTTA);
                    theBlocks.add(Material.LIGHT_GRAY_TERRACOTTA);
                    theBlocks.add(Material.LIGHT_BLUE_TERRACOTTA);
                    theBlocks.add(Material.GRAY_TERRACOTTA);
                    theBlocks.add(Material.CYAN_TERRACOTTA);
                    break;
                case "glazed terracotta":
                    theBlocks.add(Material.RED_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.ORANGE_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.YELLOW_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.GREEN_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.LIME_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.BLUE_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.MAGENTA_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.PURPLE_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.PINK_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.WHITE_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.BLACK_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.BROWN_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.GRAY_GLAZED_TERRACOTTA);
                    theBlocks.add(Material.CYAN_GLAZED_TERRACOTTA);
                    break;
                case "concrete":
                    theBlocks.add(Material.RED_CONCRETE);
                    theBlocks.add(Material.ORANGE_CONCRETE);
                    theBlocks.add(Material.YELLOW_CONCRETE);
                    theBlocks.add(Material.GREEN_CONCRETE);
                    theBlocks.add(Material.LIME_CONCRETE);
                    theBlocks.add(Material.BLUE_CONCRETE);
                    theBlocks.add(Material.MAGENTA_CONCRETE);
                    theBlocks.add(Material.PURPLE_CONCRETE);
                    theBlocks.add(Material.PINK_CONCRETE);
                    theBlocks.add(Material.WHITE_CONCRETE);
                    theBlocks.add(Material.BLACK_CONCRETE);
                    theBlocks.add(Material.BROWN_CONCRETE);
                    theBlocks.add(Material.LIGHT_GRAY_CONCRETE);
                    theBlocks.add(Material.LIGHT_BLUE_CONCRETE);
                    theBlocks.add(Material.GRAY_CONCRETE);
                    theBlocks.add(Material.CYAN_CONCRETE);
                    break;
                case "chest":
                    theBlocks.add(Material.CHEST);
                    theBlocks.add(Material.TRAPPED_CHEST);
                    break;
                case "torch":
                    theBlocks.add(Material.TORCH);
                    theBlocks.add(Material.WALL_TORCH);
                    break;
                case "redstone torch":
                    theBlocks.add(Material.REDSTONE_TORCH);
                    theBlocks.add(Material.REDSTONE_WALL_TORCH);
                    break;
                case "banner":
                    theBlocks.add(Material.WHITE_BANNER);
                    theBlocks.add(Material.RED_BANNER);
                    theBlocks.add(Material.ORANGE_BANNER);
                    theBlocks.add(Material.YELLOW_BANNER);
                    theBlocks.add(Material.GREEN_BANNER);
                    theBlocks.add(Material.LIME_BANNER);
                    theBlocks.add(Material.BLUE_BANNER);
                    theBlocks.add(Material.MAGENTA_BANNER);
                    theBlocks.add(Material.PURPLE_BANNER);
                    theBlocks.add(Material.PINK_BANNER);
                    theBlocks.add(Material.BLACK_BANNER);
                    theBlocks.add(Material.BROWN_BANNER);
                    theBlocks.add(Material.LIGHT_GRAY_BANNER);
                    theBlocks.add(Material.LIGHT_BLUE_BANNER);
                    theBlocks.add(Material.GRAY_BANNER);
                    theBlocks.add(Material.CYAN_BANNER);
                    theBlocks.add(Material.RED_WALL_BANNER);
                    theBlocks.add(Material.ORANGE_WALL_BANNER);
                    theBlocks.add(Material.YELLOW_WALL_BANNER);
                    theBlocks.add(Material.GREEN_WALL_BANNER);
                    theBlocks.add(Material.LIME_WALL_BANNER);
                    theBlocks.add(Material.BLUE_WALL_BANNER);
                    theBlocks.add(Material.MAGENTA_WALL_BANNER);
                    theBlocks.add(Material.PURPLE_WALL_BANNER);
                    theBlocks.add(Material.PINK_WALL_BANNER);
                    theBlocks.add(Material.WHITE_WALL_BANNER);
                    theBlocks.add(Material.BLACK_WALL_BANNER);
                    theBlocks.add(Material.BROWN_WALL_BANNER);
                    theBlocks.add(Material.LIGHT_GRAY_WALL_BANNER);
                    theBlocks.add(Material.LIGHT_BLUE_WALL_BANNER);
                    theBlocks.add(Material.GRAY_WALL_BANNER);
                    theBlocks.add(Material.CYAN_WALL_BANNER);
                    break;
                case "bed":
                    theBlocks.add(Material.WHITE_BED);
                    theBlocks.add(Material.RED_BED);
                    theBlocks.add(Material.ORANGE_BED);
                    theBlocks.add(Material.YELLOW_BED);
                    theBlocks.add(Material.GREEN_BED);
                    theBlocks.add(Material.LIME_BED);
                    theBlocks.add(Material.BLUE_BED);
                    theBlocks.add(Material.MAGENTA_BED);
                    theBlocks.add(Material.PURPLE_BED);
                    theBlocks.add(Material.PINK_BED);
                    theBlocks.add(Material.BLACK_BED);
                    theBlocks.add(Material.BROWN_BED);
                    theBlocks.add(Material.LIGHT_GRAY_BED);
                    theBlocks.add(Material.LIGHT_BLUE_BED);
                    theBlocks.add(Material.GRAY_BED);
                    theBlocks.add(Material.CYAN_BED);
                    break;
                case "concrete powder":
                    theBlocks.add(Material.WHITE_CONCRETE_POWDER);
                    theBlocks.add(Material.RED_CONCRETE_POWDER);
                    theBlocks.add(Material.ORANGE_CONCRETE_POWDER);
                    theBlocks.add(Material.YELLOW_CONCRETE_POWDER);
                    theBlocks.add(Material.GREEN_CONCRETE_POWDER);
                    theBlocks.add(Material.LIME_CONCRETE_POWDER);
                    theBlocks.add(Material.BLUE_CONCRETE_POWDER);
                    theBlocks.add(Material.MAGENTA_CONCRETE_POWDER);
                    theBlocks.add(Material.PURPLE_CONCRETE_POWDER);
                    theBlocks.add(Material.PINK_CONCRETE_POWDER);
                    theBlocks.add(Material.BLACK_CONCRETE_POWDER);
                    theBlocks.add(Material.BROWN_CONCRETE_POWDER);
                    theBlocks.add(Material.LIGHT_GRAY_CONCRETE_POWDER);
                    theBlocks.add(Material.LIGHT_BLUE_CONCRETE_POWDER);
                    theBlocks.add(Material.GRAY_CONCRETE_POWDER);
                    theBlocks.add(Material.CYAN_CONCRETE_POWDER);
                    break;
                case "shulker box":
                    theBlocks.add(Material.SHULKER_BOX);
                    theBlocks.add(Material.RED_SHULKER_BOX);
                    theBlocks.add(Material.ORANGE_SHULKER_BOX);
                    theBlocks.add(Material.YELLOW_SHULKER_BOX);
                    theBlocks.add(Material.GREEN_SHULKER_BOX);
                    theBlocks.add(Material.LIME_SHULKER_BOX);
                    theBlocks.add(Material.BLUE_SHULKER_BOX);
                    theBlocks.add(Material.MAGENTA_SHULKER_BOX);
                    theBlocks.add(Material.PURPLE_SHULKER_BOX);
                    theBlocks.add(Material.PINK_SHULKER_BOX);
                    theBlocks.add(Material.WHITE_SHULKER_BOX);
                    theBlocks.add(Material.BLACK_SHULKER_BOX);
                    theBlocks.add(Material.BROWN_SHULKER_BOX);
                    theBlocks.add(Material.LIGHT_GRAY_SHULKER_BOX);
                    theBlocks.add(Material.LIGHT_BLUE_SHULKER_BOX);
                    theBlocks.add(Material.GRAY_SHULKER_BOX);
                    theBlocks.add(Material.CYAN_SHULKER_BOX);
                    break;
                case "glass":
                    theBlocks.add(Material.GLASS);
                    theBlocks.add(Material.RED_STAINED_GLASS);
                    theBlocks.add(Material.ORANGE_STAINED_GLASS);
                    theBlocks.add(Material.YELLOW_STAINED_GLASS);
                    theBlocks.add(Material.GREEN_STAINED_GLASS);
                    theBlocks.add(Material.LIME_STAINED_GLASS);
                    theBlocks.add(Material.BLUE_STAINED_GLASS);
                    theBlocks.add(Material.MAGENTA_STAINED_GLASS);
                    theBlocks.add(Material.PURPLE_STAINED_GLASS);
                    theBlocks.add(Material.PINK_STAINED_GLASS);
                    theBlocks.add(Material.WHITE_STAINED_GLASS);
                    theBlocks.add(Material.BLACK_STAINED_GLASS);
                    theBlocks.add(Material.BROWN_STAINED_GLASS);
                    theBlocks.add(Material.LIGHT_GRAY_STAINED_GLASS);
                    theBlocks.add(Material.LIGHT_BLUE_STAINED_GLASS);
                    theBlocks.add(Material.GRAY_STAINED_GLASS);
                    theBlocks.add(Material.CYAN_STAINED_GLASS);
                    break;
                case "glass pane":
                    theBlocks.add(Material.GLASS_PANE);
                    theBlocks.add(Material.RED_STAINED_GLASS_PANE);
                    theBlocks.add(Material.ORANGE_STAINED_GLASS_PANE);
                    theBlocks.add(Material.YELLOW_STAINED_GLASS_PANE);
                    theBlocks.add(Material.GREEN_STAINED_GLASS_PANE);
                    theBlocks.add(Material.LIME_STAINED_GLASS_PANE);
                    theBlocks.add(Material.BLUE_STAINED_GLASS_PANE);
                    theBlocks.add(Material.MAGENTA_STAINED_GLASS_PANE);
                    theBlocks.add(Material.PURPLE_STAINED_GLASS_PANE);
                    theBlocks.add(Material.PINK_STAINED_GLASS_PANE);
                    theBlocks.add(Material.WHITE_STAINED_GLASS_PANE);
                    theBlocks.add(Material.BLACK_STAINED_GLASS_PANE);
                    theBlocks.add(Material.BROWN_STAINED_GLASS_PANE);
                    theBlocks.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                    theBlocks.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                    theBlocks.add(Material.GRAY_STAINED_GLASS_PANE);
                    theBlocks.add(Material.CYAN_STAINED_GLASS_PANE);
                    break;
                case "stairs":
                    theBlocks.add(Material.PURPUR_STAIRS);
                    theBlocks.add(Material.OAK_STAIRS);
                    theBlocks.add(Material.COBBLESTONE_STAIRS);
                    theBlocks.add(Material.BRICK_STAIRS);
                    theBlocks.add(Material.STONE_BRICK_STAIRS);
                    theBlocks.add(Material.NETHER_BRICK_STAIRS);
                    theBlocks.add(Material.SANDSTONE_STAIRS);
                    theBlocks.add(Material.SPRUCE_STAIRS);
                    theBlocks.add(Material.BIRCH_STAIRS);
                    theBlocks.add(Material.JUNGLE_STAIRS);
                    theBlocks.add(Material.QUARTZ_STAIRS);
                    theBlocks.add(Material.ACACIA_STAIRS);
                    theBlocks.add(Material.DARK_OAK_STAIRS);
                    theBlocks.add(Material.PRISMARINE_STAIRS);
                    theBlocks.add(Material.PRISMARINE_BRICK_STAIRS);
                    theBlocks.add(Material.DARK_PRISMARINE_STAIRS);
                    theBlocks.add(Material.RED_SANDSTONE_STAIRS);
                    theBlocks.add(Material.POLISHED_GRANITE_STAIRS);
                    theBlocks.add(Material.SMOOTH_RED_SANDSTONE_STAIRS);
                    theBlocks.add(Material.MOSSY_STONE_BRICK_STAIRS);
                    theBlocks.add(Material.POLISHED_DIORITE_STAIRS);
                    theBlocks.add(Material.MOSSY_COBBLESTONE_STAIRS);
                    theBlocks.add(Material.END_STONE_BRICK_STAIRS);
                    theBlocks.add(Material.STONE_STAIRS);
                    theBlocks.add(Material.SMOOTH_SANDSTONE_STAIRS);
                    theBlocks.add(Material.SMOOTH_QUARTZ_STAIRS);
                    theBlocks.add(Material.GRANITE_STAIRS);
                    theBlocks.add(Material.ANDESITE_STAIRS);
                    theBlocks.add(Material.RED_NETHER_BRICK_STAIRS);
                    theBlocks.add(Material.POLISHED_ANDESITE_STAIRS);
                    theBlocks.add(Material.DIORITE_STAIRS);
                    break;
                case "slab":
                    theBlocks.add(Material.PURPUR_SLAB);
                    theBlocks.add(Material.OAK_SLAB);
                    theBlocks.add(Material.COBBLESTONE_SLAB);
                    theBlocks.add(Material.BRICK_SLAB);
                    theBlocks.add(Material.STONE_BRICK_SLAB);
                    theBlocks.add(Material.NETHER_BRICK_SLAB);
                    theBlocks.add(Material.SANDSTONE_SLAB);
                    theBlocks.add(Material.SPRUCE_SLAB);
                    theBlocks.add(Material.BIRCH_SLAB);
                    theBlocks.add(Material.JUNGLE_SLAB);
                    theBlocks.add(Material.QUARTZ_SLAB);
                    theBlocks.add(Material.ACACIA_SLAB);
                    theBlocks.add(Material.DARK_OAK_SLAB);
                    theBlocks.add(Material.PRISMARINE_SLAB);
                    theBlocks.add(Material.PRISMARINE_BRICK_SLAB);
                    theBlocks.add(Material.DARK_PRISMARINE_SLAB);
                    theBlocks.add(Material.RED_SANDSTONE_SLAB);
                    theBlocks.add(Material.POLISHED_GRANITE_SLAB);
                    theBlocks.add(Material.SMOOTH_RED_SANDSTONE_SLAB);
                    theBlocks.add(Material.MOSSY_STONE_BRICK_SLAB);
                    theBlocks.add(Material.POLISHED_DIORITE_SLAB);
                    theBlocks.add(Material.MOSSY_COBBLESTONE_SLAB);
                    theBlocks.add(Material.END_STONE_BRICK_SLAB);
                    theBlocks.add(Material.STONE_SLAB);
                    theBlocks.add(Material.SMOOTH_SANDSTONE_SLAB);
                    theBlocks.add(Material.SMOOTH_QUARTZ_SLAB);
                    theBlocks.add(Material.GRANITE_SLAB);
                    theBlocks.add(Material.ANDESITE_SLAB);
                    theBlocks.add(Material.RED_NETHER_BRICK_SLAB);
                    theBlocks.add(Material.POLISHED_ANDESITE_SLAB);
                    theBlocks.add(Material.DIORITE_SLAB);
                    theBlocks.add(Material.PETRIFIED_OAK_SLAB);
                    theBlocks.add(Material.SMOOTH_STONE_SLAB);
                    theBlocks.add(Material.CUT_RED_SANDSTONE_SLAB);
                    theBlocks.add(Material.CUT_SANDSTONE_SLAB);
                    break;
                case "fence":
                    theBlocks.add(Material.OAK_FENCE);
                    theBlocks.add(Material.SPRUCE_FENCE);
                    theBlocks.add(Material.BIRCH_FENCE);
                    theBlocks.add(Material.JUNGLE_FENCE);
                    theBlocks.add(Material.DARK_OAK_FENCE);
                    theBlocks.add(Material.ACACIA_FENCE);
                    theBlocks.add(Material.NETHER_BRICK_FENCE);
                    break;
                case "piston":
                    theBlocks.add(Material.PISTON);
                    theBlocks.add(Material.STICKY_PISTON);
                    theBlocks.add(Material.PISTON_HEAD);
                    break;
                case "pressure plate":
                    theBlocks.add(Material.STONE_PRESSURE_PLATE);
                    theBlocks.add(Material.OAK_PRESSURE_PLATE);
                    theBlocks.add(Material.SPRUCE_PRESSURE_PLATE);
                    theBlocks.add(Material.BIRCH_PRESSURE_PLATE);
                    theBlocks.add(Material.JUNGLE_PRESSURE_PLATE);
                    theBlocks.add(Material.ACACIA_PRESSURE_PLATE);
                    theBlocks.add(Material.DARK_OAK_PRESSURE_PLATE);
                    theBlocks.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                    theBlocks.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
                    break;
                case "button":
                    theBlocks.add(Material.STONE_BUTTON);
                    theBlocks.add(Material.OAK_BUTTON);
                    theBlocks.add(Material.SPRUCE_BUTTON);
                    theBlocks.add(Material.BIRCH_BUTTON);
                    theBlocks.add(Material.JUNGLE_BUTTON);
                    theBlocks.add(Material.ACACIA_BUTTON);
                    theBlocks.add(Material.DARK_OAK_BUTTON);
                    break;
                case "trapdoor":
                    theBlocks.add(Material.IRON_TRAPDOOR);
                    theBlocks.add(Material.OAK_TRAPDOOR);
                    theBlocks.add(Material.SPRUCE_TRAPDOOR);
                    theBlocks.add(Material.BIRCH_TRAPDOOR);
                    theBlocks.add(Material.JUNGLE_TRAPDOOR);
                    theBlocks.add(Material.ACACIA_TRAPDOOR);
                    theBlocks.add(Material.DARK_OAK_TRAPDOOR);
                    break;
                case "fence gate":
                    theBlocks.add(Material.OAK_FENCE_GATE);
                    theBlocks.add(Material.SPRUCE_FENCE_GATE);
                    theBlocks.add(Material.BIRCH_FENCE_GATE);
                    theBlocks.add(Material.JUNGLE_FENCE_GATE);
                    theBlocks.add(Material.DARK_OAK_FENCE_GATE);
                    theBlocks.add(Material.ACACIA_FENCE_GATE);
                    break;
                case "door":
                    theBlocks.add(Material.IRON_DOOR);
                    theBlocks.add(Material.OAK_DOOR);
                    theBlocks.add(Material.SPRUCE_DOOR);
                    theBlocks.add(Material.BIRCH_DOOR);
                    theBlocks.add(Material.JUNGLE_DOOR);
                    theBlocks.add(Material.ACACIA_DOOR);
                    theBlocks.add(Material.DARK_OAK_DOOR);
                    break;
                case "enchanter":
                    theBlocks.add(Material.ENCHANTING_TABLE);
                    break;
                case "wall":
                    theBlocks.add(Material.COBBLESTONE_WALL);
                    theBlocks.add(Material.MOSSY_COBBLESTONE_WALL);
                    theBlocks.add(Material.BRICK_WALL);
                    theBlocks.add(Material.PRISMARINE_WALL);
                    theBlocks.add(Material.SANDSTONE_WALL);
                    theBlocks.add(Material.RED_SANDSTONE_WALL);
                    theBlocks.add(Material.MOSSY_STONE_BRICK_WALL);
                    theBlocks.add(Material.GRANITE_WALL);
                    theBlocks.add(Material.STONE_BRICK_WALL);
                    theBlocks.add(Material.NETHER_BRICK_WALL);
                    theBlocks.add(Material.RED_NETHER_BRICK_WALL);
                    theBlocks.add(Material.ANDESITE_WALL);
                    theBlocks.add(Material.END_STONE_BRICK_WALL);
                    theBlocks.add(Material.DIORITE_WALL);
                    break;
                case "sign":
                    theBlocks.add(Material.OAK_SIGN);
                    theBlocks.add(Material.BIRCH_SIGN);
                    theBlocks.add(Material.SPRUCE_SIGN);
                    theBlocks.add(Material.JUNGLE_SIGN);
                    theBlocks.add(Material.ACACIA_SIGN);
                    theBlocks.add(Material.DARK_OAK_SIGN);
                    theBlocks.add(Material.OAK_WALL_SIGN);
                    theBlocks.add(Material.BIRCH_WALL_SIGN);
                    theBlocks.add(Material.SPRUCE_WALL_SIGN);
                    theBlocks.add(Material.JUNGLE_WALL_SIGN);
                    theBlocks.add(Material.ACACIA_WALL_SIGN);
                    theBlocks.add(Material.DARK_OAK_WALL_SIGN);
                    break;
                case "planks":
                    theBlocks.add(Material.OAK_PLANKS);
                    theBlocks.add(Material.BIRCH_PLANKS);
                    theBlocks.add(Material.SPRUCE_PLANKS);
                    theBlocks.add(Material.JUNGLE_PLANKS);
                    theBlocks.add(Material.ACACIA_PLANKS);
                    theBlocks.add(Material.DARK_OAK_PLANKS);
                    break;
                default:
                    Material m = Material.matchMaterial(str);
                    if(m!=null)theBlocks.add(m);
            }
        }
        return theBlocks;
    }
    public Craft detect(CraftType type, Player player, Block sign){
        if(!type.subcraft){
            Craft current = getCraft(player);
            if(current!=null)current.release();
        }
        //TODO Watch for pilot signs
        ArrayList<Block> craft = toList(getBlocks(type.bannedBlocks, sign, type.maxSize*100));
        if(craft.size()<type.minSize){
            player.sendMessage("Not enough blocks! ("+craft.size()+"<"+type.minSize+")");
            return null;
        }
        if(craft.size()>type.maxSize){
            player.sendMessage("Too many blocks! ("+craft.size()+">"+type.maxSize+")");
            return null;
        }
        for(ArrayList<Material> materials : type.requiredRatios.keySet()){
            float requiredRatio = type.requiredRatios.get(materials);
            float actualRatio = getBlocks(craft, materials)/(float)craft.size();
            if(actualRatio<requiredRatio){
                player.sendMessage("Not enough blocks: "+materials.get(0).toString()+" or similar! ("+Math.round(actualRatio*1000)/10f+"%>"+Math.round(requiredRatio*1000)/10f+"%)");
                return null;
            }
        }
        for(ArrayList<Material> material : type.bannedRatios.keySet()){
            float ratio = type.bannedRatios.get(material);
            float actual = getBlocks(craft, material)/(float)craft.size();
            if(actual>ratio){
                player.sendMessage("Too many blocks: "+material.toString()+"! ("+Math.round(actual*1000)/10f+"%>"+Math.round(ratio*1000)/10f+"%)");
                return null;
            }
        }
        for(ArrayList<Material> material : type.limitedBlocks.keySet()){
            int limit = type.limitedBlocks.get(material);
            int actual = getBlocks(craft, material);
            if(actual>limit){
                player.sendMessage("Too many blocks: "+material.get(0).toString()+"! ("+actual+">"+limit+")");
                return null;
            }
        }
        if(!type.subcraft){
            for(Block block : craft){
                if(getCraft(block)!=null){
                    player.sendMessage("This craft is already piloted!");
                    return null;
                }
            }
        }
        player.sendMessage("Successfully piloted craft! Size: "+craft.size());
        if(!type.subcraft){
            crafts.add(new Craft(this, type, craft, player));
        }
        return new Craft(this, type, craft, player);
    }
    private HashMap<Integer, ArrayList<Block>> getBlocks(Collection<Material> bannedMaterials, Block startingBlock, int maxBlocks){
        //layer zero
        HashMap<Integer, ArrayList<Block>> results = new HashMap<>();
        ArrayList<Block> zero = new ArrayList<>();
        int total = 0;
        if(!bannedMaterials.contains(startingBlock.getType())){
            zero.add(startingBlock);
            total++;
        }
        results.put(0, zero);
        //all the other layers
        int i = 0;
        while(true){
            ArrayList<Block> layer = new ArrayList<>();
            ArrayList<Block> lastLayer = new ArrayList<>(results.get(i));
            if(i==0&&lastLayer.isEmpty()){
                lastLayer.add(startingBlock);
            }
            for(Block block : lastLayer){
                for(int x = -1; x<=1; x++){
                    for(int y = -1; y<=1; y++){
                        for(int z = -1; z<=1; z++){
                            if(x==0&&y==0&&z==0)continue;//same block
                            Block newBlock = block.getRelative(x,y,z);
                            if(bannedMaterials.contains(newBlock.getType())){
                                continue;
                            }
                            if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                            if(i>0&&results.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                            if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                            layer.add(newBlock);
                            total++;
                        }
                    }
                }
            }
            results.put(i+1, layer);
            if(layer.isEmpty()||total>maxBlocks)break;
            i++;
        }
        return results;
    }
    private int getTotal(HashMap<Integer, ArrayList<Block>> blocks){
        int total = 0;
        for(int i : blocks.keySet()){
            total+=blocks.get(i).size();
        }
        return total;
    }
    private ArrayList<Block> toList(HashMap<Integer, ArrayList<Block>> blocks){
        ArrayList<Block> list = new ArrayList<>();
        for(int i : blocks.keySet()){
            list.addAll(blocks.get(i));
        }
        return list;
    }
    public int getBlocks(Iterable<Block> blocks, Material material){
        int i = 0;
        for(Block block : blocks){
            if(block.getType()==material)i++;
        }
        return i;
    }
    public int getBlocks(Iterable<Block> blocks, Collection<Material> materials){
        int i = 0;
        for(Block block : blocks){
            if(materials.contains(block.getType()))i++;
        }
        return i;
    }
    public Craft getCraft(Block block){
        CRAFTS:for(Craft craft : crafts){
            
            for(Block b : craft.blocks){
                if(block.equals(b)){
                    return craft;
                }
            }
        }
        return null;
    }
    public Craft getCraft(Player player){
        for(Craft craft : crafts){
            if(craft.pilot.getUniqueId().equals(player.getUniqueId())){
                return craft;
            }
        }
        return null;
    }
    public boolean isHelm(String... lines){
        return (lines.length>=1&&lines[0].equalsIgnoreCase("[helm]"))||lines.length>=3&&lines[0].equals(helm[0])&&lines[1].equals(helm[1])&&lines[2].equals(helm[2]);
    }
}