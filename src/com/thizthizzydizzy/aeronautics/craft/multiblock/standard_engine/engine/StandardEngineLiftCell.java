package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.engine;
import com.thizthizzydizzy.aeronautics.Direction;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.engine.LiftCell;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerConsumer;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.StandardEngineEngine;
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
        super("aeronautics:standard_engine.turbine", craft, origin);
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