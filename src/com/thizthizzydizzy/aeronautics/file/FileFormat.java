package com.thizthizzydizzy.aeronautics.file;
import com.thizthizzydizzy.aeronautics.Aeronautics;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.JSON.JSONArray;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.craft.CraftType;
import com.thizthizzydizzy.aeronautics.craft.Medium;
import com.thizthizzydizzy.aeronautics.craft.collision_handler.CollisionHandler;
import com.thizthizzydizzy.aeronautics.craft.detector.CraftDetector;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.sink_handler.SinkHandler;
import com.thizthizzydizzy.aeronautics.craft.special.Special;
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
            public CraftType load(Aeronautics aeronautics, File f){//TODO validation?
                JSONObject json = JSON.parse(f);
                CraftType type = new CraftType(json.getString("name"), json.getInt("min_size"), json.getInt("max_size"));
                if(json.hasInt("on_board_threshold"))type.onBoardThreshold = json.getInt("on_board_threshold");
                if(json.hasString("display_name"))type.setDisplayName(json.getString("display_name"));
                if(json.hasJSONArray("engines")){
                    JSONArray engines = json.getJSONArray("engines");
                    for(Object obj : engines){
                        type.engines.add(Engine.loadEngine((JSONObject)obj));
                    }
                }
                JSONArray mediumsArray = json.getJSONArray("mediums");
                for(Object o : mediumsArray){
                    type.mediums.add(Medium.load((JSONObject)o));
                }
                JSONObject detectorObj = json.getJSONObject("detector");
                String detectorName = detectorObj.getString("name");
                for(CraftDetector detector : aeronautics.detectors){
                    if(detector.getName().equals(detectorName))type.detector = detector.newInstance();
                }
                if(type.detector==null){
                    throw new IllegalArgumentException("Invalid craft detector: "+detectorName+"!");
                }
                type.detector.load(detectorObj);
                JSONObject collisionHandlerObj = json.getJSONObject("collision_handler");
                String collisionHandlerName = collisionHandlerObj.getString("name");
                for(CollisionHandler collisionHandler : aeronautics.collisionHandlers){
                    if(collisionHandler.getName().equals(collisionHandlerName))type.collisionHandler = collisionHandler.newInstance();
                }
                if(type.collisionHandler==null){
                    throw new IllegalArgumentException("Invalid collision handler: "+collisionHandlerName+"!");
                }
                type.collisionHandler.load(collisionHandlerObj);
                JSONObject handlerObj = json.getJSONObject("sink_handler");
                String handlerName = handlerObj.getString("name");
                for(SinkHandler handler : aeronautics.sinkHandlers){
                    if(handler.getName().equals(handlerName))type.sinkHandler = handler.newInstance();
                }
                if(type.sinkHandler==null){
                    throw new IllegalArgumentException("Invalid sink handler: "+handlerName+"!");
                }
                type.sinkHandler.load(handlerObj);
                if(json.hasJSONArray("specials")){
                    JSONArray specials = json.getJSONArray("specials");
                    for(Object obj : specials){
                        type.specials.add(Special.loadSpecial((JSONObject)obj));
                    }
                }
                if(json.hasJSONArray("allowed_blocks")){
                    JSONArray allowedBlocks = json.getJSONArray("allowed_blocks");
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
                if(json.hasJSONArray("banned_blocks")){
                    JSONArray bannedBlocks = json.getJSONArray("banned_blocks");
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
                if(json.hasJSONObject("construction_mode")){
                    type.hasConstructionMode = true;
                    JSONObject construction = json.getJSONObject("construction_mode");
                    type.constructionTimeout = construction.getInt("timeout");
                    if(construction.hasInt("pilots"))type.constructionPilots = construction.getInt("pilots");
                    if(construction.hasInt("crew"))type.constructionCrew = construction.getInt("crew");
                }
                if(json.hasJSONObject("combat_mode")){
                    type.hasCombatMode = true;
                    JSONObject combat = json.getJSONObject("combat_mode");
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
    public abstract CraftType load(Aeronautics aeronautics, File f);
}