package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
public abstract class Generator{
    public static ArrayList<Generator> generators = new ArrayList<>();
    public static void init(){
        generators.clear();
    }
    private final String name;
    private String edsName;
    public Generator(String name){
        this.name = name;
    }
    public static Generator loadGenerator(JSON.JSONObject json){
        String type = json.getString("type");
        for(Generator e : generators){
            if(e.getDefinitionName().equals(type)){
                Generator gen = e.newInstance();
                gen.edsName = json.getString("energy_distribution_system");
                gen.load(json);
                return gen;
            }
        }
        throw new IllegalArgumentException("Unknown generator: "+type+"!");
    }
    protected abstract void load(JSON.JSONObject json);
    public abstract Generator newInstance();
    public String getDefinitionName(){
        return name;
    }
    public abstract void init(CraftEngine engine, StandardEngine standardEngine);
    public abstract void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged);
    public abstract void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages);
    public abstract void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes);
    public String getEDSName(){
        return edsName;
    }
}