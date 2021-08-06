package com.thizthizzydizzy.aeronautics.craft.engine;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.EnergyDistributionSystem;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.Generator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.SubEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
public abstract class Engine{
    private static ArrayList<Engine> engines = new ArrayList<>();
    public static void init(){
        engines.clear();
        engines.add(new LegacyStandardEngine());
        EnergyDistributionSystem.init();
        Generator.init();
        SubEngine.init();
        engines.add(new StandardEngine());
    }
    public static void createSigns(){
        for(Engine e : engines){
            ArrayList<CraftSign> signs = new ArrayList<>();
            e.createSigns(signs);
            for(CraftSign sign : signs){
                CraftSign.addSign(sign);
            }
        }
    }
    private final String name;
    protected Engine(String name){
        this.name = name;//TODO validate
    }
    public static Engine loadEngine(JSONObject json){//maybe something more FileFormat-friendly?
        String type = json.getString("type");
        for(Engine e : engines){
            if(e.getName().equals(type)){
                Engine engine = e.newInstance();
                engine.load(json);
                return engine;
            }
        }
        throw new IllegalArgumentException("Unknown engine: "+type+"!");
    }
    protected abstract void load(JSONObject json);
    protected abstract void createSigns(ArrayList<CraftSign> signs);
    public abstract Engine newInstance();
    public String getName(){
        return name;
    }
    public String checkValid(HashSet<Block> craft){
        return null;
    }
    public abstract void init(CraftEngine engine);
    public abstract void tick(CraftEngine engine);
    public abstract void event(CraftEngine engine, Event event);
    public abstract void onUnload(CraftEngine engine);
    public abstract void onMoved(CraftEngine engine);
    public abstract boolean canRemoveBlock(CraftEngine engine, Player player, int damage, boolean damaged, Location l);
    public abstract void updateHull(CraftEngine engine, int damage, boolean damaged);
    public abstract boolean canAddBlock(CraftEngine engine, Player player, Block block, boolean force);
    public ArrayList<Message> getMessages(CraftEngine engine){
        ArrayList<Message> messages = new ArrayList<>();
        getMessages(engine, messages);
        return messages;
    }
    public abstract void getMessages(CraftEngine engine, ArrayList<Message> messages);
    public ArrayList<Multiblock> getMultiblockTypes(CraftEngine engine){
        ArrayList<Multiblock> types = new ArrayList<>();
        getMultiblockTypes(engine, types);
        return types;
    }
    public abstract void getMultiblockTypes(CraftEngine engine, ArrayList<Multiblock> multiblockTypes);
}