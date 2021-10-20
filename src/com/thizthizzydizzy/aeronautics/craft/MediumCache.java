package com.thizthizzydizzy.aeronautics.craft;
import java.util.HashMap;
public class MediumCache{
    public HashMap<Medium, Double> mediumWeights = new HashMap<>();
    public double density;
    public double buoyancy;
    public double drag;
    public int total = 0;
    public int shipVolume;
    public void addBlock(Medium m, int y){
        mediumWeights.put(m, mediumWeights.getOrDefault(m, 0d)+1);
        double d = m.getDensity(y);
        density+=m.getDensity(y);
        buoyancy+=m.buoyancyMultiplier*d;
        drag+=m.dragMultiplier*d;
        total++;
    }
    public MediumCache calculate(){
        double total = 0;
        for(double d : mediumWeights.values())total+=d;
        for(Medium key : mediumWeights.keySet())mediumWeights.put(key, mediumWeights.get(key)/total);
        if(total>0){
            density/=total;
            drag/=total;
            buoyancy/=total;
        }
        total = 0;
        return this;
    }
}