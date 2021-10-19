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
    public void setBoolean(String key, boolean value){
        set(key, value);
    }
    public void setLong(String key, long l){
        set(key, l);
    }
    public <T extends Object> T get(String key){
        return (T)data.get(key);
    }
    public boolean getBoolean(String key){
        return get(key);
    }
    public long getLong(String key){
        return get(key);
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