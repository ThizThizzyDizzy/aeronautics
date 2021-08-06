package com.thizthizzydizzy.aeronautics.craft.special;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.JSON.JSONArray;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.HorizontalTurbineMultiblock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
public class Turbine extends Special{
    public final HashSet<Material> rotorMaterials = new HashSet<>();
    public final HashSet<Material> outletMaterials = new HashSet<>();
    public final HashMap<HashSet<Material>, HashSet<Material>> bladeMaterials = new HashMap<>();
    public boolean mismatchedRotor = false;
    public double particleDensity = 0;
    public double particleSpeed = 1;
    public double particleDiameter = 5;
    public int warmupTime = 0;
    public int maxRotorLength;
    public int particlePower = 10;
    public Turbine(){
        super("aeronautics:turbine");
    }
    @Override
    protected void load(JSON.JSONObject json){
        if(json.hasDouble("particleDensity"))particleDensity = json.getDouble("particleDensity");
        if(json.hasDouble("particleSpeed"))particleSpeed = json.getDouble("particleSpeed");
        if(json.hasDouble("particleDiameter"))particleDiameter = json.getDouble("particleDiameter");
        if(json.hasInt("warmupTime"))warmupTime = json.getInt("warmupTime");
        if(json.hasInt("particlePower"))particlePower = json.getInt("particlePower");
        maxRotorLength = json.getInt("maxRotorLength");
        JSONArray rotorMaterials = json.getJSONArray("rotorMaterials");
        for(Object obj : rotorMaterials){
            String block = (String)obj;
            if(block.startsWith("#")){
                Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                for(Tag<Material> tag : tags){
                    if(tag.getKey().toString().equals(block.substring(1))){
                        this.rotorMaterials.addAll(tag.getValues());
                        break;
                    }
                }
            }else{
                this.rotorMaterials.add(Material.matchMaterial(block));
            }
        }
        if(json.hasBoolean("mismatchedRotor"))mismatchedRotor = json.getBoolean("mismatchedRotor");
        JSONArray outletMaterials = json.getJSONArray("outletMaterials");
        for(Object obj : outletMaterials){
            String block = (String)obj;
            if(block.startsWith("#")){
                Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                for(Tag<Material> tag : tags){
                    if(tag.getKey().toString().equals(block.substring(1))){
                        this.outletMaterials.addAll(tag.getValues());
                        break;
                    }
                }
            }else{
                this.outletMaterials.add(Material.matchMaterial(block));
            }
        }
        JSONArray bladeMaterials = json.getJSONArray("bladeMaterials");
        for(Object o : bladeMaterials){
            HashSet<Material> baseMaterials = new HashSet<>();
            HashSet<Material> tipMaterials = new HashSet<>();
            JSONArray blade = (JSONArray)o;
            JSONArray base = (JSONArray)blade.get(0);
            JSONArray tip = (JSONArray)blade.get(1);
            for(Object obj : base){
                String block = (String)obj;
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            baseMaterials.addAll(tag.getValues());
                            break;
                        }
                    }
                }else{
                    baseMaterials.add(Material.matchMaterial(block));
                }
            }
            for(Object obj : tip){
                String block = (String)obj;
                if(block.startsWith("#")){
                    Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                    for(Tag<Material> tag : tags){
                        if(tag.getKey().toString().equals(block.substring(1))){
                            tipMaterials.addAll(tag.getValues());
                            break;
                        }
                    }
                }else{
                    tipMaterials.add(Material.matchMaterial(block));
                }
            }
            this.bladeMaterials.put(baseMaterials, tipMaterials);
        }
    }
    @Override
    public void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Special newInstance(){
        return new Turbine();
    }
    @Override
    public void init(CraftSpecial special){}
    @Override
    public void tick(CraftSpecial special){}
    @Override
    public void event(CraftSpecial special, Event event){}
    @Override
    public boolean removeBlock(CraftSpecial special, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftSpecial special, int damage, boolean damaged){}
    @Override
    public boolean addBlock(CraftSpecial special, Player player, Block block, boolean force){
        return true;
    }
    @Override
    public void getMessages(CraftSpecial special, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftSpecial special, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new HorizontalTurbineMultiblock(special, this));
    }
}