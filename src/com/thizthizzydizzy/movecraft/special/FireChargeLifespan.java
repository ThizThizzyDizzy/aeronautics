package com.thizthizzydizzy.movecraft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.CraftSpecial;
import com.thizthizzydizzy.movecraft.craft.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
public class FireChargeLifespan extends Special{
    private int time;
    public FireChargeLifespan(){
        super("movecraft:fire_charge_lifespan");
    }
    @Override
    protected void load(JSON.JSONObject json){
        time = json.getInt("time");
    }
    @Override
    public Special newInstance(){
        return new FireChargeLifespan();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public void init(CraftSpecial special){
        special.set("fireballs", new HashMap<SmallFireball, Integer>());
    }
    @Override
    public void tick(CraftSpecial special){
        HashMap<SmallFireball, Integer> map = (HashMap<SmallFireball, Integer>)special.get("fireballs");
        for(Iterator<SmallFireball> it = map.keySet().iterator(); it.hasNext();){
            SmallFireball fireball = it.next();
            map.put(fireball, map.get(fireball)+1);
            if(map.get(fireball)>time){
                fireball.remove();
                it.remove();
            }
        }
    }
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
                        ((HashMap<SmallFireball, Integer>)special.get("fireballs")).put(fireball, 0);
                    }
                }
            }
        }
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
    @Override
    public void getMessages(CraftSpecial special, ArrayList<Message> messages){}
}