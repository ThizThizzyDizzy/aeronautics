package com.thizthizzydizzy.aeronautics;
import java.util.ArrayList;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
public enum Direction{
    NORTH(0,0,-1),
    SOUTH(0,0,1),
    EAST(1,0,0),
    WEST(-1,0,0),
    UP(0,1,0),
    DOWN(0,-1,0),
    NONE(0,0,0);
    public static final ArrayList<Direction> LATERAL = new ArrayList<>();
    public static final ArrayList<Direction> VERTICAL = new ArrayList<>();
    public static final ArrayList<Direction> NONZERO = new ArrayList<>();
    static{
        LATERAL.add(NORTH);
        LATERAL.add(SOUTH);
        LATERAL.add(EAST);
        LATERAL.add(WEST);
        VERTICAL.add(UP);
        VERTICAL.add(DOWN);
        NONZERO.addAll(LATERAL);
        NONZERO.addAll(VERTICAL);
    }
    public static Direction fromBlockFace(BlockFace face){
        switch(face){
            case NORTH:
                return NORTH;
            case SOUTH:
                return SOUTH;
            case EAST:
                return EAST;
            case WEST:
                return WEST;
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            case SELF:
                return NONE;
        }
        return null;
    }
    public static Direction fromAxis(Axis axis){
        return fromAxis(axis, false);
    }
    public static Direction fromAxis(Axis axis, boolean negative){
        return switch(axis){
            case X -> EAST;
            case Y -> UP;
            case Z -> SOUTH;
        };
    }
    public final int x;
    public final int y;
    public final int z;
    private Direction(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Direction getLateral(int lateral){
        if(lateral>0){
            return switch(this){
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
                default -> null;
            };
        }
        return switch(this){
            case NORTH -> WEST;
            case EAST -> NORTH;
            case SOUTH -> EAST;
            case WEST -> SOUTH;
            default -> null;
        };
    }
    public Direction getVertical(int vertical){
        if(vertical>0)return UP;
        return DOWN;
    }
    @Override
    public String toString() {
        return name()+": "+x+" "+y+" "+z;
    }
    public boolean matches(BlockFace facing){
        return facing.getModX()==x&&facing.getModY()==y&&facing.getModZ()==z;
    }
    public boolean matches(Axis axis){
        return switch(this){
            case DOWN, UP -> axis==Axis.Y;
            case EAST, WEST -> axis==Axis.X;
            case NORTH, SOUTH -> axis==Axis.Z;
            default -> false;
        };
    }
    /**
     * Gets the number of clockwise 90-degree rotations required to reach newFacing
     * @param newFacing the new direction
     * @return the number of rotations required, from -3 to 3
     */
    public int getRotation(Direction newFacing){
        int thisRot;
        switch(this){
            case NORTH -> thisRot = 0;
            case EAST -> thisRot = 1;
            case SOUTH -> thisRot = 2;
            case WEST -> thisRot = 3;
            default -> throw new IllegalArgumentException("this is not a lateral direction!");
        }
        int thatRot;
        switch(newFacing){
            case NORTH -> thatRot = 0;
            case EAST -> thatRot = 1;
            case SOUTH -> thatRot = 2;
            case WEST -> thatRot = 3;
            default -> throw new IllegalArgumentException("newFacing is not a lateral direction!");
        }
        return thatRot-thisRot;
    }
    public Direction getLeft(){
        switch(this){
            case NORTH:
                return WEST;
            case WEST:
                return SOUTH;
            case SOUTH:
                return EAST;
            case EAST:
                return NORTH;
            default:
                throw new IllegalArgumentException("this is not a lateral direction!");
        }
    }
    public Direction getRight(){
        switch(this){
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            default:
                throw new IllegalArgumentException("this is not a lateral direction!");
        }
    }
    public BlockFace toBlockFace(){
        return switch (this) {
            case DOWN -> BlockFace.DOWN;
            case EAST -> BlockFace.EAST;
            case NONE -> BlockFace.SELF;
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case UP -> BlockFace.UP;
            case WEST -> BlockFace.WEST;
        };
    }
    public Direction getOpposite(){
        return switch (this) {
            case NORTH -> SOUTH;
            case EAST -> WEST;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case UP -> DOWN;
            case DOWN -> UP;
            case NONE -> NONE;
        };
    }
    public Direction get2DX(){ //where x goes RIGHT
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            case UP -> EAST;
            case DOWN -> EAST;
            case NONE -> NONE;
        };
    }
    public Direction get2DY(){ //where y goes DOWN
        return switch (this) {
            case NORTH, EAST, SOUTH, WEST -> DOWN;
            case UP -> NORTH;
            case DOWN -> SOUTH;
            case NONE -> NONE;
        };
    }
    public boolean isVertical(){
        return y!=0;
    }
    public Vector toVector(){
        return new Vector(x, y, z);
    }
}