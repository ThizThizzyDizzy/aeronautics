package com.thizthizzydizzy.movecraft;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
public class MovementDetails{
    public final int moveTime;
    public final int horizDist;
    public final int vertDist;
    public final HashMap<ArrayList<Material>, Float> requiredRatios = new HashMap<>();
    public final HashMap<ArrayList<Material>, Float> requiredEngineRatios = new HashMap<>();
    public final HashMap<ArrayList<Material>, Integer> requiredBlocks = new HashMap<>();
    public final HashMap<ArrayList<Material>, Integer> requiredEngineBlocks = new HashMap<>();
    public MovementDetails(int moveTime, int horiz, int vert){
        this.moveTime = moveTime;
        this.horizDist = horiz;
        this.vertDist = vert;
    }
}