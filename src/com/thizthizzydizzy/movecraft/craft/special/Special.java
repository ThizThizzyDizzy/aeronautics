package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import java.util.ArrayList;
public abstract class Special{
    private static ArrayList<Special> specials = new ArrayList<>();
    static{
        specials.add(new FireChargeLifespan());
        specials.add(new FireChargeDirector());
        specials.add(new TNTDirector());
        specials.add(new BlockResistance());
        specials.add(new TNTTracer());
    }
    private final String name;
    protected Special(String name){
        this.name = name;//TODO validate
    }
    public static Special loadSpecial(JSONObject json){//maybe something more FileFormat-friendly?
        String type = json.getString("type");
        for(Special e : specials){
            if(e.getName().equals(type)){
                Special special = e.newInstance();
                special.load(json);
                return special;
            }
        }
        throw new IllegalArgumentException("Unknown special: "+type+"!");
    }
    protected abstract void load(JSONObject json);
    public abstract Special newInstance();
    public String getName(){
        return name;
    }
}