package com.thizthizzydizzy.movecraft.craft.special;
import com.thizthizzydizzy.movecraft.JSON;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
public class BlockResistance extends Special{
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
}