package com.thizthizzydizzy.movecraft.event;
import com.thizthizzydizzy.movecraft.Movecraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
public class MobSpawn implements Listener{
    private final Movecraft movecraft;
    //TODO rename class, this also handles right-clicking with a stick
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
            case PRIMED_TNT:
            case SHULKER_BULLET:
            case WITHER_SKULL:
            case UNKNOWN:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case TRIDENT:
            case VEX:
            case WITHER:
                return;
        }
        if(movecraft.getCraft(event.getLocation())!=null)event.setCancelled(true);
    }
}