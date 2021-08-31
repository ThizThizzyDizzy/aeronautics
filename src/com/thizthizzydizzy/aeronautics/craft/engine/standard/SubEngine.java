package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
public abstract class SubEngine{
    public static ArrayList<SubEngine> engines = new ArrayList<>();
    public static void init(){
        engines.clear();
    }
    private final String name;
    private String edsName;
    public SubEngine(String name){
        this.name = name;
    }
    public static SubEngine loadEngine(JSON.JSONObject json){
        String type = json.getString("type");
        for(SubEngine e : engines){
            if(e.getDefinitionName().equals(type)){
                SubEngine engine = e.newInstance();
                engine.edsName = json.getString("energy_distribution_system");
                engine.load(json);
                return engine;
            }
        }
        throw new IllegalArgumentException("Unknown engine: "+type+"! (Within StandardEngine)");
    }
    protected abstract void load(JSON.JSONObject json);
    public abstract SubEngine newInstance();
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