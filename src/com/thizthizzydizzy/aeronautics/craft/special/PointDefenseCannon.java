package com.thizthizzydizzy.aeronautics.craft.special;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.PDCMultiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.PDCTarget;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.BoundingBox;
public class PointDefenseCannon extends Special{
    public float trackingSpeed;
    public float accuracy;
    public int minTargetTimeout;
    public int targetingAttempts;
    public float firingAngle;
    public float targetingRange;
    public float range;
    public float muzzleVelocity;
    public float acceleration;
    public float verticalAngle;
    public boolean noGravity;
    public PointDefenseCannon(){
        super("aeronautics:pdc");
    }
    @Override
    protected void load(JSON.JSONObject json){
        trackingSpeed = json.getFloat("trackingSpeed");
        accuracy = json.getFloat("accuracy");
        minTargetTimeout = json.getInt("minTargetTimeout");
        targetingAttempts = json.getInt("targetingAttempts");
        firingAngle = json.getFloat("firingAngle");
        targetingRange = json.getFloat("targetingRange");
        range = json.getFloat("range");
        muzzleVelocity = json.getFloat("muzzleVelocity");
        acceleration = json.getFloat("acceleration");
        verticalAngle = json.getFloat("verticalAngle");
        noGravity = json.getBoolean("noGravity");
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Special newInstance(){
        return new PointDefenseCannon();
    }
    @Override
    public void init(CraftSpecial special){}
    @Override
    public void tick(CraftSpecial special){}
    @Override
    public void event(CraftSpecial special, Event event){}
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
    public void getMultiblockTypes(CraftSpecial special, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new PDCMultiblock(special, this));
    }
    public HashSet<PDCTarget> getTargets(CraftSpecial special){
        HashSet<PDCTarget> targets = new HashSet<>();
        for(Craft craft : special.getCraft().aeronautics.getCrafts()){
            if(craft==special.getCraft())continue;
            targets.add(new PDCTarget(){
                @Override
                public BoundingBox getBoundingBox(){
                    return craft.getBoundingBox();
                }
                @Override
                public boolean isTarget(Block block){
                    return craft.contains(block);
                }
                @Override
                public boolean isValid(){
                    return !craft.dead;
                }
                @Override
                public World getWorld(){
                    return craft.getWorld();
                }
            });
        }
        return targets;
    }
}