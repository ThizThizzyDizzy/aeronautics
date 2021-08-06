package com.thizthizzydizzy.aeronautics.craft.engine.standard.engine;
import com.thizthizzydizzy.aeronautics.JSON;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.Message;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.SubEngine;
import com.thizthizzydizzy.aeronautics.craft.multiblock.Multiblock;
import com.thizthizzydizzy.vanillify.Vanillify;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
public class LiftCell extends SubEngine{
    private int minSize, maxSize;
    private HashMap<Material, Integer> interior = new HashMap<>();
    private HashMap<Material, Integer> exterior = new HashMap<>();
    public LiftCell(){
        super("aeronautics:lift_cell");
    }
    @Override
    protected void load(JSON.JSONObject json){
        minSize = json.getInt("min_size");
        maxSize = json.getInt("max_size");
        for(Object obj : json.getJSONArray("interior")){
            JSON.JSONObject jobj = (JSON.JSONObject)obj;
            for(Object o : jobj.getJSONArray("materials")){
                for(Material m : Vanillify.getBlocks((String)o)){
                    interior.put(m, 100);
                }
            }
        }
        for(Object obj : json.getJSONArray("exterior")){
            JSON.JSONObject jobj = (JSON.JSONObject)obj;
            for(Object o : jobj.getJSONArray("materials")){
                for(Material m : Vanillify.getBlocks((String)o)){
                    exterior.put(m, 100);
                }
            }
        }
    }
    @Override
    public SubEngine newInstance(){
        return new LiftCell();
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
}