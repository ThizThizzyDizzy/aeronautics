package com.thizthizzydizzy.movecraft.file;
import com.thizthizzydizzy.movecraft.JSON;
import com.thizthizzydizzy.movecraft.JSON.JSONArray;
import com.thizthizzydizzy.movecraft.JSON.JSONObject;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import com.thizthizzydizzy.movecraft.engine.Engine;
import com.thizthizzydizzy.movecraft.special.Special;
import java.io.File;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
public abstract class FileFormat{
    private static final ArrayList<FileFormat> formats = new ArrayList<>();
    static{
        formats.add(new FileFormat(){
            @Override
            public String getFileExtension(){
                return "json";
            }
            @Override
            public String getName(){
                return "JSON";
            }
            @Override
            public CraftType load(File f){//TODO validation?
                JSONObject json = JSON.parse(f);
                CraftType type = new CraftType(json.getString("name"), json.getInt("minSize"), json.getInt("maxSize"));
                if(json.hasString("displayName"))type.setDisplayName(json.getString("displayName"));
                if(json.hasJSONArray("engines")){
                    JSONArray engines = json.getJSONArray("engines");
                    for(Object obj : engines){
                        type.engines.add(Engine.loadEngine((JSONObject)obj));
                    }
                }
                if(json.hasJSONArray("specials")){
                    JSONArray specials = json.getJSONArray("specials");
                    for(Object obj : specials){
                        type.specials.add(Special.loadSpecial((JSONObject)obj));
                    }
                }
                if(json.hasJSONArray("allowedBlocks")){
                    JSONArray allowedBlocks = json.getJSONArray("allowedBlocks");
                    for(Object obj : allowedBlocks){
                        String block = (String)obj;
                        if(block.startsWith("#")){
                            Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                            for(Tag<Material> tag : tags){
                                if(tag.getKey().toString().equals(block.substring(1))){
                                    type.allowedBlocks.addAll(tag.getValues());
                                    break;
                                }
                            }
                        }else{
                            type.allowedBlocks.add(Material.matchMaterial(block));
                        }
                    }
                }else{
                    for(Material m : Material.values()){
                        if(m.isBlock()&&!m.isLegacy()){
                            type.allowedBlocks.add(m);
                        }
                    }
                }
                if(json.hasJSONArray("bannedBlocks")){
                    JSONArray bannedBlocks = json.getJSONArray("bannedBlocks");
                    for(Object obj : bannedBlocks){
                        String block = (String)obj;
                        if(block.startsWith("#")){
                            Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
                            for(Tag<Material> tag : tags){
                                if(tag.getKey().toString().equals(block.substring(1))){
                                    type.bannedBlocks.addAll(tag.getValues());
                                    break;
                                }
                            }
                        }else{
                            type.bannedBlocks.add(Material.matchMaterial(block));
                        }
                    }
                }
                if(json.hasInt("sinkMoveTime"))type.sinkMoveTime = json.getInt("sinkMoveTime");
                if(json.hasJSONObject("constructionMode")){
                    type.hasConstructionMode = true;
                    JSONObject construction = json.getJSONObject("constructionMode");
                    type.constructionTimeout = construction.getInt("timeout");
                    if(construction.hasInt("pilots"))type.constructionPilots = construction.getInt("pilots");
                    if(construction.hasInt("crew"))type.constructionCrew = construction.getInt("crew");
                }
                if(json.hasJSONObject("combatMode")){
                    type.hasCombatMode = true;
                    JSONObject combat = json.getJSONObject("combatMode");
                    type.combatTimeout = combat.getInt("timeout");
                    if(combat.hasInt("pilots"))type.combatPilots = combat.getInt("pilots");
                    if(combat.hasInt("crew"))type.combatCrew = combat.getInt("crew");
                }
                return type;
            }
        });
    }
    public static void addFileFormat(FileFormat format){
        for(FileFormat f : formats){
            if(f.getFileExtension().equalsIgnoreCase(format.getFileExtension())){
                throw new IllegalArgumentException("Cannot add file format "+format.getName()+"! Extension "+f.getFileExtension()+" is already used by "+f.getName());
            }
        }
        formats.add(format);
    }
    public static ArrayList<FileFormat> getFileFormats(){
        return new ArrayList<>(formats);//so you can't remove them (:
    }
    public abstract String getFileExtension();
    public abstract String getName();
    public abstract CraftType load(File f);
}