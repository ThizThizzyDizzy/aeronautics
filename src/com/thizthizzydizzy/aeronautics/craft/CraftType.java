package com.thizthizzydizzy.aeronautics.craft;
import com.thizthizzydizzy.aeronautics.craft.detector.CraftDetector;
import com.thizthizzydizzy.aeronautics.craft.engine.Engine;
import com.thizthizzydizzy.aeronautics.craft.sink_handler.SinkHandler;
import com.thizthizzydizzy.aeronautics.craft.special.Special;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class CraftType{
    private final String name;
    private String displayName;
    public final int minSize;
    public final int maxSize;
    public ArrayList<Engine> engines = new ArrayList<>();
    public ArrayList<Medium> mediums = new ArrayList<>();
    public ArrayList<Special> specials = new ArrayList<>();
    public HashSet<Material> allowedBlocks = new HashSet<>();
    public HashSet<Material> bannedBlocks = new HashSet<>();
//    public int sinkMoveTime = 10;
    public boolean hasConstructionMode;
    public int constructionTimeout;
    public int constructionPilots;
    public int constructionCrew;
    public boolean hasCombatMode;
    public int combatTimeout;
    public int combatPilots;
    public int combatCrew;
    public CraftDetector detector;
    public SinkHandler sinkHandler;
    public int onBoardThreshold = 2;
    public CraftType(String name, int minSize, int maxSize){
        this.name = name;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }
    public String getName(){
        return name;
    }
    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }
    public String getDisplayName(){
        return displayName==null?name:displayName;
    }
    public String checkValid(HashSet<Block> craft){
        if(craft.size()<minSize)return "Craft too small! ("+craft.size()+"<"+minSize+")";
        if(craft.size()>maxSize)return "Craft too large! ("+craft.size()+">"+maxSize+")";
        for(Engine engine : engines){
            String error = engine.checkValid(craft);
            if(error!=null)return error;
        }
        for(Special special : specials){
            String error = special.checkValid(craft);
            if(error!=null)return error;
        }
        return null;
    }
}