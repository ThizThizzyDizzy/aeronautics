package com.thizthizzydizzy.aeronautics.craft;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
public class BlockCache{
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, CachedBlock>>> blocks = new HashMap<>();
    public int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
    public double covX, covY, covZ;
    public void add(Block b){
        cache(new CachedBlock(b.getX(), b.getY(), b.getZ(), b.getType()));
        if(b.getX()<minX||blocks.size()==1)minX = b.getX();
        if(b.getY()<minY||blocks.size()==1)minY = b.getY();
        if(b.getZ()<minZ||blocks.size()==1)minZ = b.getZ();

        if(b.getX()>maxX||blocks.size()==1)maxX = b.getX();
        if(b.getY()>maxY||blocks.size()==1)maxY = b.getY();
        if(b.getZ()>maxZ||blocks.size()==1)maxZ = b.getZ();
    }
    public boolean isSolid(double x, double y, double z){
        CachedBlock b = blocks.getOrDefault((int)Math.floor(x), new HashMap<>()).getOrDefault((int)Math.floor(y), new HashMap<>()).get((int)Math.floor(z));
        return b!=null&&b.isSolid(x-Math.floor(x), y-Math.floor(y), z-Math.floor(z));
    }
    private void cache(CachedBlock b){
        if(blocks.containsKey(b.x)){
            HashMap<Integer, HashMap<Integer, CachedBlock>> blox = blocks.get(b.x);
            if(blox.containsKey(b.y)){
                HashMap<Integer, CachedBlock> blx = blox.get(b.y);
                blx.put(b.z, b);
            }else{
                HashMap<Integer, CachedBlock> blx = new HashMap<>();
                blx.put(b.z, b);
                blox.put(b.y, blx);
            }
        }else{
            HashMap<Integer, HashMap<Integer, CachedBlock>> blox = new HashMap<>();
            HashMap<Integer, CachedBlock> blx = new HashMap<>();
            blx.put(b.z, b);
            blox.put(b.y, blx);
            blocks.put(b.x, blox);
        }
    }
    public void calcCOV(){
        double x = 0;
        double y = 0;
        double z = 0;
        int total = 0;
        for(var blox : blocks.values()){
            for(var blx : blox.values()){
                for(var b : blx.values()){
                    x+=b.x;
                    y+=b.y;
                    z+=b.z;
                    total++;
                }
            }
        }
        covX = x/total;
        covY = y/total;
        covZ = z/total;
    }
    public static class CachedBlock{
        public final int x;
        public final int y;
        public final int z;
        public final Material material;
        public CachedBlock(int x, int y, int z, Material material){
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }
        private boolean isSolid(double xOff, double yOff, double zOff){
            return true;//TODO handle slabs, stairs, passable blocks, etc.
        }
    }
}