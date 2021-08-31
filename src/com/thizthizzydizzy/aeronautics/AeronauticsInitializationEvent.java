package com.thizthizzydizzy.aeronautics;
import com.thizthizzydizzy.aeronautics.craft.collision_handler.CollisionHandler;
import com.thizthizzydizzy.aeronautics.craft.detector.CraftDetector;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.sink_handler.SinkHandler;
import com.thizthizzydizzy.aeronautics.craft.special.Special;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
public class AeronauticsInitializationEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private final Aeronautics plugin;
    public AeronauticsInitializationEvent(Aeronautics plugin){
        this.plugin = plugin;
    }
    public Aeronautics getPlugin(){
        return plugin;
    }
    @Override
    public HandlerList getHandlers(){
        return handlers;
    }
    public static HandlerList getHandlerList(){
        return handlers;
    }
    public void registerCraftDetector(CraftDetector detector){
        plugin.registerCraftDetector(detector);
    }
    public void registerSinkHandler(SinkHandler handler){
        plugin.registerSinkHandler(handler);
    }
    public void registerCollisionHandler(CollisionHandler handler){
        plugin.registerCollisionHandler(handler);
    }
    public void registerEngine(Engine engine){
        plugin.registerEngine(engine);
    }
    public void registerSpecial(Special special){
        plugin.registerSpecial(special);
    }
}