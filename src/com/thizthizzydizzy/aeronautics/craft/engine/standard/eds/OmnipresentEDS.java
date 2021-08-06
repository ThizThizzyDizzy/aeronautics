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
    public void getMultiblockTypes(CraftEngine engine, StandardEngine standardEngine, ArrayList<Multiblock> multiblockTypes){}
    @Override
    public ArrayList<Multiblock> getConnectedMultiblocks(CraftEngine engine){
        ArrayList<Multiblock> multiblocks = engine.getCraft().getMultiblocks();//TODO check to make sure that's even using this EDS!
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