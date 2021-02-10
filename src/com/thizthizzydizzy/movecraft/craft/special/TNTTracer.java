package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import com.thizthizzydizzy.movecraft.Movecraft;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.CraftSpecial;
import com.thizthizzydizzy.movecraft.craft.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
public class TNTTracer extends Special{
    private Material tracerBlock;
    private int tracerTime;
    private Material explosionBlock;
    private int explosionTime;
    private int interval;
    private float squareVelocityThreshold;
    public TNTTracer(){
        super("movecraft:tnt_tracer");
    }
    @Override
    protected void load(JSON.JSONObject json){
        if(json.hasJSONObject("tracer")){
            JSONObject jsonTrail = json.getJSONObject("tracer");
            tracerBlock = Material.matchMaterial(jsonTrail.getString("block"));
            tracerTime = jsonTrail.getInt("time");
        }
        if(json.hasJSONObject("explosion")){
            JSONObject jsonTrail = json.getJSONObject("explosion");
            explosionBlock = Material.matchMaterial(jsonTrail.getString("block"));
            explosionTime = jsonTrail.getInt("time");
        }
        interval = json.getInt("interval");
        squareVelocityThreshold = json.getFloat("squareVelocityThreshold");
    }
    @Override
    public Special newInstance(){
        return new TNTTracer();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public void init(CraftSpecial special){
        special.set("tnt", new HashMap<TNTPrimed, Integer>());
    }
    @Override
    public void tick(CraftSpecial special){
        HashMap<TNTPrimed, Integer> tnts = (HashMap<TNTPrimed, Integer>)special.get("tnt");
        for(TNTPrimed tnt : special.getCraft().getWorld().getEntitiesByClass(TNTPrimed.class)){
            boolean found = false;
            for(TNTPrimed tracked : tnts.keySet()){
                if(tracked==tnt){
                    found = true;
                    break;
                }
            }
            if(found||tnt.getVelocity().lengthSquared()<=squareVelocityThreshold)continue;
            //TODO is it even on this craft?
            tnts.put(tnt, -1);
        }
        for(Iterator<TNTPrimed> it = tnts.keySet().iterator(); it.hasNext();){
            TNTPrimed tnt = it.next();
            if(tnt.getFuseTicks()<=0){
                if(explosionBlock!=null)createGhostBlock(special.getCraft().movecraft, tnt.getLocation(), explosionBlock, explosionTime);
                it.remove();
                continue;
            }
            double vel = tnt.getVelocity().lengthSquared();
            if(tracerBlock!=null){
                if(vel>squareVelocityThreshold){
                    int nextTracer = tnts.get(tnt);
                    if(nextTracer<=-1)nextTracer = interval;
                    if(nextTracer<=0){
                        createGhostBlock(special.getCraft().movecraft, tnt.getLocation(), tracerBlock, tracerTime);
                    }
                    tnts.put(tnt, nextTracer-1);
                }
            }
        }
    }
    @Override
    public void event(CraftSpecial special, Event event){}
    private void createGhostBlock(Movecraft movecraft, Location l, Material m, long time){
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(p.getWorld()!=l.getWorld())continue;
            BlockData data = l.getWorld().getBlockAt(l).getBlockData();
            p.sendBlockChange(l, m.createBlockData());
            new BukkitRunnable() {
                @Override
                public void run(){
                    p.sendBlockChange(l, data);
                }
            }.runTaskLater(movecraft, time);
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