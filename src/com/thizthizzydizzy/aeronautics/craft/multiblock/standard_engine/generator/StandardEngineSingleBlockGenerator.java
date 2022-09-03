package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.generator;
import com.thizthizzydizzy.aeronautics.craft.Craft;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.generator.SingleBlockGenerator;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import org.bukkit.block.Block;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerSupplier;
import java.util.List;
public class StandardEngineSingleBlockGenerator extends Multiblock implements PowerSupplier{
    private final CraftEngine enigne;
    private final StandardEngine standardEngine;
    private final SingleBlockGenerator generator;
    private int power;
    public StandardEngineSingleBlockGenerator(CraftEngine engine, StandardEngine standardEngine, SingleBlockGenerator generator){
        this(engine, standardEngine, generator, null, null);
    }
    private StandardEngineSingleBlockGenerator(CraftEngine enigne, StandardEngine standardEngine, SingleBlockGenerator generator, Craft craft, Block origin){
        super("aeronautics:standard_engine.single_block_generator", craft, origin);
        this.enigne = enigne;
        this.standardEngine = standardEngine;
        this.generator = generator;
    }
    @Override
    public Multiblock detect(Craft craft, Block origin){
        if(origin.getType()!=generator.block)return null;
        return new StandardEngineSingleBlockGenerator(enigne, standardEngine, generator, craft, origin);
    }
    @Override
    public void init(){}
    @Override
    public void tick(){
        power = generator.power;
    }
    @Override
    public boolean rescan(){
        return origin.getType()==generator.block;
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
        return new String[]{
            (onSign?"Pow: ":"Producing ")+generator.power
        };
    }
    @Override
    public boolean contains(Block block){
        return block==origin;
    }
}