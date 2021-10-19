package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.craft.BlockCache;
public class StandardEngineBlockCache extends BlockCache{
    public double comX, comY, comZ;
    public long mass;
    private final StandardEngine standardEngine;
    public StandardEngineBlockCache(StandardEngine standardEngine){
        this.standardEngine = standardEngine;
    }
    public void calcCenters(){
        super.calcCOV();
        double x = 0;
        double y = 0;
        double z = 0;
        long total = 0;
        for(var blox : blocks.values()){
            for(var blx : blox.values()){
                for(var b : blx.values()){
                    x+=b.x;
                    y+=b.y;
                    z+=b.z;
                    total+=standardEngine.getMass(b.material);
                }
            }
        }
        mass = total;
        comX = x/total;
        comY = y/total;
        comZ = z/total;
    }
}