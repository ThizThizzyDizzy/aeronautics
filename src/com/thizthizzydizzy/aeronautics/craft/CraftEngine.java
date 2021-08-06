package com.thizthizzydizzy.aeronautics.craft;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import java.util.HashMap;
public class CraftEngine{
    private final HashMap<String, Object> data = new HashMap<>();
    private final Craft craft;
    private final Engine engine;
    public CraftEngine(Craft craft, Engine engine){
        this.craft = craft;
        this.engine = engine;
        engine.init(this);
    }
    public void set(String key, Object value){
        data.put(key, value);
    }
    public Object get(String key){
        return data.get(key);
    }
    public void remove(String key){
        data.remove(key);
    }
    public Engine getEngine(){
        return engine;
    }
    public Craft getCraft(){
        return craft;
    }
}