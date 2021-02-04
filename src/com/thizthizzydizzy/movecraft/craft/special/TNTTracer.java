package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import org.bukkit.Material;
public class TNTTracer extends Special{
    private Material tracerBlock;
    private int tracerTime;
    private Material explosionBlock;
    private int explosionTime;
    private int interval;
    private float velocityThreshold;
    public TNTTracer(){
        super("movecraft:tnt_tracer");
    }
    @Override
    protected void load(JSON.JSONObject json){
        if(json.hasJSONObject("tracer")){
            JSONObject jsonTrail = json.getJSONObject("tracer");
            tracerBlock = Material.matchMaterial(jsonTrail.getString("block"));
            tracerTime = jsonTrail.getInt("time");
        }
        if(json.hasJSONObject("explosion")){
            JSONObject jsonTrail = json.getJSONObject("explosion");
            explosionBlock = Material.matchMaterial(jsonTrail.getString("block"));
            explosionTime = jsonTrail.getInt("time");
        }
        interval = json.getInt("interval");
        velocityThreshold = json.getFloat("velocityThreshold");
    }
    @Override
    public Special newInstance(){
        return new TNTTracer();
    }
}