package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.generator;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.FurnaceGenerator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.FurnaceGenerator.FuelInfo;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerSupplier;
import java.util.List;
import org.bukkit.ChatColor;
public class StandardEngineFurnaceGenerator extends Multiblock implements PowerSupplier{
    private final CraftEngine enigne;
    private final StandardEngine standardEngine;
    private final FurnaceGenerator generator;
    private boolean powerRequested = false;
    private int power = 0;
    private FuelInfo burning = null;
    private int burnTime = 0;
    public StandardEngineFurnaceGenerator(CraftEngine engine, StandardEngine standardEngine, FurnaceGenerator generator){
        this(engine, standardEngine, generator, null, null);
    }
    private StandardEngineFurnaceGenerator(CraftEngine enigne, StandardEngine standardEngine, FurnaceGenerator generator, Craft craft, Block block){
        super("aeronautics:standard_engine.furnace_generator", craft, block);
        this.enigne = enigne;
        this.standardEngine = standardEngine;
        this.generator = generator;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(origin.getType()!=Material.FURNACE)return null;
        return new StandardEngineFurnaceGenerator(enigne, standardEngine, generator, craft, origin);
    }
    @Override
    public void init(){}
    @Override
    public void tick(){
        if(burning!=null){
            burnTime--;
            power = burning.power;
            if(burnTime<=0)burning = null;
        }else if(powerRequested){
            Furnace furnace = (Furnace)origin.getState();
            FurnaceInventory inventory = furnace.getInventory();
            ItemStack stack = inventory.getFuel();
            if(stack!=null&&stack.getAmount()>0){
                if(generator.fuels.containsKey(stack.getType())){
                    burning = generator.fuels.get(stack.getType());
                    burnTime += burning.time;
                    stack.setAmount(stack.getAmount()-1);
                    inventory.setFuel(stack);
                }
            }
        }
    }
    @Override
    public boolean rescan(){
        return origin.getType()==Material.FURNACE;
    }
    @Override
    public void onDestroy(){}
    @Override
    public void onRotated(int rotation){}
    @Override
    public int getAvailablePower(){
        return power;
    }
    @Override
    public int generate(int power){
        if(power>0)powerRequested = true;
        power = Math.min(this.power, power);
        this.power-=power;
        return power;
    }
    @Override
    public void getPowerConnectors(CraftEngine engine, StandardEngine standardEngine, List<Block> connectors){
        connectors.add(origin);
    }
    @Override
    public String getEDSName(){
        return generator.getEDSName();
    }
    @Override
    public String[] getBlockStats(boolean onSign){
        if(burning!=null){
            return new String[]{
                ChatColor.GREEN+"Running",
                (onSign?"Pow":"Power")+": "+burning.power,
                (onSign?"Tim":"Time")+": "+burnTime
            };
        }else{
            return new String[]{
                ChatColor.YELLOW+"Idle"
            };
        }
    }
    @Override
    public boolean contains(Block block){
        return block.getX()==origin.getX()&&block.getY()==origin.getY()&&block.getZ()==origin.getZ();
    }
}