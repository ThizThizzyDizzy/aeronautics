package com.thizthizzydizzy.movecraft;
import org.bukkit.block.BlockFace;
public enum Direction{
    NORTH(0,0,-1),
    SOUTH(0,0,1),
    EAST(1,0,0),
    WEST(-1,0,0),
    UP(0,1,0),
    DOWN(0,-1,0),
    NONE(0,0,0);
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
            switch(this){
                case NORTH:
                    return EAST;
                case EAST:
                    return SOUTH;
                case SOUTH:
                    return WEST;
                case WEST:
                    return NORTH;
            }
            return null;
        }
        switch(this){
            case NORTH:
                return WEST;
            case EAST:
                return NORTH;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
        }
        return null;
    }
    public Direction getVertical(int vertical){
        if(vertical>0)return UP;
        return DOWN;
    }
    @Override
    public String toString() {
        return name()+": "+x+" "+y+" "+z;
    }
}