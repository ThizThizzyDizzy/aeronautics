package com.thizthizzydizzy.aeronautics.craft.engine.standard.engine;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.MultiblockSubEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.SubEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.engine.StandardEngineTurbine;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import org.bukkit.Material;
public class Turbine extends MultiblockSubEngine{
    public int minLength, maxLength, minBladeLength, maxBladeLength, warmupTimeBase, powerUsageBase;
    public double particleDensity, particleSpeed, particlePower, warmupTimePower, powerUsagePower;
    public ArrayList<Material> rotors = new ArrayList<>();
    public ArrayList<Material> outlets = new ArrayList<>();
    public ArrayList<Material> bladeMaterials = new ArrayList<>();
    public Turbine(){
        super("aeronautics:turbine");
    }
    @Override
    protected void load(JSON.JSONObject json){
        minLength = json.getInt("min_length");
        maxLength = json.getInt("max_length");
        minBladeLength = json.getInt("min_blade_length");
        maxBladeLength = json.getInt("max_blade_length");
        particleDensity = json.getDouble("particle_density");
        particleSpeed = json.getDouble("particle_speed");
        particlePower = json.getDouble("particle_power");
        warmupTimeBase = json.getInt("warmup_time_base");
        warmupTimePower = json.getDouble("warmup_time_power");
        powerUsageBase = json.getInt("power_usage_base");
        powerUsagePower = json.getDouble("power_usage_power");
        for(Object o : json.getJSONArray("rotors")){
            rotors.addAll(Vanillify.getBlocks((String)o));
        }
        for(Object o : json.getJSONArray("outlets")){
            outlets.addAll(Vanillify.getBlocks((String)o));
        }
        for(Object o : json.getJSONArray("blade_materials")){
            bladeMaterials.addAll(Vanillify.getBlocks((String)o));
        }
    }
    @Override
    public SubEngine newInstance(){
        return new Turbine();
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void tick(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){
        //TODO recalculate thrust if multiblocks are changed?
    }
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new StandardEngineTurbine(engine, standardEngine, this));
    }
}