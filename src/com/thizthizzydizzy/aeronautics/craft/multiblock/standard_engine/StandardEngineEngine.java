package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine;
public interface StandardEngineEngine{
    public double getThrottleMin();
    public double getThrottleMax();
    public double getMaxThrust();
    public void setThrottle(double throttle);
}