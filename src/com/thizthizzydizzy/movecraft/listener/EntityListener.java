package com.thizthizzydizzy.movecraft.listener;
import com.thizthizzydizzy.movecraft.Movecraft;
import com.thizthizzydizzy.movecraft.craft.Craft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
public class EntityListener implements Listener{
    private final Movecraft movecraft;
    public EntityListener(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event){
        Craft craft = movecraft.getCraft(event.getLocation());
        if(craft!=null)craft.event(event);
    }
}