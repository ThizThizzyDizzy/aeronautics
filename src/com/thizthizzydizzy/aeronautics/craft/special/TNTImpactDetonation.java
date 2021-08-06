package com.thizthizzydizzy.aeronautics.craft.special;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
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
public class TNTImpactDetonation extends Special{
    private float squareVelocityThreshold;
    private float slowdownFactor;
    public TNTImpactDetonation(){
        super("aeronautics:tnt_impact_detonation");
    }
    @Override
    protected void load(JSON.JSONObject json){
        slowdownFactor = json.getFloat("slowdownFactor");
        squareVelocityThreshold = json.getFloat("squareVelocityThreshold");
    }
    @Override
    public Special newInstance(){
        return new TNTImpactDetonation();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public void init(CraftSpecial special){
        special.set("tnt", new HashMap<TNTPrimed, Double>());
    }
    @Override
    public void tick(CraftSpecial special){
        HashMap<TNTPrimed, Double> tnts = (HashMap<TNTPrimed, Double>)special.get("tnt");
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
            tnts.put(tnt, tnt.getVelocity().lengthSquared());
        }
        for(Iterator<TNTPrimed> it = tnts.keySet().iterator(); it.hasNext();){
            TNTPrimed tnt = it.next();
            if(tnt.getFuseTicks()<=0){
                it.remove();
                continue;
            }
            double vel = tnt.getVelocity().lengthSquared();
            double lastVelSquared = tnts.get(tnt);
            if(vel<lastVelSquared*slowdownFactor&&lastVelSquared>squareVelocityThreshold){
                tnt.setFuseTicks(0);
            }else{
                tnts.put(tnt, vel);
            }
        }
    }
    @Override
    public void event(CraftSpecial special, Event event){}
    private void createGhostBlock(Aeronautics aeronautics, Location l, Material m, long time){
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(p.getWorld()!=l.getWorld())continue;
            BlockData data = l.getWorld().getBlockAt(l).getBlockData();
            p.sendBlockChange(l, m.createBlockData());
            new BukkitRunnable() {
                @Override
                public void run(){
                    p.sendBlockChange(l, data);
                }
            }.runTaskLater(aeronautics, time);
        }
    }
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special, int damage, boolean damaged){}
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
    @Override
    public void getMessages(CraftSpecial special, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftSpecial special, ArrayList<Multiblock> multiblockTypes){}
}