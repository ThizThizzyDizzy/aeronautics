package com.thizthizzydizzy.movecraft.craft;
import com.thizthizzydizzy.movecraft.special.Special;
import java.util.HashMap;
public class CraftSpecial{
    private final HashMap<String, Object> data = new HashMap<>();
    private final Craft craft;
    private final Special special;
    public CraftSpecial(Craft craft, Special special){
        this.craft = craft;
        this.special = special;
        special.init(this);
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
    public Special getSpecial(){
        return special;
    }
    public Craft getCraft(){
        return craft;
    }
}