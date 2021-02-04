package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON;
public class FireChargeLifespan extends Special{
    private int time;
    public FireChargeLifespan(){
        super("movecraft:fire_charge_lifespan");
    }
    @Override
    protected void load(JSON.JSONObject json){
        time = json.getInt("time");
    }
    @Override
    public Special newInstance(){
        return new FireChargeLifespan();
    }
}