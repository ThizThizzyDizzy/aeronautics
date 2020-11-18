package com.thizthizzydizzy.movecraft.option;
import com.thizthizzydizzy.movecraft.craft.CraftType;
import java.util.Objects;
public abstract class OptionBoolean extends Option<Boolean>{
    public OptionBoolean(String name, boolean global, boolean craft, Boolean defaultValue){
        super(name, global, craft, defaultValue);
    }
    public OptionBoolean(String name, boolean global, boolean craft, Boolean defaultValue, Object defaultConfigValue){
        super(name, global, craft, defaultValue, defaultConfigValue);
    }
    @Override
    public Boolean load(Object o){
        if(o instanceof String){
            Boolean.valueOf((String)o);
        }
        if(o instanceof Boolean){
            return (Boolean)o;
        }
        if(o instanceof Number){
            return ((Number) o).intValue()>=1;
        }
        return null;
    }
    @Override
    public Boolean get(CraftType craft){
        if(craftValues.containsKey(craft))return Objects.equals(craftValues.get(craft), true);
        return Objects.equals(globalValue, true)||Objects.equals(craftValues.get(craft), true);
    }
    /**
     * Checks if the global value is true.
     * @return <code>true</code> if the global value is <code>true</code>, or <code>false</code> otherwise
     */
    public boolean isTrue(){
        return Objects.equals(getValue(), true);
    }
}