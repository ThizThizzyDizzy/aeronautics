package com.thizthizzydizzy.aeronautics;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.EnergyDistributionSystem;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.Generator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.SubEngine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public class StandardEngineInitializationEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private final StandardEngine engine;
    public StandardEngineInitializationEvent(StandardEngine engine){
        this.engine = engine;
    }
    @Override
    public HandlerList getHandlers(){
        return handlers;
    }
    public static HandlerList getHandlerList(){
        return handlers;
    }
    public void registerEnergyDistributionSystem(EnergyDistributionSystem eds){
        EnergyDistributionSystem.energyDistributionSystems.add(eds);
    }
    public void registerGenerator(Generator gen){
        Generator.generators.add(gen);
    }
    public void registerEngine(SubEngine engine){
        SubEngine.engines.add(engine);
    }
}