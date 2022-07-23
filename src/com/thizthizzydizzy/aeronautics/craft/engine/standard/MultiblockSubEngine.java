package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.StandardEngineEngine;
import java.util.ArrayList;
import org.bukkit.util.Vector;
public abstract class MultiblockSubEngine extends SubEngine{
    public MultiblockSubEngine(String name){
        super(name);
    }
    public <T extends StandardEngineEngine, Multiblock> ArrayList<T> getMultiblocks(CraftEngine engine, StandardEngine standardEngine){
        ArrayList<T> multis = new ArrayList<>();
        ArrayList<com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock> types = new ArrayList<>();
        ArrayList<String> typeNames = new ArrayList<>();
        getMultiblockTypes(engine, standardEngine, types);
        for(var type : types){
            typeNames.add(type.name);
        }
        for(var multi : engine.getCraft().getMultiblocks()){
            if(multi instanceof StandardEngineEngine){
                if(typeNames.contains(multi.name))multis.add((T)multi);
            }
        }
        return multis;
    }
    @Override
    public double getMaxThrust(CraftEngine engine, StandardEngine standardEngine, Direction dir){
        double thrust = 0;
        for(var multi : getMultiblocks(engine, standardEngine)){
            thrust+=multi.getMaxThrust(dir);
        }
        return thrust;
    }
    @Override
    public void setThrottle(CraftEngine engine, StandardEngine standardEngine, Direction dir, double throttle){
        for(var multi : getMultiblocks(engine, standardEngine)){
            multi.setThrottle(dir, throttle);
        }
    }
    @Override
    public double getThrottleMin(CraftEngine engine, StandardEngine standardEngine, Direction dir){
        double throttle = Double.NaN;
        for(var multi : getMultiblocks(engine, standardEngine)){
            double t = multi.getThrottleMin(dir);
            if(Double.isNaN(throttle)||t<throttle)throttle = t;
        }
        return Double.isNaN(throttle)?0:throttle;
    }
    @Override
    public double getThrottleMax(CraftEngine engine, StandardEngine standardEngine, Direction dir){
        double throttle = Double.NaN;
        for(var multi : getMultiblocks(engine, standardEngine)){
            double t = multi.getThrottleMax(dir);
            if(Double.isNaN(throttle)||t>throttle)throttle = t;
        }
        return Double.isNaN(throttle)?0:throttle;
    }
    @Override
    public Vector getCurrentThrust(CraftEngine engine, StandardEngine standardEngine){
        Vector thrust = new Vector();
        for(var multi : getMultiblocks(engine, standardEngine)){
            thrust.add(multi.getCurrentThrust());
        }
        return thrust;
    }
}