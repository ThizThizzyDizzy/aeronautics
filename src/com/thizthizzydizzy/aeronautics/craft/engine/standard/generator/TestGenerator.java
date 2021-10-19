package com.thizthizzydizzy.aeronautics.craft.engine.standard.generator;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.Generator;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.generator.StandardEngineTestGenerator;
import java.util.ArrayList;
public class TestGenerator extends Generator{
    public int power;
    public TestGenerator(){
        super("aeronautics_test:generator");
    }
    @Override
    protected void load(JSON.JSONObject json){
        power = json.getInt("power");
    }
    @Override
    public Generator newInstance(){
        return new TestGenerator();
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void tick(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){}
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){}
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){
        multiblockTypes.add(new StandardEngineTestGenerator(engine, standardEngine, this));
    }
}