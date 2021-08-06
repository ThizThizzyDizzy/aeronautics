package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.engine;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.engine.Turbine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerUser;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
public class StandardEngineTurbine extends Multiblock implements PowerUser{
    private final CraftEngine engine;
    private final StandardEngine standardEngine;
    private final Turbine turbine;
    private final Direction facing;
    private final int length;
    private final ArrayList<Blade> blades;
    public StandardEngineTurbine(CraftEngine engine, StandardEngine standardEngine, Turbine turbine){
        this(engine, standardEngine, turbine, null, null, null, 0, null);
    }
    public StandardEngineTurbine(CraftEngine engine, StandardEngine standardEngine, Turbine turbine, Craft craft, Block origin, Direction facing, int length, ArrayList<Blade> blades){
        super("aeronautics:standard_engine.turbine", craft, origin);
        this.engine = engine;
        this.standardEngine = standardEngine;
        this.turbine = turbine;
        this.facing = facing;
        this.length = length;
        this.blades = blades;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(!turbine.outlets.contains(origin.getType()))return null;//no outlet!
        Direction dir = null;
        for(Direction d : Direction.NONZERO){
            if(turbine.rotors.contains(origin.getRelative(d.x, d.y, d.z).getType())){
                if(dir!=null)return null;//two many rotors found
                else dir = d;
            }
        }
        if(dir==null)return null;//no rotors found
        int len = 0;
        ROTOR:for(int i = 1; i<=turbine.maxLength*2; i++){//doubled so you can't double up turbines
            Block b = origin.getRelative(dir.x*i, dir.y*i, dir.z*i);
            if(turbine.outlets.contains(b.getType()))return null;//another outlet found
            if(turbine.rotors.contains(b.getType())&&craft.contains(b)&&b.getBlockData() instanceof Orientable o&&dir.matches(o.getAxis())){
                if(i>turbine.maxLength)continue;//don't worry about blade searching; just checking for extra outlets
                Direction up, right;
                if(dir.isVertical()){
                    up = dir.NORTH;
                    right = dir.EAST;
                }else{
                    up = dir.get2DY().getOpposite();
                    right = dir.get2DX().getOpposite();
                }
                int[][] bladeLengths = new int[3][4];//by rotation, then each blade
                for(int j = 0; j<3; j++){
                    for(int k = 0; k<4; k++){
                        var main = switch(k){
                            case 0 -> up;
                            case 1 -> right;
                            case 2 -> up.getOpposite();
                            case 3 -> right.getOpposite();
                            default -> null;
                        };
                        var secondaryCW = switch(k){
                            case 0 -> right.getOpposite();
                            case 1 -> up;
                            case 2 -> right;
                            case 3 -> up.getOpposite();
                            default -> null;
                        };
                        var secondary = switch(j){
                            case 0 -> secondaryCW.getOpposite();
                            case 1 -> Direction.NONE;
                            default -> secondaryCW;
                        };
                        int length = 0;
                        BLADE:for(int d = 1; d<=turbine.maxBladeLength; d++){
                            Block bl = b.getRelative(main.x*d+(secondary.x-1)/2*d, main.y*d+(secondary.y-1)/2*d, main.z*d+(secondary.z-1)/2*d);
                            int dist = switch(j){//checking for occlusion (excluding future blade position)
                                case 0, 1 -> ((d-1)/2);
                                case 2 -> d<(length+3)/2?2*d-(d-1)/2-3:(length-d+(length-d+1)/2);//only checking clockwise since this is regardless of blade spin direction; just checking for obstructions
                                default -> 0;
                            };
                            for(int m = 1; m<=dist; m++){
                                Block blo = bl.getRelative(secondaryCW.x*m, secondaryCW.y*m, secondaryCW.z*m);
                                if(!blo.getType().isAir())break BLADE;//TODO mediums check
                            }
                            if(turbine.bladeMaterials.contains(bl.getType())&&craft.contains(bl))length = d;//could use ++ but I didn't
                            else break;
                        }
                        if(length<turbine.minBladeLength)length = 0;
                        bladeLengths[j][k] = length;
                    }
                }
                int idx = -1;
                boolean[] full = new boolean[3];
                for(int j = 0; j<3; j++){
                    boolean yesNoMaybeSo = true;
                    boolean isPartial = false;
                    for(int k = 0; k<4; k++){
                        if(bladeLengths[j][k]==0)yesNoMaybeSo = false;//length will be zero if smaller than minimum
                        else isPartial = true;
                    }
                    if(yesNoMaybeSo)isPartial = false;
                    if(isPartial)continue ROTOR;//partial blade here; no blade is possible
                    if(yesNoMaybeSo){
                        if(idx==-1)idx = j;
                        else continue ROTOR;//two valid rotor rotations; something's not right
                    }
                }
                if(idx==-1)continue;//no valid rotor rotations, no blade here
                blades.add(new Blade(i, idx, bladeLengths[idx]));
                len = i;
            }else break;
        }
        if(len==0)return null;//no blades
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void init(){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void tick(){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean rescan(){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void onDestroy(){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void onRotated(int rotation){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void getPowerConnectors(CraftEngine engine, StandardEngine standardEngine, List<Block> connectors){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public String getEDSName(){
        return turbine.getEDSName();
    }
    private static class Blade{
        private final int location;
        private final int rotation;
        private final int[] length;
        public Blade(int location, int rotation, int[] length){
            this.location = location;
            this.rotation = rotation;
            this.length = length;
        }
    }
}