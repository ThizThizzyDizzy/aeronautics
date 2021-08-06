package com.thizthizzydizzy.aeronautics.craft.engine;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.JSON.JSONArray;
import com.thizthizzydizzy.aeronautics.JSON.JSONObject;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.CraftSign;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
public class FancyEngine extends Engine{
    public HashSet<Material> engineAxles = new HashSet<>();
    public HashSet<Material> engineBlocks = new HashSet<>();
    public HashSet<Material> connectorAxles = new HashSet<>();
    public HashSet<Material> connectorJunctions = new HashSet<>();
    public HashSet<Material> connectorBelts = new HashSet<>();
    public HashSet<Material> turbineRotorMaterials = new HashSet<>();
    public HashSet<Material> turbineOutletMaterials = new HashSet<>();
    public HashSet<Material> turbineBladeMaterials = new HashSet<>();
    public int maxTurbineLength;
    public int horizontalTurbineMinRadius;
    public int horizontalTurbineMaxRadius;
    public int verticalTurbineMinRadius;
    public int verticalTurbineMaxRadius;
    public float horizontalParticleDensity;
    public float horizontalParticleSpeed;
    public float horizontalParticlePower;
    public float verticalParticleDensity;
    public float verticalParticleSpeed;
    public float verticalParticlePower;
    public float warmupTimePower;
    public int warmupTimeBase;
    public FancyEngine(){
        super("aeronautics:fancy_engine");
    }
    @Override
    protected void load(JSON.JSONObject json){
        JSONObject engine = json.getJSONObject("engine");
        JSONArray engineAxles = engine.getJSONArray("axles");
        for(Object obj : engineAxles){
            this.engineAxles.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONArray engines = engine.getJSONArray("engines");
        for(Object obj : engines){
            this.engineBlocks.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONObject connectors = json.getJSONObject("connectors");
        JSONArray connectorAxles = connectors.getJSONArray("axles");
        for(Object obj : connectorAxles){
            this.connectorAxles.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONArray connectorJunctions = connectors.getJSONArray("junctions");
        for(Object obj : connectorJunctions){
            this.connectorJunctions.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONArray connectorBelts = connectors.getJSONArray("belts");
        for(Object obj : connectorBelts){
            this.connectorBelts.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONObject turbine = json.getJSONObject("turbine");
        JSONArray rotorMaterials = turbine.getJSONArray("rotorMaterials");
        for(Object obj : rotorMaterials){
            turbineRotorMaterials.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONArray outletMaterials = turbine.getJSONArray("outletMaterials");
        for(Object obj : outletMaterials){
            turbineOutletMaterials.addAll(Vanillify.getBlocks((String)obj));
        }
        JSONArray bladeMaterials = turbine.getJSONArray("bladeMaterials");
        for(Object obj : bladeMaterials){
            turbineBladeMaterials.addAll(Vanillify.getBlocks((String)obj));
        }
        maxTurbineLength = turbine.getInt("maxTurbineLength");
        horizontalTurbineMinRadius = turbine.getInt("horizontalTurbineMinRadius");
        horizontalTurbineMaxRadius = turbine.getInt("horizontalTurbineMaxRadius");
        verticalTurbineMinRadius = turbine.getInt("verticalTurbineMinRadius");
        verticalTurbineMaxRadius = turbine.getInt("verticalTurbineMaxRadius");
        horizontalParticleDensity = turbine.getFloat("horizontalParticleDensity");
        horizontalParticleSpeed = turbine.getFloat("horizontalParticleSpeed");
        horizontalParticlePower = turbine.getFloat("horizontalParticlePower");
        verticalParticleDensity = turbine.getFloat("verticalParticleDensity");
        verticalParticleSpeed = turbine.getFloat("verticalParticleSpeed");
        verticalParticlePower = turbine.getFloat("verticalParticlePower");
        warmupTimePower = turbine.getFloat("warmupTimePower");
        warmupTimeBase = turbine.getInt("warmupTimeBase");
    }
    @Override
    protected void createSigns(ArrayList<CraftSign> signs){}
    @Override
    public Engine newInstance(){
        return new FancyEngine();
    }
    @Override
    public void init(CraftEngine engine){
        //TODO rescan connections
    }
    @Override
    public void tick(CraftEngine engine){
        //TODO lift/movement
    }
    @Override
    public void event(CraftEngine engine, Event event){
        //TODO enable/disable engines?
    }
    @Override
    public void onUnload(CraftEngine engine){
        //TODO stop?
    }
    @Override
    public void onMoved(CraftEngine engine){}
    @Override
    public boolean canRemoveBlock(CraftEngine engine, Player player, int damage, boolean damaged, Location l){
        return true;
    }
    @Override
    public void updateHull(CraftEngine engine, int damage, boolean damaged){
        throw new UnsupportedOperationException("Not supported yet.");//TODO rescan connections
    }
    @Override
    public boolean canAddBlock(CraftEngine engine, Player player, Block block, boolean force){
        return true;
    }
    @Override
    public void getMessages(CraftEngine engine, ArrayList<Message> messages){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void getMultiblockTypes(CraftEngine engine, ArrayList<Multiblock> multiblockTypes){
//        multiblockTypes.add(new HorizontalTurbine(engine, this));
//        multiblockTypes.add(new VerticalTurbine(engine, this));
    }
}