package com.thizthizzydizzy.aeronautics.craft.sink_handler;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.Craft;
public abstract class SinkHandler{
    private final String name;
    public SinkHandler(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public abstract SinkHandler newInstance();
    public abstract void load(JSON.JSONObject json);
    public abstract void onStartSinking(Craft craft);
    public abstract void tick(Craft craft);
}