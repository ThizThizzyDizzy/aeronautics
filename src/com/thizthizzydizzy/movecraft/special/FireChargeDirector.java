package com.thizthizzydizzy.movecraft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.JSON.JSONArray;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.CraftSpecial;
import com.thizthizzydizzy.movecraft.event.BlockMoveEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
public class FireChargeDirector extends Special{
    private String displayName;
    private int range;
    private float angle;
    private Material directorItem;
    private HashSet<Material> transparent = new HashSet<>();
    public FireChargeDirector(){
        super("movecraft:fire_charge_director");
    }
    @Override
    protected void load(JSON.JSONObject json){
        displayName = json.getString("name");
        range = json.getInt("range");
        angle = json.getFloat("angle");
        directorItem = Material.matchMaterial(json.getString("item"));
        transparent.add(Material.AIR);
        transparent.add(Material.CAVE_AIR);
        transparent.add(Material.VOID_AIR);
        if(json.hasJSONArray("transparent")){
            JSONArray transparent = json.getJSONArray("transparent");
            for(Object obj : transparent){
                String block = (String)obj;
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            this.transparent.addAll(tag.getValues());
                            break;
                        }
                    }
                }else{
                    this.transparent.add(Material.matchMaterial(block));
                }
            }
        }
    }
    @Override
    public Special newInstance(){
        return new FireChargeDirector();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){
        signs.add(new CraftSign(getName()){
            @Override
            public boolean matches(Craft craft, Sign sign){
                if(craft==null)return false;//no craft
                if(!craft.hasSpecial(FireChargeDirector.this.getName()))return false;//this special isn't on that craft
                return sign.getLine(0).trim().equalsIgnoreCase(displayName);
            }
            @Override
            public void click(Craft craft, Sign sign, PlayerInteractEvent event){
                if(!craft.isCrew(event.getPlayer()))return;
                CraftSpecial special = craft.getSpecial(FireChargeDirector.this.getName());
                if(event.getAction()==Action.RIGHT_CLICK_BLOCK)((FireChargeDirector)special.getSpecial()).addDirector(special, event.getPlayer());
                if(event.getAction()==Action.LEFT_CLICK_BLOCK)((FireChargeDirector)special.getSpecial()).RemoveDirector(special, event.getPlayer());
            }
            @Override
            public void update(Craft craft, Sign sign){
                sign.setLine(0, displayName);
                sign.update();//TODO only if it changes! (so bascially never?)
            }
            @Override
            public boolean canLeftClick(Craft craft, Sign sign){
                return true;
            }
            @Override
            public boolean canRightClick(Craft craft, Sign sign){
                return true;
            }
        });
    }
    @Override
    public void init(CraftSpecial special){
        special.set("directors", new HashSet<Player>());
        special.set("targets", new HashMap<Player, Block>());
    }
    @Override
    public void tick(CraftSpecial special){}
    @Override
    public void event(CraftSpecial special, Event event){
        if(event instanceof EntitySpawnEvent){
            EntitySpawnEvent ese = (EntitySpawnEvent)event;
            if(ese.getEntityType()==EntityType.SMALL_FIREBALL){
                SmallFireball fireball = (SmallFireball)ese.getEntity();
                ProjectileSource shooter = fireball.getShooter();
                if(shooter instanceof BlockProjectileSource){
                    Block b = ((BlockProjectileSource)shooter).getBlock();
                    if(special.getCraft().blocks.contains(b)){
                        Block target = getTarget(special, fireball.getLocation(), fireball.getDirection());
                        Vector targetVector = getDirection(special, fireball.getDirection());
                        if(target!=null||targetVector!=null){
                            Vector direction = fireball.getDirection();
                            direction = direction.normalize();
                            if(target!=null)targetVector = target.getLocation().toVector().subtract(fireball.getLocation().toVector()).normalize();
                            if(targetVector.getX() - direction.getX() > angle){
                                direction.setX(direction.getX() + angle);
                            }else if(targetVector.getX() - direction.getX() < -angle){
                                direction.setX(direction.getX() - angle);
                            }else{
                                direction.setX(targetVector.getX());
                            }
                            if(targetVector.getY() - direction.getY() > angle){
                                direction.setY(direction.getY() + angle);
                            }else if(targetVector.getY() - direction.getY() < -angle){
                                direction.setY(direction.getY() - angle);
                            }else{
                                direction.setY(targetVector.getY());
                            }
                            if(targetVector.getZ() - direction.getZ() > angle){
                                direction.setZ(direction.getZ() + angle);
                            }else if(targetVector.getZ() - direction.getZ() < -angle){
                                direction.setZ(direction.getZ() - angle);
                            }else{
                                direction.setZ(targetVector.getZ());
                            }
                            fireball.setDirection(direction.normalize().multiply(fireball.getDirection().length()));
                        }
                    }
                }
            }
        }
        if(event instanceof PlayerInteractEvent){
            PlayerInteractEvent pie = (PlayerInteractEvent)event;
            if(pie.getAction()==Action.RIGHT_CLICK_AIR||pie.getAction()==Action.RIGHT_CLICK_BLOCK){
                Block target = getTarget(special, pie.getPlayer());
                HashMap<Player, Block> targets = ((HashMap<Player, Block>)special.get("targets"));
                if(target==null||pie.getPlayer().isSneaking())targets.remove(pie.getPlayer());
                else targets.put(pie.getPlayer(), target);
            }
        }
        if(event instanceof BlockMoveEvent){
            BlockMoveEvent bme = (BlockMoveEvent)event;
            HashMap<Player, Block> targets = (HashMap<Player, Block>)special.get("targets");
            for(Player p : targets.keySet()){
                if(bme.getFromBlock()==targets.get(p)){
                    targets.put(p, bme.getToBlock());
                }
            }
        }
    }
    private void addDirector(CraftSpecial special, Player player){
        ((HashSet<Player>)special.get("directors")).add(player);
    }
    private void RemoveDirector(CraftSpecial special, Player player){
        ((HashSet<Player>)special.get("directors")).remove(player);
    }
    private Block getTarget(CraftSpecial special, Location location, Vector v){
        HashSet<Player> directors = (HashSet<Player>)special.get("directors");
        if(directors.isEmpty())return null;
        Block smallest = null;
        double a = 0;
        for(Player p : directors){
            Block b = getTarget(special, p);
            if(b==null)continue;
            double angle = v.normalize().angle(b.getLocation().toVector().subtract(location.toVector().normalize()));
            if(smallest==null||angle<a){
                smallest = b;
                a = angle;
            }
        }
        return smallest;
    }
    private Vector getDirection(CraftSpecial special, Vector vect){
        HashSet<Player> directors = (HashSet<Player>)special.get("directors");
        if(directors.isEmpty())return null;
        Vector smallest = null;
        double a = 0;
        for(Player p : directors){
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
    public Vector getDirection(Player p){
        if(p.getInventory().getItemInMainHand().getType()==directorItem||p.getInventory().getItemInOffHand().getType()==directorItem){
            return p.getLocation().getDirection();
        }
        return null;
    }
    public Block getTarget(CraftSpecial special, Player p){
        HashMap<Player, Block> targets = (HashMap<Player, Block>)special.get("targets");
        if(targets.containsKey(p))return targets.get(p);
        if(p.getInventory().getItemInMainHand().getType()==Material.STICK||p.getInventory().getItemInOffHand().getType()==Material.STICK){
            RayTraceResult result = p.rayTraceBlocks(range, FluidCollisionMode.NEVER);
            if(result!=null&&result.getHitBlock()!=null&&!special.getCraft().blocks.contains(result.getHitBlock())){
                return result.getHitBlock();
            }
            Block b = p.getTargetBlock(transparent, range);
            if(!special.getCraft().blocks.contains(b))return b;
        }
        return null;
    }
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special){}
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
}