package com.thizthizzydizzy.movecraft.engine;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import com.thizthizzydizzy.movecraft.craft.CraftEngine;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.Message;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
public abstract class Engine{
    private static ArrayList<Engine> engines = new ArrayList<>();
    public static void init(){
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
    public abstract boolean removeBlock(CraftEngine engine, Player player, int damage, boolean damaged, Location l);
    public abstract void updateHull(CraftEngine engine);
    public abstract boolean addBlock(CraftEngine engine, Player player, Block block, boolean force);
    public abstract Message getMessage(CraftEngine engine);
}