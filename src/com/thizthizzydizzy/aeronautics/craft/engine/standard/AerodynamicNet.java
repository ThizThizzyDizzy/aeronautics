package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.util.Vector;
public class AerodynamicNet{
    public final AerodynamicNetSide north;
    public final AerodynamicNetSide east;
    public final AerodynamicNetSide south;
    public final AerodynamicNetSide west;
    public final AerodynamicNetSide up;
    public final AerodynamicNetSide down;
    public final ArrayList<AerodynamicNetSide> sides = new ArrayList<>();
    public final double originX;
    public final double originY;
    public final double originZ;
    public final HashMap<Direction, AerodynamicSettings> aerodynamics = new HashMap<>();
    public AerodynamicSettings cwAerodynamics;
    public AerodynamicSettings ccwAerodynamics;
    public AerodynamicNet(double originX, double originY, double originZ, int resolution, double xSize, double ySize, double zSize){
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        north = new AerodynamicNetSide(Direction.NORTH, resolution, xSize, ySize, zSize);
        east = new AerodynamicNetSide(Direction.EAST, resolution, xSize, ySize, zSize);
        south = new AerodynamicNetSide(Direction.SOUTH, resolution, xSize, ySize, zSize);
        west = new AerodynamicNetSide(Direction.WEST, resolution, xSize, ySize, zSize);
        up = new AerodynamicNetSide(Direction.UP, resolution, xSize, ySize, zSize);
        down = new AerodynamicNetSide(Direction.DOWN, resolution, xSize, ySize, zSize);
        sides.add(north);
        sides.add(east);
        sides.add(south);
        sides.add(west);
        sides.add(up);
        sides.add(down);
    }
    public AerodynamicNetSide getSide(Direction d){
        for(AerodynamicNetSide side : sides)if(side.direction==d)return side;
        return null;
    }
    public void blend(int blend){
        //TODO properly handle edges and corners
        for(var side : sides)side.blend(blend);
    }
    public void calculateGeometricNormals(){
        for(var side : sides)side.calculateGeometricNormals();
    }
    public void calculateNormals(){
        for(var side : sides)side.calculateNormals();
    }
    public void inflate(double distance) {
        for(var side : sides)side.inflate(distance);
    }
    public void calculateSharpness(double flatnessPow){
        for(var side : sides)side.calculateSharpness(flatnessPow);
    }
    public void calculateAerodynamics(double flatnessPenalty, double sideWeight){
        for(Direction d : Direction.NONZERO){
            calculateAerodynamics(d, flatnessPenalty, sideWeight);
        }
    }
    private void calculateAerodynamics(Direction front, double flatnessPenalty, double sideWeight){
        for(var side : sides){
            double totalAerodynamicness = 0;
            double total = 0;
            for(var points : side.net){
                for(var point : points){
                    double[] aero = aeroCalc(point, front, flatnessPenalty, sideWeight);
                    totalAerodynamicness+=aero[0]*aero[1];
                    total+=aero[1];
                }
            }
            totalAerodynamicness/=total;
            aerodynamics.put(front, new AerodynamicSettings(totalAerodynamicness));
        }
        for(int dir = 0; dir<2; dir++){
            double totalAerodynamicness = 0;
            double total = 0;
            for(var direction : Direction.LATERAL){
                var side = getSide(direction);
                for(int x = 0; x<side.net.length; x++){
                    var points = side.net[x];
                    double percent = x/(side.net.length-1d);
                    var testDirection = percent>0.5?direction.getOpposite():direction;
                    if(dir==0)testDirection = testDirection.getOpposite();
                    double weight = -Math.abs(2*percent-1)+1;
                    for(var point : points){//this is vertical, so the index doesn't matter
                        double[] aero = aeroCalc(point, testDirection, flatnessPenalty, sideWeight);
                        totalAerodynamicness+=aero[0]*aero[1]*weight;
                        total+=aero[1]*weight;
                    }
                }
                for(var direc : Direction.VERTICAL){
                    var sid = getSide(direc);
                    for(int x = 0; x<sid.net.length; x++){
                        var points = sid.net[x];
                        double xPercent = x/(sid.net.length-1d);
                        for(int y = 0; y<points.length; y++){
                            var point = points[y];
                            double yPercent = y/(points.length-1d);
                            double xDiff = Math.abs(xPercent-.5);
                            double yDiff = Math.abs(yPercent-.5);
                            double percent = (xDiff<yDiff?xPercent:yPercent);
                            double weight = -Math.abs(2*percent-1)+1;
                            double[] aero = aeroCalc(point, direction, flatnessPenalty, sideWeight);
                            totalAerodynamicness+=aero[0]*aero[1]*weight*.25;//too lazy to properly weight each side's affect on the top, so I'll just average them
                            total+=aero[1]*weight*.25;
                        }
                    }
                }
            }
            totalAerodynamicness/=total;
            AerodynamicSettings as = new AerodynamicSettings(totalAerodynamicness);
            if(dir==0)cwAerodynamics = as;
            else ccwAerodynamics = as;
        }
    }
    private double[] aeroCalc(AerodynamicNetPoint point, Direction testDirection, double flatnessPenalty, double sideWeight){
        double angle = point.normal.angle(new Vector(testDirection.x, testDirection.y, testDirection.z))/Math.PI;//0 to 1, half circle
        double forwardness = 1-(Math.min(angle, 1-angle)*2);
        double forwardFactor = Math.max(point.sharpness, forwardness*point.sharpness);//Math.signum(point.sharpness)*Math.sqrt(Math.abs(point.sharpness)));
        double sideness = 1-forwardness;
        double sideFactorNoFlatness = sideness*(1-Math.abs(point.sharpness));
        double sideFactorWithFlatness = sideFactorNoFlatness*(1-point.flatness);
        double sideFactor = sideFactorNoFlatness+(sideFactorWithFlatness-sideFactorNoFlatness)*flatnessPenalty;
        double aerodynamicness = forwardFactor*forwardness*(1-sideWeight)+sideFactor*sideness*sideWeight;//Math.max(forwardFactor, sideFactor);
        point.forwardness.put(testDirection, forwardness);
        point.sideness.put(testDirection, sideness);
        point.angle.put(testDirection, angle);
        point.forwardFactor.put(testDirection, forwardFactor);
        point.sideFactor.put(testDirection, sideFactor);
        point.aerodynamicness.put(testDirection, aerodynamicness);
        double forwardWeight = ((1-angle)+1)/2;
//        double sharpnessWeight = Math.abs(point.sharpness);
        double caveWeight = Math.max(0.2, -point.sharpness)*5;
        double weight = forwardWeight*caveWeight;//*sharpnessWeight*caveWeight;
        return new double[]{aerodynamicness, weight};//(Math.abs(forwardness-.5)*2+1)/2
    }
}