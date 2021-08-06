package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine;
public interface PowerConsumer extends PowerUser{
    /**
     * Get the amount of power requested by this block this tick.
     * @return the amount of power currently requested by this block.
     */
    public int getDemand();
    /**
     * Called when this block is provided with power.
     * @param power the amount of power provided. May not be more than is
     * returned by <code>getDemand()</code>
     */
    public void consume(int power);
}
