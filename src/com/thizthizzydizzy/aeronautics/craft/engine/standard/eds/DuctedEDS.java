package com.thizthizzydizzy.aeronautics.craft.engine.standard.eds;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.EnergyDistributionSystem;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerUser;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
public class DuctedEDS extends EnergyDistributionSystem{
    private ArrayList<BalancedEDS> connectedBits = new ArrayList<>();
    private ArrayList<Material> connectors = new ArrayList<>();
    private ArrayList<Material> axles = new ArrayList<>();
    private ArrayList<Material> belts = new ArrayList<>();
    private boolean rescanNeeded;
    public DuctedEDS(){
        super("aeronautics:ducted");
    }
    public DuctedEDS(String name){
        super("aeronautics:ducted", name);
    }
    @Override
    protected void load(JSON.JSONObject json){
        if(json.hasJSONArray("connectors")){
            for(Object obj : ((JSON.JSONArray)json.getJSONArray("connectors"))){
                connectors.addAll(Vanillify.getBlocks((String)obj));
            }
        }
        if(json.hasJSONArray("axles")){
            for(Object obj : ((JSON.JSONArray)json.getJSONArray("axles"))){
                axles.addAll(Vanillify.getBlocks((String)obj));
            }
        }
        if(json.hasJSONArray("belts")){
            for(Object obj : ((JSON.JSONArray)json.getJSONArray("belts"))){
                belts.addAll(Vanillify.getBlocks((String)obj));
            }
        }
    }
    @Override
    public EnergyDistributionSystem newInstance(String name){
        return new DuctedEDS(name);
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){
        rescan(engine, standardEngine);
    }
    @Override
    public void tick(CraftEngine engine, StandardEngine standardEngine){
        if(rescanNeeded)rescan(engine, standardEngine);
        for(var eds : connectedBits)eds.tick(engine, standardEngine);
    }
    @Override
    public void updateHull(CraftEngine engine, StandardEngine aThis, int damage, boolean damaged){
        rescanNeeded = true;//maybe only rescan if connector blocks were destroyed or if damage is negative? (i.e. block added)
    }
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){}
    private void rescan(CraftEngine engine, StandardEngine standardEngine){
        rescanNeeded = false;
        connectedBits.clear();
        HashMap<Block, Multiblock> powerConnectors = new HashMap<>();
        for(Multiblock mb : engine.getCraft().getMultiblocks()){
            ArrayList<Block> localPowerConnectors = new ArrayList<>();
            if(mb instanceof PowerUser pu){
                if(pu.getEDSName().equals(getName()))pu.getPowerConnectors(engine, standardEngine, localPowerConnectors);
            }
            for(Block b : localPowerConnectors)powerConnectors.put(b, mb);
        }
        ArrayList<Block> missingConnectors = new ArrayList<>(powerConnectors.keySet());
        while(!missingConnectors.isEmpty()){
            Block b = missingConnectors.get(0);//get the first one
            HashSet<Block> blocks = getBlocks(engine, standardEngine, missingConnectors, b, engine.getCraft().blocks.size()); //the power train probably won't include the entire craft, but what if it does?
            ArrayList<Block> theseConnectors = new ArrayList<>(missingConnectors);
            missingConnectors.removeAll(blocks);
            theseConnectors.removeAll(missingConnectors);//now it's got the right stuff in it
            ArrayList<Multiblock> connectedMultiblocks = new ArrayList<>();
            for(Block bl : theseConnectors)if(!connectedMultiblocks.contains(powerConnectors.get(bl)))connectedMultiblocks.add(powerConnectors.get(bl));
            connectedBits.add(new BalancedEDS(DuctedEDS.this.getDefinitionName(), DuctedEDS.this.getName()){
                @Override
                public ArrayList<Multiblock> getConnectedMultiblocks(CraftEngine engine){
                    return connectedMultiblocks;
                }
                @Override
                protected void load(JSON.JSONObject json){}
                @Override
                public EnergyDistributionSystem newInstance(String name){return null;}//NOP
                @Override
                public void init(CraftEngine engine, StandardEngine standardEngine){}
                @Override
                public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){}
                @Override
                public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){}
                @Override
                public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){}
            });
        }
    }
    private HashSet<Block> getBlocks(CraftEngine engine, StandardEngine standardEngine, ArrayList<Block> missingConnectors, Block origin, int limit){
        //layer zero
        HashSet<Block> results = new HashSet<>();
        HashMap<Integer, ArrayList<Block>> layers = new HashMap<>();
        ArrayList<Block> zero = new ArrayList<>();
        int total = 0;
        zero.add(origin);
        total++;
        layers.put(0, zero);
        results.addAll(zero);
        //all the other layers
        int i = 0;
        while(true){
            ArrayList<Block> layer = new ArrayList<>();
            ArrayList<Block> lastLayer = new ArrayList<>(layers.get(i));
            if(i==0&&lastLayer.isEmpty()){
                lastLayer.add(origin);
            }
            for(Block block : lastLayer){
                for(int x = -1; x<=1; x++){
                    for(int y = -1; y<=1; y++){
                        for(int z = -1; z<=1; z++){
                            if(Math.abs(x)+Math.abs(y)+Math.abs(z)!=1)continue;//not equal to 1 taxicab offset
//                            if(x==0&&y==0&&z==0)continue;//same block
                            Block newBlock = block.getRelative(x,y,z);
                            if(!connectors.contains(block.getType())&&!axles.contains(block.getType())&&!belts.contains(block.getType())&&!missingConnectors.contains(block))continue;//not part of the power train, nor a connector
                            if(!engine.getCraft().contains(block))continue;//not even part of the craft
                            Axis axis = toAxis(newBlock.getFace(block));
                            if(axles.contains(block.getType())){
                                BlockData data = block.getBlockData();
                                if(data instanceof Orientable o){
                                    if(axis!=o.getAxis())continue;//axle not facing the right direction
                                }else continue;//axle that's not orientable? :O
                            }
                            if(axles.contains(newBlock.getType())){
                                BlockData data = newBlock.getBlockData();
                                if(data instanceof Orientable o){
                                    if(axis!=o.getAxis())continue;//axle not facing the right direction
                                }else continue;//axle that's not orientable? :O
                            }
                            if(belts.contains(block.getType())){
                                BlockData data = block.getBlockData();
                                if(data instanceof Orientable o){
                                    boolean everythingIsFine = false;
                                    if(belts.contains(newBlock.getType())&&newBlock.getBlockData() instanceof Orientable or){
                                        if(or.getAxis()==o.getAxis())everythingIsFine = true;//everything is fine
                                    }
                                    if(!everythingIsFine&&axis!=o.getAxis())continue;//belt not facing axle direction
                                }else continue;//belt that's not orientable? :O
                            }
                            if(belts.contains(newBlock.getType())){
                                BlockData data = newBlock.getBlockData();
                                if(data instanceof Orientable o){
                                    boolean everythingIsFine = false;
                                    if(belts.contains(block.getType())&&block.getBlockData() instanceof Orientable or){
                                        if(or.getAxis()==o.getAxis())everythingIsFine = true;//everything is fine
                                    }
                                    if(!everythingIsFine&&axis!=o.getAxis())continue;//belt not facing axle direction
                                }else continue;//belt that's not orientable? :O
                            }
                            if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                            if(i>0&&layers.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                            if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                            layer.add(newBlock);
                            total++;
                        }
                    }
                }
            }
            layers.put(i+1, layer);
            results.addAll(layer);
            if(layer.isEmpty()||total>=limit)break;
            i++;
        }
        return results;
    }
    private Axis toAxis(BlockFace face){
        return switch(face){
            case DOWN, UP -> Axis.Y;
            case EAST, WEST -> Axis.X;
            case NORTH, SOUTH -> Axis.Z;
            default -> null;
        };
    }
}