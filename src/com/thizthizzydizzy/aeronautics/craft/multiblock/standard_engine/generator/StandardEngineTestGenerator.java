package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.generator;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.TestGenerator;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerSupplier;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
public class StandardEngineTestGenerator extends Multiblock implements PowerSupplier{
    private final CraftEngine engine;
    private final StandardEngine standardEngine;
    private final TestGenerator generator;
    private Direction facing;
    private int power;
    public StandardEngineTestGenerator(CraftEngine engine, StandardEngine standardEngine, TestGenerator generator){
        this(engine, standardEngine, generator, null, null, null);
    }
    public StandardEngineTestGenerator(CraftEngine engine, StandardEngine standardEngine, TestGenerator generator, Craft craft, Block origin, Direction facing){
        super("aeronautics_test:standard_engine.test_generator", craft, origin);
        this.engine = engine;
        this.standardEngine = standardEngine;
        this.generator = generator;
        this.facing = facing;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(origin.getType()!=Material.STRIPPED_DARK_OAK_LOG)return null;//quick fail
        if(origin.getBlockData() instanceof Orientable or){
            if(or.getAxis()==Axis.Y)return null;//no tall engines
            HashSet<Block> theBlocks = new HashSet<>();
            int len = 0;
            Direction d = Direction.fromAxis(or.getAxis());//only check the positive direction to prevent multiblock duplicates
            Direction l = d.getLeft();
            Direction r = d.getRight();
            for(int i = 1; i<=5; i++){//check 5 because 4 is the max and I don't want to allow more
                Block layerOrigin = origin.getRelative(d.x*i,d.y*i,d.z*i);
                if(layerOrigin.getType()!=Material.STRIPPED_DARK_OAK_LOG)break;
                if(layerOrigin.getBlockData() instanceof Orientable o&&o.getAxis()!=or.getAxis())break;//not facing the right direction
                Block lef, rig;//left & right redstone blocks (next to the axles)
                if((lef = layerOrigin.getRelative(l.toBlockFace())).getType()!=Material.REDSTONE_BLOCK)break;
                if((rig = layerOrigin.getRelative(r.toBlockFace())).getType()!=Material.REDSTONE_BLOCK)break;
                if(layerOrigin.getRelative(0,1,0).getType()!=Material.REDSTONE_BLOCK)break;
                if(layerOrigin.getRelative(0,-1,0).getType()!=Material.REDSTONE_BLOCK)break;
                Block lefn, rign;//left & right lower red nether brick walls
                if(lef.getRelative(0,1,0).getType()!=Material.RED_NETHER_BRICK_WALL)break;
                if((lefn = lef.getRelative(0,-1,0)).getType()!=Material.RED_NETHER_BRICK_WALL)break;
                if(rig.getRelative(0,1,0).getType()!=Material.RED_NETHER_BRICK_WALL)break;
                if((rign = rig.getRelative(0,-1,0)).getType()!=Material.RED_NETHER_BRICK_WALL)break;
                if(lefn.getRelative(l.toBlockFace()).getType()!=Material.REDSTONE_BLOCK)break;
                if(lefn.getRelative(0,-1,0).getType()!=Material.REDSTONE_BLOCK)break;
                if(rign.getRelative(r.toBlockFace()).getType()!=Material.REDSTONE_BLOCK)break;
                if(rign.getRelative(0,-1,0).getType()!=Material.REDSTONE_BLOCK)break;
                theBlocks.add(layerOrigin);
                theBlocks.add(lef);theBlocks.add(rig);
                theBlocks.add(layerOrigin.getRelative(0,1,0));
                theBlocks.add(layerOrigin.getRelative(0,-1,0));
                theBlocks.add(lefn);theBlocks.add(rign);
                theBlocks.add(lef.getRelative(0,1,0));
                theBlocks.add(rig.getRelative(0,1,0));
                theBlocks.add(lefn.getRelative(l.toBlockFace()));
                theBlocks.add(lefn.getRelative(0,-1,0));
                theBlocks.add(rign.getRelative(r.toBlockFace()));
                theBlocks.add(rign.getRelative(0,-1,0));
                for(Multiblock m : craft.getMultiblocks()){
                    if(m.origin==layerOrigin)return null;//if, in any way, another multiblock was found on the axle after scanning this layer, ABORT
                }
                len++;
            }
            if(len==4){
                if(!craft.contains(theBlocks))return null;//after all that searching, it doesn't even have the engine
                return new StandardEngineTestGenerator(engine, standardEngine, generator, craft, origin, d);
            }
        }
        return null;
    }
    @Override
    public void init(){}
    @Override
    public void tick(){
        power = generator.power;
    }
    @Override
    public boolean rescan(){
        return detect(craft, origin) instanceof StandardEngineTestGenerator setg&&setg.facing==facing;
    }
    @Override
    public void onDestroy(){}
    @Override
    public void onRotated(int rotation){
        while(rotation>0){
            facing = facing.getRight();
        }
        while(rotation<0){
            facing = facing.getLeft();
        }
    }
    @Override
    public int getAvailablePower(){
        return power;
    }
    @Override
    public int generate(int power){
        power = Math.min(this.power, power);
        this.power-=power;
        return power;
    }
    @Override
    public void getPowerConnectors(CraftEngine engine, StandardEngine standardEngine, List<Block> connectors){
        connectors.add(origin);
        connectors.add(origin.getRelative(facing.toBlockFace(), 3));//ignore the middle ones cuz nobody's gonna try and access those, right?
    }
    @Override
    public String getEDSName(){
        return generator.getEDSName();
    }
    @Override
    public String[] getBlockStats(boolean onSign){
        return new String[]{
            (onSign?"Pow: ":"Producing ")+generator.power
        };
    }
    @Override
    public boolean contains(Block block){
        return false;
        //TODO actually check
    }
}