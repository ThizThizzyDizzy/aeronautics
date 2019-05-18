package com.thizthizzydizzy.movecraft;
public enum Direction{
    NORTH(0,0,-1),
    SOUTH(0,0,1),
    EAST(1,0,0),
    WEST(-1,0,0),
    UP(0,1,0),
    DOWN(0,-1,0),
    NONE(0,0,0);
    public final int x;
    public final int y;
    public final int z;
    private Direction(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
}