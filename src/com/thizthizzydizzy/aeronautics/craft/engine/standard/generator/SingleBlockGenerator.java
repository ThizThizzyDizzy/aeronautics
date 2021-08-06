package com.thizthizzydizzy.aeronautics.craft.engine.standard.generator;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.Generator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.generator.StandardEngineSingleBlockGenerator;
import java.util.ArrayList;
import org.bukkit.Material;
public class SingleBlockGenerator extends Generator{
    public Material block;
    public int power;
    public SingleBlockGenerator(){
        super("aeronautics:single_block_generator");
    }
    @Override
    protected void load(JSON.JSONObject json){
        block = Material.matchMaterial(json.getString("block"));
        power = json.getInt("power");
    }
    @Override
    public Generator newInstance(){
        return new SingleBlockGenerator();
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new StandardEngineSingleBlockGenerator(engine, standardEngine, this));
    }
}