package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine;
public interface PowerSupplier extends PowerUser{
    /**
     * @return the amount of power currently available to be generated this tick
     */
    public int getAvailablePower();
    /**
     * Called when something requires an amount of power
     * @param power the amount of power requested
     * @return the amount of power that is actually produced
     */
    public int generate(int power);
}
