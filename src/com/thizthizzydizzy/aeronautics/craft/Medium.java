package com.thizthizzydizzy.aeronautics.craft;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
public class Medium{
    public HashSet<Material> blocks = new HashSet<>();
    public HashMap<Integer, Double> densityMap = new HashMap<>();
    public double buoyancyMultiplier, dragMultiplier;
    public static Medium load(JSONObject obj){
        Medium medium = new Medium();
        if(obj.hasJSONArray("blocks"))for(Object o : obj.getJSONArray("blocks")){
            String s = (String)o;
            medium.blocks.addAll(Vanillify.getBlocks(s));
        }
        if(obj.hasJSONArray("density"))for(Object o : obj.getJSONArray("density")){
            JSONObject densityObj = (JSONObject)o;
            medium.densityMap.put(densityObj.getInt("y"), densityObj.getDouble("density"));
        }
        if(obj.hasDouble("buoyancy_multiplier"))medium.buoyancyMultiplier = obj.getDouble("buoyancy_multiplier");
        if(obj.hasDouble("drag_multiplier"))medium.dragMultiplier = obj.getDouble("drag_multiplier");
        return medium;
    }
    public double getDensity(int y){
        if(densityMap.containsKey(y))return densityMap.get(y);
        int lowerPos = 0, upperPos = 0;
        double lower = Double.NaN;
        double upper = Double.NaN;
        for(int i : densityMap.keySet()){
            if(i<y){
                if(i>lowerPos||Double.isNaN(lower)){
                    lowerPos = i;
                    lower = densityMap.get(i);
                }
            }
            if(i>y){
                if(i>upperPos||Double.isNaN(upper)){
                    upperPos = i;
                    upper = densityMap.get(i);
                }
            }
        }
        if(Double.isNaN(lower))return upper;//if both nan, return nan, already done by this
        if(Double.isNaN(upper))return lower;
        double percent = ((double)y-lowerPos)/((double)upperPos-lowerPos);
        return lower+percent*(upper-lower);
    }
}