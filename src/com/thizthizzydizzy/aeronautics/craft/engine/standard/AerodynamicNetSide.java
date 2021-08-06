package com.thizthizzydizzy.aeronautics.craft.engine.standard;
import com.thizthizzydizzy.aeronautics.Direction;
import java.util.ArrayList;
import org.bukkit.util.Vector;
public class AerodynamicNetSide{
    public AerodynamicNetPoint[][] net;
    public final Direction direction;
    public AerodynamicNetSide(Direction direction, int resolution, double xSize, double ySize, double zSize){
        net = new AerodynamicNetPoint[resolution+1][resolution+1];
        Direction theX = direction.getOpposite().get2DX();
        Direction theY = direction.getOpposite().get2DY();
        for(int x = 0; x<=resolution; x++){
            double X = (double)x/resolution*2-1;
            for(int y = 0; y<=resolution; y++){
                double Y = (double)y/resolution*2-1;
                double tX = theX.x*X+theY.x*Y+direction.x;
                double tY = theX.y*X+theY.y*Y+direction.y;
                double tZ = theX.z*X+theY.z*Y+direction.z;
                net[x][y] = new AerodynamicNetPoint(this, tX*xSize, tY*ySize, tZ*zSize);
            }
        }
        this.direction = direction;
    }
    public void blend(int blend){
        double[][] newX = new double[net.length][net[0].length];
        double[][] newY = new double[net.length][net[0].length];
        double[][] newZ = new double[net.length][net[0].length];
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                double theX = 0;
                double theY = 0;
                double theZ = 0;
                int total = 0;
                for(int X = x-blend; X<=x+blend; X++){
                    for(int Y = y-blend; Y<=y+blend; Y++){
                        if(X>=0&&Y>=0&&X<net.length&&Y<net[x].length){
                            theX+=net[X][Y].x;
                            theY+=net[X][Y].y;
                            theZ+=net[X][Y].z;
                            total++;
                        }
                    }
                }
                newX[x][y] = theX/total;
                newY[x][y] = theY/total;
                newZ[x][y] = theZ/total;
            }
        }
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                net[x][y].x = newX[x][y];
                net[x][y].y = newY[x][y];
                net[x][y].z = newZ[x][y];
            }
        }
    }
    public void calculateGeometricNormals(){
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                net[x][y].normal = new Vector(0, 0, 0);
            }
        }
        for(int x = 0; x<net.length-1; x++){
            for(int y = 0; y<net[x].length-1; y++){
                var a = net[x][y];
                var b = net[x][y+1];
                var c = net[x+1][y+1];
                var d = net[x+1][y];
                var ab = new Vector(b.x-a.x, b.y-a.y, b.z-a.z);
                var bc = new Vector(c.x-b.x, c.y-b.y, c.z-b.z);
                var cd = new Vector(d.x-c.x, d.y-c.y, d.z-c.z);
                var da = new Vector(a.x-d.x, a.y-d.y, a.z-d.z);
                Vector[] cross = new Vector[]{ab.crossProduct(bc),bc.crossProduct(cd),cd.crossProduct(da),da.crossProduct(ab)};
                for(Vector cros : cross){
                    a.normal.add(cros);
                    b.normal.add(cros);
                    c.normal.add(cros);
                    d.normal.add(cros);
                }
            }
        }
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                net[x][y].normal.normalize();
            }
        }
    }
    public void calculateNormals(){
        calculateGeometricNormals();
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                ArrayList<Vector> adjacent = new ArrayList<>();
                if(x>0)                 adjacent.add(new Vector(net[x][y].x-net[x-1][y].x,net[x][y].y-net[x-1][y].y,net[x][y].z-net[x-1][y].z).normalize());
                else                    adjacent.add(new Vector(net[x][y].x-net[x+1][y].x,net[x][y].y-net[x+1][y].y,net[x][y].z-net[x+1][y].z).multiply(-1).normalize());
                if(y>0)                 adjacent.add(new Vector(net[x][y].x-net[x][y-1].x,net[x][y].y-net[x][y-1].y,net[x][y].z-net[x][y-1].z).normalize());
                else                    adjacent.add(new Vector(net[x][y].x-net[x][y+1].x,net[x][y].y-net[x][y+1].y,net[x][y].z-net[x][y+1].z).multiply(-1).normalize());
                if(x<net.length-1)      adjacent.add(new Vector(net[x][y].x-net[x+1][y].x,net[x][y].y-net[x+1][y].y,net[x][y].z-net[x+1][y].z).normalize());
                else                    adjacent.add(new Vector(net[x][y].x-net[x-1][y].x,net[x][y].y-net[x-1][y].y,net[x][y].z-net[x-1][y].z).multiply(-1).normalize());
                if(y<net[x].length-1)   adjacent.add(new Vector(net[x][y].x-net[x][y+1].x,net[x][y].y-net[x][y+1].y,net[x][y].z-net[x][y+1].z).normalize());
                else                    adjacent.add(new Vector(net[x][y].x-net[x][y-1].x,net[x][y].y-net[x][y-1].y,net[x][y].z-net[x][y-1].z).multiply(-1).normalize());
                Vector geometricNormal = net[x][y].normal;
                net[x][y].normal = new Vector();
                for(Vector v : adjacent){
                    net[x][y].normal.add(v);
                }
                if(net[x][y].normal.length()<.01)net[x][y].normal = geometricNormal;
                net[x][y].normal.normalize();
                double ang = net[x][y].normal.angle(geometricNormal);
                if(ang>Math.PI/4){
                    if(ang<Math.PI/2){
                        double fac = (ang-Math.PI/4)/(Math.PI/4);
                        net[x][y].normal.multiply(1-fac);
                        geometricNormal.multiply(fac);
                        net[x][y].normal.add(geometricNormal).normalize();
                    }else net[x][y].normal = geometricNormal;
                }
            }
        }
    }
    public void inflate(double distance){
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                net[x][y].x+=net[x][y].normal.getX()*distance;
                net[x][y].y+=net[x][y].normal.getY()*distance;
                net[x][y].z+=net[x][y].normal.getZ()*distance;
            }
        }
    }
    public void calculateSharpness(double flatnessPow){
        for(int x = 0; x<net.length; x++){
            for(int y = 0; y<net[x].length; y++){
                ArrayList<AerodynamicNetPoint> adjacent = new ArrayList<>();
                if(x>0)adjacent.add(net[x-1][y]);
                if(y>0)adjacent.add(net[x][y-1]);
                if(x<net.length-1)adjacent.add(net[x+1][y]);
                if(y<net[x].length-1)adjacent.add(net[x][y+1]);
                double angle = 0;
                for(AerodynamicNetPoint adj : adjacent){
                    angle+=net[x][y].normal.angle(new Vector(adj.x-net[x][y].x,adj.y-net[x][y].y,adj.z-net[x][y].z).normalize());//angle between this normal and the vector to that
                }
                angle/=adjacent.size();
                //if angle = 0, sharpness = -1
                //if angle = pi/2, sharpness = 0
                //if angle = pi/4, sharpness = 11
                net[x][y].sharpness = (angle/(Math.PI/2)-1);
                net[x][y].flatness = Math.pow(1-Math.abs(net[x][y].sharpness), flatnessPow);
            }
        }
    }
    private Vector pow(Vector v, double pow){
        return v.multiply(Math.pow(v.length(), pow)/v.length());
    }
}