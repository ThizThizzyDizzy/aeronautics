package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.CraftSpecial;
import com.thizthizzydizzy.movecraft.craft.Message;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
public class MobSpawnProtection extends Special implements Listener{
    public MobSpawnProtection(){
        super("movecraft:mob_spawn_protection");
    }
    @Override
    protected void load(JSON.JSONObject json){}
    @Override
    public Special newInstance(){
        return new MobSpawnProtection();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public void init(CraftSpecial special){}
    @Override
    public void tick(CraftSpecial special){}
    @Override
    public void event(CraftSpecial special, Event event){
        if(event instanceof EntitySpawnEvent){
            EntitySpawnEvent ese = (EntitySpawnEvent)event;
            switch(ese.getEntityType()){
                case BAT:
                case BEE:
                case BLAZE:
                case CAT:
                case CAVE_SPIDER:
                case CHICKEN:
                case COD:
                case COW:
                case CREEPER:
                case DOLPHIN:
                case DONKEY:
                case DROWNED:
                case ELDER_GUARDIAN:
                case ENDERMAN:
                case ENDERMITE:
                case ENDER_DRAGON:
                case EVOKER:
                case FOX:
                case GHAST:
                case GIANT:
                case GUARDIAN:
                case HOGLIN:
                case HORSE:
                case HUSK:
                case ILLUSIONER:
                case LLAMA:
                case MAGMA_CUBE:
                case MULE:
                case MUSHROOM_COW:
                case OCELOT:
                case PANDA:
                case PARROT:
                case PHANTOM:
                case PIG:
                case PIGLIN:
                case PIGLIN_BRUTE:
                case PILLAGER:
                case POLAR_BEAR:
                case PUFFERFISH:
                case RABBIT:
                case RAVAGER:
                case SALMON:
                case SHEEP:
                case SHULKER:
                case SILVERFISH:
                case SKELETON:
                case SKELETON_HORSE:
                case SLIME:
                case SPIDER:
                case SQUID:
                case STRAY:
                case STRIDER:
                case TRADER_LLAMA:
                case TROPICAL_FISH:
                case TURTLE:
                case VILLAGER:
                case VINDICATOR:
                case WANDERING_TRADER:
                case WITCH:
                case WITHER_SKELETON:
                case WOLF:
                case ZOGLIN:
                case ZOMBIE:
                case ZOMBIE_HORSE:
                case ZOMBIE_VILLAGER:
                case ZOMBIFIED_PIGLIN:
                    ese.setCancelled(true);
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
                case IRON_GOLEM:
                case ITEM_FRAME:
                case LEASH_HITCH:
                case LLAMA_SPIT:
                case LIGHTNING:
                case MINECART:
                case MINECART_CHEST:
                case MINECART_COMMAND:
                case MINECART_FURNACE:
                case MINECART_HOPPER:
                case MINECART_MOB_SPAWNER:
                case MINECART_TNT:
                case PAINTING:
                case PLAYER:
                case PRIMED_TNT:
                case SHULKER_BULLET:
                case SNOWMAN:
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
                    //allowed to spawn
            }
        }
    }
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special){}
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
    @Override
    public void getMessages(CraftSpecial special, ArrayList<Message> messages){}
}