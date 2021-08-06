package com.thizthizzydizzy.aeronautics.craft.multiblock.standard_engine;
import com.thizthizzydizzy.aeronautics.craft.CraftEngine;
import com.thizthizzydizzy.aeronautics.craft.engine.standard.StandardEngine;
import java.util.List;
import org.bukkit.block.Block;
public interface PowerUser{
    /**
     * Gets a list of blocks that can be used as power connectors for energy
     * distribution systems such as aeronautics:ducted.
     * @param engine the CraftEngine
     * @param standardEngine the StandardEngine
     * @param connectors the list of connectors to add to
     */
    public void getPowerConnectors(CraftEngine engine, StandardEngine standardEngine, List<Block> connectors);
    public String getEDSName();
}
