package com.thizthizzydizzy.movecraft.craft.engine;
import com.thizthizzydizzy.movecraft.JSON.JSONArray;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
public class AirshipEngine extends Engine{
    public HashMap<Material, Integer> fuels = new HashMap<>();
    public HashMap<Material, Float> liftBlocks = new HashMap<>();
    public HashMap<Material, Float> engineBlocks = new HashMap<>();
    private Integer moveTime;
    private Integer horizMoveDistance;
    private Integer vertMoveDistance;
    public AirshipEngine(){
        super("movecraft:airship");
    }
    @Override
    protected void load(JSONObject json){
        if(json.hasJSONArray("fuels")){
            JSONArray jsonFuels = json.getJSONArray("fuels");
            for(Object obj : jsonFuels){
                JSONObject jsonFuel = (JSONObject)obj;
                String item = jsonFuel.getString("item");
                int value = jsonFuel.getInt("value");
                if(item.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_ITEMS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(item.substring(1))){
                            for(Material m : tag.getValues()){
                                fuels.put(m, value);
                            }
                            break;
                        }
                    }
                }else{
                    fuels.put(Material.matchMaterial(item), value);
                }
            }
        }
        moveTime = json.getInt("moveTime");
        horizMoveDistance = json.getInt("horizMoveDistance");
        vertMoveDistance = json.getInt("vertMoveDistance");
        if(json.hasJSONArray("lift")){
            JSONArray jsonLiftBlocks = json.getJSONArray("lift");
            for(Object obj : jsonLiftBlocks){
                JSONObject jsonLiftBlock = (JSONObject)obj;
                String block = jsonLiftBlock.getString("block");
                float percent = jsonLiftBlock.getFloat("required");
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            for(Material m : tag.getValues()){
                                liftBlocks.put(m, percent);
                            }
                            break;
                        }
                    }
                }else{
                    liftBlocks.put(Material.matchMaterial(block), percent);
                }
            }
        }
        if(json.hasJSONArray("engine")){
            JSONArray jsonEngineBlocks = json.getJSONArray("engine");
            for(Object obj : jsonEngineBlocks){
                JSONObject jsonEngineBlock = (JSONObject)obj;
                String block = jsonEngineBlock.getString("block");
                float percent = jsonEngineBlock.getFloat("required");
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            for(Material m : tag.getValues()){
                                engineBlocks.put(m, percent);
                            }
                            break;
                        }
                    }
                }else{
                    engineBlocks.put(Material.matchMaterial(block), percent);
                }
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public Engine newInstance(){
        return new AirshipEngine();
    }
}