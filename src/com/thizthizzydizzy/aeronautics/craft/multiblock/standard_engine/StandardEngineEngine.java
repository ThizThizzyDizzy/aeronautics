package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine;
import com.thizthizzydizzy.aeronautics.Direction;
public interface StandardEngineEngine{
    public double getThrottleMin(Direction dir);
    public double getThrottleMax(Direction dir);
    public double getMaxThrust(Direction dir);
    public void setThrottle(Direction dir, double throttle);
}