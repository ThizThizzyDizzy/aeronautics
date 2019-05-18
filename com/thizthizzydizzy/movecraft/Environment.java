package com.thizthizzydizzy.movecraft;
public enum Environment{
    AIR,UNDERWATER,WATER,LAND;
    public static Environment match(String str){
        return valueOf(str.toUpperCase().replace(" ", "_").replace("_", "-").replace("-", ""));
    }
}