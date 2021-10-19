package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.engine;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.engine.LiftCell;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerConsumer;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.StandardEngineEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.block.Block;
public class StandardEngineLiftCell extends Multiblock implements PowerConsumer, StandardEngineEngine{
    private final CraftEngine engine;
    private final StandardEngine standardEngine;
    private final LiftCell liftCell;
    private double targetThrottle;
    private double currentThrottle;
    private Random rand = new Random();
    private int storedPower = 0;
    private int dxn;
    private int dxp;
    private int dyn;
    private int dyp;
    private int dzn;
    private int dzp;
    public StandardEngineLiftCell(CraftEngine engine, StandardEngine standardEngine, LiftCell liftCell){
        this(engine, standardEngine, liftCell, null, null, 0, 0, 0, 0, 0, 0);
    }
    public StandardEngineLiftCell(CraftEngine engine, StandardEngine standardEngine, LiftCell liftCell, Craft craft, Block origin, int dxn, int dxp, int dyn, int dyp, int dzn, int dzp){
        super("aeronautics:standard_engine.lift_cell", craft, origin);
        this.engine = engine;
        this.standardEngine = standardEngine;
        this.liftCell = liftCell;
        this.dxn = dxn;
        this.dxp = dxp;
        this.dyn = dyn;
        this.dyp = dyp;
        this.dzn = dzn;
        this.dzp = dzp;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(!liftCell.cores.contains(origin.getType()))return null;//not a core
        craft.aeronautics.debug(craft.getCrew(), "Scanning potential lift cell: "+origin.getX()+" "+origin.getY()+" "+origin.getZ());
        int xn = 0, yn = 0, zn = 0, xp = 0, yp = 0, zp = 0;
        DIR:for(Direction d : Direction.NONZERO){
            while(Math.max(xp+xn-1, Math.max(yp+yn-1, zp+zn-1))<liftCell.maxSize){
                ArrayList<Block> blocksToScan = new ArrayList<>();
                if(d.x>0){
                    for(int dy = -yn; dy<=yp; dy++){
                        for(int dz = -zn; dz<=zp; dz++){
                            blocksToScan.add(origin.getRelative(xp+1,dy,dz));
                        }
                    }
                }
                if(d.x<0){
                    for(int dy = -yn; dy<=yp; dy++){
                        for(int dz = -zn; dz<=zp; dz++){
                            blocksToScan.add(origin.getRelative(-xn-1,dy,dz));
                        }
                    }
                }
                if(d.y>0){
                    for(int dx = -xn; dx<=xp; dx++){
                        for(int dz = -zn; dz<=zp; dz++){
                            blocksToScan.add(origin.getRelative(dx,yp+1,dz));
                        }
                    }
                }
                if(d.y<0){
                    for(int dx = -xn; dx<=xp; dx++){
                        for(int dz = -zn; dz<=zp; dz++){
                            blocksToScan.add(origin.getRelative(dx,-yn-1,dz));
                        }
                    }
                }
                if(d.z>0){
                    for(int dx = -xn; dx<=xp; dx++){
                        for(int dy = -yn; dy<=yp; dy++){
                            blocksToScan.add(origin.getRelative(dx,dy,zp+1));
                        }
                    }
                }
                if(d.z<0){
                    for(int dx = -xn; dx<=xp; dx++){
                        for(int dy = -yn; dy<=yp; dy++){
                            blocksToScan.add(origin.getRelative(dx,dy,-zn-1));
                        }
                    }
                }
                if(blocksToScan.isEmpty())throw new IllegalArgumentException("Something has gone terribly wrong!");
                for(Block b : blocksToScan){
                    if(!liftCell.interior.contains(b.getType())){
                        continue DIR;
                    }
                }
                if(d.x>0)xp++;
                if(d.y>0)yp++;
                if(d.z>0)zp++;
                if(d.x<0)xn++;
                if(d.y<0)yn++;
                if(d.z<0)zn++;
            }
        }
        craft.aeronautics.debug(craft.getCrew(), "Core size: "+(xn+xp+1)+" "+(yn+yp+1)+" "+(zn+zp+1));
        ArrayList<Block> blocksToScan = new ArrayList<>();
        for(int dy = -yn; dy<=yp; dy++){
            for(int dz = -zn; dz<=zp; dz++){
                blocksToScan.add(origin.getRelative(xp+1,dy,dz));
                blocksToScan.add(origin.getRelative(-xn-1,dy,dz));
            }
        }
        for(int dx = -xn; dx<=xp; dx++){
            for(int dz = -zn; dz<=zp; dz++){
                blocksToScan.add(origin.getRelative(dx,yp+1,dz));
                blocksToScan.add(origin.getRelative(dx,-yn-1,dz));
            }
        }
        for(int dx = -xn; dx<=xp; dx++){
            for(int dy = -yn; dy<=yp; dy++){
                blocksToScan.add(origin.getRelative(dx,dy,zp+1));
                blocksToScan.add(origin.getRelative(dx,dy,-zn-1));
            }
        }
        for(Block b : blocksToScan){
            if(!liftCell.exterior.contains(b.getType())){
                craft.aeronautics.debug(craft.getCrew(), "Invalid Casing at "+b.getX()+" "+b.getY()+" "+b.getZ());
                return null;//invalid casing
            }
        }
        return new StandardEngineLiftCell(engine, standardEngine, liftCell, craft, origin, xn, xp, yn, yp, zn, zp);
    }
    @Override
    public void init(){}
    @Override
    public void tick(){
        double throttleInterval = 1d/getWarmupTime();
        double lessThrottle = Math.max(0, currentThrottle-throttleInterval*liftCell.noPowerCooldownMult);
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
        storedPower = 0;
    }
    @Override
    public boolean rescan(){
        return liftCell.cores.contains(origin.getType());//TODO actual rescan, not just checking the core!
    }
    @Override
    public void onDestroy(){}
    @Override
    public void onRotated(int rotation){
        while(rotation>0){
            int xn = dzp;
            int xp = dzn;
            int zn = dxn;
            int zp = dxp;
            dxn = xn;
            dxp = xp;
            dzn = zn;
            dzp = zp;
        }
        while(rotation<0){
            
        }
    }
    @Override
    public void getPowerConnectors(CraftEngine engine, StandardEngine standardEngine, List<Block> connectors){
        for(int x = -dxn-1; x<=dxp+1; x++){
            for(int y = -dyn-1; y<=dyp+1; y++){
                for(int z = -dzn-1; z<=dzp+1; z++){
                    int numEdges = 0;
                    if(x==dxn-1||x==dxp+1)numEdges++;
                    if(y==dyn-1||y==dyp+1)numEdges++;
                    if(z==dzn-1||z==dzp+1)numEdges++;
                    if(numEdges==1)connectors.add(origin.getRelative(x, y, z));
                }
            }
        }
    }
    @Override
    public String getEDSName(){
        return liftCell.getEDSName();
    }
    @Override
    public double getThrottleMin(Direction dir){
        return 0;
    }
    @Override
    public double getThrottleMax(Direction dir){
        return dir==Direction.UP?1:0;
    }
    @Override
    public double getMaxThrust(Direction dir){
        return dir==Direction.UP?liftCell.liftPerBlock*getVolume():0;
    }
    @Override
    public void setThrottle(Direction dir, double throttle){
        if(dir==Direction.UP)targetThrottle = Math.max(0, Math.min(1, throttle));
    }
    private double getWarmupTime(){
        return liftCell.warmupTimeBase*getVolume()/getSurfaceArea();
    }
    private int getVolume(){
        return (dxn+dxp+1)*(dyn+dyp+1)*(dzn+dzp+1);
    }
    private int getSurfaceArea(){
        return (dxn+dxp+1)*(dzn+dzp+1)*2+(dxn+dxp+1)*(dyn+dyp+1)*2+(dzn+dzp+1)*(dyn+dyp+1)*2;
    }
    @Override
    public int getDemand(){
        return (int)((liftCell.powerUsageMin+(liftCell.powerUsageMax-liftCell.powerUsageMin)*targetThrottle)*getSurfaceArea());
    }
    @Override
    public void consume(int power){
        storedPower+=power;
    }
}