package com.thizthizzydizzy.aeronautics.craft.special;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
public abstract class Special{
    private static ArrayList<Special> specials = new ArrayList<>();
    public static void init(){
        specials.clear();
        specials.add(new FireChargeLifespan());
        specials.add(new FireChargeDirector());
        specials.add(new TNTDirector());
        specials.add(new BlockResistance());
        specials.add(new TNTTracer());
        specials.add(new SpillProtection());
        specials.add(new TNTImpactDetonation());
        specials.add(new MobSpawnProtection());
        specials.add(new PointDefenseCannon());
        specials.add(new DamageReport());
        specials.add(new Turbine());
        specials.add(new VerticalTurbine());
    }
    public static void createSigns(){
        for(Special s : specials){
            ArrayList<CraftSign> signs = new ArrayList<>();
            s.createSigns(signs);
            for(CraftSign sign : signs){
                CraftSign.addSign(sign);
            }
        }
    }
    private final String name;
    protected Special(String name){
        this.name = name;//TODO validate
    }
    public static Special loadSpecial(JSONObject json){//maybe something more FileFormat-friendly?
        String type = json.getString("type");
        for(Special e : specials){
            if(e.getName().equals(type)){
                Special special = e.newInstance();
                special.load(json);
                return special;
            }
        }
        throw new IllegalArgumentException("Unknown special: "+type+"!");
    }
    protected abstract void load(JSONObject json);
    public abstract void createSigns(ArrayList<CraftSign> signs);
    public abstract Special newInstance();
    public String getName(){
        return name;
    }
    public String checkValid(HashSet<Block> craft){
        return null;
    }
    public abstract void init(CraftSpecial special);
    public abstract void tick(CraftSpecial special);
    public abstract void event(CraftSpecial special, Event event);
    public abstract boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l);
    public abstract void updateHull(CraftSpecial special, int damage, boolean damaged);
    public abstract boolean addBlock(CraftSpecial special, Player player, Block block, boolean force);
    public ArrayList<Message> getMessages(CraftSpecial special){
        ArrayList<Message> messages = new ArrayList<>();
        getMessages(special, messages);
        return messages;
    }
    public abstract void getMessages(CraftSpecial special, ArrayList<Message> messages);
    public ArrayList<Multiblock> getMultiblockTypes(CraftSpecial special){
        ArrayList<Multiblock> types = new ArrayList<>();
        getMultiblockTypes(special, types);
        return types;
    }
    public abstract void getMultiblockTypes(CraftSpecial special, ArrayList<Multiblock> multiblockTypes);
}