package com.thizthizzydizzy.aeronautics.craft.engine.standard.engine;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.MultiblockSubEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.SubEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.engine.StandardEngineLiftCell;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Material;
public class LiftCell extends MultiblockSubEngine{
    public int minSize, maxSize;
    public HashSet<Material> interior = new HashSet<>();
    public HashSet<Material> exterior = new HashSet<>();
    public HashSet<Material> cores = new HashSet<>();
    public double noPowerCooldownMult;
    public int liftPerBlock;
    public int warmupTimeBase;
    public double powerUsageMin;
    public double powerUsageMax;
    public LiftCell(){
        super("aeronautics:lift_cell");
    }
    @Override
    protected void load(JSON.JSONObject json){
        minSize = json.getInt("min_size");
        maxSize = json.getInt("max_size");
        for(Object o : json.getJSONArray("interior")){
            interior.addAll(Vanillify.getBlocks((String)o));
        }
        for(Object o : json.getJSONArray("exterior")){
            exterior.addAll(Vanillify.getBlocks((String)o));
        }
        for(Object o : json.getJSONArray("cores")){
            cores.addAll(Vanillify.getBlocks((String)o));
        }
        noPowerCooldownMult = json.getDouble("no_power_cooldown_mult");
        liftPerBlock = json.getInt("lift_per_block");
        warmupTimeBase = json.getInt("warmup_time_base");
        powerUsageMin = json.getDouble("power_usage_min");
        powerUsageMax = json.getDouble("power_usage_max");
    }
    @Override
    public SubEngine newInstance(){
        return new LiftCell();
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void tick(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){}
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new StandardEngineLiftCell(engine, standardEngine, this));
    }
}