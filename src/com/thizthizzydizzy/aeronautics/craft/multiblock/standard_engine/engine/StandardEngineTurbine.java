package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.engine;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.engine.Turbine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerConsumer;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerUser;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.StandardEngineEngine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.Vector;
public class StandardEngineTurbine extends Multiblock implements PowerConsumer, StandardEngineEngine{
    private final CraftEngine engine;
    private final StandardEngine standardEngine;
    private final Turbine turbine;
    private Direction facing;
    private final int length;
    private final ArrayList<Blade> blades = new ArrayList<>();
    private double targetThrottle;
    private double currentThrottle;
    private Random rand = new Random();
    private int storedPower = 0;
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
        if(blades!=null)this.blades.addAll(blades);
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
        ArrayList<Blade> blades = new ArrayList<>();
        craft.aeronautics.debug(craft.getCrew(), "Scanning potential turbine: "+origin.getX()+" "+origin.getY()+" "+origin.getZ()+" "+dir.toString());
        int len = 0;
        ROTOR:for(int i = 1; i<=turbine.maxLength*2; i++){//doubled so you can't double up turbines
            Block b = origin.getRelative(dir.x*i, dir.y*i, dir.z*i);
            if(turbine.outlets.contains(b.getType())){
                craft.aeronautics.debug(craft.getCrew(), "Another outlet found: "+b.getX()+" "+b.getY()+" "+b.getZ()+" "+b.getType().toString());
                return null;
            }//another outlet found
            if(turbine.rotors.contains(b.getType())&&craft.contains(b)&&b.getBlockData() instanceof Orientable o&&dir.matches(o.getAxis())){
                if(i>turbine.maxLength)continue;//don't worry about blade searching; just checking for extra outlets
                Direction up, right;
                if(dir.isVertical()){
                    up = Direction.NORTH;
                    right = Direction.EAST;
                }else{
                    up = dir.get2DY().getOpposite();
                    right = dir.get2DX().getOpposite();
                }
                int[][] bladeLengths = new int[3][4];//by rotation, then each blade
                for(int rotIdx = 0; rotIdx<3; rotIdx++){
                    for(int bladeIdx = 0; bladeIdx<4; bladeIdx++){
                        var main = switch(bladeIdx){
                            case 0 -> up;
                            case 1 -> right;
                            case 2 -> up.getOpposite();
                            case 3 -> right.getOpposite();
                            default -> null;
                        };
                        var secondaryCW = switch(bladeIdx){
                            case 0 -> right.getOpposite();
                            case 1 -> up;
                            case 2 -> right;
                            case 3 -> up.getOpposite();
                            default -> null;
                        };
                        var secondary = switch(rotIdx){
                            case 0 -> secondaryCW.getOpposite();
                            case 1 -> Direction.NONE;
                            default -> secondaryCW;
                        };
                        if(i==1){
                            craft.aeronautics.debug(craft.getCrew(), "DD "+rotIdx+" "+bladeIdx+" U "+up.name()+" R "+right.name()+" M "+main.name()+" S "+secondary.name());
                        }
                        int length = 0;
                        BLADE:for(int d = 1; d<=turbine.maxBladeLength; d++){
                            Block bl = b.getRelative(main.x*d+(secondary.x*d+secondary.x)/2, main.y*d+(secondary.y*d+secondary.y)/2, main.z*d+(secondary.z*d+secondary.z)/2);
                            int dist = switch(rotIdx){//checking for occlusion (excluding future blade position)
                                case 0, 1 -> ((d-1)/2);
                                case 2 -> d<(length+3)/2?2*d-(d-1)/2-3:(length-d+(length-d+1)/2);//only checking clockwise since this is regardless of blade spin direction; just checking for obstructions
                                default -> 0;
                            };
                            for(int m = 1; m<=dist; m++){
                                Block blo = bl.getRelative(secondaryCW.x*m, secondaryCW.y*m, secondaryCW.z*m);
                                boolean valid = false;
                                for(var medium : craft.type.mediums){
                                    if(medium.blocks.contains(blo.getType())){
                                        valid = true;
                                        break;
                                    }
                                }
                                if(!valid)break BLADE;//block is not valid for medium
                            }
                            if(turbine.bladeMaterials.contains(bl.getType())&&craft.contains(bl))length = d;//could use ++ but I didn't
                            else break;
                        }
                        if(length<turbine.minBladeLength)length = 0;
                        bladeLengths[rotIdx][bladeIdx] = length;
                    }
                }
                craft.aeronautics.debug(craft.getCrew(), "Blade lengths at "+i+": "+Arrays.deepToString(bladeLengths));
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
                        else{
                            //two valid rotor rotations; could be overlapping blades; use the biggest one
                            int lj = 0;
                            int lidx = 0;
                            for(int l : bladeLengths[j])if(l>lj)lj = l;
                            for(int l : bladeLengths[idx])if(l>lidx)lidx = l;
                            if(lj>lidx)idx = j;
                        }
                    }
                }
                if(idx==-1)continue;//no valid rotor rotations, no blade here
                Blade blade = new Blade(i, idx, bladeLengths[idx]);
                blades.add(blade);
                craft.aeronautics.debug(craft.getCrew(), "New blade found: "+blade.location+" "+blade.rotation+" "+Arrays.toString(blade.length));
                len = i;
            }else break;
        }
        craft.aeronautics.debug(craft.getCrew(), "Length: "+len);
        if(len==0)return null;//no blades
        return new StandardEngineTurbine(engine, standardEngine, turbine, craft, origin, dir, len, blades);
    }
    @Override
    public void init(){}
    @Override
    public void tick(){
        double pSpeed = turbine.particleSpeed;
        double particleDensity = turbine.particleDensity;
        double throttleInterval = 1d/getWarmupTime();
        double lessThrottle = Math.signum(currentThrottle)*Math.max(0, Math.abs(currentThrottle)-throttleInterval);
        if(currentThrottle<targetThrottle){
            currentThrottle+=throttleInterval;
            if(currentThrottle>targetThrottle)currentThrottle = targetThrottle;
        }
        if(currentThrottle>targetThrottle){
            currentThrottle-=throttleInterval;
            if(currentThrottle<targetThrottle)currentThrottle = targetThrottle;
        }
        double satisfaction = (double)storedPower/getDemand();
        currentThrottle = lessThrottle+(currentThrottle-lessThrottle)*satisfaction;
        pSpeed*=Math.pow(currentThrottle, turbine.particlePower);
        particleDensity*=Math.pow(currentThrottle, turbine.particlePower);
        for(Blade blade : blades){
            int numParticles = (int)particleDensity;
            if(particleDensity<1){
                if(rand.nextDouble()<particleDensity)numParticles = 1;
            }
            for(int i = 0; i<numParticles; i++){
                double x = origin.getX()+facing.x*blade.location+.5;
                double y = origin.getY()+facing.y*blade.location+.5;
                double z = origin.getZ()+facing.z*blade.location+.5;
                Vector offset = new Vector(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()).normalize().multiply(blade.getLength());
                x+=offset.getX();
                y+=offset.getY();
                z+=offset.getZ();
                craft.getWorld().spawnParticle(Particle.CLOUD, x, y, z, 0, -facing.x*pSpeed, -facing.y*pSpeed, -facing.z*pSpeed);
            }
        }
        storedPower = 0;
    }
    @Override
    public boolean rescan(){
        return turbine.outlets.contains(origin.getType());//TODO actual rescan, not just checking the outlet!
    }
    @Override
    public void onDestroy(){}
    @Override
    public void onRotated(int rotation){
        while(rotation>0){
            facing = facing.getRight();
        }
        while(rotation<0){
            facing = facing.getLeft();
        }
    }
    @Override
    public void getPowerConnectors(CraftEngine engine, StandardEngine standardEngine, List<Block> connectors){
        for(int i = 1; i<=length; i++){
            connectors.add(origin.getRelative(facing.toBlockFace(), i));
        }
    }
    @Override
    public String getEDSName(){
        return turbine.getEDSName();
    }
    @Override
    public double getThrottleMin(){
        return -1;
    }
    @Override
    public double getThrottleMax(){
        return 1;
    }
    @Override
    public double getMaxThrust(){
        double bladeVolume = 0;
        for(Blade b : blades){
            for(int l : b.length){
                bladeVolume+=(Math.PI*l*l)/4;
            }
            bladeVolume--;
        }
        return Math.max(0, bladeVolume);
    }
    @Override
    public void setThrottle(double throttle){
        targetThrottle = throttle;
    }
    private double getWarmupTime(){
        double size = 0;
        for(Blade b : blades)size+=b.getLength();
        return turbine.warmupTimeBase*Math.pow(size, turbine.warmupTimePower);
    }
    @Override
    public int getDemand(){
        double size = 0;
        for(Blade b : blades)size+=b.getLength();
        return (int)(turbine.powerUsageBase*Math.pow(Math.abs(currentThrottle), turbine.powerUsagePower));
    }
    @Override
    public void consume(int power){
        storedPower+=power;
    }
    public static class Blade{
        private final int location;
        private final int rotation;
        private final int[] length;
        public Blade(int location, int rotation, int[] length){
            this.location = location;
            this.rotation = rotation;
            this.length = length;
        }
        private double getLength(){
            double len = 0;
            for(int i : length)len+=i;
            return len/length.length;
        }
    }
}