package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON;
public class FireChargeDirector extends Special{
    private String displayName;
    private float range;
    private float angle;
    public FireChargeDirector(){
        super("movecraft:fire_charge_director");
    }
    @Override
    protected void load(JSON.JSONObject json){
        displayName = json.getString("name");
        range = json.getFloat("range");
        angle = json.getFloat("angle");
    }
    @Override
    public Special newInstance(){
        return new FireChargeDirector();
    }
}