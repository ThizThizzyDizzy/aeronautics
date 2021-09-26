package com.thizthizzydizzy.aeronautics.craft.engine.standard.generator;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.Generator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.generator.StandardEngineFurnaceGenerator;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
public class FurnaceGenerator extends Generator{
    public HashMap<Material, FuelInfo> fuels = new HashMap<>();
    public FurnaceGenerator(){
        super("aeronautics:furnace_generator");
    }
    @Override
    protected void load(JSON.JSONObject json){
        for(Object o : json.getJSONArray("fuels")){
            JSON.JSONObject jobj = (JSON.JSONObject)o;
            for(Object ob : jobj.getJSONArray("items")){
                for(Material m : Vanillify.getItemsAndBlocks((String)ob)){
                    fuels.put(m, new FuelInfo(jobj.getInt("power"), jobj.getInt("time")));
                }
            }
        }
    }
    @Override
    public Generator newInstance(){
        return new FurnaceGenerator();
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){}
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new StandardEngineFurnaceGenerator(engine, standardEngine, this));
    }
    public static class FuelInfo{
        public final int power;
        public final int time;
        public FuelInfo(int power, int time){
            this.power = power;
            this.time = time;
        }
    }
}