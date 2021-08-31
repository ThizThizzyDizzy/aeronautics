package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
public abstract class EnergyDistributionSystem{
    public static ArrayList<EnergyDistributionSystem> energyDistributionSystems = new ArrayList<>();
    public static void init(){
        energyDistributionSystems.clear();
    }
    private final String definitionName, name;
    public EnergyDistributionSystem(String definitionName){
        this(definitionName, null);
    }
    public EnergyDistributionSystem(String definitionName, String name){
        this.definitionName = definitionName;
        this.name = name;
    }
    public static EnergyDistributionSystem loadEnergyDistributionSystem(JSONObject json){
        String type = json.getString("type");
        for(EnergyDistributionSystem e : energyDistributionSystems){
            if(e.getDefinitionName().equals(type)){
                EnergyDistributionSystem system = e.newInstance(json.getString("name"));
                system.load(json);
                return system;
            }
        }
        throw new IllegalArgumentException("Unknown energy distribution system: "+type+"!");
    }
    protected abstract void load(JSONObject json);
    public abstract EnergyDistributionSystem newInstance(String name);
    public String getDefinitionName(){
        return definitionName;
    }
    public String getName(){
        return name;
    }
    public abstract void init(CraftEngine engine, StandardEngine standardEngine);
    public abstract void tick(CraftEngine engine, StandardEngine standardEngine);
    public abstract void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged);
    public abstract void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages);
    public abstract void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes);
}