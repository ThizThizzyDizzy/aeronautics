package com.thizthizzydizzy.aeronautics.craft.collision_handler;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Medium;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import java.util.Collection;
import java.util.List;
import org.bukkit.block.Block;
public class StandardCollisionHandler extends CollisionHandler{
    private final Aeronautics aeronautics;
    public StandardCollisionHandler(Aeronautics aeronautics){
        super("aeronautics:standard");
        this.aeronautics = aeronautics;
    }
    @Override
    public CollisionHandler newInstance(){
        return new StandardCollisionHandler(aeronautics);
    }
    @Override
    public void load(JSON.JSONObject json){}
    @Override
    public CollisionResult collide(Craft craft, Collection<Block> blocks, Collection<Craft.BlockMovement> movements, Craft.BlockMovement movement, List<Medium> mediums){
        if(movement.rotation!=0){
            craft.notifyPilots("Rotation obstructed!");
            return CollisionResult.CANCEL;//no collisions when rotating
        }
        CraftEngine craftEngine = craft.getEngine("movecraft:standard_engine");
        StandardEngine standardEngine = (StandardEngine)craftEngine.getEngine();
        int mass1 = standardEngine.getMass(movement.from.getBlock().getType());
        int mass2 = standardEngine.getMass(movement.to.getBlock().getType());
        //TODO less damage at lower velocities; also apply forces to colliding ships! (reduce speed by a factor of (mass of all destroyed blocks)/(total ship mass))
        double d1 = mass2/(double)mass1;//weight for thing 1
        double d2 = mass1/(double)mass2;//weight for thing 2
        double total = d1+d2;
        double d = craft.rand.nextDouble()*total;
        if(total>d1)return CollisionResult.BREAK_OTHER;
        else return CollisionResult.BREAK;
    }
}