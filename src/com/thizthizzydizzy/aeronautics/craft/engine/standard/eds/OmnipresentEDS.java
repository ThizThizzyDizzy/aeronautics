package com.thizthizzydizzy.aeronautics.craft.engine.standard.eds;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.EnergyDistributionSystem;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import java.util.ArrayList;
import java.util.Iterator;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerUser;
public class OmnipresentEDS extends BalancedEDS{
    public OmnipresentEDS(){
        super("aeronautics:omnipresent");
    }
    public OmnipresentEDS(String name){
        super("aeronautics:omnipresent", name);
    }
    @Override
    protected void load(JSON.JSONObject json){}
    @Override
    public EnergyDistributionSystem newInstance(String name){
        return new OmnipresentEDS(name);
    }
    @Override
    public void init(CraftEngine engine, StandardEngine standardEngine){}
    @Override
    public void updateHull(CraftEngine engine, StandardEngine standardEngine, int damage, boolean damaged){}
    @Override
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){}
    @Override
    public ArrayList<Multiblock> getConnectedMultiblocks(CraftEngine engine){
        ArrayList<Multiblock> multiblocks = engine.getCraft().getMultiblocks();
        for(Iterator<Multiblock> it = multiblocks.iterator(); it.hasNext();){
            Multiblock next = it.next();
            if(next instanceof PowerUser){
                if(((PowerUser)next).getEDSName().equals(getName())){
                    //yay, moving on
                }else it.remove();
            }else it.remove();
        }
        return multiblocks;
    }
}