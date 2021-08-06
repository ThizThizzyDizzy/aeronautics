package com.thizthizzydizzy.aeronautics.craft.multiblock;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftSpecial;
import com.thizthizzydizzy.aeronautics.craft.special.VerticalTurbine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Stairs;
public class VerticalTurbineMultiblock extends Multiblock{
    private final CraftSpecial special;
    private final VerticalTurbine turbine;
    private HashMap<Integer, Integer> blades = new HashMap<>();
    private int warmupProgress = 0;
    private int bladeDelay = 20;
    private int bladeTimer = 20;
    private Random rand = new Random();
    public VerticalTurbineMultiblock(CraftSpecial special, VerticalTurbine turbine){
        this(special, turbine, null, null);
    }
    private VerticalTurbineMultiblock(CraftSpecial special, VerticalTurbine turbine, Craft craft, Block origin){
        super("aeronautics:verical_turbine", craft, origin);
        this.special = special;
        this.turbine = turbine;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(!scan(craft, origin))return null;
        VerticalTurbineMultiblock mb = new VerticalTurbineMultiblock(special, turbine, craft, origin);
        mb.blades = this.blades;
        this.blades = new HashMap<>();
        return mb;
    }
    private boolean scan(Craft craft, Block origin){
        if(!turbine.outletMaterials.contains(origin.getType()))return false;//not the outlet!
        craft.aeronautics.debug(craft.getCrew(), "_A");
        HashSet<Block> theBlocks = new HashSet<>();
        ArrayList<Block> theRotor = new ArrayList<>();
        theBlocks.add(origin);
        int rotorLength = 0;
        for(int i = 1; i<=turbine.maxRotorLength; i++){
            Block b = origin.getRelative(0,i,0);
            if(turbine.rotorMaterials.contains(b.getType())){
                BlockData data = b.getBlockData();
                if(data instanceof Orientable){
                    if(((Orientable)data).getAxis()==Axis.Y){
                        theRotor.add(b);
                        rotorLength++;
                    }else break;
                }else break;
            }else break;
        }
        craft.aeronautics.debug(craft.getCrew(), "_B");
        if(rotorLength==0)return false;//that's too short!
        craft.aeronautics.debug(craft.getCrew(), "_C");
        theBlocks.addAll(theRotor);
        craft.aeronautics.debug(craft.getCrew(), "_D");
        blades.clear();
        for(int i = 0; i<theRotor.size(); i++){
            detectBlade(craft, origin, i+1);
        }
        craft.aeronautics.debug(craft.getCrew(), "_E"+theRotor.size()+" "+blades.size());
        if(blades.isEmpty())return false;
        if(!craft.contains(theBlocks))return false;
        craft.aeronautics.debug(craft.getCrew(), "_F");
        return true;
    }
    private void detectBlade(Craft craft, Block origin, int pos){
        detectClockwiseBlade1(craft, origin, pos);
        detectClockwiseBlade2(craft, origin, pos);
        detectClockwiseBlade3(craft, origin, pos);
        detectCounterClockwiseBlade1(craft, origin, pos);
        detectCounterClockwiseBlade2(craft, origin, pos);
        detectCounterClockwiseBlade3(craft, origin, pos);
        if(blades.containsKey(pos))craft.aeronautics.debug(craft.getCrew(), "Found blade: "+blades.get(pos)+" at "+pos);
    }
    private void detectClockwiseBlade1(Craft craft, Block origin, int pos){
        craft.aeronautics.debug(craft.getCrew(), "ACW1");
        Block axle = origin.getRelative(0,pos,0);
        for(Direction d : Direction.LATERAL){
            Direction r = d.getRight();
            Direction l = d.getLeft();
            Direction b = d.getOpposite();
            if(!isAir(axle.getRelative(d.x,d.y,d.z)))return;
            if(!isAir(axle.getRelative(d.x*2,d.y*2,d.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3,d.y*3,d.z*3)))return;
            if(!isAir(axle.getRelative(d.x*4,d.y*4,d.z*4)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x,d.y*3+l.y,d.z*3+l.z)))return;
            if(!isAir(axle.getRelative(d.x*4+l.x,d.y*4+l.y,d.z*4+l.z)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x,d.y*3+r.y,d.z*3+r.z)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x,d.y*4+r.y,d.z*4+r.z)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*2,d.y*2+r.y*2,d.z*2+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*3,d.y*3+r.y*3,d.z*3+r.z*3)))return;
            craft.aeronautics.debug(craft.getCrew(), "B");
            HashSet<Block> blade = new HashSet<>();
            blade.add(axle.getRelative(d.x+l.x,d.y+l.y,d.z+l.z));
            blade.add(axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z));
            blade.add(axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2));
            blade.add(axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2));
            for(Block block : blade){
                if(!turbine.bladeMaterials.contains(block.getType()))return;
                BlockData data = block.getBlockData();
                if(data instanceof Stairs){
                    Stairs s = (Stairs)data;
                    if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
                }else return;
            }
            craft.aeronautics.debug(craft.getCrew(), "C");
            Stairs s1 = (Stairs)axle.getRelative(d.x+l.x,d.y+l.y,d.z+l.z).getBlockData();
            Stairs s2 = (Stairs)axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z).getBlockData();
            Stairs s3 = (Stairs)axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2).getBlockData();
            Stairs s4 = (Stairs)axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2).getBlockData();
            if(!l.matches(s1.getFacing()))return;
            if(s1.getHalf()!=Bisected.Half.TOP)return;
            if(!l.matches(s2.getFacing()))return;
            if(s2.getHalf()!=Bisected.Half.TOP)return;
            if(!l.matches(s3.getFacing()))return;
            if(s3.getHalf()!=Bisected.Half.TOP)return;
            if(!b.matches(s4.getFacing()))return;
            if(s4.getHalf()!=Bisected.Half.BOTTOM)return;
            if(!craft.contains(blade))return;
            craft.aeronautics.debug(craft.getCrew(), "D");
        }
        blades.put(pos, 1);
    }
    private void detectClockwiseBlade2(Craft craft, Block origin, int pos){
        craft.aeronautics.debug(craft.getCrew(), "ACW2");
        Block axle = origin.getRelative(0,pos,0);
        for(Direction d : Direction.LATERAL){
            Direction l = d.getLeft();
            Direction r = d.getRight();
            Direction b = d.getOpposite();
            if(!isAir(axle.getRelative(d.x*1+r.x*1,d.y*1+r.y*1,d.z*1+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*1,d.y*2+r.y*1,d.z*2+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*1,d.y*3+r.y*1,d.z*3+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x*1,d.y*4+r.y*1,d.z*4+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*1+r.x*2,d.y*1+r.y*2,d.z*1+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*2,d.y*2+r.y*2,d.z*2+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*1+r.x*3,d.y*1+r.y*3,d.z*1+r.z*3)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*3,d.y*2+r.y*3,d.z*2+r.z*3)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*3,d.y*3+r.y*3,d.z*3+r.z*3)))return;
            if(!isAir(axle.getRelative(d.x*1+r.x*4,d.y*1+r.y*4,d.z*1+r.z*4)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*4,d.y*2+r.y*4,d.z*2+r.z*4)))return;
            craft.aeronautics.debug(craft.getCrew(), "B");
            HashSet<Block> blade = new HashSet<>();
            blade.add(axle.getRelative(d.x,d.y,d.z));
            blade.add(axle.getRelative(d.x*2,d.y*2,d.z*2));
            blade.add(axle.getRelative(d.x*3,d.y*3,d.z*3));
            blade.add(axle.getRelative(d.x*4,d.y*4,d.z*4));
            for(Block block : blade){
                if(!turbine.bladeMaterials.contains(block.getType()))return;
                BlockData data = block.getBlockData();
                if(data instanceof Stairs){
                    Stairs s = (Stairs)data;
                    if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
                }else return;
            }
            craft.aeronautics.debug(craft.getCrew(), "C");
            Stairs s1 = (Stairs)axle.getRelative(d.x,d.y,d.z).getBlockData();
            Stairs s2 = (Stairs)axle.getRelative(d.x*2,d.y*2,d.z*2).getBlockData();
            Stairs s3 = (Stairs)axle.getRelative(d.x*3,d.y*3,d.z*3).getBlockData();
            Stairs s4 = (Stairs)axle.getRelative(d.x*4,d.y*4,d.z*4).getBlockData();
            if(!l.matches(s1.getFacing()))return;
            if(s1.getHalf()!=Bisected.Half.TOP)return;
            if(!l.matches(s2.getFacing()))return;
            if(s2.getHalf()!=Bisected.Half.TOP)return;
            if(!l.matches(s3.getFacing()))return;
            if(s3.getHalf()!=Bisected.Half.TOP)return;
            if(!b.matches(s4.getFacing()))return;
            if(s4.getHalf()!=Bisected.Half.BOTTOM)return;
            if(!craft.contains(blade))return;
            craft.aeronautics.debug(craft.getCrew(), "D");
        }
        blades.put(pos, 2);
    }
    private void detectClockwiseBlade3(Craft craft, Block origin, int pos){
        craft.aeronautics.debug(craft.getCrew(), "ACW3");
        Block axle = origin.getRelative(0,pos,0);
        for(Direction d : Direction.LATERAL){
            Direction r = d.getRight();
            Direction l = d.getLeft();
            Direction b = d.getOpposite();
            if(!isAir(axle.getRelative(d.x,d.y,d.z)))return;
            if(!isAir(axle.getRelative(d.x*2,d.y*2,d.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3,d.y*3,d.z*3)))return;
            if(!isAir(axle.getRelative(d.x*4,d.y*4,d.z*4)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x,d.y*3+r.y,d.z*3+r.z)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x,d.y*4+r.y,d.z*4+r.z)))return;
            if(!isAir(axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x,d.y*3+l.y,d.z*3+l.z)))return;
            if(!isAir(axle.getRelative(d.x*4+l.x,d.y*4+l.y,d.z*4+l.z)))return;
            if(!isAir(axle.getRelative(d.x*2+l.x*2,d.y*2+l.y*2,d.z*2+l.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2)))return;
            if(!isAir(axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x*3,d.y*3+l.y*3,d.z*3+l.z*3)))return;
            craft.aeronautics.debug(craft.getCrew(), "B");
            HashSet<Block> blade = new HashSet<>();
            blade.add(axle.getRelative(d.x+r.x,d.y+r.y,d.z+r.z));
            blade.add(axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z));
            blade.add(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2));
            blade.add(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2));
            for(Block block : blade){
                if(!turbine.bladeMaterials.contains(block.getType()))return;
                BlockData data = block.getBlockData();
                if(data instanceof Stairs){
                    Stairs s = (Stairs)data;
                    if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
                }else return;
            }
            craft.aeronautics.debug(craft.getCrew(), "C");
            Stairs s1 = (Stairs)axle.getRelative(d.x+r.x,d.y+r.y,d.z+r.z).getBlockData();
            Stairs s2 = (Stairs)axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z).getBlockData();
            Stairs s3 = (Stairs)axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2).getBlockData();
            Stairs s4 = (Stairs)axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2).getBlockData();
            if(!l.matches(s1.getFacing()))return;
            if(s1.getHalf()!=Bisected.Half.TOP)return;
            if(!l.matches(s2.getFacing()))return;
            if(s2.getHalf()!=Bisected.Half.TOP)return;
            if(!l.matches(s3.getFacing()))return;
            if(s3.getHalf()!=Bisected.Half.TOP)return;
            if(!b.matches(s4.getFacing()))return;
            if(s4.getHalf()!=Bisected.Half.BOTTOM)return;
            if(!craft.contains(blade))return;
            craft.aeronautics.debug(craft.getCrew(), "D");
        }
        blades.put(pos, 3);
    }
    private void detectCounterClockwiseBlade1(Craft craft, Block origin, int pos){
        craft.aeronautics.debug(craft.getCrew(), "ACCW1");
        Block axle = origin.getRelative(0,pos,0);
        for(Direction d : Direction.LATERAL){
            Direction l = d.getLeft();
            Direction r = d.getRight();
            Direction b = d.getOpposite();
            if(!isAir(axle.getRelative(d.x,d.y,d.z)))return;
            if(!isAir(axle.getRelative(d.x*2,d.y*2,d.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3,d.y*3,d.z*3)))return;
            if(!isAir(axle.getRelative(d.x*4,d.y*4,d.z*4)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x,d.y*3+r.y,d.z*3+r.z)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x,d.y*4+r.y,d.z*4+r.z)))return;
            if(!isAir(axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x,d.y*3+l.y,d.z*3+l.z)))return;
            if(!isAir(axle.getRelative(d.x*4+l.x,d.y*4+l.y,d.z*4+l.z)))return;
            if(!isAir(axle.getRelative(d.x*2+l.x*2,d.y*2+l.y*2,d.z*2+l.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2)))return;
            if(!isAir(axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x*3,d.y*3+l.y*3,d.z*3+l.z*3)))return;
            craft.aeronautics.debug(craft.getCrew(), "B");
            HashSet<Block> blade = new HashSet<>();
            blade.add(axle.getRelative(d.x+r.x,d.y+r.y,d.z+r.z));
            blade.add(axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z));
            blade.add(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2));
            blade.add(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2));
            for(Block block : blade){
                if(!turbine.bladeMaterials.contains(block.getType()))return;
                BlockData data = block.getBlockData();
                if(data instanceof Stairs){
                    Stairs s = (Stairs)data;
                    if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
                }else return;
            }
            craft.aeronautics.debug(craft.getCrew(), "C");
            Stairs s1 = (Stairs)axle.getRelative(d.x+r.x,d.y+r.y,d.z+r.z).getBlockData();
            Stairs s2 = (Stairs)axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z).getBlockData();
            Stairs s3 = (Stairs)axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2).getBlockData();
            Stairs s4 = (Stairs)axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2).getBlockData();
            if(!r.matches(s1.getFacing()))return;
            if(s1.getHalf()!=Bisected.Half.TOP)return;
            if(!r.matches(s2.getFacing()))return;
            if(s2.getHalf()!=Bisected.Half.TOP)return;
            if(!r.matches(s3.getFacing()))return;
            if(s3.getHalf()!=Bisected.Half.TOP)return;
            if(!b.matches(s4.getFacing()))return;
            if(s4.getHalf()!=Bisected.Half.BOTTOM)return;
            if(!craft.contains(blade))return;
            craft.aeronautics.debug(craft.getCrew(), "D");
        }
        blades.put(pos, -1);
    }
    private void detectCounterClockwiseBlade2(Craft craft, Block origin, int pos){
        craft.aeronautics.debug(craft.getCrew(), "ACCW2");
        Block axle = origin.getRelative(0,pos,0);
        for(Direction d : Direction.LATERAL){
            Direction l = d.getLeft();
            Direction r = d.getRight();
            Direction b = d.getOpposite();
            if(!isAir(axle.getRelative(d.x*1+r.x*1,d.y*1+r.y*1,d.z*1+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*1,d.y*2+r.y*1,d.z*2+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*1,d.y*3+r.y*1,d.z*3+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x*1,d.y*4+r.y*1,d.z*4+r.z*1)))return;
            if(!isAir(axle.getRelative(d.x*1+r.x*2,d.y*1+r.y*2,d.z*1+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*2,d.y*2+r.y*2,d.z*2+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*1+r.x*3,d.y*1+r.y*3,d.z*1+r.z*3)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*3,d.y*2+r.y*3,d.z*2+r.z*3)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*3,d.y*3+r.y*3,d.z*3+r.z*3)))return;
            if(!isAir(axle.getRelative(d.x*1+r.x*4,d.y*1+r.y*4,d.z*1+r.z*4)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*4,d.y*2+r.y*4,d.z*2+r.z*4)))return;
            craft.aeronautics.debug(craft.getCrew(), "B");
            HashSet<Block> blade = new HashSet<>();
            blade.add(axle.getRelative(d.x,d.y,d.z));
            blade.add(axle.getRelative(d.x*2,d.y*2,d.z*2));
            blade.add(axle.getRelative(d.x*3,d.y*3,d.z*3));
            blade.add(axle.getRelative(d.x*4,d.y*4,d.z*4));
            for(Block block : blade){
                if(!turbine.bladeMaterials.contains(block.getType()))return;
                BlockData data = block.getBlockData();
                if(data instanceof Stairs){
                    Stairs s = (Stairs)data;
                    if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
                }else return;
            }
            craft.aeronautics.debug(craft.getCrew(), "C");
            Stairs s1 = (Stairs)axle.getRelative(d.x,d.y,d.z).getBlockData();
            Stairs s2 = (Stairs)axle.getRelative(d.x*2,d.y*2,d.z*2).getBlockData();
            Stairs s3 = (Stairs)axle.getRelative(d.x*3,d.y*3,d.z*3).getBlockData();
            Stairs s4 = (Stairs)axle.getRelative(d.x*4,d.y*4,d.z*4).getBlockData();
            if(!r.matches(s1.getFacing()))return;
            if(s1.getHalf()!=Bisected.Half.TOP)return;
            if(!r.matches(s2.getFacing()))return;
            if(s2.getHalf()!=Bisected.Half.TOP)return;
            if(!r.matches(s3.getFacing()))return;
            if(s3.getHalf()!=Bisected.Half.TOP)return;
            if(!b.matches(s4.getFacing()))return;
            if(s4.getHalf()!=Bisected.Half.BOTTOM)return;
            if(!craft.contains(blade))return;
            craft.aeronautics.debug(craft.getCrew(), "D");
        }
        blades.put(pos, -2);
    }
    private void detectCounterClockwiseBlade3(Craft craft, Block origin, int pos){
        craft.aeronautics.debug(craft.getCrew(), "ACCW3");
        Block axle = origin.getRelative(0,pos,0);
        for(Direction d : Direction.LATERAL){
            Direction l = d.getLeft();
            Direction r = d.getRight();
            Direction b = d.getOpposite();
            if(!isAir(axle.getRelative(d.x,d.y,d.z)))return;
            if(!isAir(axle.getRelative(d.x*2,d.y*2,d.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3,d.y*3,d.z*3)))return;
            if(!isAir(axle.getRelative(d.x*4,d.y*4,d.z*4)))return;
            if(!isAir(axle.getRelative(d.x*3+l.x,d.y*3+l.y,d.z*3+l.z)))return;
            if(!isAir(axle.getRelative(d.x*4+l.x,d.y*4+l.y,d.z*4+l.z)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x,d.y*3+r.y,d.z*3+r.z)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x,d.y*4+r.y,d.z*4+r.z)))return;
            if(!isAir(axle.getRelative(d.x*2+r.x*2,d.y*2+r.y*2,d.z*2+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2)))return;
            if(!isAir(axle.getRelative(d.x*3+r.x*3,d.y*3+r.y*3,d.z*3+r.z*3)))return;
            craft.aeronautics.debug(craft.getCrew(), "B");
            HashSet<Block> blade = new HashSet<>();
            blade.add(axle.getRelative(d.x+l.x,d.y+l.y,d.z+l.z));
            blade.add(axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z));
            blade.add(axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2));
            blade.add(axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2));
            for(Block block : blade){
                if(!turbine.bladeMaterials.contains(block.getType()))return;
                BlockData data = block.getBlockData();
                if(data instanceof Stairs){
                    Stairs s = (Stairs)data;
                    if(s.getShape()!=Stairs.Shape.STRAIGHT)return;
                }else return;
            }
            craft.aeronautics.debug(craft.getCrew(), "C");
            Stairs s1 = (Stairs)axle.getRelative(d.x+l.x,d.y+l.y,d.z+l.z).getBlockData();
            Stairs s2 = (Stairs)axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z).getBlockData();
            Stairs s3 = (Stairs)axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2).getBlockData();
            Stairs s4 = (Stairs)axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2).getBlockData();
            if(!r.matches(s1.getFacing()))return;
            if(s1.getHalf()!=Bisected.Half.TOP)return;
            if(!r.matches(s2.getFacing()))return;
            if(s2.getHalf()!=Bisected.Half.TOP)return;
            if(!r.matches(s3.getFacing()))return;
            if(s3.getHalf()!=Bisected.Half.TOP)return;
            if(!b.matches(s4.getFacing()))return;
            if(s4.getHalf()!=Bisected.Half.BOTTOM)return;
            if(!craft.contains(blade))return;
            craft.aeronautics.debug(craft.getCrew(), "D");
        }
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
                double x = origin.getX()+.5;
                double y = origin.getY()+pos+.5;
                double z = origin.getZ()+.5;
                x+=(rand.nextDouble()-.5)*turbine.particleDiameter;
                y+=(rand.nextDouble()-.5)*turbine.particleDiameter;
                z+=(rand.nextDouble()-.5)*turbine.particleDiameter;
                craft.getWorld().spawnParticle(Particle.CLOUD, x, y, z, 0, 0, -pSpeed, 0);
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
        Block axle = origin.getRelative(0,pos,0);
        switch(blades.get(pos)){
            case -3:
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    if(!isAir(axle.getRelative(d.x+l.x*2,d.y+l.y*2,d.z+l.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*2+l.x*2,d.y*2+l.y*2,d.z*2+l.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*2+l.x*3,d.y*2+l.y*3,d.z*2+l.z*3)))return false;
                    if(!isAir(axle.getRelative(d.x*2+l.x*4,d.y*2+l.y*4,d.z*2+l.z*4)))return false;
                    if(!isAir(axle.getRelative(d.x*3+l.x*3,d.y*3+l.y*3,d.z*3+l.z*3)))return false;
                }
                return true;
            case -2:
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    if(!isAir(axle.getRelative(d.x*1+l.x,d.y*1+l.y,d.z*1+l.z)))return false;
                    if(!isAir(axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z)))return false;
                    if(!isAir(axle.getRelative(d.x*3+l.x,d.y*3+l.y,d.z*3+l.z)))return false;
                    if(!isAir(axle.getRelative(d.x*4+l.x,d.y*4+l.y,d.z*4+l.z)))return false;
                    if(!isAir(axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2)))return false;
                }
                return true;
            case -1:
                for(Direction d : Direction.LATERAL){
                    Direction r = d.getRight();
                    if(!isAir(axle.getRelative(d.x*1,d.y*1,d.z*1)))return false;
                    if(!isAir(axle.getRelative(d.x*2,d.y*2,d.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*3,d.y*3,d.z*3)))return false;
                    if(!isAir(axle.getRelative(d.x*4,d.y*4,d.z*4)))return false;
                    if(!isAir(axle.getRelative(d.x*3+r.x,d.y*3+r.y,d.z*3+r.z)))return false;
                    if(!isAir(axle.getRelative(d.x*4+r.x,d.y*4+r.y,d.z*4+r.z)))return false;
                }
                return true;
            case 1:
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    if(!isAir(axle.getRelative(d.x*1,d.y*1,d.z*1)))return false;
                    if(!isAir(axle.getRelative(d.x*2,d.y*2,d.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*3,d.y*3,d.z*3)))return false;
                    if(!isAir(axle.getRelative(d.x*4,d.y*4,d.z*4)))return false;
                    if(!isAir(axle.getRelative(d.x*3+l.x,d.y*3+l.y,d.z*3+l.z)))return false;
                    if(!isAir(axle.getRelative(d.x*4+l.x,d.y*4+l.y,d.z*4+l.z)))return false;
                }
                return true;
            case 2:
                for(Direction d : Direction.LATERAL){
                    Direction r = d.getRight();
                    if(!isAir(axle.getRelative(d.x*1+r.x,d.y*1+r.y,d.z*1+r.z)))return false;
                    if(!isAir(axle.getRelative(d.x*2+r.x,d.y*2+r.y,d.z*2+r.z)))return false;
                    if(!isAir(axle.getRelative(d.x*3+r.x,d.y*3+r.y,d.z*3+r.z)))return false;
                    if(!isAir(axle.getRelative(d.x*4+r.x,d.y*4+r.y,d.z*4+r.z)))return false;
                    if(!isAir(axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2)))return false;
                }
                return true;
            case 3:
                for(Direction d : Direction.LATERAL){
                    Direction r = d.getRight();
                    if(!isAir(axle.getRelative(d.x+r.x*2,d.y+r.y*2,d.z+r.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*2+r.x*2,d.y*2+r.y*2,d.z*2+r.z*2)))return false;
                    if(!isAir(axle.getRelative(d.x*2+r.x*3,d.y*2+r.y*3,d.z*2+r.z*3)))return false;
                    if(!isAir(axle.getRelative(d.x*2+r.x*4,d.y*2+r.y*4,d.z*2+r.z*4)))return false;
                    if(!isAir(axle.getRelative(d.x*3+r.x*3,d.y*3+r.y*3,d.z*3+r.z*3)))return false;
                }
                return true;
        }
        craft.notifyCrew("VerticalTurbine blade "+pos+" in invalid state "+blades.get(pos)+"!");
        return false;
    }
    private void spin(int pos){
        HashSet<Block> oldBlocks = new HashSet<>();
        HashSet<Block> newBlocks = new HashSet<>();
        Block axle = origin.getRelative(0,pos,0);
        switch(blades.get(pos)){
            case -3:
                blades.put(pos, -1);
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    Direction r = d.getRight();
                    Direction b = d.getOpposite();
                    Block toRot = axle.getRelative(d.x+l.x,d.y+l.y,d.z+l.z);
                    Stairs stairs = (Stairs)toRot.getBlockData();
                    stairs.setFacing(d.toBlockFace());
                    toRot.setBlockData(stairs, false);
                    Block from = axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z);
                    Block to = axle.getRelative(d.x+l.x*2,d.y+l.y*2,d.z+l.z*2);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(d.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2);
                    to = axle.getRelative(d.x*2+l.x*3,d.y*2+l.y*3,d.z*2+l.z*3);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(d.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2);
                    to = axle.getRelative(d.x*2+l.x*4,d.y*2+l.y*4,d.z*2+l.z*4);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.BOTTOM);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                }
                break;
            case -2:
                blades.put(pos, -3);
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    Direction r = d.getRight();
                    Direction b = d.getOpposite();
                    Block to = axle.getRelative(d.x*1+l.x*1,d.y*1+l.y*1,d.z*1+l.z*1);
                    Block from = axle.getRelative(d.x*1,d.y*1,d.z*1);
                    to.setType(from.getType(), false);
                    Stairs stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to= axle.getRelative(d.x*2+l.x*1,d.y*2+l.y*1,d.z*2+l.z*1);
                    from = axle.getRelative(d.x*2,d.y*2,d.z*2);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2);
                    from = axle.getRelative(d.x*3,d.y*3,d.z*3);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2);
                    from = axle.getRelative(d.x*4,d.y*4,d.z*4);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(b.toBlockFace());
                    stairs.setHalf(Bisected.Half.BOTTOM);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                }
                break;
            case -1:
                blades.put(pos, -2);
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    Direction r = d.getRight();
                    Direction b = d.getOpposite();
                    Block from = axle.getRelative(d.x*1+r.x*1,d.y*1+r.y*1,d.z*1+r.z*1);
                    Block to = axle.getRelative(d.x*1,d.y*1,d.z*1);
                    to.setType(from.getType(), false);
                    Stairs stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*2+r.x*1,d.y*2+r.y*1,d.z*2+r.z*1);
                    to = axle.getRelative(d.x*2,d.y*2,d.z*2);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2);
                    to = axle.getRelative(d.x*3,d.y*3,d.z*3);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(r.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2);
                    to = axle.getRelative(d.x*4,d.y*4,d.z*4);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(b.toBlockFace());
                    stairs.setHalf(Bisected.Half.BOTTOM);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                }
                break;
            case 1:
                blades.put(pos, 2);
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    Direction r = d.getRight();
                    Direction b = d.getOpposite();
                    Block from = axle.getRelative(d.x*1+l.x*1,d.y*1+l.y*1,d.z*1+l.z*1);
                    Block to = axle.getRelative(d.x*1,d.y*1,d.z*1);
                    to.setType(from.getType(), false);
                    Stairs stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from= axle.getRelative(d.x*2+l.x*1,d.y*2+l.y*1,d.z*2+l.z*1);
                    to = axle.getRelative(d.x*2,d.y*2,d.z*2);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2);
                    to = axle.getRelative(d.x*3,d.y*3,d.z*3);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    from = axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2);
                    to = axle.getRelative(d.x*4,d.y*4,d.z*4);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(b.toBlockFace());
                    stairs.setHalf(Bisected.Half.BOTTOM);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                }
                break;
            case 2:
                blades.put(pos, 3);
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    Direction r = d.getRight();
                    Direction b = d.getOpposite();
                    Block to = axle.getRelative(d.x*1+r.x*1,d.y*1+r.y*1,d.z*1+r.z*1);
                    Block from = axle.getRelative(d.x*1,d.y*1,d.z*1);
                    to.setType(from.getType(), false);
                    Stairs stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*2+r.x*1,d.y*2+r.y*1,d.z*2+r.z*1);
                    from = axle.getRelative(d.x*2,d.y*2,d.z*2);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*3+r.x*2,d.y*3+r.y*2,d.z*3+r.z*2);
                    from = axle.getRelative(d.x*3,d.y*3,d.z*3);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*4+r.x*2,d.y*4+r.y*2,d.z*4+r.z*2);
                    from = axle.getRelative(d.x*4,d.y*4,d.z*4);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(b.toBlockFace());
                    stairs.setHalf(Bisected.Half.BOTTOM);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                }
                break;
            case 3:
                blades.put(pos, 1);
                for(Direction d : Direction.LATERAL){
                    Direction l = d.getLeft();
                    Direction r = d.getRight();
                    Direction b = d.getOpposite();
                    Block toRot = axle.getRelative(d.x+l.x,d.y+l.y,d.z+l.z);
                    Stairs stairs = (Stairs)toRot.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    toRot.setBlockData(stairs, false);
                    Block to = axle.getRelative(d.x*2+l.x,d.y*2+l.y,d.z*2+l.z);
                    Block from = axle.getRelative(d.x+l.x*2,d.y+l.y*2,d.z+l.z*2);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*3+l.x*2,d.y*3+l.y*2,d.z*3+l.z*2);
                    from = axle.getRelative(d.x*2+l.x*3,d.y*2+l.y*3,d.z*2+l.z*3);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(l.toBlockFace());
                    stairs.setHalf(Bisected.Half.TOP);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                    to = axle.getRelative(d.x*4+l.x*2,d.y*4+l.y*2,d.z*4+l.z*2);
                    from = axle.getRelative(d.x*2+l.x*4,d.y*2+l.y*4,d.z*2+l.z*4);
                    to.setType(from.getType(), false);
                    stairs = (Stairs)to.getBlockData();
                    stairs.setFacing(b.toBlockFace());
                    stairs.setHalf(Bisected.Half.BOTTOM);
                    to.setBlockData(stairs, false);
                    oldBlocks.add(from);
                    newBlocks.add(to);
                    from.setType(Material.AIR, false);
                }
                break;
        }
        craft.blocks.removeAll(oldBlocks);
        craft.blocks.addAll(newBlocks);
    }
    @Override
    public boolean rescan(){
        return scan(craft, origin);
    }
    @Override
    public void onDestroy(){}
    @Override
    public void onRotated(int rotation){}
}