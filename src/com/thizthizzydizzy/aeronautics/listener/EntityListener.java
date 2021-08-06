package com.thizthizzydizzy.aeronautics.listener;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
public class EntityListener implements Listener{
    private final Aeronautics aeronautics;
    public EntityListener(Aeronautics aeronautics){
        this.aeronautics = aeronautics;
    }
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event){
        Craft craft = aeronautics.getCraft(event.getLocation());
        if(craft!=null)craft.event(event);
    }
}