package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Craft;
import com.thizthizzydizzy.movecraft.Movecraft;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
public class MobSpawn implements Listener{
    private final Movecraft movecraft;
    public MobSpawn(Movecraft movecraft){
        this.movecraft = movecraft;
    }
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event){
        switch(event.getEntityType()){
            case AREA_EFFECT_CLOUD:
            case ARMOR_STAND:
            case ARROW:
            case BOAT:
            case DRAGON_FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case ENDER_CRYSTAL:
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case EVOKER_FANGS:
            case EXPERIENCE_ORB:
            case FALLING_BLOCK:
            case FIREBALL:
            case FIREWORK:
            case FISHING_HOOK:
            case ITEM_FRAME:
            case LEASH_HITCH:
            case LLAMA_SPIT:
            case LIGHTNING:
            case MINECART:
            case MINECART_TNT:
            case MINECART_CHEST:
            case MINECART_HOPPER:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_MOB_SPAWNER:
            case PAINTING:
            case PLAYER:
            case SHULKER_BULLET:
            case WITHER_SKULL:
            case UNKNOWN:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case TRIDENT:
            case VEX:
            case WITHER:
                return;
            case SMALL_FIREBALL:
            case PRIMED_TNT://this won't work properly if two different crafts have their weapons butted up against each other...  but when is that gonna happen?
                Craft craft = movecraft.getCraft(event.getLocation());
                if(craft!=null){
                    craft.newRound(event.getEntity());
                }
                return;
        }
        if(movecraft.getCraft(event.getLocation())!=null)event.setCancelled(true);
    }
}