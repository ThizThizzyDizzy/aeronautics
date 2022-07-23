package com.thizthizzydizzy.aeronautics.craft.engine.standard.eds;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.EnergyDistributionSystem;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerConsumer;
import java.util.ArrayList;
import com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine.PowerSupplier;
import java.util.HashMap;
/**
 * A general energy distribution system that 
 * @author Thiz
 */
public abstract class BalancedEDS extends EnergyDistributionSystem{
    public BalancedEDS(String definitionName){
        super(definitionName);
    }
    public BalancedEDS(String definitionName, String name){
        super(definitionName, name);
    }
    @Override
    public void tick(CraftEngine engine, StandardEngine standardEngine){
        ArrayList<Multiblock> multiblocks = getConnectedMultiblocks(engine);
        HashMap<PowerSupplier, Integer> suppliers = new HashMap<>();
        ArrayList<PowerConsumer> consumers = new ArrayList<>();
        int totalDemand = 0;
        int immediateSupply = 0;
        for(Multiblock mb : multiblocks){
            if(mb instanceof PowerConsumer pc){
                totalDemand+=pc.getDemand();
                consumers.add(pc);
            }
            if(mb instanceof PowerSupplier ps){
                suppliers.put(ps, ps.getAvailablePower());
                immediateSupply+=ps.getAvailablePower();
            }
        }
        int actualSupply = 0;
        double powerDemandPercentage = Math.min(1, totalDemand/(double)immediateSupply);
        for(var ps : suppliers.keySet()){
            int power = (int)(suppliers.get(ps)*powerDemandPercentage);
            power = ps.generate(power);
            actualSupply+=power;
        }
        if(actualSupply<totalDemand){
            for(var ps : suppliers.keySet()){
                actualSupply+=ps.generate(totalDemand-actualSupply);//asking for more power than has been provided; this will cause some generators to turn on or spin up
                if(actualSupply>=totalDemand)break;
            }
        }
        //got all the power you can get
        double powerSupplyPercentage = Math.min(1,actualSupply/(double)totalDemand);
        int remainingSupply = actualSupply;
        for(var pc : consumers){
            int power = (int)(pc.getDemand()*powerSupplyPercentage);
            pc.consume(power);
            remainingSupply-=power;
        }
        if(remainingSupply>0){//use up the last few shreds of power
            for(var pc : consumers){
                int power = Math.min(remainingSupply, pc.getDemand());
                pc.consume(power);
                remainingSupply-=power;
            }
        }
//        if(remainingSupply!=0)throw new IllegalArgumentException("Power was not distributed properly! "+remainingSupply+" remaining supply should be zero!");
        lastSupply = actualSupply;
        lastDemand = totalDemand;
        lastExcess = remainingSupply;
    }
    int lastSupply, lastDemand, lastExcess;
    public abstract ArrayList<Multiblock> getConnectedMultiblocks(CraftEngine engine);
    @Override
    public void getMessages(CraftEngine engine, StandardEngine standardEngine, ArrayList<Message> messages){
        messages.add(new Message(Message.Priority.CRITICAL, true, true, lastSupply+"/"+lastDemand+"|"+lastExcess));
    }
}