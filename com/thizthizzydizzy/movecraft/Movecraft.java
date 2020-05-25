//TODO Skiff launches and moving with other crafts
//TODO /cruise, /release, /pilot, etc.
//TODO ship structural integrity bossbar, showing how close to death the ship is from before it took the first bit of damage (disappears when ship is repiloted, or after ship exits combat)
//TODO command to customize all of this
//TODO notify everyone with actionbar when ship is on fire
//TODO status and fuel signs
//TODO phantom explosions while a ship is sinking?
/*
    Ship health bossbar (shows minimum possible health)
        Minimum health is calculated based off of all stats
            For example, if the ship had 60%/50% wool, and now has 55%/50% wool, that is at 50% health
            If the ship had a size of 500/400, and now has 425/400, that is at 25% health
            The LOWEST of these numbers is displayed on the bossbar
*/
package com.thizthizzydizzy.movecraft;
import com.thizthizzydizzy.movecraft.event.BlockChange;
import com.thizthizzydizzy.movecraft.event.MobSpawn;
import com.thizthizzydizzy.movecraft.event.PlayerEvent;
import com.thizthizzydizzy.movecraft.event.PlayerInteract;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
public class Movecraft extends JavaPlugin{
    public static final String[] helm = {"\\  |  /","-       -","/  |  \\"};
    public ArrayList<Craft> sinking = new ArrayList<>();
    public static Movecraft instance;
    public boolean debug = false;
    public boolean corruption = false;
    private static final ArrayList<String> movements = new ArrayList<>();
    static{
        movements.add("fly");
        movements.add("dive");
    }
    public double fireballAngleLimit;
    public static final Set<Material> transparent = new HashSet<>();
    static{
        transparent.add(Material.AIR);
        transparent.add(Material.CAVE_AIR);
        transparent.add(Material.VOID_AIR);
        transparent.addAll(getBlocks("glass"));
        transparent.addAll(getBlocks("glass pane"));
        transparent.add(Material.IRON_BARS);
        transparent.add(Material.REDSTONE_WIRE);
        transparent.add(Material.IRON_TRAPDOOR);
        transparent.add(Material.OAK_TRAPDOOR);
        transparent.add(Material.BIRCH_TRAPDOOR);
        transparent.add(Material.SPRUCE_TRAPDOOR);
        transparent.add(Material.JUNGLE_TRAPDOOR);
        transparent.add(Material.DARK_OAK_TRAPDOOR);
        transparent.add(Material.ACACIA_TRAPDOOR);
        transparent.add(Material.LEVER);
        transparent.add(Material.STONE_BUTTON);
        transparent.add(Material.OAK_BUTTON);
        transparent.add(Material.BIRCH_BUTTON);
        transparent.add(Material.SPRUCE_BUTTON);
        transparent.add(Material.JUNGLE_BUTTON);
        transparent.add(Material.DARK_OAK_BUTTON);
        transparent.add(Material.ACACIA_BUTTON);
        transparent.addAll(getBlocks("slab"));
        transparent.addAll(getBlocks("stairs"));
        transparent.add(Material.OAK_SIGN);
        transparent.add(Material.BIRCH_SIGN);
        transparent.add(Material.SPRUCE_SIGN);
        transparent.add(Material.JUNGLE_SIGN);
        transparent.add(Material.DARK_OAK_SIGN);
        transparent.add(Material.ACACIA_SIGN);
        transparent.add(Material.OAK_WALL_SIGN);
        transparent.add(Material.BIRCH_WALL_SIGN);
        transparent.add(Material.SPRUCE_WALL_SIGN);
        transparent.add(Material.JUNGLE_WALL_SIGN);
        transparent.add(Material.DARK_OAK_WALL_SIGN);
        transparent.add(Material.ACACIA_WALL_SIGN);
    }
    public int directorTargetRange;
    public double tntAngleLimit;
    public void rotateSubcraft(CraftType type, Player player, Block sign, int amount, String name){
        Craft parent = getCraft(sign);
        Craft craft = detect(type, player, sign);
        if(craft!=null){
            if(!craft.checkCrew(player))return;
            if(parent!=null){
                if(!parent.checkCrew(player))return;
                parent.rotateSubcraft(craft, player, sign, amount, name);
            }else{
                craft.rotateAbout(sign.getLocation(), amount);
            }
        }
    }
    public Craft getCraft(Location location){
        for(Craft craft : crafts){
            if(craft.getBoundingBox().contains(location.toVector()))return craft;
        }
        return null;
    }
    public void clearCopilot(Player player){
        for(Craft craft : crafts){
            craft.copilots.remove(player);
        }
    }
    void clearDirector(Player player){
        for(Craft craft : crafts){
            craft.aaDirectors.remove(player);
            craft.cannonDirectors.remove(player);
        }
    }
    public void playerJoined(Player player){
        for(Craft craft : crafts){
            if(craft.pilot.getUniqueId().equals(player.getUniqueId()))craft.pilot = player;
        }
    }
    public void launchProjectile(CraftType projectile, Player player, Block sign){
        Craft craft = detect(projectile, player, sign);
        if(craft==null)return;
        debug(player, "Launching projectile");
        if(craft.getYSize()>1){
            player.sendMessage("Projectile too tall!\nProjectiles must be 1x1!");
            return;
        }
        if(((WallSign)sign.getBlockData()).getFacing().getModX()!=0){
            if(craft.getZSize()>1){
                player.sendMessage("Projectile too wide!\nProjectiles must be 1x1!");
                return;
            }
        }else{
            if(craft.getXSize()>1){
                player.sendMessage("Projectile too wide!\nProjectiles must be 1x1!");
                return;
            }
        }
        craft.fuel = projectile.fuel;
        Direction direction = Direction.fromBlockFace(((WallSign)sign.getBlockData()).getFacing().getOppositeFace());
        debug(player, "Launched projectile heading "+direction.toString());
        craft.cruise(direction);
    }
    public void reload() {
    }
    public boolean placeBlock(Player player, Block block, Block against) {
        Craft craft = getCraft(against);
        if(craft!=null){
            if(!craft.checkCrew(player))return false;
            return craft.addBlock(player, block, false);
        }
        return true;
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
    public ArrayList<Craft> projectiles = new ArrayList<>();
    public int constructionTimeout,combatTimeout,combatPilots,combatCrew,damageTimeout;
    public boolean combatAND,combatBossbar;
    public double redThreshold,yellowThreshold,tntThreshold;
    private int fireballLifespan;
    public HashMap<Material, Float> resistances = new HashMap<>();
    public HashMap<SmallFireball, Integer> fireballs = new HashMap<>();
    public HashMap<SmallFireball, Vector> velocities = new HashMap<>();
    public HashMap<TNTPrimed, Double> tnts = new HashMap<>();
    public ArrayList<BlockMoveListener> listeners = new ArrayList<>();
    public void onMove(Block from, Block to){
        for(Craft c : crafts){
            for(Player p : c.aaDirectors){
                if(c.aaTargets.get(p)==from){
                    c.aaTargets.put(p,to);
                }
            }
            for(Player p : c.cannonDirectors){
                if(c.cannonTargets.get(p)==from){
                    c.cannonTargets.put(p,to);
                }
            }
        }
    }
    public void onEnable(){
        instance = this;
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new BlockChange(this), this);
        pm.registerEvents(new MobSpawn(this), this);
        pm.registerEvents(new PlayerEvent(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Loading Config">
        ArrayList<Object> l = (ArrayList<Object>)getConfig().getList("crafts");
        for(Object o : l){
            LinkedHashMap craft = (LinkedHashMap)o;
            CraftType type = new CraftType((String)craft.get("name"));
            type.minSize = (int) craft.get("min-size");
            type.maxSize = (int) craft.get("max-size");
            if(craft.containsKey("fuels")){
            ArrayList<Object> fuels = (ArrayList<Object>)craft.get("fuels");
                for(Object ob : fuels){
                    LinkedHashMap f = (LinkedHashMap)ob;
                    type.fuels.put(Material.matchMaterial((String)f.get("item")), (int)f.get("value"));
                }
            }
            for(String movement : movements){
                if(craft.containsKey(movement)){
                    LinkedHashMap fly = (LinkedHashMap) craft.get(movement);
                    MovementDetails move = new MovementDetails((int)fly.get("move-time"), (int)fly.get("horiz-move-distance"), (int)fly.get("vert-move-distance"));
                    if(fly.containsKey("required-blocks")){
                        ArrayList<Object> requiredBlocks = (ArrayList<Object>)fly.get("required-blocks");
                        for(Object ob : requiredBlocks){
                            LinkedHashMap b = (LinkedHashMap)ob;
                            Object required = b.get("required");
                            if(required instanceof Double){
                                move.requiredRatios.put(getBlocks((String)b.get("block")),((Number)required).floatValue());
                            }else move.requiredBlocks.put(getBlocks((String)b.get("block")), (int)required);
                        }
                    }
                    if(fly.containsKey("engines")){
                        ArrayList<Object> engines = (ArrayList<Object>)fly.get("engines");
                        for(Object ob : engines){
                            LinkedHashMap b = (LinkedHashMap)ob;
                            Object required = b.get("required");
                            if(required instanceof Double){
                                move.requiredEngineRatios.put(getBlocks((String)b.get("block")),((Number)required).floatValue());
                            }else move.requiredEngineBlocks.put(getBlocks((String)b.get("block")), (int)required);
                        }
                    }
                    switch(movement){
                        case "fly":
                            type.flight = move;
                            break;
                        case "dive":
                            type.dive = move;
                            break;
                    }
                }
            }
            if(craft.containsKey("allowed-blocks")){
                ArrayList list = (ArrayList)craft.get("allowed-blocks");
                ArrayList<Material> mats = new ArrayList<>();
                for(Object obj : list){
                    mats.addAll(getBlocks((String)obj));
                }
                FOR:for(Material m : Material.values()){
                    if(m.isBlock()&&!m.isLegacy()&&!mats.contains(m)){
                        type.banBlock(m);
                    }
                }
            }
            if(craft.containsKey("banned-blocks")){
                ArrayList list = (ArrayList)craft.get("banned-blocks");
                for(Object obj : list){
                    type.banBlocks((String)obj);
                }
            }
            if(craft.containsKey("limited-blocks")){
                ArrayList<Object> limitedBlocks = (ArrayList<Object>)craft.get("limited-blocks");
                for(Object ob : limitedBlocks){
                    LinkedHashMap b = (LinkedHashMap)ob;
                    Object required = b.get("required");
                    if(required instanceof Double){
                        type.addBannedRatio((String)b.get("block"), ((Number)required).floatValue());
                    }else type.limitBlock((String)b.get("block"), (int)required);
                }
            }
            craftTypes.add(type);
        }
        constructionTimeout = getConfig().getInt("construction-timeout");
        combatTimeout = getConfig().getInt("combat-timeout");
        combatPilots = getConfig().getInt("combat-pilots");
        combatCrew = getConfig().getInt("combat-crew");
        combatAND = getConfig().getString("combat-mode").trim().equalsIgnoreCase("and");
        combatBossbar = getConfig().getBoolean("combat-bossbar");
        damageTimeout = getConfig().getInt("damage-report-timeout");
        yellowThreshold = getConfig().getDouble("yellow-threshold");
        redThreshold = getConfig().getDouble("red-threshold");
        tntThreshold = getConfig().getDouble("tnt-threshold");
        fireballLifespan = getConfig().getInt("fireball-lifespan");
        fireballAngleLimit = getConfig().getDouble("fireball-angle");
        tntAngleLimit = getConfig().getDouble("tnt-angle");
        directorTargetRange = getConfig().getInt("director-target-range");
        if(getConfig().contains("block-resistance")){
            ArrayList<Object> rs = (ArrayList<Object>)getConfig().get("block-resistance");
            for(Object ob : rs){
                LinkedHashMap b = (LinkedHashMap)ob;
                Object required = b.get("resistance");
                if(required instanceof Double){
                    for(Material m : getBlocks((String)b.get("block"))){
                        resistances.put(m,((Number)required).floatValue());
                    }
                }else{
                    for(Material m : getBlocks((String)b.get("block"))){
                        resistances.put(m,((Number)required).intValue()/100f);
                    }
                }
            }
        }
        for(CraftType type : craftTypes){
            for(String child : type.tempChildren){
                for(CraftType possible : craftTypes){
                    if(possible.name.equalsIgnoreCase(child)){
                        type.children.add(possible);
                        break;
                    }
                }
            }
            type.tempChildren.clear();
        }
        l = (ArrayList<Object>)getConfig().getList("subcrafts");
        for(Object o : l){
            LinkedHashMap craft = (LinkedHashMap)o;
            CraftType type = new CraftType((String)craft.get("name"), CraftType.SUBCRAFT);
            type.minSize = (int)craft.get("min-size");
            type.maxSize = (int)craft.get("max-size");
            if(craft.containsKey("allowed-blocks")){
                ArrayList list = (ArrayList)craft.get("allowed-blocks");
                ArrayList<Material> mats = new ArrayList<>();
                for(Object obj : list){
                    mats.addAll(getBlocks((String)obj));
                }
                FOR:for(Material m : Material.values()){
                    if(m.isBlock()&&!m.isLegacy()&&!mats.contains(m)){
                        type.banBlock(m);
                    }
                }
            }
            if(craft.containsKey("banned-blocks")){
                ArrayList list = (ArrayList)craft.get("banned-blocks");
                for(Object obj : list){
                    type.banBlocks((String)obj);
                }
            }
            if(craft.containsKey("limited-blocks")){
                ArrayList<Object> limitedBlocks = (ArrayList<Object>)craft.get("limited-blocks");
                for(Object ob : limitedBlocks){
                    LinkedHashMap b = (LinkedHashMap)ob;
                    Object required = b.get("required");
                    if(required instanceof Double){
                        type.addBannedRatio((String)b.get("block"), ((Number)required).floatValue());
                    }else type.limitBlock((String)b.get("block"), (int)required);
                }
            }
            subcraftTypes.add(type);
        }
        l = (ArrayList<Object>)getConfig().getList("projectiles");
        for(Object o : l){
            LinkedHashMap craft = (LinkedHashMap)o;
            CraftType type = new CraftType((String)craft.get("name"), CraftType.PROJECTILE);
            type.minSize = (int)craft.get("min-size");
            type.maxSize = (int)craft.get("max-size");
            for(String movement : movements){
                if(craft.containsKey(movement)){
                    LinkedHashMap fly = (LinkedHashMap) craft.get(movement);
                    MovementDetails move = new MovementDetails((int)fly.get("move-time"), (int)fly.get("horiz-move-distance"), (int)fly.get("vert-move-distance"));
                    if(fly.containsKey("required-blocks")){
                        ArrayList<Object> requiredBlocks = (ArrayList<Object>)fly.get("required-blocks");
                        for(Object ob : requiredBlocks){
                            LinkedHashMap b = (LinkedHashMap)ob;
                            Object required = b.get("required");
                            if(required instanceof Double){
                                move.requiredRatios.put(getBlocks((String)b.get("block")),((Number)required).floatValue());
                            }else move.requiredBlocks.put(getBlocks((String)b.get("block")), (int)required);
                        }
                    }
                    if(fly.containsKey("engines")){
                        ArrayList<Object> engines = (ArrayList<Object>)fly.get("engines");
                        for(Object ob : engines){
                            LinkedHashMap b = (LinkedHashMap)ob;
                            Object required = b.get("required");
                            if(required instanceof Double){
                                move.requiredEngineRatios.put(getBlocks((String)b.get("block")),((Number)required).floatValue());
                            }else move.requiredEngineBlocks.put(getBlocks((String)b.get("block")), (int)required);
                        }
                    }
                    switch(movement){
                        case "fly":
                            type.flight = move;
                            break;
                        case "dive":
                            type.dive = move;
                            break;
                    }
                }
            }
            if(craft.containsKey("allowed-blocks")){
                ArrayList list = (ArrayList)craft.get("allowed-blocks");
                ArrayList<Material> mats = new ArrayList<>();
                for(Object obj : list){
                    mats.addAll(getBlocks((String)obj));
                }
                FOR:for(Material m : Material.values()){
                    if(m.isBlock()&&!m.isLegacy()&&!mats.contains(m)){
                        type.banBlock(m);
                    }
                }
            }
            if(craft.containsKey("banned-blocks")){
                ArrayList list = (ArrayList)craft.get("banned-blocks");
                for(Object obj : list){
                    type.banBlocks((String)obj);
                }
            }
            if(craft.containsKey("limited-blocks")){
                ArrayList<Object> limitedBlocks = (ArrayList<Object>)craft.get("limited-blocks");
                for(Object ob : limitedBlocks){
                    LinkedHashMap b = (LinkedHashMap)ob;
                    Object required = b.get("required");
                    if(required instanceof Double){
                        type.addBannedRatio((String)b.get("block"), ((Number)required).floatValue());
                    }else type.limitBlock((String)b.get("block"), (int)required);
                }
            }
            if(craft.containsKey("move-forward"))type.moveForward = (int)craft.get("move-forward");
            if(craft.containsKey("move-horiz"))type.moveHoriz = (int)craft.get("move-horiz");
            if(craft.containsKey("move-vert"))type.moveVert = (int)craft.get("move-vert");
            if(craft.containsKey("strength"))type.collisionDamage = (int)craft.get("strength");
            if(craft.containsKey("fuel"))type.fuel = (int)craft.get("fuel");
            craftTypes.add(type);
        }
//</editor-fold>
        new BukkitRunnable(){
            @Override
            public void run(){
                for (World w : getServer().getWorlds()) {
                    if(w==null||w.getPlayers().isEmpty())continue;
                    for(SmallFireball fireball : w.getEntitiesByClass(SmallFireball.class)){
                        if(!(fireball.getShooter() instanceof LivingEntity)){
                            if(!fireballs.containsKey(fireball)){
                                Craft c = getNearestCraft(fireball.getLocation());
                                if(c!=null){
                                    Block b = c.getAATarget(fireball.getLocation(), fireball.getVelocity());
                                    Vector targetVector = c.getAADirection(fireball.getVelocity());
                                    if(b!=null||targetVector!=null){
                                        debug(c.pilot, "Directing fireball!");
                                        Vector aaVel = fireball.getVelocity();
                                        double speed = aaVel.length();
                                        debug(c.pilot, "Old speed: "+speed);
                                        aaVel = aaVel.normalize();
                                        if(b!=null)targetVector = b.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize();
                                        if(targetVector.getX() - aaVel.getX() > fireballAngleLimit){
                                            aaVel.setX(aaVel.getX() + fireballAngleLimit);
                                        }else if(targetVector.getX() - aaVel.getX() < -fireballAngleLimit){
                                            aaVel.setX(aaVel.getX() - fireballAngleLimit);
                                        }else{
                                            aaVel.setX(targetVector.getX());
                                        }
                                        if(targetVector.getY() - aaVel.getY() > fireballAngleLimit){
                                            aaVel.setY(aaVel.getY() + fireballAngleLimit);
                                        }else if(targetVector.getY() - aaVel.getY() < -fireballAngleLimit){
                                            aaVel.setY(aaVel.getY() - fireballAngleLimit);
                                        }else{
                                            aaVel.setY(targetVector.getY());
                                        }
                                        if(targetVector.getZ() - aaVel.getZ() > fireballAngleLimit){
                                            aaVel.setZ(aaVel.getZ() + fireballAngleLimit);
                                        }else if(targetVector.getZ() - aaVel.getZ() < -fireballAngleLimit){
                                            aaVel.setZ(aaVel.getZ() - fireballAngleLimit);
                                        }else{
                                            aaVel.setZ(targetVector.getZ());
                                        }
                                        fireball.setDirection(aaVel.normalize().multiply(fireball.getDirection().length()));
                                    }
                                }
                                fireballs.put(fireball, 0);
                                velocities.put(fireball, fireball.getVelocity());
                            }
                        }
                    }
                    for(TNTPrimed tnt : w.getEntitiesByClass(TNTPrimed.class)){
                        if(tnts.containsKey(tnt)||tnt.getVelocity().lengthSquared()<=.35)continue;
                        Craft c = getNearestCraft(tnt.getLocation());
                        tnts.put(tnt, tnt.getVelocity().lengthSquared());
                        if(c==null)continue;
                        debug(c.pilot, "Tracking new TNT: "+tnt.getVelocity().lengthSquared());
                        Block b = c.getCannonTarget(tnt.getLocation(), tnt.getVelocity());
                        Vector targetVector = c.getCannonDirection(tnt.getVelocity());
                        if(b!=null||targetVector!=null){
                            Vector tntVel = tnt.getVelocity();
                            double speed = tntVel.length();
                            tntVel = tntVel.normalize();
                            if(b!=null)targetVector = b.getLocation().toVector().subtract(tnt.getLocation().toVector()).normalize();
                            if(targetVector.getX() - tntVel.getX() > tntAngleLimit){
                                tntVel.setX(tntVel.getX() + tntAngleLimit);
                            }else if(targetVector.getX() - tntVel.getX() < -tntAngleLimit){
                                tntVel.setX(tntVel.getX() - tntAngleLimit);
                            }else{
                                tntVel.setX(targetVector.getX());
                            }
                            if(targetVector.getY() - tntVel.getY() > tntAngleLimit){
                                tntVel.setY(tntVel.getY() + tntAngleLimit);
                            }else if(targetVector.getY() - tntVel.getY() < -tntAngleLimit){
                                tntVel.setY(tntVel.getY() - tntAngleLimit);
                            }else{
                                tntVel.setY(targetVector.getY());
                            }
                            if(targetVector.getZ() - tntVel.getZ() > tntAngleLimit){
                                tntVel.setZ(tntVel.getZ() + tntAngleLimit);
                            }else if(targetVector.getZ() - tntVel.getZ() < -tntAngleLimit){
                                tntVel.setZ(tntVel.getZ() - tntAngleLimit);
                            }else{
                                tntVel.setZ(targetVector.getZ());
                            }
                            tntVel = tntVel.multiply(speed);
                            tntVel.setY(tnt.getVelocity().getY());
                            tnt.setVelocity(tntVel);
                        }
                    }
                }
                for (Iterator<TNTPrimed> it = tnts.keySet().iterator(); it.hasNext();) {
                    TNTPrimed tnt = it.next();
                    if(tnt.getFuseTicks()<=0){
                        it.remove();
                        continue;
                    }
                    double vel = tnt.getVelocity().lengthSquared();
                    if(vel<tnts.get(tnt)/10&&tnts.get(tnt)>.35){
                        tnt.setFuseTicks(0);
                        System.out.println("Exploding TNT: "+vel+" "+tnts.get(tnt));
                    }
                    else{
                        tnts.put(tnt, vel);
                    }
                }
                for (Iterator it = fireballs.keySet().iterator(); it.hasNext();) {
                    SmallFireball f = (SmallFireball)it.next();
                    if(f.isDead()){
                        it.remove();
                        continue;
                    }
                    if(f.getDirection().length()>0.5){
                        Vector diff = f.getVelocity().subtract(velocities.get(f));
                        f.setVelocity(velocities.get(f).add(diff.multiply(.1)));
                        velocities.put(f, f.getVelocity());
                    }
                    fireballs.put(f, fireballs.get(f)+1);
                    if(fireballs.get(f)>fireballLifespan){
                        f.remove();
                        it.remove();
                    }
                }
            }
        }.runTaskTimer(this, 1, 1);
        getCommand("movecraft").setExecutor(new CommandMovecraft(this));
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
                case "stripped wood":
                    theBlocks.add(Material.STRIPPED_OAK_WOOD);
                    theBlocks.add(Material.STRIPPED_BIRCH_WOOD);
                    theBlocks.add(Material.STRIPPED_SPRUCE_WOOD);
                    theBlocks.add(Material.STRIPPED_DARK_OAK_WOOD);
                    theBlocks.add(Material.STRIPPED_ACACIA_WOOD);
                    theBlocks.add(Material.STRIPPED_JUNGLE_WOOD);
                    break;
                case "wood":
                    theBlocks.add(Material.OAK_WOOD);
                    theBlocks.add(Material.BIRCH_WOOD);
                    theBlocks.add(Material.SPRUCE_WOOD);
                    theBlocks.add(Material.DARK_OAK_WOOD);
                    theBlocks.add(Material.ACACIA_WOOD);
                    theBlocks.add(Material.JUNGLE_WOOD);
                    break;
                case "stripped log":
                    theBlocks.add(Material.STRIPPED_OAK_LOG);
                    theBlocks.add(Material.STRIPPED_BIRCH_LOG);
                    theBlocks.add(Material.STRIPPED_SPRUCE_LOG);
                    theBlocks.add(Material.STRIPPED_DARK_OAK_LOG);
                    theBlocks.add(Material.STRIPPED_ACACIA_LOG);
                    theBlocks.add(Material.STRIPPED_JUNGLE_LOG);
                    break;
                case "log":
                    theBlocks.add(Material.OAK_LOG);
                    theBlocks.add(Material.BIRCH_LOG);
                    theBlocks.add(Material.SPRUCE_LOG);
                    theBlocks.add(Material.DARK_OAK_LOG);
                    theBlocks.add(Material.ACACIA_LOG);
                    theBlocks.add(Material.JUNGLE_LOG);
                    break;
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
        if(type.type==CraftType.CRAFT){
            Craft current = getCraft(player);
            if(current!=null)current.release();
        }
        ArrayList<Block> craft = toList(getBlocks(type.bannedBlocks, sign, type.maxSize+10));
        if(craft.size()<type.minSize){
            player.sendMessage("Not enough blocks! ("+craft.size()+"<"+type.minSize+")");
            return null;
        }
        if(craft.size()>type.maxSize){
            player.sendMessage("Too many blocks! ("+craft.size()+">"+type.maxSize+")");
            return null;
        }
        if(type.flight!=null){
            for(ArrayList<Material> materials : type.flight.requiredRatios.keySet()){
                float requiredRatio = type.flight.requiredRatios.get(materials);
                float actualRatio = getBlocks(craft, materials)/(float)craft.size();
                if(actualRatio<requiredRatio){
                    player.sendMessage("Not enough flight blocks: "+materials.get(0).toString()+" or similar! ("+Math.round(actualRatio*1000)/10f+"%>"+Math.round(requiredRatio*1000)/10f+"%)");
                    return null;
                }
            }
        }
        if(type.dive!=null){
            for(ArrayList<Material> materials : type.dive.requiredRatios.keySet()){
                float requiredRatio = type.dive.requiredRatios.get(materials);
                float actualRatio = getBlocks(craft, materials)/(float)craft.size();
                if(actualRatio<requiredRatio){
                    player.sendMessage("Not enough dive blocks: "+materials.get(0).toString()+" or similar! ("+Math.round(actualRatio*1000)/10f+"%>"+Math.round(requiredRatio*1000)/10f+"%)");
                    return null;
                }
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
        if(type.type==CraftType.CRAFT){
            for(Block block : craft){
                Craft cr = getCraft(block);
                if(cr!=null&&cr.type==type){
                    player.sendMessage("This craft is already piloted!");
                    return null;
                }
            }
        }
        HashSet<String> pilots = new HashSet<>();
        boolean allowed = true;
        for(Block b : craft){
            if(Movecraft.Tags.isSign(b.getType())){
                Sign s = (Sign) b.getState();
                if(s.getLine(0).equalsIgnoreCase("Pilot:")){
                    allowed = false;
                    pilots.add(s.getLine(1));
                    pilots.add(s.getLine(2));
                    pilots.add(s.getLine(3));
                }
            }
        }
        for(String s : pilots){
            if(player.getName().equals(s))allowed = true;
        }
        if(!allowed){
            player.sendMessage("You are not a registered pilot on this craft!");
            return null;
        }
        HashSet<Block> blocks = new HashSet<>(craft);
        Craft theCraft = new Craft(this, type, blocks, player);
        if(type.type!=CraftType.SUBCRAFT){
            Craft parent = getCraft(sign);
            if(parent!=null){
                String error = parent.undock(blocks);
                if(error!=null){
                    player.sendMessage("Cannot undock from ship: "+error);
                    theCraft.release();
                    return null;
                }
            }
            if(type.type==CraftType.PROJECTILE){
                projectiles.add(theCraft);
            }else crafts.add(theCraft);
        }
        player.sendMessage("Successfully piloted craft! Size: "+blocks.size());
        return theCraft;
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
        CRAFTS:for(Craft craft : projectiles){
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
    public Craft getCoCraft(Player player){
        for(Craft craft : crafts){
            for(Player p : craft.copilots){
                if(p.getUniqueId().equals(player.getUniqueId())){
                    return craft;
                }
            }
        }
        return null;
    }
    public Craft getNearestCraft(Location l){
        Craft nearest = null;
        double dist = 0;
        for(Craft craft : crafts){
            if(nearest==null||dist>craft.distance(l)){
                nearest = craft;
                dist = craft.distance(l);
            }
        }
        return nearest;
    }
    public static boolean isHelm(String... lines){
        return (lines.length>=1&&lines[0].equalsIgnoreCase("[helm]"))||lines.length>=3&&lines[0].equals(helm[0])&&lines[1].equals(helm[1])&&lines[2].equals(helm[2]);
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
    public void debug(Player player, String text){
        if(!debug)return;
        if(!text.contains("[Movecraft]"))text = "[Movecraft] - "+text;
        getLogger().log(Level.FINEST, text);
        if(player!=null)player.sendMessage(text);
    }
    public void debug(Player player, boolean critical, boolean success, String text){
        if(!debug)return;
        if(success)debug(player, "[Movecraft] "+(critical?ChatColor.DARK_GREEN:ChatColor.GREEN)+"O"+ChatColor.RESET+" "+text);
        else debug(player, "[Movecraft] "+(critical?ChatColor.DARK_RED:ChatColor.RED)+"X"+ChatColor.RESET+" "+text);
    }
    public void addBlockMoveListener(BlockMoveListener listener){
        listeners.add(listener);
    }
    public void removeBlockMoveListener(BlockMoveListener listener){
        listeners.remove(listener);
    }
}