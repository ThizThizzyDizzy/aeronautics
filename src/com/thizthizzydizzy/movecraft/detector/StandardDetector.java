package com.thizthizzydizzy.movecraft.detector;
import com.thizthizzydizzy.movecraft.Movecraft;
import com.thizthizzydizzy.movecraft.craft.Craft;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
public class StandardDetector extends CraftDetector{
    private final Movecraft movecraft;
    public StandardDetector(Movecraft movecraft){
        super("movecraft:standard");
        this.movecraft = movecraft;
    }
    @Override
    public Craft detect(CraftType type, Player player, Block origin){
        HashSet<Block> craft = getBlocks(type.allowedBlocks, type.bannedBlocks, origin, type.maxSize+10);
        String error = type.checkValid(craft);
        if(error!=null&&player!=null){
            player.sendMessage(error);
            return null;
        }
        return new Craft(movecraft, origin.getWorld(), type, craft);
    }
    private HashSet<Block> getBlocks(HashSet<Material> allowed, HashSet<Material> banned, Block origin, int limit){
        //layer zero
        HashSet<Block> results = new HashSet<>();
        HashMap<Integer, ArrayList<Block>> layers = new HashMap<>();
        ArrayList<Block> zero = new ArrayList<>();
        int total = 0;
        if(isAllowed(origin.getType(), allowed, banned)){
            zero.add(origin);
            total++;
        }
        layers.put(0, zero);
        results.addAll(zero);
        //all the other layers
        int i = 0;
        while(true){
            ArrayList<Block> layer = new ArrayList<>();
            ArrayList<Block> lastLayer = new ArrayList<>(layers.get(i));
            if(i==0&&lastLayer.isEmpty()){
                lastLayer.add(origin);
            }
            for(Block block : lastLayer){
                for(int x = -1; x<=1; x++){
                    for(int y = -1; y<=1; y++){
                        for(int z = -1; z<=1; z++){
                            if(x==0&&y==0&&z==0)continue;//same block
                            Block newBlock = block.getRelative(x,y,z);
                            if(!isAllowed(newBlock.getType(), allowed, banned))continue;
                            if(lastLayer.contains(newBlock))continue;//if the new block is on the same layer, ignore
                            if(i>0&&layers.get(i-1).contains(newBlock))continue;//if the new block is on the previous layer, ignore
                            if(layer.contains(newBlock))continue;//if the new block is on the next layer, but already processed, ignore
                            layer.add(newBlock);
                            total++;
                        }
                    }
                }
            }
            layers.put(i+1, layer);
            results.addAll(layer);
            if(layer.isEmpty()||total>=limit)break;
            i++;
        }
        return results;
    }
    private boolean isAllowed(Material type, HashSet<Material> allowed, HashSet<Material> banned){
        return allowed.contains(type)&&!banned.contains(type);
    }
}