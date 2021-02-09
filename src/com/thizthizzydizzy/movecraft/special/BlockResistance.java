package com.thizthizzydizzy.movecraft.special;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.craft.CraftSign;
import com.thizthizzydizzy.movecraft.craft.CraftSpecial;
import com.thizthizzydizzy.movecraft.craft.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
public class BlockResistance extends Special implements Listener{
    public HashMap<Material, Float> resistances = new HashMap<>();
    public BlockResistance(){
        super("movecraft:block_resistance");
    }
    @Override
    protected void load(JSON.JSONObject json){
        if(json.hasJSONArray("resistances")){
            JSON.JSONArray jsonResistances = json.getJSONArray("resistances");
            for(Object obj : jsonResistances){
                JSON.JSONObject jsonResistance = (JSON.JSONObject)obj;
                String block = jsonResistance.getString("block");
                float value = jsonResistance.getFloat("resistance");
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            for(Material m : tag.getValues()){
                                resistances.put(m, value);
                            }
                            break;
                        }
                    }
                }else{
                    resistances.put(Material.matchMaterial(block), value);
                }
            }
        }
    }
    @Override
    public Special newInstance(){
        return new BlockResistance();
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public void init(CraftSpecial special){}
    @Override
    public void tick(CraftSpecial special){}
    @Override
    public void event(CraftSpecial special, Event event){
        if(event instanceof BlockExplodeEvent){
            BlockExplodeEvent bee = (BlockExplodeEvent)event;
            if(resistances.containsKey(bee.getBlock().getType())){
                if(check(bee.getBlock()))bee.setCancelled(true);
            }
        }
        if(event instanceof EntityExplodeEvent){
            EntityExplodeEvent eee = (EntityExplodeEvent)event;
            for (Iterator<Block> it = eee.blockList().iterator(); it.hasNext();) {
                Block b = it.next();
                if(resistances.containsKey(b.getType())){
                    if(check(b))it.remove();
                }
            }
        }
    }
    private boolean check(Block b){
        Random rand = new Random(b.getX()+b.getY()+b.getZ()+(System.currentTimeMillis()>>12));
        return rand.nextFloat()<=resistances.get(b.getType());
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
    public Message getMessage(CraftSpecial special){
        return null;
    }
}