package com.thizthizzydizzy.aeronautics.craft.multiblock;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.special.LegacyTurbine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Stairs;
public class HorizontalTurbineMultiblock extends Multiblock{
    private Direction facing;//the direction the turbine is facing (the opposite direction as thrust would be given)
    private final CraftSpecial special;
    private final LegacyTurbine turbine;
    private HashMap<Integer, Integer> blades = new HashMap<>();
    private int warmupProgress = 0;
    private int bladeDelay = 20;
    private int bladeTimer = 20;
    private Random rand = new Random();
    public HorizontalTurbineMultiblock(CraftSpecial special, LegacyTurbine turbine){
        this(special, turbine, null, null, null);
    }
    private HorizontalTurbineMultiblock(CraftSpecial special, LegacyTurbine turbine, Craft craft, Block origin, Direction facing){
        super("aeronautics:turbine", craft, origin);
        this.facing = facing;
        this.special = special;
        this.turbine = turbine;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        Direction dir = scan(craft, origin);
        if(dir==null)return null;
        HorizontalTurbineMultiblock mb = new HorizontalTurbineMultiblock(special, turbine, craft, origin, dir);
        mb.blades = this.blades;
        this.blades = new HashMap<>();
        return mb;
    }
    private Direction scan(Craft craft, Block origin){
        if(!turbine.outletMaterials.contains(origin.getType()))return null;//not the outlet!
        Direction dir = null;
        for(Direction d : Direction.LATERAL){
            if(turbine.rotorMaterials.contains(origin.getRelative(d.x,d.y,d.z).getType())){
                if(dir!=null)return null;//that has two rotors! not allowed!
                else dir = d;
            }
        }
        if(dir==null)return null;//that has no rotors!
        craft.aeronautics.debug(craft.getCrew(), "_A");
        HashSet<Block> theBlocks = new HashSet<>();
        ArrayList<Block> theRotor = new ArrayList<>();
        theBlocks.add(origin);
        int rotorLength = 0;
        for(int i = 1; i<=turbine.maxRotorLength; i++){
            Block b = origin.getRelative(dir.x*i,dir.y*i,dir.z*i);
            if(turbine.rotorMaterials.contains(b.getType())){
                BlockData data = b.getBlockData();
                if(data instanceof Orientable){
                    if(dir.matches(((Orientable)data).getAxis())){
                        theRotor.add(b);
                        rotorLength++;
                    }else break;
                }else break;
            }else break;
        }
        craft.aeronautics.debug(craft.getCrew(), "_B");
        if(rotorLength==0)return null;//that's too short!
        craft.aeronautics.debug(craft.getCrew(), "_C");
        theBlocks.addAll(theRotor);
        Block otherEnd = origin.getRelative(dir.x*(rotorLength+1),dir.y*(rotorLength+1),dir.z*(rotorLength+1));
        if(turbine.outletMaterials.contains(otherEnd.getType()))return null;//that's another outlet!
        craft.aeronautics.debug(craft.getCrew(), "_D");
        blades.clear();
        for(int i = 0; i<theRotor.size(); i++){
            detectBlade(craft, origin, i+1, dir);
        }
        craft.aeronautics.debug(craft.getCrew(), "_E"+theRotor.size()+" "+blades.size());
        if(blades.isEmpty())return null;
        if(!craft.contains(theBlocks))return null;
        craft.aeronautics.debug(craft.getCrew(), "_F");
        return dir;
    }
    private void detectBlade(Craft craft, Block origin, int pos, Direction dir){
        detectClockwiseBlade1(craft, origin, pos, dir);
        detectClockwiseBlade2(craft, origin, pos, dir);
        detectClockwiseBlade3(craft, origin, pos, dir);
        detectCounterClockwiseBlade1(craft, origin, pos, dir);
        detectCounterClockwiseBlade2(craft, origin, pos, dir);
        detectCounterClockwiseBlade3(craft, origin, pos, dir);
        if(blades.containsKey(pos))craft.aeronautics.debug(craft.getCrew(), "Found blade: "+blades.get(pos)+" at "+pos);
    }
    private void detectClockwiseBlade3(Craft craft, Block origin, int pos, Direction dir){
        craft.aeronautics.debug(craft.getCrew(), "ACW3");
        Block axle = origin.getRelative(dir.x*pos,dir.y*pos,dir.z*pos);
        Direction left = dir.getLeft();
        Direction right = dir.getRight();
        if(!isAir(axle.getRelative(0,1,0)))return;
        if(!isAir(axle.getRelative(0,2,0)))return;
        if(!isAir(axle.getRelative(left.x,2,left.z)))return;
        if(!isAir(axle.getRelative(0,-1,0)))return;
        if(!isAir(axle.getRelative(0,-2,0)))return;
        if(!isAir(axle.getRelative(right.x,-2,right.z)))return;
        if(!isAir(axle.getRelative(left.x,0,left.z)))return;
        if(!isAir(axle.getRelative(left.x*2,0,left.z*2)))return;
        if(!isAir(axle.getRelative(left.x*2,-1,left.z*2)))return;
        if(!isAir(axle.getRelative(right.x,0,right.z)))return;
        if(!isAir(axle.getRelative(right.x*2,0,right.z*2)))return;
        if(!isAir(axle.getRelative(right.x*2,1,right.z*2)))return;
        craft.aeronautics.debug(craft.getCrew(), "B");
        HashSet<Block> axleBases = new HashSet<>();
        axleBases.add(axle.getRelative(left.x,1,left.z));
        axleBases.add(axle.getRelative(right.x,1,right.z));
        axleBases.add(axle.getRelative(left.x,-1,left.z));
        axleBases.add(axle.getRelative(right.x,-1,right.z));
        HashSet<Block> axleTips = new HashSet<>();
        axleTips.add(axle.getRelative(right.x,2,right.z));
        axleTips.add(axle.getRelative(right.x*2,-1,right.z*2));
        axleTips.add(axle.getRelative(left.x,-2,left.z));
        axleTips.add(axle.getRelative(left.x*2,1,left.z*2));
        craft.aeronautics.debug(craft.getCrew(), "C");
        boolean found = false;
        BASE:for(HashSet<Material> base : turbine.bladeMaterials.keySet()){
            for(Block b : axleBases)if(!base.contains(b.getType()))continue BASE;
            HashSet<Material> tip = turbine.bladeMaterials.get(base);
            for(Block b : axleTips)if(!tip.contains(b.getType()))continue BASE;
            found = true;
            break;
        }
        if(!found)return;
        craft.aeronautics.debug(craft.getCrew(), "D");
        for(Block b : axleTips){
            BlockData data = b.getBlockData();
            if(data instanceof Stairs){
                Stairs s = (Stairs)data;
                if(s.getShape()!=Stairs.Shape.STRAIGHT){
                    craft.aeronautics.debug(craft.getCrew(), "DE2_"+b.getX()+" "+b.getY()+" "+b.getZ());
                    return;
                }
            }else{
                craft.aeronautics.debug(craft.getCrew(), "DE1_"+b.getX()+" "+b.getY()+" "+b.getZ());
                return;
            }
        }
        craft.aeronautics.debug(craft.getCrew(), "E");
        Stairs st = (Stairs)axle.getRelative(right.x,2,right.z).getBlockData();
        if(!left.matches(st.getFacing()))return;
        if(st.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sr = (Stairs)axle.getRelative(right.x*2,-1,right.z*2).getBlockData();
        if(!left.matches(sr.getFacing()))return;
        if(sr.getHalf()!=Bisected.Half.TOP)return;
        Stairs sb = (Stairs)axle.getRelative(left.x,-2,left.z).getBlockData();
        if(!right.matches(sb.getFacing()))return;
        if(sb.getHalf()!=Bisected.Half.TOP)return;
        Stairs sl = (Stairs)axle.getRelative(left.x*2,1,left.z*2).getBlockData();
        if(!right.matches(sl.getFacing()))return;
        if(sl.getHalf()!=Bisected.Half.BOTTOM)return;
        craft.aeronautics.debug(craft.getCrew(), "F");
        if(!craft.contains(axleTips))return;
        if(!craft.contains(axleBases))return;
        craft.aeronautics.debug(craft.getCrew(), "G");
        blades.put(pos, 3);
    }
    private void detectClockwiseBlade2(Craft craft, Block origin, int pos, Direction dir){
        craft.aeronautics.debug(craft.getCrew(), "ACW2");
        Block axle = origin.getRelative(dir.x*pos,dir.y*pos,dir.z*pos);
        Direction left = dir.getLeft();
        Direction right = dir.getRight();
        if(!isAir(axle.getRelative(left.x,1,left.z)))return;
        if(!isAir(axle.getRelative(left.x,2,left.z)))return;
        if(!isAir(axle.getRelative(left.x*2,1,left.z*2)))return;
        if(!isAir(axle.getRelative(left.x,-1,left.z)))return;
        if(!isAir(axle.getRelative(left.x,-2,left.z)))return;
        if(!isAir(axle.getRelative(left.x*-2,1,left.z*2)))return;
        if(!isAir(axle.getRelative(right.x,1,right.z)))return;
        if(!isAir(axle.getRelative(right.x,2,right.z)))return;
        if(!isAir(axle.getRelative(right.x*2,1,right.z*2)))return;
        if(!isAir(axle.getRelative(right.x,-1,right.z)))return;
        if(!isAir(axle.getRelative(right.x,-2,right.z)))return;
        if(!isAir(axle.getRelative(right.x*-2,1,right.z*2)))return;
        craft.aeronautics.debug(craft.getCrew(), "B");
        HashSet<Block> axleBases = new HashSet<>();
        axleBases.add(axle.getRelative(left.x,0,left.z));
        axleBases.add(axle.getRelative(right.x,0,right.z));
        axleBases.add(axle.getRelative(0,-1,0));
        axleBases.add(axle.getRelative(0,1,0));
        HashSet<Block> axleTips = new HashSet<>();
        axleTips.add(axle.getRelative(left.x*2,0,left.z*2));
        axleTips.add(axle.getRelative(right.x*2,0,right.z*2));
        axleTips.add(axle.getRelative(0,-2,0));
        axleTips.add(axle.getRelative(0,2,0));
        craft.aeronautics.debug(craft.getCrew(), "C");
        boolean found = false;
        for(HashSet<Material> base : turbine.bladeMaterials.keySet()){
            for(Block b : axleBases)if(!base.contains(b.getType()))continue;
            HashSet<Material> tip = turbine.bladeMaterials.get(base);
            for(Block b : axleTips)if(!tip.contains(b.getType()))continue;
            found = true;
            break;
        }
        if(!found)return;
        craft.aeronautics.debug(craft.getCrew(), "D");
        for(Block b : axleTips){
            BlockData data = b.getBlockData();
            if(data instanceof Stairs){
                Stairs s = (Stairs)data;
                if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
            }else return;
        }
        craft.aeronautics.debug(craft.getCrew(), "E");
        Stairs st = (Stairs)axle.getRelative(0,2,0).getBlockData();
        if(!left.matches(st.getFacing()))return;
        if(st.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sr = (Stairs)axle.getRelative(right.x*2,0,right.z*2).getBlockData();
        if(!left.matches(sr.getFacing()))return;
        if(sr.getHalf()!=Bisected.Half.TOP)return;
        Stairs sb = (Stairs)axle.getRelative(0,-2,0).getBlockData();
        if(!right.matches(sb.getFacing()))return;
        if(sb.getHalf()!=Bisected.Half.TOP)return;
        Stairs sl = (Stairs)axle.getRelative(left.x*2,0,left.z*2).getBlockData();
        if(!right.matches(sl.getFacing()))return;
        if(sl.getHalf()!=Bisected.Half.BOTTOM)return;
        craft.aeronautics.debug(craft.getCrew(), "F");
        if(!craft.contains(axleTips))return;
        if(!craft.contains(axleBases))return;
        craft.aeronautics.debug(craft.getCrew(), "G");
        blades.put(pos, 2);
    }
    private void detectClockwiseBlade1(Craft craft, Block origin, int pos, Direction dir){
        craft.aeronautics.debug(craft.getCrew(), "ACW1");
        Block axle = origin.getRelative(dir.x*pos,dir.y*pos,dir.z*pos);
        Direction right = dir.getRight();
        Direction left = dir.getLeft();
        if(!isAir(axle.getRelative(0,1,0)))return;
        if(!isAir(axle.getRelative(0,2,0)))return;
        if(!isAir(axle.getRelative(right.x,2,right.z)))return;
        if(!isAir(axle.getRelative(0,-1,0)))return;
        if(!isAir(axle.getRelative(0,-2,0)))return;
        if(!isAir(axle.getRelative(left.x,-2,left.z)))return;
        if(!isAir(axle.getRelative(right.x,0,right.z)))return;
        if(!isAir(axle.getRelative(right.x*2,0,right.z*2)))return;
        if(!isAir(axle.getRelative(right.x*2,-1,right.z*2)))return;
        if(!isAir(axle.getRelative(left.x,0,left.z)))return;
        if(!isAir(axle.getRelative(left.x*2,0,left.z*2)))return;
        if(!isAir(axle.getRelative(left.x*2,1,left.z*2)))return;
        craft.aeronautics.debug(craft.getCrew(), "B");
        HashSet<Block> axleBases = new HashSet<>();
        axleBases.add(axle.getRelative(right.x,1,right.z));
        axleBases.add(axle.getRelative(left.x,1,left.z));
        axleBases.add(axle.getRelative(right.x,-1,right.z));
        axleBases.add(axle.getRelative(left.x,-1,left.z));
        HashSet<Block> axleTips = new HashSet<>();
        axleTips.add(axle.getRelative(left.x,2,left.z));
        axleTips.add(axle.getRelative(left.x*2,-1,left.z*2));
        axleTips.add(axle.getRelative(right.x,-2,right.z));
        axleTips.add(axle.getRelative(right.x*2,1,right.z*2));
        craft.aeronautics.debug(craft.getCrew(), "C");
        boolean found = false;
        for(HashSet<Material> base : turbine.bladeMaterials.keySet()){
            for(Block b : axleBases)if(!base.contains(b.getType()))continue;
            HashSet<Material> tip = turbine.bladeMaterials.get(base);
            for(Block b : axleTips)if(!tip.contains(b.getType()))continue;
            found = true;
            break;
        }
        if(!found)return;
        craft.aeronautics.debug(craft.getCrew(), "D");
        for(Block b : axleTips){
            BlockData data = b.getBlockData();
            if(data instanceof Stairs){
                Stairs s = (Stairs)data;
                if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
            }else return;
        }
        craft.aeronautics.debug(craft.getCrew(), "E");
        Stairs st = (Stairs)axle.getRelative(left.x,2,left.z).getBlockData();
        if(!left.matches(st.getFacing()))return;
        if(st.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sr = (Stairs)axle.getRelative(right.x*2,1,right.z*2).getBlockData();
        if(!left.matches(sr.getFacing()))return;
        if(sr.getHalf()!=Bisected.Half.TOP)return;
        Stairs sb = (Stairs)axle.getRelative(right.x,-2,right.z).getBlockData();
        if(!right.matches(sb.getFacing()))return;
        if(sb.getHalf()!=Bisected.Half.TOP)return;
        Stairs sl = (Stairs)axle.getRelative(left.x*2,-1,left.z*2).getBlockData();
        if(!right.matches(sl.getFacing()))return;
        if(sl.getHalf()!=Bisected.Half.BOTTOM)return;
        craft.aeronautics.debug(craft.getCrew(), "F");
        if(!craft.contains(axleTips))return;
        if(!craft.contains(axleBases))return;
        craft.aeronautics.debug(craft.getCrew(), "G");
        blades.put(pos, 1);
    }
    private void detectCounterClockwiseBlade1(Craft craft, Block origin, int pos, Direction dir){
        craft.aeronautics.debug(craft.getCrew(), "ACCW1");
        Block axle = origin.getRelative(dir.x*pos,dir.y*pos,dir.z*pos);
        Direction left = dir.getLeft();
        Direction right = dir.getRight();
        if(!isAir(axle.getRelative(0,1,0)))return;
        if(!isAir(axle.getRelative(0,2,0)))return;
        if(!isAir(axle.getRelative(left.x,2,left.z)))return;
        if(!isAir(axle.getRelative(0,-1,0)))return;
        if(!isAir(axle.getRelative(0,-2,0)))return;
        if(!isAir(axle.getRelative(right.x,-2,right.z)))return;
        if(!isAir(axle.getRelative(left.x,0,left.z)))return;
        if(!isAir(axle.getRelative(left.x*2,0,left.z*2)))return;
        if(!isAir(axle.getRelative(left.x*2,-1,left.z*2)))return;
        if(!isAir(axle.getRelative(right.x,0,right.z)))return;
        if(!isAir(axle.getRelative(right.x*2,0,right.z*2)))return;
        if(!isAir(axle.getRelative(right.x*2,1,right.z*2)))return;
        craft.aeronautics.debug(craft.getCrew(), "B");
        HashSet<Block> axleBases = new HashSet<>();
        axleBases.add(axle.getRelative(left.x,1,left.z));
        axleBases.add(axle.getRelative(right.x,1,right.z));
        axleBases.add(axle.getRelative(left.x,-1,left.z));
        axleBases.add(axle.getRelative(right.x,-1,right.z));
        HashSet<Block> axleTips = new HashSet<>();
        axleTips.add(axle.getRelative(right.x,2,right.z));
        axleTips.add(axle.getRelative(right.x*2,-1,right.z*2));
        axleTips.add(axle.getRelative(left.x,-2,left.z));
        axleTips.add(axle.getRelative(left.x*2,1,left.z*2));
        craft.aeronautics.debug(craft.getCrew(), "C");
        boolean found = false;
        for(HashSet<Material> base : turbine.bladeMaterials.keySet()){
            for(Block b : axleBases)if(!base.contains(b.getType()))continue;
            HashSet<Material> tip = turbine.bladeMaterials.get(base);
            for(Block b : axleTips)if(!tip.contains(b.getType()))continue;
            found = true;
            break;
        }
        if(!found)return;
        craft.aeronautics.debug(craft.getCrew(), "D");
        for(Block b : axleTips){
            BlockData data = b.getBlockData();
            if(data instanceof Stairs){
                Stairs s = (Stairs)data;
                if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
            }else return;
        }
        craft.aeronautics.debug(craft.getCrew(), "E");
        Stairs st = (Stairs)axle.getRelative(right.x,2,right.z).getBlockData();
        if(!right.matches(st.getFacing()))return;
        if(st.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sr = (Stairs)axle.getRelative(right.x*2,-1,right.z*2).getBlockData();
        if(!left.matches(sr.getFacing()))return;
        if(sr.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sb = (Stairs)axle.getRelative(left.x,-2,left.z).getBlockData();
        if(!left.matches(sb.getFacing()))return;
        if(sb.getHalf()!=Bisected.Half.TOP)return;
        Stairs sl = (Stairs)axle.getRelative(left.x*2,1,left.z*2).getBlockData();
        if(!right.matches(sl.getFacing()))return;
        if(sl.getHalf()!=Bisected.Half.TOP)return;
        craft.aeronautics.debug(craft.getCrew(), "F");
        if(!craft.contains(axleTips))return;
        if(!craft.contains(axleBases))return;
        craft.aeronautics.debug(craft.getCrew(), "G");
        blades.put(pos, -1);
    }
    private void detectCounterClockwiseBlade2(Craft craft, Block origin, int pos, Direction dir){
        craft.aeronautics.debug(craft.getCrew(), "ACCW2");
        Block axle = origin.getRelative(dir.x*pos,dir.y*pos,dir.z*pos);
        Direction left = dir.getLeft();
        Direction right = dir.getRight();
        if(!isAir(axle.getRelative(left.x,1,left.z)))return;
        if(!isAir(axle.getRelative(left.x,2,left.z)))return;
        if(!isAir(axle.getRelative(left.x*2,1,left.z*2)))return;
        if(!isAir(axle.getRelative(left.x,-1,left.z)))return;
        if(!isAir(axle.getRelative(left.x,-2,left.z)))return;
        if(!isAir(axle.getRelative(left.x*-2,1,left.z*2)))return;
        if(!isAir(axle.getRelative(right.x,1,right.z)))return;
        if(!isAir(axle.getRelative(right.x,2,right.z)))return;
        if(!isAir(axle.getRelative(right.x*2,1,right.z*2)))return;
        if(!isAir(axle.getRelative(right.x,-1,right.z)))return;
        if(!isAir(axle.getRelative(right.x,-2,right.z)))return;
        if(!isAir(axle.getRelative(right.x*-2,1,right.z*2)))return;
        craft.aeronautics.debug(craft.getCrew(), "B");
        HashSet<Block> axleBases = new HashSet<>();
        axleBases.add(axle.getRelative(left.x,0,left.z));
        axleBases.add(axle.getRelative(right.x,0,right.z));
        axleBases.add(axle.getRelative(0,-1,0));
        axleBases.add(axle.getRelative(0,1,0));
        HashSet<Block> axleTips = new HashSet<>();
        axleTips.add(axle.getRelative(left.x*2,0,left.z*2));
        axleTips.add(axle.getRelative(right.x*2,0,right.z*2));
        axleTips.add(axle.getRelative(0,-2,0));
        axleTips.add(axle.getRelative(0,2,0));
        craft.aeronautics.debug(craft.getCrew(), "C");
        boolean found = false;
        for(HashSet<Material> base : turbine.bladeMaterials.keySet()){
            for(Block b : axleBases)if(!base.contains(b.getType()))continue;
            HashSet<Material> tip = turbine.bladeMaterials.get(base);
            for(Block b : axleTips)if(!tip.contains(b.getType()))continue;
            found = true;
            break;
        }
        if(!found)return;
        craft.aeronautics.debug(craft.getCrew(), "D");
        for(Block b : axleTips){
            BlockData data = b.getBlockData();
            if(data instanceof Stairs){
                Stairs s = (Stairs)data;
                if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
            }else return;
        }
        craft.aeronautics.debug(craft.getCrew(), "E");
        Stairs st = (Stairs)axle.getRelative(0,2,0).getBlockData();
        if(!right.matches(st.getFacing()))return;
        if(st.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sr = (Stairs)axle.getRelative(right.x*2,0,right.z*2).getBlockData();
        if(!left.matches(sr.getFacing()))return;
        if(sr.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sb = (Stairs)axle.getRelative(0,-2,0).getBlockData();
        if(!left.matches(sb.getFacing()))return;
        if(sb.getHalf()!=Bisected.Half.TOP)return;
        Stairs sl = (Stairs)axle.getRelative(left.x*2,0,left.z*2).getBlockData();
        if(!right.matches(sl.getFacing()))return;
        if(sl.getHalf()!=Bisected.Half.TOP)return;
        craft.aeronautics.debug(craft.getCrew(), "F");
        if(!craft.contains(axleTips))return;
        if(!craft.contains(axleBases))return;
        craft.aeronautics.debug(craft.getCrew(), "G");
        blades.put(pos, -2);
    }
    private void detectCounterClockwiseBlade3(Craft craft, Block origin, int pos, Direction dir){
        craft.aeronautics.debug(craft.getCrew(), "ACCW3");
        Block axle = origin.getRelative(dir.x*pos,dir.y*pos,dir.z*pos);
        Direction right = dir.getRight();
        Direction left = dir.getLeft();
        if(!isAir(axle.getRelative(0,1,0)))return;
        if(!isAir(axle.getRelative(0,2,0)))return;
        if(!isAir(axle.getRelative(right.x,2,right.z)))return;
        if(!isAir(axle.getRelative(0,-1,0)))return;
        if(!isAir(axle.getRelative(0,-2,0)))return;
        if(!isAir(axle.getRelative(left.x,-2,left.z)))return;
        if(!isAir(axle.getRelative(right.x,0,right.z)))return;
        if(!isAir(axle.getRelative(right.x*2,0,right.z*2)))return;
        if(!isAir(axle.getRelative(right.x*2,-1,right.z*2)))return;
        if(!isAir(axle.getRelative(left.x,0,left.z)))return;
        if(!isAir(axle.getRelative(left.x*2,0,left.z*2)))return;
        if(!isAir(axle.getRelative(left.x*2,1,left.z*2)))return;
        craft.aeronautics.debug(craft.getCrew(), "B");
        HashSet<Block> axleBases = new HashSet<>();
        axleBases.add(axle.getRelative(right.x,1,right.z));
        axleBases.add(axle.getRelative(left.x,1,left.z));
        axleBases.add(axle.getRelative(right.x,-1,right.z));
        axleBases.add(axle.getRelative(left.x,-1,left.z));
        HashSet<Block> axleTips = new HashSet<>();
        axleTips.add(axle.getRelative(left.x,2,left.z));
        axleTips.add(axle.getRelative(left.x*2,-1,left.z*2));
        axleTips.add(axle.getRelative(right.x,-2,right.z));
        axleTips.add(axle.getRelative(right.x*2,1,right.z*2));
        craft.aeronautics.debug(craft.getCrew(), "C");
        boolean found = false;
        for(HashSet<Material> base : turbine.bladeMaterials.keySet()){
            for(Block b : axleBases)if(!base.contains(b.getType()))continue;
            HashSet<Material> tip = turbine.bladeMaterials.get(base);
            for(Block b : axleTips)if(!tip.contains(b.getType()))continue;
            found = true;
            break;
        }
        if(!found)return;
        craft.aeronautics.debug(craft.getCrew(), "D");
        for(Block b : axleTips){
            BlockData data = b.getBlockData();
            if(data instanceof Stairs){
                Stairs s = (Stairs)data;
                if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
            }else return;
        }
        craft.aeronautics.debug(craft.getCrew(), "E");
        Stairs st = (Stairs)axle.getRelative(left.x,2,left.z).getBlockData();
        if(!right.matches(st.getFacing()))return;
        if(st.getHalf()!=Bisected.Half.BOTTOM)return;
        Stairs sr = (Stairs)axle.getRelative(left.x*2,-1,left.z*2).getBlockData();
        if(!right.matches(sr.getFacing()))return;
        if(sr.getHalf()!=Bisected.Half.TOP)return;
        Stairs sb = (Stairs)axle.getRelative(right.x,-2,right.z).getBlockData();
        if(!left.matches(sb.getFacing()))return;
        if(sb.getHalf()!=Bisected.Half.TOP)return;
        Stairs sl = (Stairs)axle.getRelative(right.x*2,1,right.z*2).getBlockData();
        if(!left.matches(sl.getFacing()))return;
        if(sl.getHalf()!=Bisected.Half.BOTTOM)return;
        craft.aeronautics.debug(craft.getCrew(), "F");
        if(!craft.contains(axleTips))return;
        if(!craft.contains(axleBases))return;
        craft.aeronautics.debug(craft.getCrew(), "G");
        blades.put(pos, -3);
    }
    private boolean isAir(Block b){
        return b.getType().isAir()||b.getType()==Material.WATER||b.getType()==Material.BUBBLE_COLUMN;
    }
    @Override
    public void init(){}
    @Override
    public void tick(){
        double pSpeed = turbine.particleSpeed;
        double particleDensity = turbine.particleDensity;
        if(warmupProgress<turbine.warmupTime){
            warmupProgress++;
            float speed = warmupProgress/(float)turbine.warmupTime;
            pSpeed*=Math.pow(speed, turbine.particlePower);
            particleDensity*=Math.pow(speed, turbine.particlePower);
            bladeDelay = (int)((1-speed)*18)+2;
        }else bladeDelay = 1;
        bladeTimer--;
        if(bladeTimer<=0){
            bladeTimer+=bladeDelay;
            spinBlades();
        }
        for(int pos : blades.keySet()){
            int numParticles = (int)particleDensity;
            if(particleDensity<1){
                if(rand.nextDouble()<particleDensity)numParticles = 1;
            }
            for(int i = 0; i<numParticles; i++){
                double x = origin.getX()+facing.x*pos+.5;
                double y = origin.getY()+facing.y*pos+.5;
                double z = origin.getZ()+facing.z*pos+.5;
                x+=(rand.nextDouble()-.5)*turbine.particleDiameter;
                y+=(rand.nextDouble()-.5)*turbine.particleDiameter;
                z+=(rand.nextDouble()-.5)*turbine.particleDiameter;
                craft.getWorld().spawnParticle(Particle.CLOUD, x, y, z, 0, -facing.x*pSpeed, -facing.y*pSpeed, -facing.z*pSpeed);
            }
        }
    }
    private void spinBlades(){
        for(int pos : blades.keySet()){
            if(!canSpin(pos)){
                craft.aeronautics.debug(craft.getCrew(), "can't spin ("+pos+")");
                destroy();
                return;
            }
        }
        for(int pos : blades.keySet()){
            spin(pos);
        }
    }
    private boolean canSpin(int pos){
        Block axle = origin.getRelative(facing.x*pos,facing.y*pos,facing.z*pos);
        Direction left = facing.getLeft();
        Direction right = facing.getRight();
        switch(blades.get(pos)){
            case -3:
                return isAir(axle.getRelative(left.x*2,1,left.z*2))
                        &&isAir(axle.getRelative(right.x,2,right.z))
                        &&isAir(axle.getRelative(right.x*2,-1,right.z*2))
                        &&isAir(axle.getRelative(left.x,-2,left.z));
            case -2:
                return isAir(axle.getRelative(left.x,1,left.z))
                        &&isAir(axle.getRelative(left.x,2,left.z))
                        &&isAir(axle.getRelative(right.x,-1,right.z))
                        &&isAir(axle.getRelative(right.x,-2,right.z))
                        &&isAir(axle.getRelative(left.x,-1,left.z))
                        &&isAir(axle.getRelative(left.x*2,-1,left.z*2))
                        &&isAir(axle.getRelative(right.x,-1,right.z))
                        &&isAir(axle.getRelative(right.x*2,-1,right.z*2));
            case -1:
            case 1:
                return isAir(axle.getRelative(0,1,0))
                        &&isAir(axle.getRelative(0,2,0))
                        &&isAir(axle.getRelative(right.x,0,right.z))
                        &&isAir(axle.getRelative(right.x*2,0,right.z*2))
                        &&isAir(axle.getRelative(0,-1,0))
                        &&isAir(axle.getRelative(0,-2,0))
                        &&isAir(axle.getRelative(left.x,0,left.z))
                        &&isAir(axle.getRelative(left.x*2,0,left.z*2));
            case 2:
                return isAir(axle.getRelative(right.x,1,right.z))
                        &&isAir(axle.getRelative(right.x,2,right.z))
                        &&isAir(axle.getRelative(left.x,-1,left.z))
                        &&isAir(axle.getRelative(left.x,-2,left.z))
                        &&isAir(axle.getRelative(right.x,-1,right.z))
                        &&isAir(axle.getRelative(right.x*2,-1,right.z*2))
                        &&isAir(axle.getRelative(left.x,-1,left.z))
                        &&isAir(axle.getRelative(left.x*2,-1,left.z*2));
            case 3:
                return isAir(axle.getRelative(right.x*2,1,right.z*2))
                        &&isAir(axle.getRelative(right.x,-2,right.z))
                        &&isAir(axle.getRelative(left.x*2,-1,left.z*2))
                        &&isAir(axle.getRelative(left.x,2,left.z));
        }
        craft.notifyCrew("Turbine blade "+pos+" in invalid state "+blades.get(pos)+"!");  
        return false;
    }
    private void spin(int pos){
        HashSet<Block> oldBlocks = new HashSet<>();
        HashSet<Block> newBlocks = new HashSet<>();
        Block axle = origin.getRelative(facing.x*pos,facing.y*pos,facing.z*pos);
        Direction left = facing.getLeft();
        Direction right = facing.getRight();
        switch(blades.get(pos)){
            case -3:
                Block from = axle.getRelative(left.x,2,left.z);
                Block to = axle.getRelative(left.x*2,1,left.z*2);
                to.setType(from.getType(), false);
                Stairs stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);//also water support?
                from = axle.getRelative(left.x*2,-1,left.z*2);
                to = axle.getRelative(left.x,-2,left.z);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);
                from = axle.getRelative(right.x,-2,right.z);
                to = axle.getRelative(right.x*2,-1,right.z*2);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);
                from = axle.getRelative(right.x*2,1,right.z*2);
                to = axle.getRelative(right.x,2,right.z);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);
                blades.put(pos,-1);
                break;
            case -2:
                from = axle.getRelative(0,1,0);
                to = axle.getRelative(left.x,1,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(left.x,0,left.z);
                to = axle.getRelative(left.x,-1,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(0,-1,0);
                to = axle.getRelative(right.x,-1,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(right.x,0,right.z);
                to = axle.getRelative(right.x,1,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(0,2,0);
                to = axle.getRelative(left.x,2,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(left.x*2,0,left.z*2);
                to = axle.getRelative(left.x*2,-1,left.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(0,-2,0);
                to = axle.getRelative(right.x,-2,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(right.x*2,0,right.z*2);
                to = axle.getRelative(right.x*2,1,right.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                blades.put(pos,-3);
                break;
            case -1:
                from = axle.getRelative(right.x,1,right.z);
                to = axle.getRelative(0,1,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(left.x,1,left.z);
                to = axle.getRelative(left.x,0,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(left.x,-1,left.z);
                to = axle.getRelative(0,-1,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(right.x,-1,right.z);
                to = axle.getRelative(right.x,0,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(right.x,2,right.z);
                to = axle.getRelative(0,2,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(left.x*2,1,left.z*2);
                to = axle.getRelative(left.x*2,0,left.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(left.x,-2,left.z);
                to = axle.getRelative(0,-2,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from = axle.getRelative(right.x*2,-1,right.z*2);
                to = axle.getRelative(right.x*2,0,right.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                blades.put(pos,-2);
                break;
            case 1:
                to = axle.getRelative(0,1,0);
                from = axle.getRelative(left.x,1,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(left.x,0,left.z);
                from = axle.getRelative(left.x,-1,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(0,-1,0);
                from = axle.getRelative(right.x,-1,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(right.x,0,right.z);
                from = axle.getRelative(right.x,1,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(0,2,0);
                from = axle.getRelative(left.x,2,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(left.x*2,0,left.z*2);
                from = axle.getRelative(left.x*2,-1,left.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(0,-2,0);
                from = axle.getRelative(right.x,-2,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(right.x*2,0,right.z*2);
                from = axle.getRelative(right.x*2,1,right.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                blades.put(pos, 2);
                break;
            case 2:
                to = axle.getRelative(right.x,1,right.z);
                from = axle.getRelative(0,1,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(left.x,1,left.z);
                from = axle.getRelative(left.x,0,left.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(left.x,-1,left.z);
                from = axle.getRelative(0,-1,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(right.x,-1,right.z);
                from = axle.getRelative(right.x,0,right.z);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(right.x,2,right.z);
                from = axle.getRelative(0,2,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(left.x*2,1,left.z*2);
                from = axle.getRelative(left.x*2,0,left.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(left.x,-2,left.z);
                from = axle.getRelative(0,-2,0);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                to = axle.getRelative(right.x*2,-1,right.z*2);
                from = axle.getRelative(right.x*2,0,right.z*2);
                to.setType(from.getType(), false);
                from.setType(Material.AIR, false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                blades.put(pos,3);
                break;
            case 3:
                to = axle.getRelative(left.x,2,left.z);
                from = axle.getRelative(left.x*2,1,left.z*2);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);//also water support?
                to = axle.getRelative(left.x*2,-1,left.z*2);
                from = axle.getRelative(left.x,-2,left.z);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.BOTTOM);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);
                to = axle.getRelative(right.x,-2,right.z);
                from = axle.getRelative(right.x*2,-1,right.z*2);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(right.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);
                to = axle.getRelative(right.x*2,1,right.z*2);
                from = axle.getRelative(right.x,2,right.z);
                to.setType(from.getType(), false);
                stairs = (Stairs)to.getBlockData();
                stairs.setFacing(left.toBlockFace());
                stairs.setHalf(Bisected.Half.TOP);
                to.setBlockData(stairs, false);
                oldBlocks.add(from);
                newBlocks.add(to);
                from.setType(Material.AIR, false);
                blades.put(pos, 1);
                break;
        }
        craft.blocks.removeAll(oldBlocks);
        craft.blocks.addAll(newBlocks);
    }
    @Override
    public boolean rescan(){
        return facing==scan(craft, origin);
    }
    @Override
    public void onDestroy(){}
    @Override
    public void onRotated(int rotation){
        while(rotation>0){
            rotation--;
            switch(facing){
                case NORTH:
                    facing = Direction.EAST;
                    break;
                case EAST:
                    facing = Direction.SOUTH;
                    break;
                case SOUTH:
                    facing = Direction.WEST;
                    break;
                case WEST:
                    facing = Direction.NORTH;
                    break;
            }
        }
    }
}