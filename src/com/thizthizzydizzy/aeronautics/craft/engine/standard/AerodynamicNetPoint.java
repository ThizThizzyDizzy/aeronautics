package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.Direction;
import java.util.HashMap;
import org.bukkit.util.Vector;
public class AerodynamicNetPoint{
    public final AerodynamicNetSide side;
    public double x;
    public double y;
    public double z;
    public Vector normal;
    public double sharpness;
    public double flatness;
    public HashMap<Direction, Double> aerodynamicness = new HashMap<>();
    public HashMap<Direction, Double> forwardFactor = new HashMap<>();
    public HashMap<Direction, Double> sideFactor = new HashMap<>();
    public HashMap<Direction, Double> forwardness = new HashMap<>();
    public HashMap<Direction, Double> sideness = new HashMap<>();
    public HashMap<Direction, Double> angle = new HashMap<>();
    public AerodynamicNetPoint(AerodynamicNetSide side, double x, double y, double z){
        this.side = side;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}