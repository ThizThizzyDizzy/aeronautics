//TODO buttons stick when ships move
//TODO signs that are holding water can still be destroyed when their holding block is popped
//todo something to do with detection lag

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
import com.thizthizzydizzy.movecraft.craft.Direction;
import com.thizthizzydizzy.movecraft.command.CommandMovecraft;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.listener.BlockListener;
import com.thizthizzydizzy.movecraft.listener.EntityListener;
import com.thizthizzydizzy.movecraft.listener.PlayerListener;
import com.thizthizzydizzy.movecraft.option.Option;
import java.lang.reflect.Field;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
public class Movecraft extends JavaPlugin{
    public static final String[] helm = {"\\  |  /","-       -","/  |  \\"};
    public static Movecraft instance;
    private static final ArrayList<String> movements = new ArrayList<>();
    static{
        movements.add("fly");
        movements.add("dive");
    }
    public static final Set<Material> transparent = new HashSet<>();
    static{
        transparent.add(Material.AIR);
        transparent.add(Material.CAVE_AIR);
        transparent.add(Material.VOID_AIR);
        transparent.addAll(getBlocks("glass"));
        transparent.addAll(getBlocks("glass pane"));
        transparent.add(Material.IRON_BARS);
        transparent.add(Material.REDSTONE_WIRE);
        transparent.addAll(getBlocks("trapdoor"));
        transparent.add(Material.LEVER);
        transparent.addAll(getBlocks("button"));
        transparent.addAll(getBlocks("slab"));
        transparent.addAll(getBlocks("stairs"));
        transparent.addAll(getBlocks("sign"));
    }
    public boolean debug = false;
    public ArrayList<CraftType> craftTypes = new ArrayList<>();
    public ArrayList<CraftType> subcraftTypes = new ArrayList<>();
    public ArrayList<Craft> crafts = new ArrayList<>();
    public ArrayList<Craft> tickingCrafts = new ArrayList<>();
    public ArrayList<Craft> projectiles = new ArrayList<>();
    public ArrayList<Craft> sinking = new ArrayList<>();
    public ArrayList<TrackedFireball> fireballs = new ArrayList<>();
    public ArrayList<TrackedTNT> tnts = new ArrayList<>();
    private BukkitTask tickLoop;
    public void onEnable(){
        instance = this;
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        //<editor-fold defaultstate="collapsed" desc="Register Events">
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new EntityListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Register Config">
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
//</editor-fold>
        tickLoop = new BukkitRunnable(){
            @Override
            public void run(){
                //<editor-fold defaultstate="collapsed" desc="AA/cannon directors">
                for (World w : getServer().getWorlds()) {
                    if(w==null||w.getPlayers().isEmpty())continue;
                    //<editor-fold defaultstate="collapsed" desc="AA directors">
                    for(SmallFireball fireball : w.getEntitiesByClass(SmallFireball.class)){
                        if(!(fireball.getShooter() instanceof LivingEntity)){
                            boolean found = false;
                            for(TrackedFireball tracked : fireballs){
                                if(tracked.fireball==fireball){
                                    found = true;
                                    break;
                                }
                            }
                            if(!found){
                                Craft craft = getNearestCraft(fireball.getLocation());
                                if(craft!=null){
                                    Block b = craft.getAATarget(fireball.getLocation(), fireball.getVelocity());
                                    Vector targetVector = craft.getAADirection(fireball.getVelocity());
                                    if(b!=null||targetVector!=null){
                                        debug(craft.pilot, "Directing fireball!");
                                        Vector aaVel = fireball.getVelocity();
                                        double speed = aaVel.length();
                                        debug(craft.pilot, "Old speed: "+speed);
                                        aaVel = aaVel.normalize();
                                        if(b!=null)targetVector = b.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize();
                                        if(targetVector.getX() - aaVel.getX() > Option.FIREBALL_ANGLE.get(craft)){
                                            aaVel.setX(aaVel.getX() + Option.FIREBALL_ANGLE.get(craft));
                                        }else if(targetVector.getX() - aaVel.getX() < -Option.FIREBALL_ANGLE.get(craft)){
                                            aaVel.setX(aaVel.getX() - Option.FIREBALL_ANGLE.get(craft));
                                        }else{
                                            aaVel.setX(targetVector.getX());
                                        }
                                        if(targetVector.getY() - aaVel.getY() > Option.FIREBALL_ANGLE.get(craft)){
                                            aaVel.setY(aaVel.getY() + Option.FIREBALL_ANGLE.get(craft));
                                        }else if(targetVector.getY() - aaVel.getY() < -Option.FIREBALL_ANGLE.get(craft)){
                                            aaVel.setY(aaVel.getY() - Option.FIREBALL_ANGLE.get(craft));
                                        }else{
                                            aaVel.setY(targetVector.getY());
                                        }
                                        if(targetVector.getZ() - aaVel.getZ() > Option.FIREBALL_ANGLE.get(craft)){
                                            aaVel.setZ(aaVel.getZ() + Option.FIREBALL_ANGLE.get(craft));
                                        }else if(targetVector.getZ() - aaVel.getZ() < -Option.FIREBALL_ANGLE.get(craft)){
                                            aaVel.setZ(aaVel.getZ() - Option.FIREBALL_ANGLE.get(craft));
                                        }else{
                                            aaVel.setZ(targetVector.getZ());
                                        }
                                        fireball.setDirection(aaVel.normalize().multiply(fireball.getDirection().length()));
                                    }
                                }
                                fireballs.add(new TrackedFireball(fireball, 0, fireball.getVelocity(), craft.type));
                            }
                        }
                    }
//</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="Cannon directors">
                    for(TNTPrimed tnt : w.getEntitiesByClass(TNTPrimed.class)){
                        boolean found = false;
                        for(TrackedTNT tracked : tnts){
                            if(tracked.tnt==tnt){
                                found = true;
                                break;
                            }
                        }
                        if(found||tnt.getVelocity().lengthSquared()<=.35)continue;
                        Craft craft = getNearestCraft(tnt.getLocation());
                        tnts.add(new TrackedTNT(tnt, tnt.getVelocity().lengthSquared(), 0, craft.type));
                        if(craft==null)continue;
                        debug(craft.pilot, "Tracking new TNT: "+tnt.getVelocity().lengthSquared());
                        Block b = craft.getCannonTarget(tnt.getLocation(), tnt.getVelocity());
                        Vector targetVector = craft.getCannonDirection(tnt.getVelocity());
                        if(b!=null||targetVector!=null){
                            Vector tntVel = tnt.getVelocity();
                            double speed = tntVel.length();
                            tntVel = tntVel.normalize();
                            if(b!=null)targetVector = b.getLocation().toVector().subtract(tnt.getLocation().toVector()).normalize();
                            if(targetVector.getX() - tntVel.getX() > Option.TNT_ANGLE.get(craft)){
                                tntVel.setX(tntVel.getX() + Option.TNT_ANGLE.get(craft));
                            }else if(targetVector.getX() - tntVel.getX() < -Option.TNT_ANGLE.get(craft)){
                                tntVel.setX(tntVel.getX() - Option.TNT_ANGLE.get(craft));
                            }else{
                                tntVel.setX(targetVector.getX());
                            }
                            if(targetVector.getY() - tntVel.getY() > Option.TNT_ANGLE.get(craft)){
                                tntVel.setY(tntVel.getY() + Option.TNT_ANGLE.get(craft));
                            }else if(targetVector.getY() - tntVel.getY() < -Option.TNT_ANGLE.get(craft)){
                                tntVel.setY(tntVel.getY() - Option.TNT_ANGLE.get(craft));
                            }else{
                                tntVel.setY(targetVector.getY());
                            }
                            if(targetVector.getZ() - tntVel.getZ() > Option.TNT_ANGLE.get(craft)){
                                tntVel.setZ(tntVel.getZ() + Option.TNT_ANGLE.get(craft));
                            }else if(targetVector.getZ() - tntVel.getZ() < -Option.TNT_ANGLE.get(craft)){
                                tntVel.setZ(tntVel.getZ() - Option.TNT_ANGLE.get(craft));
                            }else{
                                tntVel.setZ(targetVector.getZ());
                            }
                            tntVel = tntVel.multiply(speed);
                            tntVel.setY(tnt.getVelocity().getY());
                            tnt.setVelocity(tntVel);
                        }
                    }
                    //</editor-fold>
                }
//</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Velocity-based TNT explosions">
                for(Iterator<TrackedTNT> it = tnts.iterator(); it.hasNext();){
                    TrackedTNT tracked = it.next();
                    TNTPrimed tnt = tracked.tnt;
                    if(tnt.getFuseTicks()<=0){
                        if(Option.TRACER_EXPLOSION.get(tracked.craftType)!=null)createGhostBlock(tnt.getLocation(), Option.TRACER_EXPLOSION.get(tracked.craftType), Option.TRACER_EXPLOSION_TIME.get(tracked.craftType));
                        it.remove();
                        continue;
                    }
                    double vel = tnt.getVelocity().lengthSquared();
                    if(Option.TRACER_STREAM.get(tracked.craftType)!=null){
                        if(vel>Math.pow(Option.TRACER_VELOCITY_THRESHOLD.get(tracked.craftType),2)){
                            Integer nextTracer = tracked.tracerTimer;
                            if(nextTracer==null||nextTracer<=-1)nextTracer = Option.TRACER_INTERVAL.get(tracked.craftType);
                            if(nextTracer<=0){
                                createGhostBlock(tnt.getLocation(), Option.TRACER_STREAM.get(tracked.craftType), Option.TRACER_STREAM_TIME.get(tracked.craftType));
                            }
                            tracked.tracerTimer = nextTracer-1;
                        }
                    }
                    if(vel<tracked.velocitySquared/10&&tracked.velocitySquared>.35){
                        tnt.setFuseTicks(0);
                        debug(null, "Exploding TNT: "+vel+" "+tracked.velocitySquared);
                    }else{
                        tracked.velocitySquared = vel;
                    }
                }
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Killing dead fireballs">
                for(Iterator<TrackedFireball> it = fireballs.iterator(); it.hasNext();){
                    TrackedFireball tracked = it.next();
                    SmallFireball fireball = tracked.fireball;
                    if(fireball.isDead()){
                        it.remove();
                        continue;
                    }
                    if(fireball.getDirection().length()>0.5){
                        Vector diff = fireball.getVelocity().subtract(tracked.velocity);
                        fireball.setVelocity(tracked.velocity.add(diff.multiply(.1)));
                        tracked.velocity = fireball.getVelocity();
                    }
                    tracked.despawnTimer++;
                    if(tracked.despawnTimer>Option.FIREBALL_LIFESPAN.get(tracked.craftType)){
                        fireball.remove();
                        it.remove();
                    }
                }
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Ticking crafts">
                for(Iterator<Craft> it = tickingCrafts.iterator(); it.hasNext();){
                    Craft c = it.next();
                    if(c.notTickingAnymore)it.remove();
                    c.tick();
                } //</editor-fold>
            }
        }.runTaskTimer(this, 1, 1);
        pm.addPermission(new Permission("movecraft.reload"));
        pm.addPermission(new Permission("movecraft.debug"));
        getCommand("movecraft").setExecutor(new CommandMovecraft(this));
        logger.log(Level.INFO, "{0} has been enabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
        reload();//load config
        HashMap<Material, Float> resistances = Option.BLOCK_RESISTANCE_OVERRIDE.getValue();
        if(resistances!=null){
            for(Material m : resistances.keySet()){
                try{
                    Field block = net.minecraft.server.v1_16_R3.Blocks.class.getDeclaredField(m.name());
                    block.setAccessible(true);
                    Field field = net.minecraft.server.v1_16_R3.BlockBase.class.getDeclaredField("durability");
                    field.setAccessible(true);
                    field.set(block.get(null), 1.8f);
                }catch(NoSuchFieldException|SecurityException|IllegalArgumentException|IllegalAccessException ex){
                    Logger.getLogger(Movecraft.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    public void onDisable(){
        tickLoop.cancel();
        PluginDescriptionFile pdfFile = getDescription();
        Logger logger = getLogger();
        logger.log(Level.INFO, "{0} has been disabled! (Version {1}) by ThizThizzyDizzy", new Object[]{pdfFile.getName(), pdfFile.getVersion()});
    }
    public void reload(){
        for(Craft c : crafts){
            c.release();
        }
        for(Craft c : projectiles){
            c.release();
        }
        craftTypes.clear();
        subcraftTypes.clear();
        crafts.clear();
        tickingCrafts.clear();
        projectiles.clear();
        sinking.clear();
        //<editor-fold defaultstate="collapsed" desc="Loading Crafts">
        //<editor-fold defaultstate="collapsed" desc="Crafts">
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
            for(Object key : craft.keySet()){
                if(key instanceof String){
                    String str = ((String)key).toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
                    for(Option option : Option.options){
                        if(!option.craft)continue;
                        if(option.getLocalName().equals(str)){
                            option.setValue(type, option.load(craft.get(key)));
                        }
                    }
                }
            }
            craftTypes.add(type);
        }
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Craft Children">
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
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Subcrafts">
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
//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Projectiles">
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
//</editor-fold>
        for(Option option : Option.options){
            if(option.global)option.setValue(option.loadFromConfig(getConfig()));
        }
    }
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
    public void clearDirector(Player player){
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
    public boolean placeBlock(Player player, Block block, Block against) {
        Craft craft = getCraft(against);
        if(craft!=null){
            if(!craft.checkCrew(player))return false;
            return craft.addBlock(player, block, false);
        }
        return true;
    }
    private void createGhostBlock(Location l, Material m, long time){
        for(Player p : getServer().getOnlinePlayers()){
            if(p.getWorld()!=l.getWorld())continue;
            BlockData data = l.getWorld().getBlockAt(l).getBlockData();
            p.sendBlockChange(l, m.createBlockData());
            new BukkitRunnable() {
                @Override
                public void run(){
                    p.sendBlockChange(l, data);
                }
            }.runTaskLater(this, time);
        }
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
                    theBlocks.add(Material.STRIPPED_CRIMSON_HYPHAE);
                    theBlocks.add(Material.STRIPPED_WARPED_HYPHAE);
                    break;
                case "wood":
                    theBlocks.add(Material.OAK_WOOD);
                    theBlocks.add(Material.BIRCH_WOOD);
                    theBlocks.add(Material.SPRUCE_WOOD);
                    theBlocks.add(Material.DARK_OAK_WOOD);
                    theBlocks.add(Material.ACACIA_WOOD);
                    theBlocks.add(Material.JUNGLE_WOOD);
                    theBlocks.add(Material.CRIMSON_HYPHAE);
                    theBlocks.add(Material.WARPED_HYPHAE);
                    break;
                case "stripped log":
                    theBlocks.add(Material.STRIPPED_OAK_LOG);
                    theBlocks.add(Material.STRIPPED_BIRCH_LOG);
                    theBlocks.add(Material.STRIPPED_SPRUCE_LOG);
                    theBlocks.add(Material.STRIPPED_DARK_OAK_LOG);
                    theBlocks.add(Material.STRIPPED_ACACIA_LOG);
                    theBlocks.add(Material.STRIPPED_JUNGLE_LOG);
                    theBlocks.add(Material.STRIPPED_CRIMSON_STEM);
                    theBlocks.add(Material.STRIPPED_WARPED_STEM);
                    break;
                case "log":
                    theBlocks.add(Material.OAK_LOG);
                    theBlocks.add(Material.BIRCH_LOG);
                    theBlocks.add(Material.SPRUCE_LOG);
                    theBlocks.add(Material.DARK_OAK_LOG);
                    theBlocks.add(Material.ACACIA_LOG);
                    theBlocks.add(Material.JUNGLE_LOG);
                    theBlocks.add(Material.CRIMSON_STEM);
                    theBlocks.add(Material.WARPED_STEM);
                    break;
                case "wool":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_WOOL"))theBlocks.add(m);
                    }
                    break;
                case "carpet":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_CARPET"))theBlocks.add(m);
                    }
                    break;
                case "terracotta":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("TERRACOTTA")&&!m.name().contains("GLAZED"))theBlocks.add(m);
                    }
                    break;
                case "glazed terracotta":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_GLAZED_TERRACOTTA"))theBlocks.add(m);
                    }
                    break;
                case "concrete":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_CONCRETE"))theBlocks.add(m);
                    }
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
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_BANNER"))theBlocks.add(m);
                    }
                    break;
                case "bed":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_BED"))theBlocks.add(m);
                    }
                    break;
                case "concrete powder":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_CONCRETE_POWDER"))theBlocks.add(m);
                    }
                    break;
                case "shulker box":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("SHULKER_BOX"))theBlocks.add(m);
                    }
                    break;
                case "glass":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("GLASS"))theBlocks.add(m);
                    }
                    break;
                case "glass pane":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("GLASS_PANE"))theBlocks.add(m);
                    }
                    break;
                case "stairs":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_STAIRS"))theBlocks.add(m);
                    }
                    break;
                case "slab":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_SLAB"))theBlocks.add(m);
                    }
                    break;
                case "fence":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_FENCE"))theBlocks.add(m);
                    }
                    break;
                case "piston":
                    theBlocks.add(Material.PISTON);
                    theBlocks.add(Material.STICKY_PISTON);
                    theBlocks.add(Material.PISTON_HEAD);
                    break;
                case "pressure plate":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_PRESSURE_PLATE"))theBlocks.add(m);
                    }
                    break;
                case "button":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_BUTTON"))theBlocks.add(m);
                    }
                    break;
                case "trapdoor":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_TRAPDOOR"))theBlocks.add(m);
                    }
                    break;
                case "fence gate":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_FENCE_GATE"))theBlocks.add(m);
                    }
                    break;
                case "door":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_DOOR"))theBlocks.add(m);
                    }
                    break;
                case "enchanter":
                    theBlocks.add(Material.ENCHANTING_TABLE);
                    break;
                case "wall":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_WALL"))theBlocks.add(m);
                    }
                    break;
                case "sign":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_SIGN"))theBlocks.add(m);
                    }
                    break;
                case "planks":
                    for(Material m : Material.values()){
                        if(m.isLegacy())continue;
                        if(m.name().endsWith("_PLANKS"))theBlocks.add(m);
                    }
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
        debug(null, "Finding craft for block "+block.getX()+" "+block.getY()+" "+block.getZ());
        if(getCraft(block.getLocation())==null)return null;
        CRAFTS:for(Craft craft : crafts){
            if(craft.containsBlock(block))return craft;
        }
        CRAFTS:for(Craft craft : projectiles){
            if(craft.containsBlock(block))return craft;
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
    public static class TrackedFireball{
        public final SmallFireball fireball;
        public int despawnTimer;
        public Vector velocity;
        public CraftType craftType;
        public TrackedFireball(SmallFireball fireball, int something, Vector velocity, CraftType craftType){
            this.fireball = fireball;
            this.despawnTimer = something;
            this.velocity = velocity;
            this.craftType = craftType;
        }
    }
    public static class TrackedTNT{
        public final TNTPrimed tnt;
        public double velocitySquared;
        public int tracerTimer;
        public CraftType craftType;
        public TrackedTNT(TNTPrimed tnt, double velocitySquared, int somethingTracer, CraftType craftType){
            this.tnt = tnt;
            this.velocitySquared = velocitySquared;
            this.tracerTimer = somethingTracer;
            this.craftType = craftType;
        }
    }
}