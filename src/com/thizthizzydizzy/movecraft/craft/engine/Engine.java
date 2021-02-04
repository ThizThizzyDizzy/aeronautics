package com.thizthizzydizzy.movecraft.craft.engine;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import java.util.ArrayList;
public abstract class Engine{
    private static ArrayList<Engine> engines = new ArrayList<>();
    static{
        engines.add(new AirshipEngine());
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
    public abstract Engine newInstance();
    public String getName(){
        return name;
    }
}